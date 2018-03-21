/********************************************************
SimpleDec.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Simple Variable Declaration Class
Compiler Project - CIS*4650
********************************************************/

package AbSynCM;

public class SimpleDec extends VarDec {

    public SimpleDec (int pos, NameTy typ, String name) {
        this.pos = pos;
        this.typ = typ;
        this.name = name;
    }
}
