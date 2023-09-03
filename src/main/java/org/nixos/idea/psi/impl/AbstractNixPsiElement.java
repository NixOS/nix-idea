package org.nixos.idea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.interpretation.AttributeMap;
import org.nixos.idea.interpretation.Declaration;
import org.nixos.idea.interpretation.Scope;
import org.nixos.idea.interpretation.VariableUsage;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.util.MergeFunctions;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

abstract class AbstractNixPsiElement extends ASTWrapperPsiElement implements NixPsiElement {

    private static final Key<CachedValue<Scope>> KEY_SCOPE = Key.create("AbstractNixPsiElement.scope");

    private @Nullable AttributeMap<Collection<Declaration>> declarations;
    private @Nullable AttributeMap<Collection<VariableUsage>> usages;

    AbstractNixPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull AttributeMap<Collection<Declaration>> getDeclarations() {
        if (declarations == null) {
            AttributeMap.Builder<Collection<Declaration>> builder = AttributeMap.builder();
            Collection<Declaration> declarationList = Declaration.allOf(this);
            if (declarationList != null) {
                for (Declaration declaration : declarationList) {
                    builder.merge(declaration.path(), List.of(declaration), MergeFunctions::mergeLists);
                }
            }
            DECLARATIONS.compareAndSet(this, null, builder.build());
        }
        return Objects.requireNonNull(declarations);
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
        if (usages == null) {
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
            USAGES.compareAndSet(this, null, builder.build());
        }
        return Objects.requireNonNull(usages);
    }

    @Override
    public void subtreeChanged() {
        super.subtreeChanged();
        declarations = null;
        usages = null;
    }

    // VarHandle mechanics
    private static final VarHandle DECLARATIONS;
    private static final VarHandle USAGES;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            DECLARATIONS = l.findVarHandle(AbstractNixPsiElement.class, "declarations", AttributeMap.class);
            USAGES = l.findVarHandle(AbstractNixPsiElement.class, "usages", AttributeMap.class);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

}
