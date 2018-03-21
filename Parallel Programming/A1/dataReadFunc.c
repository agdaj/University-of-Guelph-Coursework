/**************************************
dataReadFunc.c
Name: Jireh Agda (0795472)
Date Created: 2016 10 10
     Last Modified: 2016 10 12
NCDB Query Program Parallelized
Assignment 1 - CIS*3090
- Functions to read and store NCDB data
**************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "pilot.h"
#include "dataReadFunc.h"


/****File Opening and Closing Functions****/

/****
openFile: Opens NCDB file for reading
Preconditions: Path points to valid file
Postconditions: Returns FILE *pointer to NCDB file
****/
FILE *openFile (char * path)
{
    FILE *recordFile = NULL;

    /*Immediate abort if file cannot be opened at any time*/
    recordFile = fopen (path, "r");
    if (recordFile == NULL)
    {
        PI_Abort (0, "Cannot open file", __FILE__, __LINE__);
    }

    return (recordFile);
}

/****
closeFile: Closes NCDB file
Preconditions: recordFile points to open NCDB file
Postconditions: NCDB file is closed if open
****/
void closeFile (FILE *recordFile)
{
    if (recordFile != NULL)
    {
        fclose (recordFile);
    }

    return;
}


/****Data Reading and Storing Functions****/

/****
createRecordSet: Creates RecordSet for worker/PI_MAIN that contains the process' NCDB data set
Preconditions: recordFile is open, startRecord and endRecord point to existing records in FILE, or endRecord+1 for marking the end of file
Postconditions: Returns RecordSet struct populated with Records of NCDB data
NOTE: Returns malloc'd RecordSet, free using freeRecordSet
****/
RecordSet *createRecordSet (FILE *recordFile, int startRecord, int endRecord)
{
    int i;
    int relativeIndex;
    int sameCollision;
    RecordSet *newRecordSet;

    relativeIndex = 0;

    /*Initialize RecordSet*/  
    newRecordSet = malloc (sizeof (RecordSet));
    if (newRecordSet == NULL)
    {
        PI_Abort (0, "malloc unsuccessful (RecordSet)", __FILE__, __LINE__);
    }
    newRecordSet->records = endRecord - startRecord;
    newRecordSet->recordArray = malloc (sizeof (Record *) * newRecordSet->records);
    newRecordSet->collisions = 0;
    newRecordSet->collisionIndex = NULL;
    if (newRecordSet->recordArray == NULL)
    {
        PI_Abort (0, "malloc unsuccessful (Record array)", __FILE__, __LINE__);
    }

    /*Read worker's section of file, noting if any collision changes occur, indexing when it occurs*/
    for (i = startRecord; i < endRecord; i++)
    {
        fseek (recordFile, offsetCalc (i), SEEK_SET);
        newRecordSet->recordArray[relativeIndex] = recordReader (recordFile);

        if (newRecordSet->collisions == 0)
        {
            newRecordSet->collisions++;
            newRecordSet->collisionIndex = malloc (sizeof (int) * newRecordSet->collisions);
            if (newRecordSet->collisionIndex == NULL)
            {
                PI_Abort (0, "malloc unsuccessful (int array)", __FILE__, __LINE__);
            }
            newRecordSet->collisionIndex[(newRecordSet->collisions)-1] = relativeIndex;
        }   
        else
        {
            sameCollision = collisionChecker (newRecordSet->recordArray[relativeIndex-1], newRecordSet->recordArray[relativeIndex]);
            if (sameCollision == 0)
            {
                newRecordSet->collisions++;
                newRecordSet->collisionIndex = realloc (newRecordSet->collisionIndex, sizeof (int) * newRecordSet->collisions);
                if (newRecordSet->collisionIndex == NULL)
                {
                    PI_Abort (0, "realloc unsuccessful (int array)", __FILE__, __LINE__);
                }
                newRecordSet->collisionIndex[(newRecordSet->collisions)-1] = relativeIndex;
            }
        }

        relativeIndex++;
    }

    return (newRecordSet);
}

/****
offsetCalc: Calculates offset from file start to specific record number
Preconditions: recordNum > 0
Postconditions: Returns offset required to seek to record number desired
****/
int offsetCalc (int recordNum)
{
    return (H_LENGTH + ((recordNum - 1) * R_LENGTH));
}

/****
recordReader: Reads a record line from an NCDB file
Preconditions: Record line follows expected format
Postconditions: Returns Record struct with fields allocated
NOTE: Returns malloc'd Record, free after use
****/
Record *recordReader (FILE * recordFile)
{
    char bufferArray[ELEMENTS][MAX_WIDTH];
    int i, j;
    Record *newRecord;

    newRecord = malloc (sizeof (Record));
    if (newRecord == NULL)
    {
        PI_Abort (0, "malloc unsuccessful (Record)", __FILE__, __LINE__);
    }

    /*Read record line*/
    fscanf (recordFile, "%4s,%2s,%1s,%2s,%1s,%2s,%2s,%2s,%1s,%1s,%1s,%2s,%2s,%2s,%4s,%2s,%1s,%2s,%2s,%1s,%2s,%1s",
            bufferArray[0], bufferArray[1], bufferArray[2], bufferArray[3], bufferArray[4], bufferArray[5],
            bufferArray[6], bufferArray[7], bufferArray[8], bufferArray[9], bufferArray[10], bufferArray[11],
            bufferArray[12], bufferArray[13], bufferArray[14], bufferArray[15], bufferArray[16], bufferArray[17],
            bufferArray[18], bufferArray[19], bufferArray[20], bufferArray[21]);

    /*Store collision level data first, then vehicle level data, then person level data, from field string to int*/
    i = 0;

    for (j = 0; j < C_ELEMENTS; j++)
    {
        newRecord->collisionData[j] = fieldToInt (bufferArray[i]);
        i++;
    }
    for (j = 0; j < V_ELEMENTS; j++)
    {
        newRecord->vehicleData[j] = fieldToInt (bufferArray[i]);
        i++;
    }
    for (j = 0; j < P_ELEMENTS; j++)
    {
        newRecord->personData[j] = fieldToInt (bufferArray[i]);
        i++;
    }

    return (newRecord);
}

/****
fieldToInt: Converts field string to integer
Preconditions: Field has been read from NCDB file
Postconditions: Returns int value of NCDB field, including special codes for specific cases
****/
int fieldToInt (char *field)
{
    if (strcmp (field, "N") == 0 || strcmp (field, "NN") == 0 || strcmp (field, "NNNN") == 0)
    {
        return (DUMMY);
    }
    else if (strcmp (field, "Q") == 0 || strcmp (field, "QQ") == 0 || strcmp (field, "QQQQ") == 0)
    {
        return (OTHER);
    }
    else if (strcmp (field, "U") == 0 || strcmp (field, "UU") == 0 || strcmp (field, "UUUU") == 0 ||
             strcmp (field, "X") == 0 || strcmp (field, "XX") == 0 || strcmp (field, "XXXX") == 0)
    {
        return (NO_DATA);
    }
    else if (strcmp (field, "M") == 0)
    {
        return (MALE);
    }
    else if (strcmp (field, "F") == 0)
    {
        return (FEMALE);
    }
    else
    {
        /*If field does not match special cases, assume integer, convert and return*/
        return (atoi (field));
    }
}

/****
collisionChecker: Checks two records if they belong in the same collision or not
Preconditions: Both records are filled, recordOne implied to be earlier in file than recordTwo
Postconditions: Returns 1 if the records belong to the same collision; 0 otherwise
****/
int collisionChecker (Record *recordOne, Record *recordTwo)
{
    int i;

    /*First check if any collision level data are different*/
    for (i = 0; i < C_ELEMENTS; i++)
    {
        if (recordOne->collisionData[i] != recordTwo->collisionData[i])
        {
            return (0);
        }
    }

    /*Then check to see if V_ID changed from a value other than 1 -> 1*/
    if (recordOne->vehicleData[0] != 1 && recordTwo->vehicleData[0] == 1)
    {
        return (0);
    }

    /*Lastly, check if V_TYPE or V_YEAR changed*/
    if ((recordOne->vehicleData[0] == 1 && recordOne->vehicleData[0] == recordTwo->vehicleData[0]) &&
        (recordOne->vehicleData[1] != recordTwo->vehicleData[1] || recordOne->vehicleData[2] != recordTwo->vehicleData[2]))
    {
        return (0);
    }

    return (1);
}

/****
freeRecordSet: Frees RecordSet from heap memory 
Preconditions: toBeFreedRecords is not NULL and populated with Records
Postconditions: RecordSet is freed from memory
****/ 
void freeRecordSet (RecordSet * toBeFreedRecords)
{
    int i;

    if (toBeFreedRecords == NULL)
    {
        return;
    }

    for (i = 0; i < toBeFreedRecords->records; i++)
    { 
        free (toBeFreedRecords->recordArray[i]);
    }
    free (toBeFreedRecords->recordArray);
    free (toBeFreedRecords->collisionIndex);
    free (toBeFreedRecords);

    return;
}
