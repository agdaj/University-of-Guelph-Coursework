/*****************************
Name: Jireh Agda
Date Last Modified: 2014 07 30
Hash Table ADT Module
*****************************/

#include "hashTableADT.h"
#include "linkedListADT.h"
#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/*Hash Table ADT Basic Functions*/

HashTable * createHashTable (int size, void (* destroyHashData) (void *))
{
    HashTable * newHashTable;
    int a;

    /*If the given table size is under 1 or if the destroy function is NULL, the creation of the hash table stops immediately and the user is notified*/
    if (size <= 0 || destroyHashData == NULL)
    {
        printf ("Error - Given hash table size is 0 or lower\n");
        return (NULL);
    }

    /*A new hash table is made if possible, and initialized*/
    newHashTable = malloc (sizeof (HashTable));
    if (newHashTable == NULL)
    {
        printf ("Error - Could not create full hash table\n");
        return (NULL);
    }
    else
    {
        /*The hash table is configured by the given size, and the positions are initialized with list head nodes*/
        newHashTable->hashTableSize = size;
        newHashTable->hashPosition = malloc (sizeof (List *) * size);
        if (newHashTable->hashPosition == NULL)
        {
            printf ("Error - Could not create main hash table\n");
            free (newHashTable);
            return (NULL);
        }
        else
        {
            for (a = 0; a < size; a ++)
            {
                newHashTable->hashPosition[a] = createList (destroyHashData);
            }

            return (newHashTable);
        }
    }
}

void destroyHashTable (HashTable * noMoreTable)
{
    HashNode * destroyData;
    HashNode * nextData;
    int z;

    /*The function checks if the given hash table pointer points to a hash table, and will destroy the table (remove from memory) if it does*/
    if (noMoreTable == NULL)
    {
        printf ("Error - No hash table passed in to destroy\n");
    }
    else
    {
        /*Each hash position linked list is removed from memory*/
        for (z = 0; z < noMoreTable->hashTableSize; z ++)
        {
            destroyList (noMoreTable->hashPosition[z]);
        }

        free (noMoreTable->hashPosition);
        free (noMoreTable);
    }

    return;
}



/*Hash Table ADT Main Functions*/

void insertHashTable (HashTable * modifyTable, char * insertKey, void * insertValue)
{
    List * insertPointer;
    int insertPosition;

    /*If either the table or key are NULL, the function will not continue and return immediately with a error message*/
    if (modifyTable == NULL || insertKey == NULL)
    {
        printf ("Error - Table or key missing to insert data\n");
        return;
    }

    /*The key is hashed to find the hash position*/
    insertPosition = hashFunction (insertKey, modifyTable->hashTableSize);

    /*The node will be placed in the position where the first NULL pointer is found*/
    insertPointer = modifyTable->hashPosition[insertPosition];

    /*The node is placed at the back of the linked list with addToBack (collision resolution)*/
    addToBack (insertPointer, insertValue);

    return;
}

void * removeHashTable (HashTable * reducedTable, char * removeKey)
{
    List * removeList;
    int removeMatch;
    int removePosition;
    Node * placeHolder;
    Node * removeValue;
    void * outValue;

    /*If either the table or key are NULL, the function will not continue and return immediately with a error message*/
    if (reducedTable == NULL || removeKey == NULL)
    {
        printf ("Error - Table or key missing to remove data\n");
        return (NULL);
    }

    /*The key is hashed to find the hash position*/
    removePosition = hashFunction (removeKey, reducedTable->hashTableSize);

    /*To 'skip' the list head node, the pointer shifts one over before the search*/
    removeList = reducedTable->hashPosition[removePosition];
    removeValue = removeList->firstNode;
    placeHolder = NULL;

    /*The hash position is searched for the correct key-value pair, and will remove it from the linked list in the position.
      The value is returned*/
    while (removeValue != NULL)
    {
        removeMatch = strcmp (removeKey, removeValue->nodeData->/*Insert string of focus here*/);

        if (removeMatch == 0)
        {
            outValue = removeValue->nodeData;

            /*The linked list ir realigned to adjust from the removed node*/
            if (placeHolder == NULL)
            {
                reducedTable->hashPosition[removePosition]->firstNode = removeValue->next;
            }
            else
            {
                placeHolder->next = removeValue->next;
            }

            free (removeValue);

            return (outValue);
        }

        placeHolder = removeValue;
        removeValue = removeValue->next;
    }

    /*If the key-value pair is not found, no removing takes place and an error message will be printed*/
    printf ("Error - Key does not point to a value\n");
    return (NULL);
}

void * lookupHashTable (HashTable * theTable, char * lookupKey)
{
    List * lookupList;
    int lookupMatch;
    int lookupPosition;
    Node * lookupValue;
    void * foundValue;

    /*If either the table or key are NULL, the function will not continue and return immediately with a error message*/
    if (theTable == NULL || lookupKey == NULL)
    {
        printf ("Error - Table or key missing to lookup data\n");
        return (NULL);
    }

    /*The key is hashed to find the hash position*/
    lookupPosition = hashFunction (lookupKey, theTable->hashTableSize);

    /*To 'skip' the list head node, the pointer shifts one over before the search*/
    lookupList = theTable->hashPosition[lookupPosition];
    lookupValue = lookupList->firstNode;

    /*The position is searched through for the matching key-value pair and returns*/
    while (lookupValue != NULL)
    {
        lookupMatch = strcmp (lookupKey, lookupValue->nodeData->/*Insert string of focus here*/);

        if (lookupMatch == 0)
        {
            foundValue = lookupValue->nodeData;

            return (foundValue);
        }

        lookupValue = lookupValue->next;
    }

    /*The function will return this error if the key-value pair cannot be found*/
    printf ("Error - Key does not result in an already placed value\n");
    return (NULL);
}

void updateHashTable (HashTable * newTable, char * updateKey, void * newValue)
{
    List * formerList;
    int updateMatch;
    int updatePosition;
    Node * formerValue;

    /*If either the table or key are NULL, the function will not continue and return immediately with a error message*/
    if (newTable == NULL || updateKey == NULL)
    {
        printf ("Error - Table or key missing to update data\n");
        return;
    }

    /*The key is hashed to find the hash position*/
    updatePosition = hashFunction (updateKey, newTable->hashTableSize);

    /*The position is checked for the matching key in the list (past the head list node)*/
    formerList = newTable->hashPosition[updatePosition];
    formerValue = formerList->firstNode;

    /*A non-empty position is looked through here, where in the event of a match, the value is replaced*/
    while (formerValue != NULL)
    {
        updateMatch = strcmp (updateKey, formerValue->nodeData->/*Insert string of focus here*/);

        if (updateMatch == 0)
        {
            formerValue->phoneData = newValue;

            return;
        }

        formerValue = formerValue->next;
    }

    /*Else, the value is simply inserted before returning*/

    printf ("Error - Key does not result in an already placed value\n");
    insertHashTable (newTable, updateKey, newValue);
    printf ("Key and value added to hash table\n");

    return;
}



/*Hash Table ADT Hash Function(s)*/

int hashFunction (char * keyString, int tableSize)
{
    char * numString;
    char numTracker;
    int i;
    int key;
    int tempNum;

    /*If either parameter given cannot be used, the hash function will return immediately and an error message is printed*/
    if (keyString == NULL || tableSize <= 0)
    {
        printf ("Error - Given hash function parameters not suitable to be hashed\n");
        return (0);
    }

    i = 0;
    key = 0;

    /*The key is sent through preconditioning (turning all non-digit characters to numbers*/
    numString = preconditionKey (keyString);

    numTracker = numString[i];

    /*Each num character is added into each other to get an intermediate key*/
    while (numTracker != '\0')
    {
        tempNum = (int) numTracker - 48;

        key = key + tempNum;

        i ++;
        numTracker = numString[i];
    }

    /*The division method of hashing is used, and the result is returned*/
    key = key % tableSize;

    free (numString);

    return (key);
}

char * preconditionKey (char * stringToNum)
{
    char * fullNumString;
    char stringTracker;
    int conversion;
    int j;

    j = 0;

    /*If the parameter string is NULL, the function will not work with it and will return with an error*/
    if (stringToNum == NULL)
    {
        printf ("Error - Given hash function parameters not suitable to be preconditioned\n");
        return (0);
    }

    /*A temporary new string is made and data is copied over to be preconditioned*/
    fullNumString = malloc (sizeof (char) * (strlen (stringToNum) + 1));
    if (fullNumString == NULL)
    {
        printf ("Error - Key could not be preconditioned\n");
        printf ("Hash function may not work properly\n");
        return (0);
    }
    else
    {
        strcpy (fullNumString, stringToNum);

        stringTracker = fullNumString[j];

        /*Each character in the string is investigated to turn all non-digit characters into digits predictably, using their ASCII number
          and converting a single digit form back into its respective char form*/
        while (stringTracker != '\0')
        {
            if (isdigit (stringTracker))
            {
                fullNumString[j] = fullNumString[j];
            }
            else
            {
                conversion = (int) fullNumString[j] % 10;
                fullNumString[j] = (char) (conversion + 48);
            }

            j++;
            stringTracker = fullNumString[j];
        }
    }

    return (fullNumString);
}
