/*******************************************
IndexVar.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Index Variable Class
Compiler Project - CIS*4650
*******************************************/

package AbSynCM;

public class IndexVar extends Var {
    public Exp index;

    public IndexVar (int pos, String name, Exp index) {
        this.pos = pos;
        this.name = name;
        this.index = index;
    }
}
