/*******************************************************
ArrayDec.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Array Variable Declaration Class
Compiler Project - CIS*4650
*******************************************************/

package AbSynCM;

public class ArrayDec extends VarDec {
    public IntExp size;

    public ArrayDec (int pos, NameTy typ, String name, IntExp size) {
        this.pos = pos;
        this.typ = typ;
        this.name = name;
        this.size = size;
    }
}
