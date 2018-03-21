/*****************************
Name: Jireh Agda
Date Last Modified: 2014 07 30
Queue ADT Module
*****************************/

#include "linkedListADT.h"
#include "queueADT.h"
#include <stdio.h>
#include <stdlib.h>

/*Queue ADT Basic Functions*/

Queue * createQueue (void (* destroyQueueData) (void *))
{
    Queue * newQueue;

    /*The queue struct is made if possible*/
    newQueue = malloc (sizeof (Queue));
    if (newQueue == NULL)
    {
        printf ("Error - Could not create queue\n");
        return (NULL);
    }
    else
    {
        newQueue->queueList = createList (destroyQueueData);
        return (newQueue);
    }
}

void destroyQueue (Queue * queueToBeDestroyed)
{
    /*The queue pointer is checked to see if a queue exists to be destroyed*/
    if (queueToBeDestroyed == NULL)
    {
        printf ("Error - No queue detected to be destroyed\n");
    }
    else
    {
        destroyList (queueToBeDestroyed->queueList);
        free (queueToBeDestroyed);
    }

    return;
}



/*Queue ADT Add/Remove Functions*/

void addToQueue (Queue * queueToAdd, void * unit)
{
    /*The queue pointer is analyzed to see if it has been initialized for the add to take place*/
    if (queueToAdd == NULL)
    {
        printf ("Error - No queue to add value to\n");
    }
    else
    {
        addToBack (queueToAdd->queueList, unit);
    }

    return;
}

void * removeFromQueue (Queue * removeQueue)
{
    void * firstUnit;

    /*The queue pointer is checked to see if a queue exists to remove an element from*/
    if (removeQueue == NULL)
    {
        printf ("Error - No queue to remove value from\n");
        return (0);
    }
    else
    {
        firstUnit = removeFromFront (removeQueue->queueList);
        return (firstUnit);
    }
}
