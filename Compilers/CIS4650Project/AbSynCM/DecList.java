/*********************************************
DecList.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 03 05
C- Abstract Syntax Tree Declaration List Class
Compiler Project - CIS*4650
*********************************************/

package AbSynCM;

public class DecList {
    public Dec head;
    public DecList tail;

    public DecList (Dec head, DecList tail) {
        this.head = head;
        this.tail = tail;
    }
}
