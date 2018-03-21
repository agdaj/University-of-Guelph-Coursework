/********************************************
CallExp.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 04 04
C- Abstract Syntax Tree Call Expression Class
Compiler Project - CIS*4650
********************************************/

package AbSynCM;

public class CallExp extends Exp {
    public String func;
    public ExpList args;
    public FunctionDec funcToCall = null;

    public CallExp (int pos, String func, ExpList args) {
        this.pos = pos;
        this.func = func;
        this.args = args;
    }
}
