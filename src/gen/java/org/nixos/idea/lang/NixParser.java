// This is a generated file. Not intended for manual editing.
package org.nixos.idea.lang;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static org.nixos.idea.psi.NixTypes.*;
import static org.nixos.idea.psi.impl.NixParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class NixParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, EXTENDS_SETS_);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return nixFile(builder_, level_ + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(ANTIQUOTATION, STRING_PART, STRING_TEXT),
    create_token_set_(BIND, BIND_ATTR, BIND_INHERIT),
    create_token_set_(ATTR, STD_ATTR, STRING_ATTR),
    create_token_set_(EXPR, EXPR_APP, EXPR_ASSERT, EXPR_IF,
      EXPR_LAMBDA, EXPR_LET, EXPR_OP, EXPR_OP_AND,
      EXPR_OP_BASE, EXPR_OP_CONCAT, EXPR_OP_DIV, EXPR_OP_EQ,
      EXPR_OP_GE, EXPR_OP_GT, EXPR_OP_HAS, EXPR_OP_IMPLICATION,
      EXPR_OP_LE, EXPR_OP_LT, EXPR_OP_MINUS, EXPR_OP_MUL,
      EXPR_OP_NE, EXPR_OP_NEG, EXPR_OP_NOT, EXPR_OP_OR,
      EXPR_OP_PLUS, EXPR_OP_UPDATE, EXPR_SELECT, EXPR_SIMPLE,
      EXPR_WITH, IDENTIFIER, IND_STRING, LEGACY_APP_OR,
      LEGACY_LET, LIST, LITERAL, PARENS,
      PATH, SET, STD_STRING, STRING),
  };

  /* ********************************************************** */
  // DOLLAR LCURLY expr recover_antiquotation RCURLY
  public static boolean antiquotation(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "antiquotation")) return false;
    if (!nextTokenIs(builder_, DOLLAR)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ANTIQUOTATION, null);
    result_ = consumeTokens(builder_, 1, DOLLAR, LCURLY);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expr(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, recover_antiquotation(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RCURLY) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // std_attr | string_attr
  public static boolean attr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, ATTR, "<attr>");
    result_ = std_attr(builder_, level_ + 1);
    if (!result_) result_ = string_attr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // attr ( DOT attr )*
  public static boolean attr_path(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attr_path")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ATTR_PATH, "<attr path>");
    result_ = attr(builder_, level_ + 1);
    result_ = result_ && attr_path_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( DOT attr )*
  private static boolean attr_path_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attr_path_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!attr_path_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "attr_path_1", pos_)) break;
    }
    return true;
  }

  // DOT attr
  private static boolean attr_path_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "attr_path_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, DOT);
    result_ = result_ && attr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // bind_attr | bind_inherit
  public static boolean bind(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bind")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, BIND, "<bind>");
    result_ = bind_attr(builder_, level_ + 1);
    if (!result_) result_ = bind_inherit(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // attr_path ASSIGN bind_value SEMI
  public static boolean bind_attr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bind_attr")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BIND_ATTR, "<bind attr>");
    result_ = attr_path(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, ASSIGN);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, bind_value(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, SEMI) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // INHERIT [ LPAREN expr RPAREN ] attr* SEMI
  public static boolean bind_inherit(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bind_inherit")) return false;
    if (!nextTokenIs(builder_, INHERIT)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BIND_INHERIT, null);
    result_ = consumeToken(builder_, INHERIT);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, bind_inherit_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, bind_inherit_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, SEMI) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [ LPAREN expr RPAREN ]
  private static boolean bind_inherit_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bind_inherit_1")) return false;
    bind_inherit_1_0(builder_, level_ + 1);
    return true;
  }

  // LPAREN expr RPAREN
  private static boolean bind_inherit_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bind_inherit_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, LPAREN);
    result_ = result_ && expr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, RPAREN);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // attr*
  private static boolean bind_inherit_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bind_inherit_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!attr(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "bind_inherit_2", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // <<parseBindValue expr0>>
  public static boolean bind_value(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bind_value")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPR, "<bind value>");
    result_ = parseBindValue(builder_, level_ + 1, NixParser::expr0);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // !(RBRAC | RCURLY)
  static boolean brac_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "brac_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !brac_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RBRAC | RCURLY
  private static boolean brac_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "brac_recover_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, RBRAC);
    if (!result_) result_ = consumeTokenFast(builder_, RCURLY);
    return result_;
  }

  /* ********************************************************** */
  // !(RPAREN | RCURLY | RBRAC)
  static boolean braces_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braces_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !braces_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RPAREN | RCURLY | RBRAC
  private static boolean braces_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "braces_recover_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, RPAREN);
    if (!result_) result_ = consumeTokenFast(builder_, RCURLY);
    if (!result_) result_ = consumeTokenFast(builder_, RBRAC);
    return result_;
  }

  /* ********************************************************** */
  // !RCURLY
  static boolean curly_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "curly_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeTokenFast(builder_, RCURLY);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ELSE expr
  static boolean else_$(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "else_$")) return false;
    if (!nextTokenIs(builder_, ELSE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, ELSE);
    pinned_ = result_; // pin = 1
    result_ = result_ && expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // <<parseNonBindValue expr0>>
  public static boolean expr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPR, "<expr>");
    result_ = parseNonBindValue(builder_, level_ + 1, NixParser::expr0);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // expr_assert
  //   | expr_if
  //   | expr_let
  //   | expr_with
  //   | expr_lambda
  //   | expr_op
  static boolean expr0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr0")) return false;
    boolean result_;
    result_ = expr_assert(builder_, level_ + 1);
    if (!result_) result_ = expr_if(builder_, level_ + 1);
    if (!result_) result_ = expr_let(builder_, level_ + 1);
    if (!result_) result_ = expr_with(builder_, level_ + 1);
    if (!result_) result_ = expr_lambda(builder_, level_ + 1);
    if (!result_) result_ = expr_op(builder_, level_ + 1, -1);
    return result_;
  }

  /* ********************************************************** */
  // expr_select ( !missing_semi expr_select ) *
  public static boolean expr_app(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_app")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPR_APP, "<expr app>");
    result_ = expr_select(builder_, level_ + 1);
    result_ = result_ && expr_app_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ( !missing_semi expr_select ) *
  private static boolean expr_app_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_app_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!expr_app_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "expr_app_1", pos_)) break;
    }
    return true;
  }

  // !missing_semi expr_select
  private static boolean expr_app_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_app_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expr_app_1_0_0(builder_, level_ + 1);
    result_ = result_ && expr_select(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !missing_semi
  private static boolean expr_app_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_app_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !missing_semi(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ASSERT expr SEMI expr
  public static boolean expr_assert(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_assert")) return false;
    if (!nextTokenIs(builder_, ASSERT)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPR_ASSERT, null);
    result_ = consumeToken(builder_, ASSERT);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expr(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, SEMI)) && result_;
    result_ = pinned_ && expr(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // IF expr then else
  public static boolean expr_if(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_if")) return false;
    if (!nextTokenIs(builder_, IF)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPR_IF, null);
    result_ = consumeToken(builder_, IF);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expr(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, then(builder_, level_ + 1)) && result_;
    result_ = pinned_ && else_$(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // lambda_params !missing_semi COLON expr
  public static boolean expr_lambda(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_lambda")) return false;
    if (!nextTokenIs(builder_, "<expr lambda>", ID, LCURLY)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPR_LAMBDA, "<expr lambda>");
    result_ = lambda_params(builder_, level_ + 1);
    result_ = result_ && expr_lambda_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    pinned_ = result_; // pin = 3
    result_ = result_ && expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // !missing_semi
  private static boolean expr_lambda_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_lambda_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !missing_semi(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LET !LCURLY recover_let (bind recover_let)* IN expr
  public static boolean expr_let(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_let")) return false;
    if (!nextTokenIs(builder_, LET)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPR_LET, null);
    result_ = consumeToken(builder_, LET);
    result_ = result_ && expr_let_1(builder_, level_ + 1);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, recover_let(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, expr_let_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, IN)) && result_;
    result_ = pinned_ && expr(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // !LCURLY
  private static boolean expr_let_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_let_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeToken(builder_, LCURLY);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (bind recover_let)*
  private static boolean expr_let_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_let_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!expr_let_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "expr_let_3", pos_)) break;
    }
    return true;
  }

  // bind recover_let
  private static boolean expr_let_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_let_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = bind(builder_, level_ + 1);
    result_ = result_ && recover_let(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // expr_simple [ !missing_semi ( select_attr | legacy_app_or )]
  public static boolean expr_select(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_select")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPR_SELECT, "<expr select>");
    result_ = expr_simple(builder_, level_ + 1);
    result_ = result_ && expr_select_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // [ !missing_semi ( select_attr | legacy_app_or )]
  private static boolean expr_select_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_select_1")) return false;
    expr_select_1_0(builder_, level_ + 1);
    return true;
  }

  // !missing_semi ( select_attr | legacy_app_or )
  private static boolean expr_select_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_select_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expr_select_1_0_0(builder_, level_ + 1);
    result_ = result_ && expr_select_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !missing_semi
  private static boolean expr_select_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_select_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !missing_semi(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // select_attr | legacy_app_or
  private static boolean expr_select_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_select_1_0_1")) return false;
    boolean result_;
    result_ = select_attr(builder_, level_ + 1);
    if (!result_) result_ = legacy_app_or(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // identifier
  //   | literal
  //   | string
  //   | parens
  //   | set
  //   | list
  //   | legacy_let
  public static boolean expr_simple(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_simple")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPR_SIMPLE, "<expr simple>");
    result_ = identifier(builder_, level_ + 1);
    if (!result_) result_ = literal(builder_, level_ + 1);
    if (!result_) result_ = string(builder_, level_ + 1);
    if (!result_) result_ = parens(builder_, level_ + 1);
    if (!result_) result_ = set(builder_, level_ + 1);
    if (!result_) result_ = list(builder_, level_ + 1);
    if (!result_) result_ = legacy_let(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // WITH expr SEMI expr
  public static boolean expr_with(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_with")) return false;
    if (!nextTokenIs(builder_, WITH)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, EXPR_WITH, null);
    result_ = consumeToken(builder_, WITH);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expr(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, SEMI)) && result_;
    result_ = pinned_ && expr(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // ID
  public static boolean identifier(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "identifier")) return false;
    if (!nextTokenIs(builder_, ID)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ID);
    exit_section_(builder_, marker_, IDENTIFIER, result_);
    return result_;
  }

  /* ********************************************************** */
  // IND_STRING_OPEN string_part* IND_STRING_CLOSE
  public static boolean ind_string(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ind_string")) return false;
    if (!nextTokenIs(builder_, IND_STRING_OPEN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, IND_STRING, null);
    result_ = consumeToken(builder_, IND_STRING_OPEN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, ind_string_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, IND_STRING_CLOSE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // string_part*
  private static boolean ind_string_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "ind_string_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!string_part(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "ind_string_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // ID [ !missing_semi AT param_set ] | param_set [ !missing_semi AT ID ]
  static boolean lambda_params(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lambda_params")) return false;
    if (!nextTokenIs(builder_, "", ID, LCURLY)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = lambda_params_0(builder_, level_ + 1);
    if (!result_) result_ = lambda_params_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ID [ !missing_semi AT param_set ]
  private static boolean lambda_params_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lambda_params_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ID);
    result_ = result_ && lambda_params_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ !missing_semi AT param_set ]
  private static boolean lambda_params_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lambda_params_0_1")) return false;
    lambda_params_0_1_0(builder_, level_ + 1);
    return true;
  }

  // !missing_semi AT param_set
  private static boolean lambda_params_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lambda_params_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = lambda_params_0_1_0_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, AT);
    result_ = result_ && param_set(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !missing_semi
  private static boolean lambda_params_0_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lambda_params_0_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !missing_semi(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // param_set [ !missing_semi AT ID ]
  private static boolean lambda_params_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lambda_params_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = param_set(builder_, level_ + 1);
    result_ = result_ && lambda_params_1_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ !missing_semi AT ID ]
  private static boolean lambda_params_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lambda_params_1_1")) return false;
    lambda_params_1_1_0(builder_, level_ + 1);
    return true;
  }

  // !missing_semi AT ID
  private static boolean lambda_params_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lambda_params_1_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = lambda_params_1_1_0_0(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 0, AT, ID);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !missing_semi
  private static boolean lambda_params_1_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "lambda_params_1_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !missing_semi(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // OR_KW
  public static boolean legacy_app_or(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "legacy_app_or")) return false;
    if (!nextTokenIs(builder_, OR_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _UPPER_, LEGACY_APP_OR, null);
    result_ = consumeToken(builder_, OR_KW);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LET LCURLY recover_set (bind recover_set)* RCURLY
  public static boolean legacy_let(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "legacy_let")) return false;
    if (!nextTokenIs(builder_, LET)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LEGACY_LET, null);
    result_ = consumeTokens(builder_, 2, LET, LCURLY);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, recover_set(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, legacy_let_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RCURLY) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (bind recover_set)*
  private static boolean legacy_let_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "legacy_let_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!legacy_let_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "legacy_let_3", pos_)) break;
    }
    return true;
  }

  // bind recover_set
  private static boolean legacy_let_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "legacy_let_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = bind(builder_, level_ + 1);
    result_ = result_ && recover_set(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // braces_recover !(ASSERT | SEMI | IF | THEN | ELSE | LET | IN | WITH) !bind
  static boolean let_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "let_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = braces_recover(builder_, level_ + 1);
    result_ = result_ && let_recover_1(builder_, level_ + 1);
    result_ = result_ && let_recover_2(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !(ASSERT | SEMI | IF | THEN | ELSE | LET | IN | WITH)
  private static boolean let_recover_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "let_recover_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !let_recover_1_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ASSERT | SEMI | IF | THEN | ELSE | LET | IN | WITH
  private static boolean let_recover_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "let_recover_1_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, ASSERT);
    if (!result_) result_ = consumeTokenFast(builder_, SEMI);
    if (!result_) result_ = consumeTokenFast(builder_, IF);
    if (!result_) result_ = consumeTokenFast(builder_, THEN);
    if (!result_) result_ = consumeTokenFast(builder_, ELSE);
    if (!result_) result_ = consumeTokenFast(builder_, LET);
    if (!result_) result_ = consumeTokenFast(builder_, IN);
    if (!result_) result_ = consumeTokenFast(builder_, WITH);
    return result_;
  }

  // !bind
  private static boolean let_recover_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "let_recover_2")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !bind(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LBRAC recover_list (expr_select recover_list)* RBRAC
  public static boolean list(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "list")) return false;
    if (!nextTokenIs(builder_, LBRAC)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LIST, null);
    result_ = consumeToken(builder_, LBRAC);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, recover_list(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, list_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RBRAC) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (expr_select recover_list)*
  private static boolean list_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "list_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!list_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "list_2", pos_)) break;
    }
    return true;
  }

  // expr_select recover_list
  private static boolean list_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "list_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expr_select(builder_, level_ + 1);
    result_ = result_ && recover_list(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // brac_recover !expr_select
  static boolean list_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "list_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = brac_recover(builder_, level_ + 1);
    result_ = result_ && list_recover_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !expr_select
  private static boolean list_recover_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "list_recover_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !expr_select(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // INT | FLOAT | URI | path
  public static boolean literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "literal")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, LITERAL, "<literal>");
    result_ = consumeToken(builder_, INT);
    if (!result_) result_ = consumeToken(builder_, FLOAT);
    if (!result_) result_ = consumeToken(builder_, URI);
    if (!result_) result_ = path(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // <<parseIsBindValue>> ( RCURLY | IN | bind )
  static boolean missing_semi(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "missing_semi")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = parseIsBindValue(builder_, level_ + 1);
    result_ = result_ && missing_semi_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // RCURLY | IN | bind
  private static boolean missing_semi_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "missing_semi_1")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, RCURLY);
    if (!result_) result_ = consumeTokenFast(builder_, IN);
    if (!result_) result_ = bind(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // expr
  static boolean nixFile(PsiBuilder builder_, int level_) {
    return expr(builder_, level_ + 1);
  }

  /* ********************************************************** */
  // ID [ param_has ]
  public static boolean param(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAM, "<param>");
    result_ = consumeToken(builder_, ID);
    result_ = result_ && param_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, NixParser::param_recover);
    return result_;
  }

  // [ param_has ]
  private static boolean param_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_1")) return false;
    param_has(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // HAS expr
  static boolean param_has(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_has")) return false;
    if (!nextTokenIs(builder_, HAS)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, HAS);
    pinned_ = result_; // pin = 1
    result_ = result_ && expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // curly_recover !COMMA
  static boolean param_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = curly_recover(builder_, level_ + 1);
    result_ = result_ && param_recover_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !COMMA
  private static boolean param_recover_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_recover_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !consumeTokenFast(builder_, COMMA);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // LCURLY ( param COMMA )* [ ( ELLIPSIS | param ) ] recover_param_set RCURLY
  public static boolean param_set(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_set")) return false;
    if (!nextTokenIs(builder_, LCURLY)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARAM_SET, null);
    result_ = consumeToken(builder_, LCURLY);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, param_set_1(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, param_set_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && report_error_(builder_, recover_param_set(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RCURLY) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // ( param COMMA )*
  private static boolean param_set_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_set_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!param_set_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "param_set_1", pos_)) break;
    }
    return true;
  }

  // param COMMA
  private static boolean param_set_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_set_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = param(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COMMA);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // [ ( ELLIPSIS | param ) ]
  private static boolean param_set_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_set_2")) return false;
    param_set_2_0(builder_, level_ + 1);
    return true;
  }

  // ELLIPSIS | param
  private static boolean param_set_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "param_set_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ELLIPSIS);
    if (!result_) result_ = param(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // !(RPAREN | RCURLY)
  static boolean paren_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "paren_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !paren_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // RPAREN | RCURLY
  private static boolean paren_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "paren_recover_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, RPAREN);
    if (!result_) result_ = consumeTokenFast(builder_, RCURLY);
    return result_;
  }

  /* ********************************************************** */
  // LPAREN expr recover_parens RPAREN
  public static boolean parens(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parens")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PARENS, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expr(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, recover_parens(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // SPATH | PATH_SEGMENT (PATH_SEGMENT | antiquotation)* PATH_END
  public static boolean path(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "path")) return false;
    if (!nextTokenIs(builder_, "<path>", PATH_SEGMENT, SPATH)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PATH, "<path>");
    result_ = consumeToken(builder_, SPATH);
    if (!result_) result_ = path_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // PATH_SEGMENT (PATH_SEGMENT | antiquotation)* PATH_END
  private static boolean path_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "path_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, PATH_SEGMENT);
    result_ = result_ && path_1_1(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, PATH_END);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (PATH_SEGMENT | antiquotation)*
  private static boolean path_1_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "path_1_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!path_1_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "path_1_1", pos_)) break;
    }
    return true;
  }

  // PATH_SEGMENT | antiquotation
  private static boolean path_1_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "path_1_1_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, PATH_SEGMENT);
    if (!result_) result_ = antiquotation(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  static boolean recover_antiquotation(PsiBuilder builder_, int level_) {
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    exit_section_(builder_, level_, marker_, true, false, NixParser::curly_recover);
    return true;
  }

  /* ********************************************************** */
  static boolean recover_let(PsiBuilder builder_, int level_) {
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    exit_section_(builder_, level_, marker_, true, false, NixParser::let_recover);
    return true;
  }

  /* ********************************************************** */
  static boolean recover_list(PsiBuilder builder_, int level_) {
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    exit_section_(builder_, level_, marker_, true, false, NixParser::list_recover);
    return true;
  }

  /* ********************************************************** */
  static boolean recover_param_set(PsiBuilder builder_, int level_) {
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    exit_section_(builder_, level_, marker_, true, false, NixParser::curly_recover);
    return true;
  }

  /* ********************************************************** */
  static boolean recover_parens(PsiBuilder builder_, int level_) {
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    exit_section_(builder_, level_, marker_, true, false, NixParser::paren_recover);
    return true;
  }

  /* ********************************************************** */
  static boolean recover_set(PsiBuilder builder_, int level_) {
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    exit_section_(builder_, level_, marker_, true, false, NixParser::set_recover);
    return true;
  }

  /* ********************************************************** */
  // DOT attr_path [ select_default ]
  static boolean select_attr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_attr")) return false;
    if (!nextTokenIs(builder_, DOT)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, DOT);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, attr_path(builder_, level_ + 1));
    result_ = pinned_ && select_attr_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [ select_default ]
  private static boolean select_attr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_attr_2")) return false;
    select_default(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // OR_KW expr_select
  static boolean select_default(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "select_default")) return false;
    if (!nextTokenIs(builder_, OR_KW)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, OR_KW);
    pinned_ = result_; // pin = 1
    result_ = result_ && expr_select(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // [ REC ] LCURLY recover_set (bind recover_set)* RCURLY
  public static boolean set(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set")) return false;
    if (!nextTokenIs(builder_, "<set>", LCURLY, REC)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, SET, "<set>");
    result_ = set_0(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, LCURLY);
    pinned_ = result_; // pin = 2
    result_ = result_ && report_error_(builder_, recover_set(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, set_3(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RCURLY) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [ REC ]
  private static boolean set_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_0")) return false;
    consumeToken(builder_, REC);
    return true;
  }

  // (bind recover_set)*
  private static boolean set_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_3")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!set_3_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "set_3", pos_)) break;
    }
    return true;
  }

  // bind recover_set
  private static boolean set_3_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_3_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = bind(builder_, level_ + 1);
    result_ = result_ && recover_set(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // curly_recover !bind
  static boolean set_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = curly_recover(builder_, level_ + 1);
    result_ = result_ && set_recover_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // !bind
  private static boolean set_recover_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "set_recover_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !bind(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // ID | OR_KW
  public static boolean std_attr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "std_attr")) return false;
    if (!nextTokenIs(builder_, "<std attr>", ID, OR_KW)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STD_ATTR, "<std attr>");
    result_ = consumeToken(builder_, ID);
    if (!result_) result_ = consumeToken(builder_, OR_KW);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // STRING_OPEN string_part* STRING_CLOSE
  public static boolean std_string(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "std_string")) return false;
    if (!nextTokenIs(builder_, STRING_OPEN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STD_STRING, null);
    result_ = consumeToken(builder_, STRING_OPEN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, std_string_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, STRING_CLOSE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // string_part*
  private static boolean std_string_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "std_string_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!string_part(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "std_string_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // std_string | ind_string
  public static boolean string(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string")) return false;
    if (!nextTokenIs(builder_, "<string>", IND_STRING_OPEN, STRING_OPEN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, STRING, "<string>");
    result_ = std_string(builder_, level_ + 1);
    if (!result_) result_ = ind_string(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // std_string | antiquotation
  public static boolean string_attr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_attr")) return false;
    if (!nextTokenIs(builder_, "<string attr>", DOLLAR, STRING_OPEN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRING_ATTR, "<string attr>");
    result_ = std_string(builder_, level_ + 1);
    if (!result_) result_ = antiquotation(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // string_text | antiquotation
  public static boolean string_part(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_part")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, STRING_PART, "<string part>");
    result_ = string_text(builder_, level_ + 1);
    if (!result_) result_ = antiquotation(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, NixParser::string_part_recover);
    return result_;
  }

  /* ********************************************************** */
  // !(DOLLAR | STRING_CLOSE | IND_STRING_CLOSE | string_token)
  static boolean string_part_recover(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_part_recover")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NOT_);
    result_ = !string_part_recover_0(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // DOLLAR | STRING_CLOSE | IND_STRING_CLOSE | string_token
  private static boolean string_part_recover_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_part_recover_0")) return false;
    boolean result_;
    result_ = consumeTokenFast(builder_, DOLLAR);
    if (!result_) result_ = consumeTokenFast(builder_, STRING_CLOSE);
    if (!result_) result_ = consumeTokenFast(builder_, IND_STRING_CLOSE);
    if (!result_) result_ = string_token(builder_, level_ + 1);
    return result_;
  }

  /* ********************************************************** */
  // string_token+
  public static boolean string_text(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_text")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRING_TEXT, "<string text>");
    result_ = string_token(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!string_token(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "string_text", pos_)) break;
    }
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // STR | IND_STR | STR_ESCAPE | IND_STR_ESCAPE
  static boolean string_token(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "string_token")) return false;
    boolean result_;
    result_ = consumeToken(builder_, STR);
    if (!result_) result_ = consumeToken(builder_, IND_STR);
    if (!result_) result_ = consumeToken(builder_, STR_ESCAPE);
    if (!result_) result_ = consumeToken(builder_, IND_STR_ESCAPE);
    return result_;
  }

  /* ********************************************************** */
  // THEN expr
  static boolean then(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "then")) return false;
    if (!nextTokenIs(builder_, THEN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_);
    result_ = consumeToken(builder_, THEN);
    pinned_ = result_; // pin = 1
    result_ = result_ && expr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  /* ********************************************************** */
  // Expression root: expr_op
  // Operator priority table:
  // 0: BINARY(expr_op_implication)
  // 1: BINARY(expr_op_or)
  // 2: BINARY(expr_op_and)
  // 3: BINARY(expr_op_eq) BINARY(expr_op_ne)
  // 4: BINARY(expr_op_lt) BINARY(expr_op_le) BINARY(expr_op_gt) BINARY(expr_op_ge)
  // 5: BINARY(expr_op_update)
  // 6: PREFIX(expr_op_not)
  // 7: BINARY(expr_op_plus) BINARY(expr_op_minus)
  // 8: BINARY(expr_op_mul) BINARY(expr_op_div)
  // 9: BINARY(expr_op_concat)
  // 10: POSTFIX(expr_op_has)
  // 11: PREFIX(expr_op_neg)
  // 12: ATOM(expr_op_base)
  public static boolean expr_op(PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "expr_op")) return false;
    addVariant(builder_, "<expr op>");
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, "<expr op>");
    result_ = expr_op_not(builder_, level_ + 1);
    if (!result_) result_ = expr_op_neg(builder_, level_ + 1);
    if (!result_) result_ = expr_op_base(builder_, level_ + 1);
    pinned_ = result_;
    result_ = result_ && expr_op_0(builder_, level_ + 1, priority_);
    exit_section_(builder_, level_, marker_, null, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean expr_op_0(PsiBuilder builder_, int level_, int priority_) {
    if (!recursion_guard_(builder_, level_, "expr_op_0")) return false;
    boolean result_ = true;
    while (true) {
      Marker marker_ = enter_section_(builder_, level_, _LEFT_, null);
      if (priority_ < 0 && consumeTokenSmart(builder_, IMPL)) {
        result_ = expr_op(builder_, level_, 0);
        exit_section_(builder_, level_, marker_, EXPR_OP_IMPLICATION, result_, true, null);
      }
      else if (priority_ < 1 && consumeTokenSmart(builder_, OR)) {
        result_ = expr_op(builder_, level_, 1);
        exit_section_(builder_, level_, marker_, EXPR_OP_OR, result_, true, null);
      }
      else if (priority_ < 2 && consumeTokenSmart(builder_, AND)) {
        result_ = expr_op(builder_, level_, 2);
        exit_section_(builder_, level_, marker_, EXPR_OP_AND, result_, true, null);
      }
      else if (priority_ < 3 && consumeTokenSmart(builder_, EQ)) {
        result_ = expr_op(builder_, level_, 3);
        exit_section_(builder_, level_, marker_, EXPR_OP_EQ, result_, true, null);
      }
      else if (priority_ < 3 && consumeTokenSmart(builder_, NEQ)) {
        result_ = expr_op(builder_, level_, 3);
        exit_section_(builder_, level_, marker_, EXPR_OP_NE, result_, true, null);
      }
      else if (priority_ < 4 && consumeTokenSmart(builder_, LT)) {
        result_ = expr_op(builder_, level_, 4);
        exit_section_(builder_, level_, marker_, EXPR_OP_LT, result_, true, null);
      }
      else if (priority_ < 4 && consumeTokenSmart(builder_, LEQ)) {
        result_ = expr_op(builder_, level_, 4);
        exit_section_(builder_, level_, marker_, EXPR_OP_LE, result_, true, null);
      }
      else if (priority_ < 4 && consumeTokenSmart(builder_, GT)) {
        result_ = expr_op(builder_, level_, 4);
        exit_section_(builder_, level_, marker_, EXPR_OP_GT, result_, true, null);
      }
      else if (priority_ < 4 && consumeTokenSmart(builder_, GEQ)) {
        result_ = expr_op(builder_, level_, 4);
        exit_section_(builder_, level_, marker_, EXPR_OP_GE, result_, true, null);
      }
      else if (priority_ < 5 && consumeTokenSmart(builder_, UPDATE)) {
        result_ = expr_op(builder_, level_, 4);
        exit_section_(builder_, level_, marker_, EXPR_OP_UPDATE, result_, true, null);
      }
      else if (priority_ < 7 && consumeTokenSmart(builder_, PLUS)) {
        result_ = expr_op(builder_, level_, 7);
        exit_section_(builder_, level_, marker_, EXPR_OP_PLUS, result_, true, null);
      }
      else if (priority_ < 7 && consumeTokenSmart(builder_, MINUS)) {
        result_ = expr_op(builder_, level_, 7);
        exit_section_(builder_, level_, marker_, EXPR_OP_MINUS, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, TIMES)) {
        result_ = expr_op(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, EXPR_OP_MUL, result_, true, null);
      }
      else if (priority_ < 8 && consumeTokenSmart(builder_, DIVIDE)) {
        result_ = expr_op(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, EXPR_OP_DIV, result_, true, null);
      }
      else if (priority_ < 9 && consumeTokenSmart(builder_, CONCAT)) {
        result_ = expr_op(builder_, level_, 8);
        exit_section_(builder_, level_, marker_, EXPR_OP_CONCAT, result_, true, null);
      }
      else if (priority_ < 10 && expr_op_has_0(builder_, level_ + 1)) {
        result_ = true;
        exit_section_(builder_, level_, marker_, EXPR_OP_HAS, result_, true, null);
      }
      else {
        exit_section_(builder_, level_, marker_, null, false, false, null);
        break;
      }
    }
    return result_;
  }

  public static boolean expr_op_not(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_op_not")) return false;
    if (!nextTokenIsSmart(builder_, NOT)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, NOT);
    pinned_ = result_;
    result_ = pinned_ && expr_op(builder_, level_, 6);
    exit_section_(builder_, level_, marker_, EXPR_OP_NOT, result_, pinned_, null);
    return result_ || pinned_;
  }

  public static boolean expr_op_neg(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_op_neg")) return false;
    if (!nextTokenIsSmart(builder_, MINUS)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, null);
    result_ = consumeTokenSmart(builder_, MINUS);
    pinned_ = result_;
    result_ = pinned_ && expr_op(builder_, level_, 11);
    exit_section_(builder_, level_, marker_, EXPR_OP_NEG, result_, pinned_, null);
    return result_ || pinned_;
  }

  // HAS attr_path
  private static boolean expr_op_has_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_op_has_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeTokenSmart(builder_, HAS);
    result_ = result_ && attr_path(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // expr_app
  public static boolean expr_op_base(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expr_op_base")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPR_OP_BASE, "<expr op base>");
    result_ = expr_app(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

}
