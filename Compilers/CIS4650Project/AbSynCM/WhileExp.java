/*********************************************
WhileExp.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 04 04
C- Abstract Syntax Tree While Expression Class
Compiler Project - CIS*4650
*********************************************/

package AbSynCM;

public class WhileExp extends Exp implements Stmt {
    public Exp test;
    public Exp body;

    public WhileExp (int pos, Exp test, Exp body) {
        this.pos = pos;
        this.test = test;
        this.body = body;
    }
}
