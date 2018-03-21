# Makefile for C-Minus Compiler Project
# Written by Jireh Agda (0795472), for CIS*4650
# Date Last Modified: 2017 03 24

JAVAC=javac
JFLEX=jflex
CLASSPATH=-classpath /usr/share/java/cup.jar:.
CUP=cup

all: cm.class

cm.class: AbSynCM/*.java SymTableCM/*.java AssembleCM/*.java parser.java sym.java LexerCM.java cm.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) $^

LexerCM.java: ScannerCM.flex
	$(JFLEX) ScannerCM.flex

parser.java: ParserCM.cup
	$(CUP) -dump -expect 3 ParserCM.cup

clean:
	rm -f parser.java sym.java LexerCM.java AbSynCM/*.class SymTableCM/*.class AssembleCM/*.class *.class *~
