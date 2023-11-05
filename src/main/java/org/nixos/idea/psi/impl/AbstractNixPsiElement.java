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
import org.nixos.idea.interpretation.Declaration;
import org.nixos.idea.interpretation.Scope;
import org.nixos.idea.interpretation.VariableUsage;
import org.nixos.idea.lang.navigation.reference.NixAttributeReference;
import org.nixos.idea.lang.navigation.reference.NixScopeReference;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixAttrPath;
import org.nixos.idea.psi.NixBindInherit;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprSelect;
import org.nixos.idea.psi.NixInheritedName;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.psi.NixVariableAccess;
import org.nixos.idea.util.MergeFunctions;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

abstract class AbstractNixPsiElement extends ASTWrapperPsiElement implements NixPsiElement {

    private static final Key<CachedValue<Scope>> KEY_SCOPE = Key.create("AbstractNixPsiElement.scope");

    private @Nullable AttributeMap<Collection<Declaration>> myDeclarations;
    private @Nullable AttributeMap<Collection<VariableUsage>> myUsages;

    AbstractNixPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull AttributeMap<Collection<Declaration>> getDeclarations() {
        if (myDeclarations == null) {
            AttributeMap.Builder<Collection<Declaration>> builder = AttributeMap.builder();
            Collection<Declaration> declarationList = Declaration.allOf(this);
            if (declarationList != null) {
                for (Declaration declaration : declarationList) {
                    builder.merge(declaration.path(), List.of(declaration), MergeFunctions::mergeLists);
                }
            }
            MY_DECLARATIONS.compareAndSet(this, null, builder.build());
        }
        return Objects.requireNonNull(myDeclarations);
    }

    @Override
    public @NotNull Scope getScope() {
        return CachedValuesManager.getCachedValue(this, KEY_SCOPE, () -> {
            PsiElement parent = getParent();
            Scope parentScope = Scope.EMPTY;
            if (parent instanceof NixPsiElement) {
                parentScope = ((NixPsiElement) parent).getScope();
            }
            Scope result = parentScope.subScope(this, getDeclarations());
            return CachedValueProvider.Result.create(result, this);
        });
    }

    @Override
    public @NotNull AttributeMap<Collection<VariableUsage>> getUsages() {
        if (myUsages == null) {
            AttributeMap.Builder<Collection<VariableUsage>> builder = AttributeMap.builder();
            VariableUsage usage = VariableUsage.by(this);
            if (usage != null) {
                builder.set(usage.path(), List.of(usage));
            } else {
                for (PsiElement child : getChildren()) {
                    if (child instanceof NixPsiElement) {
                        builder.merge(((NixPsiElement) child).getUsages(), MergeFunctions::mergeLists);
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
        myDeclarations = null;
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
