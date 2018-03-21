/*************************************************
OpExp.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Operation Expression Class
Compiler Project - CIS*4650
*************************************************/

package AbSynCM;

public class OpExp extends Exp {
    public static final int PLUS  = 0;
    public static final int MINUS = 1;
    public static final int MUL   = 2;
    public static final int DIV   = 3;
    public static final int LT    = 4;
    public static final int LE    = 5;
    public static final int GT    = 6;
    public static final int GE    = 7;
    public static final int EQ    = 8;
    public static final int NE    = 9;
    public static final int ERROR = 10;

    public Exp left;
    public int op;
    public Exp right;

    public OpExp (int pos, Exp left, int op, Exp right) {
        this.pos = pos;
        this.left = left;
        this.op = op;
        this.right = right;
    }
}
