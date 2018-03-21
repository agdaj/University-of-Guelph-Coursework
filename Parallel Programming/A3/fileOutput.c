/***************************************************
fileOutput.c
Name: Jireh Agda (0795472)
Date Created: 2016 11 11
     Last Modified: 2016 11 13
Red/Blue Algorithm Computation Parallelized (OpenMP)
- Functions to print the board to stdout and file
***************************************************/

#include <stdio.h>

#include "fileOutput.h"
#include "orbsInitialize.h"


/****Red/Blue Printing Functions****/

/****
endPrinting: Print board to redblue.txt and statistics to file and stdout
Preconditions: Red/Blue board and data initialized
Postconditions: Board and data are printed to file and stdout
****/
void endPrinting (RBS_Param *parameters, char **rbBoard, int iterations, int maxCDensity, double timeTaken)
{
	FILE * fp;

	/*Print output on stdout*/
	printLastLine (stdout, parameters, iterations, maxCDensity, timeTaken);

	/*Print to redblue.txt*/
	fp = fopen ("redblue.txt", "w");
	if (fp != NULL)
	{
		printBoard (fp, parameters, rbBoard);
		printLastLine (fp, parameters, iterations, maxCDensity, timeTaken);
		fclose (fp);
	}

	return;
}

/****
printBoard: Output board to selected FILE stream
Preconditions: Red/Blue board initialized
Postconditions: Board is printed to selected stream
****/
void printBoard (FILE *stream, RBS_Param *parameters, char **rbBoard)
{
	char toPrint;
	int i, j;

	/*Print to stream, choosing correct symbol for a cell*/
	for (i = 0; i < parameters->bWidth; i++)
	{
		for (j = 0; j < parameters->bWidth; j++)
		{
			if (rbBoard[i][j] == RED)		toPrint = RED_CHAR;
			else if (rbBoard[i][j] == BLUE)	toPrint = BLUE_CHAR;
			else							toPrint = WHITE_CHAR;

			fprintf (stream, "%c", toPrint);
		}

		fprintf (stream, "\n");
	}

	return;
}

/****
printLastLine: Output statistics to selected FILE stream
Preconditions: Red/Blue statistics initialized
Postconditions: Statistics are printed to stream
****/
void printLastLine (FILE *stream, RBS_Param *parameters, int iterations, int maxCDensity, double wallTime)
{
	fprintf (stream, "p%d b%d t%d c%d m%d ", parameters->processes, parameters->bWidth, parameters->tWidth,
		parameters->cDensity, parameters->maxSteps);

	if (parameters->seed == true)
	{
		fprintf (stream, "s%d ", parameters->rSeed);
	}

	fprintf (stream, "Iterations: %d density %d Timer: %lf\n", iterations, maxCDensity, wallTime);

	return;
}