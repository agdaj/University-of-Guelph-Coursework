/* 
 * BadActivityException.java
 * By: Jireh Agda for CIS*2430
 */
package dayplanner;

/**
 * BadActivityException extends Exception to be thrown whenever an Activity object (or any descendant) is unable to be created due to bad parameters.
 * @author agdaj
 */
public class BadActivityException extends Exception
{
    /**
     * Creates a general BadActivityException with the message "Activity parameters invalid - Could not create Activity object"
     */
    public BadActivityException ()
    {
        super ("Activity parameters invalid - Could not create Activity object");
    }
    
    /**
     * Creates a BadActivityException with a specific message parameter given by the caller
     * @param message specific message tailored to specific Activity parameter error
     */
    public BadActivityException (String message)
    {
        super (message);
    }
}
