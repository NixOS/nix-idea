// This is a generated file. Not intended for manual editing.
package org.nixos.idea.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.nixos.idea.psi.NixTypes.*;
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
    else if (t == BIND_SET) {
      r = bind_set(b, 0);
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
    else if (t == EVAL_EXPR) {
      r = eval_expr(b, 0);
    }
    else if (t == EXPR) {
      r = expr(b, 0);
    }
    else if (t == EXPR_APP) {
      r = expr_app(b, 0);
    }
    else if (t == EXPR_FUNCTION) {
      r = expr_function(b, 0);
    }
    else if (t == EXPR_IF) {
      r = expr_if(b, 0);
    }
    else if (t == EXPR_LIST) {
      r = expr_list(b, 0);
    }
    else if (t == EXPR_OP) {
      r = expr_op(b, 0);
    }
    else if (t == EXPR_SELECT) {
      r = expr_select(b, 0);
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
    else if (t == FORMAL) {
      r = formal(b, 0);
    }
    else if (t == FORMAL_SET) {
      r = formal_set(b, 0);
    }
    else if (t == FORMALS) {
      r = formals(b, 0);
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
    else if (t == LET_IN) {
      r = let_in(b, 0);
    }
    else if (t == LITERAL) {
      r = literal(b, 0);
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
    else if (t == PATH_ASSIGN) {
      r = path_assign(b, 0);
    }
    else if (t == PATH_STMT) {
      r = path_stmt(b, 0);
    }
    else if (t == PATHS_ASSIGN) {
      r = paths_assign(b, 0);
    }
    else if (t == PATHS_EXPR) {
      r = paths_expr(b, 0);
    }
    else if (t == PRIMARY) {
      r = primary(b, 0);
    }
    else if (t == PURE_BIND) {
      r = pure_bind(b, 0);
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
  // ID | OR_KW | FNUTT string_attr FNUTT
  public static boolean attr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ATTR, "<attr>");
    r = consumeToken(b, ID);
    if (!r) r = consumeToken(b, OR_KW);
    if (!r) r = attr_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // FNUTT string_attr FNUTT
  private static boolean attr_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FNUTT);
    r = r && string_attr(b, l + 1);
    r = r && consumeToken(b, FNUTT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // attr_path ASSIGN expr
  public static boolean attr_assign(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr_assign")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ATTR_ASSIGN, "<attr assign>");
    r = attr_path(b, l + 1);
    r = r && consumeToken(b, ASSIGN);
    p = r; // pin = 2
    r = r && expr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // attr (DOT attr)*
  public static boolean attr_path(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "attr_path")) return false;
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
  // LCURLY binds RCURLY
  public static boolean bind_set(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bind_set")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LCURLY);
    r = r && binds(b, l + 1);
    r = r && consumeToken(b, RCURLY);
    exit_section_(b, m, BIND_SET, r);
    return r;
  }

  /* ********************************************************** */
  // ( (require_expr | inherit_attrs | attr_assign) SEMI) +
  public static boolean binds(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binds")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BINDS, "<binds>");
    r = binds_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!binds_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "binds", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (require_expr | inherit_attrs | attr_assign) SEMI
  private static boolean binds_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binds_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = binds_0_0(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, null, r);
    return r;
  }

  // require_expr | inherit_attrs | attr_assign
  private static boolean binds_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "binds_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = require_expr(b, l + 1);
    if (!r) r = inherit_attrs(b, l + 1);
    if (!r) r = attr_assign(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // formal_set NAMED ID
  public static boolean bn_lambda(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bn_lambda")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = formal_set(b, l + 1);
    r = r && consumeTokens(b, 0, NAMED, ID);
    exit_section_(b, m, BN_LAMBDA, r);
    return r;
  }

  /* ********************************************************** */
  // bool_op logical
  public static boolean bool_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bool_expr")) return false;
    if (!nextTokenIs(b, "<bool expr>", AND, OR)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, BOOL_EXPR, "<bool expr>");
    r = bool_op(b, l + 1);
    r = r && logical(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AND | OR
  static boolean bool_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "bool_op")) return false;
    if (!nextTokenIs(b, "", AND, OR)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AND);
    if (!r) r = consumeToken(b, OR);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LCURLY binds RCURLY
  public static boolean call_args(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "call_args")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CALL_ARGS, null);
    r = consumeToken(b, LCURLY);
    p = r; // pin = 1
    r = r && report_error_(b, binds(b, l + 1));
    r = p && consumeToken(b, RCURLY) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // DOT attr_path
  static boolean cont_path(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "cont_path")) return false;
    if (!nextTokenIs(b, DOT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && attr_path(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IND_STRING_OPEN ind_string_parts IND_STRING_CLOSE
  static boolean doc_string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doc_string")) return false;
    if (!nextTokenIs(b, IND_STRING_OPEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IND_STRING_OPEN);
    r = r && ind_string_parts(b, l + 1);
    r = r && consumeToken(b, IND_STRING_CLOSE);
    exit_section_(b, m, null, r);
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
  // expr_function
  public static boolean expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR, "<expr>");
    r = expr_function(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // expr_select+
  public static boolean expr_app(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_app")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_APP, "<expr app>");
    r = expr_select(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!expr_select(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expr_app", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ID COLON expr_function
  //                 | expr_if
  //                 | WITH expr SEMI expr_function
  //                 | ASSERT expr SEMI expr_function
  //                 | let_in expr_function
  //                 | lambda COLON expr_function
  public static boolean expr_function(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_function")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_FUNCTION, "<expr function>");
    r = expr_function_0(b, l + 1);
    if (!r) r = expr_if(b, l + 1);
    if (!r) r = expr_function_2(b, l + 1);
    if (!r) r = expr_function_3(b, l + 1);
    if (!r) r = expr_function_4(b, l + 1);
    if (!r) r = expr_function_5(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ID COLON expr_function
  private static boolean expr_function_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_function_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ID, COLON);
    r = r && expr_function(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // WITH expr SEMI expr_function
  private static boolean expr_function_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_function_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, WITH);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, SEMI);
    r = r && expr_function(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ASSERT expr SEMI expr_function
  private static boolean expr_function_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_function_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSERT);
    r = r && expr(b, l + 1);
    r = r && consumeToken(b, SEMI);
    r = r && expr_function(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // let_in expr_function
  private static boolean expr_function_4(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_function_4")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = let_in(b, l + 1);
    r = r && expr_function(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // lambda COLON expr_function
  private static boolean expr_function_5(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_function_5")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = lambda(b, l + 1);
    r = r && consumeToken(b, COLON);
    r = r && expr_function(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IF expr THEN expr ELSE expr
  //           | expr_op
  public static boolean expr_if(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_if")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_IF, "<expr if>");
    r = expr_if_0(b, l + 1);
    if (!r) r = expr_op(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // IF expr THEN expr ELSE expr
  private static boolean expr_if_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_if_0")) return false;
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

  /* ********************************************************** */
  // expr_select*
  public static boolean expr_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_list")) return false;
    Marker m = enter_section_(b, l, _NONE_, EXPR_LIST, "<expr list>");
    int c = current_position_(b);
    while (true) {
      if (!expr_select(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "expr_list", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // logical bool_expr* | unary_op logical
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

  // unary_op logical
  private static boolean expr_op_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_op_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = unary_op(b, l + 1);
    r = r && logical(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // expr_simple ( (cont_path (or_select)?) | OR_KW )?
  public static boolean expr_select(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_select")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_SELECT, "<expr select>");
    r = expr_simple(b, l + 1);
    r = r && expr_select_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ( (cont_path (or_select)?) | OR_KW )?
  private static boolean expr_select_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_select_1")) return false;
    expr_select_1_0(b, l + 1);
    return true;
  }

  // (cont_path (or_select)?) | OR_KW
  private static boolean expr_select_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_select_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = expr_select_1_0_0(b, l + 1);
    if (!r) r = consumeToken(b, OR_KW);
    exit_section_(b, m, null, r);
    return r;
  }

  // cont_path (or_select)?
  private static boolean expr_select_1_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_select_1_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = cont_path(b, l + 1);
    r = r && expr_select_1_0_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (or_select)?
  private static boolean expr_select_1_0_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_select_1_0_0_1")) return false;
    expr_select_1_0_0_1_0(b, l + 1);
    return true;
  }

  // (or_select)
  private static boolean expr_select_1_0_0_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_select_1_0_0_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = or_select(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // literal_simple_string
  //               | doc_string
  //                 /* Let expressions `let {..., body = ...}' are just desugared
  //                    into `(rec {..., body = ...}).body'. */
  //               | LET bind_set
  //               | REC bind_set
  //               | literal_list
  //               | import_stmt
  //               | literal
  //               | bind_set
  //               | eval_expr
  public static boolean expr_simple(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_simple")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, EXPR_SIMPLE, "<expr simple>");
    r = literal_simple_string(b, l + 1);
    if (!r) r = doc_string(b, l + 1);
    if (!r) r = expr_simple_2(b, l + 1);
    if (!r) r = expr_simple_3(b, l + 1);
    if (!r) r = literal_list(b, l + 1);
    if (!r) r = import_stmt(b, l + 1);
    if (!r) r = literal(b, l + 1);
    if (!r) r = bind_set(b, l + 1);
    if (!r) r = eval_expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // LET bind_set
  private static boolean expr_simple_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_simple_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LET);
    r = r && bind_set(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // REC bind_set
  private static boolean expr_simple_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "expr_simple_3")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, REC);
    r = r && bind_set(b, l + 1);
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
  // ID NAMED formal_set
  public static boolean fn_lambda(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "fn_lambda")) return false;
    if (!nextTokenIs(b, ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ID, NAMED);
    r = r && formal_set(b, l + 1);
    exit_section_(b, m, FN_LAMBDA, r);
    return r;
  }

  /* ********************************************************** */
  // ID
  //          | ID IS expr
  //          | ELLIPSIS
  public static boolean formal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formal")) return false;
    if (!nextTokenIs(b, "<formal>", ELLIPSIS, ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FORMAL, "<formal>");
    r = consumeToken(b, ID);
    if (!r) r = formal_1(b, l + 1);
    if (!r) r = consumeToken(b, ELLIPSIS);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // ID IS expr
  private static boolean formal_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formal_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, ID, IS);
    r = r && expr(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LCURLY formals RCURLY
  public static boolean formal_set(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formal_set")) return false;
    if (!nextTokenIs(b, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LCURLY);
    r = r && formals(b, l + 1);
    r = r && consumeToken(b, RCURLY);
    exit_section_(b, m, FORMAL_SET, r);
    return r;
  }

  /* ********************************************************** */
  // formal (COMMA formal)*
  public static boolean formals(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formals")) return false;
    if (!nextTokenIs(b, "<formals>", ELLIPSIS, ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FORMALS, "<formals>");
    r = formal(b, l + 1);
    r = r && formals_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (COMMA formal)*
  private static boolean formals_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formals_1")) return false;
    int c = current_position_(b);
    while (true) {
      if (!formals_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "formals_1", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // COMMA formal
  private static boolean formals_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "formals_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMA);
    r = r && formal(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // IMPORT path_stmt call_args?
  public static boolean import_stmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "import_stmt")) return false;
    if (!nextTokenIs(b, IMPORT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, IMPORT);
    r = r && path_stmt(b, l + 1);
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
  // fn_lambda formal_set | bn_lambda | formal_set
  public static boolean lambda(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lambda")) return false;
    if (!nextTokenIs(b, "<lambda>", ID, LCURLY)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LAMBDA, "<lambda>");
    r = lambda_0(b, l + 1);
    if (!r) r = bn_lambda(b, l + 1);
    if (!r) r = formal_set(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // fn_lambda formal_set
  private static boolean lambda_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "lambda_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = fn_lambda(b, l + 1);
    r = r && formal_set(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LET pure_bind IN
  public static boolean let_in(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "let_in")) return false;
    if (!nextTokenIs(b, LET)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LET);
    r = r && pure_bind(b, l + 1);
    r = r && consumeToken(b, IN);
    exit_section_(b, m, LET_IN, r);
    return r;
  }

  /* ********************************************************** */
  // ID | INT | BOOL | PATH | SPATH | HPATH | URI | STR
  public static boolean literal(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, LITERAL, "<literal>");
    r = consumeToken(b, ID);
    if (!r) r = consumeToken(b, INT);
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
  // LBRAC expr_list RBRAC
  static boolean literal_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_list")) return false;
    if (!nextTokenIs(b, LBRAC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRAC);
    r = r && expr_list(b, l + 1);
    r = r && consumeToken(b, RBRAC);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // FNUTT string_parts FNUTT
  static boolean literal_simple_string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "literal_simple_string")) return false;
    if (!nextTokenIs(b, FNUTT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FNUTT);
    r = r && string_parts(b, l + 1);
    r = r && consumeToken(b, FNUTT);
    exit_section_(b, m, null, r);
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
    boolean r;
    Marker m = enter_section_(b, l, _LEFT_, MUL_EXPR, "<mul expr>");
    r = mul_op(b, l + 1);
    r = r && primary(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // TIMES | DIVIDE | IMPL
  static boolean mul_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "mul_op")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, TIMES);
    if (!r) r = consumeToken(b, DIVIDE);
    if (!r) r = consumeToken(b, IMPL);
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
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NIX_INIT, null);
    r = consumeToken(b, DOLLAR_CURLY);
    p = r; // pin = 1
    r = r && report_error_(b, expr(b, l + 1));
    r = p && consumeToken(b, RCURLY) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // OW_KW expr_select
  static boolean or_select(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "or_select")) return false;
    if (!nextTokenIs(b, OW_KW)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OW_KW);
    r = r && expr_select(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ASSIGN path_stmt
  public static boolean path_assign(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "path_assign")) return false;
    if (!nextTokenIs(b, ASSIGN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && path_stmt(b, l + 1);
    exit_section_(b, m, PATH_ASSIGN, r);
    return r;
  }

  /* ********************************************************** */
  // PATH | HPATH | SPATH | literal_simple_string
  public static boolean path_stmt(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "path_stmt")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PATH_STMT, "<path stmt>");
    r = consumeToken(b, PATH);
    if (!r) r = consumeToken(b, HPATH);
    if (!r) r = consumeToken(b, SPATH);
    if (!r) r = literal_simple_string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ASSIGN paths_expr
  public static boolean paths_assign(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paths_assign")) return false;
    if (!nextTokenIs(b, ASSIGN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, ASSIGN);
    r = r && paths_expr(b, l + 1);
    exit_section_(b, m, PATHS_ASSIGN, r);
    return r;
  }

  /* ********************************************************** */
  // LBRAC (path_stmt)+ RBRAC
  public static boolean paths_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paths_expr")) return false;
    if (!nextTokenIs(b, LBRAC)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LBRAC);
    r = r && paths_expr_1(b, l + 1);
    r = r && consumeToken(b, RBRAC);
    exit_section_(b, m, PATHS_EXPR, r);
    return r;
  }

  // (path_stmt)+
  private static boolean paths_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paths_expr_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = paths_expr_1_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!paths_expr_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "paths_expr_1", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // (path_stmt)
  private static boolean paths_expr_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "paths_expr_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = path_stmt(b, l + 1);
    exit_section_(b, m, null, r);
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
  // (attr_assign SEMI)+
  public static boolean pure_bind(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pure_bind")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PURE_BIND, "<pure bind>");
    r = pure_bind_0(b, l + 1);
    int c = current_position_(b);
    while (r) {
      if (!pure_bind_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "pure_bind", c)) break;
      c = current_position_(b);
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // attr_assign SEMI
  private static boolean pure_bind_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "pure_bind_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = attr_assign(b, l + 1);
    r = r && consumeToken(b, SEMI);
    exit_section_(b, m, null, r);
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
  // (REQUIRE|REQUIRES|IMPORTS) paths_assign
  public static boolean require_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "require_expr")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, REQUIRE_EXPR, "<require expr>");
    r = require_expr_0(b, l + 1);
    r = r && paths_assign(b, l + 1);
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
