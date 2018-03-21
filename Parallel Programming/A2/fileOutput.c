/************************************************
fileOutput.c
Name: Jireh Agda (0795472)
Date Created: 2016 11 01
     Last Modified: 2016 11 08
Red/Blue Algorithm Computation Parallelized
- Functions to print the board to stdout and file
************************************************/

#include <stdio.h>

#include "fileOutput.h"
#include "rbsInitialize.h"
#include "putcolour.h"


/****Red/Blue Printing Functions****/

/****
endPrinting: Print board to redblue.txt and statistics to file and stdout
Preconditions: Red/Blue board and data initialized
Postconditions: Board and data are printed to file and stdout
****/
void endPrinting (double timeTaken)
{
	FILE * fp;

	/*Print output on stdout*/
	//printBoard (stdout);
	printLastLine (stdout, timeTaken, true);

	/*Print to redblue.txt*/
	fp = fopen ("redblue.txt", "w");
	if (fp != NULL)
	{
		printBoard (fp);
		printLastLine (fp, timeTaken, true);
		fclose (fp);
	}

	return;
}

/****
printBoard: Output board to selected FILE stream
Preconditions: Red/Blue board initialized
Postconditions: Board is printed to selected stream
****/
void printBoard (FILE *stream)
{
	char toPrint;
	int i, j;

	/*Set colour, then print to stream (only noticable on command line*/
	for (i = 0; i < parameters->bWidth; i++)
	{
		for (j = 0; j < parameters->bWidth; j++)
		{
			if (rbBoard[i][j] == RED)
			{
				toPrint = RED_CHAR;
				setcolour (PC_RED);
			}
			else if (rbBoard[i][j] == BLUE)
			{
				toPrint = BLUE_CHAR;
				setcolour (PC_BLUE);
			}
			else
			{
				toPrint = WHITE_CHAR;
				setcolour (PC_AUTO);
			}

			fprintf (stream, "%c", toPrint);
		}

		setcolour (PC_AUTO);
		fprintf (stream, "\n");
	}

	setcolour (PC_AUTO);    //reset colours to 'normal'

	return;
}

/****
printLastLine: Output statistics to selected FILE stream
Preconditions: Red/Blue statistics initialized
Postconditions: Statistics are printed to stream, including recorded time if isTime == true
****/
void printLastLine (FILE *stream, double wallTime, bool isTime)
{
	fprintf (stream, "p%d b%d t%d c%d m%d ", parameters->processes, parameters->bWidth, parameters->tWidth,
		parameters->cDensity, parameters->maxSteps);

	if (parameters->seed == true)
	{
		fprintf (stream, "s%d ", parameters->rSeed);
	}

	if (parameters->interactive == true)
	{
		fprintf (stream, "i ");
	}

	fprintf (stream, "Iterations: %d density %d", iterations, maxCDensity);
	if (isTime == true)    fprintf (stream, " Timer: %lf\n", wallTime);
	else                   fprintf (stream, "\n");

	return;
}