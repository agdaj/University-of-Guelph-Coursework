# easySudoku.py (Python Script)
# Python Sudoku Solver Using Machine Vision
# By: Jireh Agda (0795472)
# Date Last Modified: 2017 04 17
# Tested on Python 3.5.2, Using OpenCV 3.2.0

# Allow system commands
import os

# Allow math functions
import math

# Allow use of OpenCV and NumPy
import cv2
import numpy as np

notExit = True
debug = False    # Toggle to show intermediary steps

print ("Easy Sudoku Solver")
print ("This script accepts image files containing Sudoku boards and solves the Sudoku board,")
print ("displaying the solution in the terminal")

# Loop until 'q' or 'Q' is entered
while notExit:
    print (" ")
    userInput = input("Enter an image file name or (q)uit: ")

    # Open the image is a valid image has been chosen, else error, or quit
    if userInput == 'Q' or userInput == 'q':
        notExit = False
    else:
        # Generically read images as if they were colour, convert to grayscale
        imgC = cv2.imread(userInput,cv2.IMREAD_COLOR)
        cv2.imshow("Initial Image",imgC)
        cv2.waitKey(0)

        imgG = cv2.cvtColor(imgC,cv2.COLOR_BGR2GRAY)
        height, width, channels = imgC.shape

        # Perform Local Adaptive Thresholding with Gaussian window
        th = cv2.adaptiveThreshold(imgG,255,cv2.ADAPTIVE_THRESH_GAUSSIAN_C,cv2.THRESH_BINARY,11,2)

        if debug:
            cv2.imshow("After Threshold", th)
            cv2.waitKey(0)

        # Find the contours in the image -> rectangles of the image -> Sudoku board
        edgesC, contours, hierarchy = cv2.findContours(th,cv2.RETR_TREE,cv2.CHAIN_APPROX_SIMPLE)

        # Out of contours, we are looking for contours that encompass at least 40% of picture
        # i.e. Sudoku board should be main focus
        minArea = height * width * 0.4

        # Default the corners to the image corners in case Sudoku board encompasses entire image
        tlCorner = [0, 0]
        trCorner = [width, 0]
        blCorner = [0, height]
        brCorner = [width, height]

        # Loop over the contours and approximate any 4-vertex polygons -> quadrilaterals
        for c in contours:
            # Calculate area of contour
            cArea = cv2.contourArea(c)

            # Approximate parameters of contours, grab smallest area that is still >40% area
            # Prevents using over-encompassing rectangles as the board, will still not detect
            # Sudoku squares even if board is the entire image
            epsilon = 0.1*cv2.arcLength(c,True)
            approx = cv2.approxPolyDP(c,epsilon,True)
            if (len(approx) == 4 and cArea >= minArea):
                for npArray in approx[0]:
                    tlCorner = npArray
                for npArray in approx[1]:
                    trCorner = npArray
                for npArray in approx[2]:
                    brCorner = npArray
                for npArray in approx[3]:
                    blCorner = npArray

                if debug:
                    imgCCont = imgC.copy()
                    cv2.drawContours(imgCCont, [c], -1, (0, 255, 0), 2)
                    cv2.imshow("After Approx Contour: Square", imgCCont)
                    cv2.waitKey(0)

        if debug:
            imgCCir = imgC.copy()
            cv2.circle(imgCCir, (tlCorner[0], tlCorner[1]), 5, (0,0,255), -1)
            cv2.circle(imgCCir, (trCorner[0], trCorner[1]), 5, (0,0,255), -1)
            cv2.circle(imgCCir, (blCorner[0], blCorner[1]), 5, (0,0,255), -1)
            cv2.circle(imgCCir, (brCorner[0], brCorner[1]), 5, (0,0,255), -1)

            cv2.imshow("After Corner Detection", imgCCir)
            cv2.waitKey(0)

        # Apply perspective transformation on the corners found to a 270x270 (divisible by 9)
        pts1 = np.float32([tlCorner,trCorner,blCorner,brCorner])
        pts2 = np.float32([[0,0],[270,0],[0,270],[270,270]])

        M = cv2.getPerspectiveTransform(pts1,pts2)
        dstC = cv2.warpPerspective(imgC,M,(270,270))

        if debug:
            cv2.imshow ("After Perspective Correction", dstC)
            cv2.waitKey(0)

        # Initialize Sudoku board and threshold comparer board
        sudokuBoard = np.zeros((9,9),dtype=np.int8)
        thBoard = np.zeros((9,9),dtype=np.float32)

        # Then, use trained sets of digit images to match numbers - template matching
        # When matched, place into Sudoku board
        dstG = cv2.cvtColor(dstC,cv2.COLOR_BGR2GRAY)
        dst = cv2.adaptiveThreshold(dstG,255,cv2.ADAPTIVE_THRESH_GAUSSIAN_C,cv2.THRESH_BINARY,11,2)

        for i in range(1,10):
            for j in range(1,5):
                template = cv2.imread('templates/' + str(i) + '-' + str(j) + '.png', cv2.IMREAD_GRAYSCALE)
                thTemp = cv2.adaptiveThreshold(template,255,cv2.ADAPTIVE_THRESH_GAUSSIAN_C,cv2.THRESH_BINARY,11,2)
                w, h = thTemp.shape[::-1]

                res = cv2.matchTemplate(dst,thTemp,cv2.TM_CCOEFF_NORMED)
                threshold = 0.6    # 0.6 threshold was chosen to capture as many numbers but still apply some filtering
                loc = np.where (res >= threshold)
                for pt in zip(*loc[::-1]):
                    locX = pt[0] / 30
                    locY = pt[1] / 30

                    sudokuX = math.floor(pt[0] / 30)
                    sudokuY = math.floor(pt[1] / 30)

                    # To exclude Sudoku board edges from the matching, check rectangles' 'centre of gravity'
                    # Assess whether the match's centre significantly passes a square middle point
                    # Note: pt tracks top-left rectangle point, approx. buffer after is centre
                    edgeEpsilonX = locX - sudokuX
                    edgeEpsilonY = locY - sudokuY
                    epsilonX = 0.6
                    epsilonY = 0.4
                    if (edgeEpsilonX < epsilonX and edgeEpsilonY < epsilonY):
                        # Only fill in a number if the current threshold count is lower than the one found
                        # i.e. pick best fit
                        currRow = thBoard[sudokuY]
                        currTh = currRow[sudokuX]

                        foundRow = res[pt[1]]
                        foundTh = foundRow[pt[0]]

                        if (foundTh > currTh):
                            sudokuBoard[sudokuY][sudokuX] = i
                            thBoard[sudokuY][sudokuX] = foundTh
                            cv2.rectangle(dstC, pt, (pt[0] + w, pt[1] + h), (0,0,255), 2)
                            cv2.putText(dstC, str(i), (int(pt[0] + (w/ 2)), int(pt[1] + (h/ 2))), cv2.FONT_HERSHEY_SIMPLEX,\
                                1, (255, 0, 0), 2)

        if debug:
            cv2.imshow('After Template Matching',dstC)
            cv2.waitKey(0)

        # Finally, send Sudoku board (as a list of numbers) to C program Sudoku solver
        sudokuString = ' '
        for sudokuRow in sudokuBoard:
            for sudokuNum in sudokuRow:
                sudokuString = sudokuString + str(sudokuNum) + ' '

        success = os.system('./easySudokuC' + sudokuString)
