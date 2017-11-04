// This is a generated file. Not intended for manual editing.
package org.nixos.idea.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.nixos.idea.psi.NixTypes.*;
import static org.nixos.idea.psi.NixParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class NixParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == ADD_EXPR) {
      r = add_expr(b, 0);
    }
    else if (t == ATTR) {
      r = attr(b, 0);
    }
    else if (t == ATTR_ASSIGN) {
      r = attr_assign(b, 0);
    }
    else if (t == ATTR_PATH) {
      r = attr_path(b, 0);
    }
    else if (t == ATTRS) {
      r = attrs(b, 0);
    }
    else if (t == BIND_OR_SELECT) {
      r = bind_or_select(b, 0);
    }
    else if (t == BIND_SET) {
      r = bind_set(b, 0);
    }
    else if (t == BINDING) {
      r = binding(b, 0);
    }
    else if (t == BINDS) {
      r = binds(b, 0);
    }
    else if (t == BN_LAMBDA) {
      r = bn_lambda(b, 0);
    }
    else if (t == BOOL_EXPR) {
      r = bool_expr(b, 0);
    }
    else if (t == CALL_ARGS) {
      r = call_args(b, 0);
    }
    else if (t == CONT_PATH) {
      r = cont_path(b, 0);
    }
    else if (t == DEFVAL) {
      r = defval(b, 0);
    }
    else if (t == DOC_STRING) {
      r = doc_string(b, 0);
    }
    else if (t == EVAL_EXPR) {
      r = eval_expr(b, 0);
    }
    else if (t == EVAL_OR_SELECT) {
      r = eval_or_select(b, 0);
    }
    else if (t == EXPR) {
      r = expr(b, 0);
    }
    else if (t == EXPR_APP) {
      r = expr_app(b, 0);
    }
    else if (t == EXPR_LIST) {
      r = expr_list(b, 0);
    }
    else if (t == EXPR_OP) {
      r = expr_op(b, 0);
    }
    else if (t == EXPR_SIMPLE) {
      r = expr_simple(b, 0);
    }
    else if (t == FACTOR) {
      r = factor(b, 0);
    }
    else if (t == FN_LAMBDA) {
      r = fn_lambda(b, 0);
    }
    else if (t == IMPORT_STMT) {
      r = import_stmt(b, 0);
    }
    else if (t == IND_STRING_PARTS) {
      r = ind_string_parts(b, 0);
    }
    else if (t == INHERIT_ATTRS) {
      r = inherit_attrs(b, 0);
    }
    else if (t == LAMBDA) {
      r = lambda(b, 0);
    }
    else if (t == LIST) {
      r = list(b, 0);
    }
    else if (t == LIST_EXPR) {
      r = list_expr(b, 0);
    }
    else if (t == LITERAL) {
      r = literal(b, 0);
    }
    else if (t == LITERAL_SIMPLE_STRING) {
      r = literal_simple_string(b, 0);
    }
    else if (t == LOGICAL) {
      r = logical(b, 0);
    }
    else if (t == MUL_EXPR) {
      r = mul_expr(b, 0);
    }
    else if (t == NIX_INIT) {
      r = nix_init(b, 0);
    }
    else if (t == PARAM) {
      r = param(b, 0);
    }
    else if (t == PARAM_SET) {
      r = param_set(b, 0);
    }
    else if (t == PARAMS) {
      r = params(b, 0);
    }
    else if (t == PATH_EXPR) {
      r = path_expr(b, 0);
    }
    else if (t == PRIMARY) {
      r = primary(b, 0);
    }
    else if (t == REL_EXPR) {
      r = rel_expr(b, 0);
    }
    else if (t == RELATIVE) {
      r = relative(b, 0);
    }
    else if (t == REQUIRE_EXPR) {
      r = require_expr(b, 0);
    }
    else if (t == STRING_ATTR) {
      r = string_attr(b, 0);
    }
    else if (t == STRING_PARTS) {
      r = string_parts(b, 0);
    }
    else if (t == UNARY_OP) {
      r = unary_op(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return nixFile(b, l + 1);
  }

  /* ********************************************************** */
  // add_op factor
  public static boolean add_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "add_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, ADD_EXPR, "<add expr>");
    r = add_op(b, l + 1);
    r = r && factor(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PLUS | MINUS | CONCAT | UPDATE
  static boolean add_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "add_op")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PLUS);
    if (!r) r = consumeToken(b, MINUS);
    if (!r) r = consumeToken(b, CONCAT);
    if (!r) r = consumeToken(b, UPDATE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ID | FNUTT_OPEN string_attr FNUTT_CLOSE
  public static boolean attr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr")) return false;
    if (!nextTokenIs(b, "<attr>", FNUTT_OPEN, ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTR, "<attr>");
    r = consumeToken(b, ID);
    if (!r) r = attr_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FNUTT_OPEN string_attr FNUTT_CLOSE
  private static boolean attr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FNUTT_OPEN);
    r = r && string_attr(b, l + 1);
    r = r && consumeToken(b, FNUTT_CLOSE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // attr_path ASSIGN expr
  public static boolean attr_assign(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr_assign")) return false;
    if (!nextTokenIs(b, "<attr assign>", FNUTT_OPEN, ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTR_ASSIGN, "<attr assign>");
    r = attr_path(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    r = r && expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // attr (DOT attr)*
  public static boolean attr_path(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr_path")) return false;
    if (!nextTokenIs(b, "<attr path>", FNUTT_OPEN, ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTR_PATH, "<attr path>");
    r = attr(b, l + 1);
    r = r && attr_path_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (DOT attr)*
  private static boolean attr_path_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr_path_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!attr_path_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "attr_path_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // DOT attr
  private static boolean attr_path_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr_path_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && attr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // attr*
  public static boolean attrs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attrs")) return false;
    Marker m = enter_section_(b, l, _NONE_, ATTRS, "<attrs>");
    int c = current_position_(b);
    while (true) {
      if (!attr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "attrs", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // bind_set cont_path?
  public static boolean bind_or_select(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_or_select")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = bind_set(b, l + 1);
    r = r && bind_or_select_1(b, l + 1);
    exit_section_(b, m, BIND_OR_SELECT, r);
    return r;
  }

  // cont_path?
  private static boolean bind_or_select_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_or_select_1")) return false;
    cont_path(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // LCURLY binds? RCURLY
  public static boolean bind_set(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_set")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LCURLY);
    r = r && bind_set_1(b, l + 1);
    r = r && consumeToken(b, RCURLY);
    exit_section_(b, m, BIND_SET, r);
    return r;
  }

  // binds?
  private static boolean bind_set_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_set_1")) return false;
    binds(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (require_expr | inherit_attrs | attr_assign) SEMI
  public static boolean binding(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binding")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BINDING, "<binding>");
    r = binding_0(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // require_expr | inherit_attrs | attr_assign
  private static boolean binding_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binding_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = require_expr(b, l + 1);
    if (!r) r = inherit_attrs(b, l + 1);
    if (!r) r = attr_assign(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // binding+
  public static boolean binds(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binds")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BINDS, "<binds>");
    r = binding(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!binding(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "binds", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // param_set NAMED ID
  public static boolean bn_lambda(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bn_lambda")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = param_set(b, l + 1);
    r = r && consumeTokens(b, 0, NAMED, ID);
    exit_section_(b, m, BN_LAMBDA, r);
    return r;
  }

  /* ********************************************************** */
  // bool_op logical
  public static boolean bool_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bool_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, BOOL_EXPR, "<bool expr>");
    r = bool_op(b, l + 1);
    r = r && logical(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AND | OR | IMPL | OR_KW
  static boolean bool_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bool_op")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OR);
    if (!r) r = consumeToken(b, IMPL);
    if (!r) r = consumeToken(b, OR_KW);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LCURLY binds? RCURLY
  public static boolean call_args(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "call_args")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LCURLY);
    r = r && call_args_1(b, l + 1);
    r = r && consumeToken(b, RCURLY);
    exit_section_(b, m, CALL_ARGS, r);
    return r;
  }

  // binds?
  private static boolean call_args_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "call_args_1")) return false;
    binds(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // DOT attr_path
  public static boolean cont_path(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cont_path")) return false;
    if (!nextTokenIs(b, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && attr_path(b, l + 1);
    exit_section_(b, m, CONT_PATH, r);
    return r;
  }

  /* ********************************************************** */
  // ID IS expr
  public static boolean defval(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "defval")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ID, IS);
    r = r && expr(b, l + 1);
    exit_section_(b, m, DEFVAL, r);
    return r;
  }

  /* ********************************************************** */
  // IND_STRING_OPEN ind_string_parts IND_STRING_CLOSE
  public static boolean doc_string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doc_string")) return false;
    if (!nextTokenIs(b, IND_STRING_OPEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IND_STRING_OPEN);
    r = r && ind_string_parts(b, l + 1);
    r = r && consumeToken(b, IND_STRING_CLOSE);
    exit_section_(b, m, DOC_STRING, r);
    return r;
  }

  /* ********************************************************** */
  // LPAREN expr RPAREN
  public static boolean eval_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eval_expr")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, EVAL_EXPR, r);
    return r;
  }

  /* ********************************************************** */
  // eval_expr cont_path?
  public static boolean eval_or_select(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eval_or_select")) return false;
    if (!nextTokenIs(b, LPAREN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = eval_expr(b, l + 1);
    r = r && eval_or_select_1(b, l + 1);
    exit_section_(b, m, EVAL_OR_SELECT, r);
    return r;
  }

  // cont_path?
  private static boolean eval_or_select_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "eval_or_select_1")) return false;
    cont_path(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // lambda
  //        | IF expr THEN expr ELSE expr
  //        | WITH expr SEMI expr
  //        | ASSERT expr_op SEMI expr
  //        | LET binds IN expr
  //        | expr_op
  public static boolean expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR, "<expr>");
    r = lambda(b, l + 1);
    if (!r) r = expr_1(b, l + 1);
    if (!r) r = expr_2(b, l + 1);
    if (!r) r = expr_3(b, l + 1);
    if (!r) r = expr_4(b, l + 1);
    if (!r) r = expr_op(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // IF expr THEN expr ELSE expr
  private static boolean expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IF);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, THEN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, ELSE);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // WITH expr SEMI expr
  private static boolean expr_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WITH);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, SEMI);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ASSERT expr_op SEMI expr
  private static boolean expr_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSERT);
    r = r && expr_op(b, l + 1);
    r = r && consumeToken(b, SEMI);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LET binds IN expr
  private static boolean expr_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LET);
    r = r && binds(b, l + 1);
    r = r && consumeToken(b, IN);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expr_simple+
  public static boolean expr_app(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_app")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_APP, "<expr app>");
    r = expr_simple(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!expr_simple(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expr_app", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // list_expr*
  public static boolean expr_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_list")) return false;
    Marker m = enter_section_(b, l, _NONE_, EXPR_LIST, "<expr list>");
    int c = current_position_(b);
    while (true) {
      if (!list_expr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expr_list", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // logical bool_expr* | unary_op expr_op
  public static boolean expr_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_op")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_OP, "<expr op>");
    r = expr_op_0(b, l + 1);
    if (!r) r = expr_op_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // logical bool_expr*
  private static boolean expr_op_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_op_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = logical(b, l + 1);
    r = r && expr_op_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // bool_expr*
  private static boolean expr_op_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_op_0_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!bool_expr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expr_op_0_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // unary_op expr_op
  private static boolean expr_op_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_op_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unary_op(b, l + 1);
    r = r && expr_op(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // literal_simple_string
  //               | doc_string
  //               | REC bind_set
  //               | list
  //               | import_stmt
  //               | literal
  //               | bind_or_select
  //               | eval_or_select
  //               | defval
  //               | attr_path
  //               | unary_op expr_op
  public static boolean expr_simple(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_simple")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_SIMPLE, "<expr simple>");
    r = literal_simple_string(b, l + 1);
    if (!r) r = doc_string(b, l + 1);
    if (!r) r = expr_simple_2(b, l + 1);
    if (!r) r = list(b, l + 1);
    if (!r) r = import_stmt(b, l + 1);
    if (!r) r = literal(b, l + 1);
    if (!r) r = bind_or_select(b, l + 1);
    if (!r) r = eval_or_select(b, l + 1);
    if (!r) r = defval(b, l + 1);
    if (!r) r = attr_path(b, l + 1);
    if (!r) r = expr_simple_10(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // REC bind_set
  private static boolean expr_simple_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_simple_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REC);
    r = r && bind_set(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // unary_op expr_op
  private static boolean expr_simple_10(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_simple_10")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unary_op(b, l + 1);
    r = r && expr_op(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // primary mul_expr*
  public static boolean factor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factor")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FACTOR, "<factor>");
    r = primary(b, l + 1);
    r = r && factor_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // mul_expr*
  private static boolean factor_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "factor_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!mul_expr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "factor_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // ID NAMED param_set
  public static boolean fn_lambda(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fn_lambda")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ID, NAMED);
    r = r && param_set(b, l + 1);
    exit_section_(b, m, FN_LAMBDA, r);
    return r;
  }

  /* ********************************************************** */
  // IMPORT path_expr call_args?
  public static boolean import_stmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_stmt")) return false;
    if (!nextTokenIs(b, IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IMPORT);
    r = r && path_expr(b, l + 1);
    r = r && import_stmt_2(b, l + 1);
    exit_section_(b, m, IMPORT_STMT, r);
    return r;
  }

  // call_args?
  private static boolean import_stmt_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_stmt_2")) return false;
    call_args(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (IND_STR | nix_init)*
  public static boolean ind_string_parts(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ind_string_parts")) return false;
    Marker m = enter_section_(b, l, _NONE_, IND_STRING_PARTS, "<ind string parts>");
    int c = current_position_(b);
    while (true) {
      if (!ind_string_parts_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "ind_string_parts", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // IND_STR | nix_init
  private static boolean ind_string_parts_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "ind_string_parts_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IND_STR);
    if (!r) r = nix_init(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INHERIT (LPAREN expr RPAREN)? attrs
  public static boolean inherit_attrs(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inherit_attrs")) return false;
    if (!nextTokenIs(b, INHERIT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INHERIT);
    r = r && inherit_attrs_1(b, l + 1);
    r = r && attrs(b, l + 1);
    exit_section_(b, m, INHERIT_ATTRS, r);
    return r;
  }

  // (LPAREN expr RPAREN)?
  private static boolean inherit_attrs_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inherit_attrs_1")) return false;
    inherit_attrs_1_0(b, l + 1);
    return true;
  }

  // LPAREN expr RPAREN
  private static boolean inherit_attrs_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inherit_attrs_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LPAREN);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, RPAREN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // (fn_lambda | bn_lambda | param_set | ID) COLON expr
  public static boolean lambda(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lambda")) return false;
    if (!nextTokenIs(b, "<lambda>", ID, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LAMBDA, "<lambda>");
    r = lambda_0(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // fn_lambda | bn_lambda | param_set | ID
  private static boolean lambda_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lambda_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fn_lambda(b, l + 1);
    if (!r) r = bn_lambda(b, l + 1);
    if (!r) r = param_set(b, l + 1);
    if (!r) r = consumeToken(b, ID);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LBRAC expr_list RBRAC
  public static boolean list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "list")) return false;
    if (!nextTokenIs(b, LBRAC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRAC);
    r = r && expr_list(b, l + 1);
    r = r && consumeToken(b, RBRAC);
    exit_section_(b, m, LIST, r);
    return r;
  }

  /* ********************************************************** */
  // eval_or_select | bind_or_select | attr_path | literal_simple_string | literal
  public static boolean list_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "list_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LIST_EXPR, "<list expr>");
    r = eval_or_select(b, l + 1);
    if (!r) r = bind_or_select(b, l + 1);
    if (!r) r = attr_path(b, l + 1);
    if (!r) r = literal_simple_string(b, l + 1);
    if (!r) r = literal(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // INT | BOOL | PATH | SPATH | HPATH | URI | STR
  public static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL, "<literal>");
    r = consumeToken(b, INT);
    if (!r) r = consumeToken(b, BOOL);
    if (!r) r = consumeToken(b, PATH);
    if (!r) r = consumeToken(b, SPATH);
    if (!r) r = consumeToken(b, HPATH);
    if (!r) r = consumeToken(b, URI);
    if (!r) r = consumeToken(b, STR);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // FNUTT_OPEN string_parts FNUTT_CLOSE
  public static boolean literal_simple_string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_simple_string")) return false;
    if (!nextTokenIs(b, FNUTT_OPEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FNUTT_OPEN);
    r = r && string_parts(b, l + 1);
    r = r && consumeToken(b, FNUTT_CLOSE);
    exit_section_(b, m, LITERAL_SIMPLE_STRING, r);
    return r;
  }

  /* ********************************************************** */
  // relative rel_expr*
  public static boolean logical(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logical")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LOGICAL, "<logical>");
    r = relative(b, l + 1);
    r = r && logical_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // rel_expr*
  private static boolean logical_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "logical_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!rel_expr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "logical_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // mul_op primary
  public static boolean mul_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mul_expr")) return false;
    if (!nextTokenIs(b, "<mul expr>", DIVIDE, TIMES)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, MUL_EXPR, "<mul expr>");
    r = mul_op(b, l + 1);
    r = r && primary(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TIMES | DIVIDE
  static boolean mul_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mul_op")) return false;
    if (!nextTokenIs(b, "", DIVIDE, TIMES)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TIMES);
    if (!r) r = consumeToken(b, DIVIDE);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expr
  static boolean nixFile(PsiBuilder b, int l) {
    return expr(b, l + 1);
  }

  /* ********************************************************** */
  // DOLLAR_CURLY expr RCURLY
  public static boolean nix_init(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "nix_init")) return false;
    if (!nextTokenIs(b, DOLLAR_CURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOLLAR_CURLY);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, RCURLY);
    exit_section_(b, m, NIX_INIT, r);
    return r;
  }

  /* ********************************************************** */
  // defval
  //          | ID
  //          | ELLIPSIS
  public static boolean param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "param")) return false;
    if (!nextTokenIs(b, "<param>", ELLIPSIS, ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAM, "<param>");
    r = defval(b, l + 1);
    if (!r) r = consumeToken(b, ID);
    if (!r) r = consumeToken(b, ELLIPSIS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LCURLY params RCURLY
  public static boolean param_set(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "param_set")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LCURLY);
    r = r && params(b, l + 1);
    r = r && consumeToken(b, RCURLY);
    exit_section_(b, m, PARAM_SET, r);
    return r;
  }

  /* ********************************************************** */
  // param (COMMA param)*
  public static boolean params(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "params")) return false;
    if (!nextTokenIs(b, "<params>", ELLIPSIS, ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMS, "<params>");
    r = param(b, l + 1);
    r = r && params_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA param)*
  private static boolean params_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "params_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!params_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "params_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA param
  private static boolean params_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "params_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && param(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PATH | HPATH | SPATH | list_expr
  public static boolean path_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "path_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATH_EXPR, "<path expr>");
    r = consumeToken(b, PATH);
    if (!r) r = consumeToken(b, HPATH);
    if (!r) r = consumeToken(b, SPATH);
    if (!r) r = list_expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // expr_app
  public static boolean primary(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "primary")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PRIMARY, "<primary>");
    r = expr_app(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // rel_op relative
  public static boolean rel_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rel_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, REL_EXPR, "<rel expr>");
    r = rel_op(b, l + 1);
    r = r && relative(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // EQ | NEQ | LEQ | GEQ | GT | LT
  static boolean rel_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rel_op")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, EQ);
    if (!r) r = consumeToken(b, NEQ);
    if (!r) r = consumeToken(b, LEQ);
    if (!r) r = consumeToken(b, GEQ);
    if (!r) r = consumeToken(b, GT);
    if (!r) r = consumeToken(b, LT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // factor add_expr*
  public static boolean relative(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relative")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, RELATIVE, "<relative>");
    r = factor(b, l + 1);
    r = r && relative_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // add_expr*
  private static boolean relative_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "relative_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!add_expr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "relative_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // (REQUIRE|REQUIRES|IMPORTS) ASSIGN LBRAC path_expr* RBRAC
  public static boolean require_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "require_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REQUIRE_EXPR, "<require expr>");
    r = require_expr_0(b, l + 1);
    r = r && consumeTokens(b, 0, ASSIGN, LBRAC);
    r = r && require_expr_3(b, l + 1);
    r = r && consumeToken(b, RBRAC);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // REQUIRE|REQUIRES|IMPORTS
  private static boolean require_expr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "require_expr_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REQUIRE);
    if (!r) r = consumeToken(b, REQUIRES);
    if (!r) r = consumeToken(b, IMPORTS);
    exit_section_(b, m, null, r);
    return r;
  }

  // path_expr*
  private static boolean require_expr_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "require_expr_3")) return false;
    int c = current_position_(b);
    while (true) {
      if (!path_expr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "require_expr_3", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  /* ********************************************************** */
  // STR
  //               | nix_init
  public static boolean string_attr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_attr")) return false;
    if (!nextTokenIs(b, "<string attr>", DOLLAR_CURLY, STR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING_ATTR, "<string attr>");
    r = consumeToken(b, STR);
    if (!r) r = nix_init(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // (STR | nix_init)*
  public static boolean string_parts(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_parts")) return false;
    Marker m = enter_section_(b, l, _NONE_, STRING_PARTS, "<string parts>");
    int c = current_position_(b);
    while (true) {
      if (!string_parts_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "string_parts", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // STR | nix_init
  private static boolean string_parts_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_parts_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STR);
    if (!r) r = nix_init(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // NOT | MINUS
  public static boolean unary_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "unary_op")) return false;
    if (!nextTokenIs(b, "<unary op>", MINUS, NOT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, UNARY_OP, "<unary op>");
    r = consumeToken(b, NOT);
    if (!r) r = consumeToken(b, MINUS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
