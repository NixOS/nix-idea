package org.nixos.idea.lang;

import org.nixos.idea.file.NixFile;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.nixos.idea.lang.NixParser;
import org.nixos.idea.psi.NixTypes;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;

public class NixParserDefinition implements ParserDefinition {
    public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(NixTypes.SCOMMENT,NixTypes.MCOMMENT);
    public static final TokenSet STRING_LITERALS = TokenSet.create(NixTypes.LITERAL_SIMPLE_STRING, NixTypes.DOC_STRING);

    public static final IFileElementType FILE = new IFileElementType(Language.<NixLanguage>findInstance(NixLanguage.class));

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        NixLexer lxr = new NixLexer((Reader) null);
        return new FlexAdapter(lxr);
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return WHITE_SPACES;
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return STRING_LITERALS;
    }

    @NotNull
    public PsiParser createParser(final Project project) {
        return new NixParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return FILE;
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new NixFile(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        return NixTypes.Factory.createElement(node);
    }
}

