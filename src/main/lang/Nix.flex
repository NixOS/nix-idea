package org.nixos.idea.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.nixos.idea.psi.NixTypes.*;

%%

%{
  private final Deque<Integer> state = new ArrayDeque<>();

  private void pushState(int sst) {
    state.push(sst);
    yybegin(sst);
  }

  private int popState() {
    int sst;
    if (state.isEmpty()) {
      sst = YYINITIAL;
    } else {
      sst = state.pop();
    }
    yybegin(state.isEmpty() ? YYINITIAL : state.peek());
    return sst;
  }
%}

%public
%class NixLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%xstate STRING IND_STRING

ANY=[^]
ID=[a-zA-Z_][a-zA-Z0-9_'-]*
INT=[0-9]+
FLOAT=(([1-9][0-9]*\.[0-9]*)|(0?\.[0-9]+))([Ee][+-]?[0-9]+)?
PATH=[a-zA-Z0-9._+-]*(\/[a-zA-Z0-9._+-]+)+\/?
HPATH=\~(\/[a-zA-Z0-9._+-]+)+\/?
SPATH=\<[a-zA-Z0-9._+-]+(\/[a-zA-Z0-9._+-]+)*\>
URI=[a-zA-Z][a-zA-Z0-9.+-]*\:[a-zA-Z0-9%/?:@&=+$,\-_.!~*']+

WHITE_SPACE=[\ \t\r\n]+
SCOMMENT=#[^\r\n]*
MCOMMENT=\/\*([^*]|\*[^\/])*\*\/

%%

<YYINITIAL> {
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
  "{"                   { pushState(YYINITIAL); return LCURLY; }
  "}"                   { popState(); return RCURLY; }
  "["                   { return LBRAC; }
  "]"                   { return RBRAC; }
  "${"                  { pushState(YYINITIAL); return DOLLAR_CURLY; }

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
  \'\'                  { pushState(IND_STRING); return IND_STRING_OPEN; }

  // Note that `true`, `false` and `null` are built-in variables but not
  // keywords. Therefore, they are not listed here.
  {ID}                  { return ID; }
  {INT}                 { return INT; }
  {FLOAT}               { return FLOAT; }
  {PATH}                { return PATH; }
  {HPATH}               { return HPATH; }
  {SPATH}               { return SPATH; }
  {URI}                 { return URI; }

  {SCOMMENT}            { return SCOMMENT; }
  {MCOMMENT}            { return MCOMMENT; }
  {WHITE_SPACE}         { return com.intellij.psi.TokenType.WHITE_SPACE; }
  [^]                   { return com.intellij.psi.TokenType.BAD_CHARACTER; }
}

<STRING> {
  ([^\$\"\\]|\$[^\{\"\\]|\\{ANY}|\$\\{ANY})*\$/\" { return STR; }
  ([^\$\"\\]|\$[^\{\"\\]|\\{ANY}|\$\\{ANY})+ |
  \$|\\|\$\\            { return STR; }
  "${"                  { pushState(YYINITIAL); return DOLLAR_CURLY; }
  \"                    { popState(); return STRING_CLOSE; }
}

<IND_STRING> {
  ([^\$\']|\$[^\{\']|\'[^\'\$])+ |
  "''$" |
  \$ |
  "'''" |
  "''"\\{ANY}           { return IND_STR; }
  "${"                  { pushState(YYINITIAL); return DOLLAR_CURLY; }
  "''"                  { popState(); return IND_STRING_CLOSE; }
  "'"                   { return IND_STR; }
}
