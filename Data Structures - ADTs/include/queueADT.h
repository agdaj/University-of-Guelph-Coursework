/*****************************
Name: Jireh Agda
Date Last Modified: 2014 07 30
Queue ADT Header
*****************************/

/*This is the header file for the queue ADT*/
/*The first set of functions create and destroy the queue.
  The second set of functions add and remove data from the queue*/

#ifndef queueADT_h
#define queueADT_h

#include "linkedListADT.h"    /*This header file includes the definition of the List struct*/

/*This struct contains the List struct*/
struct QueueStruct
{
    List * queueList;
};
typedef struct QueueStruct Queue;

/*Queue ADT Basic Functions*/

/****
Preconditions: A valid destroy function for the data to be inserted is passed in
Postconditions: A queue is created with a list in it, else an appropriate error message appears
****/
Queue * createQueue (void (* destroyQueueData) (void *));

/****
Preconditions: A queue is initialized and passed in
Postconditions: The queue is freed from memory, else an error message appears for the portion of queue that does not exist
****/
void destroyQueue (Queue * queueToBeDestroyed);



/*Queue ADT Add/Remove Functions*/

/****
Preconditions: An initialized queue is passed in
Postconditions: The queue increases in size by one, and a node is added to the back of the queue, else an error message appears
****/
void addToQueue (Queue * queueToAdd, void * unit);

/****
Preconditions: A non-empty queue is passed in
Postconditions: The front node of the queue is removed and its value is returned, else an error message appears
****/
void * removeFromQueue (Queue * removeQueue);

#endif
