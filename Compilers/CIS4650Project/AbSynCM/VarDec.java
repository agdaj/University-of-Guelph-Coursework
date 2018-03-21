/*************************************************
VarDec.java (Abstract)
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 04 04
C- Abstract Syntax Tree Variable Declaration Class
Compiler Project - CIS*4650
*************************************************/

package AbSynCM;

abstract public class VarDec extends Dec {
    public NameTy typ;
    public String name;

    //for code generation markers
    public int offset;
    public int nestLevel;
    public boolean isAddress;
}
