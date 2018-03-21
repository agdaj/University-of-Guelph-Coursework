/***************************************************
orbsMain.c
Name: Jireh Agda (0795472)
Date Created: 2016 11 11
     Last Modified: 2016 11 14
Red/Blue Algorithm Computation Parallelized (OpenMP)
- Main, outlines orbs program structure
***************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <omp.h>

#include "orbsInitialize.h"
#include "orbsAlgo.h"
#include "fileOutput.h"
#include "wallclock.h"

/*Definitions for malloc progress, in case of needing to free after error*/
#define TILE_ALLOC 3
#define BOARD_ALLOC 2
#define PARAM_ALLOC 1

void cleanupORBS (int allocProgress, RBS_Param *parameters, char **rbBoard, int **tileCoord, int totalTiles);

int main (int argc, char *argv[])
{
	int i, j, cThreshold;
	double wallTime;
	RBS_Param *parameters;
	char **rbBoard;
	int **tileCoord, totalTiles = 0;
	int iterations = 0, maxCDensity = 0;    //stats keeping variables

	/*Initialize RBS data*/
	parameters = rbsInitialize (argc, argv);
	if (parameters == NULL)		return (-1);

	rbBoard = boardInitialize (parameters);
	if (rbBoard == NULL)
	{
		cleanupORBS (PARAM_ALLOC, parameters, rbBoard, NULL, 0);
		return (-1);
	}

	tileCoord = calculateTileSets (parameters, &totalTiles);
	if (tileCoord == NULL)
	{
		cleanupORBS (BOARD_ALLOC, parameters, rbBoard, NULL, 0);
		return (-1);
	}

	cThreshold = parameters->tWidth * parameters->tWidth * parameters->cDensity / 100;
	omp_set_num_threads (parameters->processes);    //set threads to make with OpenMP usage
	
	printf ("Running Red/Blue Computation...\n");

	/*Start the simulation and timer*/
	StartTime ();
	mainWorkFunc (parameters->bWidth, parameters->tWidth, parameters->maxSteps, rbBoard, tileCoord, totalTiles, cThreshold, &iterations, &maxCDensity);
	wallTime = EndTime ();
	
	/*Print out final results to redblue.txt and stdout*/
	endPrinting (parameters, rbBoard, iterations, maxCDensity, wallTime);
	
	/*Clean up program*/
	cleanupORBS (TILE_ALLOC, parameters, rbBoard, tileCoord, totalTiles);

	return 0;
}

/****
cleanupORBS: Clean up and free heap resources allocated during initialization
Preconditions: allocProgress accurately represents state of allocated memory
Postconditions: Heap memory is freed
****/
void cleanupORBS (int allocProgress, RBS_Param *parameters, char **rbBoard, int **tileCoord, int totalTiles)
{
	int i;

	/*Free memory based on how much memory has been already allocated*/
	switch (allocProgress)
	{
		case TILE_ALLOC:
			for (i = 0; i < totalTiles; i++)
			{
				free (tileCoord[i]);
			}
			free (tileCoord);

		case BOARD_ALLOC:
			for (i = 0; i < parameters->bWidth; i++)
			{
				free (rbBoard[i]);
			}
			free (rbBoard);

		case PARAM_ALLOC:
			free (parameters);
	}

	return;
}