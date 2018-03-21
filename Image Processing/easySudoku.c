/*******************************
easySudoku.c
By: Jireh Agda (0795472)
Date Last Modified: 2017 04 14
Solves Sudoku Puzzles Entered as
an 81-int Set of Arguments
*******************************/

#include <stdio.h>
#include <string.h>

#define TRUE 1
#define FALSE 0
#define RED   "\x1B[31m"  //RED colour for in-place numbers
#define RESET "\x1B[0m"

int solved = FALSE;
int sudokuBoard[9][9];    //global Sudoku board
int copyBoard[9][9];      //reference board to track in-place numbers

void sudokuSolver (int currSquare);
int isValidSquare (int currSquare);
void printBoard ();

int main (int argc, char *argv[])
{
    int i, j, k;
    int argLength, argNum;

    /* First check the argument count */
    if (argc != 82)
    {
        fprintf (stderr, "Error - need 81 integers (1-9) as arguments\n");    //81 integer arguments + program name = 82 arguments total
        return (1);
    }

    /* Then, fill the Sudoku board with the given number arguments */
    i = 1;
    for (j = 0; j < 9; j++)
    {
        for (k = 0; k < 9; k++)
        {
            argLength = strlen (argv[i]);
            argNum = argv[i][0] - 48;                                         //ascii -> 0 starts at 48

            if (argLength != 1 || (argNum < 0 || argNum > 9))
            {
                fprintf (stderr, "Error - need 81 integers (1-9) as arguments\n");
                return (1);
            }

            sudokuBoard[j][k] = argNum;
            copyBoard[j][k] = argNum;
            i++;
        }
    }

    printf ("Original Sudoku Grid:\n");
    printBoard();    //print original board

    /* Finally, attempt to solve the board and print it if possible */
    sudokuSolver(0);

    if (solved == TRUE)
    {
        printf ("Solution:\n");
        printBoard ();
    }
    else
        fprintf (stderr, "Error - board unsolvable\n");

    return (0);
}

/****
sudokuSolver: Recursively solve sudoku puzzle using backtracking (use with 0)
Preconditions: Sudoku board and copy board initialized with 0 - 9 (where 0 == empty)
Postconditions: Modifies solved to TRUE of sudoku board is solved, else stays FALSE
****/
void sudokuSolver (int currSquare)
{
    int row, column, num;

    if (currSquare >= 81)    //if the last square has been passed, assume a solution was found, reverse recursion
    {
        solved = TRUE;
        return;
    }
    else
    {
        row = currSquare / 9;
        column = currSquare % 9;

        /* If the current square isn't a fixed square, try numbers on it */
        if (copyBoard[row][column] == 0)
        {
            /* Recursively iterate through 1 - 9 on each square until a valid board is completed */
            for (num = 1; num <= 9; num++)
            {
                sudokuBoard[row][column] = num;

                /* Check if the current placeholder num is valid (no conflicts when placed), recursion if valid */
                if (isValidSquare (currSquare) == TRUE)
                {
                    sudokuSolver (currSquare + 1);
                    if (solved == TRUE)    //if the recursion reached a solution, skip other combinations and recurse backwards
                        num = 9;
                }
            }

            /* Reset the board when backtracking */
            if (solved == FALSE)
                sudokuBoard[row][column] = 0;
        }
        else
        {
            /* Move on to the next square, this one is fixed */
            sudokuSolver (currSquare + 1);
        }
    }
}

/****
isValidSquare: Assess whether current square location contains valid number (no conflict on row, column and 3x3 grid)
Preconditions: Sudoku board initialized with 0 - 9, current square does not contain 0 (where 0 represents empty square)
Postcondition: Return TRUE = 1 if square number is valid, else FALSE = 0
****/
int isValidSquare (int currSquare)
{
    int row, column;
    int x, y, xEnd, yEnd;

    row = currSquare / 9;
    column = currSquare % 9;

    /* First check row */
    for (y = 0; y < 9; y++)
    {
        if (y != column)    //prevent checking current grid
        {
            if (sudokuBoard[row][y] == sudokuBoard[row][column])
                return FALSE;
        }
    }

    /* Then check column */
    for (x = 0; x < 9; x++)
    {
        if (x != row)       //prevent checking current grid
        {
            if (sudokuBoard[x][column] == sudokuBoard[row][column])
                return FALSE;
        }
    }

    /* Lastly check 3x3 grid */
    x = (row / 3) * 3; xEnd = x + 2;        //0,1,2=>0, 3,4,5=>3, 6,7,8=>6
    y = (column / 3) * 3; yEnd = y + 2;

    for (x = x; x <= xEnd; x++)
    {
        y = (column / 3) * 3;

        for (y = y; y <= yEnd; y++)
        {
            if (x != row && y != column)    //prevent checking current grid
            {
                if (sudokuBoard[x][y] == sudokuBoard[row][column])
                    return FALSE;
            }
        }
    }

    return TRUE;
}

/****
printBoard: Prints out sudokuBoard to the terminal visually
Preconditions: Each element of sudokuBoard 2D-array is single-digit number
Postconditions: Sudoku board is printed
****/
void printBoard ()
{
    int i, j, rowPrint;

    rowPrint = 0;

    for (i = 1; i <= 13; i++)
    {
        if (i == 1 || i == 5 || i == 9 || i == 13)            /* Print the board dividers */
            printf ("+-----+-----+-----+\n");
        else                                                  /* Print the board rows */
        {
            printf ("|");

            for (j = 0; j < 9; j++)
            {
                if (copyBoard[rowPrint][j] != 0)
                    printf (RED "%d" RESET, sudokuBoard[rowPrint][j]);
                else
                    printf ("%d", sudokuBoard[rowPrint][j]);

                if (j == 2 || j == 5 || j == 8)
                    printf ("|");
                else
                    printf (" ");
            }

            printf ("\n");
            rowPrint++;
        }
    }
}
