/******************************************************
VarDecList.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Variable Declaration List Class
Compiler Project - CIS*4650
******************************************************/

package AbSynCM;

public class VarDecList {
    public VarDec head;
    public VarDecList tail;

    public VarDecList (VarDec head, VarDecList tail) {
        this.head = head;
        this.tail = tail;
    }
}
