/***********************************************
IntExp.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Integer Expression Class
Compiler Project - CIS*4650
***********************************************/

package AbSynCM;

public class IntExp extends Exp {
    public int value;

    public IntExp (int pos, int value) {
        this.pos = pos;
        this.value = value;
    }
}
