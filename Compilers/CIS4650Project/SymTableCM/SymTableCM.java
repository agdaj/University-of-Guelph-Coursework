/**********************************************
SymTableCM.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 16
     Last Modified: 2017 04 04
C- Symbol Table Main Class (Storing + Printing)
Compiler Project - CIS*4650
**********************************************/

package SymTableCM;

import java.util.*;
import AbSynCM.*;

public class SymTableCM {
    private Hashtable<String, ArrayList<SymbolCM>> symTable;
    private int currentScope;
    private boolean printTable;
    private boolean successTable;

    private static final int INT_ARRAY = 2;    //to differentiate int variable with int array
    private static final int VOID_ARRAY = 3;   //likewise for voids (even though void variables are not allowed)

    private int currentFuncReturn;
    private boolean returnNeeded;
    private boolean returnExists;
    private boolean arrayAsSingleVar = false;  //in order to facilitate array variables not being subscripted (for function calls)
    private char blockChar;

    //default table size (prime number)
    private static final int TABLE_SIZE = 19;

    //indentation standard
    private static final int SPACES = 4;

    /* The following are printing-related functions */
    private static void indent (int spaces) {
        for (int i = 0; i < spaces; i++) System.out.print (" ");
    }

    /* Create the hash table for the symbol table, and preload input() and output() functions */
    public SymTableCM () {
        symTable = new Hashtable<String, ArrayList<SymbolCM>> (TABLE_SIZE);

        FunctionDec inputFunc = new FunctionDec (0, new NameTy(0, NameTy.INT), "input", null, null);
        SymbolCM inputSym = new SymbolCM ("input", inputFunc, 0);
        addToTable (inputSym);

        VarDec outputX = new SimpleDec (0, new NameTy(0, NameTy.INT), "x");
        VarDecList outputParam = new VarDecList (outputX, null);
        FunctionDec outputFunc = new FunctionDec (0, new NameTy(0, NameTy.VOID), "output", outputParam, null);
        SymbolCM outputSym = new SymbolCM ("output", outputFunc, 0);
        addToTable (outputSym);

        currentScope = 0;
        successTable = true;
        blockChar = 'A';
    }

    /* This is the main public interface to create (and print) symbol tables generated by the
       CUP parser output (DecList), return success state */
    public boolean buildSymTableCM (boolean printTable, DecList tree, int spaces) {
        this.printTable = printTable;

        if (printTable == true) {
            indent (spaces);
            System.out.println ("Entering the global scope:");
        }

        spaces += SPACES;
        Dec lastDec = null;    //track the last declaration to see if it is void main(void)
        while (tree != null) {
            buildTable (tree.head, spaces);
            lastDec = tree.head;
            tree = tree.tail;
        }

        /* Evaluate the last declaration to see if it is void main(void) */
        if (lastDec == null) {
            System.err.println ("Error - .cm file lacks a declaration");
            successTable = false;
        }
        else if (!(lastDec instanceof FunctionDec)) {
            System.err.println ("Error - Last declaration is not void main(void)");
            successTable = false;
        }
        else {
            FunctionDec lastFuncDec = (FunctionDec)lastDec;
            if (lastFuncDec.func.compareTo("main") != 0) {
                System.err.println ("Error - Last declaration is not 'main'");
                successTable = false;
            }
        }

        /* Once the entire tree has been traversed, print global scope declarations */
        if (printTable == true) {
            listTable (spaces);
            indent (spaces - SPACES);
            System.out.println ("Leaving the global scope.");
        }

        return (successTable);
    }

    /* For each Dec (declaration) in DecList, analyze FunctionDec or VarDec and call appropriate buildTable*/
    private void buildTable (Dec tree, int spaces) {
        if (tree == null) {
            successTable = false;
            if (printTable == true) {
                indent (spaces);
                System.out.println ("Illegal declaration");    //print illegal error, reflect parse error
            }
        }
        else if (tree instanceof FunctionDec)
            buildTable ((FunctionDec)tree, spaces);
        else if (tree instanceof VarDec)
            buildTable ((VarDec)tree, spaces);
        else {
            successTable = false;
            if (printTable == true) {
                indent (spaces);
                System.out.println ("Illegal declaration at line " + (tree.pos + 1));
            }
        }
    }

    /* Load in the FunctionDec (function declaration) in the symbol table if it is not previously defined */
    private void buildTable (FunctionDec tree, int spaces) {
        SymbolCM newFuncSymbol = new SymbolCM (tree.func, tree, currentScope);    //create new SymbolCM for the function

        if (addToTable(newFuncSymbol) == false) {
            System.err.println ("Error - Redefine error at line " + (tree.pos + 1) + " for " + tree.func);
            successTable = false;
        }

        if (printTable == true) {
            indent (spaces);
            System.out.println ("Entering function " + tree.func + ":");
        }

        spaces += SPACES;
        currentFuncReturn = tree.result.typ;    //store current function's return type to type check later
        if (tree.result.typ == NameTy.INT)      //check if return is needed (if function returns int)
            returnNeeded = true;
        else
            returnNeeded = false;
        returnExists = false;
        currentScope++;
        buildTable (tree.params, spaces);    //add parameters variables to table
        buildTable (tree.body, spaces);      //add body variables and type check in the body

        if (returnNeeded == true && returnExists == false) {    //if a return is needed but never found, print error
            System.err.println ("Error - No return at line "+ (tree.pos + 1) + " for " + tree.func + " - Expect int");
            successTable = false;
        }
        returnExists = false;                //reset return search

        if (printTable == true) {
            listTable (spaces);       //after function is finished, print the table's scope variables and leave scope
            indent (spaces - SPACES);
            System.out.println ("Leaving function " + tree.func + ".");
        }

        removeFromTable ();    //then, remove all variables of the current scope and go back a depth level
        currentScope--;
    }

    /* If the Dec is a VarDec, enter the variable into the table */
    private void buildTable (VarDec tree, int spaces) {
        if (tree == null) {
            successTable = false;
            if (printTable == true) {
                indent(spaces);
                System.out.println ("Illegal variable declaration");    //print illegal line, reflect parse error
            }
        }
        else if (tree instanceof SimpleDec) {
            tree.nestLevel = currentScope;    //record nest level of variable declaration
            SymbolCM newVarSymbol = new SymbolCM (tree.name, tree, currentScope);    //create new SymbolCM for the variable

            if (addToTable(newVarSymbol) == false) {
                System.err.println ("Error - Redefine error at line " + (tree.pos + 1) + " for " + tree.name);
                successTable = false;
            }
        }
        else if (tree instanceof ArrayDec) {
            ArrayDec thisTree = (ArrayDec)tree;
            tree.nestLevel = currentScope;    //record nest level of variable declaration
            SymbolCM newVarSymbol = new SymbolCM (tree.name, tree, currentScope);    //create new SymbolCM for the variable

            if (addToTable(newVarSymbol) == false) {
                System.err.println ("Error - Redefine error at line " + (tree.pos + 1) + " for " + tree.name);
                successTable = false;
            }

            buildTable (thisTree.size, spaces);    //after adding (or failing to add) Symbol, check array's int expression for an int
        }
        else {
            successTable = false;
            if (printTable == true) {
                indent (spaces);
                System.out.println ("Illegal variable declaration at line " + (tree.pos + 1));
            }
        }
    }

    /* Variable declarations are sifted through when in a list, calling the above function to add into the symbol table */
    private void buildTable (VarDecList tree, int spaces) {
        while (tree != null) {
            buildTable (tree.head, spaces);
            tree = tree.tail;
        }
    }

    /* Any reference to an expresion list go here, and call the relevant expression-based overloaded function below */
    private void buildTable (ExpList tree, int spaces) {
        while (tree != null) {
            buildTable (tree.head, spaces);
            tree = tree.tail;
        }
    }

    /* Given an Exp (expression) decide which kind to call to print */
    private int buildTable (Exp tree, int spaces) {
        if (tree == null) {
            successTable = false;
            if (printTable == true) {
                indent (spaces);
                System.out.println ("Illegal expression");    //print illegal line, reflect parse error
            }
        }
        else if (tree instanceof NilExp)
            return (NameTy.VOID);
        else if (tree instanceof VarExp)
            return (buildTable ((VarExp)tree, spaces));
        else if (tree instanceof IntExp)
            return (NameTy.INT);
        else if (tree instanceof CallExp)
            return (buildTable ((CallExp)tree, spaces));
        else if (tree instanceof OpExp)
            return (buildTable ((OpExp)tree, spaces));
        else if (tree instanceof AssignExp)
            return (buildTable ((AssignExp)tree, spaces));
        else if (tree instanceof IfExp)
            buildTable ((IfExp)tree, spaces);
        else if (tree instanceof WhileExp)
            buildTable ((WhileExp)tree, spaces);
        else if (tree instanceof ReturnExp)
            buildTable ((ReturnExp)tree, spaces);
        else if (tree instanceof CompoundExp) {
            char blockName = blockChar;
            blockChar++;

            if (printTable == true) {
                indent (spaces);
                System.out.println ("Entering block " + blockName + ":");
            }

            spaces += SPACES;
            currentScope++;
            buildTable ((CompoundExp)tree, spaces);

            if (printTable == true) {
                listTable (spaces);       //after function is finished, print the table's scope variables
                indent (spaces - SPACES);
                System.out.println ("Leaving block " + blockName + ".");
            }

            removeFromTable ();    //then, remove all variables of the current scope and go back a depth level
            currentScope--;
        }
        else {
            successTable = false;
            if (printTable == true) {
                indent (spaces);
                System.out.println ("Illegal expression at line " + (tree.pos + 1));
            }
        }

        return (NameTy.VOID);
    }

    /* Nil expressions just return NameTy.VOID */
    private int buildTable (NilExp tree, int spaces) {
        return (NameTy.VOID);
    }

    /* Return the variable's type if correctly referenced, else print error */
    private int buildTable (VarExp tree, int spaces) {
        if (tree.variable == null) {
            successTable = false;
            if (printTable == true) {
                indent(spaces);
                System.out.println ("Illegal variable");    //print illegal line, reflect parse error
            }
        }
        else {
            SymbolCM varLookup = lookupTable (tree.variable.name);
            if (varLookup == null) {
                System.err.println ("Error - Undefined error at line " + (tree.pos + 1) + " for " + tree.variable.name);
                successTable = false;
            }
            else if (varLookup.type instanceof FunctionDec) {
                System.err.println ("Error - Usage error at line " + (tree.pos + 1) + " for " + tree.variable.name + " - Function used as variable");
                successTable = false;
            }
            else {
                checkVar ((VarDec)varLookup.type, tree, spaces);    //inspect if the variable usage matches the declaration (simple vs. array)
                if (varLookup.type instanceof ArrayDec) {
                    if (tree.variable instanceof IndexVar) {
                        return (((VarDec)varLookup.type).typ.typ);
                    }
                    else {
                        if (((VarDec)varLookup.type).typ.typ == NameTy.INT)
                            return (INT_ARRAY);
                        else
                            return (VOID_ARRAY);
                    }
                }
                else
                    return (((VarDec)varLookup.type).typ.typ);
            }
        }

        return (NameTy.INT);
    }

    /* Compare a symbol's variable declaration with usage, and check any IndexVar's expression to be int */
    /* If the declaration macthes usage, link variable to declaration for code generation */
    private void checkVar (VarDec dec, VarExp usage, int spaces) {
        if (dec instanceof SimpleDec && usage.variable instanceof IndexVar) {
            System.err.println ("Error - Usage error at line " + (usage.pos + 1) + " - Used SimpleDec as IndexVar");
            successTable = false;
        }
        else if (dec instanceof ArrayDec && usage.variable instanceof SimpleVar) {
            /* Will allow reference of array declaration as single variable (-> referring to whole array/first element) */
            if (arrayAsSingleVar == false) {
                System.err.println ("Error - Usage error at line " + (usage.pos + 1) + " - Used ArrayDec as SimpleVar");
                successTable = false;
            }
            else
                usage.variable.varDecRef = dec;
        }
        else if (dec instanceof SimpleDec && usage.variable instanceof SimpleVar) {    /* With a match, the declaration is tied to the variable usage */
            usage.variable.varDecRef = dec;
        }
        else if (dec instanceof ArrayDec && usage.variable instanceof IndexVar) {
            usage.variable.varDecRef = dec;
        }

        if (usage.variable instanceof IndexVar) {
            if (buildTable(((IndexVar)usage.variable).index, spaces) != NameTy.INT) {
                System.err.println ("Error - Usage error at line " + (usage.pos + 1) + " - Used IndexVar with non-int expression");
                successTable = false;
            }
        }
    }


    /* Int expressions just return NameTy.INT */
    private int buildTable (IntExp tree, int spaces) {
        return (NameTy.INT);
    }

    /* Call expressions return the return type of the calling function, else print an error (will return NameTy.VOID) */
    /* With a valid reference, link function call to function declaration for code generation */
    private int buildTable (CallExp tree, int spaces) {
        SymbolCM funcLookup = lookupTable (tree.func);
        if (funcLookup == null) {
            System.err.println ("Error - Undefined error at line " + (tree.pos + 1) + " for " + tree.func);
            successTable = false;
        }
        else if (funcLookup.type instanceof VarDec) {
            System.err.println ("Error - Usage error at line " + (tree.pos + 1) + " for " + tree.func + " - Variable used as function");
            successTable = false;
        }
        else {
            /* If the function is in the table, check the argument types */
            checkArgs (((FunctionDec)funcLookup.type).params, tree.args, spaces, tree.pos + 1);
            tree.funcToCall = (FunctionDec)funcLookup.type;    /* Link the call to the function declaration */
            return (((FunctionDec)funcLookup.type).result.typ);
        }

        return (NameTy.VOID);
    }

    /* Compare a symbol's parameter declarations with the argument list of a CallExp */
    private void checkArgs (VarDecList funcParams, ExpList args, int spaces, int callPos) {
        arrayAsSingleVar = true;    //temporarily allow array declarations to be used as SimpleVar reference (necessary)
        while (funcParams != null && args != null) {
            if (funcParams.head.typ.typ != buildTable(args.head, spaces)) {    //if there is a mismatch, check types for reason (may not be error if SimpleVar->array)
                //for array parameters, must match to a single identifier representing an array variable (otherwise is actually an error
                if (funcParams.head instanceof ArrayDec) {
                    if (!(args.head instanceof VarExp) || !(((VarExp)args.head).variable instanceof SimpleVar)) {
                        System.err.println ("Error - Arg error at line " + (args.head.pos + 1) + " - Array parameter not matched with single identifier representing an array variable");
                        successTable = false;
                    }
                    else {
                        SymbolCM checkArgsSym = lookupTable (((SimpleVar)((VarExp)args.head).variable).name);
                        if (checkArgsSym == null || !(checkArgsSym.type instanceof ArrayDec)) {
                            System.err.println ("Error - Arg error at line " + (args.head.pos + 1) + " - Array parameter not matched with single identifier representing an array variable");
                            successTable = false;
                        }
//                        else {
//                            ((ArrayDec)checkArgsSym.type).size = ((ArrayDec)((VarExp)args.head).variable.varDecRef).size;    //link parameter with argument
//                        }
                    }
                }
                else if (funcParams.head.typ.typ == NameTy.INT) {
                    System.err.println ("Error - Arg error at line " + (args.head.pos + 1) + " - Expect int");
                    successTable = false;
                }
                else {
                    System.err.println ("Error - Arg error at line " + (args.head.pos + 1) + " - Expect void");
                    successTable = false;
                }
            }
            else {    //for array parameters, must match to a single identifier representing an array variable (this executes if ArrayVar->array)
                if (funcParams.head instanceof ArrayDec) {
                    if (!(args.head instanceof VarExp) || !(((VarExp)args.head).variable instanceof SimpleVar)) {
                        System.err.println ("Error - Arg error at line " + (args.head.pos + 1) + " - Array parameter not matched with single identifier representing an array variable");
                        successTable = false;
                    }
                    else {
                        SymbolCM checkArgsSym = lookupTable (((SimpleVar)((VarExp)args.head).variable).name);
                        if (checkArgsSym == null || !(checkArgsSym.type instanceof ArrayDec)) {
                            System.err.println ("Error - Arg error at line " + (args.head.pos + 1) + " - Array parameter not matched with single identifier representing an array variable");
                            successTable = false;
                        }
//                        else {
//                            ((ArrayDec)checkArgsSym.type).size = ((ArrayDec)((VarExp)args.head).variable.varDecRef).size;    //link parameter with argument
//                        }
                    }
                }
//                else if (args.head instanceof VarExp) {
//                    ((VarExp)args.head).variable.varDecRef = (VarDec)funcParams.head;    //link parameter to declaration if argument is variable based
//                }
            }

            funcParams = funcParams.tail;
            args = args.tail;
        }

        /* If there is a mismatch in the number of arguments, set error */
        if ((funcParams == null && args != null) || (funcParams != null && args == null)) {
            System.err.print ("Error - Usage error at line " + callPos);
            if (funcParams == null && args != null)
                System.err.println (" - Too many arguments");
            else
                System.err.println (" - Too few arguments");
            successTable = false;
        }

        arrayAsSingleVar = false;    //reset after function argument analysis is done
    }

    /* Operation expressions evaluate both ends of the expressions to check for type matching (NameTy.INT) */
    private int buildTable (OpExp tree, int spaces) {
        if (buildTable(tree.left, spaces) != buildTable(tree.right, spaces)) {
            System.err.println ("Error - Operation error at line " + (tree.pos + 1) + " - Type mismatch");
            successTable = false;
        }

        return (NameTy.INT);
    }

    /* Assignment expressions evaluate the match between the variable expression and the assignment expression */
    private int buildTable (AssignExp tree, int spaces) {
        SymbolCM varLookup = lookupTable (tree.lhs.name);
        if (varLookup == null) {
            System.err.println ("Error - Undefined error at line " + (tree.pos + 1) + " for " + tree.lhs.name);
            successTable = false;
        }
        else if (varLookup.type instanceof FunctionDec) {
            System.err.println ("Error - Usage error at line " + (tree.pos + 1) + " for " + tree.lhs.name + " - Function used as variable");
            successTable = false;
        }
        else {
            checkVar ((VarDec)varLookup.type, new VarExp(tree.lhs.pos, tree.lhs), spaces);    //inspect if the variable usage matches the declaration (simple vs. array)

            int varResult;
            if (varLookup.type instanceof ArrayDec) {                        //if the used variable is an array...
                    if (tree.lhs instanceof SimpleVar)                       //and it a single var is used to represent the entire array, recognize as array
                        if (((VarDec)varLookup.type).typ.typ == NameTy.INT)  //and it is a reference to a single array member, recognize as one int/void
                            varResult = INT_ARRAY;
                        else
                            varResult = VOID_ARRAY;
                    else                                                     //and it is a reference to a single array member, recognize as one int/void
                        varResult = ((VarDec)varLookup.type).typ.typ;
            }
            else
                varResult = ((VarDec)varLookup.type).typ.typ;

            if (varResult != buildTable(tree.rhs, spaces)) {
                System.err.print ("Error - Assign error at line " + (tree.pos + 1) + " - Type mismatch");
                if (varResult == NameTy.INT)
                    System.err.println (" - Expect int assignment");
                else if (varResult == NameTy.VOID)
                    System.err.println (" - Expect void assignment");
                else if (varResult == INT_ARRAY)
                    System.err.println (" - Expect int array assignment");
                else
                    System.err.println (" - Expect void array assignment");

                successTable = false;
            }
        }

        return (NameTy.INT);    //assignments result in a '1' in C
    }

    /* If expressions evaluate the test if the result is an int, and further evaluate the code body of the if and else (if exists) */
    private void buildTable (IfExp tree, int spaces) {
        if (buildTable(tree.test, spaces) != NameTy.INT) {
            System.err.println ("Error - If error at line " + (tree.pos + 1) + " - Test does not result in int");
            successTable = false;
        }

        buildTable (tree.thenExp, spaces);
        if (tree.elseExp != null)    //evaluate else portion if exists, same process as always
            buildTable (tree.elseExp, spaces);
    }

    /* While expressions evaluate the test if the result is an int, and further evaluate the code body */
    private void buildTable (WhileExp tree, int spaces) {
        if (buildTable(tree.test, spaces) != NameTy.INT) {
            System.err.println ("Error - While error at line " + (tree.pos + 1) + " - Test does not result in int");
            successTable = false;
        }

        buildTable (tree.body, spaces);
    }

    /* Return expressions will be compared to the currentFuncReturn to check for match/mismatch */
    private void buildTable (ReturnExp tree, int spaces) {
        if (buildTable(tree.exp, spaces) != currentFuncReturn) {
            System.err.print ("Error - Return error at line " + (tree.pos + 1));
            if (currentFuncReturn == NameTy.INT)
                System.err.println (" - Expect int return");
            else
                System.err.println (" - Expect void return");

            successTable = false;
        }

        returnExists = true;
    }

    /* Compound expressions call the respective buildTable functions, which handle variable declarations and type checking expressions */
    private void buildTable (CompoundExp tree, int spaces) {
        buildTable (tree.decs, spaces);
        buildTable (tree.exps, spaces);
    }

    /* These functions are basic hash table functions that buildSymTableCM will eventually call */

    /* Add new SymbolCM to the symbol table, while also checking for redefine errors */
    private boolean addToTable (SymbolCM newSymbol) {
        ArrayList<SymbolCM> tableList = symTable.get(newSymbol.name);
        SymbolCM lookupSym = lookupTable(newSymbol.name);
        if (tableList == null) {         //if the Hashtable list is empty, create one and place into Hashtable with the new SymbolCM
            tableList = new ArrayList<SymbolCM> (TABLE_SIZE);
            tableList.add(0, newSymbol);
            symTable.put(newSymbol.name, tableList);
            return (true);
        }
        else if (lookupSym == null) {    //if the ArrayList but the lookup did not return a result of the same name, place Symbol to list front
            tableList.add(0, newSymbol);
            return (true);
        }
        else {                           //if the lookup returned a SymbolCM, analyze if the variable has been previously defined at the current scope
            if (lookupSym.depth == currentScope)
                return (false);
            else {
                tableList.add(0, newSymbol);
                return (true);
            }
        }
    }

    /* Look up a SymbolCM based on a given name, while also checking for undefined errors */
    private SymbolCM lookupTable (String name) {
        ArrayList<SymbolCM> tableList = symTable.get(name);
        if (tableList == null)
            return null;    //if no mapping ever existed -> no definition of SymbolCM

        /* If mapping exists, search for Symbol definition */
        Iterator<SymbolCM> symIterator = tableList.iterator();
        while (symIterator.hasNext() == true) {
            SymbolCM currentSym = symIterator.next();

            /* If a SymbolCM matches the current name, return the SymbolCM */
            if (currentSym.name.compareTo(name) == 0)
                return (currentSym);
        }

        return null;
    }

    /* Attain an Enumeration of all Hashtable values, and print all SymbolsCM of the currentScope */
    private void listTable (int spaces) {
        Enumeration<ArrayList<SymbolCM>> tableContents = symTable.elements();

        while (tableContents.hasMoreElements() == true) {
            ArrayList<SymbolCM> tableList = tableContents.nextElement();
            Iterator<SymbolCM> symIterator = tableList.iterator();

            while (symIterator.hasNext() == true) {
                SymbolCM currentSym = symIterator.next();

                /* Print the declaration in a specific format */
                if (currentSym.depth == currentScope) {
                    indent(spaces);
                    if (currentSym.type instanceof SimpleDec) {
                        if (((SimpleDec)currentSym.type).typ.typ == NameTy.INT)
                            System.out.println ("int " + currentSym.name + ";");
                        else
                            System.out.println ("void " + currentSym.name + ";");
                    }
                    else if (currentSym.type instanceof ArrayDec) {
                        String intValue = "";
                        if (((ArrayDec)currentSym.type).size != null)
                            intValue = intValue.concat(Integer.toString(((ArrayDec)currentSym.type).size.value));

                        if (((ArrayDec)currentSym.type).typ.typ == NameTy.INT)
                            System.out.println ("int " + currentSym.name + "[" + intValue + "];");
                        else
                            System.out.println ("void " + currentSym.name + "[" + intValue + "];");
                    }
                    else {    //else condition refers to a FunctionDec
                        String argsString = "";    //store the paramaters list into a separate String
                        VarDecList funcParams = ((FunctionDec)currentSym.type).params;
                        if (funcParams == null)
                            argsString = "void";
                        else {
                            while (funcParams != null) {
                                if (funcParams.head.typ.typ == NameTy.INT)
                                    argsString = argsString.concat("int");
                                else
                                    argsString = argsString.concat("void");

                                if (funcParams.head instanceof ArrayDec)
                                    argsString = argsString.concat("[]");

                                funcParams = funcParams.tail;
                                if (funcParams != null)
                                    argsString = argsString.concat(", ");
                            }
                        }

                        if (((FunctionDec)currentSym.type).result.typ == NameTy.INT)
                            System.out.println ("int " + currentSym.name + "(" + argsString + ");");
                        else
                            System.out.println ("void " + currentSym.name + "(" + argsString + ");");
                    }
                }
            }
        }
    }

    /* Will Enumerate through all Hashtable values and remove SymbolCM that are of the currentScope */
    private void removeFromTable () {
        Enumeration<ArrayList<SymbolCM>> tableContents = symTable.elements();

        while (tableContents.hasMoreElements() == true) {
            ArrayList<SymbolCM> tableList = tableContents.nextElement();
            Iterator<SymbolCM> symIterator = tableList.iterator();

            while (symIterator.hasNext() == true) {
                SymbolCM currentSym = symIterator.next();

                /* print the declaration in a specific format */
                if (currentSym.depth == currentScope)
                    symIterator.remove();
            }
        }
    }
}

