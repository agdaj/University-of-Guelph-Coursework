/* 
 * DayPlannerGUI.java
 * By: Jireh Agda for CIS*2430
 */
package dayplanner;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import java.util.Scanner;
import java.util.ArrayList;

/**
 * DayPlannerGUI creates a GUI for using a DayPlanner to manage Activities through adding and searching Activities.
 * DayPlannerGUI takes in a file output stream as an argument to have a location to store DayPlanner Activity information in.
 * @author agdaj
 */
public class DayPlannerGUI extends JFrame
{
    /**
     * Indicates pixel width of main window to be used
     */
    public static final int WIDTH = 540;
    /**
     * Indicates pixel height of main window to be used
     */
    public static final int HEIGHT = 525;
    /**
     * Indicates pixel width of exit check window to be used
     */
    public static final int EXIT_WIDTH = 230;
    /**
     * Indicates pixel height of exit check window to be used
     */
    public static final int EXIT_HEIGHT = 110;
    /**
     * Indicates pixel width of thank you window to be used
     */
    public static final int FINAL_WIDTH = 250;
    /**
     * Indicates pixel height of thank you window to be used
     */
    public static final int FINAL_HEIGHT = 65;
    /**
     * Indicates pixel width of instruction windows to be used (+/- a few chars)
     */
    public static final int INSTRUCT_WIDTH = 520;
    /**
     * Indicates pixel height of instruction windows to be used (+/- a few lines)
     */
    public static final int INSTRUCT_HEIGHT = 295;
        
    //stores internal size data of some main componenets
    private static final int MAIN_PANEL_WIDTH = 500;
    private static final int MAIN_PANEL_HEIGHT = 400;
    private static final int TEXT_FIELD_LENGTH = 30;
    private static final int TEXT_AREA_LENGTH = 35;
    private static final int TEXT_AREA_DEPTH = 15;
    
    //holds main DayPlanner functionality variables
    private DayPlanner mainPlanner;
    private PrintWriter saveFile;
    
    //stores and divides program to three panels to be set visible at different times
    private JPanel introPanel;
    private JPanel addPanel;
    private JPanel searchPanel;
    
    //determines if an instruction window pops out for each add panel and search panel, only being seen once per program run when user chooses either command
    private boolean addInstructions;
    private boolean searchInstructions;
    
    //holds add activity components to be used for making Activities
    private JComboBox addActivityType;
    private JTextField activityTitle;
    private JTextField activityStartTime;
    private JTextField activityEndTime;
    private JTextField activityLocation;
    private JTextField activityComment;
    private JPanel locationPanel;
    private JTextArea messages;
    
    //holds search activity components to be used for searching Activities
    private JComboBox searchActivityType;
    private JTextField activityKeywords;
    private JTextField startSearchTime;
    private JTextField endSearchTime;
    private JTextArea searchResultsBox;
    
    /**
     * Creates a GUI interface for adding activities to a provided DayPlanner that can also save Activities to a given output stream
     * @param plannerToUse DayPlanner to be used
     * @param saveStream output stream saved Activities go to
     */
    public DayPlannerGUI (DayPlanner plannerToUse, PrintWriter saveStream)
    {
        super ();
        setSize (WIDTH, HEIGHT);
        
        setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener (new ExitCheck ());    //adds window listener to enable a final check of exit before a full exit via another window
        
        //sets two main attributes to contribute to the use and maintenance of a DayPlanner
        mainPlanner = plannerToUse;
        saveFile = saveStream;
        
        addInstructions = false;
        searchInstructions = false;
        
        setTitle ("DayPlanner");
        getContentPane ().setBackground (Color.CYAN);
        
        setJMenuBar (setMenu ());    //sets a menu bar, made and called through a separate method
        
        setIntro ();
        setAddPanel ();
        setSearchPanel ();
        
        introPanel.setVisible (true);    //sets intro panel visible only to be changed later through commands
        addPanel.setVisible (false);
        searchPanel.setVisible (false);
        
        setLayout (new BoxLayout (getContentPane (), BoxLayout.X_AXIS));
        add (introPanel);
        add (addPanel);
        add (searchPanel);
    }
    
    private JMenuBar setMenu ()
    {
        JMenu commandsMenu = new JMenu ("Commands");
        
        JMenuItem addCommand = new JMenuItem ("Add");
        addCommand.addActionListener (new MenuListener ());
        commandsMenu.add (addCommand);
        
        JMenuItem searchCommand = new JMenuItem ("Search");
        searchCommand.addActionListener (new MenuListener ());
        commandsMenu.add (searchCommand);
        
        JMenuItem quitCommand = new JMenuItem ("Quit");
        quitCommand.addActionListener (new MenuListener ());
        commandsMenu.add (quitCommand);
        
        JMenuBar mainBar = new JMenuBar ();    //create menu bar that will be placed at the top of the main window
        mainBar.add (commandsMenu);
        
        return (mainBar);
    }
    
    private void setIntro ()
    {
        introPanel = new JPanel ();
        introPanel.setLayout (new FlowLayout (FlowLayout.LEADING));
        introPanel.setPreferredSize (new Dimension (100, 100));
        introPanel.setBackground (Color.CYAN);
        
        //places main instructions on intro panel, with empty spaces (Strings) in betweed sentence portions for spacing
        introPanel.add (new JLabel ("                                                                   "));
        introPanel.add (new JLabel ("Welcome to the DayPlanner Program!"));
        introPanel.add (new JLabel ("                                                                   "));
        introPanel.add (new JLabel ("The DayPlanner Program allows you to add and search for activities"));
        introPanel.add (new JLabel ("you may wish to store here and keeps track of it."));
        introPanel.add (new JLabel ("                                                                   "));
        introPanel.add (new JLabel ("Please choose a command from the \"Commands\" menu to add activities,"));
        introPanel.add (new JLabel ("search for activities or quit the program."));
    }
    
    private void setAddPanel ()
    {
        //creates panel where all add activity-related items are placed into
        addPanel = new JPanel ();
        addPanel.setLayout (new BorderLayout ());
        addPanel.setPreferredSize (new Dimension (MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT));    //sets rigid panel size to place components to appropriate areas
        
        //sets panel for the main adding activity components to be added to (combo boxes and text fields)
        JPanel activityInfoAdd = new JPanel ();
        activityInfoAdd.setLayout (new FlowLayout (FlowLayout.LEADING));
        activityInfoAdd.setBorder (BorderFactory.createLineBorder (Color.BLACK));
        
        JPanel addHeader = new JPanel ();
        addHeader.setLayout (new FlowLayout (FlowLayout.LEADING));
        addHeader.add (new JLabel ("Adding Activities:"));
        activityInfoAdd.add (addHeader);
        
        //adds combo box of Activity types to add
        JPanel typePanel = new JPanel ();
        typePanel.setLayout (new FlowLayout (FlowLayout.LEADING));
        typePanel.add (new JLabel ("Type:"));
        String[] activityTypes = {"Home", "School", "Other"};
        addActivityType = new JComboBox (activityTypes);
        addActivityType.setSelectedIndex (1);    //sets default combo box selection to 'School'
        addActivityType.addActionListener (new ComboBoxListener ());
        typePanel.add (addActivityType);
        activityInfoAdd.add (typePanel);
        
        JPanel titlePanel = new JPanel ();
        titlePanel.setLayout (new FlowLayout (FlowLayout.LEADING));
        titlePanel.add (new JLabel ("Title:"));
        activityTitle = new JTextField (TEXT_FIELD_LENGTH);
        titlePanel.add (activityTitle);
        activityInfoAdd.add (titlePanel);
        
        JPanel startTimePanel = new JPanel ();
        startTimePanel.setLayout (new GridLayout (2, 1));
        startTimePanel.add (new JLabel ("Start Time (dd/mm/yy hh:mm):"));
        activityStartTime = new JTextField (TEXT_FIELD_LENGTH);
        startTimePanel.add (activityStartTime);
        activityInfoAdd.add (startTimePanel);
        
        JPanel stopTimePanel = new JPanel ();
        stopTimePanel.setLayout (new GridLayout (2, 1));
        stopTimePanel.add (new JLabel ("End Time (dd/mm/yy hh:mm):"));
        activityEndTime = new JTextField (TEXT_FIELD_LENGTH);
        stopTimePanel.add (activityEndTime);
        activityInfoAdd.add (stopTimePanel);
        
        locationPanel = new JPanel ();
        locationPanel.setLayout (new FlowLayout (FlowLayout.LEADING));
        locationPanel.add (new JLabel ("Location:"));
        activityLocation = new JTextField (TEXT_FIELD_LENGTH);
        locationPanel.add (activityLocation);
        locationPanel.setVisible (false);
        activityInfoAdd.add (locationPanel);
        
        JPanel commentPanel = new JPanel ();
        commentPanel.setLayout (new FlowLayout (FlowLayout.LEADING));
        commentPanel.add (new JLabel ("Comment:"));
        activityComment = new JTextField (TEXT_FIELD_LENGTH);
        commentPanel.add (activityComment);
        activityInfoAdd.add (commentPanel);
        
        addPanel.add (activityInfoAdd, BorderLayout.CENTER);
        
        //sets panel where the 'reset' and 'enter' buttons are added to
        JPanel resetEnterPanel = new JPanel ();
        resetEnterPanel.setLayout (new GridLayout (6, 1));
        resetEnterPanel.setBorder (BorderFactory.createLineBorder (Color.BLACK));
        
        resetEnterPanel.add (new JLabel (""));
        
        JPanel resetPanel = new JPanel ();
        resetPanel.setLayout (new FlowLayout ());
        JButton resetButton = new JButton ("Reset");
        resetButton.addActionListener (new AddButtonsListener ());
        resetPanel.add (resetButton);
        resetEnterPanel.add (resetPanel);

        resetEnterPanel.add (new JLabel (""));
        resetEnterPanel.add (new JLabel (""));
        
        JPanel enterPanel = new JPanel ();
        enterPanel.setLayout (new FlowLayout ());
        JButton enterButton = new JButton ("Enter");
        enterButton.addActionListener (new AddButtonsListener ());
        enterPanel.add (enterButton);
        resetEnterPanel.add (enterPanel);
        
        resetEnterPanel.add (new JLabel (""));
        
        addPanel.add (resetEnterPanel, BorderLayout.EAST);
        
        //sets panel for where messages related to adding activities are placed to (via a text area)
        JPanel messagesPanel = new JPanel ();
        messagesPanel.setLayout (new FlowLayout ());
        messagesPanel.setBorder (BorderFactory.createLineBorder (Color.BLACK));
        messagesPanel.add (new JLabel ("Messages:"));
        messages = new JTextArea (TEXT_AREA_DEPTH, TEXT_AREA_LENGTH);
        messages.setEditable (false);
        JScrollPane messageScroll = new JScrollPane (messages);
        messageScroll.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        messageScroll.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        messagesPanel.add (messageScroll);
        
        addPanel.add (messagesPanel, BorderLayout.SOUTH);
    }
    
    private class ComboBoxListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent event)
        {
            JComboBox typeSelect = (JComboBox) event.getSource ();
            String typeString = (String) typeSelect.getSelectedItem ();

            //sets location text field visible and not visible if combo box selection of Activity type is at 'Other' or not
            if (typeString.equals ("Other"))
            {
                locationPanel.setVisible (true);
            }
            else
            {
                locationPanel.setVisible (false);
            }
            
            validate ();
        }
    }
    
    private class AddButtonsListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent event)
        {
            String action = event.getActionCommand ();
        
            if (action.equals ("Reset"))
            {
                //empties all text fields
                activityTitle.setText ("");
                activityStartTime.setText ("");
                activityEndTime.setText ("");
                activityLocation.setText ("");
                activityComment.setText ("");
            }
            else if (action.equals ("Enter"))
            {
                //gather user input from text fields
                String title = activityTitle.getText ();
                Time start = Time.timeParser (activityStartTime.getText ());
                Time end = Time.timeParser (activityEndTime.getText ());
                String location = activityLocation.getText ();
                String comment = activityComment.getText ();
                if (comment.equals (""))
                {
                    comment = null;
                }
                
                //determine type of Activity user wants to make and try to make it, else an error message is set on the messages text area and the user is prompted to try again
                Activity newActivity = null;
                String type = (String) addActivityType.getSelectedItem ();
                if (type.equals ("Home"))
                {
                    try
                    {
                        newActivity = new HomeActivity (title, start, end, comment);
                    }
                    catch (BadActivityException exception)
                    {
                        String endMessage = exception.getMessage () + "\nPlease change input and try again";
                        messages.setText (endMessage);
                        return;
                    }
                }
                else if (type.equals ("School"))
                {
                    try
                    {
                        newActivity = new SchoolActivity (title, start, end, comment);
                    }
                    catch (BadActivityException exception)
                    {
                        String endMessage = exception.getMessage () + "\nPlease change input and try again";
                        messages.setText (endMessage);
                        return;
                    }
                }
                else if (type.equals ("Other"))
                {
                    try
                    {
                        newActivity = new OtherActivity (title, start, end, location, comment);
                    }
                    catch (BadActivityException exception)
                    {
                        String endMessage = exception.getMessage () + "\nPlease change input and try again";
                        messages.setText (endMessage);
                        return;
                    }
                }
                else
                {
                    messages.setText ("Error - Encountered unexpected add error - Please try again");
                    return;
                }
                
                //if Activity add is successful, it is added and the resulting Activity is outputted as a final confirmation
                mainPlanner.addToPlanner (newActivity);
                String successMessage = newActivity.toString () + "\n\nActivity Added Successfully";
                messages.setText (successMessage);
            }
            else
            {
                System.out.println ("Unexpected Add Panel Button Logic Error");
            }

            validate ();
        }
    }
    
    private void setSearchPanel ()
    {
        //creates panel where all search activity-related items are placed into
        searchPanel = new JPanel ();
        searchPanel.setLayout (new BorderLayout ());
        searchPanel.setPreferredSize (new Dimension (MAIN_PANEL_WIDTH, MAIN_PANEL_HEIGHT));    //sets rigid panel size to place components to appropriate areas
        
        //sets panel for the main search activity components to be added to (combo boxes and text fields)
        JPanel activityInfoSearch = new JPanel ();
        activityInfoSearch.setLayout (new FlowLayout (FlowLayout.LEADING));
        activityInfoSearch.setBorder (BorderFactory.createLineBorder (Color.BLACK));
        
        JPanel searchHeader = new JPanel ();
        searchHeader.setLayout (new FlowLayout (FlowLayout.LEADING));
        searchHeader.add (new JLabel ("Searching Activities:"));
        activityInfoSearch.add (searchHeader);
        
        //adds combo box of Activity types to search for
        JPanel typePanel = new JPanel ();
        typePanel.setLayout (new FlowLayout (FlowLayout.LEADING));
        typePanel.add (new JLabel ("Type:"));
        String[] activityTypes = {"", "Home", "School", "Other"};
        searchActivityType = new JComboBox (activityTypes);
        searchActivityType.setSelectedIndex (0);    //sets default combo box selection to ''
        typePanel.add (searchActivityType);
        activityInfoSearch.add (typePanel);
        
        JPanel titlePanel = new JPanel ();
        titlePanel.setLayout (new FlowLayout (FlowLayout.LEADING));
        titlePanel.add (new JLabel ("Title:"));
        activityKeywords = new JTextField (TEXT_FIELD_LENGTH);
        titlePanel.add (activityKeywords);
        activityInfoSearch.add (titlePanel);
        
        JPanel startTimePanel = new JPanel ();
        startTimePanel.setLayout (new GridLayout (2, 1));
        startTimePanel.add (new JLabel ("Start Time (dd/mm/yy hh:mm):"));
        startSearchTime = new JTextField (TEXT_FIELD_LENGTH);
        startTimePanel.add (startSearchTime);
        activityInfoSearch.add (startTimePanel);
        
        JPanel stopTimePanel = new JPanel ();
        stopTimePanel.setLayout (new GridLayout (2, 1));
        stopTimePanel.add (new JLabel ("End Time (dd/mm/yy hh:mm):"));
        endSearchTime = new JTextField (TEXT_FIELD_LENGTH);
        stopTimePanel.add (endSearchTime);
        activityInfoSearch.add (stopTimePanel);
        
        searchPanel.add (activityInfoSearch, BorderLayout.CENTER);
        
        //sets panel where the 'reset' and 'enter' buttons are added to
        JPanel resetEnterPanel = new JPanel ();
        resetEnterPanel.setLayout (new GridLayout (6, 1));
        resetEnterPanel.setBorder (BorderFactory.createLineBorder (Color.BLACK));
        
        resetEnterPanel.add (new JLabel (""));
        
        JPanel resetPanel = new JPanel ();
        resetPanel.setLayout (new FlowLayout ());
        JButton resetButton = new JButton ("Reset");
        resetButton.addActionListener (new SearchButtonsListener ());
        resetPanel.add (resetButton);
        resetEnterPanel.add (resetPanel);

        resetEnterPanel.add (new JLabel (""));
        resetEnterPanel.add (new JLabel (""));
        
        JPanel enterPanel = new JPanel ();
        enterPanel.setLayout (new FlowLayout ());
        JButton enterButton = new JButton ("Enter");
        enterButton.addActionListener (new SearchButtonsListener ());
        enterPanel.add (enterButton);
        resetEnterPanel.add (enterPanel);
        
        resetEnterPanel.add (new JLabel (""));
        
        searchPanel.add (resetEnterPanel, BorderLayout.EAST);
        
        //sets panel for where messages related to searching activities are placed to (via a text area), including search results
        JPanel messagesPanel = new JPanel ();
        messagesPanel.setLayout (new FlowLayout ());
        messagesPanel.setBorder (BorderFactory.createLineBorder (Color.BLACK));
        messagesPanel.add (new JLabel ("Search Results:"));
        searchResultsBox = new JTextArea (TEXT_AREA_DEPTH, TEXT_AREA_LENGTH);
        searchResultsBox.setEditable (false);
        JScrollPane messageScroll = new JScrollPane (searchResultsBox);
        messageScroll.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        messageScroll.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        messagesPanel.add (messageScroll);
        
        searchPanel.add (messagesPanel, BorderLayout.SOUTH);
    }
    
    private class SearchButtonsListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent event)
        {
            String action = event.getActionCommand ();

            if (action.equals ("Reset"))
            {
                //empties all text fields
                activityKeywords.setText ("");
                startSearchTime.setText ("");
                endSearchTime.setText ("");
            }
            else if (action.equals ("Enter"))
            {
                //sets search parameters based on user input
                String typeToSearch = (String) searchActivityType.getSelectedItem ();
                if (typeToSearch.equals (""))
                {
                    typeToSearch = null;
                }
                String titleString = activityKeywords.getText ();
                if (titleString.equals (""))
                {
                    titleString = null;
                }
                
                Time searchPeriodStart;
                Time searchPeriodEnd;
               
                String startString = startSearchTime.getText ();
                if (startString.equals (""))
                {
                    searchPeriodStart = null;
                }
                else
                {
                    searchPeriodStart = Time.timeParser (startString);
                    if (searchPeriodStart == null)    //checks for valid time if anything is entered in start time text field
                    {
                        searchResultsBox.setText ("Invalid Start Time Entered - Please enter a different time and try again");
                        return;
                    }
                }
                
                String stopString = endSearchTime.getText ();
                if (stopString.equals (""))
                {
                    searchPeriodEnd = null;
                }
                else
                {
                    searchPeriodEnd = Time.timeParser (stopString);
                    if (searchPeriodEnd == null)    //checks for valid time if anything is entered in endt time text field
                    {
                        searchResultsBox.setText ("Invalid End Time Entered - Please enter a different time and try again");
                        return;
                    }
                    else if (searchPeriodStart != null && (searchPeriodStart.compareTo (searchPeriodEnd) != 0 && searchPeriodStart.compareTo (searchPeriodEnd) != -1))    //checks if entered end time is equal to or follows given start time if any
                    {
                        searchResultsBox.setText ("End Time Entered does not follow Start Time Entered - Please enter a different time and try again");
                        return;
                    }
                }
                
                //gather any search results or messages realted to searching activities and places them on search results text area
                ArrayList<String> results = mainPlanner.searchMatches (titleString, searchPeriodStart, searchPeriodEnd, typeToSearch);
                String finalMessageString = "";
                for (int i = 0; i < results.size (); i ++)
                {
                    finalMessageString = finalMessageString + results.get (i);
                }
                searchResultsBox.setText (finalMessageString);
            }
            else
            {
                System.out.println ("Unexpected Search Panel Buttons Logic Error");
            }

            validate ();
        }
    }
    
    private class MenuListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent event)
        {
            String actionString = event.getActionCommand ();

            if (actionString.equals ("Add"))
            {
                //sets add panel to be visible
                introPanel.setVisible (false);
                addPanel.setVisible (true);
                searchPanel.setVisible (false);
                
                //if it is the first time the user chooses 'Add', an instruction window pops out for adding activities
                if (addInstructions == false)
                {
                    AddInstructionsWindow addTutorial = new AddInstructionsWindow ();
                    addTutorial.setVisible (true);
                    addInstructions = true;
                }
            }
            else if (actionString.equals ("Search"))
            {
                //sets search panel to be visible
                introPanel.setVisible (false);
                addPanel.setVisible (false);
                searchPanel.setVisible (true);
                
                //if it is the first time the user chooses 'Search', an instruction window pops out for searching activities
                if (searchInstructions == false)
                {
                    SearchInstructionsWindow searchTutorial = new SearchInstructionsWindow ();
                    searchTutorial.setVisible (true);
                    searchInstructions = true;
                }
            }
            else if (actionString.equals ("Quit"))
            {
                //outputs the exit check window to reaffirm exit by user
                ExitWindow lastCheckWindow = new ExitWindow ();
                lastCheckWindow.setVisible (true);
            }
            else
            {
                System.out.println ("Unexpected Menu Logic Error");
            }

            validate ();
        }
    }
    
    private class AddInstructionsWindow extends JFrame
    {
        public AddInstructionsWindow ()
        {
            super ();
            setSize (INSTRUCT_WIDTH, INSTRUCT_HEIGHT);
            
            setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
            
            setTitle ("Add Instructions");
            getContentPane ().setBackground (Color.GREEN);
            
            //sets text informing user of how to add Activities
            setLayout (new FlowLayout (FlowLayout.LEADING));
            add (new JLabel ("To add Activities to your DayPlanner,"));
            add (new JLabel ("enter in the fields a Title, a Start Time, and End Time and a Comment"));
            add (new JLabel ("and click 'Enter'!                                              "));
            add (new JLabel ("                                                                "));
            add (new JLabel ("Choose an Activity type to save your Activity under by using"));
            add (new JLabel ("the provided combo box."));
            add (new JLabel ("                                                      "));
            add (new JLabel ("If you choose 'Other', please also enter a Location for the Activity."));
            add (new JLabel ("                                                                     "));
            add (new JLabel ("To empty all text fields, click 'Reset'."));
            add (new JLabel ("                                                                     "));
            add (new JLabel ("NOTE: You must have a Title to create and store an Activity, and the"));
            add (new JLabel ("Start Time must precede the End Time."));
            add (new JLabel ("                                                      "));
            add (new JLabel ("Also, please follow the given format for the Time text fields to add"));
            add (new JLabel ("Activities successfully."));
        }
    }
    
    private class SearchInstructionsWindow extends JFrame
    {
        public SearchInstructionsWindow ()
        {
            super ();
            setSize (INSTRUCT_WIDTH + 20, INSTRUCT_HEIGHT + 45);
            
            setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
            
            setTitle ("Search Instructions");
            getContentPane ().setBackground (Color.GREEN);
            
            //sets text informing user of how to search for Activities
            setLayout (new FlowLayout (FlowLayout.LEADING));
            add (new JLabel ("To search for Activities in the DayPlanner,"));
            add (new JLabel ("enter in the fields a Title, a Start Time, and End Time and click 'Enter'!"));
            add (new JLabel ("                                                      "));
            add (new JLabel ("Choose an Activity type to search for, or choose '' to search for all"));
            add (new JLabel ("types of Activities."));
            add (new JLabel ("The Title field will search for Activities that match the keywords you input"));
            add (new JLabel ("(minus case-differences), or leave it empty to search for all titles."));
            add (new JLabel ("The Time fields will limit the search period of Activities, or leave them"));
            add (new JLabel ("empty to search Activities of any Time."));
            add (new JLabel ("                                                                                 "));
            add (new JLabel ("To empty all text fields, click 'Reset'."));
            add (new JLabel ("                                                                             "));
            add (new JLabel ("NOTE: If you enter both a Start Time and End Time for searching, the"));
            add (new JLabel ("Start Time must precede the End Time."));
            add (new JLabel ("                                                 "));
            add (new JLabel ("Also, please follow the given format for the Time text fields to search"));
            add (new JLabel ("Activities successfully."));      
        }
    }
    
    private class ExitCheck extends WindowAdapter
    {
        @Override
        public void windowClosing (WindowEvent event)
        {
            //outputs the ExitWindow to allow user to finalize quit when clicking 'x'
            ExitWindow lastCheckWindow = new ExitWindow ();
            lastCheckWindow.setVisible (true);
        }
    }
    
    private class ExitWindow extends JFrame implements ActionListener
    {
        public ExitWindow ()
        {
            super ();
            setSize (EXIT_WIDTH, EXIT_HEIGHT);
            
            setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);    //allows main exit control to be at the buttons
            
            setTitle ("Exit?");
            getContentPane ().setBackground (Color.YELLOW);
            
            setLayout (new BorderLayout ());
            add (new JLabel ("Are you sure you want to quit?"), BorderLayout.CENTER);
            
            //creates panel of 'Yes' and 'No' Buttons to finalize a quit of DayPlanner program
            JPanel yesNoPanel = new JPanel ();
            yesNoPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));
            yesNoPanel.setBackground (Color.ORANGE);
            JButton saveButton = new JButton ("Yes");
            saveButton.addActionListener (this);
            yesNoPanel.add (saveButton);
            JButton cancelButton = new JButton ("No");
            cancelButton.addActionListener (this);
            yesNoPanel.add (cancelButton);
            
            add (yesNoPanel, BorderLayout.SOUTH);           
        }
        
        @Override
        public void actionPerformed (ActionEvent event)
        {
            String exitCommand = event.getActionCommand ();
            
            if (exitCommand.equals ("Yes"))
            {
                //saves Activities to given output stream and outputs a ThankYou window
                mainPlanner.saveActivities (saveFile);
                saveFile.close ();
                ThankYouWindow lastWindow = new ThankYouWindow ();
                lastWindow.setVisible (true);
            }
            else if (exitCommand.equals ("No"))
            {
                dispose ();
            }
            else
            {
                System.out.println ("Unexpected Exit Window Logic Error");
            }
        }
    }
    
    private class ThankYouWindow extends JFrame
    {
        public ThankYouWindow ()
        {
            super ();
            setSize (FINAL_WIDTH, FINAL_HEIGHT);
            
            setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
            
            setTitle ("Exit");
            getContentPane ().setBackground (Color.YELLOW);
            
            setLayout (new FlowLayout ());
            add (new JLabel ("Thank you for using the program!"));      
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main (String[] args)
    {
        DayPlanner mainPlanner = new DayPlanner ();
        
        int numOfFiles = args.length;
        Scanner fileInput = null;
        PrintWriter saveFile = null;
        
        if (numOfFiles == 1)    //prepares output file stream
        {
            try
            {
                saveFile = new PrintWriter (new FileOutputStream (args[0]));
            }
            catch (FileNotFoundException error)
            {
                System.out.println ("Error - Could not open file to save to: " + args[0]);
                System.exit (0);
            }
        }
        else if (numOfFiles == 2)    //loads input file and prepares output file stream
        {
            try
            {
                fileInput = new Scanner (new FileInputStream (args[0]));
            }
            catch (FileNotFoundException error)
            {
                System.out.println ("Error - Could not open file to load from: " + args[0]);
                System.exit (0);
            }

            mainPlanner.loadFile (fileInput);
            fileInput.close ();
            
            try
            {
                saveFile = new PrintWriter (new FileOutputStream (args[1]));
            }
            catch (FileNotFoundException error)
            {
                System.out.println ("Error - Could not open file to save to: " + args[1]);
                System.exit (0);
            }
        }
        else
        {
            System.out.println ("Please Add 1 or 2 Arguments as Input and Output Files (Input File Optional)");
            System.exit (0);
        }
        
        DayPlannerGUI mainWindow = new DayPlannerGUI (mainPlanner, saveFile);
        mainWindow.setVisible (true);
    }
}
