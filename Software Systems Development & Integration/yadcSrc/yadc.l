/***************************************
Name: Jireh Agda
Student ID: 0795472
Date Last Modified: 2015 03 02
Lex Specification File for yadc Compiler
***************************************/

%{
    #include <stdio.h>
    #include <stdlib.h>
    #include <string.h>
    #include "y.tab.h"

    int fileno(FILE *stream);
    char *strdup(const char *s);

    void yyerror (char * errorString);

    int lineNo = 1;
%}

/*Some options to turn off making functions that will not be used*/
%option nounput
%option noinput

/*Comments are given its own exclusive state to help deal with potential variation*/
%x COMMENT

parameters      [0-9a-zA-Z_]+
emptyValue      \"[ \t\n]*\"
value           \"[^"\n]+\"

%%

                /*whitespace is ignored while newlines are used to increment lineNo*/
[ \t]+          { /*ignore whitespace*/ }
\n              { lineNo ++; }

                /*the comments are dealt with in its own FSA, with an end of line (EOF) or newline indicating the end*/
#               { BEGIN COMMENT; }
<COMMENT>.      { /*ignore inner characters*/ }
                /*reset state to state 0 and increment lineNo*/
<COMMENT>\n     { BEGIN 0; lineNo ++; }

                /*return the required parameters as special tokens*/
"title"         { return (TITLE); }
"fields"        { return (FIELDS); }
"buttons"       { return (BUTTONS); }

                /*special tokens which carry meaning in the GUI are defined here, where its value is put into yylval*/
                /*emptyString is listed first to prioritize its matching over a regular string, which can match the same thing*/                
{parameters}    { yylval.string = strdup (yytext); return (PARAM); }
{emptyValue}    { yyerror ("Error - Blank Value"); return (BLANK); }
{value}         {
                    /*the string inside of the quotes (not inclideing quotes) is duplicated and set to yylval.string*/
                    yytext[yyleng - 1] = '\0';
                    yylval.string = strdup (yytext + (sizeof (char) * 1));
                    return (VALUE);
                }

                /*return specific character tokens required by grammar in yadc.y file*/
=               { return (EQUALS); }
"{"             { return (LBRACE); }
,               { return (COMMA); }
"}"             { return (RBRACE); }
;               { return (SCOLON); }

                /*anything that does not follow the above rules is an error*/
.               { yyerror ("Error - Invalid Token"); return (ERROR); }

%%

/****
Preconditions: An error defined by the yadc specifications files or syntax error occurs
Postconditions: An error is outputted to stderr
****/
void yyerror (char * errorString)
{
    fprintf (stderr, "Line #%d - %s\n", lineNo, errorString); 
    return;
}
