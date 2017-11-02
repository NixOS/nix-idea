// This is a generated file. Not intended for manual editing.
package org.nixos.idea.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.nixos.idea.psi.impl.*;

public interface NixTypes {

  IElementType ADD_EXPR = new NixElementType("ADD_EXPR");
  IElementType ATTR = new NixElementType("ATTR");
  IElementType ATTRS = new NixElementType("ATTRS");
  IElementType ATTR_ASSIGN = new NixElementType("ATTR_ASSIGN");
  IElementType ATTR_PATH = new NixElementType("ATTR_PATH");
  IElementType BINDING = new NixElementType("BINDING");
  IElementType BINDS = new NixElementType("BINDS");
  IElementType BIND_OR_SELECT = new NixElementType("BIND_OR_SELECT");
  IElementType BIND_SET = new NixElementType("BIND_SET");
  IElementType BN_LAMBDA = new NixElementType("BN_LAMBDA");
  IElementType BOOL_EXPR = new NixElementType("BOOL_EXPR");
  IElementType CALL_ARGS = new NixElementType("CALL_ARGS");
  IElementType CONT_PATH = new NixElementType("CONT_PATH");
  IElementType DEFVAL = new NixElementType("DEFVAL");
  IElementType DOC_STRING = new NixElementType("DOC_STRING");
  IElementType EVAL_EXPR = new NixElementType("EVAL_EXPR");
  IElementType EVAL_OR_SELECT = new NixElementType("EVAL_OR_SELECT");
  IElementType EXPR = new NixElementType("EXPR");
  IElementType EXPR_APP = new NixElementType("EXPR_APP");
  IElementType EXPR_LIST = new NixElementType("EXPR_LIST");
  IElementType EXPR_OP = new NixElementType("EXPR_OP");
  IElementType EXPR_SIMPLE = new NixElementType("EXPR_SIMPLE");
  IElementType FACTOR = new NixElementType("FACTOR");
  IElementType FN_LAMBDA = new NixElementType("FN_LAMBDA");
  IElementType IMPORT_STMT = new NixElementType("IMPORT_STMT");
  IElementType IND_STRING_PARTS = new NixElementType("IND_STRING_PARTS");
  IElementType INHERIT_ATTRS = new NixElementType("INHERIT_ATTRS");
  IElementType LAMBDA = new NixElementType("LAMBDA");
  IElementType LIST = new NixElementType("LIST");
  IElementType LIST_EXPR = new NixElementType("LIST_EXPR");
  IElementType LITERAL = new NixElementType("LITERAL");
  IElementType LITERAL_SIMPLE_STRING = new NixElementType("LITERAL_SIMPLE_STRING");
  IElementType LOGICAL = new NixElementType("LOGICAL");
  IElementType MUL_EXPR = new NixElementType("MUL_EXPR");
  IElementType NIX_INIT = new NixElementType("NIX_INIT");
  IElementType PARAM = new NixElementType("PARAM");
  IElementType PARAMS = new NixElementType("PARAMS");
  IElementType PARAM_SET = new NixElementType("PARAM_SET");
  IElementType PATH_EXPR = new NixElementType("PATH_EXPR");
  IElementType PRIMARY = new NixElementType("PRIMARY");
  IElementType RELATIVE = new NixElementType("RELATIVE");
  IElementType REL_EXPR = new NixElementType("REL_EXPR");
  IElementType REQUIRE_EXPR = new NixElementType("REQUIRE_EXPR");
  IElementType STRING_ATTR = new NixElementType("STRING_ATTR");
  IElementType STRING_PARTS = new NixElementType("STRING_PARTS");
  IElementType UNARY_OP = new NixElementType("UNARY_OP");

  IElementType AND = new NixTokenType("&&");
  IElementType ASSERT = new NixTokenType("assert");
  IElementType ASSIGN = new NixTokenType("=");
  IElementType BOOL = new NixTokenType("BOOL");
  IElementType COLON = new NixTokenType(":");
  IElementType COMMA = new NixTokenType(",");
  IElementType CONCAT = new NixTokenType("++");
  IElementType DIVIDE = new NixTokenType("/");
  IElementType DOLLAR = new NixTokenType("$");
  IElementType DOLLAR_CURLY = new NixTokenType("${");
  IElementType DOT = new NixTokenType(".");
  IElementType ELLIPSIS = new NixTokenType("...");
  IElementType ELSE = new NixTokenType("else");
  IElementType EQ = new NixTokenType("==");
  IElementType FNUTT_CLOSE = new NixTokenType("FNUTT_CLOSE");
  IElementType FNUTT_OPEN = new NixTokenType("FNUTT_OPEN");
  IElementType GEQ = new NixTokenType(">=");
  IElementType GT = new NixTokenType(">");
  IElementType HPATH = new NixTokenType("HPATH");
  IElementType ID = new NixTokenType("ID");
  IElementType IF = new NixTokenType("if");
  IElementType IMPL = new NixTokenType("->");
  IElementType IMPORT = new NixTokenType("import");
  IElementType IMPORTS = new NixTokenType("imports");
  IElementType IN = new NixTokenType("in");
  IElementType IND_STR = new NixTokenType("IND_STR");
  IElementType IND_STRING_CLOSE = new NixTokenType("IND_STRING_CLOSE");
  IElementType IND_STRING_OPEN = new NixTokenType("IND_STRING_OPEN");
  IElementType INHERIT = new NixTokenType("inherit");
  IElementType INT = new NixTokenType("INT");
  IElementType IS = new NixTokenType("?");
  IElementType LBRAC = new NixTokenType("[");
  IElementType LCURLY = new NixTokenType("{");
  IElementType LEQ = new NixTokenType("<=");
  IElementType LET = new NixTokenType("let");
  IElementType LPAREN = new NixTokenType("(");
  IElementType LT = new NixTokenType("<");
  IElementType MCOMMENT = new NixTokenType("MCOMMENT");
  IElementType MINUS = new NixTokenType("-");
  IElementType NAMED = new NixTokenType("@");
  IElementType NEQ = new NixTokenType("!=");
  IElementType NOT = new NixTokenType("!");
  IElementType OR = new NixTokenType("||");
  IElementType OR_KW = new NixTokenType("or");
  IElementType PATH = new NixTokenType("PATH");
  IElementType PLUS = new NixTokenType("+");
  IElementType RBRAC = new NixTokenType("]");
  IElementType RCURLY = new NixTokenType("}");
  IElementType REC = new NixTokenType("rec");
  IElementType REQUIRE = new NixTokenType("require");
  IElementType REQUIRES = new NixTokenType("requires");
  IElementType RPAREN = new NixTokenType(")");
  IElementType SCOMMENT = new NixTokenType("SCOMMENT");
  IElementType SEMI = new NixTokenType(";");
  IElementType SPATH = new NixTokenType("SPATH");
  IElementType STR = new NixTokenType("STR");
  IElementType THEN = new NixTokenType("then");
  IElementType TIMES = new NixTokenType("*");
  IElementType UPDATE = new NixTokenType("//");
  IElementType URI = new NixTokenType("URI");
  IElementType WITH = new NixTokenType("with");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == ADD_EXPR) {
        return new NixAddExprImpl(node);
      }
      else if (type == ATTR) {
        return new NixAttrImpl(node);
      }
      else if (type == ATTRS) {
        return new NixAttrsImpl(node);
      }
      else if (type == ATTR_ASSIGN) {
        return new NixAttrAssignImpl(node);
      }
      else if (type == ATTR_PATH) {
        return new NixAttrPathImpl(node);
      }
      else if (type == BINDING) {
        return new NixBindingImpl(node);
      }
      else if (type == BINDS) {
        return new NixBindsImpl(node);
      }
      else if (type == BIND_OR_SELECT) {
        return new NixBindOrSelectImpl(node);
      }
      else if (type == BIND_SET) {
        return new NixBindSetImpl(node);
      }
      else if (type == BN_LAMBDA) {
        return new NixBnLambdaImpl(node);
      }
      else if (type == BOOL_EXPR) {
        return new NixBoolExprImpl(node);
      }
      else if (type == CALL_ARGS) {
        return new NixCallArgsImpl(node);
      }
      else if (type == CONT_PATH) {
        return new NixContPathImpl(node);
      }
      else if (type == DEFVAL) {
        return new NixDefvalImpl(node);
      }
      else if (type == DOC_STRING) {
        return new NixDocStringImpl(node);
      }
      else if (type == EVAL_EXPR) {
        return new NixEvalExprImpl(node);
      }
      else if (type == EVAL_OR_SELECT) {
        return new NixEvalOrSelectImpl(node);
      }
      else if (type == EXPR) {
        return new NixExprImpl(node);
      }
      else if (type == EXPR_APP) {
        return new NixExprAppImpl(node);
      }
      else if (type == EXPR_LIST) {
        return new NixExprListImpl(node);
      }
      else if (type == EXPR_OP) {
        return new NixExprOpImpl(node);
      }
      else if (type == EXPR_SIMPLE) {
        return new NixExprSimpleImpl(node);
      }
      else if (type == FACTOR) {
        return new NixFactorImpl(node);
      }
      else if (type == FN_LAMBDA) {
        return new NixFnLambdaImpl(node);
      }
      else if (type == IMPORT_STMT) {
        return new NixImportStmtImpl(node);
      }
      else if (type == IND_STRING_PARTS) {
        return new NixIndStringPartsImpl(node);
      }
      else if (type == INHERIT_ATTRS) {
        return new NixInheritAttrsImpl(node);
      }
      else if (type == LAMBDA) {
        return new NixLambdaImpl(node);
      }
      else if (type == LIST) {
        return new NixListImpl(node);
      }
      else if (type == LIST_EXPR) {
        return new NixListExprImpl(node);
      }
      else if (type == LITERAL) {
        return new NixLiteralImpl(node);
      }
      else if (type == LITERAL_SIMPLE_STRING) {
        return new NixLiteralSimpleStringImpl(node);
      }
      else if (type == LOGICAL) {
        return new NixLogicalImpl(node);
      }
      else if (type == MUL_EXPR) {
        return new NixMulExprImpl(node);
      }
      else if (type == NIX_INIT) {
        return new NixNixInitImpl(node);
      }
      else if (type == PARAM) {
        return new NixParamImpl(node);
      }
      else if (type == PARAMS) {
        return new NixParamsImpl(node);
      }
      else if (type == PARAM_SET) {
        return new NixParamSetImpl(node);
      }
      else if (type == PATH_EXPR) {
        return new NixPathExprImpl(node);
      }
      else if (type == PRIMARY) {
        return new NixPrimaryImpl(node);
      }
      else if (type == RELATIVE) {
        return new NixRelativeImpl(node);
      }
      else if (type == REL_EXPR) {
        return new NixRelExprImpl(node);
      }
      else if (type == REQUIRE_EXPR) {
        return new NixRequireExprImpl(node);
      }
      else if (type == STRING_ATTR) {
        return new NixStringAttrImpl(node);
      }
      else if (type == STRING_PARTS) {
        return new NixStringPartsImpl(node);
      }
      else if (type == UNARY_OP) {
        return new NixUnaryOpImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
