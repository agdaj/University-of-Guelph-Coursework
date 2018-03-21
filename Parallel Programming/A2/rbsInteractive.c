/*********************************************
rbsInteractive.c
Name: Jireh Agda (0795472)
Date Created: 2016 11 04
     Last Modified: 2016 11 08
Red/Blue Algorithm Computation Parallelized
- Functions to run the Red/Blue Algorithm with
  interactivity (one worker thread)
*********************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>

#include "rbsInitialize.h"
#include "rbsInteractive.h"
#include "rbsAlgo.h"
#include "fileOutput.h"

#define MAXBUFFER 100


/****Interactive Mode Functions****/

/****
interactiveRBS: Initializes an interface for the program's interactive mode
Preconditions: Program is in interactive mode
Postconditions: Red/Blue is simulated to user's content
****/
void interactiveRBS ()
{
	bool exitProg = false;
	int stepInput;
	char inputBuffer[MAXBUFFER];

	int boardSize = parameters->bWidth, tileWidth = parameters->tWidth;
	int steps = 0, maxSteps = parameters->maxSteps, cLimit = cThreshold;
	
	bool myContinue = contAlgo;
	int numOfBlocks = totalBlocks;

	int totalHalfSteps = 0, i;

	printBoard (stdout);

	/*Take in commands in a loop and start interactive Red/Blue Algorithm*/
	while (exitProg == false && myContinue == true && steps < maxSteps)
	{
		printf ("Input interactive command (<Enter>, #, 'h', 'c', 'x'):\n");
		fgets (inputBuffer, MAXBUFFER, stdin);
		printf ("\n");

		stepInput = processInput (inputBuffer);
		if (stepInput >= 0)
		{
			/*Exit rbs program*/
			if (stepInput == 0)
			{
				printf ("Now exiting...\n\n");
				exitProg = true;
			}
			else
			{
				/*Simulate Red/Blue Algorithm by user's parameter*/
				if (totalHalfSteps % 2 != 0 && stepInput > 1)    stepInput--;    //sync method if only red shift has occurred

				/*Perform a number of half steps*/
				for (i = 0; i < stepInput; i++)
				{
					if (totalHalfSteps % 2 == 0)    //perform red step
					{
						shiftReds (0, boardSize, boardSize);
						totalHalfSteps++;

						currentBlock = 0;
						maxCDensity = checkBlocks (numOfBlocks, tileWidth, cLimit, false);    //only for recording density
						contAlgo = true;
					}
					else                            //perform blue step and condition
					{
						shiftBlues (0, boardSize, boardSize);
						totalHalfSteps++;
						
						currentBlock = 0;    //reset end condition items
						maxCDensity = checkBlocks (numOfBlocks, tileWidth, cLimit, false);    //always record max density
						iterations = totalHalfSteps/2;

						myContinue = contAlgo;
						steps++;
					}

					if (myContinue == false || steps >= maxSteps)    i = stepInput;    //if end condition encountered during simulation, exit immediately
				}

				/*Print intermediary board and line*/
			    printBoard (stdout);
				printLastLine (stdout, 0.00, false);
				printf ("\n");
			}
		}
		else
		{
			printf ("Error\n\n");
		}
	}

	if (myContinue == false)    printf ("Threshold reached\n\n");
	else if (steps >= maxSteps) printf ("Max steps reached\n\n");

	endPrinting (0.00);

	return;
}

/****
processInput: Processes user input in interactive mode
Preconditions: Program is in interactive mode
Postconditions: User's input is interpreted into the number of half steps to be made
****/
int processInput (char *input)
{
	char *validNum;
	int inputNum;

	if (strlen (input) == 1)    //empty, just <Enter>, supply threads with 2 half steps
	{
		return (2);
	}
	else if (strlen (input) >= MAXBUFFER - 1 && input[MAXBUFFER-2] != '\n')
	{
	    while (getchar () != '\n') {};    //flush buffer if input exceeds MAXBUFFER chars
		return (-1);
	}
	else
	{
		input[strlen(input)-1] = '\0';    //replace newline with null char

	    if (strcmp (input, "h") == 0)    //1 half step
		{
			return (1);
		}
		else if (strcmp (input, "c") == 0)    //maxSteps - # iterations already done (* 2 since half steps unit)
		{
			return ((parameters->maxSteps - iterations) * 2);
		}
		else if (strcmp (input, "x") == 0)    //exit with code 0
		{
			return (0);
		}
		else
		{
			//num amount of steps if valid, including half step sync
			//half step unit, so * 2 the input
	        validNum = NULL;
	        inputNum = strtol (input, &validNum, 10);
			if (input[0] == '\0' || validNum[0] != '\0')	return (-1);
			if (inputNum < 1)								return (-1);    //only allow min. of 1

			return (inputNum * 2);
		}
	}
}