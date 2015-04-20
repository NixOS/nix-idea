// This is a generated file. Not intended for manual editing.
package cc.cflags.nixitch.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import cc.cflags.nixitch.psi.impl.*;

public interface NixTypes {

  IElementType ADD_EXPR = new NixElementType("ADD_EXPR");
  IElementType ATTR = new NixElementType("ATTR");
  IElementType ATTRS = new NixElementType("ATTRS");
  IElementType ATTR_ASSIGN = new NixElementType("ATTR_ASSIGN");
  IElementType ATTR_PATH = new NixElementType("ATTR_PATH");
  IElementType BINDS = new NixElementType("BINDS");
  IElementType BIND_SET = new NixElementType("BIND_SET");
  IElementType BN_LAMBDA = new NixElementType("BN_LAMBDA");
  IElementType BOOL_EXPR = new NixElementType("BOOL_EXPR");
  IElementType CALL_ARGS = new NixElementType("CALL_ARGS");
  IElementType EVAL_EXPR = new NixElementType("EVAL_EXPR");
  IElementType EXPR = new NixElementType("EXPR");
  IElementType EXPR_APP = new NixElementType("EXPR_APP");
  IElementType EXPR_FUNCTION = new NixElementType("EXPR_FUNCTION");
  IElementType EXPR_IF = new NixElementType("EXPR_IF");
  IElementType EXPR_LIST = new NixElementType("EXPR_LIST");
  IElementType EXPR_OP = new NixElementType("EXPR_OP");
  IElementType EXPR_SELECT = new NixElementType("EXPR_SELECT");
  IElementType EXPR_SIMPLE = new NixElementType("EXPR_SIMPLE");
  IElementType FACTOR = new NixElementType("FACTOR");
  IElementType FN_LAMBDA = new NixElementType("FN_LAMBDA");
  IElementType FORMAL = new NixElementType("FORMAL");
  IElementType FORMALS = new NixElementType("FORMALS");
  IElementType FORMAL_SET = new NixElementType("FORMAL_SET");
  IElementType IMPORT_STMT = new NixElementType("IMPORT_STMT");
  IElementType IND_STRING_PARTS = new NixElementType("IND_STRING_PARTS");
  IElementType INHERIT_ATTRS = new NixElementType("INHERIT_ATTRS");
  IElementType LAMBDA = new NixElementType("LAMBDA");
  IElementType LET_IN = new NixElementType("LET_IN");
  IElementType LITERAL = new NixElementType("LITERAL");
  IElementType LOGICAL = new NixElementType("LOGICAL");
  IElementType MUL_EXPR = new NixElementType("MUL_EXPR");
  IElementType NIX_INIT = new NixElementType("NIX_INIT");
  IElementType PATHS_ASSIGN = new NixElementType("PATHS_ASSIGN");
  IElementType PATHS_EXPR = new NixElementType("PATHS_EXPR");
  IElementType PATH_ASSIGN = new NixElementType("PATH_ASSIGN");
  IElementType PATH_STMT = new NixElementType("PATH_STMT");
  IElementType PRIMARY = new NixElementType("PRIMARY");
  IElementType PURE_BIND = new NixElementType("PURE_BIND");
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
  IElementType FNUTT = new NixTokenType("\"");
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
  IElementType OW_KW = new NixTokenType("OW_KW");
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
      else if (type == BINDS) {
        return new NixBindsImpl(node);
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
      else if (type == EVAL_EXPR) {
        return new NixEvalExprImpl(node);
      }
      else if (type == EXPR) {
        return new NixExprImpl(node);
      }
      else if (type == EXPR_APP) {
        return new NixExprAppImpl(node);
      }
      else if (type == EXPR_FUNCTION) {
        return new NixExprFunctionImpl(node);
      }
      else if (type == EXPR_IF) {
        return new NixExprIfImpl(node);
      }
      else if (type == EXPR_LIST) {
        return new NixExprListImpl(node);
      }
      else if (type == EXPR_OP) {
        return new NixExprOpImpl(node);
      }
      else if (type == EXPR_SELECT) {
        return new NixExprSelectImpl(node);
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
      else if (type == FORMAL) {
        return new NixFormalImpl(node);
      }
      else if (type == FORMALS) {
        return new NixFormalsImpl(node);
      }
      else if (type == FORMAL_SET) {
        return new NixFormalSetImpl(node);
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
      else if (type == LET_IN) {
        return new NixLetInImpl(node);
      }
      else if (type == LITERAL) {
        return new NixLiteralImpl(node);
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
      else if (type == PATHS_ASSIGN) {
        return new NixPathsAssignImpl(node);
      }
      else if (type == PATHS_EXPR) {
        return new NixPathsExprImpl(node);
      }
      else if (type == PATH_ASSIGN) {
        return new NixPathAssignImpl(node);
      }
      else if (type == PATH_STMT) {
        return new NixPathStmtImpl(node);
      }
      else if (type == PRIMARY) {
        return new NixPrimaryImpl(node);
      }
      else if (type == PURE_BIND) {
        return new NixPureBindImpl(node);
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
