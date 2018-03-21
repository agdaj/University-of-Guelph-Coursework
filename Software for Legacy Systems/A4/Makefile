# Makefile for Eight Queens Programs
# By: Jireh Agda (0795472)
# Date Last Modified: 2016 04 06

all: eightQueens

eightQueens: eightQueensA eightQueensC eightQueensF eightQueens.py
	chmod 755 eightQueens.py

eightQueensA: eightqall.adb
	gnatmake -Wall $^ -o $@

eightQueensC: eightQall.c
	gcc -std=c99 -Wall -pedantic $^ -o $@

eightQueensF: eightQall.f95
	gfortran -Wall $^ -o $@

