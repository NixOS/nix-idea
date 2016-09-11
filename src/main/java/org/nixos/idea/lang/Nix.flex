package org.nixos.idea.lang;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.Stack;
import static org.nixos.idea.psi.NixTypes.*;

%%

%{
  public NixLexer() {
    this((java.io.Reader)null);
  }

  enum StrState { IN_STRING, IN_IND_STRING };

  private Stack<StrState> interpol;
  public CharSequence yylval_id, yylval_path, yylval_uri, yylval_expr;
  public void backToString() { yybegin(STRING); }
  public void backToIndString() { yybegin(IND_STRING); }

  public synchronized void assureInterpol() {
    if(interpol == null)
        interpol = new Stack<StrState>();
  }
  public synchronized void showState() {
    try {
      p(peek().toString());
    } catch (Exception e) {p("OUTSIDE");}
  }
  public synchronized void push(StrState sst) {
    assureInterpol();
    interpol.push(sst);
  }
  public synchronized StrState pop() throws Exception {
    assureInterpol();
    return interpol.pop();
  }
  public synchronized StrState peek() throws Exception {
    assureInterpol();
    return interpol.peek();
  };
  public void ps(String msg) {p(msg);showState();}
  public static void p(String msg) { System.out.println(msg);}

%}

%public
%class NixLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%state STRING IND_STRING

EOL="\r"|"\n"|"\r\n"
LINE_WS=[\ \t\f]
WHITE_SPACE=({LINE_WS}|{EOL})+

SCOMMENT=#[^\r\n]*
MCOMMENT=\/\*([^*]|\*[^\/])*\*\/
INT=[0-9]+
BOOL=(true|false)
ID=[_a-zA-Z][_a-zA-Z_0-9'-]*

STRINLINENIX=\$\{
STR_CT=([^\$\"]|\$[^\{\"])+
IND_STR_CT=([^\$\']|\$[^\{\']|\'[^\'\$])+

PATH=[a-zA-Z0-9\.\_\-\+]*(\/[a-zA-Z0-9\.\_\-\+]+)+
SPATH=\<[a-zA-Z0-9\.\_\-\+]+(\/[a-zA-Z0-9\.\_\-\+]+)*\>
HPATH=\~(\/[a-zA-Z0-9\.\_\-\+]+)+
URI=[a-zA-Z][a-zA-Z0-9\+\-\.]*\:[a-zA-Z0-9\%\/\?\:\@\&\=\+\$\,\-\_\.\!\~\*']+

%%
<YYINITIAL> {
  {WHITE_SPACE}      { return com.intellij.psi.TokenType.WHITE_SPACE; }

  "="                { return ASSIGN; }
  "("                { return LPAREN; }
  ")"                { return RPAREN; }
  "{"                { return LCURLY; }
  "}"                {
    try {
      StrState st = pop();
      if(st == StrState.IN_STRING) {
        backToString();
      } else if (st == StrState.IN_IND_STRING) {
        backToIndString();
      }
    } catch (Exception e){}
    return RCURLY;
                     }
  "["                { return LBRAC; }
  "]"                { return RBRAC; }
  "${"               { return DOLLAR_CURLY; }
  "$"                { return DOLLAR; }
  "?"                { return IS; }
  "@"                { return NAMED; }
  ":"                { return COLON; }
  ";"                { return SEMI; }
  "&&"               { return AND; }
  "||"               { return OR; }
  "!"                { return NOT; }
  "=="               { return EQ; }
  "!="               { return NEQ; }
  "<="               { return LEQ; }
  ">="               { return GEQ; }
  "<"                { return LT; }
  ">"                { return GT; }
  "+"                { return PLUS; }
  "-"                { return MINUS; }
  "/"                { return DIVIDE; }
  "*"                { return TIMES; }
  "++"               { return CONCAT; }
  "."                { return DOT; }
  ","                { return COMMA; }
  "\""               { yybegin(STRING);return FNUTT; }
  "->"               { return IMPL; }
  "//"               { return UPDATE; }
  "assert"           { return ASSERT; }
  "if"               { return IF; }
  "else"             { return ELSE; }
  "then"             { return THEN; }
  "with"             { return WITH; }
  "let"              { return LET; }
  "in"               { return IN; }
  "rec"              { return REC; }
  "or"               { return OR_KW; }
  "..."              { return ELLIPSIS; }
  "inherit"          { return INHERIT; }
  "require"          { return REQUIRE; }
  "requires"         { return REQUIRES; }
  "import"           { return IMPORT; }
  "imports"          { return IMPORTS; }

  \'\'(\ *\n)?
    {
        yybegin(IND_STRING);
        return IND_STRING_OPEN;
    }

  {SCOMMENT}         { return SCOMMENT; }
  {MCOMMENT}         { return MCOMMENT; }
  {INT}              { return INT; }
  {BOOL}             { return BOOL; }
  {ID}               { yylval_id=yytext();return ID; }
  {PATH}             { yylval_path=yytext();return PATH; }
  {SPATH}            { yylval_path=yytext();return SPATH; }
  {HPATH}            { yylval_path=yytext();return HPATH; }
  {URI}              { yylval_uri=yytext();return URI; }

  [^] { return com.intellij.psi.TokenType.BAD_CHARACTER; }

  .
    {
        zzBufferL=yytext();
    }
}

<STRING> {
  {STR_CT}
    {
        yylval_expr=yytext();
        return STR;
    }
  {STRINLINENIX}
    {
        yybegin(YYINITIAL);
        push(StrState.IN_STRING);
        return DOLLAR_CURLY;
    }
  "\""
    {
        yybegin(YYINITIAL);
        return FNUTT;
    }
  .
    {
        zzBufferL=yytext();
    }
}

<IND_STRING> {
  {IND_STR_CT}
    {
        yylval_expr = yytext();
        return IND_STR;
    }

  "''$"
    {
        yylval_expr = "$";
        return IND_STR;

    }

  "'''"
    {
        yylval_expr = "''";
        return IND_STR;

    }

  "''\."
    {
        yylval_expr = yytext();
        return IND_STR;
    }

  {STRINLINENIX}
    {
        yybegin(YYINITIAL);
        push(StrState.IN_IND_STRING);
        return DOLLAR_CURLY;
    }

  "''"
    {
        yybegin(YYINITIAL);
        return IND_STRING_CLOSE;
    }

  "'"
    {
        yylval_expr = "'";
        return IND_STR;
    }

  .
    {
        zzBufferL=yytext();
    }
}
