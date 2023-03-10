package org.nixos.idea.lang.highlighter;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public final class NixTextAttributes {

    public static final TextAttributesKey KEYWORD =
            createTextAttributesKey("NIX_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD);

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

    public static final TextAttributesKey IDENTIFIER =
            createTextAttributesKey("NIX_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey LITERAL =
            createTextAttributesKey("NIX_LITERAL", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey IMPORT =
            createTextAttributesKey("NIX_IMPORT", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey BUILTIN =
            createTextAttributesKey("NIX_BUILTIN", DefaultLanguageHighlighterColors.CONSTANT);
    public static final TextAttributesKey LOCAL_VARIABLE =
            createTextAttributesKey("NIX_LOCAL_VARIABLE", DefaultLanguageHighlighterColors.LOCAL_VARIABLE);
    public static final TextAttributesKey PARAMETER =
            createTextAttributesKey("NIX_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER);

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

    private NixTextAttributes() {
        // Cannot be instantiated
    }
}
