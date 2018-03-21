/*****************************
Name: Jireh Agda
Date Last Modified: 2014 07 30
Linked List ADT Module
*****************************/

#include "linkedListADT.h"
#include <stdio.h>
#include <stdlib.h>

/*Linked List ADT Basic Functions*/

List * createList (void (* destroyListData) (void *))
{
    List * newHead;

    /*If the function pointer parameter is NULL, the function outputs an error and returns NULL*/
    if (destroyListData == NULL)
    {
        printf ("Error - Could not create list with function pointer parameter\n");
        return (NULL);
    }
    else
    {
        /*The dummy node is made if possible, and intitialized*/
        newHead = malloc (sizeof (List));
        if (newHead == NULL)
        {
            printf ("Error - Could not create list\n");
            return (NULL);
        }
        else
        {
            newHead->listLength = 0;
            newHead->destroyData = destroyListData;
            newHead->firstNode = NULL;
            return (newHead);
        }
    }
}

void destroyList (List * listToBeDestroyed)
{
    Node * destroyNode;
    Node * nextNode;

    /*The function checks if the list passed in exists, and removes it if it does exist*/
    if (listToBeDestroyed == NULL)
    {
        printf ("Error - No list is available to be destroyed\n");
    }
    else
    {
        destroyNode = listToBeDestroyed->firstNode;

        /*The nodes in the list are removed down through until the end NULL is reached*/
        while (destroyNode != NULL)
        {
            nextNode = destroyNode->next;
            listToBeDestroyed->destroyData (destroyNode->nodeData);
            free (destroyNode);
            destroyNode = nextNode;
        }

        free (listToBeDestroyed);
    }

    return;
}



/*Linked List ADT Data Functions*/

void * getFrontValue (List * valueList)
{
    void * frontNodeValue;

    /*The function checks if the given list is empty or does not exist, and retrieves the front list value if
      it is there*/
    if (valueList == NULL || valueList->firstNode == NULL)
    {
        printf ("Error - Empty or nonexistent list - Cannot retrieve front list value\n");
        return (0);
    }
    else
    {
        frontNodeValue = valueList->firstNode->nodeData;
        return (frontNodeValue);
    }
}

int listLength (List * list)
{
    int lengthOfList;

    /*The function checks if the list passed in exists or not to retrieve the list length. A 0 is returned for a
      nonexisting list, which essentially means an empty list*/
    if (list == NULL)
    {
        printf ("Error - Could not retrieve length of nonexisting list\n");
        return (0);
    }
    else
    {
        /*The list head node contains the list length, and is just retrieved from there*/
        lengthOfList = list->listLength;
        return (lengthOfList);
    }
}

void printList (List * listToPrint, void (* printData) (void *))
{
    Node * currentNode;

    /*The function checks if the pointer passed in is NULL or only has a dummy node (representing an nonexisting/empty list) and will skip the printing process
      if these conditions are true. Otherwise, the list is printed*/
    if (listToPrint == NULL || listToPrint->firstNode == NULL)
    {
        printf ("Error - No list to print\n");
    }
    else
    {
        currentNode = listToPrint->firstNode;

        while (currentNode != NULL)
        {
            printData (currentNode->nodeData);

            currentNode = currentNode->next;
        }
    }

    return;
}



/*Linked List ADT Add/Remove Functions*/

void addToFront (List * currentList, void * nodeValue)
{
    Node * addingNode;

    /*The function checks if the calling program has an initialized list and node data as a parameter, otherwise the function will print an error*/
    if (currentList == NULL)
    {
        printf ("Error - No list exists to add onto\n");
    }
    else if (nodeValue == NULL)
    {
        printf ("Error - Value to be added at the back is NULL\n");
        printf ("Value not added\n");
    }
    else
    {
        addingNode = initNode (nodeValue);

        addingNode->next = currentList->firstNode;
        currentList->firstNode = addingNode;

        /*The head node keeping track of the list length increments*/
        currentList->listLength ++;
    }

    return;
}

void addToBack (List * fullList, void * newBackValue)
{
    Node * addNode;
    Node * nodeTracker;
    Node * placeHolderNode;

    /*The function checks if the calling program has an initialized list and node data as a parameter, otherwise the function will print an error*/
    if (fullList == NULL)
    {
        printf ("Error - No list exists to add onto\n");
    }
    else if (newBackValue == NULL)
    {
        printf ("Error - Value to be added is NULL\n");
        printf ("Value not added\n");
    }
    else
    {
        addNode = initNode (newBackValue);
        nodeTracker = fullList->firstNode;
        placeHolderNode = NULL;

        /*If the list is empty, the value is added as the first element*/
        if (nodeTracker == NULL)
        {
            fullList->firstNode = addNode;
        }
        else
        {
            /*The function travels down the list until the back of the list is reached*/
            while (nodeTracker != NULL)
            {
                placeHolderNode = nodeTracker;
                nodeTracker = nodeTracker->next;
            }

            placeHolderNode->next = addNode;
        }

        /*The head node keeping track of the list length increments*/
        fullList->listLength ++;
    }

    return;
}

void * removeFromFront (List * frontList)
{
    void * oldFrontValue;
    Node * oldFront;

    /*The function checks if the list is empty/nonexistent, and executes accordingly*/
    if (frontList == NULL || frontList->firstNode == NULL)
    {
        printf ("Error - No list exists to remove from front\n");
        return (0);
    }
    else
    {
        oldFront = frontList->firstNode;
        frontList->firstNode = oldFront->next;

        /*The head node keeping track of the list length decrements*/
        frontList->listLength --;

        oldFrontValue = oldFront->nodeData;
        return (oldFrontValue);
    }
}

void * removeFromBack (List * backList)
{
    void * oldBackValue;
    Node * oldBack;
    Node * nodeHolder;

    /*The function checks if the list is empty/nonexistent, and executes accordingly*/
    if (backList == NULL || backList->firstNode == NULL)
    {
        printf ("Error - No list exists to remove from back\n");
        return (0);
    }
    else
    {
        oldBack = backList->firstNode;

        /*The function travels down the list until the back of the list is reached*/
        while (oldBack->next != NULL)
        {
            nodeHolder = oldBack;
            oldBack = oldBack->next;
        }

        nodeHolder->next = NULL;

        /*The head node keeping track of the list length decrements*/
        backList->listLength --;

        oldBackValue = oldBack->nodeData;
        return (oldBackValue);
    }
}



/*Linked List ADT Helper Function(s)*/

Node * initNode (void * newValue)
{
    Node * newNode;

    /*The new list node is made if possible, and intitialized*/
    newNode = malloc (sizeof (Node));
    if (newNode == NULL)
    {
        printf ("Error - Could not create new list element\n");
        return (NULL);
    }
    else
    {
        newNode->nodeData = newValue;
        newNode->next = NULL;
        return (newNode);
    }
}

