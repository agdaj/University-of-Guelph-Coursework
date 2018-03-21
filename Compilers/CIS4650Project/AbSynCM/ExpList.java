/********************************************
ExpList.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Expression List Class
Compiler Project - CIS*4650
********************************************/

package AbSynCM;

public class ExpList {
    public Exp head;
    public ExpList tail;

    public ExpList (Exp head, ExpList tail) {
        this.head = head;
        this.tail = tail;
    }
}
