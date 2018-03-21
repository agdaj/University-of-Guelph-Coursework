/*****************************************
Name: Jireh Agda       Student ID: 0795472
Date Last Modified: 2015 04 02
JNI: Dialogc (Java) + ParameterManager (C)
*****************************************/

#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "Dialogc.h"
#include "ParameterManager.h"

#define COMMENT_CHAR '#'    /*Assumes '#' is the designated comment character*/
#define FALSE_IF_TRUE(error) if (error) return false    /*A Macro to determine if the following condition (an error) is true, will return false (from Boolean ADT)*/

ParameterManager * initPM = NULL;
ParameterManager * specificPM = NULL;

/****
Preconditions: None
Postconditions: Returns the success (1) or failure (0) condition according to the success of the initialization
****/
JNIEXPORT jint JNICALL Java_Dialogc_initPManager (JNIEnv * javaEnv, jclass javaClass)
{
    int manageCheck;

    initPM = PM_create (5);
    FALSE_IF_TRUE(initPM == NULL);

    /*Manages the three expected parameters at initPM first*/
    manageCheck = PM_manage (initPM, "title", STRING_TYPE, 1);
    FALSE_IF_TRUE(manageCheck == 0);
    manageCheck = PM_manage (initPM, "fields", LIST_TYPE, 1);
    FALSE_IF_TRUE(manageCheck == 0);
    manageCheck = PM_manage (initPM, "buttons", LIST_TYPE, 1);
    FALSE_IF_TRUE(manageCheck == 0);

    return (true);
}

/****
Preconditions: The initPManager has initialized initPM to be used to parse
Postconditions: A success condition (1) if the parse completes, else a false condition (0) for any point of failure (bad file format, memory failures, bad input file
****/
JNIEXPORT jint JNICALL Java_Dialogc_firstParse (JNIEnv * javaEnv, jclass javaClass, jstring filePath)
{
    const char * pathName;
    FILE * inputFile;
    int successCheck;

    /*Converts the jstring to a const char * to be used as the actual argument to open a file*/
    pathName = (* javaEnv)->GetStringUTFChars (javaEnv, filePath, NULL);
    FALSE_IF_TRUE(pathName == NULL);

    inputFile = fopen (pathName, "r");
    (* javaEnv)->ReleaseStringUTFChars (javaEnv, filePath, pathName);    /*Frees the memory allocated to the const char * matching the memory of jstring*/
    FALSE_IF_TRUE (inputFile == NULL);

    successCheck = PM_parseFrom (initPM, inputFile, COMMENT_CHAR);
    fclose (inputFile);
    FALSE_IF_TRUE (successCheck == 0);

    return (true);
}

/****
Preconditions: The firstParse is successful
Postconditions: A success condition (1) if the parse completes, else a false condition (0) for any point of failure (bad file format, memory failures, bad input file)
****/
JNIEXPORT jint JNICALL Java_Dialogc_secondParse (JNIEnv * javaEnv, jclass javaClass, jstring filePath)
{
    char * componentName;
    const char * pathName;
    FILE * inputFile;
    int checkSuccess;
    union param_value compNames;

    specificPM = PM_create (19);
    FALSE_IF_TRUE(specificPM == NULL);

    checkSuccess = PM_hasValue (initPM, "fields");
    FALSE_IF_TRUE (checkSuccess == 0);
    checkSuccess = PM_hasValue (initPM, "buttons");
    FALSE_IF_TRUE (checkSuccess == 0);

    /*Re-manages the first three parameters to be able to read them again (first set will be read after this)*/
    checkSuccess = PM_manage (specificPM, "title", STRING_TYPE, 1);
    FALSE_IF_TRUE(checkSuccess == 0);
    checkSuccess = PM_manage (specificPM, "fields", LIST_TYPE, 1);
    FALSE_IF_TRUE(checkSuccess == 0);
    checkSuccess = PM_manage (specificPM, "buttons", LIST_TYPE, 1);
    FALSE_IF_TRUE(checkSuccess == 0);

    /*Manages new parameters from the fields list*/
    compNames = PM_getValue (initPM, "fields");
    componentName = PL_next (compNames.list_val);
    while (componentName != NULL)
    {
        checkSuccess = PM_manage (specificPM, componentName, STRING_TYPE, 1);
        FALSE_IF_TRUE(checkSuccess == 0);

        componentName = PL_next (compNames.list_val);
    }

    /*Manages new parameters from the buttons list unless they are a reserved button name, where they will simply be place holders to be used later*/
    compNames = PM_getValue (initPM, "buttons");
    componentName = PL_next (compNames.list_val);
    while (componentName != NULL)
    {
        if (!(strcmp (componentName, "ADD") == 0 || strcmp (componentName, "DELETE") == 0 || strcmp (componentName, "UPDATE") == 0 || strcmp (componentName, "QUERY") == 0))
        {
            checkSuccess = PM_manage (specificPM, componentName, STRING_TYPE, 1);
            FALSE_IF_TRUE(checkSuccess == 0);
        }

        componentName = PL_next (compNames.list_val);
    }

    /*Converts the jstring to a const char * to be used as the actual argument to open a file*/
    pathName = (* javaEnv)->GetStringUTFChars (javaEnv, filePath, NULL);
    FALSE_IF_TRUE(pathName == NULL);

    inputFile = fopen (pathName, "r");
    (* javaEnv)->ReleaseStringUTFChars (javaEnv, filePath, pathName);     /*Frees the memory allocated to the const char * matching the memory of jstring*/
    FALSE_IF_TRUE (inputFile == NULL);

    checkSuccess = PM_parseFrom (specificPM, inputFile, COMMENT_CHAR);
    fclose (inputFile);
    FALSE_IF_TRUE(checkSuccess == 0);

    return (true);
}

/****
Preconditions: The second parse through of a .config file has been completed and the title value exists
Postconditions: The value of the title of the generated dialog is returned, else NULL if parse failed and function was called or Java<->C data conversion fails
****/
JNIEXPORT jstring JNICALL Java_Dialogc_retrieveTitle (JNIEnv * javaEnv, jclass javaClass)
{
    char * title;
    int valueCheck;
    union param_value titleValue;

    valueCheck = PM_hasValue (specificPM, "title");
    if (valueCheck == 0)
    {
        return (NULL);
    }

    /*Retrieves the title string and converts it to a viable jstring for Dialogc*/
    titleValue = PM_getValue (specificPM, "title");
    title = titleValue.str_val;

    return ((* javaEnv)->NewStringUTF (javaEnv, title));
}

/****
Preconditions: The second parse through of a .config file has been completed and the fields value has at least one entry
Postconditions: The value of the next field name is returned, else NULL if parse failed and function was called, there are no more fields left or Java<->C data conversion fails
****/
JNIEXPORT jstring JNICALL Java_Dialogc_retrieveFieldName (JNIEnv * javaEnv, jclass javaClass)
{
    char * currentLabel;
    int valueCheck;
    union param_value labelList;

    valueCheck = PM_hasValue (specificPM, "fields");
    if (valueCheck == 0)
    {
        return (NULL);
    }

    /*Retrieves the next field string and converts it to a viable jstring for Dialogc (can result in NULL once all fields are read)*/
    labelList = PM_getValue (specificPM, "fields");
    currentLabel = PL_next (labelList.list_val);

    return ((* javaEnv)->NewStringUTF (javaEnv, currentLabel));
}

/****
Preconditions: The second parse through of a .config file has been completed and the buttons value has at least one entry
Postconditions: The value of the next button name is returned, else NULL if parse failed and function was called, there are no more buttons left or Java<->C data conversion fails
****/
JNIEXPORT jstring JNICALL Java_Dialogc_retrieveButtonName (JNIEnv * javaEnv, jclass javaClass)
{
    char * currentButton;
    int valueCheck;
    union param_value buttonList;

    valueCheck = PM_hasValue (specificPM, "buttons");
    if (valueCheck == 0)
    {
        return (NULL);
    }

    /*Retrieves the next button string and converts it to a viable jstring for Dialogc (can result in NULL once all buttons are read)*/
    buttonList = PM_getValue (specificPM, "buttons");
    currentButton = PL_next (buttonList.list_val);

    /*The button name is returned until the list above runs out, else NULL is returned*/
    return ((* javaEnv)->NewStringUTF (javaEnv, currentButton));
}

/****
Preconditions: The second parse through of a .config file has been completed and a value is associated with a field String
Postconditions: The value of the field name is returned, else NULL if parse failed and function was called or Java<->C data conversion fails
****/
JNIEXPORT jstring JNICALL Java_Dialogc_getType (JNIEnv * javaEnv, jclass javaClass, jstring fieldName)
{
    char * fieldType;
    char * nameOfField;
    const char * cFieldName;
    int ifAnyValue;
    int nameLength;
    union param_value fieldValue;

    /*Converts input field name to a readable const char **/
    cFieldName = (* javaEnv)->GetStringUTFChars (javaEnv, fieldName, NULL);
    if (cFieldName == NULL)
    {
        return (NULL);
    }

    /*Converts (copies) the const char * to a char * by way of malloc*/
    nameLength = strlen (cFieldName);
    nameOfField = malloc (sizeof (char) * (nameLength + 1));
    if (nameOfField == NULL)
    {
        return (NULL);
    }
    strcpy (nameOfField, cFieldName);

    ifAnyValue = PM_hasValue (specificPM, nameOfField);
    if (ifAnyValue == 0)
    {
        free (nameOfField);
        return (NULL);
    }

    /*Gets the value of the field and converts it to a viable jstring*/
    fieldValue = PM_getValue (specificPM, nameOfField);
    fieldType = fieldValue.str_val;
    free (nameOfField);

    return ((* javaEnv)->NewStringUTF (javaEnv, fieldType));
}

/****
Preconditions: The second parse through of a .config file has been completed and a value is associated with a button String
Postconditions: The value of the button name is returned, else NULL if parse failed and function was called or Java<->C data conversion fails
****/
JNIEXPORT jstring JNICALL Java_Dialogc_getListener (JNIEnv * javaEnv, jclass javaClass, jstring buttonName)
{
    char * buttonListener;
    char * nameOfButton;
    const char * cButtonName;
    int ifAnyValue;
    int nameLength;
    union param_value buttonValue;

    /*Converts input button name to a readable const char **/
    cButtonName = (* javaEnv)->GetStringUTFChars (javaEnv, buttonName, NULL);
    if (cButtonName == NULL)
    {
        return (NULL);
    }

    /*Converts (copies) the const char * to a char * by way of malloc*/
    nameLength = strlen (cButtonName);
    nameOfButton = malloc (sizeof (char) * (nameLength + 1));
    if (nameOfButton == NULL)
    {
        return (NULL);
    }
    strcpy (nameOfButton, cButtonName);

    ifAnyValue = PM_hasValue (specificPM, nameOfButton);
    if (ifAnyValue == 0)
    {
        free (nameOfButton);
        return (NULL);
    }

    /*Gets the value of the button and converts it to a viable jstring*/
    buttonValue = PM_getValue (specificPM, nameOfButton);
    buttonListener = buttonValue.str_val;
    free (nameOfButton);

    return ((* javaEnv)->NewStringUTF (javaEnv, buttonListener));
}

/****
Preconditions: None
Postconditions: Any ParameterManager currently in use is freed from memory and the variables are assigned back to NULL
****/
JNIEXPORT void JNICALL Java_Dialogc_cleanUpPM (JNIEnv * javaEnv, jclass javaClass)
{
    if (initPM != NULL)
    {
        PM_destroy (initPM);
    }

    if (specificPM != NULL)
    {
        PM_destroy (specificPM);
    }

    initPM = NULL;
    specificPM = NULL;

    return;
}
