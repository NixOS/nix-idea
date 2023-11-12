package org.nixos.idea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.Attribute;
import org.nixos.idea.interpretation.AttributeMap;
import org.nixos.idea.interpretation.AttributePath;
import org.nixos.idea.interpretation.VariableUsage;
import org.nixos.idea.lang.navigation.reference.NixAttributeReference;
import org.nixos.idea.lang.navigation.reference.NixScopeReference;
import org.nixos.idea.lang.navigation.scope.Scope;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixAttrPath;
import org.nixos.idea.psi.NixBindInherit;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprSelect;
import org.nixos.idea.psi.NixInheritedName;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.psi.NixVariableAccess;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

abstract class AbstractNixPsiElement extends ASTWrapperPsiElement implements NixPsiElement {

    private static final Key<CachedValue<Scope>> KEY_SCOPE = Key.create("AbstractNixPsiElement.scope");

    private @Nullable AttributeMap<VariableUsage> myUsages;

    AbstractNixPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Scope getScope() {
        return CachedValuesManager.getCachedValue(this, KEY_SCOPE, () -> {
            Scope parentScope = getParent() instanceof NixPsiElement parent ? parent.getScope() : Scope.EMPTY;
            Scope result = parentScope.subScope(this);
            return CachedValueProvider.Result.create(result, this);
        });
    }

    @Override
    public @NotNull AttributeMap<VariableUsage> getUsages() {
        // TODO: 12.11.2023 Align with new implementation
        if (myUsages == null) {
            AttributeMap.Builder<VariableUsage> builder = AttributeMap.builder();
            VariableUsage usage = VariableUsage.by(this);
            if (usage != null) {
                builder.add(usage.path(), usage);
            } else {
                for (PsiElement child : getChildren()) {
                    if (child instanceof NixPsiElement) {
                        builder.merge(((NixPsiElement) child).getUsages());
                    }
                }
            }
            MY_USAGES.compareAndSet(this, null, builder.build());
        }
        return Objects.requireNonNull(myUsages);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Collection<? extends @NotNull PsiSymbolReference> getOwnReferences() {
        if (this instanceof NixVariableAccess) {
            return List.of(new NixScopeReference(this, getText()));
        } else if (this instanceof NixExprSelect) {
            NixExprSelect typedThis = (NixExprSelect) this;
            List<NixAttributeReference> result = new ArrayList<>();
            NixAttrPath attrPath = typedThis.getAttrPath();
            if (attrPath != null) {
                AttributePath.Builder pathBuilder = AttributePath.builder();
                for (NixAttr attr : attrPath.getAttrList()) {
                    pathBuilder.add(Attribute.of(attr));
                    result.add(new NixAttributeReference(this, typedThis.getValue(), pathBuilder.build(), attr));
                }
            }
            return List.copyOf(result);
        } else if (this instanceof NixInheritedName) {
            NixInheritedName typedThis = (NixInheritedName) this;
            NixBindInherit parent = (NixBindInherit) getParent();
            NixExpr accessedObject = parent.getExpr();
            if (accessedObject == null) {
                String variableName = Attribute.of(typedThis.getAttr()).getName();
                return variableName == null ? List.of() : List.of(new NixScopeReference(this, variableName));
            } else {
                NixAttr attr = typedThis.getAttr();
                AttributePath path = AttributePath.of(Attribute.of(attr));
                return List.of(new NixAttributeReference(this, accessedObject, path, attr));
            }
        } else {
            return List.of();
        }
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        myUsages = null;
    }

    // VarHandle mechanics
    private static final VarHandle MY_DECLARATIONS;
    private static final VarHandle MY_USAGES;
    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            MY_DECLARATIONS = l.findVarHandle(AbstractNixPsiElement.class, "myDeclarations", AttributeMap.class);
            MY_USAGES = l.findVarHandle(AbstractNixPsiElement.class, "myUsages", AttributeMap.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
