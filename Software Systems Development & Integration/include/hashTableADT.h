/*****************************
Name: Jireh Agda
Date Last Modified: 2015 01 19
Hash Table ADT Header
*****************************/

/*This is the header file for the hash table ADT, with separate chaining collision resolution and (mainly) division hash function*/
/*The first set of functions create and destroy the hash table.
  The second set of functions insert, remove, lookup and update data in the hash table.
  The last set of functions are for the hash function used by the hash table in its main functions, and should not be used separately*/

#ifndef hashTableADT_h
#define hashTableADT_h

#include "linkedListADT.h"    /*This include statement is to identify the data type List*/

/*This struct constructs the hash table, with an array of linked lists for data addition and collisions, and the table size
  of the hash table for quick convenience*/
struct HashTable
{
    List ** hashPosition;
    int hashTableSize;
};
typedef struct HashTable HashTable;

/*Hash Table ADT Basic Functions*/

/****
Preconditions: A positive integer for size and valid destroy function has been passed in
Postconditions: A hash table is created and initialized; HashTable is returned if successful, NULL otherwise
****/
HashTable * createHashTable (int size, void (* destroyHashData) (void *));

/****
Preconditions: An initialized hash table is passed in
Postconditions: The hash table is removed from memory
****/
void destroyHashTable (HashTable * noMoreTable);



/*Hash Table ADT Main Functions*/

/****
Preconditions: An initialized hash table and key string are passed in, along with a pointer to an insert value
Postconditions: A key-value pair is inserted into the hash table
****/
void insertHashTable (HashTable * modifyTable, char * insertKey, void * insertValue);

/****
Preconditions: An initialized hash table and key string are passed in
Postconditions: A key-value pair is removed from the table and the value is returned to be used if the item is found, else NULL is returned
****/
void * removeHashTable (HashTable * reducedTable, char * removeKey);

/****
Preconditions: An initialized hash table and key string are passed in
Postconditions: A value is returned to be used if the item is found, else NULL is returned
****/
void * lookupHashTable (HashTable * theTable, char * lookupKey);

/****
Preconditions: An initialized hash table and key string are passed in, along with a pointer to an update value
Postconditions: A value is updated in the table if the item is found, and the previous entry is freed from memory
                If the key is not found, the function will add it to the hash table with insertHashTable
****/
void updateHashTable (HashTable * newTable, char * updateKey, void * newValue);



/*Hash Table ADT Hash Function(s)*/

/****
Preconditions: An initialized string representing a key and the hash table size (> 0) are passed in
Postconditions: A table position (int) is returned from the hashing function, else -1 is returned
****/
int hashFunction (char * keyString, int tableSize);

/****
Preconditions: An initialized string representing a key are passed in
Postconditions: A preconditioned string is returned from the preconditioning, else NULL is returned
****/
char * preconditionKey (char * stringToNum);

#endif
