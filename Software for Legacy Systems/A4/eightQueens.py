#!/usr/bin/python3

# eightQueens.py (Python Script)
# Python Eight Queens Program Runner
# By: Jireh Agda (0795472)
# Date Last Modified: 2016 04 06

# Allow system commands
import os

notExit = True

print ("Eight Queens Solutions Runner")
print ("This script creates/modifies any queensX.txt using the compiled Eight Queens Ada, C, and Fortran programs in the current directory")

# Loop until 'exit' or 'Exit' is entered
while notExit:
    print (" ")
    userInput = input("Enter (A) for queensA.txt, (C) for queensC.txt, (F) for queensF.txt or (exit) to exit: ")

    # Process input to run appropriate program if it exists in the current directory
    if userInput == 'A' or userInput == 'a':
        success = os.system ("./eightQueensA")
        if success == 0:
            print ("./eightQueensA successfully run - queensA.txt created/modified")
    elif userInput == 'C' or userInput == 'c':
        success = os.system ("./eightQueensC")
        if success == 0:
            print ("./eightQueensC successfully run - queensC.txt created/modified")
    elif userInput == 'F' or userInput == 'f':
        success = os.system ("./eightQueensF")
        if success == 0:
            print ("./eightQueensF successfully run - queensF.txt created/modified")
    elif userInput == 'Exit' or userInput == 'exit':
        notExit = False
    else:
        print ("Please enter an appropriate input")
