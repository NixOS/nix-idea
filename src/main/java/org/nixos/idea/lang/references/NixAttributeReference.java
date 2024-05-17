package org.nixos.idea.lang.references;

import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.references.symbol.NixSymbol;
import org.nixos.idea.psi.NixAttr;
import org.nixos.idea.psi.NixPsiElement;

import java.util.Collection;

public final class NixAttributeReference extends NixSymbolReference {

    private final @NotNull NixSymbolResolver myParentResolver;

    public NixAttributeReference(@NotNull NixPsiElement owner,
                                 @NotNull NixAttr attribute,
                                 @NotNull String attributeName,
                                 @NotNull NixSymbolResolver parentResolver) {
        super(owner, attribute, attributeName);
        myParentResolver = parentResolver;
    }

    @Override
    public @NotNull Collection<NixSymbol> resolveReference() {
        return myParentResolver.resolve(myName);
    }
}
