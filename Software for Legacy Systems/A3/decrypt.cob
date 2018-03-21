* decrypt.cob
* Decrypt Input with Trithemius Cipher
* Name: Jireh Agda
* Date Last Modified: 2016 03 23

identification division.
program-id. decrypt.

environment division.
input-output section.
file-control.
    select inputFile assign to fileName
    organization is sequential
    access is sequential.

data division.
file section.
fd inputFile.
01 input-record.
    05 inputString pic X occurs 100 times.

working-storage section.
77 finish    pic 9    value 1.
77 shift     pic 99   value 00.
77 arrayNum  pic 9999.
77 asciiNum  pic 999.
77 i         pic 999.

linkage section.
77 fileName pic X(99).

procedure division using fileName.
    display "Decryption:"

* open the file with the fileName given with the call and read through the text and decrypt
    open input inputFile.
    perform until finish = 0
        move " " to input-record
        move 001 to arrayNum

* read in the file 100 characters at a time
        read inputFile into input-record
            at end move 0 to finish
        end-read

* look at each character and decrypt them as necessary
        perform until arrayNum > 100
            move function ord(inputString(arrayNum)) to asciiNum

* only look at alphabetical characters, ignoring whitespace (' ', '\n', '\t')
            if inputString(arrayNum) is alphabetic and asciiNum is not = 33 and not = 11 and not = 10 then
* set asciiNum to alphabet location relative to a (which is 0)
                if inputString(arrayNum) is alphabetic-lower then
                    subtract 98 from asciiNum
                else if inputString(arrayNum) is alphabetic-upper then
                    subtract 66 from asciiNum
                    end-if
                end-if

* shift the asciiNum, restricting the shift cycle to 0-25 (26 letters) and convert to lowercase letter
* then place back to inputString
                add shift to asciiNum
                compute asciiNum = function mod(asciiNum, 26)
                add 98 to asciiNum
                move function char(asciiNum) to inputString(arrayNum)

* change shift factor by 1 downwards (-1 is equivalent to +25)
                if shift = 0 then
                    move 25 to shift
                else
                    subtract 1 from shift
                end-if
            end-if

            add 1 to arrayNum
        end-perform

* formerly just printed the record, which included punctuation and spaces
* now prints only letters and newlines
*        display input-record with no advancing
        perform print-letters
    end-perform.
    display " ".

* reset module and close file
    move 1 to finish.
    move 0 to shift.
    close inputFile.
    goback.

* prints only letters or newlines from input-record after decrypting
print-letters.
    perform
        varying i from 1 by 1
        until i > 100

        if inputString(i) is alphabetic and (function ord(inputString(i)) is not = 10 and not = 33) then
            display inputString(i) with no advancing
        end-if
    end-perform.


