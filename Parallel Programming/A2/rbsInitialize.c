/**********************************************
rbsInitialize.c
Name: Jireh Agda (0795472)
Date Created: 2016 10 30
     Last Modified: 2016 11 08
Red/Blue Algorithm Computation Parallelized
- Functions to initialize the program and board
**********************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "rbsInitialize.h"


/****Red/Blue Argument Handling Functions****/

/****
rbsInitialize: Reads in program arguments and stores them as parameters
Preconditions: None
Postconditions: Argument data stored in RBS_Param global variable
NOTE: Uses malloc - free after use
****/
void rbsInitialize (int argc, char *argv[])
{
	bool reqParam[5] = {false, false, false, false, false};    //5 required parameters
	int paramVal[6] = {0, 0, 0, 0, 0, -1};                     //5 req. + seed (init to -1 = no seed)
	bool hasSeed = false, isInteractive = false;

	char argChar, *validNum;
	int i, paramNum;

	parameters = NULL;    //start with NULL, success = not NULL

	/*Read through each argument and store to temp buffer to be checked for validity*/
	for (i = 1; i < argc; i ++)
	{
		argChar = argv[i][0];

		/*Check first character as first set of validity check*/
		if (argChar == 'p' || argChar == 'b' || argChar == 't' || argChar == 'c' ||
			argChar == 'm' || argChar == 's')
		{
			validNum = NULL;
			paramNum = strtol (&argv[i][1], &validNum, 10);
			if (argv[i][1] == '\0' || validNum[0] != '\0')
			{
				printf ("Invalid integer parameter on %c; Aborting...\n", argChar);
				return;
			}

			switch (argChar)
			{
				case 'p':
					reqParam[0] = true;
					paramVal[0] = paramNum;
					break;

				case 'b':
					reqParam[1] = true;
					paramVal[1] = paramNum;
					break;

				case 't':
					reqParam[2] = true;
					paramVal[2] = paramNum;
					break;

				case 'c':
					reqParam[3] = true;
					paramVal[3] = paramNum;
					break;

				case 'm':
					reqParam[4] = true;
					paramVal[4] = paramNum;
					break;

				case 's':
					hasSeed = true;
					paramVal[5] = paramNum;
			}
		}
		else if (argChar == 'i')
		{
			isInteractive = true;
		}
		else
		{
			printf ("Invalid parameter detected\n");	//Ignore invalid parameters
		}
	}

	if (validityCheck (reqParam, paramVal) == false) return;

	/*Assume that arguments are valid, create struct to return*/
	parameters = malloc (sizeof (RBS_Param));
	if (parameters == NULL)
	{
		printf ("malloc error on RBS_Param; Aborting...\n");
		return;
	}

	parameters->processes = paramVal[0];
	parameters->bWidth = paramVal[1];
	parameters->tWidth = paramVal[2];
	parameters->cDensity = paramVal[3];
	parameters->maxSteps = paramVal[4];
	parameters->seed = hasSeed;
	parameters->rSeed = paramVal[5];
	parameters->interactive = isInteractive;

	return;
}

/****
validityCheck: Checks presence and validity of required program arguments (p, b, t, c, m)
Preconditions: Program arguments read and stored
Postconditions: Returns true if all arguments fall within required parameters
****/
bool validityCheck (bool argPresent[5], int argVals[5])
{
	char charArgs[5] = {'p', 'b', 't', 'c', 'm'};
	int i;

	/*Check for presence and validity of each argument*/
	for (i = 0; i < 5; i++)
	{
		if (argPresent[i] == false)
		{
			printf ("Argument %c not found; Aborting...\n", charArgs[i]);
			return false;
		}

		switch (i)
		{
			case 0:
				if (argVals[0] < 1)
				{
					printf ("p < 1; Aborting...\n");
					return false;
				}
				break;

			case 1:
				if (argVals[1] < 2)
				{
					printf ("b < 2; Aborting...\n");
					return false;
				}
				break;

			case 2:
				if ((argVals[1] % argVals[2]) != 0)
				{
					printf ("b mod t != 0; Aborting...\n");
					return false;
				}
				break;

			case 3:
				if (argVals[3] < 1 || argVals[3] > 100)
				{
					printf ("c < 1 || c > 100; Aborting...\n");
					return false;
				}
				break;

			case 4:
				if (argVals[4] < 0)
				{
					printf ("m < 0; Aborting...\n");
					return false;
				}
		}
	}

	return true;
}


/****Red/Blue Board and Global Data Initialization Functions****/

/****
boardInitialize: Initializes Red/Blue board
Preconditions: Program arguments read, checked for validity, and stored
Postconditions: Red/Blue board is initialized
NOTE: Uses malloc - free after use
****/
bool boardInitialize ()
{
	int i, j;

	/*Max out the number of threads to board width if >*/
	if (parameters->processes > parameters->bWidth)
	{
		parameters->processes = parameters->bWidth;
	}

	/*Allocate space for the board*/
	rbBoard = malloc (sizeof (short int *) * parameters->bWidth);
	if (rbBoard == NULL)
	{
		printf ("malloc error on rbBoard\n");
		return false;
	}
	
	for (i = 0; i < parameters->bWidth; i++)
	{
		rbBoard[i] = malloc (sizeof (short int) * parameters->bWidth);
		if (rbBoard[i] == NULL)
		{
			for (i = i - 1; i >= 0; i--)
			{
				free (rbBoard[i]);
			}
			free (rbBoard);

			return false;
		}
	}

	/*Seed the randomization if requested, and build the board*/
	if (parameters->seed == false)	srand (time (NULL));
	else							srand (parameters->rSeed);

	for (i = 0; i < parameters->bWidth; i++)
	{
		for (j = 0; j < parameters->bWidth; j++)
		{
			rbBoard[i][j] = (short int) rand () % 3;
		}
	}

	return true;
}

/****
divideDataSets: Creates and stores partition boundaries for the Red/Blue Algorithm work
Preconditions: Program arguments read, checked for validity, and stored
Postconditions: Global partition boundaries initialized and stored
NOTE: Uses malloc - free after use
****/
bool divideDataSets ()
{
	int i, j;
	int divideSize, moduloSize;

	dataBounds = malloc (sizeof (int) * (parameters->processes + 1));
	if (dataBounds == NULL)
	{
		printf ("malloc error on dataBounds\n");
		return false;
	}

	divideSize = parameters->bWidth / parameters->processes;    //find even division and remainder
	moduloSize = parameters->bWidth % parameters->processes;

	/*Set initial partition boundaries using n/P*/
	for (i = 0; i < parameters->processes; i++)
	{
		dataBounds[i] = i * divideSize;
	}
	dataBounds[i] = parameters->bWidth;

	/*Readjust using remaining modulo, shifting the last partitions right by an amount (load balancing)*/
	j = parameters->processes - 1;
	for (i = moduloSize - 1; i > 0; i--)
	{
		dataBounds[j] += i;
		j--;
	}

	return true;
}

/****
calculateBlockSets: Creates and stores partition boundaries for the Red/Blue Algorithm end condition work
Preconditions: Program arguments read, checked for validity, and stored
Postconditions: Global partition boundaries for end condition initialized and stored
NOTE: Uses malloc - free after use
****/
bool calculateBlockSets ()
{
	int blocksRow;
	int i, j, k;

	currentBlock = 0;
	blocksRow = parameters->bWidth / parameters->tWidth;
	totalBlocks = blocksRow * blocksRow;

	/*Allocate space for the block coordinates (x and y)*/
	blockCoord = malloc (sizeof (int *) * totalBlocks);
	if (blockCoord == NULL)
	{
		printf ("malloc error on blocksCoord\n");
		return false;
	}

	for (i = 0; i < totalBlocks; i++)
	{
		blockCoord[i] = malloc (sizeof (int) * 2);
		if (blockCoord[i] == NULL)
		{
			for (i = i - 1; i >= 0; i--)
			{
				free (blockCoord[i]);
			}
			free (blockCoord);

			return false;
		}
	}

	/*Assign coordinates to globally available data structure*/
	k = 0;
	for (i = 0; i < blocksRow; i++)
	{
		for (j = 0; j < blocksRow; j++)
		{
			blockCoord[k][0] = i * parameters->tWidth;
			blockCoord[k][1] = j * parameters->tWidth;
			k++;
		}
	}

	/*Calculate c parameter threshold for blocks*/
	cThreshold = parameters->tWidth * parameters->tWidth * parameters->cDensity / 100;

	return true;
}