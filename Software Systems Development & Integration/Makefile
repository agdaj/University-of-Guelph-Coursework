# Makefile for Dialogc and yadc (Release Version)
# Written by Jireh Agda (0795472), for CIS*2750
# Date Last Modified: 2015 04 03

CC = gcc
CFLAGS = -ansi -Wall -pedantic -g
INCLUDES = -Iinclude -IyadcSrc -I/usr/lib/jvm/java-1.6.0-openjdk/include -I/usr/lib/jvm/java-1.6.0-openjdk/include/linux
LIBS = -lpm -ll
LEX = lex
YACC = yacc

all: Dialogc clean


Dialogc: libJNIpm.so yadc 
	javac Dialogc.java PostgresLogin.java

libJNIpm.so: DialogcToPMJNI.o 
	$(CC) -shared -Wl,-soname,libJNIpm.so -L./src $(INCLUDES) -o $@ $? $(LIBS)

DialogcToPMJNI.o: src/DialogcToPMJNI.c src/libpm.a
	$(CC) $(CFLAGS) -L./src $(INCLUDES) -fPIC -c src/DialogcToPMJNI.c $(LIBS)


yadc: lex yacc src/libpm.a
	$(CC) $(CFLAGS) -L./src $(INCLUDES) yadcSrc/lex.yy.c yadcSrc/y.tab.c -o $@ $(LIBS)

lex: yadcSrc yadcSrc/yadc.l
	cd yadcSrc; $(LEX) yadc.l

yacc: yadcSrc yadcSrc/yadc.y
	cd yadcSrc; $(YACC) -d -y yadc.y


clean:
	@ rm *.o
	cd yadcSrc; rm lex.yy.c y.tab.h y.tab.c
