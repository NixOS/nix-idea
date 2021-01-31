package org.nixos.idea.lang;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.nixos.idea.psi.NixTypes;

import static com.intellij.openapi.editor.colors.TextAttributesKey.EMPTY_ARRAY;
import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class NixSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey LPAREN = createTextAttributesKey("LPAREN", DefaultLanguageHighlighterColors.PARENTHESES);
    public static final TextAttributesKey RPAREN = createTextAttributesKey("RPAREN", DefaultLanguageHighlighterColors.PARENTHESES);

    public static final TextAttributesKey LCURLY = createTextAttributesKey("LCURLY", DefaultLanguageHighlighterColors.BRACES);
    public static final TextAttributesKey RCURLY = createTextAttributesKey("RCURLY", DefaultLanguageHighlighterColors.BRACES);

    public static final TextAttributesKey LBRAC = createTextAttributesKey("LBRAC", DefaultLanguageHighlighterColors.BRACKETS);
    public static final TextAttributesKey RBRAC = createTextAttributesKey("RBRAC", DefaultLanguageHighlighterColors.BRACKETS);

    public static final TextAttributesKey COLON = createTextAttributesKey("COLON", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey SEMI = createTextAttributesKey("SEMI", DefaultLanguageHighlighterColors.SEMICOLON);
    public static final TextAttributesKey COMMA = createTextAttributesKey("COMMA", DefaultLanguageHighlighterColors.COMMA);

    public static final TextAttributesKey ID = createTextAttributesKey("ID", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey INT = createTextAttributesKey("INT", DefaultLanguageHighlighterColors.NUMBER);

    public static final TextAttributesKey ASSERT = createTextAttributesKey("ASSERT", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey IF = createTextAttributesKey("IF", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey ELSE = createTextAttributesKey("ELSE", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey THEN = createTextAttributesKey("THEN", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey LET = createTextAttributesKey("LET", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey REC = createTextAttributesKey("REC", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey IN = createTextAttributesKey("IN", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey WITH = createTextAttributesKey("WITH", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey INHERIT = createTextAttributesKey("INHERIT", DefaultLanguageHighlighterColors.KEYWORD);
    public static final TextAttributesKey OR_KW = createTextAttributesKey("OR_KW", DefaultLanguageHighlighterColors.KEYWORD);

    public static final TextAttributesKey GT = createTextAttributesKey("GT", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey LT = createTextAttributesKey("LT", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey LEQ = createTextAttributesKey("LEQ", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey GEQ = createTextAttributesKey("GEQ", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey NEQ = createTextAttributesKey("NEQ", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey EQ = createTextAttributesKey("EQ", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey PLUS = createTextAttributesKey("PLUS", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey MINUS = createTextAttributesKey("MINUS", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey DIVIDE = createTextAttributesKey("DIVIDE", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey TIMES = createTextAttributesKey("TIMES", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey NOT = createTextAttributesKey("NOT", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey AND = createTextAttributesKey("AND", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey OR = createTextAttributesKey("OR", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey IMPL = createTextAttributesKey("IMPL", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey UPDATE = createTextAttributesKey("UPDATE", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey IS = createTextAttributesKey("IS", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey NAMED = createTextAttributesKey("NAMED", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey CONCAT = createTextAttributesKey("CONCAT", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey DOT = createTextAttributesKey("DOT", DefaultLanguageHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey DOLLAR_CURLY = createTextAttributesKey("DOLLAR_CURLY", DefaultLanguageHighlighterColors.OPERATION_SIGN);

    public static final TextAttributesKey PATH = createTextAttributesKey("PATH", DefaultLanguageHighlighterColors.LABEL);
    public static final TextAttributesKey SPATH = createTextAttributesKey("SPATH", DefaultLanguageHighlighterColors.LABEL);
    public static final TextAttributesKey HPATH = createTextAttributesKey("HPATH", DefaultLanguageHighlighterColors.LABEL);

    public static final TextAttributesKey IND_STR = createTextAttributesKey("IND_STR", DefaultLanguageHighlighterColors.STRING);
    public static final TextAttributesKey STR = createTextAttributesKey("STR", DefaultLanguageHighlighterColors.STRING);

    public static final TextAttributesKey SCOMMENT = createTextAttributesKey("SCOMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    public static final TextAttributesKey MCOMMENT = createTextAttributesKey("MCOMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT);

    public static final TextAttributesKey LAMBDA = createTextAttributesKey("LAMBDA", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION);
    public static final TextAttributesKey FORMAL = createTextAttributesKey("FORMAL", DefaultLanguageHighlighterColors.PARAMETER);

    public static final TextAttributesKey[] SEMI_KEYS = new TextAttributesKey[]{SEMI};
    public static final TextAttributesKey[] COMMA_KEYS = new TextAttributesKey[]{COMMA};
    public static final TextAttributesKey[] KEYWORD_KEYS = new TextAttributesKey[]{IN,OR_KW,REC,IF,ELSE,THEN,LET,IN,WITH,INHERIT};
    public static final TextAttributesKey[] STRING_KEYS = new TextAttributesKey[]{STR,IND_STR};
    public static final TextAttributesKey[] PAREN_KEYS = new TextAttributesKey[]{LPAREN,RPAREN,LCURLY,RCURLY,LBRAC,RBRAC};
    public static final TextAttributesKey[] COLON_KEYS = new TextAttributesKey[]{COLON};
    public static final TextAttributesKey[] ID_KEYS = new TextAttributesKey[]{ID};
    public static final TextAttributesKey[] NUMBER_KEYS = new TextAttributesKey[]{INT};
    public static final TextAttributesKey[] OPERATOR_KEYS = new TextAttributesKey[]{
            GT,LT,LEQ,GEQ,NEQ,EQ,PLUS,MINUS,DIVIDE,TIMES,NOT,AND,OR,IMPL,UPDATE,IS,NAMED,CONCAT,DOT,DOLLAR_CURLY
    };
    public static final TextAttributesKey[] LINE_COMMENT_KEYS = new TextAttributesKey[]{SCOMMENT};
    public static final TextAttributesKey[] MULTI_LINE_COMMENT_KEYS = new TextAttributesKey[]{MCOMMENT};
    public static final TextAttributesKey[] FUNCTION_KEYS = new TextAttributesKey[]{LAMBDA};
    public static final TextAttributesKey[] PARAMETER_KEYS = new TextAttributesKey[]{FORMAL};

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new NixLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        {
            if (tokenType == NixTypes.PARAM) return PARAMETER_KEYS;
            if (tokenType == NixTypes.EXPR_LAMBDA) return FUNCTION_KEYS;
            if (tokenType == NixTypes.COMMA) return COMMA_KEYS;
            if (tokenType == NixTypes.SEMI) return SEMI_KEYS;
            if (tokenType == NixTypes.COLON) {
                return COLON_KEYS;
            }
            if (tokenType == NixTypes.SCOMMENT) {
                return LINE_COMMENT_KEYS;
            }
            if (tokenType == NixTypes.MCOMMENT) {
                return MULTI_LINE_COMMENT_KEYS;
            }
            if (tokenType == NixTypes.ID) {
                return ID_KEYS;
            }
            if (tokenType == NixTypes.INT) {
                return NUMBER_KEYS;
            }
            if(
                    tokenType == NixTypes.GT ||
                    tokenType == NixTypes.LT ||
                    tokenType == NixTypes.LEQ ||
                    tokenType == NixTypes.GEQ ||
                    tokenType == NixTypes.NEQ ||
                    tokenType == NixTypes.EQ ||
                    tokenType == NixTypes.PLUS ||
                    tokenType == NixTypes.MINUS ||
                    tokenType == NixTypes.DIVIDE ||
                    tokenType == NixTypes.TIMES ||
                    tokenType == NixTypes.NOT ||
                    tokenType == NixTypes.AND ||
                    tokenType == NixTypes.OR ||
                    tokenType == NixTypes.IMPL ||
                    tokenType == NixTypes.UPDATE ||
                    tokenType == NixTypes.HAS ||
                    tokenType == NixTypes.AT ||
                    tokenType == NixTypes.CONCAT ||
                    tokenType == NixTypes.DOT )
                return OPERATOR_KEYS;
            if (
                    tokenType == NixTypes.RBRAC ||
                    tokenType == NixTypes.LBRAC ||
                    tokenType == NixTypes.RPAREN ||
                    tokenType == NixTypes.LPAREN ||
                    tokenType == NixTypes.RCURLY ||
                    tokenType == NixTypes.DOLLAR_CURLY ||
                    tokenType == NixTypes.LCURLY) {
                return PAREN_KEYS;
            }
            if (tokenType == NixTypes.STR || tokenType == NixTypes.IND_STR) {
                return STRING_KEYS;
            }
            if (
                    tokenType == NixTypes.IF ||
                    tokenType == NixTypes.THEN ||
                    tokenType == NixTypes.ELSE ||
                    tokenType == NixTypes.ASSERT ||
                    tokenType == NixTypes.WITH ||
                    tokenType == NixTypes.LET ||
                    tokenType == NixTypes.IN ||
                    tokenType == NixTypes.REC ||
                    tokenType == NixTypes.INHERIT ||
                    tokenType == NixTypes.OR_KW) {
                return KEYWORD_KEYS;
            }
            return EMPTY_ARRAY;
        }
    }
}

