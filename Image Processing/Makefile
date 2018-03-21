# Makefile for Easy Sudoku Solver (CIS*4720 Project)
# By: Jireh Agda (0795472)
# Date Last Modified: 2017 04 12

all: easySudoku

easySudoku: easySudokuC easySudoku.py
	chmod 755 easySudoku.py

easySudokuC: easySudoku.c
	gcc -std=c99 -Wall -pedantic $^ -o $@
