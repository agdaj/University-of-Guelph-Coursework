/***************************************************
orbsInitialize.c
Name: Jireh Agda (0795472)
Date Created: 2016 11 11
     Last Modified: 2016 11 13
Red/Blue Algorithm Computation Parallelized (OpenMP)
- Functions to initialize the program and board
***************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include "orbsInitialize.h"


/****Red/Blue Argument Handling Functions****/

/****
rbsInitialize: Reads in program arguments and stores them as parameters
Preconditions: None
Postconditions: Argument data returned in RBS_Param return value
NOTE: Uses malloc - free after use
****/
RBS_Param *rbsInitialize (int argc, char *argv[])
{
	RBS_Param *parameters;
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
				return (NULL);
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
		else
		{
			printf ("Invalid parameter detected\n");	//Ignore invalid parameters
		}
	}

	if (validityCheck (reqParam, paramVal) == false) return (NULL);

	/*Assume that arguments are valid, create struct to return*/
	parameters = (RBS_Param *) malloc (sizeof (RBS_Param));
	if (parameters == NULL)
	{
		printf ("malloc error on RBS_Param; Aborting...\n");
		return (NULL);
	}

	parameters->processes = paramVal[0];
	parameters->bWidth = paramVal[1];
	parameters->tWidth = paramVal[2];
	parameters->cDensity = paramVal[3];
	parameters->maxSteps = paramVal[4];
	parameters->seed = hasSeed;
	parameters->rSeed = paramVal[5];

	return (parameters);
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
char **boardInitialize (RBS_Param * parameters)
{
	char **rbBoard;
	int i, j, boardNum;

	/*Max out the number of threads to board width if >*/
	if (parameters->processes > parameters->bWidth)
	{
		parameters->processes = parameters->bWidth;
	}

	/*Allocate space for the board*/
	rbBoard = (char **) malloc (sizeof (char *) * parameters->bWidth);
	if (rbBoard == NULL)
	{
		printf ("malloc error on rbBoard\n");
		return (NULL);
	}
	
	for (i = 0; i < parameters->bWidth; i++)
	{
		rbBoard[i] = (char *) malloc (sizeof (char) * parameters->bWidth);
		if (rbBoard[i] == NULL)
		{
			for (i = i - 1; i >= 0; i--)
			{
				free (rbBoard[i]);
			}
			free (rbBoard);

			return (NULL);
		}
	}

	/*Seed the randomization if requested, and build the board*/
	if (parameters->seed == false)	srand (time (NULL));
	else							srand (parameters->rSeed);

	for (i = 0; i < parameters->bWidth; i++)
	{
		for (j = 0; j < parameters->bWidth; j++)
		{
			boardNum = (int) rand () % 3;
			if (boardNum == RED)
			{
				rbBoard[i][j] = (char) RED;
			}
			else if (boardNum == BLUE)
			{
				rbBoard[i][j] = (char) BLUE;
			}
			else
			{
				rbBoard[i][j] = (char) WHITE;
			}
		}
	}

	return (rbBoard);
}

/****
calculateTileSets: Creates and stores partition boundaries for the Red/Blue Algorithm end condition work
Preconditions: Program arguments read, checked for validity, and stored
Postconditions: Global partition boundaries for end condition initialized and stored
NOTE: Uses malloc - free after use
****/
int **calculateTileSets (RBS_Param *parameters, int *totalTiles)
{
	int rowsInTile, **tileCoord;
	int i, j, k;

	rowsInTile = parameters->bWidth / parameters->tWidth;
	*totalTiles = rowsInTile * rowsInTile;

	/*Allocate space for the block coordinates (x and y)*/
	tileCoord = (int **) malloc (sizeof (int *) * (*totalTiles));
	if (tileCoord == NULL)
	{
		printf ("malloc error on blocksCoord\n");
		return (NULL);
	}

	for (i = 0; i < (*totalTiles); i++)
	{
		tileCoord[i] = (int *) malloc (sizeof (int) * 2);
		if (tileCoord[i] == NULL)
		{
			for (i = i - 1; i >= 0; i--)
			{
				free (tileCoord[i]);
			}
			free (tileCoord);

			return (NULL);
		}
	}

	/*Assign coordinates to globally available data structure*/
	k = 0;
	for (i = 0; i < rowsInTile; i++)
	{
		for (j = 0; j < rowsInTile; j++)
		{
			tileCoord[k][0] = i * parameters->tWidth;
			tileCoord[k][1] = j * parameters->tWidth;
			k++;
		}
	}

	return (tileCoord);
}