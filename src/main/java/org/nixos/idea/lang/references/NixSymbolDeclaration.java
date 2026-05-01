package org.nixos.idea.lang.references;

import com.intellij.model.psi.PsiSymbolDeclaration;
import com.intellij.openapi.util.TextRange;
import com.intellij.platform.backend.navigation.NavigationTarget;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.references.symbol.NixUserSymbol;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.util.TextRangeFactory;

@SuppressWarnings("UnstableApiUsage")
public final class NixSymbolDeclaration implements PsiSymbolDeclaration {

    private final @NotNull NixPsiElement myDeclarationElement;
    private final @NotNull NixPsiElement myIdentifier;
    private final @NotNull NixUserSymbol mySymbol;
    private final @NotNull String myDeclarationElementName;
    private final @Nullable String myDeclarationElementType;

    public NixSymbolDeclaration(@NotNull NixPsiElement declarationElement, @NotNull NixPsiElement identifier,
                                @NotNull NixUserSymbol symbol,
                                @NotNull String declarationElementName, @Nullable String declarationElementType) {
        myDeclarationElement = declarationElement;
        myIdentifier = identifier;
        mySymbol = symbol;
        myDeclarationElementName = declarationElementName;
        myDeclarationElementType = declarationElementType;
    }

    public @NotNull NixPsiElement getIdentifier() {
        return myIdentifier;
    }

    public @NotNull NavigationTarget navigationTarget() {
        return new NixNavigationTarget(myIdentifier, TargetPresentation.builder(mySymbol.presentation())
                .presentableText(myDeclarationElementName)
                .containerText(myDeclarationElementType)
                .presentation());
    }

    @Override
    public @NotNull NixPsiElement getDeclaringElement() {
        return myDeclarationElement;
    }

    @Override
    public @NotNull TextRange getRangeInDeclaringElement() {
        return TextRangeFactory.relative(myIdentifier, myDeclarationElement);
    }

    @Override
    public @NotNull NixUserSymbol getSymbol() {
        return mySymbol;
    }
}
