/*********************************************
rbsAlgo.c
Name: Jireh Agda (0795472)
Date Created: 2016 11 04
     Last Modified: 2016 11 08
Red/Blue Algorithm Computation Parallelized
- Functions to run the Red/Blue Algorithm with
  the given arguments
*********************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>

#include "rbsInitialize.h"
#include "rbsInteractive.h"
#include "rbsAlgo.h"
#include "fileOutput.h"


/****Red/Blue Work Functions****/

/****
mainWorkFunc: Function to pass to threads to work on Red/Blue Algorithm
Preconditions: Red/Blue board and statistics initialized
Postconditions: Red/Blue Algorithm is simulated on board
****/
void *mainWorkFunc (void *arg)
{
	int id = (int) arg;
	int myPartStart = dataBounds[id], myPartEnd = dataBounds[id+1];
	int boardSize = parameters->bWidth, tileWidth = parameters->tWidth;
	int steps = 0, maxSteps = parameters->maxSteps, cLimit = cThreshold;
	
	bool myContinue = contAlgo;
	int numOfBlocks = totalBlocks;

	/*Start auto Red/Blue Algorithm*/
	while (myContinue == true && steps < maxSteps)
	{
		shiftReds (myPartStart, myPartEnd, boardSize);
		pthread_barrier_wait (&barrier);

		shiftBlues (myPartStart, myPartEnd, boardSize);
		if (id == 0)
		{
			currentBlock = 0;
			maxCDensity = 0;
		}
		pthread_barrier_wait (&barrier);

		/*Check blocks for the c condition, and if last step (to determine whether to record c density)*/
		if ((steps + 1) >= maxSteps)   checkBlocks (numOfBlocks, tileWidth, cLimit, true);
		else                           checkBlocks (numOfBlocks, tileWidth, cLimit, false);
			
		if (id == 0)    iterations++;
		pthread_barrier_wait (&barrier);

		myContinue = contAlgo;
		steps++;
	}
	
	return (NULL);
}

/****
shiftReds: Moves any red cell right if unoccupied (with wrapping)
Preconditions: Red/Blue board initialized
Postconditions: Red cells are shifted to the right if able
****/
void shiftReds (int xStart, int xEnd, int boardSize)
{
    int i, j;
	bool wrapBlock;

	/*Look row by row in thread's partition for shifting red blocks*/
	for (i = xStart; i < xEnd; i++)
	{
		wrapBlock = false;
		if (rbBoard[i][0] != WHITE)    wrapBlock = true;    //Track if wrapping is blocked

		for (j = 0; j < boardSize; j++)
		{
			if (j == boardSize - 1)
			{
				if (rbBoard[i][j] == RED && wrapBlock == false)
				{
					rbBoard[i][j] = WHITE; rbBoard[i][0] = RED;    //exchange cells
					j++;    //skip just shifted cell
				}
			}
            else
            {
                if (rbBoard[i][j] == RED && rbBoard[i][j+1] == WHITE)
				{
                    rbBoard[i][j] = WHITE; rbBoard[i][j+1] = RED;    //exchange cells
					j++;    //skip just shifted cell
				}           
            }
        }
	}

	return;
}

/****
shiftBlues: Moves any blue cell down if unoccupied (with wrapping)
Preconditions: Red/Blue board initialized
Postconditions: Blue cells are shifted downwards if able
****/
void shiftBlues (int yStart, int yEnd, int boardSize)
{
    int i, j;
	bool wrapBlock;

	/*Look column by column in thread's partition for shifting blue blocks*/
	for (i = yStart; i < yEnd; i++)
	{
		wrapBlock = false;
		if (rbBoard[0][i] != WHITE)    wrapBlock = true;    //Track if wrapping is blocked

		for (j = 0; j < boardSize; j++)
		{
			if (j == boardSize - 1)
			{
				if (rbBoard[j][i] == BLUE && wrapBlock == false)
				{
					rbBoard[j][i] = WHITE; rbBoard[0][i] = BLUE;    //exchange cells
					j++;    //skip just shifted cell
				}
			}
            else
            {
                if (rbBoard[j][i] == BLUE && rbBoard[j+1][i] == WHITE)
				{
                    rbBoard[j][i] = WHITE; rbBoard[j+1][i] = BLUE;    //exchange cells
					j++;    //skip just shifted cell
				}           
            }
        }
	}

	return;
}

#define MAX(a,b) (a>b ? a : b)

/****
checkBlocks: Check through all tiles on the board for Red/Blue end condition
Preconditions: Red/Blue board and statistics initialized
Postconditions: Updates maxCDensity if the colour threshold has been passed or is the last step
                Also returns the highest density recorded when called
****/
int checkBlocks (int blocks, int blockSize, int blockThreshold, bool lastStep)
{
	bool breakLoop = false, checkBlocks = true, pastThreshold = false;
    int redTiles, blueTiles, biggestTiles;
	int myBlock, tileX, tileXEnd, tileY, tileYEnd, myMaxDensity = 0, blockDensity;
	int i, j;
	
    while (checkBlocks == true)    //cyclic decomposition; check blocks
    {
        pthread_mutex_lock (&updateStats);
        if (currentBlock >= blocks)    //if all blocks have been assigned, break out of loop
        {
			if (pastThreshold == true || lastStep == true)    //if threshold count exceeded, update up to highest found (also if last step)
			{
				if (myMaxDensity > maxCDensity)
				{
					maxCDensity = myMaxDensity;
				}
				contAlgo = false;
			}
            checkBlocks = false;
            breakLoop = true;  
        }
		else
		{
            myBlock = currentBlock;
			currentBlock++;
		}
		pthread_mutex_unlock (&updateStats);

		if (breakLoop == true)
		{
			break;
		}

		/*Set block to be investigated, then search and count for REDs and BLUEs*/
        tileX = blockCoord[myBlock][0]; tileXEnd = tileX + blockSize;
        tileY = blockCoord[myBlock][1]; tileYEnd = tileY + blockSize;
		redTiles = 0; blueTiles = 0;

		for (i = tileX; i < tileXEnd; i++)
		{
			for (j = tileY; j < tileYEnd; j++)
			{
				if (rbBoard[i][j] == RED)	redTiles++;
				if (rbBoard[i][j] == BLUE)	blueTiles++;
			}
		}
        
		/*Record the highest count from REDs or BLUEs*/
		biggestTiles = MAX(redTiles, blueTiles);
		blockDensity = biggestTiles * 100 / (blockSize * blockSize);
		if (blockDensity > myMaxDensity)    myMaxDensity = blockDensity;    //keep track of largest coloured block seen
		if (biggestTiles > blockThreshold)	pastThreshold = true;           //mark if we have passed the threshold
	}	
	
	return (myMaxDensity);		
}

