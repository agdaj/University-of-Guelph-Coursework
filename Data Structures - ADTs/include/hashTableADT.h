/*****************************
Name: Jireh Agda
Date Last Modified: 2014 07 30
Hash Table ADT Header
*****************************/

/*This is the header file for the hash table ADT, with separate chaining collision resolution and (mainly) division hash function*/
/*The first set of functions create and destroy the hash table.
  The second set of functions insert, remove, lookup and update data in the hash table.
  The last set of functions are for the hash function used by the hash table in its main functions, and should not be used separately*/

#ifndef hashTableADT_h
#define hashTableADT_h

#include "linkedListADT.h"    /*This include statement is to identify the data type PhoneNode*/

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
Postconditions: A hash table is created and initialized or an error is displayed
****/
HashTable * createHashTable (int size, void (* destroyHashData) (void *));

/****
Preconditions: An initialized hash table is passed in
Postconditions: The hash table is removed from memory or an error is displayed
****/
void destroyHashTable (HashTable * noMoreTable);



/*Hash Table ADT Main Functions*/

/****
Preconditions: An initialized hash table and key string are passed in, along with a pointer to an insert value
Postconditions: A value is inserted into the hash table, else an error is displayed
****/
void insertHashTable (HashTable * modifyTable, char * insertKey, void * insertValue);

/****
Preconditions: An initialized hash table and key string are passed in
Postconditions: A value is removed from the table and returned to be used if the item is found, else an error is displayed
****/
void * removeHashTable (HashTable * reducedTable, char * removeKey);

/****
Preconditions: An initialized hash table and key string are passed in
Postconditions: A value is returned to be used if the item is found, else an error is displayed
****/
void * lookupHashTable (HashTable * theTable, char * lookupKey);

/****
Preconditions: An initialized hash table and key string are passed in, along with a pointer to an update value
Postconditions: A value is updated in the table if the item is found, else an error is displayed
                If the key is not found, the function will add it to the hash table with insertHashTable
****/
void updateHashTable (HashTable * newTable, char * updateKey, void * newValue);



/*Hash Table ADT Hash Function(s)*/

/****
Preconditions: An initialized string representing a key and the hash table size (> 0) are passed in
Postconditions: A table position is returned from the hashing function, else an error message appears
****/
int hashFunction (char * keyString, int tableSize);

/****
Preconditions: An initialized string representing a key are passed in
Postconditions: A preconditioned string is retured from the preconditioning, else an error message appears
****/
char * preconditionKey (char * stringToNum);

#endif
