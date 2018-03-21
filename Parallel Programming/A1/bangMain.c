/*******************************************
bangMain.c
Name: Jireh Agda (0795472)
Date Created: 2016 09 23
     Last Modified: 2016 10 11
NCDB Query Program Parallelized
Assignment 1 - CIS*3090
- Main, outlines program and Pilot structure
+ Contains timing reports
*******************************************/

#include <stdio.h>
#include <stdlib.h>
#include "pilot.h"
#include "bangMain.h"
#include "dataReadFunc.h"
#include "queryFunc.h"

PI_PROCESS **workers;           //Will be dynamically allocated to # of processes available
PI_CHANNEL **toWorkers;         //Channels going to...
PI_CHANNEL **fromWorkers;       //and from dynamic number of processes
PI_BUNDLE *bcBundle;            //Bundle for broadcasting data to processes
PI_BUNDLE *rBundle;             //Bundle for reducing data from processes


/****Bang Main Functions****/

/****
workerFunction: Outlines the work a worker does in the program "bang" when parallel
Preconditions: None
Postconditions: Worker reads and stores NCDB data and performs queries on their data as required
****/
int workerFunction (int id, void *args)
{
    FILE *recordFile;
    RecordSet *myRecords;

    int *recordIndices;
    int workerNum;

    int startIndex, endIndex;

    //Query Variables
    int dataSetMsg[14][12][2];
    int query, queryEnd;
    int fatalitiesM, fatalitiesF;
    int dataPackQ3;
    int totalNewCars, totalCars, totalAgeSum;
    int locationArray[13];

    /*Phase 1: Reading and storing NCDB data*/
    PI_Read (toWorkers[id], "%^d", &workerNum, &recordIndices);

    startIndex = recordIndices[id];
    endIndex = recordIndices[id+1];

    /*Open the NCDB file and read in Records*/
    recordFile = openFile ((char *)args);
    myRecords = createRecordSet (recordFile, startIndex, endIndex);

    /*Return the results (#records, #collisions) to PI_MAIN to mark end of Phase 1*/
    PI_Write (fromWorkers[id], "%+/d %+/d", myRecords->records, myRecords->collisions);

    /*End of Phase 1 (cleanup)*/
    free (recordIndices);
    closeFile (recordFile);

    /*Phase 2: Query computation*/
    /*Run queries until special query code 0 is read, setting queryEnd to 1 to exit loop*/
    queryEnd = 0;

    do
    {
        PI_Read (toWorkers[id], "%d", &query);

        /*Call helper function to accomplish brunt work and send computated data to PI_MAIN*/
        switch (query)
        {
            case 1:
                queryOneHelper (myRecords, dataSetMsg);
                PI_Write (fromWorkers[id], "%+/*d", 14*12*2, dataSetMsg);
                break;

            case 2:
                queryTwoHelper (myRecords, &fatalitiesM, &fatalitiesF);
                PI_Write (fromWorkers[id], "%+/d %+/d", fatalitiesM, fatalitiesF);
                break;

            case 3:
                dataPackQ3 = queryThreeHelper (myRecords);
                PI_Write (fromWorkers[id], "%max/d", dataPackQ3);
                break;

            case 4:
                queryFourHelper (myRecords, &totalNewCars, &totalCars, &totalAgeSum);
                PI_Write (fromWorkers[id], "%+/d %+/d %+/d", totalNewCars, totalCars, totalAgeSum);
                break;

            case 5:
                queryFiveHelper (myRecords, locationArray);
                PI_Write (fromWorkers[id], "%+/13d", locationArray);
                break;

            case 0:
                queryEnd = 1;
                break;

            default:
                /*Ignore invalid queries*/
                queryEnd = 0;
        }
    }
    while (queryEnd == 0);

    /*Cleanup malloc RecordSet*/
    freeRecordSet (myRecords);

    return (0);
}

/****
timeUpdate: Marks the time elapsed from start and prints timing reports for program segments
Preconditions: timeArray has sufficient room to store timesStored amount of times
Postconditions: Timing report is printed and stored within timeArray
****/
int timeUpdate (int timeID, int cores, int timesStored, double *timeArray)
{
    double timeMark, timePost;

    timeMark = PI_EndTime ();

    switch (timeID)
    {
        case 0:
            timeArray[0] = timeMark;
            timePost = timeMark;
            break;

        case 1: case 2: case 3: case 4: case 5:
            timeArray[timesStored] = timeMark;
            timePost = timeMark - timeArray[timesStored-1];
            break;

        case 9:
            timeArray[timesStored] = timeMark;
            timePost = timeMark - timeArray[0];
    }

    printf ("$T%d,%d,%.1lf\n", timeID, cores, timePost);

    return (timesStored+1);
}

int main (int argc, char *argv[])
{
    int i;
    int fileSize;
    int *indexArray;
    int numRecords, numTotals, numTotalsTwo;
    int recordsPerWorker;
    FILE *recordFile;
    RecordSet *myRecords;
    int startIndex, endIndex;

    int sameCollision;
    Record *dummyRecordOne, *dummyRecordTwo;    //Dummy pointers to help determine collision boundaries

    int queryNum;
    char *validQuery;    //Check if query argument is valid with strtol

    //Query Variables
    int accidentDataSet[14][12][2];
    int totalFatalitiesM, totalFatalitiesF;
    int maxColDataPack;
    int totalNewCars, totalCars, totalAgeSum;
    int locationCounts[13];

    //Timing reports variables
    int timesRecorded = 0;
    double timesArray[argc];

    /*Calculate # of available workers (-1 for PI_MAIN)*/
    int numCores = PI_Configure (&argc, &argv);
    int numWorkers = numCores - 1;

    /*Start the clock, right after PI_Configure*/
    PI_StartTime ();

    /*Configure processes, channels and bundles between PI_MAIN (master) and workers if not serial*/
    if (numWorkers > 0)
    {
        workers = malloc (sizeof (PI_PROCESS *) * numWorkers);
        toWorkers = malloc (sizeof (PI_CHANNEL *) * numWorkers);
        fromWorkers = malloc (sizeof (PI_CHANNEL *) * numWorkers);
        if (workers == NULL || toWorkers == NULL || fromWorkers == NULL)
        {
            PI_Abort (0, "malloc unsuccessful (PI_PROCESS/PI_CHANNEL)", __FILE__, __LINE__);
        }

        for (i = 0; i < numWorkers; i++)
        {
            workers[i] = PI_CreateProcess (workerFunction, i, argv[1]);
            toWorkers[i] = PI_CreateChannel (PI_MAIN, workers[i]);
            fromWorkers[i] = PI_CreateChannel (workers[i], PI_MAIN);
        }

        bcBundle = PI_CreateBundle (PI_BROADCAST, toWorkers, numWorkers);
        rBundle = PI_CreateBundle (PI_REDUCE, fromWorkers, numWorkers);
    }

    /*Start alternate processes here if any*/
    PI_StartAll ();

    /*Phase 1: Reading and Storing NCDB Data*/
    /*Structure is different is serial mode or parallel mode*/
    recordFile = openFile (argv[1]);
    fseek (recordFile, 0, SEEK_END);
    fileSize = ftell (recordFile);
    numRecords = (fileSize - H_LENGTH) / R_LENGTH;

    if (numWorkers > 0)
    {
        /*If parallel mode: distribute work among workers*/
        printf ("Parallel Mode:\n");
        recordsPerWorker = numRecords / numWorkers;

        indexArray = malloc (sizeof (int) * (numWorkers + 1));    //+1 to add end index
        if (indexArray == NULL)
        {
            PI_Abort (0, "malloc unsuccessful (int array)", __FILE__, __LINE__);
        }
        indexArray[0] = 1;

        /*Start assigning record indices to processes via an array for Phase 1*/
        for (i = 1; i < numWorkers; i++)
        {
            indexArray[i] = indexArray[i-1] + recordsPerWorker;
            fseek (recordFile, offsetCalc (indexArray[i]), SEEK_SET);

            /*Check records at/ahead of index until collision barrier is reached*/
            dummyRecordOne = recordReader (recordFile);
            dummyRecordTwo = recordReader (recordFile);
            sameCollision = collisionChecker (dummyRecordOne, dummyRecordTwo);
            while (sameCollision == 1)
            {
                indexArray[i]++;

                free (dummyRecordOne);
                dummyRecordOne = dummyRecordTwo;
                dummyRecordTwo = recordReader (recordFile);
                sameCollision = collisionChecker (dummyRecordOne, dummyRecordTwo);
            }
            indexArray[i]++;

            free (dummyRecordOne);
            free (dummyRecordTwo);
        }
        indexArray[i] = numRecords + 1;

        /*Phase 1: Let workers read NCDB files using broadcasted index numbers*/
        PI_Broadcast (bcBundle, "%^d", numWorkers + 1, indexArray);

        free (indexArray);

        /*Reduce results from workers and double check with expected number, printing results*/
        PI_Reduce (rBundle, "%+/d %+/d", &numTotals, &numTotalsTwo);
    }
    else
    {
        /*If serial mode: PI_MAIN reads and stores entire file*/
        printf ("Serial Mode:\n");
        startIndex = 1;
        endIndex = numRecords + 1;
        myRecords = createRecordSet (recordFile, startIndex, endIndex);

        numTotals = myRecords->records;
        numTotalsTwo = myRecords->collisions;
    }

    /*Double check record numbers, Record time for Phase 1*/
    if (numTotals != numRecords)
    {
        PI_Abort (0, "Mismatch returned record total to expected record total", __FILE__, __LINE__);
    }
    printf ("%d records read, %d collisions read\n", numTotals, numTotalsTwo);
    timesRecorded = timeUpdate (0, numCores, timesRecorded, timesArray);

    /*Finish Phase 1 with cleanup*/
    closeFile (recordFile);

    /*Phase 2: Run NCDB Queries*/
    for (i = 2; i < argc; i++)
    {
        validQuery = NULL;
        queryNum = strtol (argv[i], &validQuery, 10);
        if (argv[i][0] != '\0' && validQuery[0] == '\0')    //integer validity check
        {
            /*If parallel mode, queries are broadcasted to workers, and PI_MAIN waits*/
            if (numWorkers > 0)
            {
                PI_Broadcast (bcBundle, "%d", queryNum);
            }

            /*If parallel mode: Gather query data from workers and send to printer functions to print*/
            /*If serial mode: Execute query and send reqults to printer functions*/
            /*Then record and print time*/
            switch (queryNum)
            {
                case 1:
                    if (numWorkers > 0)
                    {
                        PI_Reduce (rBundle, "%+/*d", 14*12*2, &accidentDataSet);
                    }
                    else
                    {
                        queryOneHelper (myRecords, accidentDataSet);
                    }
                    queryOnePrinter (accidentDataSet);
                    timesRecorded = timeUpdate (queryNum, numCores, timesRecorded, timesArray);
                    break;

                case 2:
                    if (numWorkers > 0)
                    {
                        PI_Reduce (rBundle, "%+/d %+/d", &totalFatalitiesM, &totalFatalitiesF);
                    }
                    else
                    {
                        queryTwoHelper (myRecords, &totalFatalitiesM, &totalFatalitiesF);
                    }
                    queryTwoPrinter (totalFatalitiesM, totalFatalitiesF);
                    timesRecorded = timeUpdate (queryNum, numCores, timesRecorded, timesArray);
                    break;

                case 3:
                    if (numWorkers > 0)
                    {
                        PI_Reduce (rBundle, "%max/d", &maxColDataPack);
                    }
                    else
                    {
                        maxColDataPack = queryThreeHelper (myRecords);
                    }
                    queryThreePrinter (maxColDataPack);
                    timesRecorded = timeUpdate (queryNum, numCores, timesRecorded, timesArray);
                    break;

                case 4:
                    if (numWorkers > 0)
                    {
                        PI_Reduce (rBundle, "%+/d %+/d %+/d", &totalNewCars, &totalCars, &totalAgeSum);
                    }
                    else
                    {
                        queryFourHelper (myRecords, &totalNewCars, &totalCars, &totalAgeSum);
                    }
                    queryFourPrinter (totalNewCars, totalCars, totalAgeSum);
                    timesRecorded = timeUpdate (queryNum, numCores, timesRecorded, timesArray);
                    break;

                case 5:
                    if (numWorkers > 0)
                    {
                        PI_Reduce (rBundle, "%+/13d", &locationCounts);
                    }
                    else
                    {
                        queryFiveHelper (myRecords, locationCounts);
                    }
                    queryFivePrinter (locationCounts);
                    timesRecorded = timeUpdate (queryNum, numCores, timesRecorded, timesArray);
                    break;

                default:
                    queryNum = 0;    //Ignore all other cases
            }
        }
    }

    /*Cleanup before exiting*/
    if (numWorkers > 0)
    {
        /*Finish up by sending special query code 0 as end for workers*/
        queryNum = 0;
        PI_Broadcast (bcBundle, "%d", queryNum);

        /*Free channel, process pointer array data*/
        free (workers);
        free (toWorkers);
        free (fromWorkers);
    }
    else
    {
        /*Free RecordSet*/
        freeRecordSet (myRecords);
    }

    /*Finish up main*/
    timesRecorded = timeUpdate (9, numCores, timesRecorded, timesArray);

    PI_StopMain (0);
    return (0);
}
