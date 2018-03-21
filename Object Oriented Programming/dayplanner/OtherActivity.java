/* 
 * OtherActivity.java
 * By: Jireh Agda for CIS*2430
 */
package dayplanner;

/**
 * OtherActivity stores activity details (that are not either home or school activities), such as the activity's title, the start and stop times of the activity, the activity's location and a comment about the activity.
 * The activity title, location and comment are in the form of a String, and the start and stop times are stored as a Time object.
 * Adding a comment is optional and methods will handle this appropriately.
 * Class invariant: Activity title must not be empty and the start Time must precede stop Time.
 * @author agdaj
 */
public class OtherActivity extends Activity
{    
    /**
     * Creates an OtherActivity with "No Title", "No Location", no comment and the times set to default 1/1/0 00:00
     * @throws dayplanner.BadActivityException If class invariant is violated
     * @throws dayplanner.BadTimeException If default Time is unable to be created
     */
    public OtherActivity () throws BadActivityException, BadTimeException
    {
        super ();
    }
    
    /**
     * Creates an OtherActivity object (without a comment) with the given parameters;
     * If any parameter is null or if the start and stop times are not in order (or equal), the program will throw a BadActivityException
     * @param title String that represents activity's title
     * @param startTime Time object that represents activity's start time
     * @param stopTime  Time object that represents activity's stop time
     * @param location String that holds the activity's location
     * @throws dayplanner.BadActivityException If any parameter is null or class invariant is violated
     */
    public OtherActivity (String title, Time startTime, Time stopTime, String location) throws BadActivityException
    {
        this (title, startTime, stopTime, location, null);
    }
    
    /**
     * Creates an OtherActivity object (with a comment) with the given parameters;
     * If any parameter (other than comment) is null or if the start and stop times are not in order (or equal), the program will throw a BadActivityException
     * @param title String that holds the activity's title
     * @param startTime Time object that holds an activity's start time
     * @param stopTime Time object that holds an activity's stop time
     * @param location String that holds the activity's location
     * @param comment String that holds a comment about the activity
     * @throws dayplanner.BadActivityException If any parameter (other than comment) is null or class invariant is violated
     */
    public OtherActivity (String title, Time startTime, Time stopTime, String location, String comment) throws BadActivityException
    {   
        super (title, startTime, stopTime, location, comment);
    }
    
    /**
     * Creates a copy of an OtherActivity object in a different memory location;
     * If the parameter is null, the program will throw a BadActivityException 
     * @param copyActivity an OtherActivity object to be copied
     * @throws dayplanner.BadActivityException If given parameter is null
     */
    public OtherActivity (OtherActivity copyActivity) throws BadActivityException
    {
        super (copyActivity);
    }
    
    /**
     * Sets a new location for the OtherActivity object
     * @param newLocation the activity's new location
     * @return true if the parameter is not null and method successfully changes location;
     * false if parameter is null
     */
    @Override
    public boolean setLocation (String newLocation)
    {
        return super.setLocation (newLocation);
    }
}
