-- eightQall.adb
-- Eight Queens Problem Solution Printing
-- By: Jireh Agda (0795472)
-- Date Last Modified: 2016 04 06

with Ada.Text_IO; use Ada.Text_IO;

procedure eightQall is
    type booleanArray is array (integer range <>) of Boolean;    -- define array types to pass along through procedures
    type integerArray is array (integer range <>) of integer;

    columnCheck : booleanArray (1..8);
    dCheckOne : booleanArray (2..16);
    dCheckTwo : booleanArray (-7..7);
    columnList : integerArray (1..8);
    k : integer;

    exit_program : exception;    -- create exception that allows program to exit from anywhere

    -- package Counter to allow for a static variable that counts solution numbers (to be printed with solution)
    -- Idea from: www.adapower.com/index/php?Command=Class&ClassID=Basics&CID=204
    package Counter is
        counter : integer := 0;
        function getCounter return integer;
    end Counter;

    package body Counter is
        -- getCounter: Returns a counter's state (having started at 0)
        function getCounter return integer is
        begin
            counter := counter + 1;
            return counter;
        end getCounter;
    end Counter;

    -- tryEightQueens: find Eight Queens solutions
    procedure tryEightQueens (i : integer; columnCheck, dCheckOne, dCheckTwo : in out booleanArray; columnList : in out integerArray) is
        k : integer;

        -- printSolution: Prints an Eight Queens solution to queensA.txt
        procedure printSolution (columnList : in integerArray) is
            solutionFile : file_type;
            solutionNum : integer;

        begin
            -- opens queensA.txt if it exists/openable
            begin
                open (solutionFile, append_file, "queensA.txt");
            exception
                when name_error =>    -- create queensA.txt if it doesn't already exist
                    create (solutionFile, append_file, "queensA.txt");
                when status_error =>    -- exit if unable to be opened
                    put_line ("queensA.txt already in use");
                    raise exit_program;
                when use_error =>    -- exit if unable to be opened
                    put_line ("queensA.txt unable to be used");
                    raise exit_program;
            end;

            solutionNum := Counter.getCounter;
            put_line (solutionFile, Integer'Image (solutionNum));

            -- for each chess board row, print '.' at each column unless columnList matches column, then print 'Q'
            for row in 1..8 loop
                for column in 1..8 loop
                    if (columnList(row) = column) then
                        put (solutionFile, "Q ");
                    else
                        put (solutionFile, ". ");
                    end if;

                end loop;

                new_line (solutionFile);
            end loop;

            new_line (solutionFile);

            if is_open (solutionFile) then
                close (solutionFile);
            end if;
        end printSolution;

    begin
        -- create an array of integers that indicate a queen's column placement for each row
        for j in 1..8 loop
            if (columnCheck(j) and dCheckOne(i+j) and dCheckTwo(i-j)) then
                columnList(i) := j;
                columnCheck(j) := false;
                dCheckOne(i+j) := false;
                dCheckTwo(i-j) := false;

                if (i < 8) then
                    k := i + 1;
                    tryEightQueens (k, columnCheck, dCheckOne, dCheckTwo, columnList);
                else
                    printSolution (columnList);
                end if;

                columnCheck(j) := true;
                dCheckOne(i+j) := true;
                dCheckTwo(i-j) := true;
            end if;
        end loop;

    end tryEightQueens;

begin
    -- initialize all Boolean to true
    for i in 1..8 loop
        columnCheck(i) := true;
    end loop;
    for i in 2..16 loop
        dCheckOne(i) := true;
    end loop;
    for i in -7..7 loop
        dCheckTwo(i) := true;
    end loop;

    -- begin the recursion to find all eight queens solution, and any raise exit_program will lead to the end
    begin
        k := 1;
        tryEightQueens (k, columnCheck, dCheckOne, dCheckTwo, columnList);    
    exception
        when exit_program =>
            null;
    end;

end eightQall;
