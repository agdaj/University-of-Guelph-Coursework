/****************************************
Name: Jireh Agda
Student ID: 0795472
Date Last Modified: 2015 04 03
Yacc Specification File for yadc Compiler
****************************************/

%{
    #include <ctype.h>
    #include <dirent.h>
    #include <stdio.h>
    #include <stdlib.h>
    #include <string.h>
    #include <unistd.h>
    #include "hashTableADT.h"

    #define SUCCESS 0
    #define FAIL 1
    #define TRUE 0
    #define FALSE 1

    char *strdup(const char *s);

    int yylex ();
    void yyerror (char * errorString);

    void addToFieldsList (char * pointerToValue);
    void addToButtonsList (char * pointerToValue);
    void initializeParse (int argc, char * argv[]);
    void verifyParse ();
    void makeCompiledClass (FILE * printClass, char * fileName);
    void makeInterface (FILE * printInterface, char * fileName);
    void makeException (FILE * printException);
    void makeAddListener (FILE * printAdd, char * fileName);
    void makeDeleteListener (FILE * printDelete, char * fileName);
    void makeUpdateListener (FILE * printUpdate, char * fileName);
    void makeQueryListener (FILE * printQuery, char * fileName);
    void cleanUp ();
    void freeString (void * stringToFree);

    char * title = NULL;
    char ** buttonsList = NULL;
    char ** fieldsList = NULL;
    int numOfButtons = 0;
    int numOfFields = 0;

    int addRequested = FALSE;
    int deleteRequested = FALSE;
    int updateRequested = FALSE;
    int queryRequested = FALSE;    
    int parseSuccess = SUCCESS;

    HashTable * symbolTable;
    extern FILE * yyin;
%}

/*the only token type of concern are strings*/
%union
{
    char * string;
}

%token TITLE FIELDS BUTTONS EQUALS LBRACE COMMA RBRACE SCOLON BLANK ERROR
%token <string> PARAM VALUE

%%

             /*the config file is to have declarations up top and parameters for fields and buttons after*/
config       : declarations parameters ;

             /*the main title, fields and buttons declarations can occur in any order*/
declarations : titleParam fieldsParam buttonsParam | titleParam buttonsParam fieldsParam
             | fieldsParam titleParam buttonsParam | fieldsParam buttonsParam titleParam
             | buttonsParam titleParam fieldsParam | buttonsParam fieldsParam titleParam
             ;

             /*the title, fields and buttons paramters' strcuture is outlined here. Title has its contents copied immediately after being seen*/
titleParam   : TITLE EQUALS VALUE SCOLON                             { title = strdup ($3); free ($3); } ;
fieldsParam  : FIELDS EQUALS LBRACE fieldsItem RBRACE SCOLON 
             | FIELDS EQUALS LBRACE RBRACE SCOLON                    { yyerror ("Error - Blank fields list not allowed"); parseSuccess = FAIL; } /*Blank fields lists are not allowed*/
             ;
buttonsParam : BUTTONS EQUALS LBRACE buttonsItem RBRACE SCOLON
             | BUTTONS EQUALS LBRACE RBRACE SCOLON                   { yyerror ("Error - Blank buttons list not allowed"); parseSuccess = FAIL; } /*Blank buttons lists are not allowed*/
             ;

             /*The inner contents of the list items fields and buttons are recursively found and set in their own array of future parameters*/
fieldsItem   : VALUE                                                 { addToFieldsList ($1); }
             | fieldsItem COMMA VALUE                                { addToFieldsList ($3); }
             | BLANK                                                 { parseSuccess = FAIL; } /*Blank values are not allowed*/
             | fieldsItem COMMA BLANK                                { parseSuccess = FAIL; }
             ;
buttonsItem  : VALUE                                                 { addToButtonsList ($1); }
             | buttonsItem COMMA VALUE                               { addToButtonsList ($3); }
             | BLANK                                                 { parseSuccess = FAIL; } /*Blank values are not allowed*/
             | buttonsItem COMMA BLANK                               { parseSuccess = FAIL; }
             ;

             /*The rest of the config file will have parameters only with the specified structure, one following another until the last parameter*/
parameters   : parameter
             | parameter parameters
             ;
parameter    : PARAM EQUALS VALUE SCOLON                             {
                                                                         int i;
                                                                         int isAButton;
                                                                         int isAField;
                                                                         int isAMatch;
                                                                         int numOfMatches;
                                                                         void * tempPointer;

                                                                         isAButton = FALSE;
                                                                         isAField = FALSE;
                                                                         numOfMatches = 0;

                                                                         /*The parameter token is searched for in both lists to see if the parameter is to be expected and not duplicated within the lists*/
                                                                         for (i = 0; i < numOfFields; i ++)
                                                                         {
                                                                             isAMatch = strcmp ($1, fieldsList[i]);
                                                                             if (isAMatch == TRUE)
                                                                             {
                                                                                 isAField = TRUE;
                                                                                 numOfMatches ++;
                                                                             }
                                                                         }
                                                                         for (i = 0; i < numOfButtons; i ++)
                                                                         {
                                                                             isAMatch = strcmp ($1, buttonsList[i]);
                                                                             if (isAMatch == TRUE && !(strcmp ($1, "ADD") == 0 || strcmp ($1, "DELETE") == 0 || strcmp ($1, "UPDATE") == 0 || strcmp ($1, "QUERY") == 0)) /*Reserved button names are exempt from the count of duplication between fields and buttons*/
                                                                             {
                                                                                 isAButton = TRUE;
                                                                                 numOfMatches ++;
                                                                             }
                                                                         }

                                                                         /*Duplicated matches or no matches result in an error, else the parameter and value are added to the hash table if viable*/
                                                                         if (numOfMatches > 1)
                                                                         {
                                                                             yyerror ("Error - Duplicate Parameter Found Listed Above");
                                                                             parseSuccess = FALSE;
                                                                             free ($1);
                                                                             free ($3);
                                                                         }
                                                                         else if (numOfMatches < 1)
                                                                         {
                                                                             yyerror ("Error - Unknown Parameter Found");
                                                                             parseSuccess = FALSE;
                                                                             free ($1);
                                                                             free ($3);
                                                                         }
                                                                         else
                                                                         {
                                                                             tempPointer = lookupHashTable (symbolTable, $1);

                                                                             /*The first viability check is if a field value is either a string, integer, float or something else (error)*/
                                                                             if (isAField == TRUE && !(strcmp ($3, "string") == 0 || strcmp ($3, "integer") == 0 || strcmp ($3, "float") == 0))
                                                                             {
                                                                                 yyerror ("Error - Field Value does not match string/integer/float");
                                                                                 parseSuccess = FALSE;
                                                                                 free ($1);
                                                                                 free ($3);
                                                                             }
                                                                             /*The second viability check is the parameter has a value already registered (i.e. this parameter is a duplicate)*/
                                                                             else if (tempPointer != NULL)
                                                                             {
                                                                                 yyerror ("Error - Duplicate Parameter Found");
                                                                                 parseSuccess = FALSE;
                                                                                 free ($1);
                                                                                 free ($3);
                                                                             }
                                                                             /*The third viability check is if a requested reserved button is called but has a value attached at the bottom, it is an error*/
                                                                             else if (isAButton == TRUE && (strcmp ($1, "ADD") == 0 || strcmp ($1, "DELETE") == 0 || strcmp ($1, "UPDATE") == 0 || strcmp ($1, "QUERY") == 0))
                                                                             {
                                                                                 yyerror ("Error - Reserved button has a value");
                                                                                 parseSuccess = FALSE;
                                                                                 free ($1);
                                                                                 free ($3);
                                                                             }
                                                                             else
                                                                             {
                                                                                 insertHashTable (symbolTable, $1, $3);
                                                                                 free ($1);
                                                                             }
                                                                         }
                                                                     } 
             | PARAM EQUALS BLANK SCOLON                             { parseSuccess = FAIL; } /*Blank values are not allowed*/
             ;

%%

/****
Preconditions: A VALUE token was received from a .config file
Postconditions: The value of VALUE is copied onto the working fields array to be used later, else the parse fails
****/
void addToFieldsList (char * pointerToValue)
{
    char ** temp;

    /*The string content is added on the fieldsList with realloc*/
    temp = realloc (fieldsList, sizeof (char *) * (numOfFields + 1));
    if (temp == NULL)
    {
        fprintf (stderr, "Error - Memory allocation failed\n");
        parseSuccess = FAIL;
    }
    else
    {
        fieldsList = temp;
        fieldsList[numOfFields] = strdup (pointerToValue);
        free (pointerToValue);
        numOfFields ++;
    }

    return;
}

/****
Preconditions: A VALUE token was received from a .config file
Postconditions: The value of VALUE is copied onto the working buttons array to be used later, else the parse fails
****/
void addToButtonsList (char * pointerToValue)
{
    char ** temp;

    /*The button list element is evaluated to see if a reserved button name is declared or if it is just another button*/ 
    if (strcmp ("ADD", pointerToValue) == 0)
    {
        /*If the ADD button has yet to be declared, a flag will now be set to indicate it has been declared, else it is an error*/
        if (addRequested == FALSE)
        {
            addRequested = TRUE;
        }
        else
        {
            yyerror ("Error - Duplicate ADD button declarations in buttons list");
            parseSuccess = FAIL;
        }
    }
    else if (strcmp ("DELETE", pointerToValue) == 0)
    {
        /*If the DELETE button has yet to be declared, a flag will now be set to indicate it has been declared, else it is an error*/
        if (deleteRequested == FALSE)
        {   
            deleteRequested = TRUE;
        }
        else
        {
            yyerror ("Error - Duplicate DELETE button declarations in buttons list");
            parseSuccess = FAIL;
        }
    }
    else if (strcmp ("UPDATE", pointerToValue) == 0)
    {
        /*If the UPDATE button has yet to be declared, a flag will now be set to indicate it has been declared, else it is an error*/
        if (updateRequested == FALSE)
        {   
            updateRequested = TRUE;
        }
        else
        {
            yyerror ("Error - Duplicate UPDATE button declarations in buttons list");
            parseSuccess = FAIL;
        }
    }
    else if (strcmp ("QUERY", pointerToValue) == 0)
    {
        /*If the QUERY button has yet to be declared, a flag will now be set to indicate it has been declared, else it is an error*/
        if (queryRequested == FALSE)
        {   
            queryRequested = TRUE;
        }
        else
        {
            yyerror ("Error - Duplicate QUERY button declarations in buttons list");
            parseSuccess = FAIL;
        }
    }

    /*The string content is added on the buttonsList with realloc*/
    temp = realloc (buttonsList, sizeof (char *) * (numOfButtons + 1));
    if (temp == NULL)
    {
        fprintf (stderr, "Error - Memory allocation failed\n");
        parseSuccess = FAIL;
    }
    else
    {
        buttonsList = temp;
        buttonsList[numOfButtons] = strdup (pointerToValue);
        free (pointerToValue);
        numOfButtons ++;
    }

    return;
}

int main (int argc, char * argv[])
{
    char fileNameChar;
    char * exceptionName;
    char * interfaceName;
    char * listenerName;
    char * mainFileName;
    char * namePointer;
    FILE * exceptionFile;
    FILE * interfaceFile;
    FILE * listenerFile;
    FILE * mainClassFile;
    int i;
    int dotHolder;
    int length;
    int parseCheck;
    int slashHolder;

    /*The parsing is initialized here*/
    initializeParse (argc, argv);

    /*The parse begins here*/
    parseCheck = yyparse ();

    fclose (yyin);

    /*The parsing results are checked here*/
    verifyParse ();

    /*If the parse fails due to syntax error (parseCheck != 0) or an inner token doesn't match data (e.g. required parameter not found -> parseSuccess == FAIL), then the program returns 1 (FAIL)*/
    if (parseCheck != 0 || parseSuccess == FAIL)
    {
        cleanUp ();
        return (FAIL);
    }
    else
    {
        length = strlen (argv[1]);
        dotHolder = length - 1;
        slashHolder = 0;

        /*The file name is retrieved by finding the last '/' and finding the first '.' from the end, what is in between is the file name (ideally)*/
        /*The '.' is replaced with '\0' and a pointer points to the space ahead the '/' (if it's there)*/
        for (i = 0; i < length; i ++)
        {
            fileNameChar = argv[1][i];
            if (fileNameChar == '/')
            {
                slashHolder = i + 1;
            }
        }
        for (i = length; i >= slashHolder; i --)
        {
            fileNameChar = argv[1][i];
            if (fileNameChar == '.')
            {
                argv[1][i] = '\0';
                dotHolder = i;
            }
        }
        namePointer = &argv[1][slashHolder];

        /*A new length is calculated based on the requirement to create a string to represent the three .java files that need to be generated here*/
        /*It is calculated by directoryName + '/' + .config file name + .java*/
        length = strlen (argv[2]) + 1 + (dotHolder - slashHolder) + 5;
        mainFileName = malloc (sizeof (char) * (length + 1));
        interfaceName = malloc (sizeof (char) * (length + 9 + 1));    /*+9 is for adding the space for 'FieldEdit'*/
        exceptionName = malloc (sizeof (char) * (length - (dotHolder - slashHolder) + 26 + 1));    /*The length of the .config name is removed and the space for IllegalFieldValueException is made*/
        if (mainFileName == NULL || interfaceName == NULL || exceptionName == NULL)
        {
            fprintf (stderr, "Error - Cannot generate .java files\n");
            if (mainFileName != NULL)
            {
                free (mainFileName);
            }
            if (interfaceName != NULL)
            {
                free (interfaceName);
            }
            if (exceptionName != NULL)
            {
                free (exceptionName);
            }
            cleanUp ();
            return (FAIL);
        }

        /*The file names are constructed with strcpy and strcat*/
        strcpy (mainFileName, argv[2]);
        strcat (mainFileName, "/");
        strcat (mainFileName, namePointer);
        strcat (mainFileName, ".java");
        strcpy (interfaceName, argv[2]);
        strcat (interfaceName, "/");
        strcat (interfaceName, namePointer);
        strcat (interfaceName, "FieldEdit.java");
        strcpy (exceptionName, argv[2]);
        strcat (exceptionName, "/");
        strcat (exceptionName, "IllegalFieldValueException.java");

        /*The .java files are opened or created here to be written with .java content*/
        mainClassFile = fopen (mainFileName, "w");
        interfaceFile = fopen (interfaceName, "w");
        exceptionFile = fopen (exceptionName, "w");
        if (mainClassFile == NULL || interfaceFile == NULL || exceptionFile == NULL)
        {
            fprintf (stderr, "Error - Cannot generate .java files\n");
            free (mainFileName);
            free (interfaceName);
            free (exceptionName);
            cleanUp ();
            return (FAIL);
        }

        /*.java content is generated here*/
        makeCompiledClass (mainClassFile, namePointer);
        makeInterface (interfaceFile, namePointer);
        makeException (exceptionFile);

        free (mainFileName);
        free (interfaceName);
        free (exceptionName);
        fclose (mainClassFile);
        fclose (interfaceFile);
        fclose (exceptionFile);

        /*If the ADD button is requested, it is made*/
        if (addRequested == TRUE)
        {
            listenerName = malloc (sizeof (char) * (length - (dotHolder - slashHolder) + 10 + 1));    /*The length of the .config name is removed and the space for DialogcAdd is made*/
            if (listenerName == NULL)
            {
                fprintf (stderr, "Error - Cannot generate .java listener files\n");
                free (listenerName);
                cleanUp ();
                return (FAIL);
            }

            /*The file names are constructed with strcpy and strcat*/
            strcpy (listenerName, argv[2]);
            strcat (listenerName, "/");
            strcat (listenerName, "DialogcAdd.java");

            /*The .java files are opened or created here to be written with .java content*/
            listenerFile = fopen (listenerName, "w");
            if (listenerFile == NULL)
            {
                fprintf (stderr, "Error - Cannot generate .java listener files\n");
                free (listenerName);
                cleanUp ();
                return (FAIL);
            }

            /*.java AddListener is generated here*/
            makeAddListener (listenerFile, namePointer);

            free (listenerName);
            fclose (listenerFile);
        }

        /*If the DELETE button is requested, it is made*/
        if (deleteRequested == TRUE)
        {
            listenerName = malloc (sizeof (char) * (length - (dotHolder - slashHolder) + 13 + 1));    /*The length of the .config name is removed and the space for DialogcDelete is made*/
            if (listenerName == NULL)
            {
                fprintf (stderr, "Error - Cannot generate .java listener files\n");
                free (listenerName);
                cleanUp ();
                return (FAIL);
            }

            /*The file names are constructed with strcpy and strcat*/
            strcpy (listenerName, argv[2]);
            strcat (listenerName, "/");
            strcat (listenerName, "DialogcDelete.java");

            /*The .java files are opened or created here to be written with .java content*/
            listenerFile = fopen (listenerName, "w");
            if (listenerFile == NULL)
            {
                fprintf (stderr, "Error - Cannot generate .java listener files\n");
                free (listenerName);
                cleanUp ();
                return (FAIL);
            }

            /*.java DeleteListener is generated here*/
            makeDeleteListener (listenerFile, namePointer);

            free (listenerName);
            fclose (listenerFile);
        }

        /*If the UPDATE button is requested, it is made*/
        if (updateRequested == TRUE)
        {
            listenerName = malloc (sizeof (char) * (length - (dotHolder - slashHolder) + 13 + 1));    /*The length of the .config name is removed and the space for DialogcUpdate is made*/
            if (listenerName == NULL)
            {
                fprintf (stderr, "Error - Cannot generate .java listener files\n");
                free (listenerName);
                cleanUp ();
                return (FAIL);
            }

            /*The file names are constructed with strcpy and strcat*/
            strcpy (listenerName, argv[2]);
            strcat (listenerName, "/");
            strcat (listenerName, "DialogcUpdate.java");

            /*The .java files are opened or created here to be written with .java content*/
            listenerFile = fopen (listenerName, "w");
            if (listenerFile == NULL)
            {
                fprintf (stderr, "Error - Cannot generate .java listener files\n");
                free (listenerName);
                cleanUp ();
                return (FAIL);
            }

            /*.java DeleteListener is generated here*/
            makeUpdateListener (listenerFile, namePointer);

            free (listenerName);
            fclose (listenerFile);
        }

        /*If the QUERY button is requested, it is made*/
        if (queryRequested == TRUE)
        {
            listenerName = malloc (sizeof (char) * (length - (dotHolder - slashHolder) + 12 + 1));    /*The length of the .config name is removed and the space for DialogcQuery is made*/
            if (listenerName == NULL)
            {
                fprintf (stderr, "Error - Cannot generate .java listener files\n");
                free (listenerName);
                cleanUp ();
                return (FAIL);
            }

            /*The file names are constructed with strcpy and strcat*/
            strcpy (listenerName, argv[2]);
            strcat (listenerName, "/");
            strcat (listenerName, "DialogcQuery.java");

            /*The .java files are opened or created here to be written with .java content*/
            listenerFile = fopen (listenerName, "w");
            if (listenerFile == NULL)
            {
                fprintf (stderr, "Error - Cannot generate .java listener files\n");
                free (listenerName);
                cleanUp ();
                return (FAIL);
            }

            /*.java QueryListener is generated here*/
            makeQueryListener (listenerFile, namePointer);

            free (listenerName);
            fclose (listenerFile);
        }

        cleanUp ();
        return (SUCCESS);
    }
}

/****
Preconditions: None
Postconditions: The arguments are used to initialize the parsing if correct, else the program exits and returns a FAIL (1)
****/
void initializeParse (int argc, char * argv[])
{
    DIR * tempDirPointer;
    int length;

    /*The arguments are checked for a file name and working directory*/
    if (argc != 3)
    {
        fprintf (stderr, "Error - Must include 2 arguments (.config file and working directory)\n");
        exit (FAIL);
    }

    /*The .config extension is checked for before attempting to start parsing*/
    length = strlen (argv[1]);
    if (length < 8)
    {
        fprintf (stderr, "Error - Argument 2 must be a .config file\n");
        exit (FAIL);
    }
    if (!(argv[1][length - 7] == '.' && argv[1][length - 6] == 'c' && argv[1][length - 5] == 'o' && argv[1][length - 4] == 'n' && argv[1][length - 3] == 'f' && argv[1][length - 2] == 'i' && argv[1][length - 1] == 'g'))
    {
        fprintf (stderr, "Error - Argument 2 must be a .config file\n");
        exit (FAIL);
    }

    /*Argument 3 is checked to see if it is an existing directory or not*/
    if (access (argv[2], F_OK) != 0)    /*First, the string is checked to see if it is an accessible file first (file or directory)*/
    {
        fprintf (stderr, "Error - Argument 3 is not an existing directory\n");
        exit (FAIL);
    }
    tempDirPointer = opendir (argv[2]);
    if (tempDirPointer == NULL)    /*Then the argument is used to open a directory, in which a NULL return means it is a file (thus exit)*/
    {
        fprintf (stderr, "Error - Argument 3 is not an existing directory\n");
        exit (FAIL);
    }
    else
    {
        closedir (tempDirPointer);
    }

    /*The .config file is opened and set to yyin if possible*/
    yyin = fopen (argv[1], "r");
    if (yyin == NULL)
    {
        fprintf (stderr, "Error - Cannot open .config file)\n");
        exit (FAIL);
    }

    /*The hash table to store parameters is initialized*/
    symbolTable = createHashTable (11, &freeString);
    if (symbolTable == NULL)
    {
        exit (FAIL);
    }

    return;
}

/****
Preconditions: The .config file has been parsed through by the yadc compiler
Postconditions: The parameters read in during parse are checked to see if they all have value, modifying parseSuccess to FAIL if incomplete, which will end main early to indicate unsuccessful parse
****/
void verifyParse ()
{
    char * buttonName;
    int i;
    void * valuePointer;

    /*Each listed parameter within fields and buttons are checked to see if they have a value or not. All parameters must have a value or else it is a parse error*/
    for (i = 0; i < numOfFields; i ++)
    {
        valuePointer = lookupHashTable (symbolTable, fieldsList[i]);
        if (valuePointer == NULL)
        {
            fprintf (stderr, "Error - %s does not have a value\n", fieldsList[i]);
            parseSuccess = FAIL;
        }
    }
    for (i = 0; i < numOfButtons; i ++)
    {
        /*If the button name currently being tested is a reserved button name, the check for a value is ignored*/
        buttonName = buttonsList[i];
        if (!(strcmp (buttonName, "ADD") == 0 || strcmp (buttonName, "DELETE") == 0 || strcmp (buttonName, "UPDATE") == 0 || strcmp (buttonName, "QUERY") == 0))
        {
            valuePointer = lookupHashTable (symbolTable, buttonsList[i]);
            if (valuePointer == NULL)
            {
                fprintf (stderr, "Error - %s does not have a value\n", buttonsList[i]);
                parseSuccess = FAIL;
            }
        }
    }

    return;
}

/****
Preconditions: The .config file has been successfully parsed through by the yadc compiler
Postconditions: The .java file containing the main GUI class is generated
****/
void makeCompiledClass (FILE * printClass, char * fileName)
{
    char * regex;
    char * stringPointer;
    char * stringPointerTwo;
    int i;

    fprintf (printClass, "import java.awt.*;\n");
    fprintf (printClass, "import javax.swing.*;\n");
    fprintf (printClass, "import javax.swing.GroupLayout.Alignment;\n");
    fprintf (printClass, "import java.awt.event.ActionEvent;\n");
    fprintf (printClass, "import java.awt.event.ActionListener;\n");
    fprintf (printClass, "import java.sql.*;\n");
    fprintf (printClass, "\n");

    fprintf (printClass, "public class %s extends JFrame implements %sFieldEdit\n", fileName, fileName);
    fprintf (printClass, "{\n");

    /*attributes are stored, with each field name associating with a text field in addition to an ID field*/
    fprintf (printClass, "    private JTextField fieldID;\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringPointer = fieldsList[i];
        fprintf (printClass, "    private JTextField field%s;\n", stringPointer);
    }
    fprintf (printClass, "    private JTextArea statusArea;\n");
    fprintf (printClass, "    private static Connection dbConnection;\n");
    fprintf (printClass, "\n");

    /*produces the constructor of the main class GUI*/
    fprintf (printClass, "    public %s ()\n", fileName);
    fprintf (printClass, "    {\n");
    fprintf (printClass, "        super ();\n");
    fprintf (printClass, "        setSize (450, 500);\n");
    fprintf (printClass, "        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);\n");
    fprintf (printClass, "        setTitle (\"%s\");\n", title);
    fprintf (printClass, "\n");
    fprintf (printClass, "        setLayout (new BorderLayout ());\n");
    fprintf (printClass, "        add (setMainArea (), BorderLayout.NORTH);\n");
    fprintf (printClass, "        add (setStatusArea (), BorderLayout.SOUTH);\n");
    fprintf (printClass, "        pack ();");
    fprintf (printClass, "    }\n");
    fprintf (printClass, "\n");

    /*constructs the main panel of the GUI that lines up labels with text fields and adds the buttons requested*/
    fprintf (printClass, "    private JPanel setMainArea ()\n");
    fprintf (printClass, "    {\n");
    fprintf (printClass, "        JPanel mainPanel = new JPanel ();\n");
    fprintf (printClass, "        mainPanel.setLayout (new BorderLayout ());\n");
    fprintf (printClass, "\n");

    /*initializes the inner components of the panel, including the ID field*/
    fprintf (printClass, "        JLabel label0 = new JLabel (\"ID\");\n");
    fprintf (printClass, "        label0.setHorizontalAlignment (SwingConstants.LEFT);\n");
    fprintf (printClass, "        fieldID = new JTextField (25);\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringPointer = fieldsList[i];
        fprintf (printClass, "        JLabel label%d = new JLabel (\"%s\");\n", (i + 1), stringPointer);
        fprintf (printClass, "        label%d.setHorizontalAlignment (SwingConstants.LEFT);\n", (i + 1));
        fprintf (printClass, "        field%s = new JTextField (25);\n", stringPointer);
    }
    fprintf (printClass, "\n");

    fprintf (printClass, "        JPanel fieldsPanel = new JPanel ();\n");
    fprintf (printClass, "        GroupLayout fieldsLayout = new GroupLayout (fieldsPanel);\n");    /*initializes the layout to group the labels and text fields*/
    fprintf (printClass, "        fieldsPanel.setLayout (fieldsLayout);\n");
    fprintf (printClass, "        fieldsLayout.setAutoCreateGaps (true);\n");
    fprintf (printClass, "        fieldsLayout.setAutoCreateContainerGaps (true);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "        GroupLayout.SequentialGroup fieldGroup = fieldsLayout.createSequentialGroup ();\n");
    fprintf (printClass, "        fieldGroup.addGroup (fieldsLayout.createParallelGroup ().addComponent (label0)");
    for (i = 0; i < numOfFields; i ++)    /*groups together the labels as one column*/
    {
        fprintf (printClass, ".addComponent (label%d)", (i + 1));
    }
    fprintf (printClass, ");\n");
    fprintf (printClass, "        fieldGroup.addGroup (fieldsLayout.createParallelGroup ().addComponent (fieldID)");
    for (i = 0; i < numOfFields; i ++)    /*groups together the text fields as one column*/
    {
        stringPointer = fieldsList[i];
        fprintf (printClass, ".addComponent (field%s)", stringPointer);
    }
    fprintf (printClass, ");\n");
    fprintf (printClass, "        fieldsLayout.setHorizontalGroup(fieldGroup);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "        GroupLayout.SequentialGroup labelGroup = fieldsLayout.createSequentialGroup();\n");
    fprintf (printClass, "        labelGroup.addGroup (fieldsLayout.createParallelGroup (Alignment.BASELINE).addComponent (label0).addComponent (fieldID));\n");
    for (i = 0; i < numOfFields; i ++)    /*groups together corresponding labels with text fields*/
    {
        stringPointer = fieldsList[i];
        fprintf (printClass, "        labelGroup.addGroup (fieldsLayout.createParallelGroup (Alignment.BASELINE).addComponent (label%d).addComponent (field%s));\n", (i + 1), stringPointer);
    }
    fprintf (printClass, "        fieldsLayout.setVerticalGroup(labelGroup);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "        mainPanel.add (fieldsPanel, BorderLayout.CENTER);\n");
    fprintf (printClass, "\n");

    fprintf (printClass, "        JPanel buttonsPanel = new JPanel ();\n");
    fprintf (printClass, "        buttonsPanel.setLayout (new FlowLayout (FlowLayout.CENTER));\n");
    for (i = 0; i < numOfButtons; i ++)    /*implements the buttons, attaching their respective ActionListeners, including the reserved buttons if declared*/
    {
        stringPointer = buttonsList[i];
        if (strcmp (stringPointer, "ADD") == 0)
        {
            fprintf (printClass, "        JButton buttonAdd = new JButton (\"ADD\");\n");
            fprintf (printClass, "        buttonAdd.addActionListener (new DialogcAdd (this));\n");
            fprintf (printClass, "        buttonsPanel.add (buttonAdd);\n");
        }
        else if (strcmp (stringPointer, "DELETE") == 0)
        {
            fprintf (printClass, "        JButton buttonDelete = new JButton (\"DELETE\");\n");
            fprintf (printClass, "        buttonDelete.addActionListener (new DialogcDelete (this));\n");
            fprintf (printClass, "        buttonsPanel.add (buttonDelete);\n");
        }
        else if (strcmp (stringPointer, "UPDATE") == 0)
        {
            fprintf (printClass, "        JButton buttonUpdate = new JButton (\"UPDATE\");\n");
            fprintf (printClass, "        buttonUpdate.addActionListener (new DialogcUpdate (this));\n");
            fprintf (printClass, "        buttonsPanel.add (buttonUpdate);\n");
        }
        else if (strcmp (stringPointer, "QUERY") == 0)
        {
            fprintf (printClass, "        JButton buttonQuery = new JButton (\"QUERY\");\n");
            fprintf (printClass, "        buttonQuery.addActionListener (new DialogcQuery (this));\n");
            fprintf (printClass, "        buttonsPanel.add (buttonQuery);\n");
        }
        else
        {
            fprintf (printClass, "        JButton button%d = new JButton (\"%s\");\n", (i + 1), stringPointer);
            fprintf (printClass, "        button%d.addActionListener (new %s (this));\n", (i + 1), (char *) lookupHashTable (symbolTable, stringPointer));
            fprintf (printClass, "        buttonsPanel.add (button%d);\n", (i + 1));
        }
    }
    fprintf (printClass, "        mainPanel.add (buttonsPanel, BorderLayout.SOUTH);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "        return (mainPanel);\n");
    fprintf (printClass, "    }\n");
    fprintf (printClass, "\n");

    /*the status area is created with the necessary properties, storing the text area as an attribute*/
    fprintf (printClass, "    private JPanel setStatusArea ()\n");
    fprintf (printClass, "    {\n");
    fprintf (printClass, "        JPanel statusPanel = new JPanel ();\n");
    fprintf (printClass, "        statusPanel.setLayout (new BorderLayout ());\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "        JTextField statusLabel = new JTextField (\"STATUS\");\n");
    fprintf (printClass, "        statusLabel.setBorder (BorderFactory.createLoweredBevelBorder ());\n");
    fprintf (printClass, "        statusLabel.setEditable (false);\n");
    fprintf (printClass, "        statusLabel.setHorizontalAlignment (SwingConstants.CENTER);\n");
    fprintf (printClass, "        statusPanel.add (statusLabel, BorderLayout.NORTH);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "        statusArea = new JTextArea (15, 15);\n");
    fprintf (printClass, "        statusArea.setBorder (BorderFactory.createLineBorder (Color.BLACK));\n");
    fprintf (printClass, "        statusArea.setEditable (false);\n");
    fprintf (printClass, "        JScrollPane textScroll = new JScrollPane (statusArea);\n");
    fprintf (printClass, "        textScroll.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);\n");
    fprintf (printClass, "        textScroll.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);\n");
    fprintf (printClass, "        statusPanel.add (textScroll, BorderLayout.CENTER);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "        return (statusPanel);\n");
    fprintf (printClass, "    }\n");
    fprintf (printClass, "\n");

    /*the ID get and set methods are implemented*/
    fprintf (printClass, "    @Override\n");
    fprintf (printClass, "    public String getDCID () throws IllegalFieldValueException\n");
    fprintf (printClass, "    {\n");
    fprintf (printClass, "        String fieldValue = fieldID.getText ();\n");
    fprintf (printClass, "        if (fieldValue.matches (\"[-]?[0-9]+\"))\n");
    fprintf (printClass, "        {\n");
    fprintf (printClass, "            return (fieldValue);\n");
    fprintf (printClass, "        }\n");
    fprintf (printClass, "        else\n");
    fprintf (printClass, "        {\n");
    fprintf (printClass, "            throw new IllegalFieldValueException (fieldValue);\n");
    fprintf (printClass, "        }\n");
    fprintf (printClass, "    }\n");
    fprintf (printClass, "\n");

    fprintf (printClass, "    @Override\n");
    fprintf (printClass, "    public void setDCID (String stringToSet)\n");
    fprintf (printClass, "    {\n");
    fprintf (printClass, "        fieldID.setText (stringToSet);\n");
    fprintf (printClass, "    }\n");
    fprintf (printClass, "\n");

    /*for each field given (other than ID), a get and set method is implemented*/
    for (i = 0; i < numOfFields; i ++)
    {
        stringPointer = fieldsList[i];
        stringPointerTwo = lookupHashTable (symbolTable, stringPointer);

        fprintf (printClass, "    @Override\n");
        fprintf (printClass, "    public String getDC%s ()", stringPointer);
        if (strcmp (stringPointerTwo, "integer") == 0 || strcmp (stringPointerTwo, "float") == 0)    /*the get method declares to throw the IllegalFieldValueException if it is declared to be either an integer or float field*/
        {
            fprintf (printClass, " throws IllegalFieldValueException\n");
        }
        else
        {
            fprintf (printClass, "\n");
        }
        fprintf (printClass, "    {\n");
        fprintf (printClass, "        String fieldValue = field%s.getText ();\n", stringPointer);
        if (strcmp (stringPointerTwo, "integer") == 0 || strcmp (stringPointerTwo, "float") == 0)
        {
            /*the set method differs if there is a restriction of input (integer/float)*/
            /*a regex check is implemented to check for proper input when using the get methods*/
            if (strcmp (stringPointerTwo, "integer") == 0)
            {
                regex = "[-]?[0-9]+";
            }
            else if (strcmp (stringPointerTwo, "float") == 0)
            {
                regex = "[-]?(([0-9]+)|([0-9]*[.]?[0-9]+))";
            }
            else
            {
                regex = NULL;
            }
            fprintf (printClass, "        if (fieldValue.matches (\"%s\"))\n", regex);
            fprintf (printClass, "        {\n");
            fprintf (printClass, "            return (fieldValue);\n");
            fprintf (printClass, "        }\n");
            fprintf (printClass, "        else\n");
            fprintf (printClass, "        {\n");
            fprintf (printClass, "            throw new IllegalFieldValueException (fieldValue);\n");
            fprintf (printClass, "        }\n");
        }
        else
        {
            fprintf (printClass, "        return (fieldValue);\n");
        }
        fprintf (printClass, "    }\n");
        fprintf (printClass, "\n");

        fprintf (printClass, "    @Override\n");
        fprintf (printClass, "    public void setDC%s (String stringToSet)\n", stringPointer);
        fprintf (printClass, "    {\n");
        fprintf (printClass, "        field%s.setText (stringToSet);\n", stringPointer);
        fprintf (printClass, "    }\n");
        fprintf (printClass, "\n");
    }

    /*the appendToStatusArea method is implemented as simple appending the given string to the text area*/
    fprintf (printClass, "    @Override\n");
    fprintf (printClass, "    public void appendToStatusArea (String message)\n");
    fprintf (printClass, "    {\n");
    fprintf (printClass, "        statusArea.append (message);\n");
    fprintf (printClass, "    }\n");
    fprintf (printClass, "\n");

    /*the getDBConnection method is implemented to let ActionListeners access the Connection*/
    fprintf (printClass, "    @Override\n");
    fprintf (printClass, "    public Connection getDBConnection ()\n");
    fprintf (printClass, "    {\n");
    fprintf (printClass, "        return (dbConnection);\n");
    fprintf (printClass, "    }\n");
    fprintf (printClass, "\n");

    /*a private subclass Authenticate is for mediating the authentication of database access before using the generated GUI for database access*/
    fprintf (printClass, "    private static class Authenticate extends JFrame\n");
    fprintf (printClass, "    {\n");
    fprintf (printClass, "        private JTextField usernameField;\n");
    fprintf (printClass, "        private JTextField passwordField;\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "        public Authenticate ()\n");
    fprintf (printClass, "        {\n");
    fprintf (printClass, "            super ();\n");
    fprintf (printClass, "            setSize (450, 500);\n");
    fprintf (printClass, "            setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);\n");
    fprintf (printClass, "            setTitle (\"Postgres Authenticate\");\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "            setLayout (new BorderLayout ());\n");
    fprintf (printClass, "            add (setAuthArea (), BorderLayout.NORTH);\n");
    fprintf (printClass, "            add (setButtonArea (), BorderLayout.SOUTH);\n");
    fprintf (printClass, "            pack ();\n");
    fprintf (printClass, "        }\n");
    fprintf (printClass, "\n");

    /*the main panel of the authentication window is made here, similar to how the fields in the generated GUI is made*/
    fprintf (printClass, "        private JPanel setAuthArea ()\n");
    fprintf (printClass, "        {\n");
    fprintf (printClass, "            JPanel mainPanel = new JPanel ();\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "            JLabel label0 = new JLabel (\"Username:\");\n");
    fprintf (printClass, "            label0.setHorizontalAlignment (SwingConstants.LEFT);\n");
    fprintf (printClass, "            usernameField = new JTextField (25);\n");
    fprintf (printClass, "            JLabel label1 = new JLabel (\"Password:\");\n");
    fprintf (printClass, "            label1.setHorizontalAlignment (SwingConstants.LEFT);\n");
    fprintf (printClass, "            passwordField = new JTextField (25);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "            JPanel fieldsPanel = new JPanel ();\n");
    fprintf (printClass, "            GroupLayout fieldsLayout = new GroupLayout (fieldsPanel);\n");
    fprintf (printClass, "            fieldsPanel.setLayout (fieldsLayout);\n");
    fprintf (printClass, "            fieldsLayout.setAutoCreateGaps (true);\n");
    fprintf (printClass, "            fieldsLayout.setAutoCreateContainerGaps (true);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "            GroupLayout.SequentialGroup fieldGroup = fieldsLayout.createSequentialGroup ();\n");
    fprintf (printClass, "            fieldGroup.addGroup (fieldsLayout.createParallelGroup ().addComponent (label0).addComponent (label1));\n");
    fprintf (printClass, "            fieldGroup.addGroup (fieldsLayout.createParallelGroup ().addComponent (usernameField).addComponent (passwordField));\n");
    fprintf (printClass, "            fieldsLayout.setHorizontalGroup(fieldGroup);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "            GroupLayout.SequentialGroup labelGroup = fieldsLayout.createSequentialGroup();\n");
    fprintf (printClass, "            labelGroup.addGroup (fieldsLayout.createParallelGroup (Alignment.BASELINE).addComponent (label0).addComponent (usernameField));\n");
    fprintf (printClass, "            labelGroup.addGroup (fieldsLayout.createParallelGroup (Alignment.BASELINE).addComponent (label1).addComponent (passwordField));\n");
    fprintf (printClass, "            fieldsLayout.setVerticalGroup(labelGroup);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "            mainPanel.add (fieldsPanel);\n");
    fprintf (printClass, "            return (mainPanel);\n");
    fprintf (printClass, "        }\n");
    fprintf (printClass, "\n");

    /*the buttons 'Sign In' and 'Cancel' are set up here, where signing in attempts for a connection while cancel exits the program*/
    fprintf (printClass, "        private JPanel setButtonArea ()\n");
    fprintf (printClass, "        {\n");
    fprintf (printClass, "            JPanel buttonsPanel = new JPanel ();\n");
    fprintf (printClass, "            buttonsPanel.setLayout (new FlowLayout (FlowLayout.CENTER));\n");
    fprintf (printClass, "            JButton buttonSignIn = new JButton (\"Sign In\");\n");
    fprintf (printClass, "            buttonSignIn.addActionListener (new SignInListener ());\n");
    fprintf (printClass, "            buttonsPanel.add (buttonSignIn);\n");
    fprintf (printClass, "            JButton buttonCancel = new JButton (\"Cancel\");\n");
    fprintf (printClass, "            buttonCancel.addActionListener (new SignInListener ());\n");
    fprintf (printClass, "            buttonsPanel.add (buttonCancel);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "            return (buttonsPanel);\n");
    fprintf (printClass, "        }\n");
    fprintf (printClass, "\n");

    /*the listener that interprets 'Sign In' and 'Cancel' is made here*/
    fprintf (printClass, "        private class SignInListener implements ActionListener\n");
    fprintf (printClass, "        {\n");
    fprintf (printClass, "            @Override\n");
    fprintf (printClass, "            public void actionPerformed (ActionEvent event)\n");
    fprintf (printClass, "            {\n");
    fprintf (printClass, "                String authCommand = event.getActionCommand ();\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                if (authCommand.equals (\"Sign In\"))\n");
    fprintf (printClass, "                {\n");
    fprintf (printClass, "                    String username = usernameField.getText ();\n");
    fprintf (printClass, "                    String password = passwordField.getText ();\n");
    fprintf (printClass, "                    try\n");
    fprintf (printClass, "                    {\n");
    fprintf (printClass, "                        dbConnection = PostgresLogin.getConnection (username, password);\n");
    fprintf (printClass, "                        dispose ();\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                        %s newWindow = new %s ();\n", fileName, fileName);
    fprintf (printClass, "                        newWindow.setVisible (true);\n");
    fprintf (printClass, "                    }\n");
    fprintf (printClass, "                    catch (SQLException exception)\n");
    fprintf (printClass, "                    {\n");
    fprintf (printClass, "                        DismissableWindow errorAuth = new DismissableWindow (\"Authentication Failed\", 320, 100, \"Authentication failed. Please try again.\");\n");
    fprintf (printClass, "                        errorAuth.setVisible (true);\n");
    fprintf (printClass, "                    }\n");
    fprintf (printClass, "                }\n");
    fprintf (printClass, "                else if (authCommand.equals (\"Cancel\"))\n");
    fprintf (printClass, "                {\n");
    fprintf (printClass, "                    System.exit (0);\n");
    fprintf (printClass, "                }\n");
    fprintf (printClass, "                else\n");
    fprintf (printClass, "                {\n");
    fprintf (printClass, "                    System.err.println (\"Unexpected Authentication Logic Error\");\n");
    fprintf (printClass, "                    return;\n");
    fprintf (printClass, "                }\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                validate ();\n");
    fprintf (printClass, "            }\n");
    fprintf (printClass, "        }\n");
    fprintf (printClass, "\n");

    /*a sub-class of a sub-class defines the authentication error popup when the user inputs the incorrect credentials*/
    fprintf (printClass, "        private class DismissableWindow extends JFrame implements ActionListener\n");
    fprintf (printClass, "        {\n");
    fprintf (printClass, "            public DismissableWindow (String windowTitle, int width, int height, String message)\n");
    fprintf (printClass, "            {\n");
    fprintf (printClass, "                super ();\n");
    fprintf (printClass, "                setSize (width, height);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                setTitle (windowTitle);\n");
    fprintf (printClass, "                setLayout (new BorderLayout ());\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                JLabel errorMessage = new JLabel (message);\n");
    fprintf (printClass, "                errorMessage.setHorizontalAlignment (SwingConstants.CENTER);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                add (errorMessage, BorderLayout.CENTER);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                JPanel dismissPanel = new JPanel ();\n");
    fprintf (printClass, "                dismissPanel.setLayout (new FlowLayout (FlowLayout.RIGHT));\n");
    fprintf (printClass, "                JButton dismissButton = new JButton (\"Dismiss\");\n");
    fprintf (printClass, "                dismissButton.addActionListener (this);\n");
    fprintf (printClass, "                dismissPanel.add (dismissButton);\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                add (dismissPanel, BorderLayout.SOUTH);\n");
    fprintf (printClass, "            }\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "            @Override\n");
    fprintf (printClass, "            public void actionPerformed (ActionEvent event)\n");
    fprintf (printClass, "            {\n");
    fprintf (printClass, "                String dismissCommand = event.getActionCommand ();\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                if (dismissCommand.equals (\"Dismiss\"))\n");
    fprintf (printClass, "                {\n");
    fprintf (printClass, "                    dispose ();\n");
    fprintf (printClass, "                }\n");
    fprintf (printClass, "                else\n");
    fprintf (printClass, "                {\n");
    fprintf (printClass, "                    System.err.println (\"Error - Unexpected Dismissable Window Error\");\n");
    fprintf (printClass, "                }\n");
    fprintf (printClass, "\n");
    fprintf (printClass, "                validate ();\n");
    fprintf (printClass, "            }\n");
    fprintf (printClass, "        }\n");
    fprintf (printClass, "    }\n");

    /*a generated GUi program starts with the authentication process before displaying the main GUI upon successful authentication*/
    fprintf (printClass, "    public static void main (String[] args)\n");
    fprintf (printClass, "    {\n");
    fprintf (printClass, "        Authenticate newWindow = new Authenticate ();\n");
    fprintf (printClass, "        newWindow.setVisible (true);\n");
    fprintf (printClass, "    }\n");
    fprintf (printClass, "}\n");

    return;
}

/****
Preconditions: The .config file has been successfully parsed through by the yadc compiler
Postconditions: The .java file containing the interface class is generated
****/
void makeInterface (FILE * printInterface, char * fileName)
{
    char * fieldName;
    int i;

    fprintf (printInterface, "import java.sql.*;\n");
    fprintf (printInterface, "\n");

    fprintf (printInterface, "public interface %sFieldEdit\n", fileName);
    fprintf (printInterface, "{\n");

    fprintf (printInterface, "    public abstract String getDCID () throws IllegalFieldValueException;");
    fprintf (printInterface, "\n");
    fprintf (printInterface, "    public abstract void setDCID (String stringToSet);\n");
    fprintf (printInterface, "\n");

    /*for each fields label (other than ID) a get and set abstract method signature is made*/
    for (i = 0; i < numOfFields; i ++)
    {
        fieldName = fieldsList[i];

        fprintf (printInterface, "    public abstract String getDC%s () throws IllegalFieldValueException;", fieldName);
        fprintf (printInterface, "\n");
        fprintf (printInterface, "    public abstract void setDC%s (String stringToSet);\n", fieldName);
        fprintf (printInterface, "\n");
    }

    fprintf (printInterface, "    public abstract void appendToStatusArea (String message);\n");
    fprintf (printInterface, "\n");
    fprintf (printInterface, "    public abstract Connection getDBConnection ();\n");
    fprintf (printInterface, "}\n");

    return;
}

/****
Preconditions: The .config file has been successfully parsed through by the yadc compiler
Postconditions: The .java file containing the exception class is generated
****/
void makeException (FILE * printException)
{
    fprintf (printException, "public class IllegalFieldValueException extends Exception\n");
    fprintf (printException, "{\n");

    fprintf (printException, "    public IllegalFieldValueException ()\n");
    fprintf (printException, "    {\n");
    fprintf (printException, "        super (\"Field has illegal value\");\n");
    fprintf (printException, "    }\n");
    fprintf (printException, "\n");

    fprintf (printException, "    public IllegalFieldValueException (String message)\n");
    fprintf (printException, "    {\n");
    fprintf (printException, "        super (message);\n");
    fprintf (printException, "    }\n");
    fprintf (printException, "}\n");

    return;
}

/****
Preconditions: The .config file has been successfully parsed through by the yadc compiler and the ADD button has been declared
Postconditions: The .java file containing the ActionListener class for the ADD button is generated
****/
void makeAddListener (FILE * printAdd, char * fileName)
{
    char * nameOfTable;
    char * stringHolderOne;
    char * stringHolderTwo;
    char * typeHolder;
    int i;
    int nameLength;

    /*the name of the file is converted to all lowercase to suit table intergration and for accurate comparison of database data*/
    nameOfTable = strdup (fileName);
    nameLength = strlen (nameOfTable);
    for (i = 0; i < nameLength; i ++)
    {
        nameOfTable[i] = (char) tolower ((int) (nameOfTable[i]));
    }

    fprintf (printAdd, "import java.awt.event.ActionEvent;\n");
    fprintf (printAdd, "import java.awt.event.ActionListener;\n");
    fprintf (printAdd, "import java.sql.*;\n");
    fprintf (printAdd, "import java.util.ArrayList;\n");
    fprintf (printAdd, "\n");

    fprintf (printAdd, "public class DialogcAdd implements ActionListener\n");
    fprintf (printAdd, "{\n");
    fprintf (printAdd, "    private %sFieldEdit dialogInterface;\n", fileName);
    fprintf (printAdd, "\n");
    fprintf (printAdd, "    public DialogcAdd (%sFieldEdit theInterface)\n", fileName);
    fprintf (printAdd, "    {\n");
    fprintf (printAdd, "        dialogInterface = theInterface;\n");
    fprintf (printAdd, "    }\n");
    fprintf (printAdd, "\n");

    /*the database addition takes place here*/
    fprintf (printAdd, "    @Override\n");
    fprintf (printAdd, "    public void actionPerformed (ActionEvent event)\n");
    fprintf (printAdd, "    {\n");
    fprintf (printAdd, "        dialogInterface.appendToStatusArea (\"ADD\");\n");
    fprintf (printAdd, "        dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printAdd, "\n");
    fprintf (printAdd, "        Connection addConnection = dialogInterface.getDBConnection ();\n");
    fprintf (printAdd, "        Statement psqlStatement = null;\n");
    fprintf (printAdd, "\n");
    fprintf (printAdd, "        try\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            DatabaseMetaData dbMetaData = addConnection.getMetaData ();\n");
    fprintf (printAdd, "            ResultSet tableCheck = dbMetaData.getTables (null, null, \"%s\", null);\n", nameOfTable);    /*the database is checked for any table of the same name*/
    fprintf (printAdd, "            if (tableCheck.next () == false)\n");    /*if there is no table by that name, it is made*/
    fprintf (printAdd, "            {\n");
    fprintf (printAdd, "                dialogInterface.appendToStatusArea (\"Database table not found. Creating table...\\n\");\n");
    fprintf (printAdd, "\n");
    fprintf (printAdd, "                String createString = \"CREATE TABLE %s ( ID SERIAL", nameOfTable);
    for (i = 0; i < numOfFields; i ++)    /*each field is represented in the SQL command CREATE TABLE, where integers are INTEGERS, floats are DECIMAL(20,3) and strings are VARCHAR(30)*/
    {
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);;
        typeHolder = NULL;
        if (strcmp (stringHolderTwo, "integer") == 0)
        {
            typeHolder = "INTEGER";
        }
        else if (strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = "DECIMAL(20,3)";
        }
        else
        {
            typeHolder = "VARCHAR(30)";
        }

        fprintf (printAdd, " , %s %s", stringHolderOne, typeHolder);
    }
    fprintf (printAdd, " , PRIMARY KEY (ID))\";\n");

    fprintf (printAdd, "                try\n");
    fprintf (printAdd, "                {\n");
    fprintf (printAdd, "                    psqlStatement = addConnection.createStatement ();\n");
    fprintf (printAdd, "                    psqlStatement.executeUpdate (createString);\n");
    fprintf (printAdd, "                    psqlStatement.close ();\n");
    fprintf (printAdd, "                }\n");
    fprintf (printAdd, "                catch (SQLException exception)\n");
    fprintf (printAdd, "                {\n");
    fprintf (printAdd, "                    dialogInterface.appendToStatusArea (\"Table unable to be created. Please try again later\\n\\n\");\n");
    fprintf (printAdd, "                    return;\n");
    fprintf (printAdd, "                }\n");
    fprintf (printAdd, "\n");
    fprintf (printAdd, "                dialogInterface.appendToStatusArea (\"Table created successfully\\n\\n\");\n");
    fprintf (printAdd, "            }\n");
    fprintf (printAdd, "            else\n");    /*the existing table is checked to see if it matches the same details as the generated GUI*/
    fprintf (printAdd, "            {\n");
    fprintf (printAdd, "                boolean IDCheck = false;\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printAdd, "                boolean %sCheck = false;\n", stringHolderOne);
    }
    fprintf (printAdd, "\n");
    fprintf (printAdd, "                ResultSet columnCheck = dbMetaData.getColumns (null, null, \"%s\", \"%%\");\n", nameOfTable);
    fprintf (printAdd, "                while (columnCheck.next () != false)\n");
    fprintf (printAdd, "                {\n");
    fprintf (printAdd, "                    String columnName = columnCheck.getString (4);\n");
    fprintf (printAdd, "                    int columnType = columnCheck.getInt (5);\n");
    fprintf (printAdd, "\n");
    fprintf (printAdd, "                    if (columnName.equalsIgnoreCase (\"ID\") && columnType == java.sql.Types.INTEGER)\n");    /*comparisons are made here, matching GUI details to table details if a match*/
    fprintf (printAdd, "                    {\n");
    fprintf (printAdd, "                        IDCheck = true;\n");
    fprintf (printAdd, "                    }\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);;
        typeHolder = NULL;
        if (strcmp (stringHolderTwo, "integer") == 0)
        {
            typeHolder = "INTEGER";
        }
        else if (strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = "NUMERIC";
        }
        else
        {
            typeHolder = "VARCHAR";
        }

        fprintf (printAdd, "                    else if (columnName.equalsIgnoreCase (\"%s\") && columnType == java.sql.Types.%s)\n", stringHolderOne, typeHolder);
        fprintf (printAdd, "                    {\n");
        fprintf (printAdd, "                        %sCheck = true;\n", stringHolderOne);
        fprintf (printAdd, "                    }\n");
    }
    fprintf (printAdd, "                    else\n");
    fprintf (printAdd, "                    {\n");
    fprintf (printAdd, "                        dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");\n");
    fprintf (printAdd, "                        return;\n");
    fprintf (printAdd, "                    }\n");
    fprintf (printAdd, "                }\n");
    fprintf (printAdd, "\n");
    fprintf (printAdd, "                if (IDCheck == false");
    for (i = 0; i < numOfFields - 1; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printAdd, " || %sCheck == false", stringHolderOne);
    }
    stringHolderOne = fieldsList[i];
    fprintf (printAdd, " || %sCheck == false)\n", stringHolderOne);    /*if all details match, it is assumed that this table belongs to this GUI*/
    fprintf (printAdd, "                {\n");
    fprintf (printAdd, "                    dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");\n");
    fprintf (printAdd, "                    return;\n");
    fprintf (printAdd, "                }\n");
    fprintf (printAdd, "            }\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "        catch (SQLException exception)\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            dialogInterface.appendToStatusArea (\"Unable to find table. Please try again later\\n\\n\");\n");
    fprintf (printAdd, "            return;\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "\n");

    /*each field is read and checked if it fits the add functionality*/
    fprintf (printAdd, "        boolean correctInput = true;\n");
    fprintf (printAdd, "        ArrayList<String> fieldNames = new ArrayList<String> (10);\n");
    fprintf (printAdd, "        ArrayList<String> fieldValues = new ArrayList<String> (10);\n");
    fprintf (printAdd, "        String fieldInput = null;\n");
    fprintf (printAdd, "\n");
    fprintf (printAdd, "        try\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            fieldInput = dialogInterface.getDCID ();\n");
    fprintf (printAdd, "            dialogInterface.appendToStatusArea (\"Error - ID field not blank\\n\");\n");
    fprintf (printAdd, "            correctInput = false;\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "        catch (IllegalFieldValueException exception)\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            if (exception.getMessage ().trim ().equals (\"\"))\n");
    fprintf (printAdd, "            {\n");
    fprintf (printAdd, "                correctInput = true;\n");
    fprintf (printAdd, "            }\n");
    fprintf (printAdd, "            else\n");
    fprintf (printAdd, "            {\n");
    fprintf (printAdd, "                dialogInterface.appendToStatusArea (\"Error - ID field not blank\\n\");\n");
    fprintf (printAdd, "                correctInput = false;\n");
    fprintf (printAdd, "            }\n");
    fprintf (printAdd, "        }\n");
    for (i = 0; i < numOfFields; i ++)
    {
        fprintf (printAdd, "\n");
        stringHolderOne = fieldsList [i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);
        if (strcmp (stringHolderTwo, "integer") == 0 || strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = NULL;
            if (strcmp (stringHolderTwo, "integer") == 0)
            {
                typeHolder = "an integer";
            }
            else
            {
                typeHolder = "a float";
            }

            fprintf (printAdd, "        try\n");
            fprintf (printAdd, "        {\n");
            fprintf (printAdd, "            fieldInput = dialogInterface.getDC%s ();\n", stringHolderOne);
            fprintf (printAdd, "            fieldNames.add (\"%s\");\n", stringHolderOne);
            fprintf (printAdd, "            fieldValues.add (fieldInput);\n");
            fprintf (printAdd, "        }\n");
            fprintf (printAdd, "        catch (IllegalFieldValueException exception)\n");
            fprintf (printAdd, "        {\n");
            fprintf (printAdd, "            dialogInterface.appendToStatusArea (\"Error - %s field not %s\\n\");\n", stringHolderOne, typeHolder);
            fprintf (printAdd, "            correctInput = false;\n");
            fprintf (printAdd, "        }\n");
        }
        else
        {
            fprintf (printAdd, "        try\n");
            fprintf (printAdd, "        {\n");
            fprintf (printAdd, "            fieldInput = dialogInterface.getDC%s ();\n", stringHolderOne);
            fprintf (printAdd, "            if (fieldInput.trim ().equals (\"\"))\n");
            fprintf (printAdd, "            {\n");
            fprintf (printAdd, "                dialogInterface.appendToStatusArea (\"Error - %s field is blank\\n\");\n", stringHolderOne);
            fprintf (printAdd, "                correctInput = false;\n");
            fprintf (printAdd, "            }\n");
            fprintf (printAdd, "            else\n");
            fprintf (printAdd, "            {\n");
            fprintf (printAdd, "                fieldNames.add (\"%s\");\n", stringHolderOne);
            fprintf (printAdd, "                fieldValues.add (fieldInput);\n");
            fprintf (printAdd, "            }\n");
            fprintf (printAdd, "        }\n");
            fprintf (printAdd, "        catch (IllegalFieldValueException exception)\n");
            fprintf (printAdd, "        {\n");
            fprintf (printAdd, "            dialogInterface.appendToStatusArea (\"Error - %s field not a string\\n\");\n", stringHolderOne);
            fprintf (printAdd, "            correctInput = false;\n");
            fprintf (printAdd, "        }\n");
        }
    }
    fprintf (printAdd, "\n");
    fprintf (printAdd, "        if (correctInput == false)\n");    /*if any field does not match the prerequisites, the listener returns without further action*/
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printAdd, "            return;\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "\n");

    /*an insert is attempted with the given info*/
    fprintf (printAdd, "        int listLength = fieldNames.size ();\n");
    fprintf (printAdd, "        String insertString = \"INSERT INTO %s (\";\n", nameOfTable);
    fprintf (printAdd, "        for (int i = 0; i < listLength - 1; i ++)\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            insertString = insertString + fieldNames.get (i) + \", \";\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "        insertString = insertString + fieldNames.get (listLength - 1) + \") VALUES (\";\n");
    fprintf (printAdd, "        for (int i = 0; i < listLength - 1; i ++)\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            insertString = insertString + \"'\" + fieldValues.get (i) + \"', \";\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "        insertString = insertString + fieldValues.get (listLength - 1) + \")\";\n");
    fprintf (printAdd, "\n");fprintf (printAdd, "System.out.println (insertString);\n");
    fprintf (printAdd, "        try\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            psqlStatement = addConnection.createStatement ();\n");
    fprintf (printAdd, "            psqlStatement.executeUpdate (insertString);\n");
    fprintf (printAdd, "            psqlStatement.close ();\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "        catch (SQLException exception)\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            dialogInterface.appendToStatusArea (\"Insert failed\\n\\n\");\n");
    fprintf (printAdd, "            return;\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "        dialogInterface.appendToStatusArea (\"Insert successful\\n\\n\");\n");
    fprintf (printAdd, "\n");

    /*if the insert is successful, the query will occur to obtain the assigned id*/
    fprintf (printAdd, "        String checkInsert = \"SELECT ID FROM %s WHERE \";\n", nameOfTable);
    fprintf (printAdd, "        for (int i = 0; i < listLength - 1; i ++)\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            checkInsert = checkInsert + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"' AND \";\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "        checkInsert = checkInsert + fieldNames.get (listLength - 1) + \" = '\" + fieldValues.get (listLength - 1) + \"'\";\n");
    fprintf (printAdd, "        try\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            psqlStatement = addConnection.createStatement ();\n");
    fprintf (printAdd, "            ResultSet newResult = psqlStatement.executeQuery (checkInsert);\n");
    fprintf (printAdd, "            while (newResult.next () != false)\n");
    fprintf (printAdd, "            {\n");
    fprintf (printAdd, "                dialogInterface.setDCID (newResult.getString (1));\n");
    fprintf (printAdd, "            }\n");
    fprintf (printAdd, "            psqlStatement.close ();\n");
    fprintf (printAdd, "            newResult.close ();\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "        catch (SQLException exception)\n");
    fprintf (printAdd, "        {\n");
    fprintf (printAdd, "            dialogInterface.appendToStatusArea (\"Result query failed\\n\\n\");\n");
    fprintf (printAdd, "        }\n");
    fprintf (printAdd, "    }\n");
    fprintf (printAdd, "}\n");

    free (nameOfTable);

    return;
}

/****
Preconditions: The .config file has been successfully parsed through by the yadc compiler and the DELETE button has been declared
Postconditions: The .java file containing the ActionListener class for the DELETE button is generated
****/
void makeDeleteListener (FILE * printDelete, char * fileName)
{
    char * nameOfTable;
    char * stringHolderOne;
    char * stringHolderTwo;
    char * typeHolder;
    int i;
    int nameLength;

    /*the name of the file is converted to all lowercase to suit table intergration and for accurate comparison of database data*/
    nameOfTable = strdup (fileName);
    nameLength = strlen (nameOfTable);
    for (i = 0; i < nameLength; i ++)
    {
        nameOfTable[i] = (char) tolower ((int) (nameOfTable[i]));
    }

    fprintf (printDelete, "import java.awt.*;\n");
    fprintf (printDelete, "import javax.swing.*;\n");
    fprintf (printDelete, "import java.awt.event.ActionEvent;\n");
    fprintf (printDelete, "import java.awt.event.ActionListener;\n");
    fprintf (printDelete, "import java.sql.*;\n");
    fprintf (printDelete, "import java.util.ArrayList;\n");
    fprintf (printDelete, "\n");

    fprintf (printDelete, "public class DialogcDelete implements ActionListener\n");
    fprintf (printDelete, "{\n");
    fprintf (printDelete, "    private %sFieldEdit dialogInterface;\n", fileName);
    fprintf (printDelete, "\n");
    fprintf (printDelete, "    public DialogcDelete (%sFieldEdit theInterface)\n", fileName);
    fprintf (printDelete, "    {\n");
    fprintf (printDelete, "        dialogInterface = theInterface;\n");
    fprintf (printDelete, "    }\n");
    fprintf (printDelete, "\n");

    /*the database deletion takes place here*/
    fprintf (printDelete, "    @Override\n");
    fprintf (printDelete, "    public void actionPerformed (ActionEvent event)\n");
    fprintf (printDelete, "    {\n");
    fprintf (printDelete, "        dialogInterface.appendToStatusArea (\"DELETE\");\n");
    fprintf (printDelete, "        dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "        Connection deleteConnection = dialogInterface.getDBConnection ();\n");
    fprintf (printDelete, "        Statement psqlStatement = null;\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "        try\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            DatabaseMetaData dbMetaData = deleteConnection.getMetaData ();\n");
    fprintf (printDelete, "            ResultSet tableCheck = dbMetaData.getTables (null, null, \"%s\", null);\n", nameOfTable);    /*the database is checked for any table of the same name*/
    fprintf (printDelete, "            if (tableCheck.next () == false)\n");    /*if there is no table by that name, it is made*/
    fprintf (printDelete, "            {\n");
    fprintf (printDelete, "                dialogInterface.appendToStatusArea (\"Database table not found. Creating table...\\n\");\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "                String createString = \"CREATE TABLE %s ( ID SERIAL", nameOfTable);
    for (i = 0; i < numOfFields; i ++)    /*each field is represented in the SQL command CREATE TABLE, where integers are INTEGERS, floats are DECIMAL(20,3) and strings are VARCHAR(30)*/
    {
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);;
        typeHolder = NULL;
        if (strcmp (stringHolderTwo, "integer") == 0)
        {
            typeHolder = "INTEGER";
        }
        else if (strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = "DECIMAL(20,3)";
        }
        else
        {
            typeHolder = "VARCHAR(30)";
        }

        fprintf (printDelete, " , %s %s", stringHolderOne, typeHolder);
    }
    fprintf (printDelete, " , PRIMARY KEY (ID))\";\n");

    fprintf (printDelete, "                try\n");
    fprintf (printDelete, "                {\n");
    fprintf (printDelete, "                    psqlStatement = deleteConnection.createStatement ();\n");
    fprintf (printDelete, "                    psqlStatement.executeUpdate (createString);\n");
    fprintf (printDelete, "                    psqlStatement.close ();\n");
    fprintf (printDelete, "                }\n");
    fprintf (printDelete, "                catch (SQLException exception)\n");
    fprintf (printDelete, "                {\n");
    fprintf (printDelete, "                    dialogInterface.appendToStatusArea (\"Table unable to be created. Please try again later\\n\\n\");\n");
    fprintf (printDelete, "                    return;\n");
    fprintf (printDelete, "                }\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "                dialogInterface.appendToStatusArea (\"Table created successfully\\n\\n\");\n");
    fprintf (printDelete, "            }\n");
    fprintf (printDelete, "            else\n");    /*the existing table is checked to see if it matches the same details as the generated GUI*/
    fprintf (printDelete, "            {\n");
    fprintf (printDelete, "                boolean IDCheck = false;\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printDelete, "                boolean %sCheck = false;\n", stringHolderOne);
    }
    fprintf (printDelete, "\n");
    fprintf (printDelete, "                ResultSet columnCheck = dbMetaData.getColumns (null, null, \"%s\", \"%%\");\n", nameOfTable);
    fprintf (printDelete, "                while (columnCheck.next () != false)\n");
    fprintf (printDelete, "                {\n");
    fprintf (printDelete, "                    String columnName = columnCheck.getString (4);\n");
    fprintf (printDelete, "                    int columnType = columnCheck.getInt (5);\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "                    if (columnName.equalsIgnoreCase (\"ID\") && columnType == java.sql.Types.INTEGER)\n");    /*comparisons are made here, matching GUI details to table details if a match*/
    fprintf (printDelete, "                    {\n");
    fprintf (printDelete, "                        IDCheck = true;\n");
    fprintf (printDelete, "                    }\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);;
        typeHolder = NULL;
        if (strcmp (stringHolderTwo, "integer") == 0)
        {
            typeHolder = "INTEGER";
        }
        else if (strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = "NUMERIC";
        }
        else
        {
            typeHolder = "VARCHAR";
        }

        fprintf (printDelete, "                    else if (columnName.equalsIgnoreCase (\"%s\") && columnType == java.sql.Types.%s)\n", stringHolderOne, typeHolder);
        fprintf (printDelete, "                    {\n");
        fprintf (printDelete, "                        %sCheck = true;\n", stringHolderOne);
        fprintf (printDelete, "                    }\n");
    }
    fprintf (printDelete, "                    else\n");
    fprintf (printDelete, "                    {\n");
    fprintf (printDelete, "                        dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");\n");
    fprintf (printDelete, "                        return;\n");
    fprintf (printDelete, "                    }\n");
    fprintf (printDelete, "                }\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "                if (IDCheck == false");
    for (i = 0; i < numOfFields - 1; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printDelete, " || %sCheck == false", stringHolderOne);
    }
    stringHolderOne = fieldsList[i];
    fprintf (printDelete, " || %sCheck == false)\n", stringHolderOne);    /*if all details match, it is assumed that this table belongs to this GUI*/
    fprintf (printDelete, "                {\n");
    fprintf (printDelete, "                    dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");\n");
    fprintf (printDelete, "                    return;\n");
    fprintf (printDelete, "                }\n");
    fprintf (printDelete, "            }\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "        catch (SQLException exception)\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            dialogInterface.appendToStatusArea (\"Unable to find table. Please try again later\\n\\n\");\n");
    fprintf (printDelete, "            return;\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "\n");

    /*each field is read and checked to see it it fits the delete functionality*/
    fprintf (printDelete, "        boolean correctInput = true;\n");
    fprintf (printDelete, "        ArrayList<String> fieldNames = new ArrayList<String> (10);\n");
    fprintf (printDelete, "        ArrayList<String> fieldValues = new ArrayList<String> (10);\n");
    fprintf (printDelete, "        String fieldInput = null;\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "        try\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            fieldInput = dialogInterface.getDCID ();\n");
    fprintf (printDelete, "            fieldNames.add (\"ID\");\n");
    fprintf (printDelete, "            fieldValues.add (fieldInput);\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "        catch (IllegalFieldValueException exception)\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            if (!exception.getMessage ().trim ().equals (\"\"))\n");
    fprintf (printDelete, "            {\n");
    fprintf (printDelete, "                dialogInterface.appendToStatusArea (\"Error - ID field not an integer\\n\");\n");
    fprintf (printDelete, "                correctInput = false;\n");
    fprintf (printDelete, "            }\n");
    fprintf (printDelete, "        }\n");
    for (i = 0; i < numOfFields; i ++)
    {
        fprintf (printDelete, "\n");
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);
        if (strcmp (stringHolderTwo, "integer") == 0 || strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = NULL;
            if (strcmp (stringHolderTwo, "integer") == 0)
            {
                typeHolder = "an integer";
            }
            else
            {
                typeHolder = "a float";
            }

            fprintf (printDelete, "        try\n");
            fprintf (printDelete, "        {\n");
            fprintf (printDelete, "            fieldInput = dialogInterface.getDC%s ();\n", stringHolderOne);
            fprintf (printDelete, "            fieldNames.add (\"%s\");\n", stringHolderOne);
            fprintf (printDelete, "            fieldValues.add (fieldInput);\n");
            fprintf (printDelete, "        }\n");
            fprintf (printDelete, "        catch (IllegalFieldValueException exception)\n");
            fprintf (printDelete, "        {\n");
            fprintf (printDelete, "            if (!exception.getMessage ().trim ().equals (\"\"))\n");
            fprintf (printDelete, "            {\n");
            fprintf (printDelete, "                dialogInterface.appendToStatusArea (\"Error - %s field not %s\\n\");\n", stringHolderOne, typeHolder);
            fprintf (printDelete, "                correctInput = false;\n");
            fprintf (printDelete, "            }\n");
            fprintf (printDelete, "        }\n");
        }
        else
        {
            fprintf (printDelete, "        try\n");
            fprintf (printDelete, "        {\n");
            fprintf (printDelete, "            fieldInput = dialogInterface.getDC%s ();\n", stringHolderOne);
            fprintf (printDelete, "            if (!fieldInput.trim ().equals (\"\"))\n");
            fprintf (printDelete, "            {\n");
            fprintf (printDelete, "                fieldNames.add (\"%s\");\n", stringHolderOne);
            fprintf (printDelete, "                fieldValues.add (fieldInput);\n");
            fprintf (printDelete, "            }\n");
            fprintf (printDelete, "        }\n");
            fprintf (printDelete, "        catch (IllegalFieldValueException exception)\n");
            fprintf (printDelete, "        {\n");
            fprintf (printDelete, "            dialogInterface.appendToStatusArea (\"Error - %s field not a string\\n\");\n", stringHolderOne);
            fprintf (printDelete, "            correctInput = false;\n");
            fprintf (printDelete, "        }\n");
        }
    }
    fprintf (printDelete, "\n");
    fprintf (printDelete, "        if (correctInput == false)\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printDelete, "            return;\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "\n");

    /*the database is queried to see it there are any matching records that match the user's input*/
    fprintf (printDelete, "        String queryString = \"SELECT * FROM %s\";\n", nameOfTable);
    fprintf (printDelete, "        int listLength = fieldNames.size ();\n");
    fprintf (printDelete, "        if (listLength > 0)\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            queryString = queryString + \" WHERE \";\n");
    fprintf (printDelete, "            for (int i = 0; i < listLength - 1; i ++)\n");
    fprintf (printDelete, "            {\n");
    fprintf (printDelete, "                queryString = queryString + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"' AND \";\n");
    fprintf (printDelete, "            }\n");
    fprintf (printDelete, "            queryString = queryString + fieldNames.get (listLength - 1) + \" = '\" + fieldValues.get (listLength - 1) + \"'\";\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "        try\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            psqlStatement = deleteConnection.createStatement (ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);\n");
    fprintf (printDelete, "            ResultSet results = psqlStatement.executeQuery (queryString);\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            if (results.next () == true)\n");
    fprintf (printDelete, "            {\n");
    fprintf (printDelete, "                DeleteConfirm newDelete = new DeleteConfirm (deleteConnection, results, fieldNames, fieldValues);\n");
    fprintf (printDelete, "                newDelete.setVisible (true);\n");
    fprintf (printDelete, "            }\n");
    fprintf (printDelete, "            else\n");
    fprintf (printDelete, "            {\n");
    fprintf (printDelete, "                dialogInterface.appendToStatusArea (\"0 results\\n\\n\");\n");
    fprintf (printDelete, "            }\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            psqlStatement.close ();\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "        catch (SQLException exception)\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            dialogInterface.appendToStatusArea (\"Query to delete failed\\n\\n\");\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "    }\n");
    fprintf (printDelete, "\n");

    /*a sub-class is printed to handle the confirming of deleting records through a separate frame*/
    fprintf (printDelete, "    private class DeleteConfirm extends JFrame\n");
    fprintf (printDelete, "    {\n");
    fprintf (printDelete, "        private Connection deleteConnection;\n");
    fprintf (printDelete, "        private ArrayList<String> fieldNames;\n");
    fprintf (printDelete, "        private ArrayList<String> fieldValues;\n");
    fprintf (printDelete, "        private int deleteNum;\n");
    fprintf (printDelete, "        private int i;\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "        public DeleteConfirm (Connection deleteConnection, ResultSet results, ArrayList<String> fieldNames, ArrayList<String> fieldValues)\n");    /*the frame is set up and stores necessary variables for later use*/
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            super ();\n");
    fprintf (printDelete, "            setSize (450, 500);\n");
    fprintf (printDelete, "            setDefaultCloseOperation (JFrame.DO_NOTHING_ON_CLOSE);\n");
    fprintf (printDelete, "            setTitle (\"Confirm Delete\");\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            this.deleteConnection = deleteConnection;\n");
    fprintf (printDelete, "            this.fieldNames = fieldNames;\n");
    fprintf (printDelete, "            this.fieldValues = fieldValues;\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            setLayout (new BorderLayout ());\n");
    fprintf (printDelete, "            add (setDeleteArea (results), BorderLayout.NORTH);\n");
    fprintf (printDelete, "            add (setButtonArea (), BorderLayout.SOUTH);\n");
    fprintf (printDelete, "            pack ();\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "\n");

    /*the main area where the user can see about-to-be-deleted records is set up here*/
    fprintf (printDelete, "        private JPanel setDeleteArea (ResultSet deleteResults)\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            JPanel mainPanel = new JPanel ();\n");
    fprintf (printDelete, "            mainPanel.setLayout (new BorderLayout ());\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            JLabel deleteLabel = new JLabel (\"Will you delete these records?\");\n");
    fprintf (printDelete, "            deleteLabel.setHorizontalAlignment (SwingConstants.LEFT);\n");
    fprintf (printDelete, "            mainPanel.add (deleteLabel, BorderLayout.NORTH);\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            JTextArea deleteArea = new JTextArea (10, 40);\n");
    fprintf (printDelete, "            deleteArea.setBorder (BorderFactory.createLineBorder (Color.BLACK));\n");
    fprintf (printDelete, "            deleteArea.setEditable (false);\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            try\n");
    fprintf (printDelete, "            {\n");
    fprintf (printDelete, "                deleteResults.beforeFirst ();\n");
    fprintf (printDelete, "                deleteNum = 0;\n");
    fprintf (printDelete, "                while (deleteResults.next () != false)\n");
    fprintf (printDelete, "                {\n");
    fprintf (printDelete, "                    deleteNum ++;\n");
    fprintf (printDelete, "                    String ID = deleteResults.getString (\"ID\");\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printDelete, "                    String %s =  deleteResults.getString (\"%s\");\n", stringHolderOne, stringHolderOne);
    }
    fprintf (printDelete, "                    deleteArea.append (\"ID = \" + ID + \"\\n\"");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printDelete, " + \"%s = \" + %s + \"\\n\"", stringHolderOne, stringHolderOne);
    }
    fprintf (printDelete, ");\n");
    fprintf (printDelete, "                    deleteArea.append (\"\\n\");\n");
    fprintf (printDelete, "                }\n");
    fprintf (printDelete, "                deleteArea.append (deleteNum + \" result(s)\\n\");\n");
    fprintf (printDelete, "                deleteResults.close ();\n");
    fprintf (printDelete, "            }\n");
    fprintf (printDelete, "            catch (SQLException exception)\n");
    fprintf (printDelete, "            {\n");
    fprintf (printDelete, "                deleteArea.append (\"Error - Unable to read delete results\\n\");\n");
    fprintf (printDelete, "            }\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            JScrollPane deleteScroll = new JScrollPane (deleteArea);\n");
    fprintf (printDelete, "            deleteScroll.setHorizontalScrollBarPolicy (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);\n");
    fprintf (printDelete, "            deleteScroll.setVerticalScrollBarPolicy (JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);\n");
    fprintf (printDelete, "            mainPanel.add (deleteScroll, BorderLayout.CENTER);\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            return (mainPanel);\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "\n");

    /*the yes and no buttons for the delete confirm are set up here*/
    fprintf (printDelete, "        private JPanel setButtonArea ()\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            JPanel buttonsPanel = new JPanel ();\n");
    fprintf (printDelete, "            buttonsPanel.setLayout (new FlowLayout (FlowLayout.CENTER));\n");
    fprintf (printDelete, "            JButton buttonYes = new JButton (\"Yes\");\n");
    fprintf (printDelete, "            buttonYes.addActionListener (new DeleteListener ());\n");
    fprintf (printDelete, "            buttonsPanel.add (buttonYes);\n");
    fprintf (printDelete, "            JButton buttonNo = new JButton (\"No\");\n");
    fprintf (printDelete, "            buttonNo.addActionListener (new DeleteListener ());\n");
    fprintf (printDelete, "            buttonsPanel.add (buttonNo);\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "            return (buttonsPanel);\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "\n");

    /*the delete listener will take in a 'yes' or 'no' and delete or not correspondingly*/
    fprintf (printDelete, "        private class DeleteListener implements ActionListener\n");
    fprintf (printDelete, "        {\n");
    fprintf (printDelete, "            @Override\n");
    fprintf (printDelete, "            public void actionPerformed (ActionEvent event)\n");
    fprintf (printDelete, "            {\n");
    fprintf (printDelete, "                String deleteCommand = event.getActionCommand ();\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "                if (deleteCommand.equals (\"Yes\"))\n");
    fprintf (printDelete, "                {\n");
    fprintf (printDelete, "                    Statement psqlStatement = null;\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "                    String deleteString = \"DELETE FROM %s\";\n", nameOfTable);    /*the delete will take place here using the SQL DELETE command using the given info*/
    fprintf (printDelete, "                    int listLength = fieldNames.size ();\n");
    fprintf (printDelete, "                    if (listLength > 0)\n");
    fprintf (printDelete, "                    {\n");
    fprintf (printDelete, "                        deleteString = deleteString + \" WHERE \";\n");
    fprintf (printDelete, "                        for (int i = 0; i < listLength - 1; i ++)\n");
    fprintf (printDelete, "                        {\n");
    fprintf (printDelete, "                            deleteString = deleteString + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"' AND \";\n");
    fprintf (printDelete, "                        }\n");
    fprintf (printDelete, "                        deleteString = deleteString + fieldNames.get (listLength - 1) + \" = '\" + fieldValues.get (listLength - 1) + \"'\";\n");
    fprintf (printDelete, "                    }\n");
    fprintf (printDelete, "                    try\n");
    fprintf (printDelete, "                    {\n");
    fprintf (printDelete, "                        psqlStatement = deleteConnection.createStatement ();\n");
    fprintf (printDelete, "                        psqlStatement.executeUpdate (deleteString);\n");
    fprintf (printDelete, "                        psqlStatement.close ();\n");
    fprintf (printDelete, "                    }\n");
    fprintf (printDelete, "                    catch (SQLException exception)\n");
    fprintf (printDelete, "                    {\n");
    fprintf (printDelete, "                        dialogInterface.appendToStatusArea (\"Delete failed\\n\\n\");\n");
    fprintf (printDelete, "                    }\n");
    fprintf (printDelete, "                    dialogInterface.appendToStatusArea (\"Delete successful\\n\");\n");
    fprintf (printDelete, "                    dialogInterface.appendToStatusArea (deleteNum + \" record(s) deleted\\n\\n\");\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "                    dispose ();\n");
    fprintf (printDelete, "                }\n");
    fprintf (printDelete, "                else if (deleteCommand.equals (\"No\"))\n");
    fprintf (printDelete, "                {\n");
    fprintf (printDelete, "                    dispose ();\n");    /*choosing no will simply dispose the window*/
    fprintf (printDelete, "                    dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printDelete, "                }\n");
    fprintf (printDelete, "                else\n");
    fprintf (printDelete, "                {\n");
    fprintf (printDelete, "                    System.err.println (\"Unexpected Confirm Delete Window Logic Error\");\n");
    fprintf (printDelete, "                    return;\n");
    fprintf (printDelete, "                }\n");
    fprintf (printDelete, "\n");
    fprintf (printDelete, "                validate ();\n");
    fprintf (printDelete, "            }\n");
    fprintf (printDelete, "        }\n");
    fprintf (printDelete, "    }\n");
    fprintf (printDelete, "}\n");

    free (nameOfTable);

    return;
}

/****
Preconditions: The .config file has been successfully parsed through by the yadc compiler and the UPDATE button has been declared
Postconditions: The .java file containing the ActionListener class for the UPDATE button is generated
****/
void makeUpdateListener (FILE * printUpdate, char * fileName)
{
    char * nameOfTable;
    char * stringHolderOne;
    char * stringHolderTwo;
    char * typeHolder;
    int i;
    int nameLength;

    /*the name of the file is converted to all lowercase to suit table intergration and for accurate comparison of database data*/
    nameOfTable = strdup (fileName);
    nameLength = strlen (nameOfTable);
    for (i = 0; i < nameLength; i ++)
    {
        nameOfTable[i] = (char) tolower ((int) (nameOfTable[i]));
    }

    fprintf (printUpdate, "import java.awt.event.ActionEvent;\n");
    fprintf (printUpdate, "import java.awt.event.ActionListener;\n");
    fprintf (printUpdate, "import java.sql.*;\n");
    fprintf (printUpdate, "import java.util.ArrayList;\n");
    fprintf (printUpdate, "\n");

    fprintf (printUpdate, "public class DialogcUpdate implements ActionListener\n");
    fprintf (printUpdate, "{\n");
    fprintf (printUpdate, "    private %sFieldEdit dialogInterface;\n", fileName);
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "    public DialogcUpdate (%sFieldEdit theInterface)\n", fileName);
    fprintf (printUpdate, "    {\n");
    fprintf (printUpdate, "        dialogInterface = theInterface;\n");
    fprintf (printUpdate, "    }\n");
    fprintf (printUpdate, "\n");

    /*the database update takes place here*/    
    fprintf (printUpdate, "    @Override\n");
    fprintf (printUpdate, "    public void actionPerformed (ActionEvent event)\n");
    fprintf (printUpdate, "    {\n");
    fprintf (printUpdate, "        dialogInterface.appendToStatusArea (\"UPDATE\");\n");
    fprintf (printUpdate, "        dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "        Connection updateConnection = dialogInterface.getDBConnection ();\n");
    fprintf (printUpdate, "        Statement psqlStatement = null;\n");
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "        try\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            DatabaseMetaData dbMetaData = updateConnection.getMetaData ();\n");
    fprintf (printUpdate, "            ResultSet tableCheck = dbMetaData.getTables (null, null, \"%s\", null);\n", nameOfTable);    /*the database is checked for any table of the same name*/
    fprintf (printUpdate, "            if (tableCheck.next () == false)\n");    /*if there is no table by that name, it is made*/
    fprintf (printUpdate, "            {\n");
    fprintf (printUpdate, "                dialogInterface.appendToStatusArea (\"Database table not found. Creating table...\\n\");\n");
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "                String createString = \"CREATE TABLE %s ( ID SERIAL", nameOfTable);
    for (i = 0; i < numOfFields; i ++)    /*each field is represented in the SQL command CREATE TABLE, where integers are INTEGERS, floats are DECIMAL(20,3) and strings are VARCHAR(30)*/
    {
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);;
        typeHolder = NULL;
        if (strcmp (stringHolderTwo, "integer") == 0)
        {
            typeHolder = "INTEGER";
        }
        else if (strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = "DECIMAL(20,3)";
        }
        else
        {
            typeHolder = "VARCHAR(30)";
        }

        fprintf (printUpdate, " , %s %s", stringHolderOne, typeHolder);
    }
    fprintf (printUpdate, " , PRIMARY KEY (ID))\";\n");

    fprintf (printUpdate, "                try\n");
    fprintf (printUpdate, "                {\n");
    fprintf (printUpdate, "                    psqlStatement = updateConnection.createStatement ();\n");
    fprintf (printUpdate, "                    psqlStatement.executeUpdate (createString);\n");
    fprintf (printUpdate, "                    psqlStatement.close ();\n");
    fprintf (printUpdate, "                }\n");
    fprintf (printUpdate, "                catch (SQLException exception)\n");
    fprintf (printUpdate, "                {\n");
    fprintf (printUpdate, "                    dialogInterface.appendToStatusArea (\"Table unable to be created. Please try again later\\n\\n\");\n");
    fprintf (printUpdate, "                    return;\n");
    fprintf (printUpdate, "                }\n");
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "                dialogInterface.appendToStatusArea (\"Table created successfully\\n\\n\");\n");
    fprintf (printUpdate, "            }\n");
    fprintf (printUpdate, "            else\n");    /*the existing table is checked to see if it matches the same details as the generated GUI*/
    fprintf (printUpdate, "            {\n");
    fprintf (printUpdate, "                boolean IDCheck = false;\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printUpdate, "                boolean %sCheck = false;\n", stringHolderOne);
    }
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "                ResultSet columnCheck = dbMetaData.getColumns (null, null, \"%s\", \"%%\");\n", nameOfTable);
    fprintf (printUpdate, "                while (columnCheck.next () != false)\n");
    fprintf (printUpdate, "                {\n");
    fprintf (printUpdate, "                    String columnName = columnCheck.getString (4);\n");
    fprintf (printUpdate, "                    int columnType = columnCheck.getInt (5);\n");
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "                    if (columnName.equalsIgnoreCase (\"ID\") && columnType == java.sql.Types.INTEGER)\n");    /*comparisons are made here, matching GUI details to table details if a match*/
    fprintf (printUpdate, "                    {\n");
    fprintf (printUpdate, "                        IDCheck = true;\n");
    fprintf (printUpdate, "                    }\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);;
        typeHolder = NULL;
        if (strcmp (stringHolderTwo, "integer") == 0)
        {
            typeHolder = "INTEGER";
        }
        else if (strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = "NUMERIC";
        }
        else
        {
            typeHolder = "VARCHAR";
        }

        fprintf (printUpdate, "                    else if (columnName.equalsIgnoreCase (\"%s\") && columnType == java.sql.Types.%s)\n", stringHolderOne, typeHolder);
        fprintf (printUpdate, "                    {\n");
        fprintf (printUpdate, "                        %sCheck = true;\n", stringHolderOne);
        fprintf (printUpdate, "                    }\n");
    }
    fprintf (printUpdate, "                    else\n");
    fprintf (printUpdate, "                    {\n");
    fprintf (printUpdate, "                        dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");\n");
    fprintf (printUpdate, "                        return;\n");
    fprintf (printUpdate, "                    }\n");
    fprintf (printUpdate, "                }\n");
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "                if (IDCheck == false");
    for (i = 0; i < numOfFields - 1; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printUpdate, " || %sCheck == false", stringHolderOne);
    }
    stringHolderOne = fieldsList[i];
    fprintf (printUpdate, " || %sCheck == false)\n", stringHolderOne);    /*if all details match, it is assumed that this table belongs to this GUI*/
    fprintf (printUpdate, "                {\n");
    fprintf (printUpdate, "                    dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");\n");
    fprintf (printUpdate, "                    return;\n");
    fprintf (printUpdate, "                }\n");
    fprintf (printUpdate, "            }\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "        catch (SQLException exception)\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            dialogInterface.appendToStatusArea (\"Unable to find table. Please try again later\\n\\n\");\n");
    fprintf (printUpdate, "            return;\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "\n");

    /*each field is read and checked to see it it fits the update functionality*/
    fprintf (printUpdate, "        boolean correctInput = true;\n");
    fprintf (printUpdate, "        boolean noUpdate = true;\n");
    fprintf (printUpdate, "        ArrayList<String> fieldNames = new ArrayList<String> (10);\n");
    fprintf (printUpdate, "        ArrayList<String> fieldValues = new ArrayList<String> (10);\n");
    fprintf (printUpdate, "        String idUpdate = null;\n");
    fprintf (printUpdate, "        String fieldInput = null;\n");
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "        try\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            idUpdate = dialogInterface.getDCID ();\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "        catch (IllegalFieldValueException exception)\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            if (exception.getMessage ().trim ().equals (\"\"))\n");
    fprintf (printUpdate, "            {\n");
    fprintf (printUpdate, "                dialogInterface.appendToStatusArea (\"Error - ID field is blank\\n\");\n");
    fprintf (printUpdate, "                correctInput = false;\n");
    fprintf (printUpdate, "            }\n");
    fprintf (printUpdate, "            else\n");
    fprintf (printUpdate, "            {\n");
    fprintf (printUpdate, "                dialogInterface.appendToStatusArea (\"Error - ID field not an integer\\n\");\n");
    fprintf (printUpdate, "                correctInput = false;\n");
    fprintf (printUpdate, "            }\n");
    fprintf (printUpdate, "        }\n");
    for (i = 0; i < numOfFields; i ++)
    {
        fprintf (printUpdate, "\n");
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);
        if (strcmp (stringHolderTwo, "integer") == 0 || strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = NULL;
            if (strcmp (stringHolderTwo, "integer") == 0)
            {
                typeHolder = "an integer";
            }
            else
            {
                typeHolder = "a float";
            }

            fprintf (printUpdate, "        try\n");
            fprintf (printUpdate, "        {\n");
            fprintf (printUpdate, "            fieldInput = dialogInterface.getDC%s ();\n", stringHolderOne);
            fprintf (printUpdate, "            fieldNames.add (\"%s\");\n", stringHolderOne);
            fprintf (printUpdate, "            fieldValues.add (fieldInput);\n");
            fprintf (printUpdate, "            noUpdate = false;\n");
            fprintf (printUpdate, "        }\n");
            fprintf (printUpdate, "        catch (IllegalFieldValueException exception)\n");
            fprintf (printUpdate, "        {\n");
            fprintf (printUpdate, "            if (!exception.getMessage ().trim ().equals (\"\"))\n");
            fprintf (printUpdate, "            {\n");
            fprintf (printUpdate, "                dialogInterface.appendToStatusArea (\"Error - %s field not %s\\n\");\n", stringHolderOne, typeHolder);
            fprintf (printUpdate, "                correctInput = false;\n");
            fprintf (printUpdate, "            }\n");
            fprintf (printUpdate, "        }\n");
        }
        else
        {
            fprintf (printUpdate, "        try\n");
            fprintf (printUpdate, "        {\n");
            fprintf (printUpdate, "            fieldInput = dialogInterface.getDC%s ();\n", stringHolderOne);
            fprintf (printUpdate, "            if (!fieldInput.trim ().equals (\"\"))\n");
            fprintf (printUpdate, "            {\n");
            fprintf (printUpdate, "                fieldNames.add (\"%s\");\n", stringHolderOne);
            fprintf (printUpdate, "                fieldValues.add (fieldInput);\n");
            fprintf (printUpdate, "                noUpdate = false;\n");
            fprintf (printUpdate, "            }\n");
            fprintf (printUpdate, "        }\n");
            fprintf (printUpdate, "        catch (IllegalFieldValueException exception)\n");
            fprintf (printUpdate, "        {\n");
            fprintf (printUpdate, "            dialogInterface.appendToStatusArea (\"Error - %s field not a string\\n\");\n", stringHolderOne);
            fprintf (printUpdate, "            correctInput = false;\n");
            fprintf (printUpdate, "        }\n");
        }
    }
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "        if (correctInput == false)\n");    /*if any field does not match the prerequisites, the listener returns without further action*/
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printUpdate, "            return;\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "        else if (noUpdate == true)\n");    /*if only an id field is given, there is no real update and the status message reflects this*/
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            dialogInterface.appendToStatusArea (\"Error - Nothing to update\\n\\n\");\n");
    fprintf (printUpdate, "            return;\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "\n");

    /*the id is checked to see if it currently exists in the database (so that only existing id's are updated)*/
    fprintf (printUpdate, "        boolean validID = false;\n");
    fprintf (printUpdate, "        String checkID = \"SELECT ID FROM %s \";\n", nameOfTable);
    fprintf (printUpdate, "        try\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            psqlStatement = updateConnection.createStatement ();\n");
    fprintf (printUpdate, "            ResultSet newResult = psqlStatement.executeQuery (checkID);\n");
    fprintf (printUpdate, "            while (newResult.next () != false)\n");
    fprintf (printUpdate, "            {\n");
    fprintf (printUpdate, "                if (idUpdate.equals (newResult.getString (1)))\n");
    fprintf (printUpdate, "                {\n");
    fprintf (printUpdate, "                    validID = true;\n");
    fprintf (printUpdate, "                }\n");
    fprintf (printUpdate, "            }\n");
    fprintf (printUpdate, "            psqlStatement.close ();\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "        catch (SQLException exception)\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            dialogInterface.appendToStatusArea (\"ID check query failed\\n\\n\");\n");
    fprintf (printUpdate, "            return;\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "        if (validID == false)\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            dialogInterface.appendToStatusArea (\"Error - Invalid ID entered\\n\\n\");\n");
    fprintf (printUpdate, "            return;\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "\n");

    /*if the id check is successful, then an SQL UPDATE command is made to update the database with the given info*/
    fprintf (printUpdate, "        int listLength = fieldNames.size ();\n");
    fprintf (printUpdate, "        String updateString = \"UPDATE %s SET \";\n", nameOfTable);
    fprintf (printUpdate, "        for (int i = 0; i < listLength - 1; i ++)\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            updateString = updateString + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"', \";\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "        updateString = updateString + fieldNames.get (listLength - 1) + \" = \" + fieldValues.get (listLength - 1) + \" WHERE ID = \" + idUpdate;\n");
    fprintf (printUpdate, "\n");
    fprintf (printUpdate, "        try\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            psqlStatement = updateConnection.createStatement ();\n");
    fprintf (printUpdate, "            psqlStatement.executeUpdate (updateString);\n");
    fprintf (printUpdate, "            psqlStatement.close ();\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "        catch (SQLException exception)\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            dialogInterface.appendToStatusArea (\"Update failed\\n\\n\");\n");
    fprintf (printUpdate, "            return;\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "        dialogInterface.appendToStatusArea (\"Update successful\\n\\n\");\n");
    fprintf (printUpdate, "\n");

    /*if the update is successful then the updated row is placed onto the GUI*/
    fprintf (printUpdate, "        String checkUpdate = \"SELECT * FROM %s WHERE ID = \" + idUpdate;\n", nameOfTable);
    fprintf (printUpdate, "        try\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            psqlStatement = updateConnection.createStatement ();\n");
    fprintf (printUpdate, "            ResultSet newResult = psqlStatement.executeQuery (checkUpdate);\n");
    fprintf (printUpdate, "            while (newResult.next () != false)\n");
    fprintf (printUpdate, "            {\n");
    fprintf (printUpdate, "                dialogInterface.setDCID (newResult.getString (\"ID\"));\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printUpdate, "                dialogInterface.setDC%s (newResult.getString (\"%s\"));\n", stringHolderOne, stringHolderOne);
    }
    fprintf (printUpdate, "            }\n");
    fprintf (printUpdate, "            psqlStatement.close ();\n");
    fprintf (printUpdate, "            newResult.close ();\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "        catch (SQLException exception)\n");
    fprintf (printUpdate, "        {\n");
    fprintf (printUpdate, "            dialogInterface.appendToStatusArea (\"Result query failed\\n\\n\");\n");
    fprintf (printUpdate, "        }\n");
    fprintf (printUpdate, "    }\n");
    fprintf (printUpdate, "}\n");

    free (nameOfTable);

    return;
}

/****
Preconditions: The .config file has been successfully parsed through by the yadc compiler and the QUERY button has been declared
Postconditions: The .java file containing the ActionListener class for the QUERY button is generated
****/
void makeQueryListener (FILE * printQuery, char * fileName)
{
    char * nameOfTable;
    char * stringHolderOne;
    char * stringHolderTwo;
    char * typeHolder;
    int i;
    int nameLength;

    /*the name of the file is converted to all lowercase to suit table intergration and for accurate comparison of database data*/
    nameOfTable = strdup (fileName);
    nameLength = strlen (nameOfTable);
    for (i = 0; i < nameLength; i ++)
    {
        nameOfTable[i] = (char) tolower ((int) (nameOfTable[i]));
    }

    fprintf (printQuery, "import java.awt.event.ActionEvent;\n");
    fprintf (printQuery, "import java.awt.event.ActionListener;\n");
    fprintf (printQuery, "import java.sql.*;\n");
    fprintf (printQuery, "import java.util.ArrayList;\n");
    fprintf (printQuery, "\n");

    fprintf (printQuery, "public class DialogcQuery implements ActionListener\n");
    fprintf (printQuery, "{\n");
    fprintf (printQuery, "    private %sFieldEdit dialogInterface;\n", fileName);
    fprintf (printQuery, "\n");
    fprintf (printQuery, "    public DialogcQuery (%sFieldEdit theInterface)\n", fileName);
    fprintf (printQuery, "    {\n");
    fprintf (printQuery, "        dialogInterface = theInterface;\n");
    fprintf (printQuery, "    }\n");
    fprintf (printQuery, "\n");

    /*the database query takes place here*/
    fprintf (printQuery, "    @Override\n");
    fprintf (printQuery, "    public void actionPerformed (ActionEvent event)\n");
    fprintf (printQuery, "    {\n");
    fprintf (printQuery, "        dialogInterface.appendToStatusArea (\"QUERY\");\n");
    fprintf (printQuery, "        dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printQuery, "\n");
    fprintf (printQuery, "        Connection queryConnection = dialogInterface.getDBConnection ();\n");
    fprintf (printQuery, "        Statement psqlStatement = null;\n");
    fprintf (printQuery, "\n");
    fprintf (printQuery, "        try\n");
    fprintf (printQuery, "        {\n");
    fprintf (printQuery, "            DatabaseMetaData dbMetaData = queryConnection.getMetaData ();\n");
    fprintf (printQuery, "            ResultSet tableCheck = dbMetaData.getTables (null, null, \"%s\", null);\n", nameOfTable);    /*the database is checked for any table of the same name*/
    fprintf (printQuery, "            if (tableCheck.next () == false)\n");    /*if there is no table by that name, it is made*/
    fprintf (printQuery, "            {\n");
    fprintf (printQuery, "                dialogInterface.appendToStatusArea (\"Database table not found. Creating table...\\n\");\n");
    fprintf (printQuery, "\n");
    fprintf (printQuery, "                String createString = \"CREATE TABLE %s ( ID SERIAL", nameOfTable);
    for (i = 0; i < numOfFields; i ++)    /*each field is represented in the SQL command CREATE TABLE, where integers are INTEGERS, floats are DECIMAL(20,3) and strings are VARCHAR(30)*/
    {
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);;
        typeHolder = NULL;
        if (strcmp (stringHolderTwo, "integer") == 0)
        {
            typeHolder = "INTEGER";
        }
        else if (strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = "DECIMAL(20,3)";
        }
        else
        {
            typeHolder = "VARCHAR(30)";
        }

        fprintf (printQuery, " , %s %s", stringHolderOne, typeHolder);
    }
    fprintf (printQuery, " , PRIMARY KEY (ID))\";\n");

    fprintf (printQuery, "                try\n");
    fprintf (printQuery, "                {\n");
    fprintf (printQuery, "                    psqlStatement = queryConnection.createStatement ();\n");
    fprintf (printQuery, "                    psqlStatement.executeUpdate (createString);\n");
    fprintf (printQuery, "                    psqlStatement.close ();\n");
    fprintf (printQuery, "                }\n");
    fprintf (printQuery, "                catch (SQLException exception)\n");
    fprintf (printQuery, "                {\n");
    fprintf (printQuery, "                    dialogInterface.appendToStatusArea (\"Table unable to be created. Please try again later\\n\\n\");\n");
    fprintf (printQuery, "                    return;\n");
    fprintf (printQuery, "                }\n");
    fprintf (printQuery, "\n");
    fprintf (printQuery, "                dialogInterface.appendToStatusArea (\"Table created successfully\\n\\n\");\n");
    fprintf (printQuery, "            }\n");
    fprintf (printQuery, "            else\n");    /*the existing table is checked to see if it matches the same details as the generated GUI*/
    fprintf (printQuery, "            {\n");
    fprintf (printQuery, "                boolean IDCheck = false;\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printQuery, "                boolean %sCheck = false;\n", stringHolderOne);
    }
    fprintf (printQuery, "\n");
    fprintf (printQuery, "                ResultSet columnCheck = dbMetaData.getColumns (null, null, \"%s\", \"%%\");\n", nameOfTable);
    fprintf (printQuery, "                while (columnCheck.next () != false)\n");
    fprintf (printQuery, "                {\n");
    fprintf (printQuery, "                    String columnName = columnCheck.getString (4);\n");
    fprintf (printQuery, "                    int columnType = columnCheck.getInt (5);\n");
    fprintf (printQuery, "\n");
    fprintf (printQuery, "                    if (columnName.equalsIgnoreCase (\"ID\") && columnType == java.sql.Types.INTEGER)\n");    /*comparisons are made here, matching GUI details to table details if a match*/
    fprintf (printQuery, "                    {\n");
    fprintf (printQuery, "                        IDCheck = true;\n");
    fprintf (printQuery, "                    }\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);;
        typeHolder = NULL;
        if (strcmp (stringHolderTwo, "integer") == 0)
        {
            typeHolder = "INTEGER";
        }
        else if (strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = "NUMERIC";
        }
        else
        {
            typeHolder = "VARCHAR";
        }

        fprintf (printQuery, "                    else if (columnName.equalsIgnoreCase (\"%s\") && columnType == java.sql.Types.%s)\n", stringHolderOne, typeHolder);
        fprintf (printQuery, "                    {\n");
        fprintf (printQuery, "                        %sCheck = true;\n", stringHolderOne);
        fprintf (printQuery, "                    }\n");
    }
    fprintf (printQuery, "                    else\n");
    fprintf (printQuery, "                    {\n");
    fprintf (printQuery, "                        dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");\n");
    fprintf (printQuery, "                        return;\n");
    fprintf (printQuery, "                    }\n");
    fprintf (printQuery, "                }\n");
    fprintf (printQuery, "\n");
    fprintf (printQuery, "                if (IDCheck == false");
    for (i = 0; i < numOfFields - 1; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printQuery, " || %sCheck == false", stringHolderOne);
    }
    stringHolderOne = fieldsList[i];
    fprintf (printQuery, " || %sCheck == false)\n", stringHolderOne);    /*if all details match, it is assumed that this table belongs to this GUI*/
    fprintf (printQuery, "                {\n");
    fprintf (printQuery, "                    dialogInterface.appendToStatusArea (\"Conflicting table found. Please rename .config name and try again later\\n\\n\");\n");
    fprintf (printQuery, "                    return;\n");
    fprintf (printQuery, "                }\n");
    fprintf (printQuery, "            }\n");
    fprintf (printQuery, "        }\n");
    fprintf (printQuery, "        catch (SQLException exception)\n");
    fprintf (printQuery, "        {\n");
    fprintf (printQuery, "            dialogInterface.appendToStatusArea (\"Unable to find table. Please try again later\\n\\n\");\n");
    fprintf (printQuery, "            return;\n");
    fprintf (printQuery, "        }\n");
    fprintf (printQuery, "\n");

    /*each field is read and checked to see it it fits the query functionality*/
    fprintf (printQuery, "        boolean correctInput = true;\n");
    fprintf (printQuery, "        boolean idQuery = false;\n");
    fprintf (printQuery, "        ArrayList<String> fieldNames = new ArrayList<String> (10);\n");
    fprintf (printQuery, "        ArrayList<String> fieldValues = new ArrayList<String> (10);\n");
    fprintf (printQuery, "        String idInput = null;\n");
    fprintf (printQuery, "        String fieldInput = null;\n");
    fprintf (printQuery, "\n");
    fprintf (printQuery, "        try\n");
    fprintf (printQuery, "        {\n");
    fprintf (printQuery, "            idInput = dialogInterface.getDCID ();\n");
    fprintf (printQuery, "            fieldNames.add (\"ID\");\n");
    fprintf (printQuery, "            fieldValues.add (idInput);\n");
    fprintf (printQuery, "            idQuery = true;\n");
    fprintf (printQuery, "        }\n");
    fprintf (printQuery, "        catch (IllegalFieldValueException exception)\n");
    fprintf (printQuery, "        {\n");
    fprintf (printQuery, "            if (!exception.getMessage ().trim ().equals (\"\"))\n");
    fprintf (printQuery, "            {\n");
    fprintf (printQuery, "                dialogInterface.appendToStatusArea (\"Error - ID field not an integer\\n\");\n");
    fprintf (printQuery, "                correctInput = false;\n");
    fprintf (printQuery, "            }\n");
    fprintf (printQuery, "        }\n");
    for (i = 0; i < numOfFields; i ++)
    {
        fprintf (printQuery, "\n");
        stringHolderOne = fieldsList[i];
        stringHolderTwo = lookupHashTable (symbolTable, stringHolderOne);
        if (strcmp (stringHolderTwo, "integer") == 0 || strcmp (stringHolderTwo, "float") == 0)
        {
            typeHolder = NULL;
            if (strcmp (stringHolderTwo, "integer") == 0)
            {
                typeHolder = "an integer";
            }
            else
            {
                typeHolder = "a float";
            }

            fprintf (printQuery, "        try\n");
            fprintf (printQuery, "        {\n");
            fprintf (printQuery, "            fieldInput = dialogInterface.getDC%s ();\n", stringHolderOne);
            fprintf (printQuery, "            fieldNames.add (\"%s\");\n", stringHolderOne);
            fprintf (printQuery, "            fieldValues.add (fieldInput);\n");
            fprintf (printQuery, "        }\n");
            fprintf (printQuery, "        catch (IllegalFieldValueException exception)\n");
            fprintf (printQuery, "        {\n");
            fprintf (printQuery, "            if (!exception.getMessage ().trim ().equals (\"\"))\n");
            fprintf (printQuery, "            {\n");
            fprintf (printQuery, "                dialogInterface.appendToStatusArea (\"Error - %s field not %s\\n\");\n", stringHolderOne, typeHolder);
            fprintf (printQuery, "                correctInput = false;\n");
            fprintf (printQuery, "            }\n");
            fprintf (printQuery, "        }\n");
        }
        else
        {
            fprintf (printQuery, "        try\n");
            fprintf (printQuery, "        {\n");
            fprintf (printQuery, "            fieldInput = dialogInterface.getDC%s ();\n", stringHolderOne);
            fprintf (printQuery, "            if (!fieldInput.trim ().equals (\"\"))\n");
            fprintf (printQuery, "            {\n");
            fprintf (printQuery, "                fieldNames.add (\"%s\");\n", stringHolderOne);
            fprintf (printQuery, "                fieldValues.add (fieldInput);\n");
            fprintf (printQuery, "            }\n");
            fprintf (printQuery, "        }\n");
            fprintf (printQuery, "        catch (IllegalFieldValueException exception)\n");
            fprintf (printQuery, "        {\n");
            fprintf (printQuery, "            dialogInterface.appendToStatusArea (\"Error - %s field not a string\\n\");\n", stringHolderOne);
            fprintf (printQuery, "            correctInput = false;\n");
            fprintf (printQuery, "        }\n");
        }
    }
    fprintf (printQuery, "\n");
    fprintf (printQuery, "        if (correctInput == false)\n");
    fprintf (printQuery, "        {\n");
    fprintf (printQuery, "            dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printQuery, "            return;\n");
    fprintf (printQuery, "        }\n");
    fprintf (printQuery, "\n");

    /*a query is formed based on the information given*/
    fprintf (printQuery, "        String queryString = \"SELECT * FROM %s\";\n", nameOfTable);
    fprintf (printQuery, "        int listLength = fieldNames.size ();\n");
    fprintf (printQuery, "        if (idQuery == true)\n");
    fprintf (printQuery, "        {\n");
    fprintf (printQuery, "            queryString = queryString + \" WHERE ID = \" + idInput;\n");
    fprintf (printQuery, "        }\n");
    fprintf (printQuery, "        else if (listLength > 0)\n");
    fprintf (printQuery, "        {\n");
    fprintf (printQuery, "            queryString = queryString + \" WHERE \";\n");
    fprintf (printQuery, "            for (int i = 0; i < listLength - 1; i ++)\n");
    fprintf (printQuery, "            {\n");
    fprintf (printQuery, "                queryString = queryString + fieldNames.get (i) + \" = '\" + fieldValues.get (i) + \"' AND \";\n");
    fprintf (printQuery, "            }\n");
    fprintf (printQuery, "            queryString = queryString + fieldNames.get (listLength - 1) + \" = '\" + fieldValues.get (listLength - 1) + \"'\";\n");
    fprintf (printQuery, "        }\n");
    fprintf (printQuery, "        try\n");
    fprintf (printQuery, "        {\n");
    fprintf (printQuery, "            psqlStatement = queryConnection.createStatement ();\n");
    fprintf (printQuery, "            ResultSet results = psqlStatement.executeQuery (queryString);\n");
    fprintf (printQuery, "\n");
    fprintf (printQuery, "            int numOfMatches = 0;\n");
    fprintf (printQuery, "            String ID = null;\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printQuery, "            String %s = null;\n", stringHolderOne);
    }
    fprintf (printQuery, "\n");
    fprintf (printQuery, "            while (results.next () != false)\n");
    fprintf (printQuery, "            {\n");
    fprintf (printQuery, "                numOfMatches ++;\n");
    fprintf (printQuery, "                ID = results.getString (\"ID\");\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printQuery, "                %s = results.getString (\"%s\");\n", stringHolderOne, stringHolderOne);
    }
    fprintf (printQuery, "                    dialogInterface.appendToStatusArea (\"ID = \" + ID + \"\\n\"");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printQuery, " + \"%s = \" + %s + \"\\n\"", stringHolderOne, stringHolderOne);
    }
    fprintf (printQuery, ");\n");
    fprintf (printQuery, "                dialogInterface.appendToStatusArea (\"\\n\");\n");
    fprintf (printQuery, "            }\n");
    fprintf (printQuery, "\n");

    /*if the query was done through the id, then the fields are replaced with the id's data*/
    fprintf (printQuery, "            dialogInterface.appendToStatusArea (numOfMatches + \" result(s)\\n\\n\");\n");
    fprintf (printQuery, "            if (numOfMatches == 1 && idQuery == true)\n");
    fprintf (printQuery, "            {\n");
    fprintf (printQuery, "                dialogInterface.setDCID (ID);\n");
    for (i = 0; i < numOfFields; i ++)
    {
        stringHolderOne = fieldsList[i];
        fprintf (printQuery, "                dialogInterface.setDC%s (%s);\n", stringHolderOne, stringHolderOne);
    }
    fprintf (printQuery, "            }\n");
    fprintf (printQuery, "\n");
    fprintf (printQuery, "            psqlStatement.close ();\n");
    fprintf (printQuery, "            results.close ();\n");
    fprintf (printQuery, "        }\n");
    fprintf (printQuery, "        catch (SQLException exception)\n");
    fprintf (printQuery, "        {\n");
    fprintf (printQuery, "            dialogInterface.appendToStatusArea (\"Query failed\\n\\n\");\n");
    fprintf (printQuery, "        }\n");
    fprintf (printQuery, "    }\n");
    fprintf (printQuery, "}\n");

    free (nameOfTable);

    return;
}

/****
Preconditions: Parsing with the yadc compiler has been attempted
Postconditions: Allocated memory during compilation is freed from memory
****/
void cleanUp ()
{
    int i;

    if (title != NULL)
    {
        free (title);
    }

    /*The lists and hash table are freed from memory*/
    for (i = 0; i < numOfFields; i ++)
    {
        free (fieldsList[i]);
    }
    free (fieldsList);

    for (i = 0; i < numOfButtons; i ++)
    {
        free (buttonsList[i]);
    }    
    free (buttonsList);

    destroyHashTable (symbolTable);

    return;
}

/****
Preconditions: A char * array has been allocated in memory
Postconditions: The array (string) is freed from memory
****/
void freeString (void * stringToFree)
{
    if (stringToFree != NULL)
    {
        free (stringToFree);
    }

    return;
}
