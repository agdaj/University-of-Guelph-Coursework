/********************************************
SimpleVar.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Simple Variable Class
Compiler Project - CIS*4650
********************************************/

package AbSynCM;

public class SimpleVar extends Var {

    public SimpleVar (int pos, String name) {
        this.pos = pos;
        this.name = name;
    }
}
