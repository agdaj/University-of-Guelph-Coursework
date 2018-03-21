/********************************************
rbsAlgo.h
Name: Jireh Agda (0795472)
Date Created: 2016 11 04
     Last Modified: 2016 11 08
Red/Blue Algorithm Computation Parallelized
- Header File for Functions that run the
  Red/Blue Algorithm with the given arguments
********************************************/

/*This is the header file for rbs's functions that individual threads will execute
  to run the Red/Blue Algorithm on the given initialized board*/

#ifndef rbsAlgo_h
#define rbsAlgo_h

#include "rbsInitialize.h"    //Contains bool definition


/****Red/Blue Work Functions****/

/****
mainWorkFunc: Function to pass to threads to work on Red/Blue Algorithm
Preconditions: Red/Blue board and statistics initialized
Postconditions: Red/Blue Algorithm is simulated on board
****/
void *mainWorkFunc (void *arg);

/****
shiftReds: Moves any red cell right if unoccupied (with wrapping)
Preconditions: Red/Blue board initialized
Postconditions: Red cells are shifted to the right if able
****/
void shiftReds (int xStart, int xEnd, int boardSize);

/****
shiftBlues: Moves any blue cell down if unoccupied (with wrapping)
Preconditions: Red/Blue board initialized
Postconditions: Blue cells are shifted downwards if able
****/
void shiftBlues (int yStart, int yEnd, int boardSize);

/****
checkBlocks: Check through all tiles on the board for Red/Blue end condition
Preconditions: Red/Blue board and statistics initialized
Postconditions: Updates maxCDensity if the colour threshold has been passed or is the last step
                Also returns the highest density recorded when called
****/
int checkBlocks (int blocks, int blockSize, int blockThreshold, bool lastStep);

#endif