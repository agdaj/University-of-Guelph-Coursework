/************************************************
VarExp.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Variable Expression Class
Compiler Project - CIS*4650
************************************************/

package AbSynCM;

public class VarExp extends Exp {
    public Var variable;

    public VarExp (int pos, Var variable) {
        this.pos = pos;
        this.variable = variable;
    }
}
