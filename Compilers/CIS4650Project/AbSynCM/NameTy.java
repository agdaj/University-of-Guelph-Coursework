/**************************************
NameTy.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Name Type Class
Compiler Project - CIS*4650
**************************************/

package AbSynCM;

public class NameTy extends SynTreeCM {
    public static final int INT  = 0;
    public static final int VOID = 1;

    public int typ;

    public NameTy (int pos, int typ) {
        this.pos = pos;
        this.typ = typ;
    }
}
