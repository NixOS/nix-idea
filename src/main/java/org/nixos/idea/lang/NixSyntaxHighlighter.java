package org.nixos.idea.lang;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixTypes;

import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;
import static java.util.Map.entry;

public class NixSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("NIX_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("NIX_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);

    public static final TextAttributesKey SEMICOLON =
            createTextAttributesKey("NIX_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey COMMA =
            createTextAttributesKey("NIX_COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey DOT =
            createTextAttributesKey("NIX_DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey ASSIGN =
            createTextAttributesKey("NIX_ASSIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey COLON =
            createTextAttributesKey("NIX_COLON", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey AT =
            createTextAttributesKey("NIX_AT", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey ELLIPSIS =
            createTextAttributesKey("NIX_ELLIPSIS", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey OPERATION_SIGN =
            createTextAttributesKey("NIX_OPERATION_SIGN", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey PARENTHESES =
            createTextAttributesKey("NIX_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey BRACES =
            createTextAttributesKey("NIX_BRACES", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey BRACKETS =
            createTextAttributesKey("NIX_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);

    public static final TextAttributesKey STRING =
            createTextAttributesKey("NIX_STRING", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey STRING_ESCAPE =
            createTextAttributesKey("NIX_STRING_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey URI =
            createTextAttributesKey("NIX_URI", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey PATH =
            createTextAttributesKey("NIX_PATH", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey NUMBER =
            createTextAttributesKey("NIX_NUMBER", DefaultLanguageHighlighterColors.NUMBER);

    public static final TextAttributesKey LINE_COMMENT =
            createTextAttributesKey("NIX_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey BLOCK_COMMENT =
            createTextAttributesKey("NIX_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);

    private static final Map<IElementType, TextAttributesKey> TOKEN_MAP = Map.<IElementType, TextAttributesKey>ofEntries(
            // Keywords
            entry(NixTypes.IF, KEYWORD),
            entry(NixTypes.THEN, KEYWORD),
            entry(NixTypes.ELSE, KEYWORD),
            entry(NixTypes.ASSERT, KEYWORD),
            entry(NixTypes.WITH, KEYWORD),
            entry(NixTypes.LET, KEYWORD),
            entry(NixTypes.IN, KEYWORD),
            entry(NixTypes.REC, KEYWORD),
            entry(NixTypes.INHERIT, KEYWORD),
            entry(NixTypes.OR_KW, KEYWORD),
            // Identifiers
            entry(NixTypes.ID, IDENTIFIER),
            // Operators
            entry(NixTypes.ASSIGN, ASSIGN),
            entry(NixTypes.COLON, COLON),
            entry(NixTypes.SEMI, SEMICOLON),
            entry(NixTypes.COMMA, COMMA),
            entry(NixTypes.DOT, DOT),
            entry(NixTypes.ELLIPSIS, ELLIPSIS),
            entry(NixTypes.AT, AT),
            entry(NixTypes.HAS, OPERATION_SIGN),
            entry(NixTypes.NOT, OPERATION_SIGN),
            entry(NixTypes.TIMES, OPERATION_SIGN),
            entry(NixTypes.DIVIDE, OPERATION_SIGN),
            entry(NixTypes.PLUS, OPERATION_SIGN),
            entry(NixTypes.MINUS, OPERATION_SIGN),
            entry(NixTypes.LT, OPERATION_SIGN),
            entry(NixTypes.GT, OPERATION_SIGN),
            entry(NixTypes.CONCAT, OPERATION_SIGN),
            entry(NixTypes.UPDATE, OPERATION_SIGN),
            entry(NixTypes.LEQ, OPERATION_SIGN),
            entry(NixTypes.GEQ, OPERATION_SIGN),
            entry(NixTypes.EQ, OPERATION_SIGN),
            entry(NixTypes.NEQ, OPERATION_SIGN),
            entry(NixTypes.AND, OPERATION_SIGN),
            entry(NixTypes.OR, OPERATION_SIGN),
            entry(NixTypes.IMPL, OPERATION_SIGN),
            // Parentheses
            entry(NixTypes.LPAREN, PARENTHESES),
            entry(NixTypes.RPAREN, PARENTHESES),
            entry(NixTypes.LBRAC, BRACKETS),
            entry(NixTypes.RBRAC, BRACKETS),
            entry(NixTypes.LCURLY, BRACES),
            entry(NixTypes.RCURLY, BRACES),
            entry(NixTypes.DOLLAR, BRACES),
            // Literals
            entry(NixTypes.INT, NUMBER),
            entry(NixTypes.FLOAT, NUMBER),
            entry(NixTypes.PATH, PATH),
            entry(NixTypes.HPATH, PATH),
            entry(NixTypes.SPATH, PATH),
            entry(NixTypes.URI, URI),
            // String literals
            entry(NixTypes.STR, STRING),
            entry(NixTypes.STRING_CLOSE, STRING),
            entry(NixTypes.STRING_OPEN, STRING),
            entry(NixTypes.IND_STR, STRING),
            entry(NixTypes.IND_STRING_CLOSE, STRING),
            entry(NixTypes.IND_STRING_OPEN, STRING),
            entry(NixTypes.STR_ESCAPE, STRING_ESCAPE),
            entry(NixTypes.IND_STR_ESCAPE, STRING_ESCAPE),
            // Other
            entry(NixTypes.SCOMMENT, LINE_COMMENT),
            entry(NixTypes.MCOMMENT, BLOCK_COMMENT),
            entry(TokenType.BAD_CHARACTER, HighlighterColors.BAD_CHARACTER));

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new NixLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return pack(TOKEN_MAP.get(tokenType));
    }
}
