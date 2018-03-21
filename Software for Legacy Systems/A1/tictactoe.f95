! tictactoe.f95
! Re-Engineered Tic-Tac-Toe to Fortran 95
! By: Jireh Agda (0795472)
! Date Last Modified: 2016 02 01

! A program to play Tic-Tac-Toe
program tictactoe
    implicit none

    character, dimension(3,3) :: tictacBoard
    character :: winner
    logical :: gameOver
    integer :: turn

    ! Writes and sets the original board and instructions for play
    write (*,*) "PLAY TIC-TAC-TOE. ENTER 1-9 TO PLAY"
    write (*,*) " "
    write (*,*) "        1 | 2 | 3 "
    write (*,*) "       ---+---+---"
    write (*,*) "        4 | 5 | 6 "
    write (*,*) "       ---+---+---"
    write (*,*) "        7 | 8 | 9 "
    write (*,*) " "
    call boardSetup (tictacBoard)

    ! Initialize turn to 0 -> start with player move
    turn = 0

    ! Contains the main loop of the game to cycle through turns
    do
        if (turn == 0) then
            call playerMove (tictacBoard)
            call showBoard (tictacBoard, turn)
            turn = 1
        else
            call compMove (tictacBoard)
            call showBoard (tictacBoard, turn)
            turn = 0
        end if

        call checkOver (tictacBoard, gameOver, winner)
        if (gameOver .eqv. .true.) exit
    end do

    write (*,*) "The game is over!"
    if (winner == "D") then
        write (*,*) "The game is a draw. "
    else
        write (*,*) "The winner is: ", winner
    end if

contains
      
! showBoard: print the board after a turn
! =========================================
subroutine showBoard (tictacBoard, turn)
    implicit none

    character, dimension(3,3), intent(in) :: tictacBoard
    integer, intent(in) :: turn

    integer :: i, j

    if (turn == 0) then
        write (*,*) "After your move..."
    else if (turn == 1) then
        write (*,*) "After my move..."
    end if

    ! Print the board at its current state
    ! Prints line of tiles and lines alternatingly
    do i = 1, 3
        write (*,400) (tictacBoard(i,j), j = 1, 3)
400     format (2X,A1,1X,"|",1X,A1,1X,"|",1X,A1,1X)

        select case (i)
            case (1, 2)
                write (*,*) "---+---+---"
        end select
    end do

    return
end subroutine showBoard

! playerMove: prompt player to make move
! =========================================
subroutine playerMove (tictacBoard)
    implicit none

    character, dimension(3,3), intent(in out) :: tictacBoard

    integer :: move

    do
        ! The program assumes an integer will be entered for a move, no defense otherwise
        ! Any form of invalid input will cycle to start of loop, else exit
        write (*,*) "Your move? "
        read (*,*) move
        if (move <= 0 .or. move > 9) then
            write (*,*) "Invalid input."
            cycle
        end if

        if (checkPlay (tictacBoard, move) .eqv. .false.) then
            write (*,*) "Invalid move, box already occupied."
            cycle
        else
            exit
        end if
    end do

    ! Place the player X mark to appropriate board tile
    select case (move)
        case (1)
            tictacBoard(1,1) = "X"
        case (2)
            tictacBoard(1,2) = "X"
        case (3)
            tictacBoard(1,3) = "X"
        case (4)
            tictacBoard(2,1) = "X"
        case (5)
            tictacBoard(2,2) = "X"
        case (6)
            tictacBoard(2,3) = "X"
        case (7)
            tictacBoard(3,1) = "X"
        case (8)
            tictacBoard(3,2) = "X"
        case (9)
            tictacBoard(3,3) = "X"
    end select

    return
end subroutine playerMove

! checkOver: check to see if the game is over
! =========================================
subroutine checkOver (tictacBoard, gameOver, winner)
    implicit none

    character, dimension(3,3), intent(in) :: tictacBoard
    character, intent(out) :: winner
    logical, intent(out) :: gameOver

    character, parameter :: BLANK = ' ', DRAW = 'D'

    logical :: diagonalMatch
    integer :: row, column

    ! Assume the game is over at start
    gameOver = .true.

    ! Check rows for a winner
    do row = 1, 3
        if (match (tictacBoard(row,1), tictacBoard(row,2), tictacBoard(row,3)) .eqv. .true.) then
            winner = tictacBoard(row,1)
            return
        end if
    end do

    ! Check columns for a winner if no row match
    do column = 1, 3
        if (match (tictacBoard(1,column), tictacBoard(2,column), tictacBoard(3,column)) .eqv. .true.) then
            winner = tictacBoard(1,column)
            return
        end if
    end do

    ! Check diagonals if no row or column match
    diagonalMatch = match (tictacBoard(1,1), tictacBoard(2,2), tictacBoard(3,3)) .or. &
                    match (tictacBoard(1,3), tictacBoard(2,2), tictacBoard(3,1))
    if (diagonalMatch .eqv. .true.) then
        winner = tictacBoard(2,2)
        return
    end if

    ! Check for a draw if no winner at all
    ! Check all tiles for an empty space
    do row = 1, 3
        do column = 1, 3
            if (tictacBoard(row,column) == BLANK) then
                gameOver = .false.
                return
            end if
        end do
    end do
            
    ! If no blanks are found, game is a draw
    winner = DRAW

    return
end subroutine checkOver
      
! compMove: play for the computer  
! =========================================
subroutine compMove (tictacBoard)
    implicit none

    character, dimension(3,3), intent(in out) :: tictacBoard

    integer :: pathSets(3,8), pathSum(8)
    integer :: boardXY(9,2), k, x, y, randPos
    integer :: i, j

    ! Sets of tic-tac-toe paths are set (rows, columns, diagonals)
    pathSets = reshape ( (/ 1, 2, 3, &
                            4, 5, 6, &
                            7, 8, 9, &
                            1, 4, 7, &
                            2, 5, 8, &
                            3, 6, 9, &
                            1, 5, 9, &
                            3, 5, 7 /), (/3,8/) )
    ! A set of (x,y) pairs are set
    boardXY = reshape ( (/ 1, 1, 1, 2, 2, 2, 3, 3, 3, &
                           1, 2, 3, 1, 2, 3, 1, 2, 3 /), (/9,2/) )

    ! Calculate path sums, where blanks are 0, X's are 1 and O's are 4
    do i = 1, 8
        pathSum(i) = 0

        do j = 1, 3
            x = boardXY(pathSets(j,i),1)
            y = boardXY(pathSets(j,i),2)

            if (tictacBoard(x,y) == " ") then
                k = 0
            else if (tictacBoard(x,y) == "X") then
                k = 1
            else if (tictacBoard(x,y) == "O") then
                k = 4
            end if

            pathSum(i) = pathSum(i) + k
        end do
    end do

    ! If a path sum is 8 (-> two O's in a path), computer
    ! makes an offensive move to win the game
    do i = 1, 8
        if (pathSum(i) == 8) then
            do j = 1, 3
                x = boardXY(pathSets(j,i),1)
                y = boardXY(pathSets(j,i),2)

                if (tictacBoard(x,y) == " ") then
                    tictacBoard(x,y) = "O"
                    return
                endif
            end do
        end if
    end do

    ! If a path sum is 2 (-> two X's in a path), computer
    ! makes a defensive move to not lose the game
    do i = 1, 8
        if (pathSum(i) == 2) then
            do j = 1, 3
                x = boardXY(pathSets(j,i),1)
                y = boardXY(pathSets(j,i),2)

                if (tictacBoard(x,y) == " ") then
                    tictacBoard(x,y) = "O"
                    return
                endif
            end do
        end if
    end do
  
    ! Else, place an O randomly on the board
    do
        randPos = int (rand (0) * 9) + 1
        x = boardXY(randPos,1)
        y = boardXY(randPos,2)

        if (tictacBoard(x,y) == " ") then
            tictacBoard(x,y) = "O"
            exit
        end if
    end do
  
    return
end subroutine compMove

! match: check a set of three tiles of a tictactoe board (row, column or diagonal)
! set match to .true. if tile elements are the same, else .false.
! =========================================
logical function match (tile1, tile2, tile3)
    implicit none

    character, intent(in) :: tile1, tile2, tile3

    if (tile1 == "X" .and. tile2 == "X" .and. tile3 == "X") then
        match = .true.
    else if (tile1 == "O" .and. tile2 == "O" .and. tile3 == "O") then
        match = .true.
    else
        match = .false.
    end if

end function match
  
! boardSetup: set up the tic-tac-toe board
! =========================================  
subroutine boardSetup (tictacBoard)
    implicit none

    character, dimension(3,3), intent(in out) :: tictacBoard

    integer :: i, j

    ! Fill every board tile with a space " "
    do i = 1, 3
        do j = 1, 3
            tictacBoard(i,j) = " "
        end do
    end do

    return
end subroutine boardSetup

! checkPlay: check if player move is valid (tile is blank)
! checkPlay is set to .true. if valid, else .false.
! ========================================= 
logical function checkPlay (tictacBoard, move)
    implicit none

    character, dimension(3,3), intent(in) :: tictacBoard
    integer, intent(in) :: move

    character :: gridChar

    ! Check move to determine current tic-tac-toe tile
    select case (move)
        case (1)
            gridChar = tictacBoard(1,1)
        case (2)
            gridChar = tictacBoard(1,2)
        case (3)
            gridChar = tictacBoard(1,3)
        case (4)
            gridChar = tictacBoard(2,1)
        case (5)
            gridChar = tictacBoard(2,2)
        case (6)
            gridChar = tictacBoard(2,3)
        case (7)
            gridChar = tictacBoard(3,1)
        case (8)
            gridChar = tictacBoard(3,2)
        case (9)
            gridChar = tictacBoard(3,3)
    end select

    if (gridChar == " ") then
        checkPlay = .true.
    else
        checkPlay = .false.
    end if

end function checkPlay

end program tictactoe
