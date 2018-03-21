/*****************************************************
fileOutput.h
Name: Jireh Agda (0795472)
Date Created: 2016 11 11
     Last Modified: 2016 11 13
Red/Blue Algorithm Computation Parallelized
- Header File for Printing the Board and Args (OpenMP)
  to File and to Stdout
*****************************************************/

/*This is the header file for orbs's functions that prints the final results of the
  Red/Blue Algorithm to a file and to stdout*/

#ifndef fileOutput_h
#define fileOutput_h

#include <stdio.h>			  //Contains FILE definition
#include "orbsInitialize.h"    //Contains bool definition

/*Define character outputs per board element type*/
#define WHITE_CHAR ' '
#define RED_CHAR '>'
#define BLUE_CHAR 'V'


/****Red/Blue Printing Functions****/

/****
endPrinting: Print board to redblue.txt and statistics to file and stdout
Preconditions: Red/Blue board and data initialized
Postconditions: Board and data are printed to file and stdout
****/
void endPrinting (RBS_Param *parameters, char **rbBoard, int iterations, int maxCDensity, double timeTaken);

/****
printBoard: Output board to selected FILE stream
Preconditions: Red/Blue board initialized
Postconditions: Board is printed to selected stream
****/
void printBoard (FILE *stream, RBS_Param *parameters, char **rbBoard);

/****
printLastLine: Output statistics to selected FILE stream
Preconditions: Red/Blue statistics initialized
Postconditions: Statistics are printed to stream
****/
void printLastLine (FILE *stream, RBS_Param *parameters, int iterations, int maxCDensity, double wallTime);

#endif