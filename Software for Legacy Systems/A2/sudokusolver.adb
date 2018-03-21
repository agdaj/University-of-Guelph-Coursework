-- sudokusolver.adb
-- Solves Sudoku Puzzles in Files
-- By: Jireh Agda (0795472)
-- Date Last Modified: 2016 02 29

with Ada.Text_IO; use Ada.Text_IO;
with Ada.Integer_Text_IO; use Ada.Integer_Text_IO;
with Ada.Strings.Unbounded; use Ada.Strings.Unbounded;
with Ada.Strings.Unbounded.Text_IO; use Ada.Strings.Unbounded.Text_IO;
with Ada.Characters.Handling; use Ada.Characters.Handling;

procedure SudokuSolver is
    type digit_grid2d is array (integer range <>, integer range <>) of integer range 0..9;    -- array type to store digit-based grids (for sudoku grid)

    copyGrid, sudokuGrid : digit_grid2d (1..9, 1..9);
    inFile, outFile : file_type;
    proceed : Boolean;

    -- promptInputFile: prompt Sudoku input file or exit from program
    procedure promptInputFile (inputFile : out file_type; proceedSolver : out Boolean) is
        fileNameInput : unbounded_string;

    begin
        proceedSolver := true;    -- assume user wants to proceed until exit is entered

        loop
            put_line ("Please specify Sudoku input file (*.txt) or 'exit' to exit:");
            get_line (fileNameInput);
            if (fileNameInput = "exit" or fileNameInput = "Exit") then
                proceedSolver := false;
                exit;
            else
                begin
                    open (inputFile, in_file, To_String (fileNameInput));
                    exit;
                exception
                    when name_error =>    -- prevent program from moving forward if file name doesn't exist
                        put_line ("File does not exist");
                    when status_error =>    -- prevent files already in use to be read from
                        put_line ("File already in use");
                    when use_error =>    -- handle file names unable to be used
                        put_line ("File unable to be used");
                end;
                new_line;
            end if;
        end loop;
    end promptInputFile;

    -- promptOutputFile: prompt Sudoku output file or exit from program
    procedure promptOutputFile (outputFile : out file_type; proceedSolver : out Boolean) is
        fileNameOutput : unbounded_string;

    begin
        proceedSolver := true;    -- assume user wants to proceed until exit is entered

        loop
            put_line ("Please specify Sudoku output file (*.txt) or 'exit' to exit:");
            get_line (fileNameOutput);
            if (fileNameOutput = "exit" or fileNameOutput = "Exit") then
                proceedSolver := false;
                exit;
            else
                begin
                    open (outputFile, append_file, To_String (fileNameOutput));
                    exit;
                exception  
                    when name_error =>    -- create an output file if it doesn't exist
                        create (outputFile, append_file, To_String (fileNameOutput));
                        exit;
                    when status_error =>    -- prevent files already in use to be written to
                        put_line ("File already in use");
                    when use_error =>    -- handle file names unable to be used
                        put_line ("File unable to be used");
                end;
                new_line;
            end if;
        end loop;
    end promptOutputFile;

    -- readSudoku: reads sudoku grid from a file
    -- Requires at least 9 rows, length 9 of each row
    -- Checks if grid is valid (modify Boolean), assumes readFile is opened
    procedure readSudoku (readGrid : out digit_grid2d; readFile : in file_type; validGrid : out Boolean) is
        fileString : unbounded_string;
        rows, columns : integer;

        -- gridCheck: check grid validity as it is read, identifies problem spot
        procedure gridCheck (readGrid : in digit_grid2d; row, column : out integer; validGrid : out Boolean) is
            boxRowEnd, boxColumnEnd, i : integer;
            numCheck : array (1..9) of Boolean;

        begin
            -- first check each row, setting Boolean array member to true when the number is found
            -- repeats are detected if already true, 0 is ignored
            for rowNum in 1..9 loop
                numCheck := (1..9 => false);
                for columnNum in 1..9 loop
                    if (readGrid(rowNum,columnNum) = 0) then
                        null;
                    elsif (not numCheck(readGrid(rowNum,columnNum))) then
                        numCheck(readGrid(rowNum,columnNum)) := true;
                    else
                        row := rowNum; column := columnNum;
                        validGrid := false;
                        return;
                    end if;
                end loop;
            end loop;

            -- then check each column
            for columnNum in 1..9 loop
                numCheck := (1..9 => false);
                for rowNum in 1..9 loop
                    if (readGrid(rowNum,columnNum) = 0) then
                        null;
                    elsif (not numCheck(readGrid(rowNum,columnNum))) then
                        numCheck(readGrid(rowNum,columnNum)) := true;
                    else
                        row := rowNum; column := columnNum;
                        validGrid := false;
                        return;
                    end if;
                end loop;
            end loop;

            -- lastly check each 3x3 grid
            row := 1;
            loop
                exit when row > 9;
                column := 1;

                loop
                    exit when column > 9;
                    boxRowEnd := row + 2; boxColumnEnd := column + 2;
                    numCheck := (1..9 => false);

                    i := row;
                    loop
                        exit when i > boxRowEnd;
                        column := boxColumnEnd - 2;

                        loop
                            exit when column > boxColumnEnd;

                            if (readGrid(i,column) = 0) then
                                null;
                            elsif (not numCheck(readGrid(i,column))) then
                                numCheck(readGrid(i,column)) := true;
                            else
                                row := i;
                                validGrid := false;
                                return;
                            end if;

                            column := column + 1;
                        end loop;

                        i := i + 1;
                    end loop;
                end loop;

                row := row + 3;
            end loop;

            validGrid := true;
        end gridCheck;

    begin
        validGrid := true;    -- assume sudoku grid is valid until it is not
        rows := 0;

        loop
            exit when end_of_file (readFile);
            exit when rows > 9;    -- read only 9 rows

            get_line (readFile, fileString);

            rows := rows + 1;

            -- check if row has enough characters (9 digits), else return false through validGrid
            if (length (fileString) < 9) then
                put_line ("File has line of incorrect length");
                validGrid := false;
                return;
            end if;

            -- loop through the line and set sudoku grid if integer, else error and validGrid->false
            columns := 1;
            for columns in 1..9 loop
                if (is_digit (element (fileString, columns))) then
                    readGrid(rows,columns) := Character'Pos(element (fileString, columns)) - 48;    -- convert element to ascii -> integer value
                else
                    put ("Invalid character at row "); put (rows, width=>1); new_line;
                    validGrid := false;
                    return;
                end if;
            end loop;            
        end loop;

        -- last check to see if enough rows were read
        if (rows < 9) then
            put_line ("File does not have enough rows");
            validGrid := false;
        end if;

        -- finally, check if grid is valid (no repeat numbers in rows, columns, boxes other than 0)
        gridCheck (readGrid, rows, columns, validGrid);
        if (validGrid = false) then
            put ("Sudoku grid has invalid number at row:"); put (rows, width=>1); put (" column:"); put (columns, width=>1); new_line;
        end if;

    end readSudoku;

    -- findSolution: find solution of given sudoku grid, with original copy to track fixed grids (start at 1,1)
    -- Modifies Boolean to true if there is a solution, else false
    -- Involves recursion - Backtracking algorithm based off of https://en.wikipedia.org/wiki/Sudoku_solving_algorithms
    procedure findSolution (origGrid, solveGrid : in out digit_grid2d; x, y : in integer; solvable : out Boolean) is
        newX, newY : integer;

        -- isValidGrid: assess choice at x, y in sudoku grid and checks if valid choice
        function isValidGrid (solveGrid : digit_grid2d; x, y : integer) return Boolean is
            i, iEnd, j, jEnd : integer;

        begin
            -- first check column
            i := 1;
            for i in 1..9 loop
                if (i /= x) then    -- prevent checking current grid
                    if (solveGrid(i,y) = solveGrid(x,y)) then
                        return false;
                    end if;
                end if;
            end loop;

            -- then check row
            j := 1;
            for j in 1..9 loop
                if (j /= y) then    -- prevent checking current grid
                    if (solveGrid(x,j) = solveGrid(x,y)) then
                        return false;
                    end if;
                end if;
            end loop;

            -- lastly check 3x3 grid
            i := ((x - 1)/3) * 3 + 1; iEnd := i + 2;   -- 1,2,3=>1, 4,5,6=>4, 7,8,9=>9
            j := ((y - 1)/3) * 3 + 1; jEnd := j + 2;

            loop
                exit when i > iEnd;
                j := ((y - 1)/3) * 3 + 1;

                loop
                    exit when j > jEnd;
                    if (i /= x and j /= y) then    -- prevent checking current grid
                        if (solveGrid(i,j) = solveGrid(x,y)) then
                            return false;
                        end if;
                    end if;

                    j := j + 1;

                end loop;

                i := i + 1;
            end loop;

            return true;
        end isValidGrid;

        -- advanceGrid: helper function to find next sudoku grid spot
        -- Traversal is column-wise
        procedure advanceGrid (row, column : in out integer) is
        begin
            if (row < 9) then
                row := row + 1;
            else
                row := 1;
                column := column + 1;
            end if;
        end advanceGrid;

    begin
        -- first set next grid location
        newX := x; newY := y;
        advanceGrid (newX, newY);

        -- if y (column) is over 9, assume puzzle was solved and return
        if (y > 9) then
            solvable := true;
            return;
        elsif (origGrid(x,y) = 0) then
            loop
                -- exceeding 9 implies failed current solution, so reset and backtrack
                begin
                    solveGrid(x,y) := solveGrid(x,y) + 1;
                exception
                    when constraint_error =>
                        solvable := false;
                        solveGrid(x,y) := 0;
                        return;
                end;

                -- check for validity, recursively continuing if valid
                if (isValidGrid (solveGrid, x, y)) then
                    -- recursive call is made on the next grid, and will return afterwards if solved
                    -- else current grid will +1
                    findSolution (origGrid, solveGrid, newX, newY, solvable);
                    if (solvable) then
                        return;
                    end if;
                end if;
            end loop;
        else
            -- call to next grid if current grid is a fixed grid number
            findSolution (origGrid, solveGrid, newX, newY, solvable);
        end if;

    end findSolution;

    -- printSolution: print (sudoku) grid on the terminal and file
    -- Assumes the file given is open and the grid contains digits to be printed
    procedure printSolution (printGrid : in digit_grid2d; solutionFile : in file_type) is
        rowPrint : integer := 1;

    begin
        for i in 1..13 loop
            case i is
                when 1 | 5 | 9 | 13 =>
                    -- print the in-between rows of a sudoku grid to stdout and file
                    put_line ("+-----+-----+-----+");
                    put_line (solutionFile, "+-----+-----+-----+");
                when others =>
                    put ("|");
                    put (solutionFile, "|");

                    -- print the rows with numbers to stdout and file
                    for j in 1..9 loop
                        put (printGrid(rowPrint,j), width=>1);
                        put (solutionFile, printGrid(rowPrint,j), width=>1);
                        case j is
                            when 3 | 6 | 9 =>
                                put ("|");
                                put (solutionFile, "|");
                            when others =>
                                put (" ");
                                put (solutionFile, " ");
                        end case; 
                    end loop;

                    rowPrint := rowPrint + 1;
                    new_line;
                    new_line (solutionFile);
            end case;
        end loop;

    end printSolution;

    -- closeFiles: close files of sudoku grid program if open
    procedure closeFiles (inputFile, outputFile : in out file_type) is
    begin
        if is_open (inputFile) then
            close (inputFile);
        end if;

        if is_open (outputFile) then
            close (outputFile);
        end if;
    end closeFiles;

begin
    -- main program structure
    -- loop to allow multiple sudoku solutions
    loop
        put_line ("Sudoku Solver in Ada");

        -- loop input file prompt until exit, loops with improper grid 
        loop
            promptInputFile (inFile, proceed);
            if (not proceed) then
                closeFiles (inFile, outFile);
                return;
            else
                readSudoku (sudokuGrid, inFile, proceed);
                if (proceed) then
                    exit;
                else
                    closeFiles (inFile, outFile);
                    new_line;
                end if;
            end if;
        end loop;
        new_line;

        -- ask for output file or exit
        promptOutputFile (outFile, proceed);
        if (not proceed) then
            closeFiles (inFile, outFile);
            return;
        end if;
        new_line;

        -- create copy of grid to track fixed vs. not fixed grids
        for i in 1..9 loop
            for j in 1..9 loop
                copyGrid(i,j) := sudokuGrid(i,j);
            end loop;
        end loop;

        -- find and print a solution if possible, and loop back
        findSolution (copyGrid, sudokuGrid, 1, 1, proceed);
        if (not proceed) then
            put_line ("Puzzle has no solution");
        else
            put_line ("Original Sudoku Grid:"); put_line (outFile, "Original Sudoku Grid:");
            printSolution (copyGrid, outFile);
            put_line ("Solution:"); put_line (outFile, "Solution:");
            printSolution (sudokuGrid, outFile);
            new_line (outFile);
        end if;

        new_line;
        closeFiles (inFile, outFile);
    end loop;

end SudokuSolver;

