/*****************************
Name: Jireh Agda
Student ID: 0795472
Date Last Modified: 2015 04 03
Dialogc Class (Java)
*****************************/

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.KeyStroke;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Dialogc is a class the edits .config files to be compiled to generate two .java a files that a user can combine their own .java files with to create a functioning GUI
 * @author agdaj
 */
public class Dialogc extends JFrame
{
    /**
     * Indicates pixel width of main window to be used
     */
    public static final int WIDTH = 630;
    /**
     * Indicates pixel height of main window to be used
     */
    public static final int HEIGHT = 650;

    //stores internal size data of some main componenets
    private static final int TEXT_FIELD_LENGTH = 30;
    private static final int TEXT_AREA_LENGTH = 50;
    private static final int TEXT_AREA_DEPTH = 30;

    //internal constants to indicate action after a save
    private static final int SAVE_THEN_NEW = 0;
    private static final int SAVE_THEN_OPEN = 1;
    private static final int SAVE_THEN_QUIT = 2;

    private static final String DEFAULT_NAME = "newFile";

    //attributes that keep track of a current files settings and progress
    private String currentFileName;
    private TitledBorder fileTitle;
    private JTextArea textArea;
    private JTextField statusBar;
    private boolean isItModified;
    private boolean isItNew;

    //attributes that a user chooses to modify how compilation and program running occurs
    private boolean compileWithLexYacc;
    private String javaCompiler;
    private String compileOptions;
    private String javaRun;
    private String runOptions;
    private String workingDirectory;
    private JMenuItem compileLabel;
    private JMenuItem compileOptionsLabel;
    private JMenuItem runLabel;
    private JMenuItem runOptionsLabel;
    private JMenuItem directoryLabel;

    //sets the file chooser for the GUI and a temp data to hold file names and file paths during JFileChooser, as well as a flag for overwriting
    private JFileChooser fileChooser;
    private String chosenFileName;
    private String filePath;

    //stores whether or not reserved button names have been declared in the .config files to be used in the generated GUI
    private boolean addRequested;
    private boolean deleteRequested;
    private boolean updateRequested;
    private boolean queryRequested;

    //stores database and Dialogc related locations (helps with database integration and images
    private String dialogcLocation;
    private String configKerberos;
    private String configJaas;

    //stores error data from any exec calls made by the program
    private JTextArea errorArea;
    private JScrollPane errorScroll;

    //loads ParameterManager(C)/JNI shared library for the native functions below
    static { System.loadLibrary ("JNIpm"); }

    //methods Dialogc uses to levearage the ParameterManager library to parse, load and compile the .config files
    private native static int initPManager ();
    private native static int firstParse (String filePath);
    private native static int secondParse (String filePath);
    private native static String retrieveTitle ();
    private native static String retrieveFieldName ();
    private native static String retrieveButtonName ();
    private native static String getType (String fieldName);
    private native static String getListener (String buttonName);
    private native static void cleanUpPM ();

    /**
     * Creates a Dialogc window that starts with the default name "newFile"
     */
    public Dialogc ()
    {
        this (DEFAULT_NAME);
    }

    /**
     * Creates a Dialogc window that starts with the passed in parameter name fileName
     * @param fileName the name of the new file at start-up
     */
    public Dialogc (String fileName)
    {
        super ();
        setSize (WIDTH, HEIGHT);

        //initializes the PManagers at the JNI level
        int checkPMInit = Dialogc.initPManager ();
        if (checkPMInit == 0)
        {
            System.err.println ("Error - Could not initialize ParameterManager");
            System.exit (0);
        }

        setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener (new ExitCheck ());    //adds window listener to enable a final save check of exit before a full exit via another window
        setTitle ("Dialogc");

        //initial settings are set here
        currentFileName = fileName;
        isItModified = false;
        isItNew = true;
        compileWithLexYacc = true;
        workingDirectory = ".";
        setDialogcDefaults ();

        fileChooser = new JFileChooser (new File (System.getProperty ("user.dir")))    //makes JFileChooser start at current directory
        {
            //Code to override function of selecting save in the save dialog to install a confirm to overwrite
            //Code based on: http://stackoverflow.com/questions/3651494/jfilechooser-with-confirmation-dialog
            @Override
            public void approveSelection()
            {
                File currentFile = getSelectedFile ();
                if (currentFile.getName ().endsWith (".config") == false)
                {
                    currentFile = new File (currentFile.getAbsolutePath () + ".config");
                }

                //if the chosen file already exists and saving is the current action, an overwrite dialog appears in the form of a JOptionPane
                if (currentFile.exists () && getDialogType () == SAVE_DIALOG)
                {
                    int result = JOptionPane.showConfirmDialog (this, currentFile.getName () + " already exists.\nWould you like to overwrite it?", "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result)
                    {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection ();
                            return;

                        case JOptionPane.NO_OPTION:
                            return;

                        case JOptionPane.CLOSED_OPTION:
                            return;

                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection ();
                            return;

                        default:
                            System.err.println ("Error - Unexpected Overwrite Window Error");
                            return;
                    }
                }

                super.approveSelection();
            }
        };
        FileNameExtensionFilter configFilter = new FileNameExtensionFilter ("Config Files (.config)", "config");    //adds the .config filter
        fileChooser.setFileFilter (configFilter);
        fileChooser.setAcceptAllFileFilterUsed (false);    //removes the all files filter to leave only the .config filter

        setJMenuBar (setMenu ());

        setLayout (new BorderLayout ());
        add (setButtonMap (), BorderLayout.NORTH);
        add (setTextArea (), BorderLayout.CENTER);
        add (setStatusBar (), BorderLayout.SOUTH);

        setErrorArea ();
    }

    //set DialogcDefaults reads default information from a .dialogc file residing in the home directory
    private void setDialogcDefaults ()
    {
        javaCompiler = null;
        compileOptions = null;
        javaRun = null;
        runOptions = null;
        dialogcLocation = null;
        configKerberos = null;
        configJaas = null;

        //the function retrieves the Home environment variable to get the .dialogc file
        String homePath = System.getenv ("HOME");
        Scanner dialogcFile = null;
        try
        {
            dialogcFile = new Scanner (new FileInputStream (homePath + "/.dialogc"));
        }
        catch (FileNotFoundException exception)
        {
            System.err.println ("Unable to find .dialogc to gather defaults");
            System.err.println ("Please ensure .dialogc is in the home directory");
            System.exit (0);
        }

        boolean doneRead = false;
        while (doneRead == false)
        {
            try
            {
                //the variables are assigned with the defaults found in the .dialogc file plus any necessary additions/modifications
                String dialogcLine = dialogcFile.nextLine ();
                if (dialogcLine.startsWith ("JAVA_C="))
                {
                    javaCompiler = dialogcLine.substring (7, dialogcLine.length ());
                }
                else if (dialogcLine.startsWith ("C_OPTIONS="))
                {
                    compileOptions = dialogcLine.substring (10, dialogcLine.length ());
                }
                else if (dialogcLine.startsWith ("JAVA_R="))
                {
                    javaRun = dialogcLine.substring (7, dialogcLine.length ());
                }
                else if (dialogcLine.startsWith ("R_OPTIONS="))
                {
                    runOptions = dialogcLine.substring (10, dialogcLine.length ());
                }
                else if (dialogcLine.startsWith ("LOCATION="))
                {
                    dialogcLocation = dialogcLine.substring (9, dialogcLine.length ()) + "/Dialogc/";
                    configKerberos = dialogcLocation + "/config/krb5.conf";
                    configJaas = dialogcLocation + "/config/jaas.conf";
                }
                else
                {
                    System.err.println ("Unknown .dialogc line found");
                }
            }
            catch (NoSuchElementException exception)
            {
                doneRead = true;    //ends loop when exception occurs
            }
        }

        //if any default is left null, then the program exits, notifying the user the .dialogc file is missing default
        if (javaCompiler == null || compileOptions == null || javaRun == null || runOptions == null || dialogcLocation == null || configKerberos == null || configJaas == null)
        {
            System.err.println ("Error - .dialogc file is missing defaults");
            System.exit (0);
        }
    }

    private JMenuBar setMenu ()
    {
        //each big menu header and menu item in the config menu is outfitted with a mnemonic, accessed using ALT and the underlined letter
        //all other menu items have attached accelerators and using CTRL + the assigned key allows selection of the item from anywhere
        JMenu fileMenu = new JMenu ("File");
        fileMenu.setMnemonic (KeyEvent.VK_F);

        JMenuItem newItem = new JMenuItem ("New");
        newItem.addActionListener (new FileListener ());
        newItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        fileMenu.add (newItem);
        JMenuItem openItem = new JMenuItem ("Open");
        openItem.addActionListener (new FileListener ());
        openItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        fileMenu.add (openItem);
        JMenuItem saveItem = new JMenuItem ("Save");
        saveItem.addActionListener (new FileListener ());
        saveItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        fileMenu.add (saveItem);
        JMenuItem saveAsItem = new JMenuItem ("Save As");
        saveAsItem.addActionListener (new FileListener ());
        saveAsItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        fileMenu.add (saveAsItem);
        JMenuItem quitItem = new JMenuItem ("Quit");
        quitItem.addActionListener (new FileListener ());
        quitItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        fileMenu.add (quitItem);

        JMenu compileMenu = new JMenu ("Compile");
        compileMenu.setMnemonic (KeyEvent.VK_C);

        JMenuItem compileItem = new JMenuItem ("Compile");
        compileItem.addActionListener (new CompileListener ());
        compileItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        compileMenu.add (compileItem);
        JMenuItem compileRunItem = new JMenuItem ("Compile and run");
        compileRunItem.addActionListener (new CompileListener ());
        compileRunItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        compileMenu.add (compileRunItem);

        JMenu configMenu = new JMenu ("Config");
        configMenu.setMnemonic (KeyEvent.VK_O);

        //each config menu item is a mini menu containing the current setting and a way to customize it
        JMenu javaCompilerItem = new JMenu ("Java Compiler");
        javaCompilerItem.setMnemonic (KeyEvent.VK_J);
        compileLabel = new JMenuItem (javaCompiler);
        compileLabel.setEnabled (false);
        javaCompilerItem.add (compileLabel);
        JMenuItem customCompiler = new JMenuItem ("Customize...");
        customCompiler.setActionCommand ("Java Compiler");
        customCompiler.addActionListener (new ConfigListener ());
        customCompiler.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_J, ActionEvent.CTRL_MASK));
        javaCompilerItem.add (customCompiler);
        configMenu.add (javaCompilerItem);
        JMenu compileOptionsItem = new JMenu ("Compile options");
        compileOptionsItem.setMnemonic (KeyEvent.VK_C);
        compileOptionsLabel = new JMenuItem (compileOptions);
        compileOptionsLabel.setEnabled (false);
        compileOptionsItem.add (compileOptionsLabel);
        JMenuItem customCompile = new JMenuItem ("Customize...");
        customCompile.setActionCommand ("Compile options");
        customCompile.addActionListener (new ConfigListener ());
        customCompile.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        compileOptionsItem.add (customCompile);
        configMenu.add (compileOptionsItem);
        JMenu runTimeItem = new JMenu ("Java Run-time");
        runTimeItem.setMnemonic (KeyEvent.VK_R);
        runLabel = new JMenuItem (javaRun);
        runLabel.setEnabled (false);
        runTimeItem.add (runLabel);
        JMenuItem customRunner = new JMenuItem ("Customize...");
        customRunner.setActionCommand ("Java Run-time");
        customRunner.addActionListener (new ConfigListener ());
        customRunner.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_T, ActionEvent.CTRL_MASK));
        runTimeItem.add (customRunner);
        configMenu.add (runTimeItem);
        JMenu runOptionsItem = new JMenu ("Run-time options");
        runOptionsItem.setMnemonic (KeyEvent.VK_O);
        runOptionsLabel = new JMenuItem (runOptions);
        runOptionsLabel.setEnabled (false);
        runOptionsItem.add (runOptionsLabel);
        JMenuItem customRun = new JMenuItem ("Customize...");
        customRun.setActionCommand ("Run-time options");
        customRun.addActionListener (new ConfigListener ());
        customRun.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_U, ActionEvent.CTRL_MASK));
        runOptionsItem.add (customRun);
        configMenu.add (runOptionsItem);
        JMenu directoryItem = new JMenu ("Working Directory");
        directoryItem.setMnemonic (KeyEvent.VK_W);
        directoryLabel = new JMenuItem (workingDirectory);
        directoryLabel.setEnabled (false);
        directoryItem.add (directoryLabel);
        JMenuItem customDirectory = new JMenuItem ("Customize...");
        customDirectory.setActionCommand ("Working Directory");
        customDirectory.addActionListener (new ConfigListener ());
        customDirectory.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        directoryItem.add (customDirectory);
        configMenu.add (directoryItem);
        JMenu compileModeItem = new JMenu ("Compile Mode");
        compileModeItem.setMnemonic (KeyEvent.VK_M);
        JRadioButtonMenuItem lexYaccMode = new JRadioButtonMenuItem ("Lex/Yacc Compiler");
        lexYaccMode.setMnemonic (KeyEvent.VK_L);
        lexYaccMode.addActionListener (new CompileModeListener ());
        lexYaccMode.setSelected (true);    //sets the Lex/Yacc compiler optiona as the default selected option
        compileModeItem.add (lexYaccMode);
        JRadioButtonMenuItem jniMode = new JRadioButtonMenuItem ("JNI Compiler");
        jniMode.setMnemonic (KeyEvent.VK_J);
        jniMode.addActionListener (new CompileModeListener ());
        compileModeItem.add (jniMode);
        ButtonGroup compileModeGroup = new ButtonGroup ();    //groups the two radio buttons together so that only one may be active at a time
        compileModeGroup.add (lexYaccMode);
        compileModeGroup.add (jniMode);
        configMenu.add (compileModeItem);

        JMenu helpMenu = new JMenu ("Help");
        helpMenu.setMnemonic (KeyEvent.VK_H);

        JMenuItem helpItem = new JMenuItem ("Help");
        helpItem.addActionListener (new HelpListener ());
        helpItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_H, ActionEvent.CTRL_MASK));
        helpMenu.add (helpItem);
        JMenuItem aboutItem = new JMenuItem ("About");
        aboutItem.addActionListener (new HelpListener ());
        aboutItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_B, ActionEvent.CTRL_MASK));
        helpMenu.add (aboutItem);

        JMenuBar mainBar = new JMenuBar ();    //create menu bar that will be placed at the top of the main window
        mainBar.add (fileMenu);
        mainBar.add (compileMenu);
        mainBar.add (configMenu);
        mainBar.add (helpMenu);

        return (mainBar);
    }

    private class CompileModeListener implements ActionListener
    {
        //listeners determine whether user intends to compile with the lex/yacc compiler or with the JNI compiler
        @Override
        public void actionPerformed (ActionEvent event)
        {
            String modeCommand = event.getActionCommand ();

            if (modeCommand.equals ("Lex/Yacc Compiler"))
            {
                compileWithLexYacc = true;
            }
            else if (modeCommand.equals ("JNI Compiler"))
            {
                compileWithLexYacc = false;
            }
            else
            {
                System.err.println ("Error - Unexpected Compile Mode Error");
            }

            validate ();
        }
    }

    private JPanel setButtonMap ()
    {
        JPanel buttonMap = new JPanel ();
        buttonMap.setLayout (new FlowLayout (FlowLayout.LEADING));
        buttonMap.setBackground (Color.GRAY);

        //each button loads an image from the images folder, sets its action command and adds the appropriate listener
        JButton newButton = new JButton (new ImageIcon (dialogcLocation + "images/new.gif"));
        newButton.setPreferredSize (new Dimension (30, 30));
        newButton.setActionCommand ("New");
        newButton.addActionListener (new FileListener ());
        buttonMap.add (newButton);

        JButton openButton = new JButton (new ImageIcon (dialogcLocation + "images/open.gif"));
        openButton.setPreferredSize (new Dimension (30, 30));
        openButton.setActionCommand ("Open");
        openButton.addActionListener (new FileListener ());
        buttonMap.add (openButton);

        JButton saveButton = new JButton (new ImageIcon (dialogcLocation + "images/save.gif"));
        saveButton.setPreferredSize (new Dimension (30, 30));
        saveButton.setActionCommand ("Save");
        saveButton.addActionListener (new FileListener ());
        buttonMap.add (saveButton);

        JButton saveAsButton = new JButton (new ImageIcon (dialogcLocation + "images/saveAs.gif"));
        saveAsButton.setPreferredSize (new Dimension (30, 30));
        saveAsButton.setActionCommand ("Save As");
        saveAsButton.addActionListener (new FileListener ());
        buttonMap.add (saveAsButton);

        JButton compileButton = new JButton (new ImageIcon (dialogcLocation + "images/compile.gif"));
        compileButton.setPreferredSize (new Dimension (30, 30));
        compileButton.setActionCommand ("Compile");
        compileButton.addActionListener (new CompileListener ());
        buttonMap.add (compileButton);

        JButton compileRunButton = new JButton (new ImageIcon (dialogcLocation + "images/compileRun.gif"));
        compileRunButton.setPreferredSize (new Dimension (30, 30));
        compileRunButton.setActionCommand ("Compile and run");
        compileRunButton.addActionListener (new CompileListener ());
        buttonMap.add (compileRunButton);

        JButton quitButton = new JButton (new ImageIcon (dialogcLocation + "images/quit.png"));
        quitButton.setPreferredSize (new Dimension (30, 30));
        quitButton.setActionCommand ("Quit");
        quitButton.addActionListener (new FileListener ());
        buttonMap.add (quitButton);

        return (buttonMap);
    }

    private JScrollPane setTextArea ()
    {
        //set border with filename title
        fileTitle = BorderFactory.createTitledBorder (currentFileName + ".config");
        fileTitle.setTitleJustification (TitledBorder.CENTER);

        textArea = new JTextArea (TEXT_AREA_DEPTH, TEXT_AREA_LENGTH);
        textArea.getDocument ().addDocumentListener (new ConfigDocumentListener ());    //adds document listener to track text changes
        textArea.setBorder (BorderFactory.createLineBorder (Color.BLACK));
        JScrollPane textScroll = new JScrollPane (textArea);
        textScroll.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        textScroll.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textScroll.setBorder (fileTitle);
        return (textScroll);
    }

    private class ConfigDocumentListener implements DocumentListener
    {
        //all methods in this listener set flag isItModified to true and changes the status bar to add [modified]
        @Override
        public void insertUpdate (DocumentEvent docEvent)
        {
            isItModified = true;
            statusBar.setText ("Current Project: " + currentFileName + " [modified]");
        }

        @Override
        public void removeUpdate (DocumentEvent docEvent)
        {
            isItModified = true;
            statusBar.setText ("Current Project: " + currentFileName + " [modified]");
        }

        @Override
        public void changedUpdate (DocumentEvent docEvent)
        {
            isItModified = true;
            statusBar.setText ("Current Project: " + currentFileName + " [modified]");
        }
    }

    private JTextField setStatusBar ()
    {
        //sets the status bar with the default 'newFile' and not modified
        statusBar = new JTextField (TEXT_FIELD_LENGTH);
        statusBar.setBorder (BorderFactory.createLoweredBevelBorder ());
        statusBar.setEditable (false);
        statusBar.setHorizontalAlignment (SwingConstants.CENTER);
        statusBar.setText ("Current Project: " + currentFileName);

        return (statusBar);
    }

    private void setErrorArea ()
    {
        //initializes error area to be used when an exec call returns unsuccessfully
        errorArea = new JTextArea (TEXT_AREA_DEPTH - 25, TEXT_AREA_LENGTH - 10);
        errorArea.setBorder (BorderFactory.createLineBorder (Color.BLACK));
        errorArea.setEditable (false);
        errorScroll = new JScrollPane (errorArea);
        errorScroll.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        errorScroll.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }

    private class FileListener implements ActionListener
    {
        //the listener executes a subroutine depending on the choice a user makes with the file menu
        @Override
        public void actionPerformed (ActionEvent event)
        {
            String fileCommand = event.getActionCommand ();

            if (fileCommand.equals ("New"))
            {
                newConfig ();
            }
            else if (fileCommand.equals ("Open"))
            {
                openConfig ();
            }
            else if (fileCommand.equals ("Save"))
            {
                saveConfig ();
            }
            else if (fileCommand.equals ("Save As"))
            {
                //save as saves the file and then sets the new settings accroding to user input
                boolean yesSave = saveDialog ();
                if (yesSave == true)
                {
                    setNewSettings ();
                }
            }
            else if (fileCommand.equals ("Quit"))
            {
                quitConfig ();
            }
            else
            {
                System.err.println ("Unexpected File Menu Logic Error");
            }

            validate ();
        }
    }

    private class CompileListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent event)
        {
            String compileCommand = event.getActionCommand ();

            if (compileCommand.equals ("Compile"))
            {
                //the compilation process that will be used will be determined by the compileWIthLexYacc flag
                if (compileWithLexYacc == true)
                {
                    lexYaccCompileConfig ();
                }
                else
                {
                    compileConfig ();
                }
            }
            else if (compileCommand.equals ("Compile and run"))
            {
                //the compilation process that will be used will be determined by the compileWIthLexYacc flag
                boolean completeCompile;
                if (compileWithLexYacc == true)
                {
                    completeCompile = lexYaccCompileConfig ();
                }
                else
                {
                    completeCompile = compileConfig ();
                }

                if (completeCompile == true)
                {
                    //after a successful compilation, the program will then attempt to compile that actual .java files given the options
                    try
                    {
                        Runtime externalRuntime = Runtime.getRuntime ();
                        Process compileProcess = externalRuntime.exec (javaCompiler + " " + compileOptions + " -cp " + dialogcLocation + "/bin/:" + workingDirectory + "/" + currentFileName + " " + workingDirectory + "/" + currentFileName + "/" + currentFileName + ".java");

                        int errorCode = compileProcess.waitFor ();
                        if (errorCode != 0)
                        {
                            makeErrorWindow ("Error - Generated files could not be compiled with the current arguments", 540, 145, compileProcess);
                            validate ();
                            return;
                        }
                    }
                    catch (Exception thrown)
                    {
                        makeErrorWindow ("Error - Generated files could not be compiled with the current arguments", 540, 90);
                        validate ();
                        return;
                    }

                    //if .java compilation is successful, the program will attempt to run the main class, along with additional flags to help integrate database support
                    try
                    {
                        Runtime externalRuntime = Runtime.getRuntime ();
                        Process runProcess = externalRuntime.exec (javaRun + " " + runOptions + " -cp /usr/share/java/postgresql-jdbc4-9.1.jar:" + dialogcLocation + "/bin/:" + workingDirectory + "/" + currentFileName + " -Djava.security.krb5.conf=" + configKerberos + " -Djava.security.auth.login.config=" + configJaas + " " + currentFileName);

                        int errorCode = runProcess.waitFor ();
                        if (errorCode != 0)
                        {
                            makeErrorWindow ("Error - Generated files could not be run with the current arguments", 510, 145, runProcess);
                            validate ();
                            return;
                        }
                    }
                    catch (Exception thrown)
                    {
                        makeErrorWindow ("Error - Generated files could not be run with the current arguments", 510, 90);
                        validate ();
                        return;
                    }
                }
            }
            else
            {
                System.err.println ("Unexpected Compile Menu Logic Error");
            }

            validate ();
        }
    }

    private class ConfigListener implements ActionListener
    {
        //the listener determines which kind of window to pop up to change the specific argument during compilation or run-time
        @Override
        public void actionPerformed (ActionEvent event)
        {
            boolean browseButton;
            String configCommand = event.getActionCommand ();
            String windowTitle = "Change " + configCommand;

            //according to the action command, a browse button will appear in the change config window
            if (configCommand.equals ("Java Compiler"))
            {
                browseButton = true;
            }
            else if (configCommand.equals ("Compile options"))
            {
                browseButton = false;
            }
            else if (configCommand.equals ("Java Run-time"))
            {
                browseButton = true;
            }
            else if (configCommand.equals ("Run-time options"))
            {
                browseButton = false;
            }
            else if (configCommand.equals ("Working Directory"))
            {
                browseButton = false;
            }
            else
            {
                System.err.println ("Unexpected Config Menu Logic Error");
                return;
            }

            ChangeConfigWindow changeWindow = new ChangeConfigWindow (windowTitle, configCommand, browseButton);
            changeWindow.setVisible (true);
            validate ();
        }
    }

    private class HelpListener implements ActionListener
    {
        //the listener opens up either a help or about window for the user
        @Override
        public void actionPerformed (ActionEvent event)
        {
            String helpCommand = event.getActionCommand ();

            if (helpCommand.equals ("Help"))
            {
                //creates a panel containing important info about this assignment
                JPanel helpPanel = new JPanel ();
                helpPanel.setLayout (new GridLayout (22, 1));
                helpPanel.setBackground (Color.WHITE);
                helpPanel.add (new JLabel ("Thank you for using Dialogc!"));
                helpPanel.add (new JLabel (""));
                helpPanel.add (new JLabel ("Dialogc is a program where you can create, modify and save .config files to be compiled into a working GUI!"));
                helpPanel.add (new JLabel (""));
                helpPanel.add (new JLabel ("New! Dialogc now has database support! (Postgres)"));
                helpPanel.add (new JLabel ("Request for database functions by calling \"ADD\", \"DELETE\", \"UPDATE\" or \"QUERY\" in the buttons list"));
                helpPanel.add (new JLabel ("- Please do not set an ActionListener for these buttons. It may be ignored or seen as a parse error."));
                helpPanel.add (new JLabel ("- Database will only store up to 30 characters for 'string' fields and up to 10^17 for 'float' fields."));
                helpPanel.add (new JLabel (""));
                helpPanel.add (new JLabel ("This program's menu is mnemonic- and accelerator-supported!"));
                helpPanel.add (new JLabel ("To use mnemonics, type ALT+the designated key"));
                helpPanel.add (new JLabel ("To use accelerators, type CTRL+the designated key"));
                helpPanel.add (new JLabel (""));
                helpPanel.add (new JLabel ("NOTES:"));
                helpPanel.add (new JLabel ("- Please use '#' as the comment character of your .config files."));
                helpPanel.add (new JLabel ("All other characters will be treated as part of the compilation."));
                helpPanel.add (new JLabel ("- Please insert the appropriate Listeners into the working directory and/or add the proper arguments to compile/run your files."));
                helpPanel.add (new JLabel ("- Please include at least one field and one button in your file, the program will not compile it otherwise."));
                helpPanel.add (new JLabel ("- There may be no duplicate paramater name between the fields and buttons."));
                helpPanel.add (new JLabel ("- (For Lex/Yacc Compiler only) Parameters can only consist of digits, underscores and/or letters."));
                helpPanel.add (new JLabel ("- (For Lex/Yacc Compiler only) All string values (in \"\") may not have '\\n' or any form of whitespace as it is regarded as a parse error."));
                helpPanel.add (new JLabel ("- (For Lex/Yacc Compiler only) The title, fields and buttons parameter come before the other parameters else it is a parse error."));

                DismissableWindow helpWindow = new DismissableWindow ("Help", 970, 450, helpPanel);
                helpWindow.setVisible (true);
            }
            else if (helpCommand.equals ("About"))
            {
                //cretes a panel to be passed in to the DismissableWindow constructor
                JPanel nameNumber = new JPanel ();
                nameNumber.setLayout (new GridLayout (4, 1));
                nameNumber.setBackground (Color.WHITE);
                JLabel nameLabel = new JLabel ("By: Jireh Agda");
                nameLabel.setHorizontalAlignment (JLabel.CENTER);
                nameNumber.add (nameLabel);
                JLabel numberLabel = new JLabel ("Student ID: 0795472");
                numberLabel.setHorizontalAlignment (JLabel.CENTER);
                nameNumber.add (numberLabel);
                JLabel dateLabel = new JLabel ("Date Last Modified: 2015 04 03");
                dateLabel.setHorizontalAlignment (JLabel.CENTER);
                nameNumber.add (dateLabel);
                JLabel descriptionLabel = new JLabel ("A4: Dialogc - Release Version");
                descriptionLabel.setHorizontalAlignment (JLabel.CENTER);
                nameNumber.add (descriptionLabel);

                DismissableWindow aboutWindow = new DismissableWindow ("About", 300, 140, nameNumber);
                aboutWindow.setVisible (true);
            }
            else
            {
                System.err.println ("Unexpected Help Menu Logic Error");
            }

            validate ();
        }
    }

    private void newConfig ()
    {
        //the file is checked to see if it has been modified, and prompts a save first if necessary
        if (isItModified == true)
        {
            saveThenAction (SAVE_THEN_NEW);
        }
        else
        {
            //if the current file has not been modified, all settings are reset to the defaults to represent a new project
            chosenFileName = DEFAULT_NAME;
            textArea.setText ("");
            setNewSettings ();
            isItNew = true;
            validate ();
        }
    }

    private void openConfig ()
    {
        //the file is checked to see if it has been modified, and prompts a save first if necessary
        if (isItModified == true)
        {
            saveThenAction (SAVE_THEN_OPEN);
        }
        else
        {
            fileChooser.setSelectedFile (new File (""));    //sets the name bar to empty in the dialog
            int openType = fileChooser.showOpenDialog (null);

            Scanner streamOpenFile;
            switch (openType)
            {
                case JFileChooser.APPROVE_OPTION:
                    File openFile = fileChooser.getSelectedFile ();
                    chosenFileName = openFile.getName ();
                    if (chosenFileName.endsWith (".config"))
                    {
                        //the true file name (-.config) is extracted from the name and stored to use later if modifications are successful
                        chosenFileName = chosenFileName.substring (0, chosenFileName.length () - 7);
                    }

                    //a stream to the chosen file is created if possible
                    try
                    {
                        streamOpenFile = new Scanner (new FileInputStream (openFile));
                    }
                    catch (FileNotFoundException exception)
                    {
                        makeErrorWindow ("Error - File could not be opened", 250, 90);
                        return;
                    }

                    boolean doneRead = false;
                    textArea.setText ("");
                    while (doneRead == false)
                    {
                        try
                        {
                            String fileLine = streamOpenFile.nextLine ();
                            textArea.append (fileLine + "\n");    //appends the total of the file contents line by line to be added to text area
                        }
                        catch (NoSuchElementException exception)
                        {
                            doneRead = true;    //ends loop when exception occurs
                        }
                    }

                    //the settings are adjusted with the newly opened file
                    isItNew = false;
                    streamOpenFile.close ();
                    setNewSettings ();
                    filePath = openFile.getAbsolutePath ();
                    validate ();
                    break;

                case JFileChooser.CANCEL_OPTION:
                    return;

                default:
                    System.err.println ("Error - Unknown Open Dialog Error Occurred");
            }
        }
    }

    private boolean saveConfig ()
    {
        boolean successSave;

        //if the current file is not new, a save saves, otherwise a save as JFileChooser opens
        if (isItNew == true)
        {
            successSave = saveDialog ();
        }
        else
        {
            successSave = saveConfigFile (new File (filePath));
        }

        if (successSave == true)
        {
            isItNew = false;
            setNewSettings ();
            return true;
        }
        else
        {
            return false;
        }
    }

    private void quitConfig ()
    {
        //the program checks for a modified file before quitting, prompting for a save first if necessary
        if (isItModified == true)
        {
            saveThenAction (SAVE_THEN_QUIT);
        }
        else
        {
            Dialogc.cleanUpPM ();
            System.exit (0);
        }
    }

    private void saveThenAction (int saveSituation)
    {
        int result = JOptionPane.showConfirmDialog (this, currentFileName + ".config has been modified.\nWould you like to save it?", "Save?", JOptionPane.YES_NO_CANCEL_OPTION);
        switch (result)
        {
            case JOptionPane.YES_OPTION:
                //if the user chooses to save a modified file before new/open/quit, the save occurs, using a dialog if the file is new
                boolean saveComplete = saveConfig ();
                if (saveComplete == false)
                {
                    return;
                }
                else
                {
                    isItNew = false;
                }
                break;

            case JOptionPane.NO_OPTION:
                //the user continues with the intended action if no is chosen
                break;

            //a closed or cancel option just returns the method, nothing happens
            case JOptionPane.CLOSED_OPTION:
                return;

            case JOptionPane.CANCEL_OPTION:
                return;

            default:
                System.err.println ("Error - Unexpected Save Then Action Window Error");
                return;
        }

        //the method continues accordingly to the intention of the calling method (make a new file, open a file, or exit)
        switch (saveSituation)
        {
            case SAVE_THEN_NEW:
                isItModified = false;
                newConfig ();
                break;

            case SAVE_THEN_OPEN:
                isItModified = false;
                openConfig ();
                break;

            case SAVE_THEN_QUIT:
                Dialogc.cleanUpPM ();
                System.exit (0);

            default:
                System.err.println ("Error - Unexpected Error After Save Then Action Window");
        }
    }

    private boolean saveDialog ()
    {
        //sets the text field of file name to empty is saving a new file, else sets the file name to already working name file
        if (isItNew == true)
        {
            fileChooser.setSelectedFile (new File (""));
        }
        else
        {
            fileChooser.setSelectedFile (new File (currentFileName));
        }

        int saveType = fileChooser.showSaveDialog (null);

        switch (saveType)
        {
            //for a successful chosen name, the name is retrieved and configured to have .config if necessary and the contents are saved
            case JFileChooser.APPROVE_OPTION:
                File saveFile = fileChooser.getSelectedFile ();
                chosenFileName = saveFile.getName ();
                if (!chosenFileName.endsWith (".config"))
                {
                    saveFile = new File (saveFile.getAbsolutePath() + ".config");
                }
                else
                {
                    //the true file name (-.config) is extracted from the name and stored to use later if modifications are successful
                    chosenFileName = chosenFileName.substring (0, chosenFileName.length () - 7);
                }
                filePath = saveFile.getAbsolutePath ();

                boolean saveSuccess = saveConfigFile (new File (filePath));
                return saveSuccess;

            //a cancel just simply means a retraction of wanting to save... no net changes happen
            case JFileChooser.CANCEL_OPTION:
                return false;

            default:
                System.err.println ("Error - Unknown Save Dialog Error Occurred");
                return false;
        }
    }

    private boolean saveConfigFile (File savingFile)
    {
        //saves that occur automatically (already has assigned name/not new) are sent here to do that
        PrintWriter fileToSave;
        try
        {
            fileToSave = new PrintWriter (new FileOutputStream (savingFile));
        }
        catch (FileNotFoundException exception)
        {
            //An error window pops up if the PrintWriter is unable to create a new file/read file
            makeErrorWindow ("Error - File could not be saved", 245, 90);
            return false;
        }

        //transfers text from main text area to saved file
        String contents = textArea.getText ();
        fileToSave.print (contents);
        fileToSave.close ();
        return true;
    }

    private void setNewSettings ()
    {
        //file settings are changes such that the status bar and title of text area reflect the new file and show the unmodified state
        currentFileName = chosenFileName;
        fileTitle.setTitle (currentFileName + ".config");
        repaint ();
        statusBar.setText ("Current Project: " + currentFileName);
        isItModified = false;
        isItNew = false;
        validate ();
    }

    private class ExitCheck extends WindowAdapter
    {
        @Override
        public void windowClosing (WindowEvent event)
        {
            //calls the quitting sequence via quitConfig
            quitConfig ();
        }
    }

    private boolean lexYaccCompileConfig ()
    {
        //the compilation process begins with saving the new or modified file first, returning false if the user chooses not to save
        if (isItModified == true || isItNew == true)
        {
            boolean checkSave = saveDialog ();
            if (checkSave == false)
            {
                makeErrorWindow ("Error - New or modified file not saved before compilation", 420, 90);
                return false;
            }
            else
            {
                isItNew = false;
                setNewSettings ();
            }
        }

        //the directory path given is used to determine if a creating a directory is necessary
        File directoryPath = new File (workingDirectory + "/" + currentFileName);
        if (directoryPath.exists () == false)
        {
            boolean mkdirSuccess = directoryPath.mkdirs ();
            if (mkdirSuccess == false)
            {
                makeErrorWindow ("Error - The working directory could not be made", 380, 90);
                return false;
            }
        }

        //the program will then attempt to compile the .config file using the external lex/yacc compiler (yadc)
        try
        {
            Runtime externalRuntime = Runtime.getRuntime ();
            Process compileProcess = externalRuntime.exec (dialogcLocation + "bin/yadc " + filePath + " " + workingDirectory + "/" + currentFileName);

            int errorCode = compileProcess.waitFor ();
            if (errorCode != 0)
            {
                makeErrorWindow ("Error - Lex/Yacc compiler cannot compile the current .config file", 510, 145, compileProcess);
                validate ();
                return false;
            }
        }
        catch (Exception thrown)
        {
            makeErrorWindow ("Error - Lex/Yacc compiler cannot compile the current .config file", 530, 90);
            validate ();
            return false;
        }

        return true;
    }

    private boolean compileConfig ()
    {
        //the compilation process begins with saving the new or modified file first, returning false if the user chooses not to save
        if (isItModified == true || isItNew == true)
        {
            boolean checkSave = saveDialog ();
            if (checkSave == false)
            {
                makeErrorWindow ("Error - New or modified file not saved before compilation", 420, 90);
                return false;
            }
            else
            {
                isItNew = false;
                setNewSettings ();
            }
        }

        //the flags for the reserved button names are reset before compilation begins with the JNI
        addRequested = false;
        deleteRequested = false;
        updateRequested = false;
        queryRequested = false;

        //the first parse through is checked for success
        int checkParseProgress;
        checkParseProgress = Dialogc.firstParse (filePath);
        if (checkParseProgress == 0)
        {
            makeErrorWindow ("Error - A title/fields/buttons parameter violates expected format", 465, 90);
            return false;
        }

        //the second parse through is checked for success
        checkParseProgress = Dialogc.secondParse (filePath);
        if (checkParseProgress == 0)
        {
            makeErrorWindow ("Error - A label or button parameter violates expected format", 450, 90);

            resetPM ();
            return false;
        }

        //enforces a minimum of 1 field by counting the list contents of the fields parameter
        ArrayList<String> fieldsList = new ArrayList<String> (5);
        String fieldName = Dialogc.retrieveFieldName ();
        while (fieldName != null)
        {
            fieldsList.add (fieldName);
            fieldName = Dialogc.retrieveFieldName ();
        }
        if (fieldsList.size () <= 0)
        {
            makeErrorWindow ("Error - Empty fields list detected - 1 field minimum required", 445, 90);

            resetPM ();
            return false;
        }

        //enforces a minimum of 1 button by counting the list contents of the buttons parameter
        ArrayList<String> buttonsList = new ArrayList<String> (5);
        String buttonName = Dialogc.retrieveButtonName ();
        while (buttonName != null)
        {
            if (buttonName.equals ("ADD"))
            {
                addRequested = true;
            }
            else if (buttonName.equals ("DELETE"))
            {
                deleteRequested = true;
            }
            else if (buttonName.equals ("UPDATE"))
            {
                updateRequested = true;
            }
            else if (buttonName.equals ("QUERY"))
            {
                queryRequested = true;
            }
            else
            {
                //it is any other button, where the user will be expected to supply their own ActionListener
            }

            buttonsList.add (buttonName);
            buttonName = Dialogc.retrieveButtonName ();
        }
        if (buttonsList.size () <= 0)
        {
            makeErrorWindow ("Error - Empty buttons list detected - 1 button minimum required", 465, 90);

            resetPM ();
            return false;
        }

        //each label value is investigated to see if it has one of "string", "integer" or "float"
        int fieldListLength = fieldsList.size ();
        for (int i = 0; i < fieldListLength; i ++)
        {
            String checkFieldType = Dialogc.getType (fieldsList.get (i));
            if (!(checkFieldType.equals ("string") || checkFieldType.equals ("integer") || checkFieldType.equals ("float")))
            {
                makeErrorWindow ("Error - A label is not either string, integer or float in \"\"", 420, 90);

                resetPM ();
                return false;
            }
        }

        //the directory path given is used to determine if a creating a directory is necessary
        File directoryPath = new File (workingDirectory + "/" + currentFileName);
        if (directoryPath.exists () == false)
        {
            boolean mkdirSuccess = directoryPath.mkdirs ();
            if (mkdirSuccess == false)
            {
                makeErrorWindow ("Error - The working directory could not be made", 380, 90);

                resetPM ();
                return false;
            }
        }

        //the program will attemp to create a stream that makes the main class file
        PrintWriter fileToGenerate;
        try
        {
            fileToGenerate = new PrintWriter (new FileOutputStream (directoryPath.getAbsolutePath () + "/" + currentFileName + ".java"));
        }
        catch (FileNotFoundException exception)
        {
            //An error window pops up if the PrintWriter is unable to create a new file/read file for the class
            makeErrorWindow ("Error - .java file (main class) could not be created", 395, 90);

            resetPM ();
            return false;
        }

        makeCompiledClass (fileToGenerate, fieldsList, buttonsList);
        fileToGenerate.close ();

        //the program will then attempt to create a stream that makes the interface file
        try
        {
            fileToGenerate = new PrintWriter (new FileOutputStream (directoryPath.getAbsolutePath () + "/" + currentFileName + "FieldEdit.java"));
        }
        catch (FileNotFoundException exception)
        {
            //An error window pops up if the PrintWriter is unable to create a new file/read file
            makeErrorWindow ("Error - .java file (interface) could not be created", 390, 90);

            resetPM ();
            return false;
        }

        makeInterface (fileToGenerate, fieldsList);
        fileToGenerate.close ();

        //the program will then attempt to create a stream that makes the exception file
        try
        {
            fileToGenerate = new PrintWriter (new FileOutputStream (directoryPath.getAbsolutePath () + "/IllegalFieldValueException.java"));
        }
        catch (FileNotFoundException exception)
        {
            //An error window pops up if the PrintWriter is unable to create a new file/read file
            makeErrorWindow ("Error - .java file (exception) could not be created", 390, 90);

            resetPM ();
            return false;
        }

        makeException (fileToGenerate);
        fileToGenerate.close ();

        //the program will then attempt to create a stream that makes the ActionListener file for the Add button if declares/requested
        if (addRequested == true)
        {
            try
            {
                fileToGenerate = new PrintWriter (new FileOutputStream (directoryPath.getAbsolutePath () + "/DialogcAdd.java"));
            }
            catch (FileNotFoundException exception)
            {
                //An error window pops up if the PrintWriter is unable to create a new file/read file
                makeErrorWindow ("Error - .java file (ActionListener) could not be created", 400, 90);

                resetPM ();
                return false;
            }

            makeAddListener (fileToGenerate, fieldsList);
            fileToGenerate.close ();
        }

        //the program will then attempt to create a stream that makes the ActionListener file for the Delete button if declares/requested
        if (deleteRequested == true)
        {
            try
            {
                fileToGenerate = new PrintWriter (new FileOutputStream (directoryPath.getAbsolutePath () + "/DialogcDelete.java"));
            }
            catch (FileNotFoundException exception)
            {
                //An error window pops up if the PrintWriter is unable to create a new file/read file
                makeErrorWindow ("Error - .java file (ActionListener) could not be created", 400, 90);

                resetPM ();
                return false;
            }

            makeDeleteListener (fileToGenerate, fieldsList);
            fileToGenerate.close ();
        }

        //the program will then attempt to create a stream that makes the ActionListener file for the Update button if declares/requested
        if (updateRequested == true)
        {
            try
            {
                fileToGenerate = new PrintWriter (new FileOutputStream (directoryPath.getAbsolutePath () + "/DialogcUpdate.java"));
            }
            catch (FileNotFoundException exception)
            {
                //An error window pops up if the PrintWriter is unable to create a new file/read file
                makeErrorWindow ("Error - .java file (ActionListener) could not be created", 400, 90);

                resetPM ();
                return false;
            }

            makeUpdateListener (fileToGenerate, fieldsList);
            fileToGenerate.close ();
        }

        //the program will then attempt to create a stream that makes the ActionListener file for the Query button if declares/requested
        if (queryRequested == true)
        {
            try
            {
                fileToGenerate = new PrintWriter (new FileOutputStream (directoryPath.getAbsolutePath () + "/DialogcQuery.java"));
            }
            catch (FileNotFoundException exception)
            {
                //An error window pops up if the PrintWriter is unable to create a new file/read file
                makeErrorWindow ("Error - .java file (ActionListener) could not be created", 400, 90);

                resetPM ();
                return false;
            }

            makeQueryListener (fileToGenerate, fieldsList);
            fileToGenerate.close ();
        }

        resetPM ();

        return true;
    }

    //resetPM resets the ParameterManagers to the form when the program first starts in that only one PM is allocated and manages only title, fields and buttons (ready for first parse again)
    private void resetPM ()
    {
        Dialogc.cleanUpPM ();
        int checkPMInit = Dialogc.initPManager ();
        if (checkPMInit == 0)
        {
            System.err.println ("Error - Could not initialize ParameterManager");
            System.exit (0);
        }
    }

    //makeCompiledClass generates the main class GUI that the .config configures for
    private void makeCompiledClass (PrintWriter printClass, ArrayList<String> listOfFields, ArrayList<String> listOfButtons)
    {
        printClass.println ("import java.awt.*;");
        printClass.println ("import javax.swing.*;");
        printClass.println ("import javax.swing.GroupLayout.Alignment;");
        printClass.println ("import java.awt.event.ActionEvent;");
        printClass.println ("import java.awt.event.ActionListener;");
        printClass.println ("import java.sql.*;");
        printClass.println ();

        printClass.println ("public class " + currentFileName + " extends JFrame implements " + currentFileName + "FieldEdit");
        printClass.println ("{");

        //attributes are stored, with each field name associating with a text field
        int listLength = listOfFields.size ();
        printClass.println ("    private JTextField fieldID;");
        for (int i = 0; i < listLength; i ++)
        {
            String fieldName = listOfFields.get (i);
            printClass.println ("    private JTextField field" + fieldName + ";");
        }
        printClass.println ("    private JTextArea statusArea;");
        printClass.println ("    private static Connection dbConnection;");
        printClass.println ();

        //produces the constructor of the main class GUI
        String configTitle = Dialogc.retrieveTitle ();
        printClass.println ("    public " + currentFileName + " ()");
        printClass.println ("    {");
        printClass.println ("        super ();");
        printClass.println ("        setSize (450, 500);");
        printClass.println ("        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);");
        printClass.println ("        setTitle (\"" + configTitle + "\");");
        printClass.println ();
        printClass.println ("        setLayout (new BorderLayout ());");
        printClass.println ("        add (setMainArea (), BorderLayout.NORTH);");
        printClass.println ("        add (setStatusArea (), BorderLayout.SOUTH);");
        printClass.println ("        pack ();");
        printClass.println ("    }");
        printClass.println ();

        //constructs the main panel of the GUI that lines up labels with text fields and adds the buttons requested
        printClass.println ("    private JPanel setMainArea ()");
        printClass.println ("    {");
        printClass.println ("        JPanel mainPanel = new JPanel ();");
        printClass.println ("        mainPanel.setLayout (new BorderLayout ());");
        printClass.println ();

        //initializes the inner components of the panel, including the ID field
        printClass.println ("        JLabel label0 = new JLabel (\"ID\");");
        printClass.println ("        label0.setHorizontalAlignment (SwingConstants.LEFT);");
        printClass.println ("        fieldID = new JTextField (25);");
        for (int i = 0; i < listLength; i ++)
        {
            String fieldName = listOfFields.get (i);
            printClass.println ("        JLabel label" + (i + 1) + " = new JLabel (\"" + fieldName + "\");");
            printClass.println ("        label" + (i + 1) + ".setHorizontalAlignment (SwingConstants.LEFT);");
            printClass.println ("        field" + fieldName + " = new JTextField (25);");
        }
        printClass.println ();

        printClass.println ("        JPanel fieldsPanel = new JPanel ();");
        printClass.println ("        GroupLayout fieldsLayout = new GroupLayout (fieldsPanel);");    //initializes the layout to group the labels and text fields
        printClass.println ("        fieldsPanel.setLayout (fieldsLayout);");
        printClass.println ("        fieldsLayout.setAutoCreateGaps (true);");
        printClass.println ("        fieldsLayout.setAutoCreateContainerGaps (true);");
        printClass.println ();
        printClass.println ("        GroupLayout.SequentialGroup fieldGroup = fieldsLayout.createSequentialGroup ();");
        printClass.print ("        fieldGroup.addGroup (fieldsLayout.createParallelGroup ().addComponent (label0)");
        for (int i = 0; i < listLength; i ++)    //groups together the labels as one column
        {
            printClass.print (".addComponent (label" + (i + 1) + ")");
        }
        printClass.println (");");
        printClass.print ("        fieldGroup.addGroup (fieldsLayout.createParallelGroup ().addComponent (fieldID)");
        for (int i = 0; i < listLength; i ++)    //groups together the text fields as one column
        {
            printClass.print (".addComponent (field" + listOfFields.get (i) + ")");
        }
        printClass.println (");");
        printClass.println ("        fieldsLayout.setHorizontalGroup(fieldGroup);");
        printClass.println ();
        printClass.println ("        GroupLayout.SequentialGroup labelGroup = fieldsLayout.createSequentialGroup();");
        printClass.println ("        labelGroup.addGroup (fieldsLayout.createParallelGroup (Alignment.BASELINE).addComponent (label0).addComponent (fieldID));");
        for (int i = 0; i < listLength; i ++)    //groups together corresponding labels with text fields
        {
            printClass.println ("        labelGroup.addGroup (fieldsLayout.createParallelGroup (Alignment.BASELINE).addComponent (label" + (i + 1) + ").addComponent (field" + listOfFields.get (i) + "));");
        }
        printClass.println ("        fieldsLayout.setVerticalGroup(labelGroup);");
        printClass.println ();
        printClass.println ("        mainPanel.add (fieldsPanel, BorderLayout.CENTER);");
        printClass.println ();

        printClass.println ("        JPanel buttonsPanel = new JPanel ();");
        printClass.println ("        buttonsPanel.setLayout (new FlowLayout (FlowLayout.CENTER));");
        listLength = listOfButtons.size ();
        for (int i = 0; i < listLength; i ++)    //implements the buttons, attaching their respective ActionListeners, including the reserved buttons if declared
        {
            String buttonName = listOfButtons.get (i);
            if (buttonName.equals ("ADD"))
            {
                printClass.println ("        JButton buttonAdd = new JButton (\"ADD\");");
                printClass.println ("        buttonAdd.addActionListener (new DialogcAdd (this));");
                printClass.println ("        buttonsPanel.add (buttonAdd);");
            }
            else if (buttonName.equals ("DELETE"))
            {
                printClass.println ("        JButton buttonDelete = new JButton (\"DELETE\");");
                printClass.println ("        buttonDelete.addActionListener (new DialogcDelete (this));");
                printClass.println ("        buttonsPanel.add (buttonDelete);");
            }
            else if (buttonName.equals ("UPDATE"))
            {
                printClass.println ("        JButton buttonUpdate = new JButton (\"UPDATE\");");
                printClass.println ("        buttonUpdate.addActionListener (new DialogcUpdate (this));");
                printClass.println ("        buttonsPanel.add (buttonUpdate);");
            }
            else if (buttonName.equals ("QUERY"))
            {
                printClass.println ("        JButton buttonQuery = new JButton (\"QUERY\");");
                printClass.println ("        buttonQuery.addActionListener (new DialogcQuery (this));");
                printClass.println ("        buttonsPanel.add (buttonQuery);");
            }
            else
            {
                printClass.println ("        JButton button" + (i + 1) + " = new JButton (\"" + buttonName + "\");");
                printClass.println ("        button" + (i + 1) + ".addActionListener (new " + Dialogc.getListener (buttonName) + " (this));");
                printClass.println ("        buttonsPanel.add (button" + (i + 1) + ");");
            }
        }
        printClass.println ("        mainPanel.add (buttonsPanel, BorderLayout.SOUTH);");
        printClass.println ();
        printClass.println ("        return (mainPanel);");
        printClass.println ("    }");
        printClass.println ();

        //the status area is created with the necessary properties, storing the text area as an attribute
        printClass.println ("    private JPanel setStatusArea ()");
        printClass.println ("    {");
        printClass.println ("        JPanel statusPanel = new JPanel ();");
        printClass.println ("        statusPanel.setLayout (new BorderLayout ());");
        printClass.println ();
        printClass.println ("        JTextField statusLabel = new JTextField (\"STATUS\");");
        printClass.println ("        statusLabel.setBorder (BorderFactory.createLoweredBevelBorder ());");
        printClass.println ("        statusLabel.setEditable (false);");
        printClass.println ("        statusLabel.setHorizontalAlignment (SwingConstants.CENTER);");
        printClass.println ("        statusPanel.add (statusLabel, BorderLayout.NORTH);");
        printClass.println ();
        printClass.println ("        statusArea = new JTextArea (15, 15);");
        printClass.println ("        statusArea.setBorder (BorderFactory.createLineBorder (Color.BLACK));");
        printClass.println ("        statusArea.setEditable (false);");
        printClass.println ("        JScrollPane textScroll = new JScrollPane (statusArea);");
        printClass.println ("        textScroll.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);");
        printClass.println ("        textScroll.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);");
        printClass.println ("        statusPanel.add (textScroll, BorderLayout.CENTER);");
        printClass.println ();
        printClass.println ("        return (statusPanel);");
        printClass.println ("    }");
        printClass.println ();

        //the ID get and set methods are implemented
        printClass.println ("    @Override");
        printClass.println ("    public String getDCID () throws IllegalFieldValueException");
        printClass.println ("    {");
        printClass.println ("        String fieldValue = fieldID.getText ();");
        printClass.println ("        if (fieldValue.matches (\"[-]?[0-9]+\"))");
        printClass.println ("        {");
        printClass.println ("            return (fieldValue);");
        printClass.println ("        }");
        printClass.println ("        else");
        printClass.println ("        {");
        printClass.println ("            throw new IllegalFieldValueException (fieldValue);");
        printClass.println ("        }");
        printClass.println ("    }");
        printClass.println ();

        printClass.println ("    @Override");
        printClass.println ("    public void setDCID (String stringToSet)");
        printClass.println ("    {");
        printClass.println ("        fieldID.setText (stringToSet);");
        printClass.println ("    }");
        printClass.println ();

        //for each field given (other than ID), a get and set method is implemented
        listLength = listOfFields.size ();
        for (int i = 0; i < listLength; i ++)
        {
            String fieldName = listOfFields.get (i);
            String fieldType = Dialogc.getType (fieldName);

            printClass.println ("    @Override");
            printClass.print ("    public String getDC" + fieldName + " ()");
            if (fieldType.equals ("integer") || fieldType.equals ("float"))    //the get method declares to throw the IllegalFieldValueException if it is declared to be either an integer or float field
            {
                printClass.println (" throws IllegalFieldValueException");
            }
            else
            {
                printClass.println ();
            }
            printClass.println ("    {");
            printClass.println ("        String fieldValue = field" + fieldName + ".getText ();");
            if (fieldType.equals ("integer") || fieldType.equals ("float"))
            {
                //the set method differs if there is a restriction of input (integer/float)
                //a regex check is implemented to check for proper input when using the get methods
                String regex;
                if (fieldType.equals ("integer"))
                {
                    regex = "[-]?[0-9]+";
                }
                else if (fieldType.equals ("float"))
                {
                    regex = "[-]?(([0-9]+)|([0-9]*[.]?[0-9]+))";
                }
                else
                {
                    regex = null;
                }
                printClass.println ("        if (fieldValue.matches (\"" + regex + "\"))");
                printClass.println ("        {");
                printClass.println ("            return (fieldValue);");
                printClass.println ("        }");
                printClass.println ("        else");
                printClass.println ("        {");
                printClass.println ("            throw new IllegalFieldValueException (fieldValue);");
                printClass.println ("        }");
            }
            else
            {
                printClass.println ("        return (fieldValue);");
            }
            printClass.println ("    }");
            printClass.println ();

            printClass.println ("    @Override");
            printClass.println ("    public void setDC" + fieldName + " (String stringToSet)");
            printClass.println ("    {");
            printClass.println ("        field" + fieldName + ".setText (stringToSet);");
            printClass.println ("    }");
            printClass.println ();
        }

        //the appendToStatusArea method is implemented as simple appending the given string to the text area
        printClass.println ("    @Override");
        printClass.println ("    public void appendToStatusArea (String message)");
        printClass.println ("    {");
        printClass.println ("        statusArea.append (message);");
        printClass.println ("    }");
        printClass.println ();

        //the getDBConnection method is implemented to let ActionListeners access the Connection
        printClass.println ("    @Override");
        printClass.println ("    public Connection getDBConnection ()");
        printClass.println ("    {");
        printClass.println ("        return (dbConnection);");
        printClass.println ("    }");
        printClass.println ();

        //a private subclass Authenticate is for mediating the authentication of database access before using the generated GUI for database access
        printClass.println ("    private static class Authenticate extends JFrame");
        printClass.println ("    {");
        printClass.println ("        private JTextField usernameField;");
        printClass.println ("        private JTextField passwordField;");
        printClass.println ();
        printClass.println ("        public Authenticate ()");
        printClass.println ("        {");
        printClass.println ("            super ();");
        printClass.println ("            setSize (450, 500);");
        printClass.println ("            setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);");
        printClass.println ("            setTitle (\"Postgres Authenticate\");");
        printClass.println ();
        printClass.println ("            setLayout (new BorderLayout ());");
        printClass.println ("            add (setAuthArea (), BorderLayout.NORTH);");
        printClass.println ("            add (setButtonArea (), BorderLayout.SOUTH);");
        printClass.println ("            pack ();");
        printClass.println ("        }");
        printClass.println ();

        //the main panel of the authentication window is made here, similar to how the fields in the generated GUI is made
        printClass.println ("        private JPanel setAuthArea ()");
        printClass.println ("        {");
        printClass.println ("            JPanel mainPanel = new JPanel ();");
        printClass.println ();
        printClass.println ("            JLabel label0 = new JLabel (\"Username:\");");
        printClass.println ("            label0.setHorizontalAlignment (SwingConstants.LEFT);");
        printClass.println ("            usernameField = new JTextField (25);");
        printClass.println ("            JLabel label1 = new JLabel (\"Password:\");");
        printClass.println ("            label1.setHorizontalAlignment (SwingConstants.LEFT);");
        printClass.println ("            passwordField = new JTextField (25);");
        printClass.println ();
        printClass.println ("            JPanel fieldsPanel = new JPanel ();");
        printClass.println ("            GroupLayout fieldsLayout = new GroupLayout (fieldsPanel);");
        printClass.println ("            fieldsPanel.setLayout (fieldsLayout);");
        printClass.println ("            fieldsLayout.setAutoCreateGaps (true);");
        printClass.println ("            fieldsLayout.setAutoCreateContainerGaps (true);");
        printClass.println ();
        printClass.println ("            GroupLayout.SequentialGroup fieldGroup = fieldsLayout.createSequentialGroup ();");
        printClass.println ("            fieldGroup.addGroup (fieldsLayout.createParallelGroup ().addComponent (label0).addComponent (label1));");
        printClass.println ("            fieldGroup.addGroup (fieldsLayout.createParallelGroup ().addComponent (usernameField).addComponent (passwordField));");
        printClass.println ("            fieldsLayout.setHorizontalGroup(fieldGroup);");
        printClass.println ();
        printClass.println ("            GroupLayout.SequentialGroup labelGroup = fieldsLayout.createSequentialGroup();");
        printClass.println ("            labelGroup.addGroup (fieldsLayout.createParallelGroup (Alignment.BASELINE).addComponent (label0).addComponent (usernameField));");
        printClass.println ("            labelGroup.addGroup (fieldsLayout.createParallelGroup (Alignment.BASELINE).addComponent (label1).addComponent (passwordField));");
        printClass.println ("            fieldsLayout.setVerticalGroup(labelGroup);");
        printClass.println ();
        printClass.println ("            mainPanel.add (fieldsPanel);");
        printClass.println ("            return (mainPanel);");
        printClass.println ("        }");
        printClass.println ();

        //the buttons 'Sign In' and 'Cancel' are set up here, where signing in attempts for a connection while cancel exits the program
        printClass.println ("        private JPanel setButtonArea ()");
        printClass.println ("        {");
        printClass.println ("            JPanel buttonsPanel = new JPanel ();");
        printClass.println ("            buttonsPanel.setLayout (new FlowLayout (FlowLayout.CENTER));");
        printClass.println ("            JButton buttonSignIn = new JButton (\"Sign In\");");
        printClass.println ("            buttonSignIn.addActionListener (new SignInListener ());");
        printClass.println ("            buttonsPanel.add (buttonSignIn);");
        printClass.println ("            JButton buttonCancel = new JButton (\"Cancel\");");
        printClass.println ("            buttonCancel.addActionListener (new SignInListener ());");
        printClass.println ("            buttonsPanel.add (buttonCancel);");
        printClass.println ();
        printClass.println ("            return (buttonsPanel);");
        printClass.println ("        }");
        printClass.println ();

        /*the listener that interprets 'Sign In' and 'Cancel' is made here*/
        printClass.println ("        private class SignInListener implements ActionListener");
        printClass.println ("        {");
        printClass.println ("            @Override");
        printClass.println ("            public void actionPerformed (ActionEvent event)");
        printClass.println ("            {");
        printClass.println ("                String authCommand = event.getActionCommand ();");
        printClass.println ();
        printClass.println ("                if (authCommand.equals (\"Sign In\"))");
        printClass.println ("                {");
        printClass.println ("                    String username = usernameField.getText ();");
        printClass.println ("                    String password = passwordField.getText ();");
        printClass.println ("                    try");
        printClass.println ("                    {");
        printClass.println ("                        dbConnection = PostgresLogin.getConnection (username, password);");
        printClass.println ("                        dispose ();");
        printClass.println ();
        printClass.println ("                        " + currentFileName + " newWindow = new " + currentFileName + " ();");
        printClass.println ("                        newWindow.setVisible (true);");
        printClass.println ("                    }");
        printClass.println ("                    catch (SQLException exception)");
        printClass.println ("                    {");
        printClass.println ("                        DismissableWindow errorAuth = new DismissableWindow (\"Authentication Failed\", 320, 100, \"Authentication failed. Please try again.\");");
        printClass.println ("                        errorAuth.setVisible (true);");
        printClass.println ("                    }");
        printClass.println ("                }");
        printClass.println ("                else if (authCommand.equals (\"Cancel\"))");
        printClass.println ("                {");
        printClass.println ("                    System.exit (0);");
        printClass.println ("                }");
        printClass.println ("                else");
        printClass.println ("                {");
        printClass.println ("                    System.err.println (\"Unexpected Authentication Logic Error\");");
        printClass.println ("                    return;");
        printClass.println ("                }");
        printClass.println ();
        printClass.println ("                validate ();");
        printClass.println ("            }");
        printClass.println ("        }");
        printClass.println ();

        //a sub-class of a sub-class defines the authentication error popup when the user inputs the incorrect credentials
        printClass.println ("        private class DismissableWindow extends JFrame implements ActionListener");
        printClass.println ("        {");
        printClass.println ("            public DismissableWindow (String windowTitle, int width, int height, String message)");
        printClass.println ("            {");
        printClass.println ("                super ();");
        printClass.println ("                setSize (width, height);");
        printClass.println ();
        printClass.println ("                setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);");
        printClass.println ();
        printClass.println ("                setTitle (windowTitle);");
        printClass.println ("                setLayout (new BorderLayout ());");
        printClass.println ();
        printClass.println ("                JLabel errorMessage = new JLabel (message);");
        printClass.println ("                errorMessage.setHorizontalAlignment (SwingConstants.CENTER);");
        printClass.println ();
        printClass.println ("                add (errorMessage, BorderLayout.CENTER);");
        printClass.println ();
        printClass.println ("                JPanel dismissPanel = new JPanel ();");
        printClass.println ("                dismissPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));");
        printClass.println ("                JButton dismissButton = new JButton (\"Dismiss\");");
        printClass.println ("                dismissButton.addActionListener (this);");
        printClass.println ("                dismissPanel.add (dismissButton);");
        printClass.println ();
        printClass.println ("                add (dismissPanel, BorderLayout.SOUTH);");
        printClass.println ("            }");
        printClass.println ();
        printClass.println ("            @Override");
        printClass.println ("            public void actionPerformed (ActionEvent event)");
        printClass.println ("            {");
        printClass.println ("                String dismissCommand = event.getActionCommand ();");
        printClass.println ();
        printClass.println ("                if (dismissCommand.equals (\"Dismiss\"))");
        printClass.println ("                {");
        printClass.println ("                    dispose ();");
        printClass.println ("                }");
        printClass.println ("                else");
        printClass.println ("                {");
        printClass.println ("                    System.err.println (\"Error - Unexpected Dismissable Window Error\");");
        printClass.println ("                }");
        printClass.println ();
        printClass.println ("                validate ();");
        printClass.println ("            }");
        printClass.println ("        }");
        printClass.println ("    }");

        //a generated GUi program starts with the authentication process before displaying the main GUI upon successful authentication
        printClass.println ("    public static void main (String[] args)");
        printClass.println ("    {");
        printClass.println ("        Authenticate newWindow = new Authenticate ();");
        printClass.println ("        newWindow.setVisible (true);");
        printClass.println ("    }");
        printClass.println ("}");
    }

    //makeInterface generates the required interface for a given compilation, given the stream to write to and the necessary field labels
    private void makeInterface (PrintWriter printInterface, ArrayList<String> listOfFields)
    {
        printInterface.println ("import java.sql.*;");
        printInterface.println ();

        printInterface.println ("public interface " + currentFileName + "FieldEdit");
        printInterface.println ("{");

        printInterface.println ("    public abstract String getDCID () throws IllegalFieldValueException;");
        printInterface.println ("    public abstract void setDCID (String stringToSet);");
        printInterface.println ();

        //for each fields label (other than ID) a get and set abstract method signature is made
        int listLength = listOfFields.size ();
        for (int i = 0; i < listLength; i ++)
        {
            String fieldName = listOfFields.get (i);

            printInterface.println ("    public abstract String getDC" + fieldName + " () throws IllegalFieldValueException;");
            printInterface.println ("    public abstract void setDC" + fieldName + " (String stringToSet);");
            printInterface.println ();
        }

        printInterface.println ("    public abstract void appendToStatusArea (String message);");
        printInterface.println ();
        printInterface.println ("    public abstract Connection getDBConnection ();");
        printInterface.println ("}");
    }

    //makeException generates the IllegalFieldValueException exception class for a given compilation
    private void makeException (PrintWriter printException)
    {
        printException.println ("public class IllegalFieldValueException extends Exception");
        printException.println ("{");

        printException.println ("    public IllegalFieldValueException ()");
        printException.println ("    {");
        printException.println ("        super (\"Field has illegal value\");");
        printException.println ("    }");
        printException.println ();

        printException.println ("    public IllegalFieldValueException (String message)");
        printException.println ("    {");
        printException.println ("        super (message);");
        printException.println ("    }");
        printException.println ("}");
    }

    //makeUpdateListener creates the ActionListener for the Add button if requested in the buttons list (reserved)
    private void makeAddListener (PrintWriter printAdd, ArrayList<String> listOfFields)
    {
        String fieldName = null;
        String fieldType = null;

        printAdd.println ("import java.awt.event.ActionEvent;");
        printAdd.println ("import java.awt.event.ActionListener;");
        printAdd.println ("import java.sql.*;");
        printAdd.println ("import java.util.ArrayList;");
        printAdd.println ();

        printAdd.println ("public class DialogcAdd implements ActionListener");
        printAdd.println ("{");
        printAdd.println ("    private " + currentFileName + "FieldEdit dialogInterface;");
        printAdd.println ();
        printAdd.println ("    public DialogcAdd (" + currentFileName + "FieldEdit theInterface)");
        printAdd.println ("    {");
        printAdd.println ("        dialogInterface = theInterface;");
        printAdd.println ("    }");
        printAdd.println ();

        //the database addition takes place here
        printAdd.println ("    @Override");
        printAdd.println ("    public void actionPerformed (ActionEvent event)");
        printAdd.println ("    {");
        printAdd.println ("        dialogInterface.appendToStatusArea (\"ADD\");");
        printAdd.println ("        dialogInterface.appendToStatusArea (\"\\n\");");
        printAdd.println ();
        printAdd.println ("        Connection addConnection = dialogInterface.getDBConnection ();");
        printAdd.println ("        Statement psqlStatement = null;");
        printAdd.println ();
        printAdd.println ("        try");
        printAdd.println ("        {");
        printAdd.println ("            DatabaseMetaData dbMetaData = addConnection.getMetaData ();");
        printAdd.println ("            ResultSet tableCheck = dbMetaData.getTables (null, null, \"" + currentFileName.toLowerCase () +  "\", null);");    //the database is checked for any table of the same name
        printAdd.println ("            if (tableCheck.next () == false)");    //if there is no table by that name, it is made
        printAdd.println ("            {");
        printAdd.println ("                dialogInterface.appendToStatusArea (\"Database table not found. Creating table...\\n\");");
        printAdd.println ();
        printAdd.print ("                String createString = \"CREATE TABLE " + currentFileName.toLowerCase () + " ( ID SERIAL");
        int listLength = listOfFields.size ();
        for (int i = 0; i < listLength; i ++)    //each field is represented in the SQL command CREATE TABLE, where integers are INTEGERS, floats are DECIMAL(20,3) and strings are VARCHAR(30)
        {
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            String tableType = null;
            if (fieldType.equals ("integer"))
            {
                tableType = "INTEGER";
            }
            else if (fieldType.equals ("float"))
            {
                tableType = "DECIMAL(20,3)";
            }
            else
            {
                tableType = "VARCHAR(30)";
            }

            printAdd.print (" , " + fieldName + " " + tableType);
        }
        printAdd.println (" , PRIMARY KEY (ID))\";");

        printAdd.println ("                try");
        printAdd.println ("                {");
        printAdd.println ("                    psqlStatement = addConnection.createStatement ();");
        printAdd.println ("                    psqlStatement.executeUpdate (createString);");
        printAdd.println ("                    psqlStatement.close ();");
        printAdd.println ("                }");
        printAdd.println ("                catch (SQLException exception)");
        printAdd.println ("                {");
        printAdd.println ("                    dialogInterface.appendToStatusArea (\"Table unable to be created. Please try again later\\n\\n\");");
        printAdd.println ("                    return;");
        printAdd.println ("                }");
        printAdd.println ();
        printAdd.println ("                dialogInterface.appendToStatusArea (\"Table created successfully\\n\\n\");");
        printAdd.println ("            }");
        printAdd.println ("            else");    //the existing table is checked to see if it matches the same details as the generated GUI
        printAdd.println ("            {");
        printAdd.println ("                boolean IDCheck = false;");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printAdd.println ("                boolean " + fieldName + "Check = false;");
        }
        printAdd.println ();
        printAdd.println ("                ResultSet columnCheck = dbMetaData.getColumns (null, null, \"" + currentFileName.toLowerCase () + "\", \"%\");");
        printAdd.println ("                while (columnCheck.next () != false)");
        printAdd.println ("                {");
        printAdd.println ("                    String columnName = columnCheck.getString (4);");
        printAdd.println ("                    int columnType = columnCheck.getInt (5);");
        printAdd.println ();
        printAdd.println ("                    if (columnName.equalsIgnoreCase (\"ID\") && columnType == java.sql.Types.INTEGER)");    //comparisons are made here, matching GUI details to table details if a match
        printAdd.println ("                    {");
        printAdd.println ("                        IDCheck = true;");
        printAdd.println ("                    }");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            String sqlType = null;
            if (fieldType.equals ("integer"))
            {
                sqlType = "INTEGER";
            }
            else if (fieldType.equals ("float"))
            {
                sqlType = "NUMERIC";
            }
            else
            {
                sqlType = "VARCHAR";
            }

            printAdd.println ("                    else if (columnName.equalsIgnoreCase (\"" + fieldName + "\") && columnType == java.sql.Types." + sqlType + ")");
            printAdd.println ("                    {");
            printAdd.println ("                        " + fieldName + "Check = true;");
            printAdd.println ("                    }");
        }
        printAdd.println ("                    else");
        printAdd.println ("                    {");
        printAdd.println ("                        dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");");
        printAdd.println ("                        return;");
        printAdd.println ("                    }");
        printAdd.println ("                }");
        printAdd.println ();
        printAdd.print ("                if (IDCheck == false");
        for (int i = 0; i < listLength - 1; i ++)
        {
            fieldName = listOfFields.get (i);
            printAdd.print (" || " + fieldName + "Check == false");
        }
        fieldName = listOfFields.get (listLength - 1);
        printAdd.println (" || " + fieldName + "Check == false)");    //if all details match, it is assumed that this table belongs to this GUI
        printAdd.println ("                {");
        printAdd.println ("                    dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");");
        printAdd.println ("                    return;");
        printAdd.println ("                }");
        printAdd.println ("            }");
        printAdd.println ("        }");
        printAdd.println ("        catch (SQLException exception)");
        printAdd.println ("        {");
        printAdd.println ("            dialogInterface.appendToStatusArea (\"Unable to find table. Please try again later\\n\\n\");");
        printAdd.println ("            return;");
        printAdd.println ("        }");
        printAdd.println ();

        //each field is read and checked if it fits the add functionality
        printAdd.println ("        boolean correctInput = true;");
        printAdd.println ("        ArrayList<String> fieldNames = new ArrayList<String> (10);");
        printAdd.println ("        ArrayList<String> fieldValues = new ArrayList<String> (10);");
        printAdd.println ("        String fieldInput = null;");
        printAdd.println ();
        printAdd.println ("        try");
        printAdd.println ("        {");
        printAdd.println ("            fieldInput = dialogInterface.getDCID ();");
        printAdd.println ("            dialogInterface.appendToStatusArea (\"Error - ID field not blank\\n\");");
        printAdd.println ("            correctInput = false;");
        printAdd.println ("        }");
        printAdd.println ("        catch (IllegalFieldValueException exception)");
        printAdd.println ("        {");
        printAdd.println ("            if (exception.getMessage ().trim ().equals (\"\"))");
        printAdd.println ("            {");
        printAdd.println ("                correctInput = true;");
        printAdd.println ("            }");
        printAdd.println ("            else");
        printAdd.println ("            {");
        printAdd.println ("                dialogInterface.appendToStatusArea (\"Error - ID field not blank\\n\");");
        printAdd.println ("                correctInput = false;");
        printAdd.println ("            }");
        printAdd.println ("        }");
        for (int i = 0; i < listLength; i ++)
        {
            printAdd.println ();
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            if (fieldType.equals ("integer") || fieldType.equals ("float"))
            {
                String endLabel = null;
                if (fieldType.equals ("integer"))
                {
                    endLabel = "an integer";
                }
                else
                {
                    endLabel = "a float";
                }

                printAdd.println ("        try");
                printAdd.println ("        {");
                printAdd.println ("            fieldInput = dialogInterface.getDC" + fieldName + " ();");
                printAdd.println ("            fieldNames.add (\"" + fieldName + "\");");
                printAdd.println ("            fieldValues.add (fieldInput);");
                printAdd.println ("        }");
                printAdd.println ("        catch (IllegalFieldValueException exception)");
                printAdd.println ("        {");
                printAdd.println ("            dialogInterface.appendToStatusArea (\"Error - " + fieldName + " field not " + endLabel + "\\n\");");
                printAdd.println ("            correctInput = false;");
                printAdd.println ("        }");
            }
            else
            {
                printAdd.println ("        try");
                printAdd.println ("        {");
                printAdd.println ("            fieldInput = dialogInterface.getDC" + fieldName + " ();");
                printAdd.println ("            if (fieldInput.trim ().equals (\"\"))");
                printAdd.println ("            {");
                printAdd.println ("                dialogInterface.appendToStatusArea (\"Error - " + fieldName + " field is blank\\n\");");
                printAdd.println ("                correctInput = false;");
                printAdd.println ("            }");
                printAdd.println ("            else");
                printAdd.println ("            {");
                printAdd.println ("                fieldNames.add (\"" + fieldName + "\");");
                printAdd.println ("                fieldValues.add (fieldInput);");
                printAdd.println ("            }");
                printAdd.println ("        }");
                printAdd.println ("        catch (IllegalFieldValueException exception)");
                printAdd.println ("        {");
                printAdd.println ("            dialogInterface.appendToStatusArea (\"Error - " + fieldName + " field not a string\\n\");");
                printAdd.println ("            correctInput = false;");
                printAdd.println ("        }");
            }
        }
        printAdd.println ();
        printAdd.println ("        if (correctInput == false)");    //if any field does not match the prerequisites, the listener returns without further action
        printAdd.println ("        {");
        printAdd.println ("            dialogInterface.appendToStatusArea (\"\\n\");");
        printAdd.println ("            return;");
        printAdd.println ("        }");
        printAdd.println ();

        //an insert is attempted with the given info
        printAdd.println ("        int listLength = fieldNames.size ();");
        printAdd.println ("        String insertString = \"INSERT INTO " + currentFileName.toLowerCase () + " (\";");
        printAdd.println ("        for (int i = 0; i < listLength - 1; i ++)");
        printAdd.println ("        {");
        printAdd.println ("            insertString = insertString + fieldNames.get (i) + \", \";");
        printAdd.println ("        }");
        printAdd.println ("        insertString = insertString + fieldNames.get (listLength - 1) + \") VALUES (\";");
        printAdd.println ("        for (int i = 0; i < listLength - 1; i ++)");
        printAdd.println ("        {");
        printAdd.println ("            insertString = insertString + \"'\" + fieldValues.get (i) + \"', \";");
        printAdd.println ("        }");
        printAdd.println ("        insertString = insertString + fieldValues.get (listLength - 1) + \")\";");
        printAdd.println ();
        printAdd.println ("        try");
        printAdd.println ("        {");
        printAdd.println ("            psqlStatement = addConnection.createStatement ();");
        printAdd.println ("            psqlStatement.executeUpdate (insertString);");
        printAdd.println ("            psqlStatement.close ();");
        printAdd.println ("        }");
        printAdd.println ("        catch (SQLException exception)");
        printAdd.println ("        {");
        printAdd.println ("            dialogInterface.appendToStatusArea (\"Insert failed\\n\\n\");");
        printAdd.println ("            return;");
        printAdd.println ("        }");
        printAdd.println ("        dialogInterface.appendToStatusArea (\"Insert successful\\n\\n\");");
        printAdd.println ();

        //if the insert is successful, the query will occur to obtain the assigned id
        printAdd.println ("        String checkInsert = \"SELECT ID FROM " + currentFileName.toLowerCase () + " WHERE \";");
        printAdd.println ("        for (int i = 0; i < listLength - 1; i ++)");
        printAdd.println ("        {");
        printAdd.println ("            checkInsert = checkInsert + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"' AND \";");
        printAdd.println ("        }");
        printAdd.println ("        checkInsert = checkInsert + fieldNames.get (listLength - 1) + \" = '\" + fieldValues.get (listLength - 1) + \"'\";");
        printAdd.println ("        try");
        printAdd.println ("        {");
        printAdd.println ("            psqlStatement = addConnection.createStatement ();");
        printAdd.println ("            ResultSet newResult = psqlStatement.executeQuery (checkInsert);");
        printAdd.println ("            while (newResult.next () != false)");
        printAdd.println ("            {");
        printAdd.println ("                dialogInterface.setDCID (newResult.getString (1));");
        printAdd.println ("            }");
        printAdd.println ("            psqlStatement.close ();");
        printAdd.println ("            newResult.close ();");
        printAdd.println ("        }");
        printAdd.println ("        catch (SQLException exception)");
        printAdd.println ("        {");
        printAdd.println ("            dialogInterface.appendToStatusArea (\"Result query failed\\n\\n\");");
        printAdd.println ("        }");
        printAdd.println ("    }");
        printAdd.println ("}");
    }

    //makeUpdateListener creates the ActionListener for the Delete button if requested in the buttons list (reserved)
    private void makeDeleteListener (PrintWriter printDelete, ArrayList<String> listOfFields)
    {
        String fieldName = null;
        String fieldType = null;

        printDelete.println ("import java.awt.*;");
        printDelete.println ("import javax.swing.*;");
        printDelete.println ("import java.awt.event.ActionEvent;");
        printDelete.println ("import java.awt.event.ActionListener;");
        printDelete.println ("import java.sql.*;");
        printDelete.println ("import java.util.ArrayList;");
        printDelete.println ();

        printDelete.println ("public class DialogcDelete implements ActionListener");
        printDelete.println ("{");
        printDelete.println ("    private " + currentFileName + "FieldEdit dialogInterface;");
        printDelete.println ();
        printDelete.println ("    public DialogcDelete (" + currentFileName + "FieldEdit theInterface)");
        printDelete.println ("    {");
        printDelete.println ("        dialogInterface = theInterface;");
        printDelete.println ("    }");
        printDelete.println ();

        //the database deletion takes place here
        printDelete.println ("    @Override");
        printDelete.println ("    public void actionPerformed (ActionEvent event)");
        printDelete.println ("    {");
        printDelete.println ("        dialogInterface.appendToStatusArea (\"DELETE\");");
        printDelete.println ("        dialogInterface.appendToStatusArea (\"\\n\");");
        printDelete.println ();
        printDelete.println ("        Connection deleteConnection = dialogInterface.getDBConnection ();");
        printDelete.println ("        Statement psqlStatement = null;");
        printDelete.println ();
        printDelete.println ("        try");
        printDelete.println ("        {");
        printDelete.println ("            DatabaseMetaData dbMetaData = deleteConnection.getMetaData ();");
        printDelete.println ("            ResultSet tableCheck = dbMetaData.getTables (null, null, \"" + currentFileName.toLowerCase () +  "\", null);");    //the database is checked for any table of the same name
        printDelete.println ("            if (tableCheck.next () == false)");    //if there is no table by that name, it is made
        printDelete.println ("            {");
        printDelete.println ("                dialogInterface.appendToStatusArea (\"Database table not found. Creating table...\\n\");");
        printDelete.println ();
        printDelete.print ("                String createString = \"CREATE TABLE " + currentFileName.toLowerCase () + " ( ID SERIAL");
        int listLength = listOfFields.size ();
        for (int i = 0; i < listLength; i ++)    //each field is represented in the SQL command CREATE TABLE, where integers are INTEGERS, floats are DECIMAL(20,3) and strings are VARCHAR(30)
        {
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            String tableType = null;
            if (fieldType.equals ("integer"))
            {
                tableType = "INTEGER";
            }
            else if (fieldType.equals ("float"))
            {
                tableType = "DECIMAL(20,3)";
            }
            else
            {
                tableType = "VARCHAR(30)";
            }

            printDelete.print (" , " + fieldName + " " + tableType);
        }
        printDelete.println (" , PRIMARY KEY (ID))\";");

        printDelete.println ("                try");
        printDelete.println ("                {");
        printDelete.println ("                    psqlStatement = deleteConnection.createStatement ();");
        printDelete.println ("                    psqlStatement.executeUpdate (createString);");
        printDelete.println ("                    psqlStatement.close ();");
        printDelete.println ("                }");
        printDelete.println ("                catch (SQLException exception)");
        printDelete.println ("                {");
        printDelete.println ("                    dialogInterface.appendToStatusArea (\"Table unable to be created. Please try again later\\n\\n\");");
        printDelete.println ("                    return;");
        printDelete.println ("                }");
        printDelete.println ();
        printDelete.println ("                dialogInterface.appendToStatusArea (\"Table created successfully\\n\\n\");");
        printDelete.println ("            }");
        printDelete.println ("            else");    //the existing table is checked to see if it matches the same details as the generated GUI
        printDelete.println ("            {");
        printDelete.println ("                boolean IDCheck = false;");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printDelete.println ("                boolean " + fieldName + "Check = false;");
        }
        printDelete.println ();
        printDelete.println ("                ResultSet columnCheck = dbMetaData.getColumns (null, null, \"" + currentFileName.toLowerCase () + "\", \"%\");");
        printDelete.println ("                while (columnCheck.next () != false)");
        printDelete.println ("                {");
        printDelete.println ("                    String columnName = columnCheck.getString (4);");
        printDelete.println ("                    int columnType = columnCheck.getInt (5);");
        printDelete.println ();
        printDelete.println ("                    if (columnName.equalsIgnoreCase (\"ID\") && columnType == java.sql.Types.INTEGER)");    //comparisons are made here, matching GUI details to table details if a match
        printDelete.println ("                    {");
        printDelete.println ("                        IDCheck = true;");
        printDelete.println ("                    }");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            String sqlType = null;
            if (fieldType.equals ("integer"))
            {
                sqlType = "INTEGER";
            }
            else if (fieldType.equals ("float"))
            {
                sqlType = "NUMERIC";
            }
            else
            {
                sqlType = "VARCHAR";
            }

            printDelete.println ("                    else if (columnName.equalsIgnoreCase (\"" + fieldName + "\") && columnType == java.sql.Types." + sqlType + ")");
            printDelete.println ("                    {");
            printDelete.println ("                        " + fieldName + "Check = true;");
            printDelete.println ("                    }");
        }
        printDelete.println ("                    else");
        printDelete.println ("                    {");
        printDelete.println ("                        dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");");
        printDelete.println ("                        return;");
        printDelete.println ("                    }");
        printDelete.println ("                }");
        printDelete.println ();
        printDelete.print ("                if (IDCheck == false");
        for (int i = 0; i < listLength - 1; i ++)
        {
            fieldName = listOfFields.get (i);
            printDelete.print (" || " + fieldName + "Check == false");
        }
        fieldName = listOfFields.get (listLength - 1);
        printDelete.println (" || " + fieldName + "Check == false)");    //if all details match, it is assumed that this table belongs to this GUI
        printDelete.println ("                {");
        printDelete.println ("                    dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");");
        printDelete.println ("                    return;");
        printDelete.println ("                }");
        printDelete.println ("            }");
        printDelete.println ("        }");
        printDelete.println ("        catch (SQLException exception)");
        printDelete.println ("        {");
        printDelete.println ("            dialogInterface.appendToStatusArea (\"Unable to find table. Please try again later\\n\\n\");");
        printDelete.println ("            return;");
        printDelete.println ("        }");
        printDelete.println ();

        //each field is read and checked to see it it fits the delete functionality
        printDelete.println ("        boolean correctInput = true;");
        printDelete.println ("        ArrayList<String> fieldNames = new ArrayList<String> (10);");
        printDelete.println ("        ArrayList<String> fieldValues = new ArrayList<String> (10);");
        printDelete.println ("        String fieldInput = null;");
        printDelete.println ();
        printDelete.println ("        try");
        printDelete.println ("        {");
        printDelete.println ("            fieldInput = dialogInterface.getDCID ();");
        printDelete.println ("            fieldNames.add (\"ID\");");
        printDelete.println ("            fieldValues.add (fieldInput);");
        printDelete.println ("        }");
        printDelete.println ("        catch (IllegalFieldValueException exception)");
        printDelete.println ("        {");
        printDelete.println ("            if (!exception.getMessage ().trim ().equals (\"\"))");
        printDelete.println ("            {");
        printDelete.println ("                dialogInterface.appendToStatusArea (\"Error - ID field not an integer\\n\");");
        printDelete.println ("                correctInput = false;");
        printDelete.println ("            }");
        printDelete.println ("        }");
        for (int i = 0; i < listLength; i ++)
        {
            printDelete.println ();
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            if (fieldType.equals ("integer") || fieldType.equals ("float"))
            {
                String endLabel = null;
                if (fieldType.equals ("integer"))
                {
                    endLabel = "an integer";
                }
                else
                {
                    endLabel = "a float";
                }

                printDelete.println ("        try");
                printDelete.println ("        {");
                printDelete.println ("            fieldInput = dialogInterface.getDC" + fieldName + " ();");
                printDelete.println ("            fieldNames.add (\"" + fieldName + "\");");
                printDelete.println ("            fieldValues.add (fieldInput);");
                printDelete.println ("        }");
                printDelete.println ("        catch (IllegalFieldValueException exception)");
                printDelete.println ("        {");
                printDelete.println ("            if (!exception.getMessage ().trim ().equals (\"\"))");
                printDelete.println ("            {");
                printDelete.println ("                dialogInterface.appendToStatusArea (\"Error - " + fieldName + " field not " + endLabel + "\\n\");");
                printDelete.println ("                correctInput = false;");
                printDelete.println ("            }");
                printDelete.println ("        }");
            }
            else
            {
                printDelete.println ("        try");
                printDelete.println ("        {");
                printDelete.println ("            fieldInput = dialogInterface.getDC" + fieldName + " ();");
                printDelete.println ("            if (!fieldInput.trim ().equals (\"\"))");
                printDelete.println ("            {");
                printDelete.println ("                fieldNames.add (\"" + fieldName + "\");");
                printDelete.println ("                fieldValues.add (fieldInput);");
                printDelete.println ("            }");
                printDelete.println ("        }");
                printDelete.println ("        catch (IllegalFieldValueException exception)");
                printDelete.println ("        {");
                printDelete.println ("            dialogInterface.appendToStatusArea (\"Error - " + fieldName + " field not a string\\n\");");
                printDelete.println ("            correctInput = false;");
                printDelete.println ("        }");
            }
        }
        printDelete.println ();
        printDelete.println ("        if (correctInput == false)");
        printDelete.println ("        {");
        printDelete.println ("            dialogInterface.appendToStatusArea (\"\\n\");");
        printDelete.println ("            return;");
        printDelete.println ("        }");
        printDelete.println ();

        //the database is queried to see it there are any matching records that match the user's input
        printDelete.println ("        String queryString = \"SELECT * FROM " + currentFileName.toLowerCase () + "\";");
        printDelete.println ("        int listLength = fieldNames.size ();");
        printDelete.println ("        if (listLength > 0)");
        printDelete.println ("        {");
        printDelete.println ("            queryString = queryString + \" WHERE \";");
        printDelete.println ("            for (int i = 0; i < listLength - 1; i ++)");
        printDelete.println ("            {");
        printDelete.println ("                queryString = queryString + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"' AND \";");
        printDelete.println ("            }");
        printDelete.println ("            queryString = queryString + fieldNames.get (listLength - 1) + \" = '\" + fieldValues.get (listLength - 1) + \"'\";");
        printDelete.println ("        }");
        printDelete.println ("        try");
        printDelete.println ("        {");
        printDelete.println ("            psqlStatement = deleteConnection.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);");
        printDelete.println ("            ResultSet results = psqlStatement.executeQuery (queryString);");
        printDelete.println ();
        printDelete.println ("            if (results.next () == true)");
        printDelete.println ("            {");
        printDelete.println ("                DeleteConfirm newDelete = new DeleteConfirm (deleteConnection, results, fieldNames, fieldValues);");
        printDelete.println ("                newDelete.setVisible (true);");
        printDelete.println ("            }");
        printDelete.println ("            else");
        printDelete.println ("            {");
        printDelete.println ("                dialogInterface.appendToStatusArea (\"0 results\\n\\n\");");
        printDelete.println ("            }");
        printDelete.println ();
        printDelete.println ("            psqlStatement.close ();");
        printDelete.println ("        }");
        printDelete.println ("        catch (SQLException exception)");
        printDelete.println ("        {");
        printDelete.println ("            dialogInterface.appendToStatusArea (\"Query to delete failed\\n\\n\");");
        printDelete.println ("        }");
        printDelete.println ("    }");
        printDelete.println ();

        //a sub-class is printed to handle the confirming of deleting records through a separate frame
        printDelete.println ("    private class DeleteConfirm extends JFrame");
        printDelete.println ("    {");
        printDelete.println ("        private Connection deleteConnection;");
        printDelete.println ("        private ArrayList<String> fieldNames;");
        printDelete.println ("        private ArrayList<String> fieldValues;");
        printDelete.println ("        private int deleteNum;");
        printDelete.println ();
        printDelete.println ("        public DeleteConfirm (Connection deleteConnection, ResultSet results, ArrayList<String> fieldNames, ArrayList<String> fieldValues)");    //the frame is set up and stores necessary variables for later use
        printDelete.println ("        {");
        printDelete.println ("            super ();");
        printDelete.println ("            setSize (450, 500);");
        printDelete.println ("            setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);");
        printDelete.println ("            setTitle (\"Confirm Delete\");");
        printDelete.println ();
        printDelete.println ("            this.deleteConnection = deleteConnection;");
        printDelete.println ("            this.fieldNames = fieldNames;");
        printDelete.println ("            this.fieldValues = fieldValues;");
        printDelete.println ();
        printDelete.println ("            setLayout (new BorderLayout ());");
        printDelete.println ("            add (setDeleteArea (results), BorderLayout.NORTH);");
        printDelete.println ("            add (setButtonArea (), BorderLayout.SOUTH);");
        printDelete.println ("            pack ();");
        printDelete.println ("        }");
        printDelete.println ();

        //the main area where the user can see about-to-be-deleted records is set up here
        printDelete.println ("        private JPanel setDeleteArea (ResultSet deleteResults)");
        printDelete.println ("        {");
        printDelete.println ("            JPanel mainPanel = new JPanel ();");
        printDelete.println ("            mainPanel.setLayout (new BorderLayout ());");
        printDelete.println ();
        printDelete.println ("            JLabel deleteLabel = new JLabel (\"Will you delete these records?\");");
        printDelete.println ("            deleteLabel.setHorizontalAlignment (SwingConstants.LEFT);");
        printDelete.println ("            mainPanel.add (deleteLabel, BorderLayout.NORTH);");
        printDelete.println ();
        printDelete.println ("            JTextArea deleteArea = new JTextArea (10, 40);");
        printDelete.println ("            deleteArea.setBorder (BorderFactory.createLineBorder (Color.BLACK));");
        printDelete.println ("            deleteArea.setEditable (false);");
        printDelete.println ();
        printDelete.println ("            try");
        printDelete.println ("            {");
        printDelete.println ("                deleteResults.beforeFirst ();");
        printDelete.println ("                deleteNum = 0;");
        printDelete.println ("                while (deleteResults.next () != false)");
        printDelete.println ("                {");
        printDelete.println ("                    deleteNum ++;");
        printDelete.println ("                    String ID = deleteResults.getString (\"ID\");");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printDelete.println ("                    String " + fieldName + " = deleteResults.getString (\"" + fieldName + "\");");
        }
        printDelete.print ("                    deleteArea.append (\"ID = \" + ID + \"\\n\"");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printDelete.print (" + \"" + fieldName + " = \" + " + fieldName + " + \"\\n\"");
        }
        printDelete.println (");");
        printDelete.println ("                    deleteArea.append (\"\\n\");");
        printDelete.println ("                }");
        printDelete.println ("                deleteArea.append (deleteNum + \" result(s)\\n\");");
        printDelete.println ("                deleteResults.close ();");
        printDelete.println ("            }");
        printDelete.println ("            catch (SQLException exception)");
        printDelete.println ("            {");
        printDelete.println ("                deleteArea.append (\"Error - Unable to read delete results\\n\");");
        printDelete.println ("            }");
        printDelete.println ();
        printDelete.println ("            JScrollPane deleteScroll = new JScrollPane (deleteArea);");
        printDelete.println ("            deleteScroll.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);");
        printDelete.println ("            deleteScroll.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);");
        printDelete.println ("            mainPanel.add (deleteScroll, BorderLayout.CENTER);");
        printDelete.println ();
        printDelete.println ("            return (mainPanel);");
        printDelete.println ("        }");
        printDelete.println ();

        //the yes and no buttons for the delete confirm are set up here
        printDelete.println ("        private JPanel setButtonArea ()");
        printDelete.println ("        {");
        printDelete.println ("            JPanel buttonsPanel = new JPanel ();");
        printDelete.println ("            buttonsPanel.setLayout (new FlowLayout (FlowLayout.CENTER));");
        printDelete.println ("            JButton buttonYes = new JButton (\"Yes\");");
        printDelete.println ("            buttonYes.addActionListener (new DeleteListener ());");
        printDelete.println ("            buttonsPanel.add (buttonYes);");
        printDelete.println ("            JButton buttonNo = new JButton (\"No\");");
        printDelete.println ("            buttonNo.addActionListener (new DeleteListener ());");
        printDelete.println ("            buttonsPanel.add (buttonNo);");
        printDelete.println ();
        printDelete.println ("            return (buttonsPanel);");
        printDelete.println ("        }");
        printDelete.println ();

        //the delete listener will take in a 'yes' or 'no' and delete or not correspondingly
        printDelete.println ("        private class DeleteListener implements ActionListener");
        printDelete.println ("        {");
        printDelete.println ("            @Override");
        printDelete.println ("            public void actionPerformed (ActionEvent event)");
        printDelete.println ("            {");
        printDelete.println ("                String deleteCommand = event.getActionCommand ();");
        printDelete.println ();
        printDelete.println ("                if (deleteCommand.equals (\"Yes\"))");
        printDelete.println ("                {");
        printDelete.println ("                    Statement psqlStatement = null;");
        printDelete.println ();
        printDelete.println ("                    String deleteString = \"DELETE FROM " + currentFileName.toLowerCase () + "\";");    //the delete will take place here using the SQL DELETE command using the given info
        printDelete.println ("                    int listLength = fieldNames.size ();");
        printDelete.println ("                    if (listLength > 0)");
        printDelete.println ("                    {");
        printDelete.println ("                        deleteString = deleteString + \" WHERE \";");
        printDelete.println ("                        for (int i = 0; i < listLength - 1; i ++)");
        printDelete.println ("                        {");
        printDelete.println ("                            deleteString = deleteString + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"' AND \";");
        printDelete.println ("                        }");
        printDelete.println ("                        deleteString = deleteString + fieldNames.get (listLength - 1) + \" = '\" + fieldValues.get (listLength - 1) + \"'\";");
        printDelete.println ("                    }");
        printDelete.println ("                    try");
        printDelete.println ("                    {");
        printDelete.println ("                        psqlStatement = deleteConnection.createStatement ();");
        printDelete.println ("                        psqlStatement.executeUpdate (deleteString);");
        printDelete.println ("                        psqlStatement.close ();");
        printDelete.println ("                    }");
        printDelete.println ("                    catch (SQLException exception)");
        printDelete.println ("                    {");
        printDelete.println ("                        dialogInterface.appendToStatusArea (\"Delete failed\\n\\n\");");
        printDelete.println ("                    }");
        printDelete.println ("                    dialogInterface.appendToStatusArea (\"Delete successful\\n\");");
        printDelete.println ("                    dialogInterface.appendToStatusArea (deleteNum + \" record(s) deleted\\n\\n\");");
        printDelete.println ();
        printDelete.println ("                    dispose ();");
        printDelete.println ("                }");
        printDelete.println ("                else if (deleteCommand.equals (\"No\"))");
        printDelete.println ("                {");
        printDelete.println ("                    dispose ();");    //choosing no will simply dispose the window
        printDelete.println ("                    dialogInterface.appendToStatusArea (\"\\n\");\n");
        printDelete.println ("                }");
        printDelete.println ("                else");
        printDelete.println ("                {");
        printDelete.println ("                    System.err.println (\"Unexpected Confirm Delete Window Logic Error\");");
        printDelete.println ("                    return;");
        printDelete.println ("                }");
        printDelete.println ();
        printDelete.println ("                validate ();");
        printDelete.println ("            }");
        printDelete.println ("        }");
        printDelete.println ("    }");
        printDelete.println ("}");
    }

    //makeUpdateListener creates the ActionListener for the Update button if requested in the buttons list (reserved)
    private void makeUpdateListener (PrintWriter printUpdate, ArrayList<String> listOfFields)
    {
        String fieldName = null;
        String fieldType = null;

        printUpdate.println ("import java.awt.event.ActionEvent;");
        printUpdate.println ("import java.awt.event.ActionListener;");
        printUpdate.println ("import java.sql.*;");
        printUpdate.println ("import java.util.ArrayList;");
        printUpdate.println ();

        printUpdate.println ("public class DialogcUpdate implements ActionListener");
        printUpdate.println ("{");
        printUpdate.println ("    private " + currentFileName + "FieldEdit dialogInterface;");
        printUpdate.println ();
        printUpdate.println ("    public DialogcUpdate (" + currentFileName + "FieldEdit theInterface)");
        printUpdate.println ("    {");
        printUpdate.println ("        dialogInterface = theInterface;");
        printUpdate.println ("    }");
        printUpdate.println ();

        //the database update takes place here
        printUpdate.println ("    @Override");
        printUpdate.println ("    public void actionPerformed (ActionEvent event)");
        printUpdate.println ("    {");
        printUpdate.println ("        dialogInterface.appendToStatusArea (\"UPDATE\");");
        printUpdate.println ("        dialogInterface.appendToStatusArea (\"\\n\");");
        printUpdate.println ();
        printUpdate.println ("        Connection updateConnection = dialogInterface.getDBConnection ();");
        printUpdate.println ("        Statement psqlStatement = null;");
        printUpdate.println ();
        printUpdate.println ("        try");
        printUpdate.println ("        {");
        printUpdate.println ("            DatabaseMetaData dbMetaData = updateConnection.getMetaData ();");
        printUpdate.println ("            ResultSet tableCheck = dbMetaData.getTables (null, null, \"" + currentFileName.toLowerCase () +  "\", null);");    //the database is checked for any table of the same name
        printUpdate.println ("            if (tableCheck.next () == false)");    //if there is no table by that name, it is made
        printUpdate.println ("            {");
        printUpdate.println ("                dialogInterface.appendToStatusArea (\"Database table not found. Creating table...\\n\");");
        printUpdate.println ();
        printUpdate.print ("                String createString = \"CREATE TABLE " + currentFileName.toLowerCase () + " ( ID SERIAL");
        int listLength = listOfFields.size ();
        for (int i = 0; i < listLength; i ++)    //each field is represented in the SQL command CREATE TABLE, where integers are INTEGERS, floats are DECIMAL(20,3) and strings are VARCHAR(30)
        {
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            String tableType = null;
            if (fieldType.equals ("integer"))
            {
                tableType = "INTEGER";
            }
            else if (fieldType.equals ("float"))
            {
                tableType = "DECIMAL(20,3)";
            }
            else
            {
                tableType = "VARCHAR(30)";
            }

            printUpdate.print (" , " + fieldName + " " + tableType);
        }
        printUpdate.println (" , PRIMARY KEY (ID))\";");

        printUpdate.println ("                try");
        printUpdate.println ("                {");
        printUpdate.println ("                    psqlStatement = updateConnection.createStatement ();");
        printUpdate.println ("                    psqlStatement.executeUpdate (createString);");
        printUpdate.println ("                    psqlStatement.close ();");
        printUpdate.println ("                }");
        printUpdate.println ("                catch (SQLException exception)");
        printUpdate.println ("                {");
        printUpdate.println ("                    dialogInterface.appendToStatusArea (\"Table unable to be created. Please try again later\\n\\n\");");
        printUpdate.println ("                    return;");
        printUpdate.println ("                }");
        printUpdate.println ();
        printUpdate.println ("                dialogInterface.appendToStatusArea (\"Table created successfully\\n\\n\");");
        printUpdate.println ("            }");
        printUpdate.println ("            else");    //the existing table is checked to see if it matches the same details as the generated GUI
        printUpdate.println ("            {");
        printUpdate.println ("                boolean IDCheck = false;");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printUpdate.println ("                boolean " + fieldName + "Check = false;");
        }
        printUpdate.println ();
        printUpdate.println ("                ResultSet columnCheck = dbMetaData.getColumns (null, null, \"" + currentFileName.toLowerCase () + "\", \"%\");");
        printUpdate.println ("                while (columnCheck.next () != false)");
        printUpdate.println ("                {");
        printUpdate.println ("                    String columnName = columnCheck.getString (4);");
        printUpdate.println ("                    int columnType = columnCheck.getInt (5);");
        printUpdate.println ();
        printUpdate.println ("                    if (columnName.equalsIgnoreCase (\"ID\") && columnType == java.sql.Types.INTEGER)");    //comparisons are made here, matching GUI details to table details if a match
        printUpdate.println ("                    {");
        printUpdate.println ("                        IDCheck = true;");
        printUpdate.println ("                    }");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            String sqlType = null;
            if (fieldType.equals ("integer"))
            {
                sqlType = "INTEGER";
            }
            else if (fieldType.equals ("float"))
            {
                sqlType = "NUMERIC";
            }
            else
            {
                sqlType = "VARCHAR";
            }

            printUpdate.println ("                    else if (columnName.equalsIgnoreCase (\"" + fieldName + "\") && columnType == java.sql.Types." + sqlType + ")");
            printUpdate.println ("                    {");
            printUpdate.println ("                        " + fieldName + "Check = true;");
            printUpdate.println ("                    }");
        }
        printUpdate.println ("                    else");
        printUpdate.println ("                    {");
        printUpdate.println ("                        dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");");
        printUpdate.println ("                        return;");
        printUpdate.println ("                    }");
        printUpdate.println ("                }");
        printUpdate.println ();
        printUpdate.print ("                if (IDCheck == false");
        for (int i = 0; i < listLength - 1; i ++)
        {
            fieldName = listOfFields.get (i);
            printUpdate.print (" || " + fieldName + "Check == false");
        }
        fieldName = listOfFields.get (listLength - 1);
        printUpdate.println (" || " + fieldName + "Check == false)");    //if all details match, it is assumed that this table belongs to this GUI
        printUpdate.println ("                {");
        printUpdate.println ("                    dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");");
        printUpdate.println ("                    return;");
        printUpdate.println ("                }");
        printUpdate.println ("            }");
        printUpdate.println ("        }");
        printUpdate.println ("        catch (SQLException exception)");
        printUpdate.println ("        {");
        printUpdate.println ("            dialogInterface.appendToStatusArea (\"Unable to find table. Please try again later\\n\\n\");");
        printUpdate.println ("            return;");
        printUpdate.println ("        }");
        printUpdate.println ();

        //each field is read and checked to see it it fits the update functionality
        printUpdate.println ("        boolean correctInput = true;");
        printUpdate.println ("        boolean noUpdate = true;");
        printUpdate.println ("        ArrayList<String> fieldNames = new ArrayList<String> (10);");
        printUpdate.println ("        ArrayList<String> fieldValues = new ArrayList<String> (10);");
        printUpdate.println ("        String idUpdate = null;");
        printUpdate.println ("        String fieldInput = null;");
        printUpdate.println ();
        printUpdate.println ("        try");
        printUpdate.println ("        {");
        printUpdate.println ("            idUpdate = dialogInterface.getDCID ();");
        printUpdate.println ("        }");
        printUpdate.println ("        catch (IllegalFieldValueException exception)");
        printUpdate.println ("        {");
        printUpdate.println ("            if (exception.getMessage ().trim ().equals (\"\"))");
        printUpdate.println ("            {");
        printUpdate.println ("                dialogInterface.appendToStatusArea (\"Error - ID field is blank\\n\");");
        printUpdate.println ("                correctInput = false;");
        printUpdate.println ("            }");
        printUpdate.println ("            else");
        printUpdate.println ("            {");
        printUpdate.println ("                dialogInterface.appendToStatusArea (\"Error - ID field not an integer\\n\");");
        printUpdate.println ("                correctInput = false;");
        printUpdate.println ("            }");
        printUpdate.println ("        }");
        for (int i = 0; i < listLength; i ++)
        {
            printUpdate.println ();
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            if (fieldType.equals ("integer") || fieldType.equals ("float"))
            {
                String endLabel = null;
                if (fieldType.equals ("integer"))
                {
                    endLabel = "an integer";
                }
                else
                {
                    endLabel = "a float";
                }

                printUpdate.println ("        try");
                printUpdate.println ("        {");
                printUpdate.println ("            fieldInput = dialogInterface.getDC" + fieldName + " ();");
                printUpdate.println ("            fieldNames.add (\"" + fieldName + "\");");
                printUpdate.println ("            fieldValues.add (fieldInput);");
                printUpdate.println ("            noUpdate = false;");
                printUpdate.println ("        }");
                printUpdate.println ("        catch (IllegalFieldValueException exception)");
                printUpdate.println ("        {");
                printUpdate.println ("            if (!exception.getMessage ().trim ().equals (\"\"))");
                printUpdate.println ("            {");
                printUpdate.println ("                dialogInterface.appendToStatusArea (\"Error - " + fieldName + " field not " + endLabel + "\\n\");");
                printUpdate.println ("                correctInput = false;");
                printUpdate.println ("            }");
                printUpdate.println ("        }");
            }
            else
            {
                printUpdate.println ("        try");
                printUpdate.println ("        {");
                printUpdate.println ("            fieldInput = dialogInterface.getDC" + fieldName + " ();");
                printUpdate.println ("            if (!fieldInput.trim ().equals (\"\"))");
                printUpdate.println ("            {");
                printUpdate.println ("                fieldNames.add (\"" + fieldName + "\");");
                printUpdate.println ("                fieldValues.add (fieldInput);");
                printUpdate.println ("                noUpdate = false;");
                printUpdate.println ("            }");
                printUpdate.println ("        }");
                printUpdate.println ("        catch (IllegalFieldValueException exception)");
                printUpdate.println ("        {");
                printUpdate.println ("            dialogInterface.appendToStatusArea (\"Error - " + fieldName + " field not a string\\n\");");
                printUpdate.println ("            correctInput = false;");
                printUpdate.println ("        }");
            }
        }
        printUpdate.println ();
        printUpdate.println ("        if (correctInput == false)");    //if any field does not match the prerequisites, the listener returns without further action
        printUpdate.println ("        {");
        printUpdate.println ("            dialogInterface.appendToStatusArea (\"\\n\");");
        printUpdate.println ("            return;");
        printUpdate.println ("        }");
        printUpdate.println ("        else if (noUpdate == true)");    //if only an id field is given, there is no real update and the status message reflects this
        printUpdate.println ("        {");
        printUpdate.println ("            dialogInterface.appendToStatusArea (\"Error - Nothing to update\\n\\n\");");
        printUpdate.println ("            return;");
        printUpdate.println ("        }");
        printUpdate.println ();

        //the id is checked to see if it currently exists in the database (so that only existing id's are updated)
        printUpdate.println ("        boolean validID = false;");
        printUpdate.println ("        String checkID = \"SELECT ID FROM " + currentFileName.toLowerCase () + "\";");
        printUpdate.println ("        try");
        printUpdate.println ("        {");
        printUpdate.println ("            psqlStatement = updateConnection.createStatement ();");
        printUpdate.println ("            ResultSet newResult = psqlStatement.executeQuery (checkID);");
        printUpdate.println ("            while (newResult.next () != false)");
        printUpdate.println ("            {");
        printUpdate.println ("                if (idUpdate.equals (newResult.getString (1)))");
        printUpdate.println ("                {");
        printUpdate.println ("                    validID = true;");
        printUpdate.println ("                }");
        printUpdate.println ("            }");
        printUpdate.println ("            psqlStatement.close ();");
        printUpdate.println ("        }");
        printUpdate.println ("        catch (SQLException exception)");
        printUpdate.println ("        {");
        printUpdate.println ("            dialogInterface.appendToStatusArea (\"ID check query failed\\n\\n\");");
        printUpdate.println ("            return;");
        printUpdate.println ("        }");
        printUpdate.println ("        if (validID == false)");
        printUpdate.println ("        {");
        printUpdate.println ("            dialogInterface.appendToStatusArea (\"Error - Invalid ID entered\\n\\n\");");
        printUpdate.println ("            return;");
        printUpdate.println ("        }");
        printUpdate.println ();

        //if the id check is successful, then an SQL UPDATE command is made to update the database with the given info
        printUpdate.println ("        int listLength = fieldNames.size ();");
        printUpdate.println ("        String updateString = \"UPDATE " + currentFileName.toLowerCase () + " SET \";");
        printUpdate.println ("        for (int i = 0; i < listLength - 1; i ++)");
        printUpdate.println ("        {");
        printUpdate.println ("            updateString = updateString + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"', \";");
        printUpdate.println ("        }");
        printUpdate.println ("        updateString = updateString + fieldNames.get (listLength - 1) + \" = \" + fieldValues.get (listLength - 1) + \" WHERE ID = \" + idUpdate;");
        printUpdate.println ();
        printUpdate.println ("        try");
        printUpdate.println ("        {");
        printUpdate.println ("            psqlStatement = updateConnection.createStatement ();");
        printUpdate.println ("            psqlStatement.executeUpdate (updateString);");
        printUpdate.println ("            psqlStatement.close ();");
        printUpdate.println ("        }");
        printUpdate.println ("        catch (SQLException exception)");
        printUpdate.println ("        {");
        printUpdate.println ("            dialogInterface.appendToStatusArea (\"Update failed\\n\\n\");");
        printUpdate.println ("            return;");
        printUpdate.println ("        }");
        printUpdate.println ("        dialogInterface.appendToStatusArea (\"Update successful\\n\\n\");");
        printUpdate.println ();

        //if the update is successful then the updated row is placed onto the GUI
        printUpdate.println ("        String checkUpdate = \"SELECT * FROM " + currentFileName.toLowerCase () + " WHERE ID = \" + idUpdate;");
        printUpdate.println ("        try");
        printUpdate.println ("        {");
        printUpdate.println ("            psqlStatement = updateConnection.createStatement ();");
        printUpdate.println ("            ResultSet newResult = psqlStatement.executeQuery (checkUpdate);");
        printUpdate.println ("            while (newResult.next () != false)");
        printUpdate.println ("            {");
        printUpdate.println ("                dialogInterface.setDCID (newResult.getString (\"ID\"));");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printUpdate.println ("                dialogInterface.setDC" + fieldName + " (newResult.getString (\"" + fieldName + "\"));");
        }
        printUpdate.println ("            }");
        printUpdate.println ("            psqlStatement.close ();");
        printUpdate.println ("            newResult.close ();");
        printUpdate.println ("        }");
        printUpdate.println ("        catch (SQLException exception)");
        printUpdate.println ("        {");
        printUpdate.println ("            dialogInterface.appendToStatusArea (\"Result query failed\\n\\n\");");
        printUpdate.println ("        }");
        printUpdate.println ("    }");
        printUpdate.println ("}");
    }

    //makeUpdateListener creates the ActionListener for the Query button if requested in the buttons list (reserved)
    private void makeQueryListener (PrintWriter printQuery, ArrayList<String> listOfFields)
    {
        String fieldName = null;
        String fieldType = null;

        printQuery.println ("import java.awt.event.ActionEvent;");
        printQuery.println ("import java.awt.event.ActionListener;");
        printQuery.println ("import java.sql.*;");
        printQuery.println ("import java.util.ArrayList;");
        printQuery.println ();

        printQuery.println ("public class DialogcQuery implements ActionListener");
        printQuery.println ("{");
        printQuery.println ("    private " + currentFileName + "FieldEdit dialogInterface;");
        printQuery.println ();
        printQuery.println ("    public DialogcQuery (" + currentFileName + "FieldEdit theInterface)");
        printQuery.println ("    {");
        printQuery.println ("        dialogInterface = theInterface;");
        printQuery.println ("    }");
        printQuery.println ();

        //the database query takes palce here
        printQuery.println ("    @Override");
        printQuery.println ("    public void actionPerformed (ActionEvent event)");
        printQuery.println ("    {");
        printQuery.println ("        dialogInterface.appendToStatusArea (\"QUERY\");");
        printQuery.println ("        dialogInterface.appendToStatusArea (\"\\n\");");
        printQuery.println ();
        printQuery.println ("        Connection queryConnection = dialogInterface.getDBConnection ();");
        printQuery.println ("        Statement psqlStatement = null;");
        printQuery.println ();
        printQuery.println ("        try");
        printQuery.println ("        {");
        printQuery.println ("            DatabaseMetaData dbMetaData = queryConnection.getMetaData ();");
        printQuery.println ("            ResultSet tableCheck = dbMetaData.getTables (null, null, \"" + currentFileName.toLowerCase () +  "\", null);");    //the database is checked for any table of the same name
        printQuery.println ("            if (tableCheck.next () == false)");    //if there is no table by that name, it is made
        printQuery.println ("            {");
        printQuery.println ("                dialogInterface.appendToStatusArea (\"Database table not found. Creating table...\\n\");");
        printQuery.println ();
        printQuery.print ("                String createString = \"CREATE TABLE " + currentFileName.toLowerCase () + " ( ID SERIAL");
        int listLength = listOfFields.size ();
        for (int i = 0; i < listLength; i ++)    //each field is represented in the SQL command CREATE TABLE, where integers are INTEGERS, floats are DECIMAL(20,3) and strings are VARCHAR(30)
        {
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            String tableType = null;
            if (fieldType.equals ("integer"))
            {
                tableType = "INTEGER";
            }
            else if (fieldType.equals ("float"))
            {
                tableType = "DECIMAL(20,3)";
            }
            else
            {
                tableType = "VARCHAR(30)";
            }

            printQuery.print (" , " + fieldName + " " + tableType);
        }
        printQuery.println (" , PRIMARY KEY (ID))\";");

        printQuery.println ("                try");
        printQuery.println ("                {");
        printQuery.println ("                    psqlStatement = queryConnection.createStatement ();");
        printQuery.println ("                    psqlStatement.executeUpdate (createString);");
        printQuery.println ("                    psqlStatement.close ();");
        printQuery.println ("                }");
        printQuery.println ("                catch (SQLException exception)");
        printQuery.println ("                {");
        printQuery.println ("                    dialogInterface.appendToStatusArea (\"Table unable to be created. Please try again later\\n\\n\");");
        printQuery.println ("                    return;");
        printQuery.println ("                }");
        printQuery.println ();
        printQuery.println ("                dialogInterface.appendToStatusArea (\"Table created successfully\\n\\n\");");
        printQuery.println ("            }");
        printQuery.println ("            else");    //the existing table is checked to see if it matches the same details as the generated GUI
        printQuery.println ("            {");
        printQuery.println ("                boolean IDCheck = false;");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printQuery.println ("                boolean " + fieldName + "Check = false;");
        }
        printQuery.println ();
        printQuery.println ("                ResultSet columnCheck = dbMetaData.getColumns (null, null, \"" + currentFileName.toLowerCase () + "\", \"%\");");
        printQuery.println ("                while (columnCheck.next () != false)");
        printQuery.println ("                {");
        printQuery.println ("                    String columnName = columnCheck.getString (4);");
        printQuery.println ("                    int columnType = columnCheck.getInt (5);");
        printQuery.println ();
        printQuery.println ("                    if (columnName.equalsIgnoreCase (\"ID\") && columnType == java.sql.Types.INTEGER)");    //comparisons are made here, matching GUI details to table details if a match
        printQuery.println ("                    {");
        printQuery.println ("                        IDCheck = true;");
        printQuery.println ("                    }");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            String sqlType = null;
            if (fieldType.equals ("integer"))
            {
                sqlType = "INTEGER";
            }
            else if (fieldType.equals ("float"))
            {
                sqlType = "NUMERIC";
            }
            else
            {
                sqlType = "VARCHAR";
            }

            printQuery.println ("                    else if (columnName.equalsIgnoreCase (\"" + fieldName + "\") && columnType == java.sql.Types." + sqlType + ")");
            printQuery.println ("                    {");
            printQuery.println ("                        " + fieldName + "Check = true;");
            printQuery.println ("                    }");
        }
        printQuery.println ("                    else");
        printQuery.println ("                    {");
        printQuery.println ("                        dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");");
        printQuery.println ("                        return;");
        printQuery.println ("                    }");
        printQuery.println ("                }");
        printQuery.println ();
        printQuery.print ("                if (IDCheck == false");
        for (int i = 0; i < listLength - 1; i ++)
        {
            fieldName = listOfFields.get (i);
            printQuery.print (" || " + fieldName + "Check == false");
        }
        fieldName = listOfFields.get (listLength - 1);
        printQuery.println (" || " + fieldName + "Check == false)");    //if all details match, it is assumed that this table belongs to this GUI
        printQuery.println ("                {");
        printQuery.println ("                    dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");");
        printQuery.println ("                    return;");
        printQuery.println ("                }");
        printQuery.println ("            }");
        printQuery.println ("        }");
        printQuery.println ("        catch (SQLException exception)");
        printQuery.println ("        {");
        printQuery.println ("            dialogInterface.appendToStatusArea (\"Unable to find table. Please try again later\\n\\n\");");
        printQuery.println ("            return;");
        printQuery.println ("        }");
        printQuery.println ();

        //each field is read and checked to see it it fits the query functionality
        printQuery.println ("        boolean correctInput = true;");
        printQuery.println ("        boolean idQuery = false;");
        printQuery.println ("        ArrayList<String> fieldNames = new ArrayList<String> (10);");
        printQuery.println ("        ArrayList<String> fieldValues = new ArrayList<String> (10);");
        printQuery.println ("        String idInput = null;");
        printQuery.println ("        String fieldInput = null;");
        printQuery.println ();
        printQuery.println ("        try");
        printQuery.println ("        {");
        printQuery.println ("            idInput = dialogInterface.getDCID ();");
        printQuery.println ("            fieldNames.add (\"ID\");");
        printQuery.println ("            fieldValues.add (idInput);");
        printQuery.println ("            idQuery = true;");
        printQuery.println ("        }");
        printQuery.println ("        catch (IllegalFieldValueException exception)");
        printQuery.println ("        {");
        printQuery.println ("            if (!exception.getMessage ().trim ().equals (\"\"))");
        printQuery.println ("            {");
        printQuery.println ("                dialogInterface.appendToStatusArea (\"Error - ID field not an integer\\n\");");
        printQuery.println ("                correctInput = false;");
        printQuery.println ("            }");
        printQuery.println ("        }");
        for (int i = 0; i < listLength; i ++)
        {
            printQuery.println ();
            fieldName = listOfFields.get (i);
            fieldType = Dialogc.getType (fieldName);
            if (fieldType.equals ("integer") || fieldType.equals ("float"))
            {
                String endLabel = null;
                if (fieldType.equals ("integer"))
                {
                    endLabel = "an integer";
                }
                else
                {
                    endLabel = "a float";
                }

                printQuery.println ("        try");
                printQuery.println ("        {");
                printQuery.println ("            fieldInput = dialogInterface.getDC" + fieldName + " ();");
                printQuery.println ("            fieldNames.add (\"" + fieldName + "\");");
                printQuery.println ("            fieldValues.add (fieldInput);");
                printQuery.println ("        }");
                printQuery.println ("        catch (IllegalFieldValueException exception)");
                printQuery.println ("        {");
                printQuery.println ("            if (!exception.getMessage ().trim ().equals (\"\"))");
                printQuery.println ("            {");
                printQuery.println ("                dialogInterface.appendToStatusArea (\"Error - " + fieldName + " field not " + endLabel + "\\n\");");
                printQuery.println ("                correctInput = false;");
                printQuery.println ("            }");
                printQuery.println ("        }");
            }
            else
            {
                printQuery.println ("        try");
                printQuery.println ("        {");
                printQuery.println ("            fieldInput = dialogInterface.getDC" + fieldName + " ();");
                printQuery.println ("            if (!fieldInput.trim ().equals (\"\"))");
                printQuery.println ("            {");
                printQuery.println ("                fieldNames.add (\"" + fieldName + "\");");
                printQuery.println ("                fieldValues.add (fieldInput);");
                printQuery.println ("            }");
                printQuery.println ("        }");
                printQuery.println ("        catch (IllegalFieldValueException exception)");
                printQuery.println ("        {");
                printQuery.println ("            dialogInterface.appendToStatusArea (\"Error - " + fieldName + " field not a string\\n\");");
                printQuery.println ("            correctInput = false;");
                printQuery.println ("        }");
            }
        }
        printQuery.println ();
        printQuery.println ("        if (correctInput == false)");
        printQuery.println ("        {");
        printQuery.println ("            dialogInterface.appendToStatusArea (\"\\n\");");
        printQuery.println ("            return;");
        printQuery.println ("        }");
        printQuery.println ();

        //a query is formed based on the information given
        printQuery.println ("        String queryString = \"SELECT * FROM " + currentFileName.toLowerCase () + "\";");
        printQuery.println ("        int listLength = fieldNames.size ();");
        printQuery.println ("        if (idQuery == true)");
        printQuery.println ("        {");
        printQuery.println ("            queryString = queryString + \" WHERE ID = \" + idInput;");
        printQuery.println ("        }");
        printQuery.println ("        else if (listLength > 0)");
        printQuery.println ("        {");
        printQuery.println ("            queryString = queryString + \" WHERE \";");
        printQuery.println ("            for (int i = 0; i < listLength - 1; i ++)");
        printQuery.println ("            {");
        printQuery.println ("                queryString = queryString + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"' AND \";");
        printQuery.println ("            }");
        printQuery.println ("            queryString = queryString + fieldNames.get (listLength - 1) + \" = '\" + fieldValues.get (listLength - 1) + \"'\";");
        printQuery.println ("        }");
        printQuery.println ("        try");
        printQuery.println ("        {");
        printQuery.println ("            psqlStatement = queryConnection.createStatement ();");
        printQuery.println ("            ResultSet results = psqlStatement.executeQuery (queryString);");
        printQuery.println ();
        printQuery.println ("            int numOfMatches = 0;");
        printQuery.println ("            String ID = null;");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printQuery.println ("            String " + fieldName + " = null;");
        }
        printQuery.println ();
        printQuery.println ("            while (results.next () != false)");
        printQuery.println ("            {");
        printQuery.println ("                numOfMatches ++;");
        printQuery.println ("                ID = results.getString (\"ID\");");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printQuery.println ("                " + fieldName + " = results.getString (\"" + fieldName + "\");");
        }
        printQuery.print ("                    dialogInterface.appendToStatusArea (\"ID = \" + ID + \"\\n\"");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printQuery.print (" + \"" + fieldName + " = \" + " + fieldName + " + \"\\n\"");
        }
        printQuery.println (");");
        printQuery.println ("                dialogInterface.appendToStatusArea (\"\\n\");");
        printQuery.println ("            }");
        printQuery.println ();

        //if the query was done through the id, then the fields are replaced with the id's data
        printQuery.println ("            dialogInterface.appendToStatusArea (numOfMatches + \" result(s)\\n\\n\");");
        printQuery.println ("            if (numOfMatches == 1 && idQuery == true)");
        printQuery.println ("            {");
        printQuery.println ("                dialogInterface.setDCID (ID);");
        for (int i = 0; i < listLength; i ++)
        {
            fieldName = listOfFields.get (i);
            printQuery.println ("                dialogInterface.setDC" + fieldName + " (" + fieldName + ");");
        }
        printQuery.println ("            }");
        printQuery.println ();
        printQuery.println ("            psqlStatement.close ();");
        printQuery.println ("            results.close ();");
        printQuery.println ("        }");
        printQuery.println ("        catch (SQLException exception)");
        printQuery.println ("        {");
        printQuery.println ("            dialogInterface.appendToStatusArea (\"Query failed\\n\\n\");");
        printQuery.println ("        }");
        printQuery.println ("    }");
        printQuery.println ("}");
    }

    //ChangeConfigWindow class allows for changing the config settings of the compilation and running of the generated program
    private class ChangeConfigWindow extends JFrame implements ActionListener
    {
        JTextField textField;
        String changeType;

        public ChangeConfigWindow (String title, String label, boolean isThereBrowse)
        {
            super ();
            setSize (350, 115);

            setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);    //lets change config window be closed without program exiting

            setTitle (title);
            setLayout (new BorderLayout ());

            JPanel textPanel = new JPanel ();
            textPanel.setBackground (Color.WHITE);
            textPanel.add (new JLabel (label + ":"));
            textField = new JTextField (TEXT_FIELD_LENGTH);
            if (label.equals ("Java Compiler"))    //the text field is set to the current setting of the specific config menu item
            {
                textField.setText (javaCompiler);
            }
            else if (label.equals ("Compile options"))
            {
                textField.setText (compileOptions);
            }
            else if (label.equals ("Java Run-time"))
            {
                textField.setText (javaRun);
            }
            else if (label.equals ("Run-time options"))
            {
                textField.setText (runOptions);
            }
            else if (label.equals ("Working Directory"))
            {
                textField.setText (workingDirectory);
            }
            textPanel.add (textField);
            add (textPanel, BorderLayout.CENTER);

            changeType = label;    //sets the action command as an attribute to be used when interpreting input

            JPanel saveCancelPanel = new JPanel ();
            saveCancelPanel.setLayout (new FlowLayout (FlowLayout.CENTER));
            saveCancelPanel.setBackground (Color.WHITE);
            JButton saveButton = new JButton ("Save");
            saveButton.addActionListener (this);
            saveCancelPanel.add (saveButton);
            if (isThereBrowse == true)    //creates a browse button if isThereBrowse is true
            {
                JButton browseButton = new JButton ("Browse...");
                browseButton.addActionListener (this);
                saveCancelPanel.add (browseButton);
            }
            JButton cancelButton = new JButton ("Cancel");
            cancelButton.addActionListener (this);
            saveCancelPanel.add (cancelButton);

            add (saveCancelPanel, BorderLayout.SOUTH);
        }

        @Override
        public void actionPerformed (ActionEvent event)
        {
            String changeCommand = event.getActionCommand ();

            if (changeCommand.equals ("Save"))
            {
                String userEntry = textField.getText ().trim ();
                textField.setText ("");

                //the appropriate config setting is modified and the setting updated in the menu
                if (changeType.equals ("Java Compiler"))
                {
                    javaCompiler = userEntry;
                    compileLabel.setText (javaCompiler);
                }
                else if (changeType.equals ("Compile options"))
                {
                    compileOptions = userEntry;
                    compileOptionsLabel.setText (compileOptions);
                }
                else if (changeType.equals ("Java Run-time"))
                {
                    javaRun = userEntry;
                    runLabel.setText (javaRun);
                }
                else if (changeType.equals ("Run-time options"))
                {
                    runOptions = userEntry;
                    runOptionsLabel.setText (runOptions);
                }
                else if (changeType.equals ("Working Directory"))
                {
                    workingDirectory = userEntry;
                    directoryLabel.setText (workingDirectory);
                }

                dispose ();
            }
            else if (changeCommand.equals ("Browse..."))
            {
                //opens a brand new open dialog (no .config filter) to choose a file to compile/run with (ideally)
                JFileChooser selectProgram = new JFileChooser (new File (System.getProperty ("user.dir")));
                int openType = selectProgram.showOpenDialog (null);

                switch (openType)
                {
                    //an approved file is retrieved and settings are adjusted accordingly
                    case JFileChooser.APPROVE_OPTION:
                        File openFile = selectProgram.getSelectedFile ();
                        String chosenProgram = openFile.getAbsolutePath ();
                        if (changeType.equals ("Java Compiler"))
                        {
                            javaCompiler = chosenProgram;
                            compileLabel.setText (javaCompiler);
                        }
                        else if (changeType.equals ("Java Run-time"))
                        {
                            javaRun = chosenProgram;
                            runLabel.setText (javaRun);
                        }

                        validate ();
                        dispose ();
                        break;

                    case JFileChooser.CANCEL_OPTION:
                        return;

                    default:
                        System.err.println ("Error - Unknown Open Dialog Error Occurred");
                }
            }
            else if (changeCommand.equals ("Cancel"))
            {
                //closes ChangeConfig window
                dispose ();
            }
            else
            {
                System.err.println ("Error - Unexpected Change Config Window Error");
            }

            validate ();
        }
    }

    //makeErrorWindow without a Process calls the version of the method with a Process with a null argument for the Process
    private void makeErrorWindow (String errorMessage, int width, int height)
    {
        makeErrorWindow (errorMessage, width, height, null);
    }

    //makeErrorWindow creates a DismissableWindow with an Error heading a corresponding error message and a process to retrieve stderr text from if any, with size integers to fit the message
    private void makeErrorWindow (String errorMessage, int width, int height, Process errorProcess)
    {
        JPanel errorPanel = new JPanel ();
        errorPanel.setBackground (Color.WHITE);
        JLabel errorLabel = new JLabel (errorMessage);
        errorLabel.setHorizontalAlignment (SwingConstants.CENTER);
        errorPanel.add (errorLabel);

        //if a Process is included assuming there is text in stderr, that text is printed onto a reserved text area and placed into errorPanel
        if (errorProcess != null)
        {
            errorPanel.setPreferredSize (new Dimension (width, height));
            errorArea.setText ("");

            //the Process' error stream is placed into a wrapper class that reads bytes of a stream into more readily accessible character lines to be printed to the JTextArea
            InputStream stderr = errorProcess.getErrorStream ();
            BufferedReader errorReader = new BufferedReader (new InputStreamReader (stderr));

            //the buffered reading prints lines into a JTextArea to be put within a DismissableWindow
            try
            {
                String errorString = errorReader.readLine ();
                while (errorString != null)
                {
                    errorArea.append (errorString + "\n");
                    errorString = errorReader.readLine ();
                }
            }
            catch (IOException exception)
            {
                errorArea.append ("Error - Cannot read from process' error stream\n");
            }

            errorPanel.add (errorScroll);
        }

        DismissableWindow openError = new DismissableWindow ("Error", width, height, errorPanel);
        openError.setVisible (true);
    }

    //DismissableWindow class allows for a general structure of a dismissable window and given attributes, it will make a proper window
    private class DismissableWindow extends JFrame implements ActionListener
    {
        public DismissableWindow (String windowTitle, int width, int height, JPanel message)
        {
            super ();
            setSize (width, height);

            setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);    //lets dismissable window be closed without program exiting

            setTitle (windowTitle);
            setLayout (new BorderLayout ());

            add (message, BorderLayout.CENTER);

            JPanel dismissPanel = new JPanel ();
            dismissPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));
            dismissPanel.setBackground (Color.WHITE);
            JButton dismissButton = new JButton ("Dismiss");
            dismissButton.addActionListener (this);
            dismissPanel.add (dismissButton);

            add (dismissPanel, BorderLayout.SOUTH);
            pack ();
        }

        @Override
        public void actionPerformed (ActionEvent event)
        {
            String dismissCommand = event.getActionCommand ();

            if (dismissCommand.equals ("Dismiss"))
            {
                //closes dismissable window
                dispose ();
            }
            else
            {
                System.err.println ("Error - Unexpected Dismissable Window Error");
            }

            validate ();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Dialogc newDialog = new Dialogc ();
        newDialog.setVisible (true);
    }
}
