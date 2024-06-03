// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.nixos.idea.psi.impl.*;

public interface NixTypes {

  IElementType ANTIQUOTATION = new NixElementType("ANTIQUOTATION");
  IElementType ATTR = new NixElementType("ATTR");
  IElementType ATTR_PATH = new NixElementType("ATTR_PATH");
  IElementType BIND = new NixElementType("BIND");
  IElementType BIND_ATTR = new NixElementType("BIND_ATTR");
  IElementType BIND_INHERIT = new NixElementType("BIND_INHERIT");
  IElementType EXPR = new NixElementType("EXPR");
  IElementType EXPR_APP = new NixElementType("EXPR_APP");
  IElementType EXPR_ASSERT = new NixElementType("EXPR_ASSERT");
  IElementType EXPR_IF = new NixElementType("EXPR_IF");
  IElementType EXPR_LAMBDA = new NixElementType("EXPR_LAMBDA");
  IElementType EXPR_LET = new NixElementType("EXPR_LET");
  IElementType EXPR_OP = new NixElementType("EXPR_OP");
  IElementType EXPR_OP_AND = new NixElementType("EXPR_OP_AND");
  IElementType EXPR_OP_BASE = new NixElementType("EXPR_OP_BASE");
  IElementType EXPR_OP_CONCAT = new NixElementType("EXPR_OP_CONCAT");
  IElementType EXPR_OP_DIV = new NixElementType("EXPR_OP_DIV");
  IElementType EXPR_OP_EQ = new NixElementType("EXPR_OP_EQ");
  IElementType EXPR_OP_GE = new NixElementType("EXPR_OP_GE");
  IElementType EXPR_OP_GT = new NixElementType("EXPR_OP_GT");
  IElementType EXPR_OP_HAS = new NixElementType("EXPR_OP_HAS");
  IElementType EXPR_OP_IMPLICATION = new NixElementType("EXPR_OP_IMPLICATION");
  IElementType EXPR_OP_LE = new NixElementType("EXPR_OP_LE");
  IElementType EXPR_OP_LT = new NixElementType("EXPR_OP_LT");
  IElementType EXPR_OP_MINUS = new NixElementType("EXPR_OP_MINUS");
  IElementType EXPR_OP_MUL = new NixElementType("EXPR_OP_MUL");
  IElementType EXPR_OP_NE = new NixElementType("EXPR_OP_NE");
  IElementType EXPR_OP_NEG = new NixElementType("EXPR_OP_NEG");
  IElementType EXPR_OP_NOT = new NixElementType("EXPR_OP_NOT");
  IElementType EXPR_OP_OR = new NixElementType("EXPR_OP_OR");
  IElementType EXPR_OP_PLUS = new NixElementType("EXPR_OP_PLUS");
  IElementType EXPR_OP_UPDATE = new NixElementType("EXPR_OP_UPDATE");
  IElementType EXPR_SELECT = new NixElementType("EXPR_SELECT");
  IElementType EXPR_SIMPLE = new NixElementType("EXPR_SIMPLE");
  IElementType EXPR_WITH = new NixElementType("EXPR_WITH");
  IElementType IDENTIFIER = new NixElementType("IDENTIFIER");
  IElementType IND_STRING = new NixElementType("IND_STRING");
  IElementType LEGACY_APP_OR = new NixElementType("LEGACY_APP_OR");
  IElementType LEGACY_LET = new NixElementType("LEGACY_LET");
  IElementType LIST = new NixElementType("LIST");
  IElementType LITERAL = new NixElementType("LITERAL");
  IElementType PARAM = new NixElementType("PARAM");
  IElementType PARAM_SET = new NixElementType("PARAM_SET");
  IElementType PARENS = new NixElementType("PARENS");
  IElementType PATH = new NixElementType("PATH");
  IElementType SET = new NixElementType("SET");
  IElementType STD_ATTR = new NixElementType("STD_ATTR");
  IElementType STD_STRING = new NixElementType("STD_STRING");
  IElementType STRING = new NixElementType("STRING");
  IElementType STRING_ATTR = new NixElementType("STRING_ATTR");
  IElementType STRING_PART = new NixElementType("STRING_PART");
  IElementType STRING_TEXT = new NixElementType("STRING_TEXT");

  IElementType AND = new NixTokenType("&&");
  IElementType ASSERT = new NixTokenType("assert");
  IElementType ASSIGN = new NixTokenType("=");
  IElementType AT = new NixTokenType("@");
  IElementType COLON = new NixTokenType(":");
  IElementType COMMA = new NixTokenType(",");
  IElementType CONCAT = new NixTokenType("++");
  IElementType DIVIDE = new NixTokenType("/");
  IElementType DOLLAR = new NixTokenType("$");
  IElementType DOT = new NixTokenType(".");
  IElementType ELLIPSIS = new NixTokenType("...");
  IElementType ELSE = new NixTokenType("else");
  IElementType EQ = new NixTokenType("==");
  IElementType FLOAT = new NixTokenType("FLOAT");
  IElementType GEQ = new NixTokenType(">=");
  IElementType GT = new NixTokenType(">");
  IElementType HAS = new NixTokenType("?");
  IElementType ID = new NixTokenType("ID");
  IElementType IF = new NixTokenType("if");
  IElementType IMPL = new NixTokenType("->");
  IElementType IN = new NixTokenType("in");
  IElementType IND_STR = new NixTokenType("IND_STR");
  IElementType IND_STRING_CLOSE = new NixTokenType("IND_STRING_CLOSE");
  IElementType IND_STRING_OPEN = new NixTokenType("IND_STRING_OPEN");
  IElementType IND_STR_ESCAPE = new NixTokenType("IND_STR_ESCAPE");
  IElementType INHERIT = new NixTokenType("inherit");
  IElementType INT = new NixTokenType("INT");
  IElementType LBRAC = new NixTokenType("[");
  IElementType LCURLY = new NixTokenType("{");
  IElementType LEQ = new NixTokenType("<=");
  IElementType LET = new NixTokenType("let");
  IElementType LPAREN = new NixTokenType("(");
  IElementType LT = new NixTokenType("<");
  IElementType MCOMMENT = new NixTokenType("MCOMMENT");
  IElementType MINUS = new NixTokenType("-");
  IElementType NEQ = new NixTokenType("!=");
  IElementType NOT = new NixTokenType("!");
  IElementType OR = new NixTokenType("||");
  IElementType OR_KW = new NixTokenType("or");
  IElementType PATH_END = new NixTokenType("PATH_END");
  IElementType PATH_SEGMENT = new NixTokenType("PATH_SEGMENT");
  IElementType PLUS = new NixTokenType("+");
  IElementType RBRAC = new NixTokenType("]");
  IElementType RCURLY = new NixTokenType("}");
  IElementType REC = new NixTokenType("rec");
  IElementType RPAREN = new NixTokenType(")");
  IElementType SCOMMENT = new NixTokenType("SCOMMENT");
  IElementType SEMI = new NixTokenType(";");
  IElementType SPATH = new NixTokenType("SPATH");
  IElementType STR = new NixTokenType("STR");
  IElementType STRING_CLOSE = new NixTokenType("STRING_CLOSE");
  IElementType STRING_OPEN = new NixTokenType("STRING_OPEN");
  IElementType STR_ESCAPE = new NixTokenType("STR_ESCAPE");
  IElementType THEN = new NixTokenType("then");
  IElementType TIMES = new NixTokenType("*");
  IElementType UPDATE = new NixTokenType("//");
  IElementType URI = new NixTokenType("URI");
  IElementType WITH = new NixTokenType("with");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ANTIQUOTATION) {
        return new NixAntiquotationImpl(node);
      }
      else if (type == ATTR_PATH) {
        return new NixAttrPathImpl(node);
      }
      else if (type == BIND_ATTR) {
        return new NixBindAttrImpl(node);
      }
      else if (type == BIND_INHERIT) {
        return new NixBindInheritImpl(node);
      }
      else if (type == EXPR) {
        return new NixExprImpl(node);
      }
      else if (type == EXPR_APP) {
        return new NixExprAppImpl(node);
      }
      else if (type == EXPR_ASSERT) {
        return new NixExprAssertImpl(node);
      }
      else if (type == EXPR_IF) {
        return new NixExprIfImpl(node);
      }
      else if (type == EXPR_LAMBDA) {
        return new NixExprLambdaImpl(node);
      }
      else if (type == EXPR_LET) {
        return new NixExprLetImpl(node);
      }
      else if (type == EXPR_OP_AND) {
        return new NixExprOpAndImpl(node);
      }
      else if (type == EXPR_OP_CONCAT) {
        return new NixExprOpConcatImpl(node);
      }
      else if (type == EXPR_OP_DIV) {
        return new NixExprOpDivImpl(node);
      }
      else if (type == EXPR_OP_EQ) {
        return new NixExprOpEqImpl(node);
      }
      else if (type == EXPR_OP_GE) {
        return new NixExprOpGeImpl(node);
      }
      else if (type == EXPR_OP_GT) {
        return new NixExprOpGtImpl(node);
      }
      else if (type == EXPR_OP_HAS) {
        return new NixExprOpHasImpl(node);
      }
      else if (type == EXPR_OP_IMPLICATION) {
        return new NixExprOpImplicationImpl(node);
      }
      else if (type == EXPR_OP_LE) {
        return new NixExprOpLeImpl(node);
      }
      else if (type == EXPR_OP_LT) {
        return new NixExprOpLtImpl(node);
      }
      else if (type == EXPR_OP_MINUS) {
        return new NixExprOpMinusImpl(node);
      }
      else if (type == EXPR_OP_MUL) {
        return new NixExprOpMulImpl(node);
      }
      else if (type == EXPR_OP_NE) {
        return new NixExprOpNeImpl(node);
      }
      else if (type == EXPR_OP_NEG) {
        return new NixExprOpNegImpl(node);
      }
      else if (type == EXPR_OP_NOT) {
        return new NixExprOpNotImpl(node);
      }
      else if (type == EXPR_OP_OR) {
        return new NixExprOpOrImpl(node);
      }
      else if (type == EXPR_OP_PLUS) {
        return new NixExprOpPlusImpl(node);
      }
      else if (type == EXPR_OP_UPDATE) {
        return new NixExprOpUpdateImpl(node);
      }
      else if (type == EXPR_SELECT) {
        return new NixExprSelectImpl(node);
      }
      else if (type == EXPR_WITH) {
        return new NixExprWithImpl(node);
      }
      else if (type == IDENTIFIER) {
        return new NixIdentifierImpl(node);
      }
      else if (type == IND_STRING) {
        return new NixIndStringImpl(node);
      }
      else if (type == LEGACY_APP_OR) {
        return new NixLegacyAppOrImpl(node);
      }
      else if (type == LEGACY_LET) {
        return new NixLegacyLetImpl(node);
      }
      else if (type == LIST) {
        return new NixListImpl(node);
      }
      else if (type == LITERAL) {
        return new NixLiteralImpl(node);
      }
      else if (type == PARAM) {
        return new NixParamImpl(node);
      }
      else if (type == PARAM_SET) {
        return new NixParamSetImpl(node);
      }
      else if (type == PARENS) {
        return new NixParensImpl(node);
      }
      else if (type == PATH) {
        return new NixPathImpl(node);
      }
      else if (type == SET) {
        return new NixSetImpl(node);
      }
      else if (type == STD_ATTR) {
        return new NixStdAttrImpl(node);
      }
      else if (type == STD_STRING) {
        return new NixStdStringImpl(node);
      }
      else if (type == STRING_ATTR) {
        return new NixStringAttrImpl(node);
      }
      else if (type == STRING_PART) {
        return new NixStringPartImpl(node);
      }
      else if (type == STRING_TEXT) {
        return new NixStringTextImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
