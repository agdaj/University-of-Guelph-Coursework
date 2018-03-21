/***************************************************
orbsAlgo.c
Name: Jireh Agda (0795472)
Date Created: 2016 11 11
     Last Modified: 2016 11 14
Red/Blue Algorithm Computation Parallelized (OpenMP)
- Functions to run the Red/Blue Algorithm with
  the given arguments
***************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "orbsInitialize.h"
#include "orbsAlgo.h"
#include "fileOutput.h"


/****Red/Blue Work Functions****/

/****
mainWorkFunc: Function to work on Red/Blue Algorithm
Preconditions: Red/Blue board and parameters initialized
Postconditions: Red/Blue Algorithm is simulated on board
****/
void mainWorkFunc (int boardSize, int tileSize, int maxSteps, char **rbBoard, int **tileCoord, int totalTiles, int cThreshold, int *iterations, int *maxCDensity)
{
	int steps = 0, cDensity;
	bool stopAlgo = false;

	/*Start auto Red/Blue Algorithm*/
	while (stopAlgo == false && steps < maxSteps)
	{
		#pragma omp parallel
		{
			/*Shift all reads, then all blues*/
			shiftReds (boardSize, rbBoard);
			shiftBlues (boardSize, rbBoard);
		}

		/*Check blocks for the c condition and for recording cDensity after check*/
		cDensity = 0;
		stopAlgo = checkTiles (tileCoord, totalTiles, tileSize, rbBoard, cThreshold, &cDensity);
		
		steps++;
	}
	
	*iterations = steps;
	*maxCDensity = cDensity;

	return;
}

/****
shiftReds: Moves any red cell right if unoccupied (with wrapping)
Preconditions: Red/Blue board initialized
Postconditions: Red cells are shifted to the right if able
****/
void shiftReds (int boardSize, char **rbBoard)
{
    int i, j;
	bool wrapBlock;

	/*Look row by row in for shifting red cells*/
	#pragma omp for schedule (dynamic)
	for (i = 0; i < boardSize; i++)
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
void shiftBlues (int boardSize, char **rbBoard)
{
    int i, j;
	bool wrapBlock;

	/*Look column by column for shifting blue cells*/
	#pragma omp for schedule (dynamic)
	for (i = 0; i < boardSize; i++)
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
checkTiles: Check through all tiles on the board for Red/Blue end condition
Preconditions: Red/Blue board and statistics initialized
Postconditions: Updates maxCDensity with the largest c density found
                Also returns if threshold has been reached
****/
bool checkTiles (int **tileCoord, int totalTiles, int tileSize, char **rbBoard, int cThreshold, int *maxCDensity)
{
	bool overThreshold = false;
    int redTiles, blueTiles, biggestTiles;
	int tileX, tileXEnd, tileY, tileYEnd, myMaxDensity = 0, blockDensity;
	int i, j, k;
	
	/*For each tile, check the density*/
	#pragma omp parallel for private (j,k,tileX,tileXEnd,tileY,tileYEnd,redTiles,blueTiles,blockDensity,biggestTiles) schedule (dynamic)
    for (i = 0; i < totalTiles; i++)    //cyclic decomposition; check blocks
    {
		/*Set block to be investigated, then search and count for REDs and BLUEs*/
        tileX = tileCoord[i][0]; tileXEnd = tileX + tileSize;
        tileY = tileCoord[i][1]; tileYEnd = tileY + tileSize;
		redTiles = 0; blueTiles = 0;

		for (j = tileX; j < tileXEnd; j++)
		{
			for (k = tileY; k < tileYEnd; k++)
			{
				if (rbBoard[j][k] == RED)	redTiles++;
				if (rbBoard[j][k] == BLUE)	blueTiles++;
			}
		}
        
		/*Record the highest count from REDs or BLUEs*/
		biggestTiles = MAX(redTiles, blueTiles);
		blockDensity = biggestTiles * 100 / (tileSize * tileSize);

		#pragma omp critical (MAX_C_LOCK)
		{
			if (blockDensity > myMaxDensity)    myMaxDensity = blockDensity;    //keep track of largest coloured block seen
		}
		if (biggestTiles > cThreshold)	
			#pragma omp atomic
			overThreshold |= true;           //mark if we have passed the threshold
	}	
	
	*maxCDensity = myMaxDensity;

	return (overThreshold);		
}

