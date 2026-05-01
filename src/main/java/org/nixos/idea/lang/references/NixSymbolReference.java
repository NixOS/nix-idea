package org.nixos.idea.lang.references;

import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.references.symbol.NixSymbol;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.util.TextRangeFactory;

@SuppressWarnings("UnstableApiUsage")
public abstract class NixSymbolReference implements PsiSymbolReference {

    protected final @NotNull NixPsiElement myElement;
    protected final @NotNull NixPsiElement myIdentifier;
    protected final @NotNull String myName;

    protected NixSymbolReference(@NotNull NixPsiElement element, @NotNull NixPsiElement identifier, @NotNull String name) {
        myElement = element;
        myIdentifier = identifier;
        myName = name;
    }

    public @NotNull NixPsiElement getIdentifier() {
        return myIdentifier;
    }

    @Override
    public @NotNull PsiElement getElement() {
        return myElement;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return TextRangeFactory.relative(myIdentifier, myElement);
    }

    @Override
    public boolean resolvesTo(@NotNull Symbol target) {
        // Check name as a shortcut to avoid resolving the reference when it cannot match anyway.
        return target instanceof NixSymbol t &&
                myName.equals(t.getName()) &&
                PsiSymbolReference.super.resolvesTo(target);
    }
}
