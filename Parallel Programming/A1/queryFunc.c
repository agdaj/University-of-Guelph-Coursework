/********************************************
queryFunc.c
Name: Jireh Agda (0795472)
Date Created: 2016 10 10
     Last Modified: 2016 10 12
NCDB Query Program Parallelized
Assignment 1 - CIS*3090
- Functions for helping with handling queries
  and printing query results
********************************************/

#include <stdio.h>
#include <stdlib.h>
#include "pilot.h"
#include "dataReadFunc.h"
#include "queryFunc.h"


/****Query Helper Functions****/

/****
queryOneHelper: Help calculate the worst month of the year for collisions and fatalities
Preconditions: RecordSet is populated with NCDB data
Postconditions: Modifies colDataSet (a 14x12x2 array) with collision and fatality counts per year per month
****/
void queryOneHelper (RecordSet *recordsToQuery, int colDataSet[14][12][2])
{
    int i, j, k;

    for (i = 0; i < 14; i++)
    {
        for (j = 0; j < 12; j++)
        {
            for (k = 0; k < 2; k++)
            {
                colDataSet[i][j][k] = 0;
            }
        }
    }

    /*Add collision counts to total if the month of the collision is provided*/
    for (i = 0; i < recordsToQuery->collisions; i++)
    {
        if (recordsToQuery->recordArray[recordsToQuery->collisionIndex[i]]->collisionData[1] != NO_DATA)
        {
            colDataSet[recordsToQuery->recordArray[recordsToQuery->collisionIndex[i]]->collisionData[0]-1999]
                      [recordsToQuery->recordArray[recordsToQuery->collisionIndex[i]]->collisionData[1]-1][0]++;
        }
    }

    /*If P_ISEV is 3 and C_MNTH is given (not NO_DATA), add to fatalities total*/
    for (i = 0; i < recordsToQuery->records; i++)
    {
        if (recordsToQuery->recordArray[i]->personData[4] == 3 &&
            recordsToQuery->recordArray[i]->collisionData[1] != NO_DATA)
        {
            colDataSet[recordsToQuery->recordArray[i]->collisionData[0]-1999][recordsToQuery->recordArray[i]->collisionData[1]-1][1]++;
        }
    }

   return;
}

/****
queryTwoHelper: Determines whether men or women are more likely to be killed in a collision
Preconditions: RecordSet is populated with NCDB data
Postconditions: Modifies menCount and womenCount to reflect fatality counts
****/
void queryTwoHelper (RecordSet *recordsToQuery, int *menCount, int *womenCount)
{
    int i;
    int menFatalities, womenFatalities;

    menFatalities = 0;
    womenFatalities = 0;

    /*Run through the records and count up men and women fatalities for each instance of P_ISEV == 3*/
    for (i = 0; i < recordsToQuery->records; i++)
    {
        if (recordsToQuery->recordArray[i]->personData[4] == 3)
        {
            if (recordsToQuery->recordArray[i]->personData[1] == MALE)
            {
                menFatalities++;
            }
            else if (recordsToQuery->recordArray[i]->personData[1] == FEMALE)
            {
                womenFatalities++;
            }
        }
    }

    *menCount = menFatalities;
    *womenCount = womenFatalities;

    return;
}

/****
queryThreeHelper: Help determine the largest collision in the NCDB
Preconditions: RecordSet is populated with NCDB data
Postconditions: Returns an int that packs the largest collision count and its date
****/
int queryThreeHelper (RecordSet *recordsToQuery)
{
    int i;
    int indexOfCollision;
    int dataPackOfCollision;

    indexOfCollision = 0;

    /*For each collision marked by collision index, assume C_VEHS is the same among the record group*/
    /*Compare C_VEHS of each collision's starting record to retrieve max*/
    for (i = 0; i < recordsToQuery->collisions; i++)
    {
        if (recordsToQuery->recordArray[recordsToQuery->collisionIndex[i]]->collisionData[5] >
            recordsToQuery->recordArray[recordsToQuery->collisionIndex[indexOfCollision]]->collisionData[5])
        {
            indexOfCollision = i;
        }
    }

    /*Producing the int return value where the structure is VVYYYYMMD*/
    dataPackOfCollision = recordsToQuery->recordArray[recordsToQuery->collisionIndex[indexOfCollision]]->collisionData[5];    //VV
    dataPackOfCollision *= 10000;                                                                                             //VV0000
    dataPackOfCollision += recordsToQuery->recordArray[recordsToQuery->collisionIndex[indexOfCollision]]->collisionData[0];   //VVYYYY
    dataPackOfCollision *= 100;                                                                                               //VVYYYY00
    dataPackOfCollision += recordsToQuery->recordArray[recordsToQuery->collisionIndex[indexOfCollision]]->collisionData[1];   //VVYYYYMM
    dataPackOfCollision *= 10;                                                                                                //VVYYYYMM0
    dataPackOfCollision += recordsToQuery->recordArray[recordsToQuery->collisionIndex[indexOfCollision]]->collisionData[2];   //VVYYYYMMD

    return (dataPackOfCollision);
}

/****
queryFourHelper: Help determine number of new cars and the average age of cars in collisions
Preconditions: RecordSet is populated with NCDB data
Postconditions: Modifies numOfNewCars, totalCars and sumVehicleAge to reflect vehicle statistics
****/
void queryFourHelper (RecordSet *recordsToQuery, int *numOfNewCars, int *totalCars, int *sumVehicleAge)
{
    int i, j;
    int newCarCount = 0, totalCarCount = 0, ageSum = 0;

    /*Check all collision's vehicle data except for the last collision, noting if vehicle has a V_YEAR or not*/
    for (i = 0; i < recordsToQuery->collisions - 1; i++)
    {
        for (j = recordsToQuery->collisionIndex[i]; j < recordsToQuery->collisionIndex[i+1]; j++)
        {
            /*Only read from records that is the last recorded data of its V_ID (before V_ID changes or collision changes)*/
            if ((j + 1) == recordsToQuery->collisionIndex[i+1] ||
                recordsToQuery->recordArray[j]->vehicleData[0] != recordsToQuery->recordArray[j+1]->vehicleData[0])
            {
                /*Only record vehicle counts with recorded vehicle years (no NO_DATA, DUMMY)*/
                if (recordsToQuery->recordArray[j]->vehicleData[2] > 0)
                {
                    totalCarCount++;

                    if (recordsToQuery->recordArray[j]->vehicleData[2] >= recordsToQuery->recordArray[j]->collisionData[0])
                    {
                        newCarCount++;
                    }

                    //C_YEAR - V_YEAR + 1
                    ageSum += (recordsToQuery->recordArray[j]->collisionData[0] - recordsToQuery->recordArray[j]->vehicleData[2] + 1);
                }
            }
        }
    }

    /*Checking for the last collision's vehicle data*/
    for (j = recordsToQuery->collisionIndex[i]; j < recordsToQuery->records; j++)
    {
        /*Only read from records that is the last recorded data of its V_ID (before V_ID changes or is the last record)*/
        if ((j + 1) == recordsToQuery->records ||
            recordsToQuery->recordArray[j]->vehicleData[0] != recordsToQuery->recordArray[j+1]->vehicleData[0])
        {
            /*Only record vehicle counts with recorded vehicle years (no NO_DATA, DUMMY)*/
            if (recordsToQuery->recordArray[j]->vehicleData[2] > 0)
            {
                totalCarCount++;

                if (recordsToQuery->recordArray[j]->vehicleData[2] >= recordsToQuery->recordArray[j]->collisionData[0])
                {
                    newCarCount++;
                }

                //C_YEAR - V_YEAR + 1
                ageSum += (recordsToQuery->recordArray[j]->collisionData[0] - recordsToQuery->recordArray[j]->vehicleData[2] + 1);
            }
        }
    }

    *numOfNewCars = newCarCount;
    *totalCars = totalCarCount;
    *sumVehicleAge = ageSum;

    return;
}

/****
queryFiveHelper: Help determine most likely place of collision
Preconditions: RecordSet is populated with NCDB data, collisionArray is a 13-element int array
Postconditions: Modifies collisionArray (13-element array) with each location's collision count
****/
void queryFiveHelper (RecordSet *recordsToQuery, int *collisionArray)
{
    int i;

    /*Initialize location counts to 0*/
    for (i = 0; i < 13; i++)
    {
        collisionArray[i] = 0;
    }

    /*Increments every instance of a location of a collision, where the 0 index is other (QQ), and all others map 1-1 onto the index*/
    for (i = 0; i < recordsToQuery->collisions; i++)
    {
        if (recordsToQuery->recordArray[recordsToQuery->collisionIndex[i]]->collisionData[7] == OTHER)
        {
            collisionArray[0]++;
        }
        else if (recordsToQuery->recordArray[recordsToQuery->collisionIndex[i]]->collisionData[7] != NO_DATA)
        {
            collisionArray[recordsToQuery->recordArray[recordsToQuery->collisionIndex[i]]->collisionData[7]]++;
        }
    }

    return;
}


/****Query Printer Functions****/

/****
queryOnePrinter: Calculates and prints query one results
Preconditions: totalColFatal is a 14x12x2 int array returned from queryOneHelper
Postconditions: Prints query one result
****/
void queryOnePrinter (int totalColFatal[14][12][2])
{
    int j, k;
    int worstMonthCol, worstMonthFatal;
    int worstMonths[12][2];

    /*Search the results for the worst months for collisions, fatalities for each year*/
    for (j = 0; j < 14; j++)
    {
        worstMonthCol = 0;
        worstMonthFatal = 0;

        for (k = 0; k < 12; k++)
        {
            if (totalColFatal[j][k][0] > totalColFatal[j][worstMonthCol][0])
            {
                worstMonthCol = k;
            }

            if (totalColFatal[j][k][1] > totalColFatal[j][worstMonthFatal][1])
            {
                worstMonthFatal = k;
            }
        }

        printf ("$Q1,%d,%d,%d\n", 1999+j, worstMonthCol+1, worstMonthFatal+1);
    }

    /*Each month has their counts added for each year into a separate data set, where
      the absolute worst months for collisions, fatalities are determined*/
    for (j = 0; j < 12; j++)
    {
        worstMonths[j][0] = 0;
        worstMonths[j][1] = 0;

        for (k = 0; k < 14; k++)
        {
            worstMonths[j][0] += totalColFatal[k][j][0];
            worstMonths[j][1] += totalColFatal[k][j][1];
        }
    }

    worstMonthCol = 0;
    worstMonthFatal = 0;
    for (j = 0; j < 12; j++)
    {
        if (worstMonths[j][0] > worstMonths[worstMonthCol][0])
        {
            worstMonthCol = j;
        }

        if (worstMonths[j][1] > worstMonths[worstMonthFatal][1])
        {
            worstMonthFatal = j;
        }
    }
    printf ("$Q1,9999,%d,%d\n", worstMonthCol+1, worstMonthFatal+1);

    return;
}

/****
queryTwoPrinter: Calculates and prints query two results
Preconditions: Fatality counts returned from queryTwoHelper
Postconditions: Prints query two result
****/
void queryTwoPrinter (int totalFatalM, int totalFatalF)
{
    float probMFatal, probFFatal;

    /*Calculate probabilies of men vs. women fatalities*/
    probMFatal = (float) totalFatalM / (float) (totalFatalM + totalFatalF);
    probFFatal = (float) totalFatalF / (float) (totalFatalM + totalFatalF);
    printf ("$Q2,%d,%d,%.2f,%.2f\n", totalFatalM, totalFatalF, probMFatal, probFFatal);

    return;
}

/****
queryThreePrinter: Extracts data and prints query three results
Preconditions: colDataPack is a return from queryThreeHelper
Postconditions: Prints query three result
****/
void queryThreePrinter (int colDataPack)
{
    int maxDay, maxMonth, maxYear, maxVehicles;

    /*Extract data from data pack of largest collision*/
    maxDay = colDataPack % 10; colDataPack /= 10;
    maxMonth = colDataPack % 100; colDataPack /= 100;
    maxYear = colDataPack % 10000; colDataPack /= 10000;
    maxVehicles = colDataPack % 100;
    printf ("$Q3,%d,%d,%d,%d\n", maxVehicles, maxYear, maxMonth, maxDay);

    return;
}

/****
queryFourPrinter: Calculates and prints query four results
Preconditions: Total counts returned from queryFourHelper
Postconditions: Prints query four result
****/
void queryFourPrinter (int totalNewCars, int totalCars, int totalAgeSum)
{
    int newCarColAvgI;
    float avgAgeCol;

    /*Calculate averages of new car and age in collisions*/
    newCarColAvgI = totalNewCars / 14;
    avgAgeCol = (float) totalAgeSum / (float) totalCars;
    printf ("$Q4,%d,%.1f\n", newCarColAvgI, avgAgeCol);

    return;
}

/****
queryFivePrinter: Calculates and prints query five results
Preconditions: locationCounts is a 13-element int array returned from queryFiveHelper
Postconditions: Prints query five result
****/
void queryFivePrinter (int *locationCounts)
{
    int j;
    int largestCount;

    /*Find largest count by traversing through array*/
    largestCount = 0;
    for (j = 0; j < 13; j++)
    {
        if (locationCounts[j] > locationCounts[largestCount])
        {
            largestCount = j;
        }
    }

    printf ("$Q5,%d,", largestCount);
    for (j = 1; j < 13; j++)
    {
        printf ("%d,", locationCounts[j]);
    }
    printf ("%d\n", locationCounts[0]);

    return;
}
