/*****************************
Name: Jireh Agda
Date Last Modified: 2014 07 30
Stack ADT Header
*****************************/

/*This is the header file for the stack ADT*/
/*The first set of functions create and destroy the stack.
  The second set of functions investigate the data of the stack.
  The third set of functions add and remove data from the stack*/

#ifndef stackADT_h
#define stackADT_h

#include "linkedListADT.h"    /*This header file includes the definition of the List struct*/

/*This struct contains the List struct*/
struct StackStruct
{
    List * stackList;
};
typedef struct StackStruct Stack;

/*Stack ADT Basic Functions*/

/****
Preconditions: A valid destroy function for the data to be inserted is passed in
Postconditions: A stack is created with a list in it, else an appropriate error message appears
****/
Stack * createStack (void (* destroyStackData) (void *));

/****
Preconditions: A stack is initialized and passed in
Postconditions: The stack is freed from memory, else an error message appears for the portion of stack that does not exist
****/
void destroyStack (Stack * stackToBeDestroyed);



/*Stack ADT Data Functions*/

/****
Preconditions: A non-empty stack is passed in
Postconditions: The value of the front value is returned, else an error message appears
****/
void * peekStack (Stack * theStack);



/*Stack ADT Add/Remove Functions*/

/****
Preconditions: An initialized stack is passed in
Postconditions: The stack increases in size by one, and a node is added to the front of the stack, else an error message appears
****/
void pushStack (Stack * stackToAdd, void * element);

/****
Preconditions: A non-empty stack is passed in
Postconditions: The front node of the stack is removed and its value is returned, else an error message appears
****/
void * popStack (Stack * removeStack);

#endif
