/*************************************************
SynTreeCM.java
Name: Jireh Agda (0795472)
Date Created: 2017 03 03
     Last Modified: 2017 04 05
C- Abstract Syntax Tree Class (Storage + Printing)
Compiler Project - CIS*4650
*************************************************/

package AbSynCM;

abstract public class SynTreeCM {
    public int pos;

    private static final int PARAM_FLAG = 1;    //help differentiate parameters of a function to other VarDecList
    private static final int NOT_PARAM = 0;

    //indentation standard
    private static final int SPACES = 4;

    /* The following are printing-related functions */
    private static void indent (int spaces) {
        for (int i = 0; i < spaces; i++) System.out.print (" ");
    }

    /* This is the main public interface to print abstract syntax trees generated by the
       CUP parser, resulting in a DecList */
    public static void showTreeCM (DecList tree, int spaces) {
        indent (spaces);
        System.out.println ("DecList:");
        spaces += SPACES;
        while (tree != null) {
            showTree (tree.head, spaces);
            tree = tree.tail;
        }
    }

    /* For each Dec (declaration) in DecList, split printing into either FunctionDec or VarDec */
    private static void showTree (Dec tree, int spaces) {
        if (tree == null) {
            indent (spaces);
            System.out.println ("Illegal declaration");    //print illegal error, reflect parse error
        }
        else if (tree instanceof FunctionDec)
            showTree ((FunctionDec)tree, spaces);
        else if (tree instanceof VarDec)
            showTree ((VarDec)tree, spaces);
        else {
            indent (spaces);
            System.out.println ("Illegal declaration at line " + tree.pos);
        }
    }

    /* If the Dec is a FunctionDec, print FunctionDec with its name and the NameTy, VarDecList of parameters and the CompoundExp body */
    private static void showTree (FunctionDec tree, int spaces) {
        indent (spaces);
        System.out.println ("FunctionDec: " + tree.func + " - " + showType(tree.result));
        spaces += SPACES;
        showTree (tree.params, spaces, PARAM_FLAG);
        showTree (tree.body, spaces);
    }

    /* If the Dec is a VarDec, print as such (considering whether it has associated array + expression) */
    private static void showTree (VarDec tree, int spaces) {
        indent (spaces);
        if (tree == null) {
            System.out.println ("Illegal variable declaration");    //print illegal line, reflect parse error
        }
        else if (tree instanceof SimpleDec) {
            System.out.println ("SimpleDec: " + tree.name + " - " + showType(tree.typ));
        }
        else if (tree instanceof ArrayDec) {
            ArrayDec thisTree = (ArrayDec)tree;
            System.out.println ("ArrayDec: " + thisTree.name + "[] - " + showType(tree.typ));
            spaces += SPACES;
            showTree (thisTree.size, spaces);
        }
        else
            System.out.println ("Illegal variable declaration at line " + tree.pos);
    }

    /* Any reference to a variable declaration list goes here, and call the VarDec-based overloaded function above */
    private static void showTree (VarDecList tree, int spaces, int paramFlag) {
        indent (spaces);
        if (paramFlag == PARAM_FLAG)
            System.out.println ("Parameters:");
        else
            System.out.println ("VarDecList:");
        spaces += SPACES;
        while (tree != null) {
            showTree (tree.head, spaces);
            tree = tree.tail;
        }
    }

    /* Any reference to an expresion list go here, and call the relevant expression-based overloaded function below */
    private static void showTree (ExpList tree, int spaces) {
        while (tree != null) {
            showTree (tree.head, spaces);
            tree = tree.tail;
        }
    }

    /* Given an Exp (expression) decide which kind to call to print */
    private static void showTree (Exp tree, int spaces) {
        if (tree == null) {
            indent (spaces);
            System.out.println ("Illegal expression");    //print illegal line, reflect parse error
        }
        else if (tree instanceof NilExp)
            showTree ((NilExp)tree, spaces);
        else if (tree instanceof VarExp)
            showTree ((VarExp)tree, spaces);
        else if (tree instanceof IntExp)
            showTree ((IntExp)tree, spaces);
        else if (tree instanceof CallExp)
            showTree ((CallExp)tree, spaces);
        else if (tree instanceof OpExp)
            showTree ((OpExp)tree, spaces);
        else if (tree instanceof AssignExp)
            showTree ((AssignExp)tree, spaces);
        else if (tree instanceof IfExp)
            showTree ((IfExp)tree, spaces);
        else if (tree instanceof WhileExp)
            showTree ((WhileExp)tree, spaces);
        else if (tree instanceof ReturnExp)
            showTree ((ReturnExp)tree, spaces);
        else if (tree instanceof CompoundExp)
            showTree ((CompoundExp)tree, spaces);
        else {
            indent (spaces);
            System.out.println ("Illegal expression at line " + tree.pos);
        }
    }

    /* Nil expressions just print NilExp (no correpsonding info stored) */
    private static void showTree (NilExp tree, int spaces) {
        indent (spaces);
        System.out.println ("NilExp;");
    }

    /* Variable expressions print VarExp and their correspoding variable parts in an indented line(s) */
    private static void showTree (VarExp tree, int spaces) {
        indent (spaces);
        System.out.print ("VarExp: ");

        if (tree.variable == null) {
            System.out.println ("Illegal variable");    //print illegal line, reflect parse error
        }
        else if (tree.variable instanceof SimpleVar)
            System.out.println (tree.variable.name);
        else if (tree.variable instanceof IndexVar) {
            IndexVar thisTree = (IndexVar)tree.variable;
            System.out.println (thisTree.name + "[]");
            spaces += SPACES;
            showTree (thisTree.index, spaces);
        }
    }

    /* Int expressions just print the integer with IntExp */
    private static void showTree (IntExp tree, int spaces) {
        if (tree != null) {
            indent (spaces);
            System.out.println ("IntExp: " + tree.value);
        }
    }

    /* Call expressions print the function name and recursively call the relevant args */
    private static void showTree (CallExp tree, int spaces) {
        indent (spaces);
        System.out.println ("CallExp: " + tree.func);
        spaces += SPACES;
        showTree (tree.args, spaces);
    }

    /* Operation expressions are printed through a switch structure, and recursively call the left and right members */
    private static void showTree (OpExp tree, int spaces) {
        indent (spaces);
        System.out.print ("OpExp:");
        switch (tree.op) {
            case OpExp.PLUS:
                System.out.println (" + ");
                break;
            case OpExp.MINUS:
                System.out.println (" - ");
                break;
            case OpExp.MUL:
                System.out.println (" * ");
                break;
            case OpExp.DIV:
                System.out.println (" / ");
                break;
            case OpExp.LT:
                System.out.println (" < ");
                break;
            case OpExp.LE:
                System.out.println (" <= ");
                break;
            case OpExp.GT:
                System.out.println (" > ");
                break;
            case OpExp.GE:
                System.out.println (" >= ");
                break;
            case OpExp.EQ:
                System.out.println (" == ");
                break;
            case OpExp.NE:
                System.out.println (" != ");
                break;
            default:
                System.out.println (" Unrecognized operator at line " + tree.pos);
                System.err.println ("Error in line " + tree.pos + " : Unrecognized operator");
        }
        spaces += SPACES;
        showTree (tree.left, spaces);
        showTree (tree.right, spaces);
    }

    /* Assignment expressions print the variable name to be assigned and recursively call the rhs expression */
    private static void showTree (AssignExp tree, int spaces) {
        indent (spaces);
        System.out.println ("AssignExp: ");
        spaces += SPACES;
        showTree (tree.lhs, spaces);
        showTree (tree.rhs, spaces);
    }

    /* If expressions print IfExp and consist of a test Exp, then Exp, and else Exp if it exists */
    private static void showTree (IfExp tree, int spaces) {
        indent (spaces);
        System.out.println ("IfExp:");
        spaces += SPACES;
        showTree (tree.test, spaces);
        showTree (tree.thenExp, spaces);
        if (tree.elseExp != null)
            showTree (tree.elseExp, spaces);
    }

    /* While expressions print WhileExp and consist of a test Exp and an Exp body */
    private static void showTree (WhileExp tree, int spaces) {
        indent (spaces);
        System.out.println ("WhileExp:");
        spaces += SPACES;
        showTree (tree.test, spaces);
        showTree (tree.body, spaces);
    }

    /* Return expressions print ReturnExp and consist of a return expression */
    private static void showTree (ReturnExp tree, int spaces) {
        indent (spaces);
        System.out.println ("ReturnExp:");
        spaces += SPACES;
        showTree (tree.exp, spaces);
    }

    /* Compound expressions print CompoundExp and consist of a VarDecList of declarations and a list of Exp (ExpList) */
    private static void showTree (CompoundExp tree, int spaces) {
        indent (spaces);
        System.out.println ("Statements:");
        spaces += SPACES;
        showTree (tree.decs, spaces, NOT_PARAM);
        showTree (tree.exps, spaces);
    }

    /* Variable printing is printed, split between SimpleVar or IndexVar */
    private static void showTree (Var tree, int spaces)
    {
        indent (spaces);
        if (tree == null)
            System.out.println ("Illegal variable");
        else if (tree instanceof SimpleVar)
            System.out.println ("SimpleVar: " + tree.name);
        else if (tree instanceof IndexVar) {
            IndexVar thisTree = (IndexVar)tree;
            System.out.println ("IndexVar: " + thisTree.name + "[]");
            spaces += SPACES;
            showTree (thisTree.index, spaces);
        }
    }

    /* References to named types are returned with this function */
    private static String showType (NameTy tree) {
        if (tree == null)    return "";    //return empty on a null NameTy

        switch (tree.typ) {
            case NameTy.INT:
                return ("int");
            case NameTy.VOID:
                return ("void");
            default:
                return ("Unrecognized type at line " + tree.pos);
        }
    }
}
