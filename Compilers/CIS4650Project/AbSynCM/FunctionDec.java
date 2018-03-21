/*************************************************
FunctionDec.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 04 04
C- Abstract Syntax Tree Function Declaration Class
Compiler Project - CIS*4650
*************************************************/

package AbSynCM;

public class FunctionDec extends Dec {
    public NameTy result;
    public String func;
    public VarDecList params;
    public CompoundExp body;

    public int funcAddr;    //absolute start address in code

    public FunctionDec (int pos, NameTy result, String func, VarDecList params, CompoundExp body) {
        this.pos = pos;
        this.result = result;
        this.func = func;
        this.params = params;
        this.body = body;
    }
}
