# Makefile for CIS*3090 National Collision Database Assignment (1)
# Written by Jireh Agda (0795472), for CIS*3090
# Date Created: 2016 09 23, Last Modified: 2016 10 12

PILOTHOME = /work/wgardner/pilot
MPEHOME = /work/wgardner/mpe

CC = mpicc
CPPFLAGS += -I$(PILOTHOME)/include -I$(MPEHOME)/include
CFLAGS += -std=c99 -Wall -g -O0
LDFLAGS += -L$(PILOTHOME)/lib -lpilot -L$(MPEHOME)/lib -lmpe

# "make all" creates the bang program and cleans
all: bang clean


# "make bang" will compile relevant .c and .h files
bang: bangMain.o dataReadFunc.o queryFunc.o
	$(CC) $^ $(LDFLAGS) -o $@

bangMain.o: bangMain.c bangMain.h
	$(CC) -c $(CPPFLAGS) $(CFLAGS) $<

dataReadFunc.o: dataReadFunc.c dataReadFunc.h
	$(CC) -c $(CPPFLAGS) $(CFLAGS) $<

queryFunc.o: queryFunc.c queryFunc.h
	$(CC) -c $(CPPFLAGS) $(CFLAGS) $<


clean:
	$(RM) *.o *.mod
