/******************************************
rbsInteractive.h
Name: Jireh Agda (0795472)
Date Created: 2016 11 04
     Last Modified: 2016 11 08
Red/Blue Algorithm Computation Parallelized
- Header File for Functions that run the
  Red/Blue Algorithm with interactivity
******************************************/

/*This is the header file for rbs's functions that handle the interactive option of
  the Red/Blue Algorithm, calling other functions to run the algorithm*/

#ifndef rbsInteractive_h
#define rbsInteractive_h


/****Interactive Mode Functions****/

/****
interactiveRBS: Initializes an interface for the program's interactive mode
Preconditions: Program is in interactive mode
Postconditions: Red/Blue is simulated to user's content
****/
void interactiveRBS ();

/****
processInput: Processes user input in interactive mode
Preconditions: Program is in interactive mode
Postconditions: User's input is interpreted into the number of half steps to be made
****/
int processInput (char *input);

#endif