/* 
 * Activity.java
 * By: Jireh Agda for CIS*2430
 */
package dayplanner;

/**
 * Activity stores all of the common attributes and methods HomeActivity, SchoolActivity and OtherActivity have into one super-class for easy maintenance.
 * Class invariant: Activity title must not be empty and the start Time must precede stop Time.
 * @author agdaj
 */
public abstract class Activity
{
    private String activityTitle;
    private Time startTime;
    private Time stopTime;
    private String activityLocation;
    private String activityComment;
    
    /**
     * Creates an Activity with "No Title", "No Location", no comment and the times set to default 1/1/0 00:00
     * @throws dayplanner.BadActivityException If class invariant is violated
     * @throws dayplanner.BadTimeException If default Time is unable to be created
     */
    public Activity () throws BadActivityException, BadTimeException
    {
        this ("No Title", new Time (), new Time (), "No Location", null);
    }
    
    /**
     * Creates an Activity object (without a comment) with the given parameters;
     * If any parameter is null or if the start and stop times are not in order (or equal), the program will throw a BadActivityException
     * @param title String that represents activity's title
     * @param startTime Time object that represents activity's start time
     * @param stopTime  Time object that represents activity's stop time
     * @param location String that represents activity's location
     * @throws dayplanner.BadActivityException If any parameter is null or class invariant is violated
     */
    public Activity (String title, Time startTime, Time stopTime, String location) throws BadActivityException
    {
        this (title, startTime, stopTime, location, null);
    }
    
    /**
     * Creates an Activity object (with a comment) with the given parameters;
     * If any parameter (other than comment) is null or if the start and stop times are not in order (or equal), the program will throw a BadActivityException
     * @param title String that holds the activity's title
     * @param startTime Time object that holds an activity's start time
     * @param stopTime Time object that holds an activity's stop time
     * @param location String that holds the activity's location
     * @param comment String that holds a comment about the activity
     * @throws dayplanner.BadActivityException If any parameter (other than comment) is null or class invariant is violated
     */
    public Activity (String title, Time startTime, Time stopTime, String location, String comment) throws BadActivityException
    {
        if (title == null || startTime == null || stopTime == null || location == null)
        {
            throw new BadActivityException ("Null Activity parameter given");
        }
        else
        {
            if (title.equals (""))
            {
                throw new BadActivityException ("Activity Title is empty");
            }
            
            int checkValidTimes = startTime.compareTo (stopTime);
            
            if (checkValidTimes <= 0)
            {
                activityTitle = title;
                
                try
                {
                    this.startTime = new Time (startTime);
                    this.stopTime = new Time (stopTime);
                }
                catch (BadTimeException exception)
                {
                    throw new BadActivityException ();
                }
                
                activityLocation = location;
                activityComment = comment;
            }
            else
            {
                throw new BadActivityException ("Activity Time order invalid");
            }
        }
    }
    
    /**
     * Creates a copy of an Activity object in a different memory location;
     * If the parameter is null, the program will throw a BadActivityException
     * @param copyActivity an Activity object to be copied
     * @throws dayplanner.BadActivityException If given parameter is null
     */
    public Activity (Activity copyActivity) throws BadActivityException
    {
        if (copyActivity == null)
        {
            throw new BadActivityException ("Null Activity parameter given");
        }
        else
        {
            activityTitle = copyActivity.activityTitle;
            
            try
            {
                startTime = new Time (copyActivity.startTime);
                stopTime = new Time (copyActivity.stopTime);
            }
            catch (BadTimeException exception)    //if somehow a Time object in an Activity is null, the program will terminate before any big issues start to arise
            {
                System.out.println ("FATAL ERROR - Unexpected null Time paramter in present Time object");
                System.exit (0);
            }
            
            activityLocation = copyActivity.activityLocation;
            activityComment = copyActivity.activityComment;
        }
    }
    
    /**
     * Checks if the passed in parameters can create a valid Activity object
     * @param inputTitle the title of the activity
     * @param inputStartTime the start time of the activity
     * @param inputStopTime the stop time of the activity
     * @param inputLocation the location of the activity
     * @return true if the parameters are not null and the class invariant is not violated;
     * false if any parameter is null or the class invariant is violated
     */
    public static boolean validActivity (String inputTitle, Time inputStartTime, Time inputStopTime, String inputLocation)
    {
        if (inputTitle == null || inputStartTime == null || inputStopTime == null || inputLocation == null)
        {
            return false;
        }
        else
        {
            if (inputTitle.equals (""))
            {
                return false;
            }
            else
            {
                int checkTimes = inputStartTime.compareTo (inputStopTime);

                if (checkTimes <= 0)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
    }
    
    /**
     * Returns the Activity's title 
     * @return title of the activity
     */
    public String getTitle ()
    {
        return activityTitle;
    }
    
    /**
     * Returns the Activity's start Time
     * @return start Time of the activity
     */
    public Time getStartTime ()
    {
        Time newStartTime = null;
        
        try
        {
            newStartTime = new Time (startTime);
        }
        catch (BadTimeException exception)    //if somehow a Time object in an Activity is null, the program will terminate before any big issues start to arise
        {
            System.out.println ("FATAL ERROR - Unexpected null Time paramter in present Time object");
            System.exit (0);
        }
        
        return newStartTime;
    }
    
    /**
     * Returns the Activity's stop Time
     * @return stop Time of the activity
     */
    public Time getStopTime ()
    {
        Time newStopTime = null;
        
        try
        {
            newStopTime = new Time (stopTime);
        }
        catch (BadTimeException exception)    //if somehow a Time object in an Activity is null, the program will terminate before any big issues start to arise
        {
            System.out.println ("FATAL ERROR - Unexpected null Time paramter in present Time object");
            System.exit (0);
        }
        
        return newStopTime;
    }
    
    /**
     * Returns the Activity's location
     * @return location the activity
     */
    public String getLocation ()
    {
        return activityLocation;
    }
    
    /**
     * Returns the Activity's comment
     * @return comment about the activity
     */
    public String getComment ()
    {
        return activityComment;
    }
    
    /**
     * Sets a new title for the Activity object
     * @param newTitle the activity's new title
     * @return true if the parameter is not null and method successfully changes title;
     * false if parameter is null
     */
    public boolean setTitle (String newTitle)
    {
        if (newTitle == null)
        {
            return false;
        }
        else
        {
            activityTitle = newTitle;
            return true;
        }
    }
    
    /**
     * Sets a new start time for the Activity object
     * @param newStartTime the activity's new start time
     * @return true if the parameter is not null and precedes/is equal to the current stop time;
     * false if parameter is null or does not precede/is not equal to current stop time
     */
    public boolean setStartTime (Time newStartTime)
    {
        if (newStartTime == null)
        {
            return false;
        }
        else
        {
            if (stopTime == null)    //if stop time is null, start time is modified
            {
                startTime = newStartTime;
                return true;
            }
            else
            {
                int compareTimes = newStartTime.compareTo (stopTime);
                
                if (compareTimes <= 0)    //start time only valid if before stop time (or equal)
                {
                    startTime = newStartTime;
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
    }
    
    /**
     * Sets a new stop time for the Activity object
     * @param newStopTime the activity's new stop time
     * @return true if the parameter is not null and is after/is equal to the current start time;
     * false if parameter is null or is not after/is not equal to the current start time
     */
    public boolean setStopTime (Time newStopTime)
    {
        if (newStopTime == null)
        {
            return false;
        }
        else
        {
            if (startTime == null)    //if start time is null, stop time is modified
            {
                stopTime = newStopTime;
                return true;
            }
            else
            {
                int compareTimes = newStopTime.compareTo (startTime);
                
                if (compareTimes >= 0)    //stop time only valid if after start time (or equal)
                {
                    stopTime = newStopTime;
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
    }
    
    /**
     * Sets a new location for the Activity object
     * @param newLocation the activity's new location
     * @return true if the parameter is not null and method successfully changes location;
     * false if parameter is null
     */
    protected boolean setLocation (String newLocation)
    {
        if (newLocation == null)
        {
            return false;
        }
        else
        {
            activityLocation = newLocation;
            return true;
        }
    }
    
    /**
     * Sets a new comment for the Activity object
     * @param newComment the activity's new comment
     * @return true if the parameter is not null and method successfully changes comment;
     * false if parameter is null
     */
    public boolean setComment (String newComment)
    {
        if (newComment == null)
        {
            return false;
        }
        else
        {
            activityComment = newComment;
            return true;
        }
    }
    
    /**
     * Checks if two separate Objects are of the exact same type and have the exact same attributes (minus case-differences)
     * @param compareTo Object to be compared to
     * @return true if the objects are of the exact same type and have the exact same attributes (minus case-differences);
     * false if any of these attributes are not equal
     */
    @Override
    public boolean equals (Object compareTo)
    {
        if (compareTo == null)
        {
            return false;
        }
        else if (getClass () != compareTo.getClass ())
        {
            return false;
        }
        else
        {
            Activity compareAct = (Activity) compareTo;
            
            return (equals (compareAct));
        }
    }
    
    /**
     * Checks if two separate Activity objects have the exact same title and location (minus case-differences), start time and stop time
     * @param compareTo Activity to be compared to
     * @return true if all the objects have the exact same title and location (minus case-differences), start time and stop time;
     * false if any of these attributes are not equal
     */
    public boolean equals (Activity compareTo)
    {
        if (compareTo == null)
        {
            return false;
        }
        else
        {
            return (activityTitle.equalsIgnoreCase (compareTo.activityTitle) && startTime.equals (compareTo.startTime) && stopTime.equals (compareTo.stopTime)) && activityLocation.equalsIgnoreCase (compareTo.activityLocation);
        }
    }
    
    /**
     * Formats Activity attributes into a String
     * @return String containing Activity attributes in a specific format
     */
    @Override
    public String toString ()
    {
        if (activityComment == null)
        {
            return activityTitle + ": " + startTime.toString () + " - " + stopTime.toString () + " @ " + activityLocation;
        }
        else
        {
            return activityTitle + ": " + startTime.toString () + " - " + stopTime.toString () + " @ " + activityLocation + "\n" +  "- " + activityComment;
        }
    }
}
