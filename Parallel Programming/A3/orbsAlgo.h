/****************************************************
orbsAlgo.h
Name: Jireh Agda (0795472)
Date Created: 2016 11 11
     Last Modified: 2016 11 13
Red/Blue Algorithm Computation Parallelized (OpenMP)
- Header File for Functions that run the
  Red/Blue Algorithm with the given arguments
***************************************************/

/*This is the header file for orbs's functions that individual threads will execute
  to run the Red/Blue Algorithm on the given initialized board*/

#ifndef rbsAlgo_h
#define rbsAlgo_h

#include "orbsInitialize.h"    //Contains bool definition


/****Red/Blue Work Functions****/

/****
mainWorkFunc: Function to work on Red/Blue Algorithm
Preconditions: Red/Blue board and parameters initialized
Postconditions: Red/Blue Algorithm is simulated on board and the iterations occurred and maximum c density
                are returned via memory reference
****/
void mainWorkFunc (int boardSize, int tileSize, int maxSteps, char **rbBoard, int **tileCoord, int totalTiles, int cThreshold, int *iterations, int *maxCDensity);

/****
shiftReds: Moves any red cell right if unoccupied (with wrapping)
Preconditions: Red/Blue board initialized
Postconditions: Red cells are shifted to the right if able
****/
void shiftReds (int boardSize, char **rbBoard);

/****
shiftBlues: Moves any blue cell down if unoccupied (with wrapping)
Preconditions: Red/Blue board initialized
Postconditions: Blue cells are shifted downwards if able
****/
void shiftBlues (int boardSize, char **rbBoard);

/****
checkTiles: Check through all tiles on the board for Red/Blue end condition
Preconditions: Red/Blue board and statistics initialized
Postconditions: Updates maxCDensity with the largest c density found
                Also returns if threshold has been reached
****/
bool checkTiles (int **tileCoord, int totalTiles, int tileSize, char **rbBoard, int cThreshold, int *maxCDensity);

#endif