package org.nixos.idea.lang.highlighter;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.lang.NixLexer;
import org.nixos.idea.psi.NixTypes;

import java.util.Map;

import static java.util.Map.entry;

public class NixSyntaxHighlighter extends SyntaxHighlighterBase {

    private static final Map<IElementType, TextAttributesKey> TOKEN_MAP = Map.<IElementType, TextAttributesKey>ofEntries(
            // Keywords
            entry(NixTypes.IF, NixTextAttributes.KEYWORD),
            entry(NixTypes.THEN, NixTextAttributes.KEYWORD),
            entry(NixTypes.ELSE, NixTextAttributes.KEYWORD),
            entry(NixTypes.ASSERT, NixTextAttributes.KEYWORD),
            entry(NixTypes.WITH, NixTextAttributes.KEYWORD),
            entry(NixTypes.LET, NixTextAttributes.KEYWORD),
            entry(NixTypes.IN, NixTextAttributes.KEYWORD),
            entry(NixTypes.REC, NixTextAttributes.KEYWORD),
            entry(NixTypes.INHERIT, NixTextAttributes.KEYWORD),
            entry(NixTypes.OR_KW, NixTextAttributes.KEYWORD),
            // Identifiers
            entry(NixTypes.ID, NixTextAttributes.IDENTIFIER),
            // Operators
            entry(NixTypes.ASSIGN, NixTextAttributes.ASSIGN),
            entry(NixTypes.COLON, NixTextAttributes.COLON),
            entry(NixTypes.SEMI, NixTextAttributes.SEMICOLON),
            entry(NixTypes.COMMA, NixTextAttributes.COMMA),
            entry(NixTypes.DOT, NixTextAttributes.DOT),
            entry(NixTypes.ELLIPSIS, NixTextAttributes.ELLIPSIS),
            entry(NixTypes.AT, NixTextAttributes.AT),
            entry(NixTypes.HAS, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.NOT, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.TIMES, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.DIVIDE, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.PLUS, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.MINUS, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.LT, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.GT, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.CONCAT, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.UPDATE, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.LEQ, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.GEQ, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.EQ, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.NEQ, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.AND, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.OR, NixTextAttributes.OPERATION_SIGN),
            entry(NixTypes.IMPL, NixTextAttributes.OPERATION_SIGN),
            // Parentheses
            entry(NixTypes.LPAREN, NixTextAttributes.PARENTHESES),
            entry(NixTypes.RPAREN, NixTextAttributes.PARENTHESES),
            entry(NixTypes.LBRAC, NixTextAttributes.BRACKETS),
            entry(NixTypes.RBRAC, NixTextAttributes.BRACKETS),
            entry(NixTypes.LCURLY, NixTextAttributes.BRACES),
            entry(NixTypes.RCURLY, NixTextAttributes.BRACES),
            entry(NixTypes.DOLLAR, NixTextAttributes.BRACES),
            // Literals
            entry(NixTypes.INT, NixTextAttributes.NUMBER),
            entry(NixTypes.FLOAT, NixTextAttributes.NUMBER),
            entry(NixTypes.PATH_SEGMENT, NixTextAttributes.PATH),
            entry(NixTypes.SPATH, NixTextAttributes.PATH),
            entry(NixTypes.URI, NixTextAttributes.URI),
            // String literals
            entry(NixTypes.STR, NixTextAttributes.STRING),
            entry(NixTypes.STR_ESCAPE, NixTextAttributes.STRING_ESCAPE),
            entry(NixTypes.STRING_CLOSE, NixTextAttributes.STRING),
            entry(NixTypes.STRING_OPEN, NixTextAttributes.STRING),
            entry(NixTypes.IND_STR, NixTextAttributes.STRING),
            entry(NixTypes.IND_STR_LF, NixTextAttributes.STRING),
            entry(NixTypes.IND_STR_INDENT, NixTextAttributes.STRING),
            entry(NixTypes.IND_STR_ESCAPE, NixTextAttributes.STRING_ESCAPE),
            entry(NixTypes.IND_STRING_CLOSE, NixTextAttributes.STRING),
            entry(NixTypes.IND_STRING_OPEN, NixTextAttributes.STRING),
            // Other
            entry(NixTypes.SCOMMENT, NixTextAttributes.LINE_COMMENT),
            entry(NixTypes.MCOMMENT, NixTextAttributes.BLOCK_COMMENT),
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
