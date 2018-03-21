/******************************************
rbsInitialize.h
Name: Jireh Agda (0795472)
Date Created: 2016 11 04
     Last Modified: 2016 11 08
Red/Blue Algorithm Computation Parallelized
- Header File for Argument Reading and
  Program Initializing
******************************************/

/*This is the header file for rbs's functions that reads and stores program arguments
  and initializes the rbs board and global data*/

#ifndef rbsInitialize_h
#define rbsInitialize_h

#include <pthread.h>

/*Define constants of how to store red/blue cells*/
#define WHITE 0
#define RED 1
#define BLUE 2

typedef int bool;    //define bool construct, since the compiler is unable to find stdbool.h
enum {false, true};

struct RBS_Param    //Struct to contain program arguments
{
	int processes;
	int bWidth;
	int tWidth;
	int cDensity;
	int maxSteps;
	bool seed;
	int rSeed;
	bool interactive;
};
typedef struct RBS_Param RBS_Param;

extern RBS_Param *parameters;  //global parameter set
extern short int **rbBoard;    //global board definition
extern int *dataBounds;        //global data boundaries (to be read in locally once)

extern int **blockCoord;       //global block data (t parameter related items, including whether to continue)
extern int currentBlock, totalBlocks;
extern int cThreshold;
extern bool contAlgo;

extern int iterations, maxCDensity;    //global stats-keeping variables

extern pthread_barrier_t barrier;      //thread sync: barrier and mutex for stats keeping
extern pthread_mutex_t updateStats;


/****Red/Blue Argument Handling Functions****/

/****
rbsInitialize: Reads in program arguments and stores them as parameters
Preconditions: None
Postconditions: Argument data stored in RBS_Param global variable
NOTE: Uses malloc - free after use
****/
void rbsInitialize (int argc, char *argv[]);

/****
validityCheck: Checks presence and validity of required program arguments (p, b, t, c, m)
Preconditions: Program arguments read and stored
Postconditions: Returns true if all arguments fall within required parameters
****/
bool validityCheck (bool argPresent[5], int argVals[5]);


/****Red/Blue Board and Global Data Initialization Functions****/

/****
boardInitialize: Initializes Red/Blue board
Preconditions: Program arguments read, checked for validity, and stored
Postconditions: Red/Blue board is initialized
NOTE: Uses malloc - free after use
****/
bool boardInitialize ();

/****
divideDataSets: Creates and stores partition boundaries for the Red/Blue Algorithm work
Preconditions: Program arguments read, checked for validity, and stored
Postconditions: Global partition boundaries initialized and stored
NOTE: Uses malloc - free after use
****/
bool divideDataSets ();

/****
calculateBlockSets: Creates and stores partition boundaries for the Red/Blue Algorithm end condition work
Preconditions: Program arguments read, checked for validity, and stored
Postconditions: Global partition boundaries for end condition initialized and stored
NOTE: Uses malloc - free after use
****/
bool calculateBlockSets ();

#endif