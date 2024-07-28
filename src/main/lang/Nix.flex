package org.nixos.idea.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import static org.nixos.idea.psi.NixTypes.*;

%%

%{
  private final AbstractIntList states = new IntArrayList();

  private void pushState(int newState) {
      assert newState != YYINITIAL : "Pusing YYINITIAL is not supported";
      // store current state on the stack to allow restoring it in popState(...)
      states.push(yystate());
      yybegin(newState);
  }

  private void popState(int expectedState) {
    assert !states.isEmpty() : "Popping an empty stack of states. Expected: " + expectedState;
    // safe-guard, because we always know which state we're currently in in the rules below
    assert yystate() == expectedState : String.format("Unexpected state. Current: %d, expected: %d", yystate(), expectedState);
    // start the lexer with the previous state, which was stored by pushState(...)
    yybegin(states.popInt());
  }

  private void replaceState(int expectedState, int newState) {
      assert newState != YYINITIAL : "Pusing YYINITIAL is not supported";
      // safe-guard, because we always know which state we're currently in in the rules below
      assert yystate() == expectedState : String.format("Unexpected state. Current: %d, expected: %d", yystate(), expectedState);
      yybegin(newState);
  }

  protected void onReset() {
      states.clear();
  }
%}

%abstract
%class _NixLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%state BLOCK STRING IND_STRING ANTIQUOTATION_START ANTIQUOTATION
%xstate IND_STRING_START IND_STRING_INDENT PATH
%suppress empty-match

ANY=[^]
ID=[a-zA-Z_][a-zA-Z0-9_'-]*
INT=[0-9]+
FLOAT=(([1-9][0-9]*\.[0-9]*)|(0?\.[0-9]+))([Ee][+-]?[0-9]+)?
PATH_CHAR=[a-zA-Z0-9\.\_\-\+]
PATH={PATH_CHAR}*(\/{PATH_CHAR}+)+\/?
PATH_SEG={PATH_CHAR}*\/
HPATH_START=\~\/
SPATH=\<{PATH_CHAR}+(\/{PATH_CHAR}+)*\>
URI=[a-zA-Z][a-zA-Z0-9.+-]*\:[a-zA-Z0-9%/?:@&=+$,\-_.!~*']+

WHITE_SPACE=[\ \t\r\n]+
SCOMMENT=#[^\r\n]*
MCOMMENT=\/\*([^*]|\*[^\/])*\*\/

%%

<STRING> {
  [^\$\"\\]+            { return STR; }
  "$"|"$$"|\\           { return STR; }
  \\{ANY}               { return STR_ESCAPE; }
  "$"/"{"               { pushState(ANTIQUOTATION_START); return DOLLAR; }
  \"                    { popState(STRING); return STRING_CLOSE; }
}

<IND_STRING_START> {
  // The first line is ignored in case it is empty
  [\ ]*\n               { replaceState(IND_STRING_START, IND_STRING_INDENT); return com.intellij.psi.TokenType.WHITE_SPACE; }
}

<IND_STRING_START, IND_STRING_INDENT> {
  [\ ]+                 { replaceState(yystate(), IND_STRING); return IND_STR_INDENT; }
  ""                    { replaceState(yystate(), IND_STRING); }
}

<IND_STRING> {
  \n                    { replaceState(IND_STRING, IND_STRING_INDENT); return IND_STR_LF; }
  [^\$\'\n]+            { return IND_STR; }
  "$"|"$$"|"'"          { return IND_STR; }
  "''$"|"'''"           { return IND_STR_ESCAPE; }
  "''"\\{ANY}           { return IND_STR_ESCAPE; }
  "$"/"{"               { pushState(ANTIQUOTATION_START); return DOLLAR; }
  "''"                  { popState(IND_STRING); return IND_STRING_CLOSE; }
}

<ANTIQUOTATION_START> {
  // '$' and '{' must be two separate tokens to make NixBraceMatcher work
  // correctly with Grammar-Kit.
  "{"                   { replaceState(ANTIQUOTATION_START, ANTIQUOTATION); return LCURLY; }
}

<ANTIQUOTATION> {
  "}"                   { popState(ANTIQUOTATION); return RCURLY; }
}

<BLOCK> {
  "}"                   { popState(BLOCK); return RCURLY; }
}

<PATH> {
  "$"/"{"               { pushState(ANTIQUOTATION_START); return DOLLAR; }
  {PATH_SEG}            { return PATH_SEGMENT; }
  {PATH_CHAR}+          { return PATH_SEGMENT; }
  // anything else, e.g. a whitespace, stops lexing of a PATH
  // we're delegating back to the parent state
  // PATH_END is an empty-length token to signal the end of the path
  ""                    { popState(PATH); return PATH_END; }
}

<YYINITIAL, BLOCK, ANTIQUOTATION> {
  "if"                  { return IF; }
  "then"                { return THEN; }
  "else"                { return ELSE; }
  "assert"              { return ASSERT; }
  "with"                { return WITH; }
  "let"                 { return LET; }
  "in"                  { return IN; }
  "rec"                 { return REC; }
  "inherit"             { return INHERIT; }
  "or"                  { return OR_KW; }
  "..."                 { return ELLIPSIS; }

  "="                   { return ASSIGN; }
  ":"                   { return COLON; }
  ";"                   { return SEMI; }
  ","                   { return COMMA; }
  "@"                   { return AT; }
  "("                   { return LPAREN; }
  ")"                   { return RPAREN; }
  "{"                   { pushState(BLOCK); return LCURLY; }
  "}"                   { return RCURLY; }
  "["                   { return LBRAC; }
  "]"                   { return RBRAC; }
  // '$' and '{' must be two separate tokens to make NixBraceMatcher work
  // correctly with Grammar-Kit.
  "$"/"{"               { return DOLLAR; }

  "."                   { return DOT; }
  "?"                   { return HAS; }
  "!"                   { return NOT; }
  "*"                   { return TIMES; }
  "/"                   { return DIVIDE; }
  "+"                   { return PLUS; }
  "-"                   { return MINUS; }
  "<"                   { return LT; }
  ">"                   { return GT; }
  "++"                  { return CONCAT; }
  "//"                  { return UPDATE; }
  "<="                  { return LEQ; }
  ">="                  { return GEQ; }
  "=="                  { return EQ; }
  "!="                  { return NEQ; }
  "&&"                  { return AND; }
  "||"                  { return OR; }
  "->"                  { return IMPL; }

  \"                    { pushState(STRING); return STRING_OPEN; }
  \'\'                  { pushState(IND_STRING_START); return IND_STRING_OPEN; }

  // Note that `true`, `false` and `null` are built-in variables but not
  // keywords. Therefore, they are not listed here.
  {ID}                  { return ID; }
  {INT}                 { return INT; }
  {FLOAT}               { return FLOAT; }
  "/" / "${"            { pushState(PATH); return PATH_SEGMENT; }
  {PATH}
  | {PATH_SEG}
  | {HPATH_START}       { pushState(PATH); return PATH_SEGMENT; }
  {SPATH}               { return SPATH; }
  {URI}                 { return URI; }

  {SCOMMENT}            { return SCOMMENT; }
  {MCOMMENT}            { return MCOMMENT; }
  {WHITE_SPACE}         { return com.intellij.psi.TokenType.WHITE_SPACE; }
}

// matched by inclusive states (%state), but not by exclusive states (%xstate)
[^]                     { return com.intellij.psi.TokenType.BAD_CHARACTER; }
