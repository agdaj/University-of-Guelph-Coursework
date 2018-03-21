! eightQall.f95
! Eight Queens Problem Solution Printing
! By: Jireh Agda (0795472)
! Date Last Modified: 2016 04 06

program eightQall
    implicit none

    integer :: k
    logical, dimension(1:8) :: columnCheck
    logical, dimension(2:16) :: dCheckOne
    logical, dimension(-7:7) :: dCheckTwo
    integer, dimension(1:8) :: columnList

    ! Initialize all logicals to .true.
    do k = 1, 8
        columnCheck(k) = .true.
    end do
    do k = 2, 16
        dCheckOne(k) = .true.
    end do
    do k = -7, 7
        dCheckTwo(k) = .true.
    end do

    k = 1
    call tryEightQueens (k, columnCheck, dCheckOne, dCheckTwo, columnList)

contains

! tryEightQueens: find Eight Queens solutions
! ===========================================
recursive subroutine tryEightQueens (i, columnCheck, dCheckOne, dCheckTwo, columnList)
    implicit none

    integer, intent(in out) :: i
    logical, dimension(1:8), intent(in out) :: columnCheck
    logical, dimension(2:16), intent(in out) :: dCheckOne
    logical, dimension(-7:7), intent(in out) :: dCheckTwo
    integer, dimension(1:8), intent(in out) :: columnList
    integer :: j, k

    ! Create an array of integers that indicate a queen's column placement for each row
    do j = 1, 8
        if (columnCheck(j) .and. dCheckOne(i+j) .and. dCheckTwo(i-j)) then
            columnList(i) = j
            columnCheck(j) = .false.
            dCheckOne(i+j) = .false.
            dCheckTwo(i-j) = .false.

            if (i < 8) then
                ! Recursively call the function to find all the solutions
                k = i + 1
                call tryEightQueens (k, columnCheck, dCheckOne, dCheckTwo, columnList)
            else
                call printSolution (columnList)
            end if

            columnCheck(j) = .true.
            dCheckOne(i+j) = .true.
            dCheckTwo(i-j) = .true.
        end if
    end do

    return
end subroutine tryEightQueens

! printSolution: Prints an Eight Queens solution to queensF.txt
! =============================================================
subroutine printSolution (columnList)
    implicit none

    integer, dimension(1:8), intent(in) :: columnList

    character(len=13), parameter :: SOLUTION_FILE = "./queensF.txt"
    character, dimension(2:17) :: printList
    logical :: fileExists
    integer :: i, j

    ! Print solution number with each solution
    integer, save :: solutionNum = 0

    solutionNum = solutionNum + 1

    ! Opens queensF.txt if it exists, else program makes it
    inquire (file=SOLUTION_FILE, exist=fileExists)
    if (fileExists) then
        open (unit=10, file=SOLUTION_FILE, status='old', action='write', position='append') 
    else
        open (unit=10, file=SOLUTION_FILE, status='new', action='write')
    end if

    write (10,*) ( solutionNum )

    ! Loop sets an array of characters that is to be printed to the solution file
    ! and prints to the file
    do i = 1, 8
        do j = 1, 8
            if (j == columnList(i)) then
                printList(2*j) = 'Q'
                printList(2*j+1) = ' '
            else
                printList(2*j) = '.'
                printList(2*j+1) = ' '
            end if
        end do

        write (10,*) ( printList(j), j = 2,17 )
    end do

    write (10,*) " "
    close (10, status='keep')

    return
end subroutine printSolution

end program eightQall
