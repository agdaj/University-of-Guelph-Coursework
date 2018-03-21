/******************************
ScannerCM.flex -> LexerCM.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 01
     Last Modified: 2017 03 05
C-Minus Flex Specification File
Compiler Project - CIS*4650
******************************/

import java_cup.runtime.*;

%%

%class LexerCM    /* Name output class to LexerCM.flex */
%line             /* Enable line and column tracking */
%column
%cup              /* Enable CUP compatibility */

/* Code Declarations */

%{
    /* Create a new java_cup.runtime.Symbol with information about
       the current token that possess no inherent value */
    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }

    /* Create a new java_cup.runtime.Symbol with information about
       the current token with its inherent value */
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

/* Macro Declarations */

/* Define a macro state for comments within a .cm file (YYINITIAL = non-comment) */
%state COMMENT

/* A line ends with a \r (carriage return), \n (line feed), or \r\n.   
   Whitespace is recognized as a lineEnd sequence, space, tab, or form feed.   
   These will essentially be ignored in the context of this scanner. */
lineEnd = \r|\n|\r\n
whiteSpace = {lineEnd} | [ \t\f]

/* identifier = a series of leters
   number = a series of digits */
letter = [a-zA-Z]
identifier = {letter}+

digit = [0-9]+
number = {digit}+

%%

/* Lexical Rules Section */
/* These are the pattern-action combinations that return relevant C- tokens to a parser */
/* Whitespace and comments in C- are filtered out here, and no nesting is allowed */

/* Reserved Keywords for C- */
<YYINITIAL>"int"              { return symbol(sym.INT); }
<YYINITIAL>"void"             { return symbol(sym.VOID); }
<YYINITIAL>"return"           { return symbol(sym.RETURN); }
<YYINITIAL>"if"               { return symbol(sym.IF); }
<YYINITIAL>"else"             { return symbol(sym.ELSE); }
<YYINITIAL>"while"            { return symbol(sym.WHILE); }

/* Basic Binary Operations of C- */
<YYINITIAL>"+"                { return symbol(sym.PLUS); }
<YYINITIAL>"-"                { return symbol(sym.MINUS); }
<YYINITIAL>"*"                { return symbol(sym.MUL); }
<YYINITIAL>"/"                { return symbol(sym.DIV); }
<YYINITIAL>"<"                { return symbol(sym.LT); }
<YYINITIAL>"<="               { return symbol(sym.LE); }
<YYINITIAL>">"                { return symbol(sym.GT); }
<YYINITIAL>">="               { return symbol(sym.GE); }
<YYINITIAL>"=="               { return symbol(sym.EQ); }
<YYINITIAL>"!="               { return symbol(sym.NE); }
<YYINITIAL>"="                { return symbol(sym.ASSIGN); }

/* Other Special Symbols of C- */
<YYINITIAL>";"                { return symbol(sym.SEMI); }
<YYINITIAL>","                { return symbol(sym.COMMA); }
<YYINITIAL>"("                { return symbol(sym.LPAREN); }
<YYINITIAL>")"                { return symbol(sym.RPAREN); }
<YYINITIAL>"["                { return symbol(sym.LBRACK); }
<YYINITIAL>"]"                { return symbol(sym.RBRACK); }
<YYINITIAL>"{"                { return symbol(sym.LBRACE); }
<YYINITIAL>"}"                { return symbol(sym.RBRACE); }

/* Other Tokens in C- (that can vary in value) */
<YYINITIAL>{identifier}       { return symbol(sym.ID, yytext()); }
<YYINITIAL>{number}           { return symbol(sym.NUM, yytext()); }

/* Irrelevant Tokens to C-, and Comment Handling */
{whiteSpace}*                 { /* Skip Whitespace */ }
"/*"                          { yybegin(COMMENT); }    /* Transition to 'comment' handling */

/* To reflect no comment-nesting, the first end-comment token found ends the COMMENT state */
<COMMENT>"*/"                 { yybegin(YYINITIAL); }    /* Transition back to parsing */
<COMMENT>.                    { /* Content here is filtered out and ignored */ }

/* Catch-all Error Token for Scanner */
.                             { return symbol(sym.ERROR); }
