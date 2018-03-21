# Makefile for Tic-Tac-Toe Re-Engineering Assignment
# Written by: Jireh Agda (0795472), for CIS*3190
# Date Last Modified: 2016 01 18

FC = gfortran

all: new

new:
	$(FC) tictactoe.f95 -o tictactoe

old:
	$(FC) tictactoe.for -o tictactoeOld

tests:
	$(FC) testChkPlay.f95 -o testChkPlay
	$(FC) testAISimple.f95 -o testAISimple
