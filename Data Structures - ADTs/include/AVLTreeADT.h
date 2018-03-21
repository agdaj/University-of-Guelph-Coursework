/*****************************
Name: Jireh Agda
Date Last Modified: 2014 08 02
AVL Tree ADT Header
*****************************/

/*This is the header file for the AVL Tree ADT*/
/*The functions in this header file are divided into wrapper functions (the functions the user of the ADT can use) at the top
  and the helper operations for these wrapper functions at the bottom*/

#ifndef AVLTreeADT_h
#define AVLTreeADT_h

/*This struct will represent a node on the binary AVL tree, with a void pointer for data, an int to keep track
  of balancing and a left and right pointer to other nodes*/
struct AVLTreeNode
{
    void * nodeData;
    int balanceCount;
    struct AVLTreeNode * left;
    struct AVLTreeNode * right;
};
typedef struct AVLTreeNode AVLTreeNode;

/*The AVL Tree struct will contain the tree itself and the compare and destroy functions needed for general operation of the ADT*/
struct AVLTree
{
    AVLTreeNode * root;
    int (* compareData) (void *, void *);
    void (* destroyData) (void *);
};
typedef struct AVLTree AVLTree;

/*The typedef helps define a more obvious boolean data that is defined with true and false as 1 and 0 respectively*/
typedef int bool;
enum {false, true};

/*AVL Tree ADT Wrapper Functions*/
/*Please use these functions when using the AVL Tree ADT*/

/****
Preconditions: A working compare and destroy function for the struct of choice are passed in
Postconditions: An AVL Tree is created and initialized with a NULL root, or an error is displayed
****/
AVLTree * createAVLTree (int (* compareFunction) (void *, void *), void (* destroyFunction) (void *));

/****
Preconditions: An AVL Tree is initialized and passed in
Postconditions: The AVL Tree is freed in memory, or an error is displayed
****/
void destroyAVLTree (AVLTree * destroyTree);

/****
Preconditions: An AVL Tree and data struct is initialized and passed in
Postconditions: The data struct is added to the AVL Tree while maintaining balance, or an error is displayed
****/
void addToAVLTree (AVLTree * dataToTree, void * newData);

/****
Preconditions: An AVL Tree and data struct is initialized and passed in
Postconditions: The data struct passed in is used to identify and remove the node in the AVL Tree while maintaining balance, or an error is displayed
****/
void removeFromAVLTree (AVLTree * dataFromTree, void * removeData);

/****
Preconditions: An AVL Tree and data struct is initialized and passed in
Postconditions: The data struct passed in is used to search the AVL tree and to return whether or not the data struct is in the AVL Tree, or an error is displayed
****/
bool findInAVLTree (AVLTree * findInTree, void * findData);

/****
Preconditions: An initialized AVL Tree with a left child node is passed in
Postconditions: An AVL Tree is created and initialized with the root being the parameter's left root child, or an error is displayed
NOTE: To free AVL Trees made with this function, simply free the pointer, and make sure to fully destroy the original AVL Tree
****/
AVLTree * getLeftSubtree (AVLTree * treeSource);

/****
Preconditions: An initialized AVL Tree with a right child node is passed in
Postconditions: An AVL Tree is created and initialized with the root being the parameter's right root child, or an error is displayed
NOTE: To free AVL Trees made with this function, simply free the pointer, and make sure to fully destroy the original AVL Tree
****/
AVLTree * getRightSubtree (AVLTree * parentTree);

/****
Preconditions: An initialized AVL Tree with data at the root node is passed in
Postconditions: The pointer to the data of the root node is returned, or an error is displayed
****/
void * getRootData (AVLTree * theTree);

/****
Preconditions: An initialized AVL Tree is passed in
Postconditions: The function will return true/false depending if the given tree is empty or not, or an error is displayed (true is returned with the error)
****/
bool isAVLTreeEmpty (AVLTree * emptyOrNotTree);



/*AVL Tree ADT Helper Operations*/
/*Please do not use these functions individually*/

/****
Preconditions: An AVL Tree with an initialized root and compare function and an initialized data struct are passed in
Postconditions: The data struct is added to the AVL Tree while maintaining balance
****/
AVLTreeNode * insertData (AVLTreeNode * currentRoot, void * toBeAdded, int (* comparer) (void *, void *));

/****
Preconditions: An AVL Tree with an initialized root, compare and destroy function and an initialized data struct are passed in
Postconditions: The data struct is used to identify and remove the node in the AVL Tree while maintaining balance
****/
AVLTreeNode * deleteData (AVLTreeNode * root, void * toBeRemoved, int (* comparing) (void *, void *), void (* destroyer) (void *));

/****
Preconditions: An AVL Tree node is initialized and passed in
Postconditions: The difference in heights (balance counts) between the node's children is returned
****/
int diffBalanceCount (AVLTreeNode * nodeToAnalyze);

/****
Preconditions: An AVL Tree node is initialized and passed in
Postconditions: The maximum height (balance count) of the node's children is returned
****/
int maxBalanceCount (AVLTreeNode * nodeToModify);

/****
Preconditions: An AVL Tree node with an imbalance on the right side with a right child is passed in
Postconditions: The ADT Tree at the node is balanced with a rotation
****/
AVLTreeNode * rotateLeftWithRightChild (AVLTreeNode * usedToBeRoot);

/****
Preconditions: An AVL Tree node with an imbalance on the left side with a left child is passed in
Postconditions: The ADT Tree at the node is balanced with a rotation
****/
AVLTreeNode * rotateRightWithLeftChild (AVLTreeNode * formerRoot);

/****
Preconditions: An AVL Tree node with an imbalance on the right side with a left child is passed in
Postconditions: The ADT Tree at the node is balanced with two rotations
****/
AVLTreeNode * doubleRotateWithRightChild (AVLTreeNode * pastRoot);

/****
Preconditions: An AVL Tree node with an imbalance on the left side with a right child is passed in
Postconditions: The ADT Tree at the node is balanced with two rotations
****/
AVLTreeNode * doubleRotateWithLeftChild (AVLTreeNode * oldRoot);

/****
Preconditions: An initialized AVL Tree node is passed in
Postconditions: The AVL Tree node at the most left (the minimum) is returned
****/
AVLTreeNode * findMinimum (AVLTreeNode * rightRoot);

/****
Preconditions: An AVL Tree with an initialized root and compare function and an initialized data struct are passed in
Postconditions: The data struct is found or not within the AVL Tree and returns accordingly
****/
bool findData (AVLTreeNode * treeRoot, void * toBeFound, int (* comparison) (void *, void *));

/****
Preconditions: An AVL Tree with an initialized root and destroy function is passed in
Postconditions: The data in the AVL Tree is recursively destroyed from memory
****/
void completeDestroy (AVLTreeNode * rootToBeDestroyed, void (* howToDestroy) (void *));

#endif
