/*************************************
eightQall.c
Eight Queens Problem Solution Printing
By: Jireh Agda (0795472)
Date Last Modified: 2016 04 06
*************************************/

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

void tryEightQueens (int i, bool * columnCheck, bool * dCheckOne, bool * dCheckTwo, int * columnList);
void printSolution (int * columnList);

int main ()
{
    int i;
    bool * columnCheck, * dCheckOne, * dCheckTwo;
    int * columnList;    //represents x array for queen placement

    columnCheck = malloc (sizeof (bool) * 8);
    dCheckOne = malloc (sizeof (bool) * 15);
    dCheckTwo = malloc (sizeof (bool) * 15);
    columnList = malloc (sizeof (int) * 8);

    /*Initialize each boolean array with true*/
    for (i = 0; i < 8; i++)
    {
        columnCheck[i] = true;
    }
    for (i = 0; i < 15; i++)
    {
        dCheckOne[i] = true;
        dCheckTwo[i] = true;
    }

    /*Call recursive function to find and print eight queens solutions to queensC.txt*/
    tryEightQueens (1, columnCheck, dCheckOne, dCheckTwo, columnList);

    free (columnCheck);
    free (dCheckOne);
    free (dCheckTwo);
    free (columnList);

    return (0);
}

/****
tryEightQueens: Find all the solutions of Eight Queens through recursive calls and print them to queensC.txt
Preconditions: a is bool array of size 8, b and c are bool arrays of size 15, columnList is int array of size 8
Postconditions: Eight Queens solutions appended to queensC.txt
****/
void tryEightQueens (int i, bool * columnCheck, bool * dCheckOne, bool * dCheckTwo, int * columnList)
{
    int j;

    /*Create an array of integers that indicate a queen's column placement for each row*/
    for (j = 1; j <= 8; j++)
    {
        if (columnCheck[j-1] && dCheckOne[(i+j)-2] && dCheckTwo[(i-j)+7])
        {
            columnList[i-1] = j;
            columnCheck[j-1] = false;
            dCheckOne[(i+j)-2] = false;
            dCheckTwo[(i-j)+7] = false;

            if (i < 8)
            {
                /*Recursively call the function to find all the solutions*/
                tryEightQueens (i + 1, columnCheck, dCheckOne, dCheckTwo, columnList);
            }
            else
            {
                printSolution (columnList);
            }

            columnCheck[j-1] = true;
            dCheckOne[(i+j)-2] = true;
            dCheckTwo[(i-j)+7] = true;
        }
    }

    return;
}

/****
printSolution: Prints an Eight Queens solution to queensC.txt
Preconditions: columnList is int array of size 8
Postconditions: Eight Queens solution appended to queensC.txt
****/
void printSolution (int * columnList)
{
    FILE * solutionFile;
    int row, column;
    static int solutionNum = 0;

    solutionNum ++;    //save solution numbers with each solution

    solutionFile = fopen ("./queensC.txt", "a");
    if (solutionFile == NULL)
    {
        printf ("Unable to open queensC.txt\n");
        exit (-1);
    }

    fprintf (solutionFile, "%d\n", solutionNum);

    /*For each chess board row, print '.' at each column unless columnList matches column, then print 'Q'*/
    for (row = 0; row < 8; row++)
    {
        for (column = 0; column < 8; column++)
        {
            if (columnList[row] == (column + 1))
            {
                fprintf (solutionFile, "Q ");
            }
            else
            {
                fprintf (solutionFile, ". ");
            }
        }

        fprintf (solutionFile, "\n");
    }

    fprintf (solutionFile, "\n");
    fclose (solutionFile);

    return;
}
