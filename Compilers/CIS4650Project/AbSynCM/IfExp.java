/******************************************
IfExp.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 04 04
C- Abstract Syntax Tree If Expression Class
Compiler Project - CIS*4650
******************************************/

package AbSynCM;

public class IfExp extends Exp implements Stmt {
    public Exp test;
    public Exp thenExp;
    public Exp elseExp;

    public IfExp (int pos, Exp test, Exp thenExp, Exp elseExp) {
        this.pos = pos;
        this.test = test;
        this.thenExp = thenExp;
        this.elseExp = elseExp;
    }
}
