/*****************************
ScannerSGML.java
Name: Jireh Agda
Date Created: 2017 01 23
     Last Modified: 2017 01 24
SGML Main Scanner/Output Class
Warm-Up Assignment - CIS*4650
*****************************/

import java.io.InputStreamReader;

/**
 * ScannerSGML defines the class that prints String representations of relevant SGML tags
 * @author agdaj
 */
public class ScannerSGML {
    private LexerSGML scanner = null;

    /**
     * Constructs SGML Scanner that uses SGML.flex lexer specifications
     */
    public ScannerSGML (LexerSGML lexer) {
        scanner = lexer;
    }

    /**
     * Retrieves the next token of the specified stream
     * @return returns TokenSGML containing Token data
     */
    public TokenSGML getNextToken() throws java.io.IOException {
        return scanner.yylex();
    }

    /**
     * @param args the command line arguments
     */
    public static void main (String argv[]) {
        try {
            ScannerSGML scanner = new ScannerSGML (new LexerSGML (new InputStreamReader (System.in)));
            TokenSGML token = null;

            while ((token = scanner.getNextToken ()) != null)    System.out.println (token);
        } catch (Exception e) {
            System.out.println ("Unexpected Exception:");
            e.printStackTrace ();
        }
    }
}
