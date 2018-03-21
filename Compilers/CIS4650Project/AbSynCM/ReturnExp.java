/**********************************************
ReturnExp.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 04 04
C- Abstract Syntax Tree Return Expression Class
Compiler Project - CIS*4650
**********************************************/

package AbSynCM;

public class ReturnExp extends Exp implements Stmt {
    public Exp exp;

    public ReturnExp (int pos, Exp exp) {
        this.pos = pos;
        this.exp = exp;
    }
}
