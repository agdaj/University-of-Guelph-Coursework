program testAISimple
    implicit none
    character, dimension(3,3) :: testBoard
    integer :: testMove
    logical :: testFunction

    call boardSetup (testBoard)

    do
        read (*,100) testMove
100     format (I3)

        if (testMove == 10) exit

        select case (testMove)
            case (1)
                testBoard(1,1) = "X"
            case (2)
                testBoard(1,2) = "X"
            case (3)
                testBoard(1,3) = "X"
            case (4)
                testBoard(2,1) = "X"
            case (5)
                testBoard(2,2) = "X"
            case (6)
                testBoard(2,3) = "X"
            case (7)
                testBoard(3,1) = "X"
            case (8)
                testBoard(3,2) = "X"
            case (9)
                testBoard(3,3) = "X"
        end select

        call compMove (testBoard)
    end do

contains

subroutine compMove (tictac)
    implicit none
    character, dimension (3,3), intent (in out) :: tictac
    integer :: pathSets(3,8), pathsum(8)
    integer :: boardXY(9,2), k, x, y, randPos
    integer :: i, j

!   COMMENT
    pathSets = reshape ( (/ 1, 2, 3, &
                            4, 5, 6, &
                            7, 8, 9, &
                            1, 4, 7, &
                            2, 5, 8, &
                            3, 6, 9, &
                            1, 5, 9, &
                            3, 5, 7 /), (/3,8/) )
!   COMMENT
    boardXY = reshape ( (/ 1, 1, 1, 2, 2, 2, 3, 3, 3, &
                           1, 2, 3, 1, 2, 3, 1, 2, 3 /), (/9,2/) )

!     YOUR CODE GOES HERE

    do i = 1, 8
        pathsum(i) = 0

        do j = 1, 3
            x = boardXY(pathSets(j,i),1)
            y = boardXY(pathSets(j,i),2)

            if (tictac(x,y) == " ") then
                k = 0
            else if (tictac(x,y) == "X") then
                k = 1
            else if (tictac(x,y) == "O") then
                k = 4
            end if

            pathsum(i) = pathsum(i) + k
        end do

        write (*,*) pathsum(i)
    end do

!
    do i = 1, 8
        if (pathsum(i) == 8) then
            do j = 1, 3
                x = boardXY(pathSets(j,i),1)
                y = boardXY(pathSets(j,i),2)

                if (tictac(x,y) == " ") then
                    tictac(x,y) = "O"
                    return
                endif
            end do
        end if
    end do

!
    do i = 1, 8
        if (pathsum(i) == 2) then
            do j = 1, 3
                x = boardXY(pathSets(j,i),1)
                y = boardXY(pathSets(j,i),2)

                if (tictac(x,y) == " ") then
                    tictac(x,y) = "O"
                    return
                endif
            end do
        end if
    end do

!
    do
        randPos = int (rand (0) * 9) + 1
        x = boardXY(randPos,1)
        y = boardXY(randPos,2)

        if (tictac(x,y) == " ") then
            tictac(x,y) = "O"
            exit
        end if
    end do

    return
end subroutine compMove

subroutine boardSetup (tictac)
    implicit none
    character, dimension(3,3), intent(in out) :: tictac
    integer :: i, j

    do i = 1, 3
        do j = 1, 3
            tictac(i,j) = " "
        end do
    end do

    return
end subroutine boardSetup

end program testAISimple
