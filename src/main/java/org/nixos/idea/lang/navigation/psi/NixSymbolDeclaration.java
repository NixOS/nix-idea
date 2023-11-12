package org.nixos.idea.lang.navigation.psi;

import com.intellij.model.psi.PsiSymbolDeclaration;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.navigation.symbol.NixSymbol;
import org.nixos.idea.psi.NixDeclarationElement;

@SuppressWarnings("UnstableApiUsage")
public final class NixSymbolDeclaration implements PsiSymbolDeclaration {

    private final @NotNull NixDeclarationElement myDeclarationElement;
    private final @NotNull TextRange myTextRange;
    private final @NotNull NixSymbol mySymbol;

    public NixSymbolDeclaration(@NotNull NixDeclarationElement myDeclarationElement, @NotNull TextRange myTextRange, @NotNull NixSymbol mySymbol) {
        this.myDeclarationElement = myDeclarationElement;
        this.myTextRange = myTextRange;
        this.mySymbol = mySymbol;
    }

    @Override
    public @NotNull NixDeclarationElement getDeclaringElement() {
        return myDeclarationElement;
    }

    @Override
    public @NotNull TextRange getRangeInDeclaringElement() {
        return myTextRange;
    }

    @Override
    public @NotNull NixSymbol getSymbol() {
        return mySymbol;
    }
}
