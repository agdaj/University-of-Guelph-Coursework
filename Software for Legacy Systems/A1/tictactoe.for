C A PROGRAM TO PLAY TIC-TAC-TOE
      PROGRAM TICTACTOE
      
      CHARACTER * 1 TICTAC(3,3), WINNER
      LOGICAL OVER
      LOGICAL CHKPLAY
      INTEGER MOVE, TURN

      WRITE(*,*) "PLAY TIC-TAC-TOE. ENTER 1-9 TO PLAY"
      WRITE(*,*) " "
      WRITE(*,*) "        1 | 2 | 3 "
      WRITE(*,*) "       ---+---+---"
      WRITE(*,*) "        4 | 5 | 6 "
      WRITE(*,*) "       ---+---+---"
      WRITE(*,*) "        7 | 8 | 9 "
      WRITE(*,*) " "
            
      CALL BOARDSETUP(TICTAC)
      
   10 TURN = 0
      WRITE(*,*) "Your move? "
      READ(*,*) MOVE
      IF (MOVE .GT. 0 .AND. MOVE .LE. 9) GO TO 11
      WRITE(*,*) "Invalid input."
      GO TO 10
   11 IF (CHKPLAY(TICTAC,MOVE)) GO TO 12
      WRITE(*,*) "Invalid move, box already occupied."
      GO TO 10
   12 IF (MOVE .EQ. 1) TICTAC(1,1) = "X"
      IF (MOVE .EQ. 2) TICTAC(1,2) = "X"
      IF (MOVE .EQ. 3) TICTAC(1,3) = "X"
      IF (MOVE .EQ. 4) TICTAC(2,1) = "X"
      IF (MOVE .EQ. 5) TICTAC(2,2) = "X"
      IF (MOVE .EQ. 6) TICTAC(2,3) = "X"
      IF (MOVE .EQ. 7) TICTAC(3,1) = "X"
      IF (MOVE .EQ. 8) TICTAC(3,2) = "X"
      IF (MOVE .EQ. 9) TICTAC(3,3) = "X"
      
   14 IF (TURN .EQ. 0) WRITE(*,*) "After your move..."
      IF (TURN .EQ. 1) WRITE(*,*) "After my move..."
      DO 20 I=1,3
      WRITE(*,400) (TICTAC(I,J), J=1,3)
  400 FORMAT(2X,A1,1X,"|",1X,A1,1X,"|",1X,A1,1X)
      GO TO (15,15,20) I
   15 WRITE(*,*) "---+---+---"
   20 CONTINUE
      IF (TURN .EQ. 1) GOTO 16
      
      CALL CHKOVR(TICTAC,OVER,WINNER)
      IF (OVER) GOTO 30
      
      TURN = 1
      CALL COMPMOVE(TICTAC)
      GOTO 14
   16 CALL CHKOVR(TICTAC,OVER,WINNER)
      IF (OVER) GOTO 30           
      GOTO 10
      
   30 WRITE(*,*) "The game is over!"
      IF (WINNER .EQ. "D") THEN
      WRITE(*,*) "The game is a draw. "
      ELSE
      WRITE(*,*) "The winner is: ", WINNER
      END IF      
      STOP 
      END
      
C SUBROUTINE TO CHECK TO SEE IF THE GAME IS OVER      
C =========================================
      SUBROUTINE CHKOVR(TICTAC,OVER,WINNER)
      CHARACTER * 1 TICTAC(3,3), WINNER
      LOGICAL OVER
      
      CHARACTER * 1 BLANK, DRAW
      PARAMETER (BLANK = ' ', DRAW = 'D')

      LOGICAL SAME
      LOGICAL DSAME
      INTEGER IR, IC

C ASSUME GAME IS OVER AT START
      OVER = .TRUE.
C
C CHECK FOR A WINNER
C CHECK ROWS FOR A WINNER
      DO 100 IR = 1, 3
      IF (SAME(TICTAC(IR,1),TICTAC(IR,2),TICTAC(IR,3))) THEN
      WINNER = TICTAC(IR,1)
      RETURN
      END IF
  100 CONTINUE
C NO WINNER BY ROWS, CHECK COLUMNS FOR A WINNER
      DO 110 IC = 1, 3
      IF (SAME(TICTAC(1,IC),TICTAC(2,IC),TICTAC(3,IC))) THEN
      WINNER = TICTAC(1,IC)
      RETURN
      END IF
  110 CONTINUE 
C NO WINNER BY ROWS OR COLUMNS, CHECK DIAGONALS
      DSAME = SAME(TICTAC(1,1),TICTAC(2,2),TICTAC(3,3)) 
     +   .OR. SAME(TICTAC(1,3),TICTAC(2,2),TICTAC(3,1)) 
      IF (DSAME) THEN
      WINNER = TICTAC(2,2)
      RETURN
      END IF
C NO WINNER AT ALL. SEE IF GAME IS A DRAW
C CHECK EACH ROW FOR AN EMPTY SPACE
      DO 140 IR = 1,3
      DO 145 IC = 1,3
      IF (TICTAC(IR,IC) .EQ. BLANK) THEN
      OVER = .FALSE.
      RETURN
      END IF
  145 CONTINUE
  140 CONTINUE
C 
C NO BLANK FOUND, GAME IS A DRAW
      WINNER = DRAW

      RETURN    
      END
      
C SUBROUTINE TO PLAY FOR THE COMPUTER  
C =========================================
      SUBROUTINE COMPMOVE(TICTAC)
      CHARACTER * 1 TICTAC(3,3)
      INTEGER PATHS(3,8), PATHSUM(8)
      DATA PATHS/1,2,3,4,5,6,7,8,9,
     +           1,4,7,2,5,8,3,6,9,
     +           1,5,9,3,5,7/
      INTEGER BOARD(9,2), K, X, Y, RANDPOS
      DATA BOARD/1,1,1,2,2,2,3,3,3,1,2,3,1,2,3,1,2,3/

      
C     YOUR CODE GOES HERE 

C     CALCULATE THE PATHSUMS
      DO 150 I = 1,8
      PATHSUM(I) = 0
      DO 149 J = 1,3
      X = BOARD(PATHS(J,I),1)
      Y = BOARD(PATHS(J,I),2)
      IF (TICTAC(X,Y) .EQ. " ") K = 0
      IF (TICTAC(X,Y) .EQ. "X") K = 1
      IF (TICTAC(X,Y) .EQ. "O") K = 4 
      PATHSUM(I) = PATHSUM(I) + K     
  149 CONTINUE
  150 CONTINUE 

C     OFFENSIVE CODE TO DEAL WITH SCENARIOS WHERE THE
C     COMPUTER HAS TWO IN A PATH
      DO 155 I = 1,8
      IF (PATHSUM(I) .EQ. 8) THEN
      DO 154 J = 1,3
      X = BOARD(PATHS(J,I),1)
      Y = BOARD(PATHS(J,I),2)
      IF (TICTAC(X,Y) .EQ. " ") THEN
      TICTAC(X,Y) = "O"
      RETURN
      END IF
  154 CONTINUE
      END IF
  155 CONTINUE
  
C     DEFENSIVE CODE TO DEAL WITH SCENARIOS WHERE THE
C     OPPONENT HAS TWO IN A PATH
      DO 160 I = 1,8
      IF (PATHSUM(I) .EQ. 2) THEN
      DO 159 J = 1,3
      X = BOARD(PATHS(J,I),1)
      Y = BOARD(PATHS(J,I),2)
      IF (TICTAC(X,Y) .EQ. " ") THEN
      TICTAC(X,Y) = "O"
      RETURN
      END IF
  159 CONTINUE
      END IF
  160 CONTINUE
  
  170 RANDPOS = INT(RAND(0)*9)+1
      X = BOARD(RANDPOS,1)
      Y = BOARD(RANDPOS,2)
      IF (TICTAC(X,Y) .EQ. " ") THEN
          TICTAC(X,Y) = "O"
          RETURN
      END IF
      GO TO 170
  
      RETURN    
      END

C FUNCTION TO CHECK TO SEE IF THREE ELEMENTS IN A ROW, COLUMN OR DIAGONAL
C ARE THE SAME           
C =========================================
      LOGICAL FUNCTION SAME(T1,T2,T3)
      CHARACTER T1,T2,T3
      
      IF (T1 .EQ. "X" .AND. T2 .EQ. "X" .AND. T3 .EQ. "X") GOTO 200      
      IF (T1 .EQ. "O" .AND. T2 .EQ. "O" .AND. T3 .EQ. "O") GOTO 200      
      SAME = .FALSE.
      GOTO 210
  200 SAME = .TRUE.
  210 END
  
C SUBROUTINE TO SET UP THE TIC-TAC-TOE BOARD  
C =========================================  
      SUBROUTINE BOARDSETUP(TICTAC)
      CHARACTER * 1 TICTAC(3,3)

      DO 310 I = 1,3
      DO 300 J = 1,3
      TICTAC(I,J) = " "
  300 CONTINUE
  310 CONTINUE
      RETURN
      END

C SUBROUTINE TO CHECK HUMAN PLAY  
C ========================================= 
      LOGICAL FUNCTION CHKPLAY(TICTAC,MOVE) 
      CHARACTER * 1 TICTAC(3,3)
      INTEGER MOVE
                
      GO TO (401,402,403,404,405,406,407,408,409) MOVE
  401 IF (TICTAC(1,1) .EQ. " ") GOTO 411
      GO TO 410
  402 IF (TICTAC(1,2) .EQ. " ") GOTO 411
      GO TO 410
  403 IF (TICTAC(1,3) .EQ. " ") GOTO 411
      GO TO 410
  404 IF (TICTAC(2,1) .EQ. " ") GOTO 411
      GO TO 410
  405 IF (TICTAC(2,2) .EQ. " ") GOTO 411
      GO TO 410
  406 IF (TICTAC(2,3) .EQ. " ") GOTO 411
      GO TO 410
  407 IF (TICTAC(3,1) .EQ. " ") GOTO 411
      GO TO 410
  408 IF (TICTAC(3,2) .EQ. " ") GOTO 411
      GO TO 410
  409 IF (TICTAC(3,3) .EQ. " ") GOTO 411
  410 CHKPLAY = .FALSE.
      GOTO 412
  411 CHKPLAY = .TRUE.
  412 END