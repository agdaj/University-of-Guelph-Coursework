/************************************************
CompoundExp.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 04 04
C- Abstract Syntax Tree Compound Expression Class
Compiler Project - CIS*4650
************************************************/

package AbSynCM;

public class CompoundExp extends Exp implements Stmt {
    public VarDecList decs;
    public ExpList exps;

    public CompoundExp (int pos, VarDecList decs, ExpList exps) {
        this.pos = pos;
        this.decs = decs;
        this.exps = exps;
    }
}
