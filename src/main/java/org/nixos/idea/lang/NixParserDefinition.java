package org.nixos.idea.lang;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.file.NixFile;
import org.nixos.idea.psi.NixTokenType;
import org.nixos.idea.psi.NixTypeUtil;
import org.nixos.idea.psi.NixTypes;

public class NixParserDefinition implements ParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(NixTypes.SCOMMENT,NixTypes.MCOMMENT);
    public static final TokenSet STRING_LITERALS = TokenSet.create(NixTypes.STD_STRING, NixTypes.IND_STRING);

    public static final IFileElementType FILE = new IFileElementType(Language.<NixLanguage>findInstance(NixLanguage.class));

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new NixLexer();
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return STRING_LITERALS;
    }

    @NotNull
    @Override
    public PsiParser createParser(final Project project) {
        return new NixParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new NixFile(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        NixTokenType leftType = asNixTokenType(left.getElementType());
        NixTokenType rightType = asNixTokenType(right.getElementType());
        if (leftType == NixTypes.SCOMMENT) {
            return SpaceRequirements.MUST_LINE_BREAK;
        }
        if (leftType == NixTypes.DOLLAR && rightType == NixTypes.LCURLY) {
            return SpaceRequirements.MUST_NOT;
        }
        else if (NixTypeUtil.MIGHT_COLLAPSE_WITH_ID.contains(leftType) &&
                 NixTypeUtil.MIGHT_COLLAPSE_WITH_ID.contains(rightType)) {
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

