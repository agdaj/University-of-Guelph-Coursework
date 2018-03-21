/**************************************************
AssignExp.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Assignment Expression Class
Compiler Project - CIS*4650
**************************************************/

package AbSynCM;

public class AssignExp extends Exp {
    public Var lhs;
    public Exp rhs;

    public AssignExp (int pos, Var lhs, Exp rhs) {
        this.pos = pos;
        this.lhs = lhs;
        this.rhs = rhs;
    }
}
