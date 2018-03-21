/*****************************
SGML.flex -> LexerSGML.java
Name: Jireh Agda (0795472)
Date Created: 2017 01 23
     Last Modified: 2017 02 02
SGML Flex Specification File
Warm-Up Assignment - CIS*4650
*****************************/

import java.util.ArrayList;

%%

%class LexerSGML
%type TokenSGML
%line
%column

/* Initialize global stack that will track nesting tags, when to transition on relevancy */
%{
    private static ArrayList<String> tagStack = new ArrayList<String> ();
    private int lastRelevantTag = 0;
%};

/* Assess and report any unmatched SGML tags, then return null on end of file */
%eofval{
    if (tagStack.size () > 0) {
        for (int i = 0; i < tagStack.size (); i++)
            System.err.println ("Unmatched SGML tag <" + tagStack.get (i) + ">");
    }

    return null;
%eofval};

/* Define a macro state for irrelevant sections of text (YYINITIAL = relevant) */
%state IRRELEVANT

/* A line ends with a \r (carriage return), \n (line feed), or \r\n.   
   Whitespace is recognized as a lineEnd sequence, space, tab, or form feed.   
   These will essentially be ignored in the context of this scanner. */
lineEnd = \r|\n|\r\n
whiteSpace = {lineEnd} | [ \t\f]

/* Tags are defined to at the very least have <> and </> for opening and closing respectively.
   Any sequence of characters (except lineEnds) can be between and will be recognized.
   -> Therefore tags must be complete within one line, and stops at the first > found.
   The only restricting feature of openTag is that it will not be recognized if the tag (inside <>) starts
   with /, this will be enforced by rule-order priority (closeTag will be recognized first in the rules). */
closeTag = <\/[^\r\n>]*>
openTag = <[^\r\n>]*>

/* word = any alphanumeric sequence with minimum one alphabetic character
   number = any number format, also encapsulates decimal number format and sign possibilities
   apostrophized = series of alphanumerics separated by ', or a hyphenated token with apostrophe suffix
   hyphenated = a series of alphanumerics separated by -
   punctuation = any non-whitespace, non-alphanumeric character */
word = [a-zA-Z0-9]*[a-zA-Z][a-zA-Z0-9]*
number = [+-]?[0-9]+ | ([+-]?[0-9]+"."[0-9]*) | ([+-]?[0-9]*"."[0-9]+)
apostrophized = ([a-zA-Z0-9]+['])+[a-zA-Z0-9]+ | ([a-zA-Z0-9]+[-])+[a-zA-Z0-9]+'[a-zA-Z0-9]+
hyphenated = ([a-zA-Z0-9]+[-])+[a-zA-Z0-9]+
punctuation = [^a-zA-Z0-9 \t\f\r\n]

%%

/* These are the pattern-action combinations that output the relevant SGML tag contents out to SGMLScanner 
   and filter out irrelevant SGML tag content */

{whiteSpace}*              { /* Skip Whitespace */ }

                           /* Content is not filtered out here, and tags are kept in check */
<YYINITIAL>{closeTag}      {
                               /* Extract tag name from <> (assume no attributes -> will mismatch otherwise) */
                               String tagName = yytext().substring(2, yytext().length() - 1);
                               tagName = tagName.toUpperCase();
                               tagName = tagName.trim ();

                               /* Check and remove tag name from stack and send for output if match */
                               String lastTag = tagStack.get(tagStack.size() - 1);
                               if (lastTag.matches (tagName) == false)
                                   System.err.println ("SGML Tag Mismatch: </" + tagName + "> @ line:" + yyline + " column:" + yycolumn);
                               else {
                                   tagStack.remove (tagStack.size() - 1);
                                   lastRelevantTag --;
                                   return new TokenSGML (TokenSGML.CLOSE_TAG, tagName, yyline, yycolumn);
                               }
                           }
<YYINITIAL>{openTag}       {
                               /* Extract tag name from <>, normalize, and remove attributes*/
                               String tagName = yytext().substring(1, yytext().length() - 1);
                               tagName = tagName.toUpperCase();
                               tagName = tagName.trim ();

                               int spaceIndex = tagName.indexOf (' ');
                               if (spaceIndex != -1)    tagName = tagName.substring(0, spaceIndex);

                               /* Add tag name to stack */
                               tagStack.add (tagName);
                              
                               /* Assess tag name to see if it is a relevant or irrelevant tag */
                               if (tagName.matches ("TEXT") == true || tagName.matches ("DATE") == true || tagName.matches ("DOC") ||
                                   tagName.matches ("DOCNO") == true || tagName.matches ("HEADLINE") == true || tagName.matches ("LENGTH") ||
                                   tagName.matches ("P")) {    //relevant tags, update last relevant tag location and send to output
                                   lastRelevantTag++;
                                   return new TokenSGML (TokenSGML.OPEN_TAG, tagName, yyline, yycolumn);
                               } else {    //irrelevant tags, transition to <IRRELEVANT>
                                   yybegin(IRRELEVANT);
                               }
                           }
<YYINITIAL>{word}          { return new TokenSGML (TokenSGML.WORD, yytext(), yyline, yycolumn); }
<YYINITIAL>{number}        { return new TokenSGML (TokenSGML.NUMBER, yytext(), yyline, yycolumn); }
<YYINITIAL>{apostrophized} { return new TokenSGML (TokenSGML.APOSTROPHIZED, yytext(), yyline, yycolumn); }
<YYINITIAL>{hyphenated}    { return new TokenSGML (TokenSGML.HYPHENATED, yytext(), yyline, yycolumn); }
<YYINITIAL>{punctuation}   { return new TokenSGML (TokenSGML.PUNCTUATION, yytext(), yyline, yycolumn); }

                           /* Irrelevant tags are still tracked for nesting, but are not sent out for output */
<IRRELEVANT>{closeTag}     {
                               /* Extract tag name from <> (assume no attributes -> will mismatch otherwise) */
                               String tagName = yytext().substring(2, yytext().length() - 1);
                               tagName = tagName.toUpperCase();
                               tagName = tagName.trim ();

                               /* Check and remove tag name from stack if match */
                               String lastTag = tagStack.get(tagStack.size() - 1);
                               if (lastTag.matches (tagName) == false)
                                   System.err.println ("SGML Tag Mismatch: </" + tagName + "> @ line:" + yyline + " column:" + yycolumn);
                               else {
                                   tagStack.remove (tagStack.size() - 1);

                                   /* If the tag stack reaches back to the last marked relevant tag, re-enter relevant (YYINITIAL) state */
                                   if (tagStack.size() == lastRelevantTag)    yybegin(YYINITIAL);
                               }
                           }
<IRRELEVANT>{openTag}      {
                               /* Extract tag name from <>, normalize, and remove attributes*/
                               String tagName = yytext().substring(1, yytext().length() - 1);
                               tagName = tagName.toUpperCase();
                               tagName = tagName.trim ();

                               int spaceIndex = tagName.indexOf (' ');
                               if (spaceIndex != -1)    tagName = tagName.substring(0, spaceIndex);

                               /* Add tag name to stack */
                               tagStack.add (tagName);
                           }
<IRRELEVANT>.              { /* Content here is filtered out and ignored */ }

                           /* If none of the above rules apply, report the error character with location */
                           /* Unmatchable with current scanner - commented out ->
.                          { System.err.println ("Error pattern @ line:" + yyline + " column:" + yycolumn); }*/

