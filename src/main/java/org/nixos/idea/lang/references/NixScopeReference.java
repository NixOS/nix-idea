package org.nixos.idea.lang.references;

import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.references.symbol.NixSymbol;
import org.nixos.idea.psi.NixPsiElement;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public final class NixScopeReference extends NixSymbolReference {

    public NixScopeReference(@NotNull NixPsiElement owner, @NotNull NixPsiElement identifier, @NotNull String variableName) {
        super(owner, identifier, variableName);
    }

    @Override
    public @NotNull Collection<NixSymbol> resolveReference() {
        return myOwner.getScope().resolveVariable(myName);
    }
}
