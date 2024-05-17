package org.nixos.idea.lang.references;

import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.references.symbol.NixSymbol;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.util.TextRangeFactory;

import java.util.Collection;

@SuppressWarnings("UnstableApiUsage")
public abstract class NixSymbolReference implements PsiSymbolReference {

    protected final @NotNull NixPsiElement myOwner;
    protected final @NotNull NixPsiElement myIdentifier;
    protected final @NotNull String myName;

    protected NixSymbolReference(@NotNull NixPsiElement owner, @NotNull NixPsiElement identifier, @NotNull String name) {
        myOwner = owner;
        myIdentifier = identifier;
        myName = name;
    }

    public @NotNull NixPsiElement getIdentifier() {
        return myIdentifier;
    }

    @Override
    public @NotNull PsiElement getElement() {
        return myOwner;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return TextRangeFactory.relative(myIdentifier, myOwner);
    }

    @Override
    public abstract @NotNull Collection<NixSymbol> resolveReference();

    @Override
    public boolean resolvesTo(@NotNull Symbol target) {
        // Check name as a shortcut to avoid resolving the reference when it cannot match anyway.
        return target instanceof NixSymbol t &&
                myName.equals(t.getName()) &&
                PsiSymbolReference.super.resolvesTo(target);
    }
}
