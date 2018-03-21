/* 
 * Time.java
 * By: Jireh Agda for CIS*2430
 */
package dayplanner;

import java.util.Scanner;

/**
 * Time provides a class that stores a specific date and time in the form of (dd/mm/yy hh:mm).
 * Users can store multiple Time objects to represent different moments of time,
 * which can then be used to compare with each other and are able to set and get different
 * components of time.
 * Includes a parser (with or without prompts) to help create Time objects.
 * Includes leap year counts and is in the form of a 24-hour clock.
 * @author agdaj
 */
public class Time
{
    private int year;
    private int month;
    private int day;
    private int hours;
    private int minutes;
    
    /**
     * Creates a Time object defaulted to the earliest Time that can be set
     * (1/1/0 00:00)
     * @throws dayplanner.BadTimeException If any parameter given falls outside a potential real Time
     */
    public Time () throws BadTimeException
    {
        this (0, 1, 1, 0, 0);
    }
    
    /**
     * Creates a Time object that holds a date and sets the time to 0:00;
     * If any parameter falls outside the appropriate range, the program will throw a BadTimeException
     * @param year year the year of the date (>= 0)
     * @param month month the month of the date (1 - 12)
     * @param day day the day of the date (depending on month: (1 - 31), (1 - 30), (1 - 28));
     * Can also be 29 in February of leap year
     * @throws dayplanner.BadTimeException If any parameter given falls outside a potential real Time
     */
    public Time (int year, int month, int day) throws BadTimeException
    {
        this (year, month, day, 0, 0);
    }
    
    /**
     * Creates a Time object that holds a time and sets the date to 1/1/0;
     * If any parameter falls outside the appropriate range, the program will throw a BadTimeException
     * @param hours the hours of the time (0 - 23)
     * @param minutes the minutes of the time (0 - 59)
     * @throws dayplanner.BadTimeException If any parameter given falls outside a potential real Time
     */
    public Time (int hours, int minutes) throws BadTimeException
    {
        this (0, 1, 1, hours, minutes);
    }
    
    /**
     * Creates a Time object with the desired parameters;
     * If any parameter falls outside the appropriate range, the program will throw a BadTimeException
     * @param year the year of the date (>= 0)
     * @param month the month of the date (1 - 12)
     * @param day the day of the date (depending on month: (1 - 31), (1 - 30), (1 - 28));
     * Can also be 29 in February of leap year
     * @param hours the hours of the time (0 - 23)
     * @param minutes the minutes of the time (0 - 59)
     * @throws dayplanner.BadTimeException If any parameter given falls outside a potential real Time
     */
    public Time (int year, int month, int day, int hours, int minutes) throws BadTimeException
    {
        if (year >= 0 && month >= 1 && month <= 12)
        {
            switch (month)   //differentiating possible days with months
            {
                case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                    if (day >= 1 && day <= 31)
                    {
                        //continue to verify time
                    }
                    else
                    {
                        throw new BadTimeException ("Invalid day parameter with given month and year");
                    }
                    break;

                case 4: case 6: case 9: case 11:
                    if (day >= 1 && day <= 30)
                    {
                        //continue to verify time
                    }
                    else
                    {
                        throw new BadTimeException ("Invalid day parameter with given month and year");
                    }
                    break;

                case 2:
                    boolean leapYear = year % 4 == 0;    //finding if given year is a leap year

                    if (leapYear == false)
                    {
                        if (day >= 1 && day <= 28)
                        {
                            //continue to verify time
                        }
                        else
                        {
                            throw new BadTimeException ("Invalid day parameter with given month and year");
                        }
                    }
                    else
                    {
                        if (day >= 1 && day <= 29)
                        {
                            //continue to verify time
                        }
                        else
                        {
                            throw new BadTimeException ("Invalid day parameter with given month and year");
                        }
                    }
                    break;
                    
                default:
                    throw new BadTimeException ();
            }
        }
        else
        {
            if (year < 0)
            {
                throw new BadTimeException ("Invalid year parameter");
            }
            else
            {
                throw new BadTimeException ("Invalid month parameter");
            }
        }
        
        if (hours >= 0 && hours < 24)    //checking if time is valid in 24 hour time
        {
            if (minutes >= 0 && minutes < 60)
            {
                this.year = year;
                this.month = month;
                this.day = day;
                this.hours = hours;
                this.minutes = minutes;
            }
            else
            {
                throw new BadTimeException ("Invalid minutes parameter");
            }
        }
        else
        {
            throw new BadTimeException ("Invalid hours parameter");
        }
    }
    
    /**
     * Creates a copy of a Time object in a different memory location;
     * If the parameter is null, the program will throw a BadTimeException
     * @param timeCopy a Time object to be copied
     * @throws dayplanner.BadTimeException If given parameter is null
     */
    public Time (Time timeCopy) throws BadTimeException
    {
        if (timeCopy == null)
        {
            throw new BadTimeException ("Null Time parameter given");
        }
        else
        {
            year = timeCopy.year;
            month = timeCopy.month;
            day = timeCopy.day;
            hours = timeCopy.hours;
            minutes = timeCopy.minutes;
        }
    }
    
    /**
     * Checks if the passed in parameters can create a valid Time object
     * @param inputYear the year of the date (>= 0)
     * @param inputMonth the month of the date (1- 12)
     * @param inputDay the day of the date (depending on month: (1 - 31), (1 - 30), (1 - 28));
     * Can also be 29 in February of leap year
     * @param inputHours the hours of the time (0 - 23)
     * @param inputMinutes the minutes of the time (0 - 59)
     * @return true if the parameters fall into the appropriate ranges;
     * false if any of the parameters fall outside the appropriate range
     */
    public static boolean validTime (int inputYear, int inputMonth, int inputDay, int inputHours, int inputMinutes)
    {
        if (inputYear >= 0 && inputMonth >= 1 && inputMonth <= 12)
        {
            switch (inputMonth)   //differentiating possible days with months
            {
                case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                    if (inputDay >= 1 && inputDay <= 31)
                    {
                        //continue to verify time
                    }
                    else
                    {
                        return false;
                    }
                    break;

                case 4: case 6: case 9: case 11:
                    if (inputDay >= 1 && inputDay <= 30)
                    {
                        //continue to verify time
                    }
                    else
                    {
                        return false;
                    }
                    break;

                case 2:
                    boolean leapYear = inputYear % 4 == 0;    //finding if given year is a leap year

                    if (leapYear == false)
                    {
                        if (inputDay >= 1 && inputDay <= 28)
                        {
                            //continue to verify time
                        }
                        else
                        {
                            return false;
                        }
                    }
                    else
                    {
                        if (inputDay >= 1 && inputDay <= 29)
                        {
                            //continue to verify time
                        }
                        else
                        {
                            return false;
                        }
                    }
                    break;
                    
                default:
                    return false;
            }
        }
        else
        {
            return false;
        }
        
        if (inputHours >= 0 && inputHours < 24)    //checking if new time is valid in 24 hour time
        {
            if (inputMinutes >= 0 && inputMinutes < 60)
            {
                return true;
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
    
    /**
     * Returns the Time object's year attribute
     * @return year of Time object
     */
    public int getYear ()
    {
        return year;
    }
    
    /**
     * Returns the Time object's month attribute
     * @return month of Time object
     */
    public int getMonth ()
    {
        return month;
    }
    
    /**
     * Returns the Time object's day attribute
     * @return day of Time object
     */
    public int getDay ()
    {
        return day;
    }
    
    /**
     * Returns the Time object's hours attribute
     * @return hours of Time object
     */
    public int getHours ()
    {
        return hours;
    }
    
    /**
     * Returns the Time object's minutes attribute
     * @return minutes of Time object
     */
    public int getMinutes ()
    {
        return minutes;
    }
    
    /**
     * Sets the year, month and day portion of the Time object
     * @param newYear the new year of the Time object (>= 0)
     * @param newMonth the new month of the Time object (1 - 12)
     * @param newDay the new day of the Time object (depending on month: (1 - 31), (1 - 30), (1 - 28));
     * Can also be 29 in February of leap year
     * @return true if parameters fall into the appropriate ranges and successfully modifies Time object;
     * false if parameters do not fall into the appropriate ranges
     */
    public boolean setDate (int newYear, int newMonth, int newDay)
    {
        if (newYear >= 0 && newMonth >= 1 && newMonth <= 12)
        {
            switch (newMonth)   //differentiating possible days with months
            {
                case 1: case 3: case 5: case 7: case 8: case 10: case 12:
                    if (newDay >= 1 && newDay <= 31)
                    {
                        year = newYear;
                        month = newMonth;
                        day = newDay;
                        return true;
                    }
                    else
                    {
                        return false;
                    }

                case 4: case 6: case 9: case 11:
                    if (newDay >= 1 && newDay <= 30)
                    {
                        year = newYear;
                        month = newMonth;
                        day = newDay;
                        return true;
                    }
                    else
                    {
                        return false;
                    }

                case 2:
                    boolean leapYear = newYear % 4 == 0;    //finding if given year is a leap year

                    if (leapYear == false)
                    {
                        if (newDay >= 1 && newDay <= 28)
                        {
                            year = newYear;
                            month = newMonth;
                            day = newDay;
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    else
                    {
                        if (newDay >= 1 && newDay <= 29)
                        {
                            year = newYear;
                            month = newMonth;
                            day = newDay;
                            return true;
                        }
                        else
                        {
                            return false;
                        }
                    }
                    
                default:
                    return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Sets the hours and month of the Time object
     * @param newHours the new hours of the time (0 - 23)
     * @param newMinutes the new minutes of the time (0 - 59)
     * @return true if the parameters fall into the appropriate ranges and successfully modifies Time object;
     * false if parameters do not fall into appropriate ranges 
     */
    public boolean setTime (int newHours, int newMinutes)
    {
        if (newHours >= 0 && newHours < 24)    //checking if new time is valid in 24 hour time
        {
            if (newMinutes >= 0 && newMinutes < 60)
            {
                hours = newHours;
                minutes = newMinutes;
                return true;
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
    
    /**
     * Checks if two separate Objects are of the exact same type and have the exact same attributes
     * @param compareTo Object to be compared to
     * @return true if the objects are of the exact same type and have the exact same attributes;
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
            Time compareTime = (Time) compareTo;
            
            return (equals (compareTime));
        }
    }
    
    /**
     * Checks if two separate Time objects have the exact same attributes
     * @param compareTo Time object to be compared to
     * @return true of all attributes are equal;
     * false if at least one set of attributes are not equal
     */
    public boolean equals (Time compareTo)
    {
        if (compareTo == null)
        {
            return false;
        }
        else
        {
            return (year == compareTo.year && month == compareTo.month && day == compareTo.day && hours == compareTo.hours && minutes == compareTo.minutes);
        }
    }
    
    /**
     * Formats Time object attributes into a String
     * @return String containing Time attributes in a specific format
     */
    @Override
    public String toString ()
    {
        String timeString;
        
        timeString = String.format ("%02d/%02d/%02d %02d:%02d", day, month, year, hours, minutes);
                
        return timeString;
    }
    
    /**
     * Compares two Time objects to determine relative Time position between each other
     * @param timeToCompare Time object to compare relative Time position
     * @return -2 if the Time parameter is null;
     * -1 if the Time parameter is later than current Time parameter;
     * 0 if Times are equal;
     * 1 if Time parameter is earlier than current Time parameter
     */
    public int compareTo (Time timeToCompare)
    {
        if (timeToCompare == null)    //returns another int (-2) for null parameter
        {
            return -2;
        }
        
        if (year > timeToCompare.year)    //checks from 'biggest' to 'smallest' Time attribute for relative Time positions
        {
            return 1;
        }
        else if (year < timeToCompare.year)
        {
            return -1;
        }
        else
        {
            if (month > timeToCompare.month)
            {
                return 1;
            }
            else if (month < timeToCompare.month)
            {
                return -1;
            }
            else
            {
                if (day > timeToCompare.day)
                {
                    return 1;
                }
                else if (day < timeToCompare.day)
                {
                    return -1;
                }
                else
                {
                    if (hours > timeToCompare.hours)
                    {
                        return 1;
                    }
                    else if (hours < timeToCompare.hours)
                    {
                        return -1;
                    }
                    else
                    {
                        if (minutes > timeToCompare.minutes)
                        {
                            return 1;
                        }
                        else if (minutes < timeToCompare.minutes)
                        {
                            return -1;
                        }
                        else
                        {
                            return 0;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Compares two Time objects to determine relative Time positions to each other
     * @param timeOne Time object to be compared against
     * @param timeTwo Time object to compare with timeOne
     * @return -2 if any parameter is null;
     * -1 if timeOne is before timeTwo in Time position;
     * 0 if timeOne is equal to timeTwo in Time position;
     * 1 if timeTwo is before timeOne in Time position
     */
    public static int compareTimes (Time timeOne, Time timeTwo)
    {
        if (timeOne == null || timeTwo == null)    //returns -2 for any null parameter
        {
            return -2;
        }
        
        if (timeOne.year > timeTwo.year)    //checks from 'biggest' to 'smallest' Time attribute to determine relative Time positions
        {
            return 1;
        }
        else if (timeOne.year < timeTwo.year)
        {
            return -1;
        }
        else
        {
            if (timeOne.month > timeTwo.month)
            {
                return 1;
            }
            else if (timeOne.month < timeTwo.month)
            {
                return -1;
            }
            else
            {
                if (timeOne.day > timeTwo.day)
                {
                    return 1;
                }
                else if (timeOne.day < timeTwo.day)
                {
                    return -1;
                }
                else
                {
                    if (timeOne.hours > timeTwo.hours)
                    {
                        return 1;
                    }
                    else if (timeOne.hours < timeTwo.hours)
                    {
                        return -1;
                    }
                    else
                    {
                        if (timeOne.minutes > timeTwo.minutes)
                        {
                            return 1;
                        }
                        else if (timeOne.minutes < timeTwo.minutes)
                        {
                            return -1;
                        }
                        else
                        {
                            return 0;
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Provides a prompt and guideline to parse and create a Time object from user input
     * @param timeInput where input will be read from
     * @return null if: Scanner object is null, input does not follow prompt, or input does not lead to valid Time;
     * Time object if the user input successfully creates a Time object
     */
    public static Time timeParserWithPrompts (Scanner timeInput)
    {
        if (timeInput == null)
        {
            System.out.println ("Scanner Object is null, Returning from Method");
            return null;    
        }
        
        System.out.print ("Please Input in the Format (dd/mm/yy hh:mm): ");
        String timeString = timeInput.nextLine ();
        
        if (timeString.matches ("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+"))    //tests if given input matches desired format to read/parse from
        {
            int i;
            int spaceLocation = 0;
            for (i = 0; i < timeString.length (); i ++)
            {
                if (timeString.charAt (i) == ' ')
                {
                    spaceLocation = i;
                }
            }
            
            String date = timeString.substring (0, spaceLocation);
            String time = timeString.substring (spaceLocation + 1, i);
            
            //splits the date and time portions of the String
            String[] dateSplit = date.split ("/");
            String[] timeSplit = time.split (":");
            
            int day = Integer.parseInt (dateSplit[0]);
            int month = Integer.parseInt (dateSplit[1]);
            int year = Integer.parseInt (dateSplit[2]);
            
            int hours = Integer.parseInt (timeSplit[0]);
            int minutes = Integer.parseInt (timeSplit[1]);
            
            boolean checkTime = Time.validTime (year, month, day, hours, minutes);
            if (checkTime == false)
            {
                System.out.println ("Given Time Does Not Exist");
                System.out.println ("Please Input Another Time");
                return null;
            }
            else
            {
                Time newTime;
                
                try
                {
                    newTime = new Time (year, month, day, hours, minutes);
                }
                catch (BadTimeException exception)
                {
                    return null;
                }
                
                return newTime;
            }
        }
        else
        {
            System.out.println ("Input Did Not Follow the Format (dd/mm/yy hh:mm)");
            return null;
        }
    }
    
    /**
     * Converts a properly formatted String (dd/mm/yy hh:mm) into a Time object
     * @param timeString String to convert into Time object
     * @return null if the String does not follow format, is an invalid Time or is null;
     * Time object if the String is successfully converted into Time object
     */
    public static Time timeParser (String timeString)
    {
        if (timeString == null)
        {
            return null;
        }
        else if (timeString.matches ("[0-9]+/[0-9]+/[0-9]+ [0-9]+:[0-9]+"))    //tests if given String matches desired format to read/parse from
        {
            int i;
            int spaceLocation = 0;
            for (i = 0; i < timeString.length (); i ++)
            {
                if (timeString.charAt (i) == ' ')
                {
                    spaceLocation = i;
                }
            }
            
            String date = timeString.substring (0, spaceLocation);
            String time = timeString.substring (spaceLocation + 1, i);
            
            //splits the date and time portions of the String
            String[] dateSplit = date.split ("/");
            String[] timeSplit = time.split (":");
            
            int day = Integer.parseInt (dateSplit[0]);
            int month = Integer.parseInt (dateSplit[1]);
            int year = Integer.parseInt (dateSplit[2]);
            
            int hours = Integer.parseInt (timeSplit[0]);
            int minutes = Integer.parseInt (timeSplit[1]);
            
            boolean checkTime = Time.validTime (year, month, day, hours, minutes);
            if (checkTime == false)
            {
                return null;
            }
            else
            {
                Time newTime;
                try
                {
                    newTime = new Time (year, month, day, hours, minutes);
                }
                catch (BadTimeException exception)
                {
                    return null;
                }
                
                return newTime;
            }
        }
        else
        {
            return null;
        }
    }
}
