/*****************************
TokenSGML.java
Name: Jireh Agda
Date Created: 2017 01 23
     Last Modified: 2017 01 25
SGML Token Specifier Class
Warm-Up Assignment - CIS*4650
*****************************/

/**
 * TokenSGML defines relevant token types to be printed by ScannerSGML
 * @author agdaj
 */
public class TokenSGML {
    public static final int OPEN_TAG = 0;
    public static final int CLOSE_TAG = 1;
    public static final int WORD = 2;
    public static final int NUMBER = 3;
    public static final int APOSTROPHIZED = 4;
    public static final int HYPHENATED = 5;
    public static final int PUNCTUATION = 6;

    public int tokenType;
    public String tokenValue;
    public int tokenLine;
    public int tokenColumn;

    /**
     * Constructs an SGML Token (TokenSGML)
     */
    public TokenSGML (int type, String value, int line, int column) {
        tokenType = type;
        tokenValue = value;
        tokenLine = line;
        tokenColumn = column;
    }

    /**
     * Returns String encoding of relevant SGML tags and content defined by SGML.flex
     * @return String format of TokenSGML
     */
    public String toString() {
        switch (tokenType) {
            case OPEN_TAG:
                return "OPEN-" + tokenValue;
            case CLOSE_TAG:
                return "CLOSE-" + tokenValue;
            case WORD:
                return "WORD(" + tokenValue + ")";
            case NUMBER:
                return "NUMBER(" + tokenValue + ")";
            case APOSTROPHIZED:
                return "APOSTROPHIZED(" + tokenValue + ")";
            case HYPHENATED:
                return "HYPHENATED(" + tokenValue + ")";
            case PUNCTUATION:
                return "PUNCTUATION(" + tokenValue + ")";
            default:
                return "UNKNOWN(" + tokenValue + ")";
        }
    }
}

