/* ParallaldoSearcher.java
 * Name: Jireh Agda (0795472)
 * Date Created: 2016 11 26
 *      Last Modified: 2016 11 28
 * Where's Parallaldo? (Java)
 * ParallaldoSearcher Class - Searches Parallaldo in Image at given rotation and row
 */
package aldo;

import java.util.concurrent.Callable;

/**
 * ParallaldoSearcher - Searches Parallaldo in Image at given rotation and row
 * @author agdaj
 */
public class ParallaldoSearcher implements Callable<Boolean>
{
    private final Parallaldo aldo;
    private final Image image;
    private final int rotation;
    private final int row;
    
    /**
     * Constructs ParallaldoSearacher that searches the given Parallaldo in the given Image at the given rotation and image row
     * @param aldo Parallaldo to search
     * @param image Image to search in
     * @param rotation Parallaldo rotation to use
     * @param row Image row to search at
     */
    public ParallaldoSearcher (Parallaldo aldo, Image image, int rotation, int row)
    {
        this.aldo = aldo;
        this.image = image;
        this.rotation = rotation;
        this.row = row;
    }

    /**
     * Returns whether the given Parallaldo is found in the Image at the given Image row in the given Parallaldo rotation
     * @return Boolean.FALSE if Parallaldo not found
     * @throws Exception if Parallaldo is found
     */
    @Override
    public Boolean call() throws Exception
    {
        return (searchImage ());
    }
    
    /*Executes the main work that searches the Image for the Parallaldo*/
    private Boolean searchImage () throws Exception
    {
        boolean isMatch = false;
        int matchWhere = 0;
        int parallaldoRows;
        
        /*Set the search area based on Parallaldo rotation*/
        if (rotation == Parallaldo.ROTATION_0 || rotation == Parallaldo.ROTATION_180)
        {
            parallaldoRows = aldo.getRows();
        }
        else
        {
            parallaldoRows = aldo.getColumns();
        }
        
        /*Repeatedly search within the Image row for any matching substring to start looking for the full Parallaldo*/
        while (isMatch == false)
        {
            /*If the substring is matchable, continue (loop can be repeated if multiple instances of substring is within the row and previous matches fail*/
            matchWhere = image.getStringRow(row).indexOf(aldo.getStringRow(0, rotation),matchWhere + 1);
            if (matchWhere < 0) return (Boolean.FALSE);

            /*If all subsequent rows match, loop is exitable*/
            for (int i = 1; i < parallaldoRows; i++)
            {
                if (image.getStringRow(row + i).startsWith(aldo.getStringRow(i, rotation), matchWhere) == false)
                {
                    i = parallaldoRows;
                    isMatch = false;
                }
                else
                {
                    isMatch = true;
                }
            }
        }
        
        int yValue;
        int xValue;
        int rotationValue;
        
        /*Set the print values based on top-left Parallaldo coordinate*/
        switch (rotation)
        {
            case Parallaldo.ROTATION_0:
                yValue = row + 1;
                xValue = matchWhere + 1;
                rotationValue = 0;
                break;
                
            case Parallaldo.ROTATION_90:
                yValue = row + 1;
                xValue = matchWhere + aldo.getRows ();
                rotationValue = 90;
                break;
                
            case Parallaldo.ROTATION_180:
                yValue = row + aldo.getRows ();
                xValue = matchWhere + aldo.getColumns();
                rotationValue = 180;
                break;
                
            case Parallaldo.ROTATION_270:
                yValue = row + aldo.getColumns();
                xValue = matchWhere + 1;
                rotationValue = 270;
                break;
                
            default:
                yValue = 0;
                xValue = 0;
                rotationValue = 0;
        }
        
        System.out.println ("$" + aldo.toString() + " " + image.toString() + " (" + yValue + "," + xValue +
                "," + rotationValue + ")");
        throw new Exception();    //throw an exception to signal that a match has been found
    }
}
