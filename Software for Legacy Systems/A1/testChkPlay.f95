program testChkPlay
!
!   Test Tic-Tac-Toe testChkPlay (A1 CIS#3190)
!

    implicit none
    character, dimension(3,3) :: testBoard
    integer :: testMove
    logical :: testFunction

    call boardSetup (testBoard)

    do
        read (*,100) testMove
100     format (I3)

        if (testMove == 10) exit

        if (same (testBoard(1,1), testBoard(1,2), testBoard (1,3))) exit

        testFunction = chkPlay (testBoard, testMove)
        write (*,*) testFunction

        select case (testMove)
            case (1)
                testBoard(1,1) = "O"
            case (2)
                testBoard(1,2) = "O"
            case (3)
                testBoard(1,3) = "O"
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
    end do

contains

logical function same (t1, t2, t3)
    implicit none
    character :: t1, t2, t3

    if (t1 == "X" .and. t2 == "X" .and. t3 == "X") then
        same = .True.
    else if (t1 == "O" .and. t2 == "O" .and. t3 == "O") then
        same = .True.
    else
        same = .False.
    end if

end function same

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

logical function chkPlay (tictac, move)
    implicit none
    character, dimension(3,3), intent(in) :: tictac
    character :: gridChar
    integer, intent(in) :: move

    select case (move)
        case (1)
            gridChar = tictac(1,1)
        case (2)
            gridChar = tictac(1,2)
        case (3)
            gridChar = tictac(1,3)
        case (4)
            gridChar = tictac(2,1)
        case (5)
            gridChar = tictac(2,2)
        case (6)
            gridChar = tictac(2,3)
        case (7)
            gridChar = tictac(3,1)
        case (8)
            gridChar = tictac(3,2)
        case (9)
            gridChar = tictac(3,3)
    end select

    if (gridChar == " ") then
        chkPlay = .True.
    else
        chkPlay = .False.
    end if

end function chkPlay

end program testChkPlay

