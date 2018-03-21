* triCipher.cob
* Encrypt and Decrypt Messages with Trithemius Cipher
* Name: Jireh Agda
* Date Last Modified: 2016 03 17

identification division.
program-id. triCipher.

environment division.
input-output section.
file-control.
    select inputFile assign to userInput
    organization is sequential
    access is sequential
    file status fileStatus.

data division.
file section.
fd inputFile.
01 input-record.
    02 inputString pic X(100).

working-storage section.
77 fileStatus pic 99     value 00.
77 fileName   pic X(99)  value spaces.

77 finish     pic 9      value 0.
77 goodInput  pic 9      value 0.

77 choice     pic 9      value 0.
77 userInput  pic X(99)  value spaces.
77 numSpaces  pic 99     value 00.
77 strLength  pic 99     value 00.
77 inspectNum pic 99     value 00.

procedure division.
* repeatedly loop the program, only finishing upon exit
    perform until finish = 1
        display "Welcome to the Trithemius Encrypter and Decrypter"
        display "Please enter a file name with text (*.txt) to encrypt or decrypt (or 'exit'): "

* take input and find exit if it is there
        accept userInput
        inspect userInput
            tallying
            inspectNum for leading "exit"
        perform string-length

* if exit is found exit loop (strLength = 4 to ensure 'exit' only)
        if inspectNum > 0 and strLength = 4 then
            move 1 to finish
        else
* attempt to open a file given input, and continue to encrypt-decrypt if available
            open input inputFile
            if fileStatus = 35 then
                display "File does not exist"
                display " "
            else
* move the input used to open a file to separate string, start paragraph encrypt-decrypt
                move spaces to fileName
                move userInput to fileName
                display " "
                perform encrypt-decrypt
            end-if
        end-if

        move spaces to userInput
        move 00 to fileStatus
    end-perform.
    stop run.

encrypt-decrypt.
* ask if user wants to encrypt or to decrypt, looping until an appropriate answer is made
    move zero to goodInput.
    perform until goodInput = 1
        move " " to userInput
        display "Choose '1' to encrypt and '2' to decrypt (or 'exit'):"
        accept userInput
        perform string-length

* identify if user chose 1 (encrypt) or 2 (decrypt)
        if strLength = 1 then
            move userInput to choice

            if choice > 0 and < 3 then
* display original text first
                display " "
                display "Original Text:"
                perform until finish = 1
                    move spaces to inputString
                    read inputFile into input-record
                        at end move 1 to finish
                    end-read
                    display input-record with no advancing
                end-perform
                display " "
                close inputFile
                move 0 to finish

* call appropriate external function using the fileName as a parameter
                evaluate choice
                    when 1 call "encrypt" using fileName
                    when 2 call "decrypt" using fileName
                end-evaluate

                move 1 to goodInput
            else
                display "Please select '1', '2', or 'exit'"
                display " "
            end-if
        else if strLength = 4 then
* identify if user want to exit
            inspect userInput
            tallying
            inspectNum for leading "exit"

            if inspectNum > 0 then
                close inputFile
                stop run
            else
                display "Please select '1', '2', or 'exit'"
                display " "
            end-if
        else
            display "Please select '1', '2', or 'exit'"
            display " "
        end-if
        end-if
    end-perform.

* code based on: http://stackoverflow.com/questions/24777344/compute-length-string-of-variable-with-cobol
string-length.
* reverse the string and count the number of 'leading' spaces
    move zeros to numSpaces.
    inspect function reverse(userInput)
        tallying
        numSpaces for leading spaces.

* length of actual string is length allocated - number of trailing spaces
    compute strLength = length of userInput - numSpaces.
