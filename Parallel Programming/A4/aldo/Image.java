/* Image.java
 * Name: Jireh Agda (0795472)
 * Date Created: 2016 11 25
 *      Last Modified: 2016 11 28
 * Where's Parallaldo? (Java)
 * Image Class - Stores Image from a given Path to be Searched for Parallaldos
 */
package aldo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Image - Stores Image from a given Path to be Searched for Parallaldos
 * @author agdaj
 */
public class Image
{
    private final Path pathName;
    private final String fileName;
    
    private int rows, columns;
    private ArrayList<String> iStrings;
    
    /**
     * Constructs Image at given Path
     * @param pathName Path to Image file
     */
    public Image (Path pathName)
    {
        this.pathName = pathName;
        fileName = this.pathName.getFileName().toString();
        
        readIFile ();
    }
    
    /*Reads in and stores the Image file strings and details*/
    private void readIFile ()
    {
        Scanner iReader = null;
        String fileString;
        
        try {
            iReader = new Scanner (new FileInputStream (pathName.toString ()));
        }
        catch (FileNotFoundException e) {
            System.out.println ("Cannot find " + pathName.toString());
            System.exit (-1);    //shut down all work if image file cannot be opened
        }
        
        /*Reads dimensions first - ASSUMES CORRECT FORMAT*/
        rows = iReader.nextInt ();
        columns = iReader.nextInt ();
        iReader.nextLine ();

        iStrings = new ArrayList<> (rows);
        
        /*Read and store the Image's strings*/
        for (int i = 0; i < rows; i++)
        {
            fileString = iReader.nextLine ();
            iStrings.add (fileString);
        }
    }
    
    /**
     * Returns the number of rows in Image
     * @return the number of rows in Image
     */
    public int getRows ()
    {
        return (rows);
    }
    
    /**
     * Returns the number of columns in Image
     * @return the number of columns in Image
     */
    public int getColumns ()
    {
        return (columns);
    }
    
    /**
     * Returns the String of Image at specified row
     * @param row row of String to return
     * @return String of Image at specified row
     */
    public String getStringRow (int row)
    {
        return (iStrings.get(row));
    }
    
    /**
     * Returns String representation of Image as its file name
     * @return String representation of Image as its file name
     */
    @Override
    public String toString ()
    {
        return (fileName);
    }
}
