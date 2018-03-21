/*****************************
Name: Jireh Agda
Student ID: 0795472
Date Last Modified: 2015 01 22
Parameter Manager Interface
*****************************/

/*This header file provides the main interface to which a user can use ParameterManager to store parameters to be read from a stream*/
/*The first set of functions outline the available functions that the interface provides to a user for use with ParameterManager and ParameterList.
  The second set of functions are helper functions to aid in the functionality of ParameterManager and ParameterList functions,
  not to be used separately*/

#ifndef ParameterManager_h
#define ParameterManager_h

#include <stdio.h>    /*Provides definition for FILE * for PM_parseFrom*/
#include "hashTableADT.h"    /*Provides definition of the HashTable stored inside ParameterManager struct*/
#include "booleanADT.h"    /*Provides definition of Boolean*/

/*Defines param_t symbolic constants that can be used to identify a parameter's value type*/
typedef enum ParameterTypes {INT_TYPE, REAL_TYPE, BOOLEAN_TYPE, STRING_TYPE, LIST_TYPE} param_t;

/*The ParameterManager struct contains a hash table to store parameters, as well as some internal information to store
  parameter details and required parameters*/
struct ParameterManager
{
    HashTable * paramHolder;
    char ** parameterNames;
    char ** requiredParameters;
    int numOfParam;
    int numOfReqParam;
};
typedef struct ParameterManager ParameterManager;

/*A data type of ParameterList is composed of a list that will contain strings and a pointer to within the list to retrieve
  strings sequentially (and a flag to determine if the iterator has been set)*/
struct ParameterList
{
    int iteratorSet;
    List * paramList;
    Node * currentNode;
};
typedef struct ParameterList ParameterList;

/*The union param_value stores the potential value types a parameter may wish to store*/
union param_value
{
    int int_val;
    float real_val;
    Boolean bool_val;
    char * str_val;
    ParameterList * list_val;
};

/*The ParameterData struct stores information regarding a parameter, its data and the state of the contents in it*/
struct ParameterData
{
    char * paramName;
    param_t paramType;
    int required;
    Boolean hasValue;
    union param_value paramValue;
};
typedef struct ParameterData ParameterData;



/*ParameterManager and ParameterList Interface*/

/****
Preconditions: Integer size is greater than 0 (positive integer)
Postconditions: A new ParameterManager is created managing no parameters; ParameterManager is returned if successful, else NULL is returned
****/
ParameterManager * PM_create (int size);

/****
Preconditions: An initialized ParameterManager is passed in
Postconditions: ParameterManager is freed from memory; 1 is returned if successful, else 0 is returned
****/
int PM_destroy (ParameterManager * p);

/****
Preconditions: An initialized ParameterManager and an open FILE * stream with expected parameter reading format are passed in
Postconditions: Parameters and their values are read from the stream and stored in ParameterManager, ignoring comments indicated with
                char up to a newline ('\n') if not nul ('\0'); 1 is returned when successful (reads all required parameters and any optional ones),
                else 0 is returned (parse error, memory unallocated)
                The ParameterManager is cleaned after if parse fails, allowing a re-read of a file
****/
int PM_parseFrom (ParameterManager * p, FILE * fp, char comment);

/****
Preconditions: An initialized ParameterManager and char * string are passed in and pname is not a duplicate parameter name
Postconditions: ParameterManager registers pname as a parameter of type ptype, and its requirement is designated as required if any number other than 0, 0 if optional;
                1 is returned upon successful registration, else 0 is returned (duplicate name, memory unallocated)
****/
int PM_manage (ParameterManager * p, char * pname, param_t ptype, int required);

/****
Preconditions: An initialized ParameterManager and char * string are passed in and pname is a registered parameter
Postconditions: If parameter pname has a value, 1 is returned, else 0 is returned (no value, unknown parameter)
****/
int PM_hasValue (ParameterManager * p, char * pname);

/****
Preconditions: An initialized ParameterManager and char * string are passed in and pname is a registered parameter with a value
Postconditions: Returns the value associated with parameter pname, result is undefined if pname is not registered or does not have a value
****/
union param_value PM_getValue (ParameterManager * p, char * pname);

/****
Preconditions: An initialized ParamaterList is passed in
Postconditions: The next string in ParameterList l is returned, else NULL is returned (no items in list, empty list)
****/
char * PL_next (ParameterList * l);



/*Helper Functions for ParameterManager and ParameterList*/

/****
Preconditions: A pointer to a valid ParameterData struct is passed in
Postconditions: The contents of a ParameterData struct is freed from memory
****/
void destroyParameterData (void * destroyParamData);

/****
Preconditions: An initialized ParameterManager with registered parameters is passed in
Postconditions: Any registered parameters are reset to have no value, freeing any memory necessary for a clean ParameterManager
****/
void cleanParameterManager (ParameterManager * toBeCleaned);

/****
Preconditions: A valid open FILE * stream is passed in
Postconditions: Any whitespace is read through up until the next non whitespace character (as determined by isspace)
****/
void whitespaceSkip (FILE * inputStream);

/****
Preconditions: A valid open FILE * stream is passed in
Postconditions: A flie stream is read through up to the newline, akin to a comment structure
****/
void commentSkip (FILE * commentStream);

/****
Preconditions: A valid open FILE * stream is passed in
Postconditions: The stream is read from until an EOF, ';' or non-whitespace character/non-comment line has been read
                The function places the last char back to the stream
****/
void endParamSkip (FILE * endStream, char commentMark);

/****
Preconditions: A valid open FILE * stream is passed in
Postconditions: A parameter name is retrieved from the stream, reading characters up to EOF, ';', whitespace or '='; a char *
                string is returned if successful, else NULL is returned (if EOF or ';' ends the read)
****/
char * getParamName (FILE * paramStream);

/****
Preconditions: A valid open FILE * stream is passed in and a minimum of 1 char pointed to by a pointer is passed in
Postconditions: A union param_value is returned with a designated type required, ignoring any cases of comments with commentChar);
                result is undefined and should not be used if goodValueFlag becomes 'n' (at spot 0)
****/
union param_value paramValueManager (param_t expectedType, FILE * valueStream, char commentChar, char * goodValueFlag);

/****
Preconditions: A valid open FILE * stream is passed in and either an INT_TYPE, REAL_TYPE or BOOLEAN_TYPE value is needed
Postconditions: A string containing the necessary value is retrieved from the stream is returned; a char * string is returned if successful,
                else NULL is returned (format error, memory unallocated, STRING_TYPE/LIST_TYPE used as argument)
****/
char * getParamValue (FILE * valueStream, param_t nonStringType);

/****
Preconditions: A valid open FILE * stream is passed in
Postconditions: A string surrounded by "" is returned; a char * string is returned if successful, else NULL is returned (parse error
                when encountering EOF or ';', memory unallocated)
****/
char * getParamString (FILE * stringStream);

/****
Preconditions: A valid open FILE * stream is passed in
Postconditions: A list of strings surrounded by "" and contained within {} is read from the stream and returned; ParameterList is
                returned is successful, else NULL is returned (parse error (early EOF/';'), memory unallocated)
****/
ParameterList * getParamList (FILE * listStream, char commentChar);

/****
Preconditions: None
Postconditions: A new ParameterList is created with no contents; ParameterList is returned if successful, else NULL is returned
****/
ParameterList * newParamList ();

/****
Preconditions: A pointer to an malloc'd char * string is passed in
Postconditions: The string is freed from memory
****/
void freeString (void * stringToBeDestroyed);

/****
Preconditions: An initialized ParamaterList is passed in
Postconditions: The ParameterList iterator is set to read contents with PL_next
****/
void setIterator (ParameterList * listOfParameters);

#endif
