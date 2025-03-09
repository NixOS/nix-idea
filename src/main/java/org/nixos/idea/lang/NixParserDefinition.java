package org.nixos.idea.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;
import org.nixos.idea.psi.NixTokenSets;
import org.nixos.idea.psi.NixTokenType;
import org.nixos.idea.psi.NixTypes;

public class NixParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(NixLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new NixLexer();
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return NixTokenSets.WHITE_SPACES;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return NixTokenSets.COMMENTS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return NixTokenSets.STRING_LITERALS;
    }

    @Override
    public @NotNull PsiParser createParser(final Project project) {
        return new NixParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new NixFile(viewProvider);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        NixTokenType leftType = asNixTokenType(left.getElementType());
        NixTokenType rightType = asNixTokenType(right.getElementType());
        if (leftType == NixTypes.SCOMMENT) {
            return SpaceRequirements.MUST_LINE_BREAK;
        }
        if (leftType == NixTypes.DOLLAR && rightType == NixTypes.LCURLY) {
            return SpaceRequirements.MUST_NOT;
        }
        else if (leftType == NixTypes.PATH_SEGMENT) {
            // path segment, antiquotation or PATH_END on the right
            return SpaceRequirements.MUST_NOT;
        }
        else if (rightType == NixTypes.PATH_END) {
            // path segment or antiquotation on the left
            return SpaceRequirements.MUST_NOT;
        }
        else if (NixTokenSets.MIGHT_COLLAPSE_WITH_ID.contains(leftType) &&
                 NixTokenSets.MIGHT_COLLAPSE_WITH_ID.contains(rightType)) {
            return SpaceRequirements.MUST;
        }
        else {
            return SpaceRequirements.MAY;
        }
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        return NixTypes.Factory.createElement(node);
    }

    private static @Nullable NixTokenType asNixTokenType(IElementType elementType) {
        return elementType instanceof NixTokenType ? (NixTokenType) elementType : null;
    }
}

