/*****************************
Name: Jireh Agda
Date Last Modified: 2014 08 02
AVL Tree ADT Module
*****************************/

#include "AVLTreeADT.h"
#include <stdio.h>
#include <stdlib.h>

/*AVL Tree ADT Wrapper Functions*/

AVLTree * createAVLTree (int (* compareFunction) (void *, void *), void (* destroyFunction) (void *))
{
    AVLTree * newTree;

    if (compareFunction == NULL || destroyFunction == NULL)
    {
        printf ("Error - Compare function or destroy function passed in is NULL - Could not create AVL Tree\n");
        return (NULL);
    }
    else
    {
        /*The AVL Tree is made if possible, and intitialized*/
        newTree = malloc (sizeof (AVLTree));
        if (newTree == NULL)
        {
            printf ("Error - Could not create AVL Tree\n");
            return (NULL);
        }
        else
        {
            newTree->root = NULL;
            newTree->compareData = compareFunction;
            newTree->destroyData = destroyFunction;
            return (newTree);
        }
    }
}

void destroyAVLTree (AVLTree * destroyTree)
{
    /*If the given AVL Tree is NULL, the function will display an error and return*/
    if (destroyTree == NULL)
    {
        printf ("Error - No AVL Tree to destroy\n");
    }
    else
    {
        /*A recursive function is called to destroy the tree of data*/
        completeDestroy (destroyTree->root, destroyTree->destroyData);

        /*The main AVL Tree struct with the passed in functions are destroyed*/
        free (destroyTree);
    }

    return;
}

void addToAVLTree (AVLTree * dataToTree, void * newData)
{
    /*If the given AVL Tree or void * data is NULL, the function will display an error and return*/
    if (dataToTree == NULL || newData == NULL)
    {
        printf ("Error - Either AVL Tree or Data Parameter is NULL - No data addition will occur\n");
    }
    else
    {
        dataToTree->root = insertData (dataToTree->root, newData, dataToTree->compareData);
    }

    return;
}

void removeFromAVLTree (AVLTree * dataFromTree, void * removeData)
{
    /*If the given AVL Tree or void * data is NULL, the function will display an error and return*/
    if (dataFromTree == NULL || removeData == NULL)
    {
        printf ("Error - Either AVL Tree or Data Parameter is NULL - No data deletion will occur\n");
    }
    else
    {
        dataFromTree->root = deleteData (dataFromTree->root, removeData, dataFromTree->compareData, dataFromTree->destroyData);
    }

    return;
}

bool findInAVLTree (AVLTree * findInTree, void * findingData)
{
    bool searchResult;

    /*If the given AVL Tree or void * data is NULL, the function will display an error and return*/
    if (findInTree == NULL || findData == NULL)
    {
        printf ("Error - Either AVL Tree or Data Parameter is NULL - No data search will occur\n");
        return (false);
    }
    else
    {
        searchResult = findData (findInTree->root, findingData, findInTree->compareData);
        return (searchResult);
    }
}

AVLTree * getLeftSubtree (AVLTree * treeSource)
{
    AVLTree * leftSubtree;

    /*If the given AVL Tree is NULL, the function will display an error and return*/
    if (treeSource == NULL)
    {
        printf ("Error - AVL Tree passed in is NULL - Could not get left subtree\n");
        return (NULL);
    }
    else
    {
        /*An AVL Tree is created with createAVLTree and the root is set to the parameter tree's left child*/
        leftSubtree = createAVLTree (treeSource->compareData, treeSource->destroyData);
        leftSubtree->root = treeSource->root->left;

        return (leftSubtree);
    }
}

AVLTree * getRightSubtree (AVLTree * parentTree)
{
    AVLTree * rightSubtree;

    /*If the given AVL Tree is NULL, the function will display an error and return*/
    if (parentTree == NULL)
    {
        printf ("Error - AVL Tree passed in is NULL - Could not get right subtree\n");
        return (NULL);
    }
    else
    {
        /*An AVL Tree is created with createAVLTree and the root is set to the parameter tree's right child*/
        rightSubtree = createAVLTree (parentTree->compareData, parentTree->destroyData);
        rightSubtree->root = parentTree->root->right;

        return (rightSubtree);
    }
}

void * getRootData (AVLTree * theTree)
{
    void * rootData;

    /*If the given AVL Tree is NULL, the function will display an error and return*/
    if (theTree == NULL)
    {
        printf ("Error - AVL Tree passed in is NULL - Could not get root data\n");
        return (NULL);
    }
    else
    {
        /*If the AVL Tree is empty, an error will be displayed and the function will return NULL*/
        if (theTree->root == NULL)
        {
            printf ("Error - AVL Tree is empty - Could not get root data\n");
            return (NULL);
        }
        else
        {
            /*The function finds and returns the node data at the root of the given AVL Tree*/
            rootData = theTree->root->nodeData;
            return (rootData);
        }
    }
}

bool isAVLTreeEmpty (AVLTree * emptyOrNotTree)
{
    /*If the given AVL Tree is NULL, the function will display a warning and returns true to partially indicate
      that the tree is empty (since the tree does not exist)*/
    if (emptyOrNotTree == NULL)
    {
        printf ("Error - AVL Tree passed in is NULL - Could not determine emptiness of AVL tree\n");
        return (true);
    }
    else
    {
        /*If the AVL Tree is empty, the function will return true*/
        if (emptyOrNotTree->root == NULL)
        {
            return (true);
        }
        else
        {
            /*The function returns false if the root (and therefore tree) is not empty*/
            return (false);
        }
    }
}



/*AVL Tree ADT Helper Operations*/

AVLTreeNode * insertData (AVLTreeNode * currentRoot, void * toBeAdded, int (* comparer) (void *, void *))
{
    int diffCount;
    AVLTreeNode * newNode;

    /*If the current tree root is NULL, the data is placed into a node (if possible) and the node is returned to connect the node to the tree*/
    if (currentRoot == NULL)
    {
        newNode = malloc (sizeof (AVLTreeNode));
        if (newNode == NULL)
        {
            printf ("Error - Data could not be inserted into AVL Tree\n");
            return (NULL);
        }
        else
        {
            newNode->nodeData = toBeAdded;
            newNode->balanceCount = 1;
            newNode->left = NULL;
            newNode->right = NULL;

            return (newNode);
        }
    }
    else
    {
        /*The function compares the current node data with the new data with the supplied function, and will point towards the current direction*/
        if (comparer (toBeAdded, currentRoot->nodeData) < 0)
        {
            /*The function will recursively call itself on the left child if the data is less than the current node data*/
            currentRoot->left = insertData (currentRoot->left, toBeAdded, comparer);

            /*The tree is balanced after the addition appropriately with rotations if needed*/
            diffCount = diffBalanceCount (currentRoot);
            if (diffCount > 1 || diffCount < -1)
            {
                if (comparer (toBeAdded, currentRoot->left->nodeData) < 0)
                {
                    currentRoot = rotateRightWithLeftChild (currentRoot);
                }
                else
                {
                    currentRoot = doubleRotateWithLeftChild (currentRoot);
                }
            }
        }
        else if (comparer (toBeAdded, currentRoot->nodeData) > 0)
        {
            /*The function will recursively call itself on the right child if the data is more than the current node data*/
            currentRoot->right = insertData (currentRoot->right, toBeAdded, comparer);

            /*The tree is balanced after the addition appropriately with rotations if needed*/
            diffCount = diffBalanceCount (currentRoot);
            if (diffCount > 1 || diffCount < -1)
            {
                if (comparer (toBeAdded, currentRoot->right->nodeData) > 0)
                {
                    currentRoot = rotateLeftWithRightChild (currentRoot);
                }
                else
                {
                    currentRoot = doubleRotateWithRightChild (currentRoot);
                }
            }
        }
        else
        {
           /*If the compare function returns a matching case, the data is not added into the tree*/
            printf ("Matching Data Found - Data not Added\n");
        }

        /*The current node's balance count (height) is adjusted to its current count*/
        currentRoot->balanceCount = maxBalanceCount (currentRoot) + 1;
        return (currentRoot);
    }
}

AVLTreeNode * deleteData (AVLTreeNode * root, void * toBeRemoved, int (* comparing) (void *, void *), void (* destroyer) (void *))
{
    int differenceCount;
    AVLTreeNode * tempNode;

    /*If the current tree root is NULL, the data must not have been found, so the user is notified and the function will return*/
    if (root == NULL)
    {
        printf ("Error - Data to be deleted cannot be found\n");
        return (NULL);
    }
    else
    {
        /*The function compares the current node data with the new data with the supplied function, and will point towards the current direction*/
        if (comparing (toBeRemoved, root->nodeData) < 0)
        {
            /*The function will recursively call itself on the left child if the data is less than the current node data*/
            root->left = deleteData (root->left, toBeRemoved, comparing, destroyer);

            /*The tree is balanced after the deletion appropriately with rotations if needed*/
            differenceCount = diffBalanceCount (root);
            if (differenceCount > 1 || differenceCount < -1)
            {
                if (root->right->left == NULL)
                {
                    root = rotateLeftWithRightChild (root);
                }
                else
                {
                    root = doubleRotateWithRightChild (root);
                }
            }
        }
        else if (comparing (toBeRemoved, root->nodeData) > 0)
        {
            /*The function will recursively call itself on the right child if the data is more than the current node data*/
            root->right = deleteData (root->right, toBeRemoved, comparing, destroyer);

            /*The tree is balanced after the deletion appropriately with rotations if needed*/
            differenceCount = diffBalanceCount (root);
            if (differenceCount > 1 || differenceCount < -1)
            {
                if (root->left->right == NULL)
                {
                    root = rotateRightWithLeftChild (root);
                }
                else
                {
                    root = doubleRotateWithLeftChild (root);
                }
            }
        }
        /*If the compare function returns a matching case, the data is now to be deleted*/
        else
        {
           /*The deletion will depend on the children the to-be-deleted node has*/
           if (root->left != NULL && root->right != NULL)
           {
               /*The minimum of the right subtree is found and the data replaces the now deleted data of the current root, and the node is
                 used to belong to will now be deleted*/
               tempNode = findMinimum (root->right);
               destroyer (root->nodeData);
               root->nodeData = tempNode->nodeData;
               root->right = deleteData (root->right, tempNode->nodeData, comparing, destroyer);
           }
           else
           {
               tempNode = root;

               /*The root to be deleted is simply replaced by its only child if it has any*/
               if (root->left == NULL)
               {
                   root = root->right;
               }
               else if (root->right == NULL)
               {
                   root = root->left;
               }

               if (tempNode->balanceCount != 0)
               {
                   destroyer (tempNode->nodeData);
               }

               free(tempNode);
           }
        }

        /*The current node's balance count (height) is adjusted to its current count*/
        if (root != NULL)
        {
            root->balanceCount = maxBalanceCount (root) + 1;
        }

        return (root);
    }
}

int diffBalanceCount (AVLTreeNode * nodeToAnalyze)
{
    AVLTreeNode * treeAtLeft;
    AVLTreeNode * treeAtRight;
    int difference;
    int heightAtLeft;
    int heightAtRight;

    /*The left side height is retrieved if available. If not, 0 is assumed*/
    treeAtLeft = nodeToAnalyze->left;
    if (treeAtLeft == NULL)
    {
        heightAtLeft = 0;
    }
    else
    {
        heightAtLeft = treeAtLeft->balanceCount;
    }

    /*The right side height is retrieved if available. If not, 0 is assumed*/
    treeAtRight = nodeToAnalyze->right;
    if (treeAtRight == NULL)
    {
        heightAtRight = 0;
    }
    else
    {
        heightAtRight = treeAtRight->balanceCount;
    }

    /*The difference is calculated and returned*/
    difference = heightAtLeft - heightAtRight;
    return (difference);
}

int maxBalanceCount (AVLTreeNode * nodeToModify)
{
    AVLTreeNode * leftTree;
    AVLTreeNode * rightTree;
    int leftHeight;
    int rightHeight;

    /*The left side height count is retrieved if available. If not, 0 is assumed*/
    leftTree = nodeToModify->left;
    if (leftTree == NULL)
    {
        leftHeight = 0;
    }
    else
    {
        leftHeight = leftTree->balanceCount;
    }

    /*The right side height count is retrieved if available. If not, 0 is assumed*/
    rightTree = nodeToModify->right;
    if (rightTree == NULL)
    {
        rightHeight = 0;
    }
    else
    {
        rightHeight = rightTree->balanceCount;
    }

    /*The counts are compared to see which one has the bigger height count, which is returned. If the heights are equal, the count returned
      is irrelevant, so the right height is returned*/
    if (leftHeight <= rightHeight)
    {
        return (rightHeight);
    }
    else
    {
        return (leftHeight);
    }
}

AVLTreeNode * rotateLeftWithRightChild (AVLTreeNode * usedToBeRoot)
{
    AVLTreeNode * toBeNewRoot;

    /*The left rotation is made here*/
    toBeNewRoot = usedToBeRoot->right;
    usedToBeRoot->right = toBeNewRoot->left;
    toBeNewRoot->left = usedToBeRoot;

    /*The balance counts (heights) are updated here*/
    toBeNewRoot->balanceCount = maxBalanceCount (toBeNewRoot) + 1;
    usedToBeRoot->balanceCount = maxBalanceCount (usedToBeRoot) + 1;

    return (toBeNewRoot);
}

AVLTreeNode * rotateRightWithLeftChild (AVLTreeNode * formerRoot)
{
    AVLTreeNode * tempNode;

    /*The right rotation is made here*/
    tempNode = formerRoot->left;
    formerRoot->left = tempNode->right;
    tempNode->right = formerRoot;

    /*The balance counts (heights) are updated here*/
    tempNode->balanceCount = maxBalanceCount (tempNode) + 1;
    formerRoot->balanceCount = maxBalanceCount (formerRoot) + 1;

    return (tempNode);
}

AVLTreeNode * doubleRotateWithRightChild (AVLTreeNode * pastRoot)
{
    /*Two rotations (starting at the right child node) are made*/
    pastRoot->right = rotateRightWithLeftChild (pastRoot->right);
    pastRoot = rotateLeftWithRightChild (pastRoot);

    return (pastRoot);
}

AVLTreeNode * doubleRotateWithLeftChild (AVLTreeNode * oldRoot)
{
    /*Two rotations (starting at the left child node) are made*/
    oldRoot->left = rotateLeftWithRightChild (oldRoot->left);
    oldRoot = rotateRightWithLeftChild (oldRoot);

    return (oldRoot);
}

AVLTreeNode * findMinimum (AVLTreeNode * rightRoot)
{
    AVLTreeNode * minNode;

    /*This recursive function will keep travelling to its left child until it reaches a NULL, indicating that this node is the minimum, and is returned.
      The balance count is set to 0 (can't be set to 0 otherwise) to mark the data that is to be not deleted*/
    if (rightRoot->left == NULL)
    {
        rightRoot->balanceCount = 0;
        return (rightRoot);
    }
    else
    {
        minNode = findMinimum (rightRoot->left);
        return (minNode);
    }
}

bool findData (AVLTreeNode * treeRoot, void * toBeFound, int (* comparison) (void *, void *))
{
    /*If the current tree root is NULL, the data must not have been found, so the user is notified and the function will return*/
    if (treeRoot == NULL)
    {
        return (false);
    }
    else
    {
        /*The function compares the current node data with the new data with the supplied function, and will point towards the current direction*/
        if (comparison (toBeFound, treeRoot->nodeData) < 0)
        {
            /*The function will recursively call itself on the left child if the data is less than the current node data*/
            return (findData (treeRoot->left, toBeFound, comparison));
        }
        else if (comparison (toBeFound, treeRoot->nodeData) > 0)
        {
            /*The function will recursively call itself on the right child if the data is more than the current node data*/
            return (findData (treeRoot->right, toBeFound, comparison));
        }
        /*If the compare function returns a matching case, TRUE will now recursively be returned*/
        else
        {
            return (true);
        }
    }
}

void completeDestroy (AVLTreeNode * rootToBeDestroyed, void (* howToDestroy) (void *))
{
    /*If the current node being analyzed is NULL, the function will return*/
    if (rootToBeDestroyed == NULL)
    {
        return;
    }
    else
    {
        /*The AVL node's data is destroyed with the supplied function*/
        howToDestroy (rootToBeDestroyed->nodeData);

        /*The function will recursively call each child node (if any) and destroy their data before this node is finished being destroyed form memory*/
        completeDestroy (rootToBeDestroyed->left, howToDestroy);
        completeDestroy (rootToBeDestroyed->right, howToDestroy);

        /*The function will destroy the node itself here, then return*/
        free (rootToBeDestroyed);

        return;
    }
}

