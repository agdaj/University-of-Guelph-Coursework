/***********************************************
dataReadFunc.h
Name: Jireh Agda (0795472)
Date Created: 2016 10 10
     Last Modified: 2016 10 12
NCDB Query Program Parallelized
Assignment 1 - CIS*3090
- Data Reading and Storing Functions Header File
***********************************************/

/*This is the header file for bang's functions that read and store NCDB data from a csv file, in a separate module*/

#ifndef dataReadFunc_h
#define dataReadFunc_h

#include <stdio.h>      //Defines FILE type

/*Define NCDB file structure and data store structure*/
#define H_LENGTH 147    //Header line length = 145 bytes + 2 EOF bytes
#define R_LENGTH 63     //Record line length = 61 bytes + 2 EOF bytes

#define C_ELEMENTS 12   //Number of elements that are of the collision level
#define V_ELEMENTS 3    //Number of elements that are of the vehicle level
#define P_ELEMENTS 7    //Number of elements that are of the person level

#define ELEMENTS C_ELEMENTS+V_ELEMENTS+P_ELEMENTS //Total number of elements
#define MAX_WIDTH 5     //Max field width (with null) for string -> int conversion

#define MALE 1          //Define integer equivalents for special character cases
#define FEMALE 2
#define DUMMY -1
#define OTHER -2
#define NO_DATA -3

struct Record
{
    int collisionData[C_ELEMENTS];
    int vehicleData[V_ELEMENTS];
    int personData[P_ELEMENTS];
};
typedef struct Record Record;

struct RecordSet
{
    int records;
    Record **recordArray;

    int collisions;
    int *collisionIndex;
};
typedef struct RecordSet RecordSet;


/****File Opening and Closing Functions****/

/****
openFile: Opens NCDB file for reading
Preconditions: Path points to valid file
Postconditions: Returns FILE *pointer to NCDB file
****/
FILE *openFile (char * path);

/****
closeFile: Closes NCDB file
Preconditions: recordFile points to open NCDB file
Postconditions: NCDB file is closed if open
****/
void closeFile (FILE *recordFile);


/****Data Reading and Storing Functions****/

/****
createRecordSet: Creates RecordSet for worker/PI_MAIN that contains the process' NCDB data set
Preconditions: recordFile is open, startRecord and endRecord point to existing records in FILE, or endRecord+1 for marking the end of file
Postconditions: Returns RecordSet struct populated with Records of NCDB data
NOTE: Returns malloc'd RecordSet, free using freeRecordSet
****/
RecordSet *createRecordSet (FILE *recordFile, int startRecord, int endRecord);

/****
offsetCalc: Calculates offset from file start to specific record number
Preconditions: recordNum > 0
Postconditions: Returns offset required to seek to record number desired
****/
int offsetCalc (int recordNum);

/****
recordReader: Reads a record line from an NCDB file
Preconditions: Record line follows expected format
Postconditions: Returns Record struct with fields allocated
NOTE: Returns malloc'd Record, free after use
****/
Record *recordReader (FILE * recordFile);

/****
fieldToInt: Converts field string to integer
Preconditions: Field has been read from NCDB file
Postconditions: Returns int value of NCDB field, including special codes for specific cases
****/
int fieldToInt (char *field);

/****
collisionChecker: Checks two records if they belong in the same collision or not
Preconditions: Both records are filled, recordOne implied to be earlier in file than recordTwo
Postconditions: Returns 1 (true) if the records belong to the same collision; 0 otherwise (false)
****/
int collisionChecker (Record *recordOne, Record *recordTwo);

/****
freeRecordSet: Frees RecordSet from memory
Preconditions: toBeFreedRecords is not NULL and populated with Records
Postconditions: RecordSet is freed from memory
****/
void freeRecordSet (RecordSet * toBeFreedRecords);


#endif
