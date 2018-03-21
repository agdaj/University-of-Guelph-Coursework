# Makefile for Cobol Trithemius Cipher
# Written by: Jireh Agda (0795472), for CIS*3190
# Date Last Modified: 2016 03 14

COC = cobc
COFLAGS = -free -Wall -O

all:
	$(COC) -x $(COFLAGS) triCipher.cob
	$(COC) -m $(COFLAGS) encrypt.cob
	$(COC) -m $(COFLAGS) decrypt.cob
