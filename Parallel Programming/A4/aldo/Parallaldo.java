/* Parallaldo.java
 * Name: Jireh Agda (0795472)
 * Date Created: 2016 11 17
 *      Last Modified: 2016 11 28
 * Where's Parallaldo? (Java)
 * Parallaldo Class - Stores Parallaldo from a given Path
 */
package aldo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Parallaldo - Stores Parallaldo from a given Path
 * @author agdaj
 */
public class Parallaldo
{
    /**
     * Constant to specify Parallaldo's rotation @ 0 degrees clockwise
     */
    public static final int ROTATION_0 = 0;
    /**
     * Constant to specify Parallaldo's rotation @ 90 degrees clockwise
     */
    public static final int ROTATION_90 = 1;
    /**
     * Constant to specify Parallaldo's rotation @ 180 degrees clockwise
     */
    public static final int ROTATION_180 = 2;
    /**
     * Constant to specify Parallaldo's rotation @ 270 degrees clockwise
     */
    public static final int ROTATION_270 = 3;
    
    private final Path pathName;
    private final String fileName;
    
    private int rows, columns;
    private ArrayList<String> pStrings0;
    private ArrayList<String> pStrings90;
    private ArrayList<String> pStrings180;
    private ArrayList<String> pStrings270;
    
    /**
     * Constructs Parallaldo at given Path
     * @param pathName Path to Parallaldo file
     */
    public Parallaldo (Path pathName)
    {
        this.pathName = pathName;
        fileName = this.pathName.getFileName().toString();
        
        readPFile ();
    }
    
    /*Reads in and stores the Parallaldo file strings and details*/
    private void readPFile ()
    {
        Scanner pReader = null;
        String fileString;
        
        try {
            pReader = new Scanner (new FileInputStream (pathName.toString ()));
        }
        catch (FileNotFoundException e) {
            System.out.println ("Cannot find " + pathName.toString());
            System.exit (-1);    //shut down all work if parallaldo file cannot be opened
        }
        
        /*Reads dimensions first - ASSUMES CORRECT FORMAT*/
        rows = pReader.nextInt ();
        columns = pReader.nextInt ();
        pReader.nextLine ();

        pStrings0 = new ArrayList<> (rows);
        
        /*Read and store the Parallaldo's strings*/
        for (int i = 0; i < rows; i++)
        {
            fileString = pReader.nextLine ();
            pStrings0.add (fileString);
        }

        /*Store rotated versions of the same Parallaldo Strings (clockwise)*/
        pStrings90 = parallaldoRotator (pStrings0);
        pStrings180 = parallaldoRotator (pStrings90);
        pStrings270 = parallaldoRotator (pStrings180);
    }
    
    /*Accepts a set of Parallaldo Strings and returns a set of Parallaldo Strings rotated 90° clockwise*/
    private ArrayList<String> parallaldoRotator (ArrayList<String> normalAldo)
    {
        int rowsWithRotate = normalAldo.get(0).length ();
        int columnsWithRotate = normalAldo.size ();
        
        ArrayList<String> rotatedAldo = new ArrayList<> (rowsWithRotate);
        
        /*Create a new rotated String*/
        for (int i = 0; i < rowsWithRotate; i++)
        {
            String newString = ""; 
            
            /*Concatenate elements of the original Parallaldo to the rotated String*/
            for (int j = columnsWithRotate - 1; j >= 0; j--)
            {
                newString = newString.concat (Character.toString (normalAldo.get(j).charAt(i)));
            }
            
            rotatedAldo.add (newString);
        }
        
        return (rotatedAldo);
    }  

    /**
     * Returns the number of rows in Parallaldo
     * @return the number of rows in Parallaldo
     */
    public int getRows ()
    {
        return (rows);
    }
    
    /**
     * Returns the number of columns in Parallaldo
     * @return the number of columns in Parallaldo
     */
    public int getColumns ()
    {
        return (columns);
    }
    
    /**
     * Returns the String of Parallaldo at specified row and rotation
     * @param row row of String to return
     * @param rotation rotation of Parallaldo - use defined constants
     * @return String of Parallaldo at specified row and rotation
     */
    public String getStringRow (int row, int rotation)
    {
        switch (rotation)
        {
            case ROTATION_0:
                return (pStrings0.get(row));
                
            case ROTATION_90:
                return (pStrings90.get(row));
                
            case ROTATION_180:
                return (pStrings180.get(row));
                
            case ROTATION_270:
                return (pStrings270.get(row));
                
            default:
                return (null);
        }
    }
    
    /**
     * Returns String representation of Parallaldo as its file name
     * @return String representation of Parallaldo as its file name
     */
    @Override
    public String toString ()
    {
        return (fileName);
    }
}
