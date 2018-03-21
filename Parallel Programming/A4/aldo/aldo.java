/* aldo.java
 * Name: Jireh Agda (0795472)
 * Date Created: 2016 11 17
 *      Last Modified: 2016 11 29
 * Where's Parallaldo? (Java)
 * Main Class - Checks Arguments, Reads Parallaldos and Submits Thread Work
 */
package aldo;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * aldo - Runs the aldo program to find Parallaldos in Images
 * @author agdaj
 */
public class aldo
{
    /**
     * Constant definition of an arbitrary size of ArrayList
     */
    public static final int ARB_SIZE = 5;
    
    /**
     * @param args the command line arguments
     */
    public static void main (String[] args)
    {
        ArrayList<Parallaldo> aldos;
        long timeStart = System.currentTimeMillis ();
        
        /*First check the arguments of the program*/
        if (checkArgs (args) == false)
        {
            System.out.println ("Run aldo using: aldo aldodir imagedir cores");
            System.exit (-1);
        }
        
        /*Read in the aldos in aldodir*/
        aldos = readAldos (args[0]);
        
        /*Start work here, submitting the aldos and the arguments imagedir and cores*/
        beginSearches (aldos, args[1], Integer.parseInt(args[2]));
        
        /*End with time report*/
        long timeEnd = System.currentTimeMillis();
        long timeDurationMS = timeEnd - timeStart;
        float timeDurationS = (float) timeDurationMS / (float) 1000;
        
        System.out.println ("Total elapsed time (s): " + timeDurationS);  
    }
    
    /*Checks the program arguments for validity and emptiness (of directories)*/
    private static boolean checkArgs (String[] args)
    {
        int numOfArgs = args.length;
        
        if (numOfArgs != 3)
        {
            System.out.println ("Please supply 3 arguments");
            return false;
        }
        
        /*Check validity of each argument*/
        for (int i = 0; i < numOfArgs; i++)
        {
            switch (i)
            {
                /*First two arguments must be directories, must not be empty*/
                case 0: case 1:
                    Path tempPath = Paths.get (args[i]);
                    int directoryElements;
                    
                    if (Files.exists (tempPath, LinkOption.NOFOLLOW_LINKS))
                    {
                        try {
                            directoryElements = (int) Files.list (tempPath).count();
                        }
                        catch (IOException e) {
                            System.out.println ("Invalid directory: " + args[i]);
                            return false;
                        }
                        
                        if (directoryElements < 1)
                        {
                            System.out.println (args[i] + " is empty");
                            return false;
                        }
                    }
                    else
                    {
                        System.out.println ("Invalid path: " + args[i]);
                        return false;
                    }
                    break;
                    
                /*Third argument must be integer >= 1 and <= 16*/
                case 2:
                    int coresToBeUsed;
                    try {
                        coresToBeUsed = Integer.parseInt(args[i]);
                    }
                    catch (NumberFormatException e) {
                        System.out.println ("Invalid parameter for cores (integer)");
                        return false;
                    }
                    
                    if (coresToBeUsed < 1 || coresToBeUsed > 16)
                    {
                        System.out.println ("cores not within 1-16 (inclusive)");
                        return false;
                    }
            }
        }
        
        return true;
    }
    
    /*Reads in all Parallaldos in directoryPath directory, assuming all files are Parallaldos*/
    private static ArrayList<Parallaldo> readAldos (String directoryPath)
    {
        ArrayList<Parallaldo> aldos = null;
        
        /*Read Parallaldos and add to ArrayList aldos to be returned*/
        try {
            Path aldoPath = Paths.get (directoryPath);
            int directoryElements = (int) Files.list (aldoPath).count();
            DirectoryStream<Path> aldoStream = Files.newDirectoryStream (aldoPath); 
            
            aldos = new ArrayList<> (directoryElements);    //assume all elements are parallaldos
            
            for (Path aldoFile: aldoStream)
            {
                aldos.add (new Parallaldo (aldoFile));
            } 
        } catch (Exception e) {
            System.out.println ("Unable to process parallaldos from: " + directoryPath);
            System.exit (-1);
        }
        
        return (aldos);
    }
    
    /*Assign work to Executor Services to find Parallaldos in all Images in imageDirPath (or do work serially here)*/
    private static void beginSearches (ArrayList<Parallaldo> aldos, String imageDirPath, int cores)
    {
        if (cores == 1)
        {
            /*Serial run*/
            ArrayList<Image> images = null;

            /*Read in Image files sequentially (serial)*/
            try {
                Path imagePath = Paths.get (imageDirPath);
                int directoryElements = (int) Files.list (imagePath).count();
                DirectoryStream<Path> imageStream = Files.newDirectoryStream (imagePath); 

                images = new ArrayList<> (directoryElements);
                
                for (Path imageFile: imageStream)
                {
                    images.add (new Image (imageFile));    //assume all are images
                } 
            } catch (Exception e) {
                System.out.println ("Unable to process image from: " + imageDirPath);
                System.exit (-1);
            }

            /*Check aldo sizes in order to help cut the search area*/
            int numOfAldos = aldos.size ();
            ArrayList<Integer> pSizeList = new ArrayList<> (numOfAldos);
            for (int i = 0; i < numOfAldos; i++)
            {
                Parallaldo aldo = aldos.get(i);
                pSizeList.add (aldo.getRows());
                pSizeList.add (aldo.getColumns());
            }

            int largestAldo = Collections.max (pSizeList);
            int smallestAldo = Collections.min (pSizeList);

            int numImageFile = images.size ();

            /*For each Image file, search for Parallaldos*/
            for (int i = 0; i < numImageFile; i++)
            {
                try {
                    Image currentImage = images.get(i);
                    int rowsInImage = currentImage.getRows();
                    int searchArea = rowsInImage - (smallestAldo - 1);
                    int cutDownArea = rowsInImage - (largestAldo - 1); 

                    /*Make calls to Callable that does search work based on Parallaldo, Image, rotation and row on Image*/
                    for (int j = 0; j < searchArea; j++)
                    {
                        for (int k = 0; k < numOfAldos; k++)
                        {
                            Parallaldo currentAldo = aldos.get(k);

                            /*Search area going into region where some aldos don't need to be searched for (can't be found within remaining region)*/
                            if (j >= cutDownArea)
                            {  
                                if ((rowsInImage - j) >= currentAldo.getRows())
                                {
                                    new ParallaldoSearcher (aldos.get(k),currentImage,Parallaldo.ROTATION_0,j).call();
                                    new ParallaldoSearcher (aldos.get(k),currentImage,Parallaldo.ROTATION_180,j).call();
                                }

                                if ((rowsInImage - j) >= currentAldo.getColumns())
                                {
                                    new ParallaldoSearcher (aldos.get(k),currentImage,Parallaldo.ROTATION_90,j).call();
                                    new ParallaldoSearcher (aldos.get(k),currentImage,Parallaldo.ROTATION_270,j).call();
                                }
                            }
                            else 
                            {
                                new ParallaldoSearcher (currentAldo,currentImage,Parallaldo.ROTATION_0,j).call();
                                new ParallaldoSearcher (currentAldo,currentImage,Parallaldo.ROTATION_90,j).call();
                                new ParallaldoSearcher (currentAldo,currentImage,Parallaldo.ROTATION_180,j).call();
                                new ParallaldoSearcher (currentAldo,currentImage,Parallaldo.ROTATION_270,j).call();
                            }
                        }
                    }
                } catch (Exception e) {
                    //nothing happens, continue computation
                }
            }    
        }
        else
        {
            ExecutorService imageReadService = Executors.newFixedThreadPool (cores);    //thread pool to read images
            
            ArrayList<Future<Image>> checkImage = new ArrayList<> (ARB_SIZE);

            /*Submit work to single background thread that reads in Images asynchronously*/
            try {
                Path imagePath = Paths.get (imageDirPath);
                DirectoryStream<Path> imageStream = Files.newDirectoryStream (imagePath); 

                for (Path imageFile: imageStream)
                {
                    checkImage.add (imageReadService.submit (new ImageReader (imageFile)));    //assume all are images
                } 
            } catch (Exception e) {
                System.out.println ("Unable to process image from: " + imageDirPath);
                System.exit (-1);
            }

            /*Check aldo sizes in order to help cut the search area*/
            int numOfAldos = aldos.size ();
            ArrayList<Integer> pSizeList = new ArrayList<> (numOfAldos);
            for (int i = 0; i < numOfAldos; i++)
            {
                Parallaldo aldo = aldos.get(i);
                pSizeList.add (aldo.getRows());
                pSizeList.add (aldo.getColumns());
            }

            int largestAldo = Collections.max (pSizeList);
            int smallestAldo = Collections.min (pSizeList);

            int numImageFile = checkImage.size ();
            ExecutorService searchServices;
            ArrayList<Future<Boolean>> checkIfFound;

            /*For each Image start a new ExecutorService and Future ArrayList*/
            for (int i = 0; i < numImageFile; i++)
            {    
                searchServices = Executors.newFixedThreadPool (cores);    //argument# threads are used for parallaldo searching (CPU-intensive)
                checkIfFound = new ArrayList<> (ARB_SIZE);    //clear and restart Future list

                try {
                    Image currentImage = checkImage.get(i).get();
                    int rowsInImage = currentImage.getRows();
                    int searchArea = rowsInImage - (smallestAldo - 1);
                    int cutDownArea = rowsInImage - (largestAldo - 1); 

                    /*Begin to submit thread work based on Parallaldo, Image, rotation and row on Image*/
                    for (int j = 0; j < searchArea; j++)
                    {
                        for (int k = 0; k < numOfAldos; k++)
                        {
                            Parallaldo currentAldo = aldos.get(k);

                            /*Search going into region where some aldos don't need to be searched for (can't be found within remaining region)*/
                            if (j >= cutDownArea)
                            {  
                                if ((rowsInImage - j) >= currentAldo.getRows())
                                {
                                    checkIfFound.add (searchServices.submit (new ParallaldoSearcher (aldos.get(k),currentImage,Parallaldo.ROTATION_0,j)));
                                    checkIfFound.add (searchServices.submit (new ParallaldoSearcher (aldos.get(k),currentImage,Parallaldo.ROTATION_180,j)));
                                }

                                if ((rowsInImage - j) >= currentAldo.getColumns())
                                {
                                    checkIfFound.add (searchServices.submit (new ParallaldoSearcher (aldos.get(k),currentImage,Parallaldo.ROTATION_90,j)));
                                    checkIfFound.add (searchServices.submit (new ParallaldoSearcher (aldos.get(k),currentImage,Parallaldo.ROTATION_270,j)));
                                }
                            }
                            else 
                            {
                                checkIfFound.add (searchServices.submit (new ParallaldoSearcher (currentAldo,currentImage,Parallaldo.ROTATION_0,j)));
                                checkIfFound.add (searchServices.submit (new ParallaldoSearcher (currentAldo,currentImage,Parallaldo.ROTATION_90,j)));
                                checkIfFound.add (searchServices.submit (new ParallaldoSearcher (currentAldo,currentImage,Parallaldo.ROTATION_180,j)));
                                checkIfFound.add (searchServices.submit (new ParallaldoSearcher (currentAldo,currentImage,Parallaldo.ROTATION_270,j)));
                            }
                        }
                    }

                    /*If Exception (indicating search is finished) is not thrown, introduce alternate method for searching if found*/
                    int numSearches = checkIfFound.size ();
                    for (int j = 0; j < numSearches; j++)
                    {
                        if (Objects.equals(checkIfFound.get(j).get(), Boolean.TRUE))
                        {
                            j = numSearches;
                        } 
                    }

                    searchServices.shutdownNow();    //shutdown the pool once all have returned FALSE if ever
                } catch (InterruptedException | ExecutionException e) {
                    searchServices.shutdownNow();    //immediately shutdown the pool to stop other threads once Parallaldo is found
                }
            }

            imageReadService.shutdown();
        }
    }
}
