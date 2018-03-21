/* 
 * BadTimeException.java
 * By: Jireh Agda for CIS*2430
 */
package dayplanner;

/**
 * BadTimeException extends Exception to be thrown whenever a Time object is unable to be created due to bad parameters.
 * @author agdaj
 */
public class BadTimeException extends Exception
{
    /**
     * Creates a general BadTimeException with the message "Time parameters invalid - Could not create Time object"
     */
    public BadTimeException ()
    {
        super ("Time parameters invalid - Could not create Time object");
    }
    
    /**
     * Creates a BadTimeException with a specific message parameter given by the caller
     * @param message specific message tailored to specific Time parameter error
     */
    public BadTimeException (String message)
    {
        super (message);
    }
}
