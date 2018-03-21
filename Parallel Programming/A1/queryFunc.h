/******************************
queryFunc.h
Name: Jireh Agda (0795472)
Date Created: 2016 10 10
     Last Modified: 2016 10 12
NCDB Query Program Parallelized
Assignment 1 - CIS*3090
- Query Functions Header File
******************************/

/*This is the header file for bang's query functions to process query requests, in a separate module*/

#ifndef queryFunc_h
#define queryFunc_h

#include "dataReadFunc.h"    //Defines Record, RecordSet structs


/****Query Helper Functions****/

/****
queryOneHelper: Help calculate the worst month of the year for collisions and fatalities
Preconditions: RecordSet is populated with NCDB data
Postconditions: Modifies colDataSet (a 14x12x2 array) with collision and fatality counts per year per month
****/
void queryOneHelper (RecordSet *recordsToQuery, int colDataSet[14][12][2]);

/****
queryTwoHelper: Help determine whether men or women are more likely to be killed in a collision
Preconditions: RecordSet is populated with NCDB data
Postconditions: Modifies menCount and womenCount to reflect fatality counts
****/
void queryTwoHelper (RecordSet *recordsToQuery, int *menCount, int *womenCount);

/****
queryThreeHelper: Help determine the largest collision in the NCDB 
Preconditions: RecordSet is populated with NCDB data
Postconditions: Returns an int that packs the largest collision count and its date
****/
int queryThreeHelper (RecordSet *recordsToQuery);

/****
queryFourHelper: Help determine number of new cars and the average age of cars in collisions
Preconditions: RecordSet is populated with NCDB data
Postconditions: Modifies numOfNewCars, totalCars and sumVehicleAge to reflect vehicle statistics
****/
void queryFourHelper (RecordSet *recordsToQuery, int *numOfNewCars, int *totalCars, int *sumVehicleAge);

/****
queryFiveHelper: Help determine most likely place of collision
Preconditions: RecordSet is populated with NCDB data, collisionArray is a 13-element int array
Postconditions: Modifies collisionArray (13-element array) with each location's collision count
****/
void queryFiveHelper (RecordSet *recordsToQuery, int *collisionArray);


/****Query Printer Functions****/

/****
queryOnePrinter: Calculates and prints query one results
Preconditions: totalColFatal is a 14x12x2 int array returned from queryOneHelper
Postconditions: Prints query one result
****/
void queryOnePrinter (int totalColFatal[14][12][2]);

/****
queryTwoPrinter: Calculates and prints query two results
Preconditions: Fatality counts returned from queryTwoHelper
Postconditions: Prints query two result
****/
void queryTwoPrinter (int totalFatalM, int totalFatalF);

/****
queryThreePrinter: Extracts data and prints query three results
Preconditions: colDataPack is a return from queryThreeHelper
Postconditions: Prints query three result
****/
void queryThreePrinter (int colDataPack);

/****
queryFourPrinter: Calculates and prints query four results
Preconditions: Total counts returned from queryFourHelper
Postconditions: Prints query four result
****/
void queryFourPrinter (int totalNewCars, int totalCars, int totalAgeSum);

/****
queryFivePrinter: Calculates and prints query five results
Preconditions: locationCounts is a 13-element int array returned from queryFiveHelper
Postconditions: Prints query five result
****/
void queryFivePrinter (int *locationCounts);


#endif
