/************************************************************
bangMain.h
Name: Jireh Agda (0795472)
Date Created: 2016 10 10
     Last Modified: 2016 10 12
NCDB Query Program Parallelized
Assignment 1 - CIS*3090
- Bang Main Header File (for worker function + time function)
************************************************************/

/*This is the header file is for bang's worker function and time function*/

#ifndef bangMain_h
#define bangMain_h


/****Bang Main Functions****/

/****
workerFunction: Outlines the work a worker does in the program "bang" when parallel
Preconditions: None
Postconditions: Worker reads and stores NCDB data and performs queries on their data as required
****/
int workerFunction (int id, void *args);

/****
timeUpdate: Marks the time elapsed from start and prints timing reports for program segments
Preconditions: timeArray has sufficient room to store timesStored amount of times
Postconditions: Timing report is printed and stored within timeArray
****/
int timeUpdate (int timeID, int cores, int timesStored, double *timeArray);


#endif
