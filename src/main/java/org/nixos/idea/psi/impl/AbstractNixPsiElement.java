package org.nixos.idea.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.references.NixScopeReference;
import org.nixos.idea.lang.references.NixSymbolDeclaration;
import org.nixos.idea.lang.references.NixSymbolReference;
import org.nixos.idea.lang.references.Scope;
import org.nixos.idea.psi.NixBindInherit;
import org.nixos.idea.psi.NixExpr;
import org.nixos.idea.psi.NixExprSelect;
import org.nixos.idea.psi.NixExprVar;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.psi.NixPsiUtil;
import org.nixos.idea.settings.NixSymbolSettings;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

abstract class AbstractNixPsiElement extends ASTWrapperPsiElement implements NixPsiElement {

    private static final Key<CachedValue<AbstractNixDeclarationHost>> KEY_DECLARATION_HOST = Key.create("AbstractNixPsiElement.declarationHost");
    private static final Key<CachedValue<Scope>> KEY_SCOPE = Key.create("AbstractNixPsiElement.scope");

    AbstractNixPsiElement(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public final @NotNull Scope getScope() {
        return CachedValuesManager.getCachedValue(this, KEY_SCOPE, () -> {
            Scope parentScope = getParent() instanceof NixPsiElement parent ? parent.getScope() : Scope.root();
            Scope result = Scope.subScope(parentScope, this);
            return CachedValueProvider.Result.create(result, this);
        });
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public final @NotNull Collection<? extends NixSymbolDeclaration> getOwnDeclarations() {
        return AbstractNixDeclarationHost.getDeclarations(this);
    }

    final @Nullable AbstractNixDeclarationHost getDeclarationHost() {
        return CachedValuesManager.getCachedValue(this, KEY_DECLARATION_HOST, () -> {
            AbstractNixDeclarationHost result = this instanceof AbstractNixDeclarationHost host ? host
                    : getParent() instanceof AbstractNixPsiElement parent ? parent.getDeclarationHost()
                    : null;
            return CachedValueProvider.Result.create(result, this);
        });
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public final @NotNull Collection<? extends NixSymbolReference> getOwnReferences() {
        if (!NixSymbolSettings.getInstance().getEnabled()) {
            return List.of();
        } else if (this instanceof NixExprVar) {
            return List.of(new NixScopeReference(this, this, getText()));
        } else if (this instanceof NixExprSelect) {
            // TODO: Attribute reference support
            return List.of();
        } else if (this instanceof NixBindInherit bindInherit) {
            NixExpr accessedObject = bindInherit.getSource();
            if (accessedObject == null) {
                return bindInherit.getAttributes().stream().flatMap(attr -> {
                    String variableName = NixPsiUtil.getAttributeName(attr);
                    if (variableName == null) {
                        return Stream.empty();
                    } else {
                        return Stream.of(new NixScopeReference(this, attr, variableName));
                    }
                }).toList();
            } else {
                // TODO: Attribute reference support
                return List.of();
            }
        } else {
            return List.of();
        }
    }

}
