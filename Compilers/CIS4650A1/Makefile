# Makefile for SGML Scanner Program
# Written by Jireh Agda (0795472), for CIS*4650
# Date Last Modified: 2017 01 24

JAVAC=javac
JFLEX=jflex

all: ScannerSGML.class

ScannerSGML.class: ScannerSGML.java LexerSGML.java TokenSGML.java

%.class: %.java
	$(JAVAC) $^

LexerSGML.java: SGML.flex
	$(JFLEX) SGML.flex

clean:
	rm -f LexerSGML.java *.class *~
