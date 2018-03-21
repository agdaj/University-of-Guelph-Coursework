/*****************************
Name: Jireh Agda
Date Last Modified: 2014 07 30
Stack ADT Module
*****************************/

#include "linkedListADT.h"
#include "stackADT.h"
#include <stdio.h>
#include <stdlib.h>

/*Stack ADT Basic Functions*/

Stack * createStack (void (* destroyStackData) (void *))
{
    Stack * newStack;

    /*The stack struct is made if possible*/
    newStack = malloc (sizeof (Stack));
    if (newStack == NULL)
    {
        printf ("Error - Could not create stack\n");
        return (NULL);
    }
    else
    {
        newStack->stackList = createList (destroyStackData);
        return (newStack);
    }
}

void destroyStack (Stack * stackToBeDestroyed)
{
    /*The stack pointer is checked to see if a stack exists to be destroyed*/
    if (stackToBeDestroyed == NULL)
    {
        printf ("Error - No stack detected to be destroyed\n");
    }
    else
    {
        destroyList (stackToBeDestroyed->stackList);
        free (stackToBeDestroyed);
    }

    return;
}



/*Stack ADT Data Functions*/

void * peekStack (Stack * theStack)
{
    void * firstValue;

    /*The stack pointer is analyzed to see if the stack has a value to peek at*/
    if (theStack == NULL)
    {
        printf ("Error - No stack to peek value\n");
        return (0);
    }
    else
    {
        firstValue = getFrontValue (theStack->stackList);
        return (firstValue);
    }
}



/*Stack ADT Add/Remove Functions*/

void pushStack (Stack * stackToAdd, void * element)
{
    /*The stack pointer is analyzed to see if it has been initialized for the push to take place*/
    if (stackToAdd == NULL)
    {
        printf ("Error - No stack to push value\n");
    }
    else
    {
        addToFront (stackToAdd->stackList, element);
    }

    return;
}

void * popStack (Stack * removeStack)
{
    void * firstElement;

    /*The stack pointer is checked to see if a stack exists to pop an element from*/
    if (removeStack == NULL)
    {
        printf ("Error - No stack to pop value\n");
        return (0);
    }
    else
    {
        firstElement = removeFromFront (removeStack->stackList);
        return (firstElement);
    }
}
