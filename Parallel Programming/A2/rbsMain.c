/******************************************
rbsMain.c
Name: Jireh Agda (0795472)
Date Created: 2016 10 30
     Last Modified: 2016 11 08
Red/Blue Algorithm Computation Parallelized
- Main, outlines rbs program structure
******************************************/

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <limits.h>

#include "rbsInitialize.h"
#include "rbsInteractive.h"
#include "rbsAlgo.h"
#include "fileOutput.h"
#include "wallclock.h"

#define MAXTHREADS 16		// for MacProc core i7

/*Definitions for malloc progress, in case of needing to free after error*/
#define BLOCKS_ALLOC 4
#define PARTS_ALLOC 3
#define BOARD_ALLOC 2
#define PARAM_ALLOC 1

void cleanupRBS (int allocProgress);

/*Global data to be shared among threads*/
RBS_Param *parameters;
short int **rbBoard;
int *dataBounds;
int **blockCoord;
int currentBlock, totalBlocks, cThreshold;
bool contAlgo = true;

/*Global stats records*/
int iterations = 0;
int maxCDensity = 0;

/*Sync methods for threads*/
pthread_barrier_t barrier;
pthread_mutex_t updateStats;

int main (int argc, char *argv[])
{
	pthread_t tid[MAXTHREADS];
	pthread_attr_t attr;
	int i, j;
	double wallTime;

	/*Initialize global data*/
	rbsInitialize (argc, argv);
	if (parameters == NULL)		return (-1);
	if (boardInitialize () == false)
	{
		cleanupRBS (PARAM_ALLOC);
		return (-1);
	}
	if (divideDataSets () == false)
	{
		cleanupRBS (BOARD_ALLOC);
		return (-1);
	}
	if (calculateBlockSets () == false)
	{
		cleanupRBS (PARTS_ALLOC);
		return (-1);
	}

	/*Test print for checking argument values stored
	printf ("%d %d %d %d %d %d %d\n", parameters->processes, parameters->bWidth, parameters->tWidth,
		parameters->cDensity, parameters->maxSteps, parameters->rSeed, parameters->interactive);*/
	
	/*Initialize pthread elements*/
	pthread_barrier_init (&barrier, NULL, parameters->processes);
	
	pthread_attr_init (&attr);
	pthread_attr_setscope (&attr, PTHREAD_SCOPE_SYSTEM);

	pthread_mutex_init (&updateStats, NULL);

	/*Run interactive mode, with no use of pthread elements*/
	if (parameters->interactive == true)
	{
		interactiveRBS ();
	}
	else
	{
		/*Start the simulation and timer*/
		StartTime ();
		for (i = 0; i < parameters->processes; i++)
		{
			pthread_create (&tid[i], &attr, mainWorkFunc, (void*) i);
		}

		for (i = 0; i < parameters->processes; i++)
		{
			pthread_join (tid[i], NULL);
		}
		wallTime = EndTime ();
	
		/*Print out final results to redblue.txt and stdout*/
		endPrinting (wallTime);
	}

	/*Clean up program*/
	pthread_attr_destroy (&attr);
	pthread_mutex_destroy (&updateStats);
	pthread_barrier_destroy (&barrier);
	cleanupRBS (BLOCKS_ALLOC);

	return 0;
}

/****
cleanupRBS: Clean up and free heap resources allocated during initialization
Preconditions: allocProgress accurately represents state of allocated memory
Postconditions: Heap memory is freed
****/
void cleanupRBS (int allocProgress)
{
	int i;

	/*Free memory based on how much memory has been already allocated*/
	switch (allocProgress)
	{
		case BLOCKS_ALLOC:
			for (i = 0; i < totalBlocks; i++)
			{
				free (blockCoord[i]);
			}
			free (blockCoord);

		case PARTS_ALLOC:
			free (dataBounds);

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