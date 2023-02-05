package org.nixos.idea.lang;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import static org.nixos.idea.psi.NixTypes.*;

%%

%{
  private final LongList states = new LongArrayList();
  private final Long2IntMap stateIndexMap = new Long2IntOpenHashMap();

  {
    states.add(YYINITIAL);
    stateIndexMap.put(YYINITIAL, 0);
  }

  private int currentStateIndex = 0;
  private int parentStateIndex = 0;

  public int getStateIndex() {
    return currentStateIndex;
  }

  public void restoreState(int stateIndex) {
    long state = states.getLong(stateIndex);
    currentStateIndex = stateIndex;
    parentStateIndex = (int) (state >> 32);
    yybegin((int) state);
  }

  private void pushState(int yystate) {
    long state = ((long) currentStateIndex << 32) | ((long) yystate & 0x0FFFFFFFFL);
    int stateIndex = stateIndexMap.get(state); // Returns 0 if not found
    if (stateIndex == 0 && state != YYINITIAL) {
      stateIndex = states.size();
      states.add(state);
      stateIndexMap.put(state, stateIndex);
    }
    parentStateIndex = currentStateIndex;
    currentStateIndex = stateIndex;
    yybegin(yystate);
  }

  private void popState() {
    restoreState(parentStateIndex);
  }
%}

%class _NixLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%xstate STRING IND_STRING ANTIQUOTATION_START

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
  [^\$\"\\]+            { return STR; }
  "$"|"$$"|\\           { return STR; }
  \\{ANY}               { return STR_ESCAPE; }
  "$"/"{"               { pushState(ANTIQUOTATION_START); return DOLLAR; }
  \"                    { popState(); return STRING_CLOSE; }
}

<IND_STRING> {
  [^\$\']+              { return IND_STR; }
  "$"|"$$"|"'"          { return IND_STR; }
  "''$"|"'''"           { return IND_STR_ESCAPE; }
  "''"\\{ANY}           { return IND_STR_ESCAPE; }
  "$"/"{"               { pushState(ANTIQUOTATION_START); return DOLLAR; }
  "''"                  { popState(); return IND_STRING_CLOSE; }
}

<ANTIQUOTATION_START> {
  // '$' and '{' must be two separate tokens to make NixBraceMatcher work
  // correctly with Grammar-Kit.
  "{"                   { popState(); pushState(YYINITIAL); return LCURLY; }
}
