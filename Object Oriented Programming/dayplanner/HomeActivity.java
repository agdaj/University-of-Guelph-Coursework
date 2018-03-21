/* 
 * HomeActivity.java
 * By: Jireh Agda for CIS*2430
 */
package dayplanner;

/**
 * HomeActivity stores home activity details, such as the activity's title, the start and stop times of the activity and a comment about the activity.
 * The activity title and comment are in the form of a String, and the start and stop times are stored as a Time object.
 * The location of the activity is automatically set to "Home" (in a String) and cannot be changed.
 * Adding a comment is optional and methods will handle this appropriately.
 * Class invariant: Activity title must not be empty and the start Time must precede stop Time.
 * @author agdaj
 */
public class HomeActivity extends Activity
{
    private static final String location = "Home";
    
    /**
     * Creates a HomeActivity with "No Title", no comment and the times set to default 1/1/0 00:00
     * @throws dayplanner.BadActivityException If class invariant is violated
     * @throws dayplanner.BadTimeException If default Time is unable to be created
     */
    public HomeActivity () throws BadActivityException, BadTimeException
    {
        this ("No Title", new Time (), new Time (), null);
    }
    
    /**
     * Creates a HomeActivity object (without a comment) with the given parameters;
     * If any parameter is null or if the start and stop times are not in order (or equal), the program will throw a BadActivityException
     * @param title String that represents activity's title
     * @param startTime Time object that represents activity's start time
     * @param stopTime  Time object that represents activity's stop time
     * @throws dayplanner.BadActivityException If any parameter is null or class invariant is violated
     */
    public HomeActivity (String title, Time startTime, Time stopTime) throws BadActivityException
    {
        this (title, startTime, stopTime, null);
    }
    
    /**
     * Creates a HomeActivity object (with a comment) with the given parameters;
     * If any parameter (other than comment) is null or if the start and stop times are not in order (or equal), the program will throw a BadActivityException
     * @param title String that holds the activity's title
     * @param startTime Time object that holds an activity's start time
     * @param stopTime Time object that holds an activity's stop time
     * @param comment String that holds a comment about the activity
     * @throws dayplanner.BadActivityException If any parameter (other than comment) is null or class invariant is violated
     */
    public HomeActivity (String title, Time startTime, Time stopTime, String comment) throws BadActivityException
    {
        super (title, startTime, stopTime, location, comment);
    }
    
    /**
     * Creates a copy of a HomeActivity object in a different memory location;
     * If the parameter is null, the program will throw a BadActivityException
     * @param copyActivity a HomeActivity object to be copied
     * @throws dayplanner.BadActivityException If given parameter is null
     */
    public HomeActivity (HomeActivity copyActivity) throws BadActivityException
    {
        super (copyActivity);
    }
}
