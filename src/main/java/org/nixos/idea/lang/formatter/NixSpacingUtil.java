package org.nixos.idea.lang.formatter;

import com.intellij.formatting.SpacingBuilder;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.nixos.idea.lang.NixLanguage;
import org.nixos.idea.psi.NixExprParens;
import org.nixos.idea.psi.NixPsiElement;
import org.nixos.idea.psi.NixTokenSets;
import org.nixos.idea.settings.NixCodeStyleSettings;

import static org.nixos.idea.psi.NixTypes.AND;
import static org.nixos.idea.psi.NixTypes.ASSIGN;
import static org.nixos.idea.psi.NixTypes.AT;
import static org.nixos.idea.psi.NixTypes.COLON;
import static org.nixos.idea.psi.NixTypes.COMMA;
import static org.nixos.idea.psi.NixTypes.CONCAT;
import static org.nixos.idea.psi.NixTypes.DIVIDE;
import static org.nixos.idea.psi.NixTypes.DOT;
import static org.nixos.idea.psi.NixTypes.EQ;
import static org.nixos.idea.psi.NixTypes.EXPR_ATTRS;
import static org.nixos.idea.psi.NixTypes.EXPR_IF;
import static org.nixos.idea.psi.NixTypes.EXPR_LAMBDA;
import static org.nixos.idea.psi.NixTypes.EXPR_LIST;
import static org.nixos.idea.psi.NixTypes.EXPR_OP_MINUS;
import static org.nixos.idea.psi.NixTypes.EXPR_OP_NEG;
import static org.nixos.idea.psi.NixTypes.GEQ;
import static org.nixos.idea.psi.NixTypes.GT;
import static org.nixos.idea.psi.NixTypes.HAS;
import static org.nixos.idea.psi.NixTypes.IMPL;
import static org.nixos.idea.psi.NixTypes.IND_STRING;
import static org.nixos.idea.psi.NixTypes.LBRAC;
import static org.nixos.idea.psi.NixTypes.LCURLY;
import static org.nixos.idea.psi.NixTypes.LEQ;
import static org.nixos.idea.psi.NixTypes.LET;
import static org.nixos.idea.psi.NixTypes.LPAREN;
import static org.nixos.idea.psi.NixTypes.LT;
import static org.nixos.idea.psi.NixTypes.MINUS;
import static org.nixos.idea.psi.NixTypes.NEQ;
import static org.nixos.idea.psi.NixTypes.NOT;
import static org.nixos.idea.psi.NixTypes.OR;
import static org.nixos.idea.psi.NixTypes.PLUS;
import static org.nixos.idea.psi.NixTypes.RBRAC;
import static org.nixos.idea.psi.NixTypes.RCURLY;
import static org.nixos.idea.psi.NixTypes.REC;
import static org.nixos.idea.psi.NixTypes.RPAREN;
import static org.nixos.idea.psi.NixTypes.SEMI;
import static org.nixos.idea.psi.NixTypes.TIMES;
import static org.nixos.idea.psi.NixTypes.UPDATE;

final class NixSpacingUtil {

    private NixSpacingUtil() {} // Cannot be instantiated

    /**
     * Returns whether the given element is absorbable according to RFC 166.
     *
     * @param element the PSI element
     * @return {@code true} if the given element is absorbable, otherwise {@code false}
     * @see <a href="https://github.com/NixOS/rfcs/blob/25c3f524631000b851375e7b96223a56e71cc0e2/rfcs/0166-nix-formatting.md#terms-and-definitions">
     * RFC 166 - Terms and definitions</a>
     */
    static boolean isAbsorbable(@Nullable NixPsiElement element) {
        if (element == null) return true;
        IElementType type = element.getNode().getElementType();
        return type == EXPR_ATTRS ||
               type == EXPR_LIST ||
               type == IND_STRING ||
               element instanceof NixExprParens p && isAbsorbable(p.getExpr());
    }

    /**
     * Returns whether recursive occurrences of the given type should be flattened to look like a sequence.
     *
     * @param type the type of the elements
     * @return {@code true} if the given type should be treated as a sequence without increasing the indentation, {@code false} otherwise.
     */
    static boolean isRecursiveSequence(IElementType type) {
        // TODO which other types?
        return type == EXPR_IF;
    }

    static @NotNull SpacingBuilder spacingBuilder(CodeStyleSettings settings) {
        CommonCodeStyleSettings commonSettings = settings.getCommonSettings(NixLanguage.INSTANCE);
        NixCodeStyleSettings customSettings = settings.getCustomSettings(NixCodeStyleSettings.class);
        return new SpacingBuilder(commonSettings)
                // Braces {}, brackets [], and parentheses ()
                .withinPair(LBRAC, RBRAC).spaceIf(commonSettings.SPACE_WITHIN_BRACKETS, true)
                .withinPair(LCURLY, RCURLY).spaceIf(commonSettings.SPACE_WITHIN_BRACES, true)
                .withinPair(LPAREN, RPAREN).spaceIf(commonSettings.SPACE_WITHIN_PARENTHESES, true)
                // Other non-operator signs
                .around(ASSIGN).spaceIf(commonSettings.SPACE_AROUND_ASSIGNMENT_OPERATORS)
                .afterInside(AT, EXPR_LAMBDA).spaceIf(customSettings.SPACE_AFTER_AT_SIGN_IN_LAMBDA)
                .beforeInside(AT, EXPR_LAMBDA).spaceIf(customSettings.SPACE_BEFORE_AT_SIGN_IN_LAMBDA)
                .after(SEMI).spaceIf(commonSettings.SPACE_AFTER_SEMICOLON)
                .before(SEMI).spaceIf(commonSettings.SPACE_BEFORE_SEMICOLON)
                .after(COMMA).spaceIf(commonSettings.SPACE_AFTER_COMMA)
                .before(COMMA).spaceIf(commonSettings.SPACE_BEFORE_COMMA)
                .afterInside(COLON, EXPR_LAMBDA).spaceIf(customSettings.SPACE_AFTER_COLON_IN_LAMBDA)
                .beforeInside(COLON, EXPR_LAMBDA).spaceIf(customSettings.SPACE_BEFORE_COLON_IN_LAMBDA)
                .betweenInside(REC, LCURLY, EXPR_ATTRS).spaceIf(customSettings.SPACE_AFTER_SET_MODIFIER)
                .betweenInside(LET, LCURLY, EXPR_ATTRS).spaceIf(customSettings.SPACE_AFTER_SET_MODIFIER)
                // Operators
                .around(DOT).none() // TODO
                .aroundInside(MINUS, EXPR_OP_MINUS).spaceIf(commonSettings.SPACE_AROUND_ADDITIVE_OPERATORS)
                .between(NixTokenSets.MIGHT_COLLAPSE_WITH_ID, MINUS).spaceIf(true) // TODO Doesn't work
                .around(PLUS).spaceIf(commonSettings.SPACE_AROUND_ADDITIVE_OPERATORS)
                .around(EQ).spaceIf(commonSettings.SPACE_AROUND_EQUALITY_OPERATORS)
                .around(NEQ).spaceIf(commonSettings.SPACE_AROUND_EQUALITY_OPERATORS)
                .around(AND).spaceIf(commonSettings.SPACE_AROUND_LOGICAL_OPERATORS)
                .around(OR).spaceIf(commonSettings.SPACE_AROUND_LOGICAL_OPERATORS)
                .around(TIMES).spaceIf(commonSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)
                .around(DIVIDE).spaceIf(commonSettings.SPACE_AROUND_MULTIPLICATIVE_OPERATORS)
                .around(GEQ).spaceIf(commonSettings.SPACE_AROUND_RELATIONAL_OPERATORS)
                .around(GT).spaceIf(commonSettings.SPACE_AROUND_RELATIONAL_OPERATORS)
                .around(LEQ).spaceIf(commonSettings.SPACE_AROUND_RELATIONAL_OPERATORS)
                .around(LT).spaceIf(commonSettings.SPACE_AROUND_RELATIONAL_OPERATORS)
                .afterInside(MINUS, EXPR_OP_NEG).spaceIf(commonSettings.SPACE_AROUND_UNARY_OPERATOR)
                .after(NOT).spaceIf(commonSettings.SPACE_AROUND_UNARY_OPERATOR)
                .around(CONCAT).spaceIf(customSettings.SPACE_AROUND_CONCAT_OPERATOR)
                .around(HAS).spaceIf(customSettings.SPACE_AROUND_HAS_ATTR_OPERATOR)
                .around(IMPL).spaceIf(customSettings.SPACE_AROUND_IMPLICATION_OPERATOR)
                .around(UPDATE).spaceIf(customSettings.SPACE_AROUND_UPDATE_ATTRS_OPERATOR);
    }
}
