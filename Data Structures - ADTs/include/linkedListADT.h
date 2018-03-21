/*****************************
Name: Jireh Agda
Date Last Modified: 2014 07 30
Linked List ADT Header
*****************************/

/*This is the header file for the linked list ADT*/
/*The first set of functions create and destroy the list.
  The second set of functions investigate the data of the linked list.
  The third set of functions add and remove data from the linked list.
  The last function is used to help the other add/remove functions, and should not be used separately*/

#ifndef linkedListADT_h
#define linkedListADT_h

/*This struct outlines the definition of an internal node of the linked list*/
struct ListNode
{
    void * nodeData;
    struct ListNode * next;
};
typedef struct ListNode Node;

/*This struct outlines the definition of the linked list head node*/
struct LinkedList
{
    int listLength;
    void (* destroyData) (void *);
    struct ListNode * firstNode;
};
typedef struct LinkedList List;

/*Linked List ADT Basic Functions*/

/****
Preconditions: A valid destroy function is passed in
Postconditions: A list is created with a head node that keeps track of list length or an error is displayed
****/
List * createList (void (* destroyListData) (void *));

/****
Preconditions: A list is initialized and passed in
Postconditions: The list is freed in memory or an error is displayed
****/
void destroyList (List * listToBeDestroyed);



/*Linked List ADT Data Functions*/

/****
Preconditions: A non-empty list is passed in
Postconditions: The value of the front node is retrieved or an error is displayed
****/
void * getFrontValue (List * valueList);

/****
Preconditions: An initialized list is passed into the function
Postconditions: The list length is retrieved from the head node or an error is displayed
****/
int listLength (List * list);

/****
Preconditions: A non-empty list and a valid printing function is passed in
Postconditions: The list of data in the linked list is printed according to the given print function or an error is displayed
****/
void printList (List * listToPrint, void (* printData) (void *));



/*Linked List ADT Add/Remove Functions*/

/****
Preconditions: An initialized list is passed in
Postconditions: The value is placed into a node at the front of the list, extending the list by one, or an error
                is displayed
****/
void addToFront (List * currentList, void * nodeValue);

/****
Preconditions: An initialized list is passed in
Postconditions: The value is placed into a node at the back of the list, extending the list by one, or an error
                is displayed
****/
void addToBack (List * fullList, void * newBackValue);

/****
Preconditions: A non-empty list is passed in
Postconditions: The node is removed from the list from the front, reducing the list length by one and returning the value in it, or an error is displayed
****/
void * removeFromFront (List * frontList);

/****
Preconditions: A non-empty list is passed in
Postconditions: The node is removed from the list at the back, reducing the list length by one and returning the value in it, or an error is displayed
****/
void * removeFromBack (List * backList);



/*Linked List ADT Helper Function(s)*/
/*Do NOT use separately*/

/****
Preconditions: None
Postconditions: A node is created and can be placed into a list or an error is displayed
****/
Node * initNode (void * newValue);

#endif
