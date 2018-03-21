/* 
 * SchoolActivity.java
 * By: Jireh Agda for CIS*2430
 */
package dayplanner;

/**
 * SchoolActivity stores school activity details, such as the activity's title, the start and stop times of the activity and a comment about the activity.
 * The activity title and comment are in the form of a String, and the start and stop times are stored as a Time object.
 * The location of the activity is automatically set to "School" (in a String) and cannot be changed.
 * Adding a comment is optional and methods will handle this appropriately.
 * Class invariant: Activity title must not be empty and the start Time must precede stop Time.
 * @author agdaj
 */
public class SchoolActivity extends Activity
{
    private static final String location = "School";
    
    /**
     * Creates a SchoolActivity with "No Title" or comment and the times set to default 1/1/0 00:00
     * @throws dayplanner.BadActivityException If class invariant is violated
     * @throws dayplanner.BadTimeException If default Time is unable to be created
     */
    public SchoolActivity () throws BadActivityException, BadTimeException
    {
        this ("No Title", new Time (), new Time (), null);
    }
    
    /**
     * Creates a SchoolActivity object (without a comment) with the given parameters;
     * If any parameter is null or if the start and stop times are not in order (or equal), the program will terminate
     * @param title String that represents activity's title
     * @param startTime Time object that represents activity's start time
     * @param stopTime  Time object that represents activity's stop time
     * @throws dayplanner.BadActivityException If any parameter is null or class invariant is violated
     */
    public SchoolActivity (String title, Time startTime, Time stopTime) throws BadActivityException
    {
        this (title, startTime, stopTime, null);
    }
    
    /**
     * Creates a SchoolActivity object (with a comment) with the given parameters;
     * If any parameter (other than comment) is null or if the start and stop times are not in order (or equal), the program will throw a BadActivityException
     * @param title String that holds the activity's title
     * @param startTime Time object that holds an activity's start time
     * @param stopTime Time object that holds an activity's stop time
     * @param comment String that holds a comment about the activity
     * @throws dayplanner.BadActivityException If any parameter (other than comment) is null or class invariant is violated
     */
    public SchoolActivity (String title, Time startTime, Time stopTime, String comment) throws BadActivityException
    {
        super (title, startTime, stopTime, location, comment);
    }
    
    /**
     * Creates a copy of a SchoolActivity object in a different memory location;
     * If the parameter is null, the program will throw a BadActivityException
     * @param copyActivity a SchoolActivity object to be copied
     * @throws dayplanner.BadActivityException If given parameter is null
     */
    public SchoolActivity (SchoolActivity copyActivity) throws BadActivityException
    {
        super (copyActivity);
    }
}
