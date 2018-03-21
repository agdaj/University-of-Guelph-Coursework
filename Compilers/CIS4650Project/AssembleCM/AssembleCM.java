/*************************************
AssembleCM.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 24
     Last Modified: 2017 04 04
C- Assembly Code Production Main Class
Compiler Project - CIS*4650
*************************************/

package AssembleCM;

import java.util.*;
import AbSynCM.*;

public abstract class AssembleCM {
    /* Special register labels to simplify assembly approach */
    private static final int PC = 7;      //program counter
    private static final int GP = 6;      //global (variable) pointer
    private static final int FP = 5;      //frame pointer
    private static final int AC = 0;      //main register to be used
    private static final int AC_1 = 1;    //secondary register to be used for two-register instructions

    private static final int OFP_FO = 0;        //constant for original frame pointer frame offset
    private static final int RET_FO = -1;       //constant for return frame offset
    private static final int INIT_FO = -2;      //constant for start of data frame offset
    private static final int MAX = 1023;

    private static boolean traceCode = true;    //use for debugging, can optionally turn off if really desired

    private static int emitLoc = 0;
    private static int highEmitLoc = 0;

    private static int entry;
    private static int globalOffset = 0;
    private static int frameOffset = 0;
    private static int iOffset = 0;

    private static int inputLoc;    //store input function code location
    private static int outputLoc;   //store output function code location

    /* This is the main public interface to generate TM assembly code from an abstract syntax tree (DecList),
       assuming valid syntax and semantics */
    public static void genCodeCM (DecList tree) {
        /* Start by emitting the prelude */
        emitComment ("Standard prelude");
        emitRM ("LD", GP, 0, AC, "Load gp with maxaddress");
        emitRM ("LDA", FP, 0, GP, "Copy gp to fp");
        emitRM ("ST", AC, 0, AC, "Clear location 0");

        /* Then, emit I/O routines */
        int savedLoc = emitSkip (1);
        emitComment ("Jump around I/O functions");
        emitComment ("Code for input routine");
        inputLoc = emitLoc;
        emitRM ("ST", AC, RET_FO, FP, "Store return address");
        emitRO ("IN", AC, 0, 0, "Input");
        emitRM ("LD", PC, RET_FO, FP, "Return to caller");

        emitComment ("Code for output routine");
        outputLoc = emitLoc;
        emitRM ("ST", AC, RET_FO, FP, "Store return address");
        emitRM ("LD", AC, -2, FP, "Load output value");
        emitRO ("OUT", AC, 0, 0, "Output");
        emitRM ("LD", PC, RET_FO, FP, "Return to caller");

        /* Backpatching I/O functions */
        int savedLoc2 = emitSkip (0);
        emitBackup (savedLoc);
        emitRM_Abs ("LDA", PC, savedLoc2, "Jump around I/O code");
        emitRestore ();

        emitComment ("End of standard prelude");

        /* Traverse the abstract syntax tree now (assumed annontated) */
        while (tree != null) {
            cGen (tree.head, 0, false);
            tree = tree.tail;
        }

        /* Emit finale assembly code */
        emitComment ("Code for finale");
        emitRM ("ST", FP, globalOffset + OFP_FO, FP, "Push ofp");
        emitRM ("LDA", FP, globalOffset, FP, "Push frame");
        emitRM ("LDA", AC, 1, PC, "Load ac with ret ptr");
        emitRM_Abs ("LDA", PC, entry, "Jump to main loc");
        emitRM ("LD", FP, OFP_FO, FP, "Pop frame");
        emitComment ("End of execution");
        emitRO ("HALT", 0, 0, 0, "");
    }

    /* This function serves as the main recursive entry point, where each tree traversal portion calls this function
       to correctly assess the tree node */
    private static void cGen (SynTreeCM tree, int offset, boolean isAddress) {
        if (tree != null) {
            if (tree instanceof Stmt)
                genStmt (tree, offset);
            else if (tree instanceof Exp)
                genExp (tree, offset, isAddress);
            else if (tree instanceof Dec)
                genDec (tree, offset);
            else
                emitComment ("BUG: missing case in cGen");
        }
    }

    /* With statement-like Exp (encapsulated in a Stmt Wrapper class), a specific sequence of code is generated */
    private static void genStmt (SynTreeCM tree, int offset) {
        if (tree != null) {
            if (tree instanceof IfExp) {
                IfExp treeNode = (IfExp)tree;
                emitComment ("-> if");

                cGen(treeNode.test, offset, false);    //work through test and send result to AC

                /* Skip past the instruction that will jump to an else if it exists */
                int elseJumpLoc = emitSkip (1);
                emitComment ("Jump to else here");

                cGen (treeNode.thenExp, offset, false);

                /* Backpatch the if skip to the end */
                int endJumpLoc = emitSkip (1);        //store the backpatch jump past else
                emitBackup (elseJumpLoc);
                emitRM_Abs ("JEQ", AC, endJumpLoc + 1, "If: Jump to else");
                emitRestore ();

                if (treeNode.elseExp != null) {
                    cGen (treeNode.elseExp, offset, false);
                }

                /* Backpatch the end skip jump */
                int endLoc = emitSkip (0);
                emitBackup (endJumpLoc);
                emitRM_Abs ("LDA", PC, endLoc, "If: Jump to end");
                emitRestore ();

                emitComment ("<- if");
            }
            else if (tree instanceof WhileExp) {
                WhileExp treeNode = (WhileExp)tree;
                emitComment ("-> while");

                /* Store start of loop location */
                int loopBackLoc = emitSkip (0);
                emitComment ("Jump after body comes back here");

                cGen(treeNode.test, offset, false);    //work through test and send result to AC

                /* Skip past the instruction that will jump to the end of while */
                int endWhileLoc = emitSkip (1);
                emitComment ("Jump to while end here");

                cGen(treeNode.body, offset, false);

                emitRM_Abs ("LDA", PC, loopBackLoc, "While: absolute jmp to test");

                /* Backpatch the while skip to the end */
                int savedLoc2 = emitSkip (0);
                emitBackup (endWhileLoc);
                emitRM_Abs ("JEQ", AC, savedLoc2, "While: Jump to end");
                emitRestore ();

                emitComment ("<- while");
            }
            else if (tree instanceof ReturnExp) {
                ReturnExp treeNode = (ReturnExp)tree;
                emitComment ("-> return");

                /* Process the return expression then return to caller */
                cGen(treeNode.exp, offset, false);
                emitRM ("LD", PC, RET_FO, FP, "Return to caller");

                emitComment ("<- return");
            }
            else if (tree instanceof CompoundExp) {
                CompoundExp treeNode = (CompoundExp)tree;
                emitComment ("-> compound statements");

                /* Cycle through the variables and load them into the frame */
                VarDecList vars = treeNode.decs;
                while (vars != null) {
                    cGen (vars.head, offset, false);
                    vars = vars.tail;
                }

                /* Cycle through the statements and generate code */
                ExpList stmts = treeNode.exps;
                while (stmts != null) {
                    cGen (stmts.head, offset, false);
                    stmts = stmts.tail;
                }

                emitComment ("<- compound statements");
            }
            else
                emitComment ("BUG: missing case in genStmt");
        }
    }

    /* For any declaration, code generated refers to its storage in the stack and eventual usage */
    private static void genDec (SynTreeCM tree, int offset) {
        if (tree != null) {
            if (tree instanceof VarDec) {
                VarDec treeNode = (VarDec)tree;
                emitComment ("Processing variable: " + treeNode.name);

                int varNestLevel = treeNode.nestLevel;
                if (varNestLevel == 0) {                    //if global variable, move globalOffset along after reserving var's location(s)
                    treeNode.offset = globalOffset;

                    if (treeNode instanceof ArrayDec) {
                        ArrayDec array = (ArrayDec)treeNode;
                        int arraySize = array.size.value;
                        globalOffset -= arraySize;          //move global offset pointer down by number of elements
                        frameOffset -= arraySize;           //in the global scope, frameOffset from gp, just like globalOffset
                    }
                    else {
                        globalOffset--;
                        frameOffset--;
                    }
                }
                else {                                      //if local variable, move frameOffset down
                    treeNode.offset = frameOffset;

                    if (treeNode instanceof ArrayDec) {
                        ArrayDec array = (ArrayDec)treeNode;
                        int arraySize = array.size.value;
                        frameOffset -= arraySize;           //move frame offset pointer down by number of elements
                    }
                    else
                        frameOffset--;
                }

                treeNode.isAddress = false;

                emitComment ("<- var dec: " + treeNode.name);
            }
            else if (tree instanceof FunctionDec) {
                FunctionDec treeNode = (FunctionDec)tree;
                emitComment ("Processing function: " + treeNode.func);

                /* Allow for the function skip here */
                int endFuncLoc = emitSkip (1);
                emitComment ("Jump around function body here");

                int oldFrameOffset = frameOffset;
                frameOffset = INIT_FO;                     //store and reset frame offset to area past return addr

                treeNode.funcAddr = highEmitLoc;           //store the absolute address of the function (in instruction space)
                entry = treeNode.funcAddr;                 //repeatedly set entry to the last function declaration
                emitRM ("ST", AC, RET_FO, FP, "Store return");    //RET_FO (return Frame Offset) = -1 always

                /* Reserve paramater space */
                VarDecList params = treeNode.params;
                while (params != null) {                   //all parameters are of 1 space (including array references)
                    emitComment ("Processing parameter: " + params.head.name);

                    //set parameter's location and move frameOffset down of this function
                    params.head.offset = frameOffset;
                    frameOffset--;
                    params.head.isAddress = true;
                    params = params.tail;
                }

                /* Generate code for body statements */
                cGen (treeNode.body, offset, false);
                emitRM ("LD", PC, RET_FO, FP, "Return to caller");

                frameOffset = oldFrameOffset;              //restore frame offset

                /* Backpatch the while skip to the end */
                int endJumpLoc = emitSkip (0);
                emitBackup (endFuncLoc);
                emitRM_Abs ("LDA", PC, endJumpLoc, "Jump around fn body: " + treeNode.func);
                emitRestore ();

                emitComment ("<- func dec: " + treeNode.func);
            }
            else
                emitComment ("BUG: missing case in genDec");
        }
    }

    /* For expressions (other than Stmt), code generated relates to variable usage and storage, and operations */
    private static void genExp (SynTreeCM tree, int offset, boolean isAddress) {
        if (tree != null) {
            if (tree instanceof NilExp) {
                //do nothing with a NilExp
                emitComment ("-> nil ");    emitComment ("<- nil");
            }
            else if (tree instanceof IntExp) {
                IntExp treeNode = (IntExp)tree;
                emitComment ("-> constant");
                emitRM ("LDC", AC, treeNode.value, AC, "Load const");    //store constant to AC
                emitComment ("<- constant");
            }
            else if (tree instanceof VarExp) {
                VarExp treeNode = (VarExp)tree;
                emitComment ("-> id");
                emitComment ("Looking up id: " + treeNode.variable.name);

                VarDec varRef = treeNode.variable.varDecRef;

                if (treeNode.variable instanceof SimpleVar) {    //if simpleVar->simpleDec, or simpleVar->arrayDec (as whole array ref)
                    if (varRef instanceof SimpleDec) {
                        if (varRef.nestLevel == 0)               //if a global scope variable, offset from GP, else offset from FP
                            emitRM ("LD", AC, varRef.offset, GP, "Load global id value: " + varRef.name);
                        else
                            emitRM ("LD", AC, varRef.offset, FP, "Load local id value: " + varRef.name);
                    }
                    else {
                        if (varRef.isAddress == true) {
                            if (varRef.nestLevel == 0)
                                emitRM ("LD", AC, varRef.offset, GP, "Load global id address (array - simple): " + varRef.name);
                            else
                                emitRM ("LD", AC, varRef.offset, FP, "Load local id address (array - simple): " + varRef.name);
                        }
                        else {
                            if (varRef.nestLevel == 0)
                                emitRM ("LDA", AC, varRef.offset, GP, "Load global id address (array - simple): " + varRef.name);
                            else
                                emitRM ("LDA", AC, varRef.offset, FP, "Load local id address (array - simple): " + varRef.name);
                        }
                    }
                }
                else {                                           //if indexVar->arrayDec (is semantic error for indexVar->simpleDec); check for out-of-bounds reference
                    ArrayDec arrayRef = (ArrayDec)varRef;

                    if (varRef.isAddress == true) {
                        if (varRef.nestLevel == 0)
                            emitRM ("LD", AC, varRef.offset, GP, "Load global id address (array): " + varRef.name);
                        else
                            emitRM ("LD", AC, varRef.offset, FP, "Load local id address (array): " + varRef.name);
                    }
                    else {
                        if (varRef.nestLevel == 0)
                            emitRM ("LDA", AC, varRef.offset, GP, "Load global id address (array): " + varRef.name);
                        else
                            emitRM ("LDA", AC, varRef.offset, FP, "Load local id address (array): " + varRef.name);
                    }

                    /* store base address and investigate index expression */
                    emitRM ("ST", AC, frameOffset, FP, "Store array addr");
                    cGen (((IndexVar)treeNode.variable).index, offset, false);

                    emitRM ("JLT", AC, 1, PC, "Halt if subscript < 0");                    //skip past jump instruction if bad subscript
                    emitRM ("LDA", PC, 3, PC, "Absolute jump if >= 0");                    //skip over output and halt, continue check
                    emitRM ("LDC", AC, -10000, AC, "Load -10000 for subscript < 0");
                    emitRO ("OUT", AC, 0, 0, "Output -10000 for subscript < 0");
                    emitRO ("HALT", 0, 0, 0, "Halt if subscript < 0");                     //halt

                    if (arrayRef.size != null) {
                        emitRM ("LDC", AC_1, arrayRef.size.value, AC_1, "Load array size to AC_1");    //begin check for out of bounds (+max)
                        emitRO ("SUB", AC_1, AC_1, AC, "Assess if index >= size (<= 0) on AC_1");
                        emitRM ("JLE", AC_1, 1, PC, "Halt if subscript >= size");              //skip past jump instruction if bad subscript
                        emitRM ("LDA", PC, 3, PC, "Absolute jump if < 0");                     //skip over output and halt, is now valid
                        emitRM ("LDC", AC, -20000, AC, "Load -20000 for subscript >= size");
                        emitRO ("OUT", AC, 0, 0, "Output -20000 for subscript >= size");
                        emitRO ("HALT", 0, 0, 0, "Halt if subscript >= size");                     //halt
                    }

                    if (varRef.isAddress == true) {
                        if (varRef.nestLevel == 0)
                            emitRM ("LD", AC_1, arrayRef.offset, GP, "Load array base addr to AC1");
                        else
                            emitRM ("LD", AC_1, arrayRef.offset, FP, "Load array base addr to AC1");
                    }
                    else {
                        if (varRef.nestLevel == 0)
                            emitRM ("LDA", AC_1, arrayRef.offset, GP, "Load array base addr to AC1");
                        else
                            emitRM ("LDA", AC_1, arrayRef.offset, FP, "Load array base addr to AC1");
                    }
                    emitRO ("SUB", AC, AC_1, AC, "Base is at top of array");
                    emitRM ("LD", AC, 0, AC, "Load value at array index");
                }

                emitComment ("<- id");
            }
            else if (tree instanceof OpExp) {
                OpExp treeNode = (OpExp)tree;
                emitComment ("-> op");

                /* Evaluate left side and store value as a temporary variable, once fully resolved */
                cGen(treeNode.left, offset, false);

                /* store assign location to temp area, push frame offset past it, and return once right is ready */
                emitRM ("ST", AC, frameOffset, FP, "Op: push left");
                frameOffset--;

                /* Evaluate right side, will be stored in AC, once fully resolved */
                cGen(treeNode.right, offset, false);

                /* Load left side to AC_1, then perform operation */
                /* If the operation is a comparison, load 0 or 1 depending on success (1) or failure (0) */
                frameOffset++;
                emitRM ("LD", AC_1, frameOffset, FP, "Op: load left");

                switch (treeNode.op) {
                    case OpExp.PLUS:
                        emitRO ("ADD", AC, AC_1, AC, "Op: +");
                        break;

                    case OpExp.MINUS:
                        emitRO ("SUB", AC, AC_1, AC, "Op: -");
                        break;

                    case OpExp.MUL:
                        emitRO ("MUL", AC, AC_1, AC, "Op: *");
                        break;

                    case OpExp.DIV:
                        emitRO ("DIV", AC, AC_1, AC, "Op: /");
                        break;

                    case OpExp.EQ:
                        emitRO ("SUB", AC, AC_1, AC, "Op: ==");
                        emitRM ("JEQ", AC, 2, PC, "Go to true case");
                        emitRM ("LDC", AC, 0, AC, "Load 0 to AC - false case");
                        emitRM ("LDA", PC, 1, PC, "Unconditional jmp past true case");
                        emitRM ("LDC", AC, 1, AC, "Load 1 to AC - true case");
                        break;

                    case OpExp.NE:
                        emitRO ("SUB", AC, AC_1, AC, "Op: !=");
                        emitRM ("JNE", AC, 2, PC, "Go to true case");
                        emitRM ("LDC", AC, 0, AC, "Load 0 to AC - false case");
                        emitRM ("LDA", PC, 1, PC, "Unconditional jmp past true case");
                        emitRM ("LDC", AC, 1, AC, "Load 1 to AC - true case");
                        break;

                    case OpExp.LT:
                        emitRO ("SUB", AC, AC_1, AC, "Op: <");
                        emitRM ("JLT", AC, 2, PC, "Go to true case");
                        emitRM ("LDC", AC, 0, AC, "Load 0 to AC - false case");
                        emitRM ("LDA", PC, 1, PC, "Unconditional jmp past true case");
                        emitRM ("LDC", AC, 1, AC, "Load 1 to AC - true case");
                        break;

                    case OpExp.LE:
                        emitRO ("SUB", AC, AC_1, AC, "Op: <=");
                        emitRM ("JLE", AC, 2, PC, "Go to true case");
                        emitRM ("LDC", AC, 0, AC, "Load 0 to AC - false case");
                        emitRM ("LDA", PC, 1, PC, "Unconditional jmp past true case");
                        emitRM ("LDC", AC, 1, AC, "Load 1 to AC - true case");
                        break;

                    case OpExp.GT:
                        emitRO ("SUB", AC, AC_1, AC, "Op: >");
                        emitRM ("JGT", AC, 2, PC, "Go to true case");
                        emitRM ("LDC", AC, 0, AC, "Load 0 to AC - false case");
                        emitRM ("LDA", PC, 1, PC, "Unconditional jmp past true case");
                        emitRM ("LDC", AC, 1, AC, "Load 1 to AC - true case");
                        break;

                    case OpExp.GE:
                        emitRO ("SUB", AC, AC_1, AC, "Op: >=");
                        emitRM ("JGE", AC, 2, PC, "Go to true case");
                        emitRM ("LDC", AC, 0, AC, "Load 0 to AC - false case");
                        emitRM ("LDA", PC, 1, PC, "Unconditional jmp past true case");
                        emitRM ("LDC", AC, 1, AC, "Load 1 to AC - true case");
                        break;

                    default:
                        emitComment ("BUG: missing case in OpExp");
                }

                emitComment ("<- op");
            }
            else if (tree instanceof CallExp) {
                CallExp treeNode = (CallExp)tree;
                emitComment ("-> call of function: " + treeNode.func);

                /* Pass in the arguments into the upcoming new frame */
                int oldFrameOffset = frameOffset;
                frameOffset = frameOffset + INIT_FO;

                int argsNum = 0;
                ExpList args = treeNode.args;
                while (args != null) {
                    cGen (args.head, offset, false);
                    emitRM ("ST", AC, frameOffset, FP, "Pass arg #" + (argsNum + 1));
                    frameOffset--;
                    argsNum++;
                    args = args.tail;
                }

                frameOffset = oldFrameOffset;    //restore frame offset

                /* Emit code for call sequence */
                int entryPoint;
                emitRM ("ST", FP, frameOffset + OFP_FO, FP, "Store current fp");
                emitRM ("LDA", FP, frameOffset, FP, "Push new frame");
                emitRM ("LDA", AC, 1, PC, "Save return addr in ac");
                if (treeNode.func.compareTo("input") == 0)           //custom load input/output code location
                    entryPoint = 4;
                else if (treeNode.func.compareTo("output") == 0)
                    entryPoint = 7;
                else
                    entryPoint = treeNode.funcToCall.funcAddr;
                emitRM_Abs ("LDA", PC, entryPoint, "Jump to function: " + treeNode.func);

                emitRM ("LD", FP, OFP_FO, FP, "Pop current frame");
                emitComment ("<- call");
            }
            else if (tree instanceof AssignExp) {
                AssignExp treeNode = (AssignExp)tree;
                emitComment ("-> assign");
                emitComment ("Looking up id: " + treeNode.lhs.name);

                /* Grab lhs variable first */
                VarDec varRef = treeNode.lhs.varDecRef;

                if (treeNode.lhs instanceof SimpleVar) {    //if simpleVar->simpleDec, or simpleVar->arrayDec (as whole array ref (*?))
                    if (varRef.nestLevel == 0)              //load address of var to be used later
                        emitRM ("LDA", AC, varRef.offset, GP, "Load global id address (simple): " + varRef.name);
                    else
                        emitRM ("LDA", AC, varRef.offset, FP, "Load local id address (simple): " + varRef.name);
                }
                else {                                      //if indexVar->arrayDec (is semantic error for indexVar->simpleDec); check for out-of-bounds reference
                    ArrayDec arrayRef = (ArrayDec)varRef;

                    if (varRef.isAddress == true) {
                        if (varRef.nestLevel == 0)
                            emitRM ("LD", AC, varRef.offset, GP, "Load global id address (array - assign): " + varRef.name);
                        else
                            emitRM ("LD", AC, varRef.offset, FP, "Load local id address (array - assign): " + varRef.name);
                    }
                    else {
                        if (varRef.nestLevel == 0)
                            emitRM ("LDA", AC, varRef.offset, GP, "Load global id address (array - assign): " + varRef.name);
                        else
                            emitRM ("LDA", AC, varRef.offset, FP, "Load local id address (array - assign): " + varRef.name);
                    }

                    /* store base address and investigate index expression */
                    emitRM ("ST", AC, frameOffset, FP, "Store array addr");
                    cGen (((IndexVar)treeNode.lhs).index, offset, false);

                    emitRM ("JLT", AC, 1, PC, "Halt if subscript < 0");                    //skip to past jump instruction
                    emitRM ("LDA", PC, 3, PC, "Absolute jump if >= 0");                    //skip over output and halt, continue check
                    emitRM ("LDC", AC, -10000, AC, "Load -10000 for subscript < 0");
                    emitRO ("OUT", AC, 0, 0, "Output -10000 for subscript < 0");
                    emitRO ("HALT", 0, 0, 0, "Halt if subscript < 0");                     //halt

                    if (arrayRef.size != null) {
                        emitRM ("LDC", AC_1, arrayRef.size.value, AC_1, "Load array size to AC_1");    //begin check for out of bounds (+max)
                        emitRO ("SUB", AC_1, AC_1, AC, "Assess if index >= size (<= 0) on AC_1");
                        emitRM ("JLE", AC_1, 1, PC, "Halt if subscript >= size");              //skip to past jump instruction
                        emitRM ("LDA", PC, 3, PC, "Absolute jump if < 0");                    //skip over output and halt, continue check
                        emitRM ("LDC", AC, -20000, AC, "Load -20000 for subscript >= size");
                        emitRO ("OUT", AC, 0, 0, "Output -20000 for subscript >= size");
                        emitRO ("HALT", 0, 0, 0, "Halt if subscript >= size");                     //halt
                    }

                    if (varRef.isAddress == true) {
                        if (varRef.nestLevel == 0)
                            emitRM ("LD", AC_1, arrayRef.offset, GP, "Load array base addr to AC1");
                        else
                            emitRM ("LD", AC_1, arrayRef.offset, FP, "Load array base addr to AC1");
                    }
                    else {
                        if (varRef.nestLevel == 0)
                            emitRM ("LDA", AC_1, arrayRef.offset, GP, "Load array base addr to AC1");
                        else
                            emitRM ("LDA", AC_1, arrayRef.offset, FP, "Load array base addr to AC1");
                    }
                    emitRO ("SUB", AC, AC_1, AC, "Base is at top of array");
                    emitRM ("LDA", AC, 0, AC, "Store addr to be assigned to");
                }

                /* store assign location to temp area, push frame offset past it, and return once assignment is ready */
                emitRM ("ST", AC, frameOffset, FP, "Assign: push left");
                frameOffset--;

                /* Calculate right hand side and store result in AC */
                cGen (treeNode.rhs, offset, false);

                frameOffset++;    //reset frame offset back to where temp assign location is
                emitRM ("LD", AC_1, frameOffset, FP, "Assign: load left");
                emitRM ("ST", AC, 0, AC_1, "Assign: store value");

                emitComment ("<- assign");
            }
            else
                emitComment ("BUG: missing case in genExp");
        }
    }

    /* The following set of functions relate to the actual emitting of TM assemble code (to System.out) */
    /* The emitCode function emits a complete String instruction (assumed formatted) */
    private static void emitCode (String inst) {
        System.out.println(inst);
    }

    /* The emitComment function emits a comment String on its own line */
    private static void emitComment (String comment) {
        System.out.println("* " + comment);
    }

    /* The emitSkip function skips a deteremined amount of instruction labels to be backpatched later */
    private static int emitSkip (int distance) {
        int i = emitLoc;
        emitLoc += distance;
        if (highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
        return i;
    }

    /* The emitBackup function reverts the emitting location to a designated number (for backpatching) */
    private static void emitBackup (int loc) {
        if (loc > highEmitLoc)
            emitComment ("BUG in emitBackup");
        emitLoc = loc;
    }

    /* The emitRestore function reverts the emitting location to the current highest instruction point
       (for backpatching 'recovery') */
    private static void emitRestore () {
        emitLoc = highEmitLoc;
    }

    /* This function emits Register Only (RO) instructions of the form 'op r,s,t' (with optional comment component) */
    private static void emitRO (String op, int r, int s, int t, String c) {
        String emitString = String.format("%3d:  %5s  %d,%d,%d ", emitLoc, op, r, s, t);
        ++emitLoc;
        if (traceCode == true)
            emitString = emitString + "\t" + c;
        if (highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
        emitCode (emitString);
    }

    /* This function emits Register Memory (RM) instructions of the form 'op r,d(s)' (with optional comment component) */
    private static void emitRM (String op, int r, int d, int s, String c) {
        String emitString = String.format("%3d:  %5s  %d,%d(%d) ", emitLoc, op, r, d, s);
        ++emitLoc;
        if (traceCode == true)
            emitString = emitString + "\t" + c;
        if (highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
        emitCode (emitString);
    }

    /* This function emits RM instructions that load absolute (address) values to registers (with optional comment component) */
    private static void emitRM_Abs (String op, int r, int a, String c) {
        String emitString = String.format("%3d:  %5s  %d,%d(%d) ", emitLoc, op, r, a - (emitLoc + 1), PC);
        ++emitLoc;
        if (traceCode == true)
            emitString = emitString + "\t" + c;
        if (highEmitLoc < emitLoc)
            highEmitLoc = emitLoc;
        emitCode (emitString);
    }
}

