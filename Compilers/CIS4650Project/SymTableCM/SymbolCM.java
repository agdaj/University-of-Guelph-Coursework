/*****************************
SymbolCM.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 16
     Last Modified: 2017 03 19
C- Symbol Table Symbol Class
Compiler Project - CIS*4650
*****************************/

package SymTableCM;

import AbSynCM.*;

public class SymbolCM {
    public String name;
    public Dec type;
    public int depth;

    public SymbolCM (String name, Dec type, int depth) {
        this.name = name;
        this.type = type;
        this.depth = depth;
    }
}
