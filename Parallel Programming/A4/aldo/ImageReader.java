/* ImageReader.java
 * Name: Jireh Agda (0795472)
 * Date Created: 2016 11 26
 *      Last Modified: 2016 11 28
 * Where's Parallaldo? (Java)
 * ImageReader Class - Reads and Returns Image from a given Path (Threadable)
 */
package aldo;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * ImageReader - Reads and Returns Image from a given Path (Threadable)
 * @author agdaj
 */
public class ImageReader implements Callable<Image>
{
    private final Path imagePath;
    
    /**
     * Constructs ImageReader that can return an Image at given Path
     * @param imagePath Path to Image file
     */
    public ImageReader (Path imagePath)
    {
        this.imagePath = imagePath;
    }

    /**
     * Creates and returns an Image file that Parallaldos can be searched on
     * @return Image that Parallaldos can be searched on
     * @throws Exception if Image reading and storing fails
     */
    @Override
    public Image call() throws Exception
    {
        return (new Image (imagePath));
    } 
}
