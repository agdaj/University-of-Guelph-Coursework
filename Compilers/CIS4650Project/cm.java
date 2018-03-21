/*****************************
cm.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 01
     Last Modified: 2017 04 05
C-Minus Compiler Main Class
Compiler Project - CIS*4650
*****************************/

import java.io.*;
import AbSynCM.*;
import SymTableCM.*;
import AssembleCM.*;

public class cm {
    //checkpoint constants
    private static final int BAD_ARGS = 0;
    private static final int ABS_STOP = 1;
    private static final int SYM_STOP = 2;
    private static final int TM_STOP = 3;
    private static final int NO_STOP = 4;

    public static void main(String argv[]) {
        /* First, check arguments for when to stop C- compilation */
        int compilationStop = checkArgs (argv);
        if (compilationStop == BAD_ARGS) {
            System.err.println ("To run program: java -classpath /usr/share/java/cup.jar:. cm [-a|-s|-c] <your_input_file>.cm");
            System.exit (1);
        }

        /* Retrieves file name without .cm extension (enforced in checkArgs) */
        String fileName = argv[argv.length - 1].substring(0, argv[argv.length - 1].lastIndexOf ("."));
        PrintStream newOutputStream;

        try {
            /* Then, parse the file for C- syntax tree */
            parser parserCM = new parser (new LexerCM (new FileReader (argv[argv.length - 1])));
            Object resultTree = parserCM.parse().value;
            if (resultTree == null) {    //if error, abort compilation
                System.exit (1);
            }

            /* If the -a flag is specified, print syntax tree to .abs file */
            if (compilationStop == ABS_STOP) {
                newOutputStream = new PrintStream (new BufferedOutputStream (new FileOutputStream (fileName.concat (".abs"))), true);
                System.setOut (newOutputStream);
                System.out.println ("Abstract Syntax Tree for " + fileName + ".cm");
                SynTreeCM.showTreeCM ((DecList)resultTree, 0);
                if (parser.goodParse != 1)
                    System.exit (1);
                else
                    System.exit (0);
            }

            if (parser.goodParse != 1)      //if error, abort compilation
                System.exit (1);

            /* Next, build the symbol table and type check the abstract syntax tree, exit if there are errors */
            SymTableCM cmSymTable = new SymTableCM();
            boolean tableCheck = true;
            if (compilationStop == SYM_STOP) {
                newOutputStream = new PrintStream (new BufferedOutputStream (new FileOutputStream (fileName.concat (".sym"))), true);
                System.setOut (newOutputStream);
                System.out.println ("Symbol Table for " + fileName + ".cm");
                tableCheck = cmSymTable.buildSymTableCM (true, (DecList)resultTree, 0);
                System.setOut (new PrintStream (new FileOutputStream (FileDescriptor.out)));
            } else
                tableCheck = cmSymTable.buildSymTableCM (false, (DecList)resultTree, 0);

            if (tableCheck == false)                 System.exit(1);
            else if (compilationStop == SYM_STOP)    System.exit(0);

            /* Finally, if syntax and semantic checks all pass, generate TM assembly code */
            newOutputStream = new PrintStream (new BufferedOutputStream (new FileOutputStream (fileName.concat (".tm"))), true);
            System.setOut (newOutputStream);
            System.out.println ("* C-Minus Compilation to TM Code of " + fileName + ".cm");
            System.out.println ("* File: " + fileName.concat (".tm"));
            AssembleCM.genCodeCM ((DecList)resultTree);
            System.setOut (new PrintStream (new FileOutputStream (FileDescriptor.out)));
        } catch (FileNotFoundException e) {
            System.err.println ("Unable to find file: " + argv[argv.length - 1] + " - cm Terminated");
            System.exit (1);
        } catch (Exception e) {
            System.err.println ("Exception Error in C- Compilation - cm Terminated");
            System.exit (1);
        }

        System.out.println ("C- Compilation Complete");
  }

    private static int checkArgs (String argv[]) {
        int numArgs = argv.length;
        /* Check argument count */
        if (numArgs > 2 || numArgs < 1) {
            if (numArgs > 2)    System.err.println ("Too many arguments used");
            else                System.err.println ("Too few arguments used");

            return (BAD_ARGS);
        }

        /* Check file extension */
        if (argv[argv.length - 1].endsWith(".cm") == false) {
            System.err.println ("File does not end in .cm");
            return (BAD_ARGS);
        }

        /* Assess which flag was used */
        if (numArgs == 2) {
            if (argv[0].equals ("-a")) return (ABS_STOP);
            else if (argv[0].equals ("-s")) return (SYM_STOP);
            else if (argv[0].equals ("-c")) return (TM_STOP);
            else {
                System.err.println ("Invalid argument used");
                return (BAD_ARGS);
            }
        } else {
            return (NO_STOP);
        }
    }
}
