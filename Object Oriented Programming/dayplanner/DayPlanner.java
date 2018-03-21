/* 
 * DayPlanner.java
 * By: Jireh Agda for CIS*2430
 */
package dayplanner; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import java.io.PrintWriter;

/**
 * DayPlanner maintains a list of Activities (HomeActivity, SchoolActivity or OtherActivity) to keep track of activities the user adds.
 * The user can add Activities and search for Activities that the user has added to help organize their day.
 * The user may also import a file to add Activities into the DayPlanner, and export the Activities into an output file.
 * @author agdaj
 */
public class DayPlanner
{
    private ArrayList<Activity> activityList;
    private HashMap<String, ArrayList<Integer>> activityMap;
    
    /**
     * Creates an empty DayPlanner object that can be filled with Activities
     */
    public DayPlanner ()
    {
        activityList = new ArrayList<Activity> (10);     
        activityMap = new HashMap<String, ArrayList<Integer>> (10);
    }
    
    /**
     * Adds an Activity (HomeActivity, SchoolActivity or OtherActivity) to the DayPlanner
     * @param newActivity an Activity to be added
     */
    public void addToPlanner (Activity newActivity)
    {
        activityList.add (newActivity);
        
        Integer activityIndex = activityList.lastIndexOf (newActivity);
        ArrayList<Integer> setNewEntry = null;
        String fullTitle = newActivity.getTitle ();
        
        if (fullTitle.length () == 0)    //adds/updates any 'no title' entries into HashMap
        {
            boolean isThereBlank = activityMap.containsKey (fullTitle);
            if (isThereBlank == false)
            {
                setNewEntry = new ArrayList<Integer> ();
                activityMap.put (fullTitle, setNewEntry);
            }
            else
            {
                setNewEntry = activityMap.get (fullTitle);
            }
            
            setNewEntry.add (activityIndex);
        }
        else    //adds/updates title tokens into HashMap
        {
            String[] splitTitle = fullTitle.split (" ");
            
            for (String titleKey : splitTitle) 
            {
                titleKey = titleKey.toLowerCase ();
                
                boolean isThereKey = activityMap.containsKey (titleKey);
                if (isThereKey == false)
                {
                    setNewEntry = new ArrayList<Integer> ();
                    activityMap.put (titleKey, setNewEntry);
                }
                else
                {
                    setNewEntry = activityMap.get (titleKey);
                }
                
                setNewEntry.add (activityIndex);
            }
        }
    }
    
    /**
     * Searches DayPlanner for any activity that matches all the given parameters and prints it out to an ArrayList of Strings
     * @param titleKeywords String to be delimited by ' ' to find words that match within an activity's title;
     * If null, any title is considered a match
     * @param startSearchTime starting Time of search period to be searched within;
     * If null, any start time is considered a match
     * @param stopSearchTime ending Time of search period to be searched within;
     * If null, any stop time is considered a match
     * @param activityType String of which Activity type to search (Home, School, Other);
     * If null, all three arrays are searched. If anything else, the method immediately returns with no search results
     * @return ArrayList of Strings of Activities information if there are any matches, otherwise ArrayList of messages indicating no match
     */
    public ArrayList<String> searchMatches (String titleKeywords, Time startSearchTime, Time stopSearchTime, String activityType)
    {   
        ArrayList<String> searchMessage = new ArrayList<String> ();
        
        if (activityType == null || activityType.equalsIgnoreCase ("Home") || activityType.equalsIgnoreCase ("School") || activityType.equalsIgnoreCase ("Other"))
        {
            ArrayList<Integer> titleMatches;
            
            if (titleKeywords == null)
            {
                titleMatches = new ArrayList<Integer> ();
                
                for (int i = 0; i < activityList.size (); i ++)    //all entries pass this stage of the search
                {
                    titleMatches.add (i);
                }
            }
            else
            {
                String[] splitTitle = titleKeywords.split (" ");
                if (splitTitle.length == 1)    //grabs a key-value pair if there is one, else return
                {
                    titleKeywords = titleKeywords.toLowerCase ();
                    
                    boolean isThereKey = activityMap.containsKey (titleKeywords);
                    if (isThereKey == true)
                    {
                        titleMatches = activityMap.get (titleKeywords);
                    }
                    else
                    {
                        searchMessage.add ("There are no Activities that Match your Title Parameter");
                        return searchMessage;
                    }
                }
                else
                {
                    ArrayList<ArrayList<Integer>> keywordMatches = new ArrayList<ArrayList<Integer>> ();

                    for (String titleKey : splitTitle)     //for each token a key-value pair is found if any and sets them into an array of arrays to find intersection later, else return
                    {
                        titleKey = titleKey.toLowerCase ();

                        boolean isThereKey = activityMap.containsKey (titleKey);
                        if (isThereKey == true)
                        {
                            ArrayList<Integer> oneMatch = activityMap.get (titleKey);
                            keywordMatches.add (oneMatch);
                        }
                        else
                        {
                            searchMessage.add ("There are no Activities that Match your Title Parameter");
                            return searchMessage;
                        }

                    }
                    
                    titleMatches = intersectionFinder (keywordMatches);
                }
            }
            
            ArrayList<Integer> matchWithTime = new ArrayList<Integer> ();

            for (Integer titleActivityMatch : titleMatches)    //checks time period of each activity if within search criteria
            {
                Time compareStart = activityList.get (titleActivityMatch).getStartTime ();
                Time compareStop = activityList.get (titleActivityMatch).getStopTime ();

                boolean inTimePeriod = isInTimePeriod (startSearchTime, stopSearchTime, compareStart, compareStop);
                if (inTimePeriod == true)
                {
                    matchWithTime.add (titleActivityMatch);
                }
            }
            
            if (matchWithTime.isEmpty () == true)
            {
                searchMessage.add ("There are no Activities that Match your Time Parameter");
                return searchMessage;
            }
            
            ArrayList<Integer> finalMatch = new ArrayList<Integer> ();
            
            if (activityType == null)
            {
                finalMatch = matchWithTime;
            }
            else if (activityType.equalsIgnoreCase ("Home"))
            { 
                Activity currentMatch;
                
                for (Integer timeMatch : matchWithTime)
                {
                    currentMatch = activityList.get (timeMatch);
                    
                    if (currentMatch instanceof HomeActivity)    //classes are compared as the final step for search
                    {
                        finalMatch.add (timeMatch);
                    }
                }
            }
            else if (activityType.equalsIgnoreCase ("School"))
            {          
                Activity currentMatch;
                
                for (Integer timeMatch : matchWithTime)
                {
                    currentMatch = activityList.get (timeMatch);
                    
                    if (currentMatch instanceof SchoolActivity)    //classes are compared as the final step for search
                    {
                        finalMatch.add (timeMatch);
                    }
                }
            }
            else if (activityType.equalsIgnoreCase ("Other"))
            {
                Activity currentMatch;
                
                for (Integer timeMatch : matchWithTime)
                {
                    currentMatch = activityList.get (timeMatch);
                    
                    if (currentMatch instanceof OtherActivity)    //classes are compared as the final step for search
                    {
                        finalMatch.add (timeMatch);
                    }
                }
            }
            
            searchMessage.add ("There is/are " + finalMatch.size () + " Activity Match(es) with your Search Parameters in the DayPlanner:\n");
            for (Integer fullMatch : finalMatch)    //all matches are printed here
            {
                searchMessage.add (activityList.get (fullMatch).toString ());
                searchMessage.add ("\n");
            }
        }
        else
        {
            searchMessage.add ("Invalid activityType Parameter - No Search Is Done");
        }
        
        return searchMessage;
    }
    
    private static ArrayList<Integer> intersectionFinder (ArrayList<ArrayList<Integer>> indicesList)
    {
        ArrayList<Integer> intersection = new ArrayList<Integer> ();
        ArrayList<Integer> tempArray = indicesList.get (0);
        
        for (ArrayList<Integer> oneList : indicesList)    //each ArrayList compares its list with the current tempArray and adds matches to intersection
        {
            for (Integer oneIndex : oneList)
            {
                if (tempArray.contains (oneIndex) == true)
                {
                    intersection.add (oneIndex);
                }
            }
            
            tempArray = copyArray (intersection);
            intersection = new ArrayList<Integer> ();
        }
        
        return tempArray;
    }
    
    private static ArrayList<Integer> copyArray (ArrayList<Integer> toBeCopied)
    {
        ArrayList<Integer> newCopy = new ArrayList<Integer> ();
        
        for (Integer toCopy : toBeCopied)
        {
            newCopy.add (toCopy);
        }
        
        return newCopy;
    }
    
    private static boolean isInTimePeriod (Time startTimeSearch, Time stopTimeSearch, Time activityStart, Time activityStop)
    {
        if (startTimeSearch == null && stopTimeSearch == null)
        {
            return true;    //if there is no specified time period to search within, the activity is assumed to be correct for the search
        }
        else if (startTimeSearch != null && stopTimeSearch == null)
        {
            if (Time.compareTimes (startTimeSearch, activityStart) == 0 || Time.compareTimes (startTimeSearch, activityStart) == -1)
            {
                return true;    //activities with a start time that falls after the searched for start time are correct for the search

            }
            else
            {
                return false;
            }
        }
        else if (startTimeSearch == null && stopTimeSearch != null)
        {
            if (Time.compareTimes (activityStop, stopTimeSearch) == 0 || Time.compareTimes (activityStop, stopTimeSearch) == -1)
            {
                return true;    //activities with a stop time that falls befire the searched for stop time are correct for the search
            }
            else
            {
                return false;
            }
        }   
        else
        {                            
            if (Time.compareTimes (startTimeSearch, activityStart) == 0 || Time.compareTimes (startTimeSearch, activityStart) == -1)
            {
                if (Time.compareTimes (activityStop, stopTimeSearch) == 0 || Time.compareTimes (activityStop, stopTimeSearch) == -1)
                {
                    return true;    //activities that take place within the specified time period are correct for the search
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }      
        }  
    }
    
    /**
     * Loads a list of Activities into the DayPlanner from a file input stream;
     * The file contents must follow the following format to be read properly: 
     * activityInfoType = 'activityInfo'\n, where activityInfoType is either
     * type ('home', 'school' or 'other'),
     * title ('title string of activity'),
     * start ('time string in format required by Time'),
     * end ('time string in format required by Time, ahead of start Time'),
     * location ('location string' (if OtherActivity)), or
     * comment ('comment string' (optional));
     * Each Activity entry must be followed by an empty line (only '\n'), including the last Activity
     * @param loadedFile an input file stream to read Activity information from and to store it into the DayPlanner
     */
    public void loadFile (Scanner loadedFile)
    {
        while (loadedFile.hasNextLine () == true)    //an outer loop to check for any Activity entries
        {
            String nextInfo = loadedFile.nextLine ();
            
            int activityType = 0;
            String title = null;
            Time startTime = null;
            Time stopTime = null;
            String location = null;
            String comment = null;
            
            while (nextInfo.equals ("") == false)    //an inner loop to check for Activity details
            {
                int infoType = typeExtractor (nextInfo);
                String activityInfo = quoteInfoExtractor (nextInfo);
                switch (infoType)
                {
                    case 1:
                        if (activityInfo.equalsIgnoreCase ("Home"))
                        {
                            activityType = 1;
                        }
                        else if (activityInfo.equalsIgnoreCase ("School"))
                        {
                            activityType = 2;   
                        }
                        else if (activityInfo.equalsIgnoreCase ("Other"))
                        {
                            activityType = 3;
                        }
                        else
                        {
                            activityType = 0;
                        }
                        break;
                        
                    case 2:
                        title = activityInfo;
                        break;
                        
                    case 3:
                        startTime = Time.timeParser (activityInfo);
                        break;
                        
                    case 4:
                        stopTime = Time.timeParser (activityInfo);
                        break;
                        
                    case 5:
                        location = activityInfo;
                        break;
                        
                    case 6:
                        comment = activityInfo;
                        break;
                        
                    default:
                        System.out.println ("FATAL ERROR - Activity in File does not follow Proper Format and Cannot Be Read");
                        System.exit (0);
                }               
                
                nextInfo = loadedFile.nextLine ();
            }
            
            switch (activityType)
            {
                case 1:
                    HomeActivity loadHome = null;
                    try
                    {
                        loadHome = new HomeActivity (title, startTime, stopTime, comment);
                    }
                    catch (BadActivityException exception)
                    {
                        System.out.println ("Error in creating new Activity - Now Exiting");
                        System.exit (0);
                    }
                    
                    addToPlanner (loadHome);
                    break;
                    
                case 2:
                    SchoolActivity loadSchool = null;
                    try
                    {
                        loadSchool = new SchoolActivity (title, startTime, stopTime, comment);
                    }
                    catch (BadActivityException exception)
                    {
                        System.out.println ("Error in creating new Activity - Now Exiting");
                        System.exit (0);
                    }
                    
                    addToPlanner (loadSchool);
                    break;
                    
                case 3:
                    OtherActivity loadOther = null;
                    try
                    {
                        loadOther = new OtherActivity (title, startTime, stopTime, location, comment);
                    }
                    catch (BadActivityException exception)
                    {
                        System.out.println ("Error in creating new Activity - Now Exiting");
                        System.exit (0);
                    }
                    
                    addToPlanner (loadOther);
                    break;
                    
                default:
                    System.out.println ("Error - Activity in File does not have suitable Activity Type");
            }
        }
    }
    
    private static int typeExtractor (String fullLineInfo)
    {
        int i;
        int equalsLocation = 0;
        
        for (i = 0; i < fullLineInfo.length (); i ++)
        {
            if (fullLineInfo.charAt (i) == '=')    //finds '=' to find and extract info type
            {
                equalsLocation = i;
                i = fullLineInfo.length () - 1;
            }
        }
        
        String typeInfo = fullLineInfo.substring (0, equalsLocation - 1);
        
        if (typeInfo.equalsIgnoreCase ("Type"))
        {
            return 1;
        }
        else if (typeInfo.equalsIgnoreCase ("Title"))
        {
            return 2;
        }
        else if (typeInfo.equalsIgnoreCase ("Start"))
        {
            return 3;
        }
        else if (typeInfo.equalsIgnoreCase ("End"))
        {
            return 4;
        }
        else if (typeInfo.equalsIgnoreCase ("Location"))
        {
            return 5;   
        }
        else if (typeInfo.equalsIgnoreCase ("Comment"))
        {
            return 6;
        }
        else
        {
            return 0;
        }
    }
    
    private static String quoteInfoExtractor (String fullLineInfo)
    {
        int i;
        int equalsLocation = 0;
        
        for (i = 0; i < fullLineInfo.length (); i ++)
        {
            if (fullLineInfo.charAt (i) == '=')    //finds '=' to find and extract data in quotes
            {
                equalsLocation = i;
                i = fullLineInfo.length () - 1;
            }
        }
        
        String quoteInfo = fullLineInfo.substring (equalsLocation + 3, i - 1);
        
        return quoteInfo;
    }
    
    /**
     * Saves the list of Activities stored in the DayPlanner to a file output stream;
     * Format of the saved Activities follows the requirements needed to reload the Activities from that file
     * @param savingFile file output stream to where Activity information is to be stored
     */
    public void saveActivities (PrintWriter savingFile)
    {        
        for (Activity storedActivity : activityList)
        {
            String activityType = null;
            if (storedActivity instanceof HomeActivity)
            {
                activityType = "home";
            }
            else if (storedActivity instanceof SchoolActivity)
            {
                activityType = "school";
            }
            else if (storedActivity instanceof OtherActivity)
            {
                activityType = "other";
            }
            else
            {
                activityType = "N/A";
            }
            savingFile.println ("type = '" + activityType + "'");
            
            String title = storedActivity.getTitle ();
            savingFile.println ("title = '" + title + "'");
            
            String startString = storedActivity.getStartTime ().toString ();
            savingFile.println ("start = '" + startString + "'");
            
            String endString = storedActivity.getStopTime ().toString ();
            savingFile.println ("end = '" + endString + "'");
            
            if (activityType.equals ("other"))
            {
                OtherActivity typecastActivity = (OtherActivity) storedActivity; 
                String location = typecastActivity.getLocation ();
                savingFile.println ("location = '" + location + "'");
            }
            
            String comment = storedActivity.getComment ();
            if (comment != null)
            {
                savingFile.println ("comment = '" + comment + "'");
            }
            
            savingFile.println ();    //follows each entry with space to be read properly
        }
    }
}
