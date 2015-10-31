import java.util.NoSuchElementException;

public class LazySearchTree<E extends Comparable<? super E>> implements
      Cloneable
{

   protected class LazySTNode
   {
      // use public access so the tree or other classes can access members
      public LazySTNode lftChild, rtChild;
      public E data;
      public LazySTNode myRoot; // needed to test for certain error
      public boolean deleted;

      public LazySTNode(E d, LazySTNode lft, LazySTNode rt, boolean del)
      {
         lftChild = lft;
         rtChild = rt;
         data = d;
         deleted = del;
      }

      public LazySTNode()
      {
         this(null, null, null, false);

      }

      // function stubs -- for use only with AVL Trees when we extend
      public int getHeight()
      {
         return 0;
      }

      boolean setHeight(int height)
      {
         return true;
      }
   }

   protected static boolean DEBUG = false;
   protected int mSize;
   protected LazySTNode mRoot;
   protected int mSizeHard;

   public LazySearchTree()
   {
      clear();
   }

   public boolean empty()
   {
      return (mSize == 0);
   }

   public int size()
   {
      return mSize;
   }

   public void clear()
   {
      mSize = 0;
      mSizeHard = 0;
      mRoot = null;
   }

   public int showHeight()
   {
      return findHeight(mRoot, -1);
   }

   public int sizeHard()
   {
      return mSizeHard;
   }

   public E findMin()
   {
      LazySTNode resultNode;
      resultNode = findMin(mRoot);
      if (resultNode == null)
         throw new NoSuchElementException();
      return resultNode.data;
   }

   public E findMinHard()
   {
      LazySTNode resultNode;
      resultNode = findMinHard(mRoot);
      if (resultNode == null)
         throw new NoSuchElementException();
      return resultNode.data;
   }

   public E findMax()
   {
      LazySTNode resultNode;
      resultNode = findMax(mRoot);
      if (resultNode == null)
         throw new NoSuchElementException();
      return resultNode.data;
   }

   public E findMaxHard()
   {
      LazySTNode resultNode;
      resultNode = findMaxHard(mRoot);
      if (resultNode == null)
         throw new NoSuchElementException();
      return resultNode.data;
   }

   public E find(E x)
   {
      LazySTNode resultNode;
      resultNode = find(mRoot, x);
      if (resultNode == null)
         throw new NoSuchElementException();

      return resultNode.data;
   }

   public boolean contains(E x)
   {
      return find(mRoot, x) != null;
   }

   public boolean insert(E x)
   {
      int oldSize = mSize;
      mRoot = insert(mRoot, x);
      return (mSize != oldSize);
   }

   public boolean remove(E x)
   {
      int oldSize = mSize;
      remove(mRoot, x);
      return (mSize != oldSize);
   }

   public void collectGarbage()
   {
      mRoot = collectGarbage(mRoot);

   }

   public boolean removeHard(E x)
   {
      int oldSize = mSize;
      LazySTNode temp = removeHard(mRoot, x);
      if (temp.equals(mRoot))
         mRoot = temp;
      return (mSize != oldSize);
   }

   public <F extends Traverser<? super E>> void traverse(F func)
   {
      traverse(func, mRoot);
   }

   public Object clone() throws CloneNotSupportedException
   {
      LazySearchTree<E> newObject = (LazySearchTree<E>) super.clone();
      newObject.clear(); // can't point to other's data

      newObject.mRoot = cloneSubtree(mRoot);
      newObject.mSize = mSize;

      return newObject;
   }

   // private helper methods ----------------------------------------
   protected LazySTNode findMin(LazySTNode root)
   {
      // sorry for the lengthy name but helps me conceptualize this process.
      LazySTNode putativeLeftChild_Min;

      if (root == null)
         return null;

      // march down the left side of tree until the lftChild becomes null
      // assigning it to putativeLeftChild_Min node sequentially putting it
      // on a stack - unwinding down the tree.
      putativeLeftChild_Min = findMin(root.lftChild);

      // when it hits the bottom it the last nodes left child will be null
      // so it falls through this filter. Otherwise it is returned to the
      // findMin call immediately above. When this is done winding back up
      // the min falls through the the last return root statement.
      if (putativeLeftChild_Min != null)
      {
         return putativeLeftChild_Min;
      }

      // if the putativeLeftChild_Min has fallen through a not-null filter
      // need to check it to see if it has been soft deleted so...
      if (root.deleted)
      {
         // need to check the rtChild since the left node could be extant
         // but "deleted" and if it were hard deleted it would not be here
         return findMin(root.rtChild);
      } else
      {
         return root;
      }
   }

   protected LazySTNode findMinHard(LazySTNode root)
   {
      if (root == null)
         return null;
      if (root.lftChild == null)
         return root;
      return findMinHard(root.lftChild);

   }

   protected LazySTNode findMax(LazySTNode root)
   {
      // sorry for the lengthy name but helps me conceptualize this process.
      LazySTNode putativeRtChild_Max;

      if (root == null)
         return null;

      // march down the right side of tree until the rtChild becomes null
      // assigning it to putativeRtChild_Max node sequentially putting it
      // on a stack - unwinding down the tree.
      putativeRtChild_Max = findMax(root.rtChild);

      // when it hits (unwinds to) the bottom, the last node's left child will
      // be null so it falls through this filter. Otherwise it is returned to
      // the
      // findMax call immediately above. When a null it hit - the base case -
      // it starts returning the results off the stack sequentially, winding
      // back up.
      if (putativeRtChild_Max != null)
      {
         return putativeRtChild_Max;
      }

      // if the putativeRtChild_Max has fallen through a not-null filter
      // need to check it to see if it has been soft deleted so...
      if (root.deleted)
      {
         // need to check the lftChild since the right node could be extant
         // but soft deleted. Normally this step would not be performed
         // because in non-lazy deletion the right node would be null
         return findMax(root.lftChild);
      } else
      {
         return root;
      }
   }

   protected LazySTNode findMaxHard(LazySTNode root)
   {
      if (root == null)
         return null;

      if (root.rtChild != null)
      {
         return findMaxHard(root.rtChild);
      } else
      {
         return root;
      }
   }

   protected LazySTNode insert(LazySTNode root, E x)
   {
      int compareResult; // avoid multiple calls to compareTo()

      if (root == null)
      {
         mSize++;
         mSizeHard++;
         return new LazySTNode(x, null, null, false);
      }

      compareResult = x.compareTo(root.data);
      if (compareResult < 0)
         root.lftChild = insert(root.lftChild, x);
      else if (compareResult > 0)
         root.rtChild = insert(root.rtChild, x);

      return root;
   }

   protected LazySTNode remove(LazySTNode root, E x)
   {
      if (root == null)
         return null;
      if (find(x) != null)
      {
         find(root, x).deleted = true;
         mSize--;
         return root;
      } else
      {
         return null;
      }
   }

   protected LazySTNode collectGarbage(LazySTNode root)
   {
      if (root == null)
         return null;

      // look for nodes marked deleted in each child sub-tree and lastly
      // the root of the whole tree. Return the mRoot because it could
      // have been changed.
      if(root.lftChild != null)
         root.lftChild = collectGarbage(root.lftChild);
      if(root.rtChild != null)
         root.rtChild = collectGarbage(root.rtChild);
      if (root.deleted)
         root = removeHard(root, root.data);
      return root;
   }

   protected LazySTNode removeHard(LazySTNode root, E x)
   {
      int compareResult; // avoid multiple calls to compareTo()
      LazySTNode putativeRightChild_min;

      if (root == null)
         return null;

      compareResult = x.compareTo(root.data);
      if (compareResult < 0)
         root.lftChild = removeHard(root.lftChild, x);
      else if (compareResult > 0)
         root.rtChild = removeHard(root.rtChild, x);

      // The program recurses above here until it matches the data
      // Now check to see if it has both children
      else if (root.lftChild != null && root.rtChild != null)
      {
         // if the node has not been previously marked as deleted
         // we need to adjust mSize as it is removed here
         if (!root.deleted)
            mSize--;

         // find the smallest node in the right subtree to replace the
         // root node with
         putativeRightChild_min = findMinHard(root.rtChild);

         // replace the data and deleted values with the min node values
         root.data = putativeRightChild_min.data;
         root.deleted = putativeRightChild_min.deleted;

         // mark the min node as deleted
         putativeRightChild_min.deleted = true;

         // go through and hard remove it
         root.rtChild = removeHard(root.rtChild, root.data);

      } else
      // if there is only one child node
      {
         // if the node has not been previously marked as deleted
         // we need to adjust mSize as it is removed here
         if (!root.deleted)
            mSize--;

         // replace the deleted node with the next lower one on the tree
         root = (root.lftChild != null) ? root.lftChild : root.rtChild;

         // adjust the Hard count
         mSizeHard--;
      }

      return root;
   }

   protected <F extends Traverser<? super E>> void traverse(F func,
         LazySTNode treeNode)
   {
      if (treeNode == null)
         return;

      traverse(func, treeNode.lftChild);
      if (!treeNode.deleted)
         func.visit(treeNode.data);
      traverse(func, treeNode.rtChild);
   }

   protected LazySTNode find(LazySTNode root, E x)
   {
      int compareResult; // avoid multiple calls to compareTo()

      if (root == null)
         return null;

      compareResult = x.compareTo(root.data);
      if (compareResult < 0)
         return find(root.lftChild, x);
      if (compareResult > 0)
         return find(root.rtChild, x);
      if (!root.deleted)
         return root; // found
      return null;
   }

   protected LazySTNode cloneSubtree(LazySTNode root)
   {
      LazySTNode newNode;
      if (root == null)
         return null;

      // does not set myRoot which must be done by caller
      newNode = new LazySTNode(root.data, cloneSubtree(root.lftChild),
            cloneSubtree(root.rtChild), root.deleted);
      return newNode;
   }

   protected int findHeight(LazySTNode treeNode, int height)
   {
      int leftHeight, rightHeight;
      if (treeNode == null)
         return height;
      height++;
      leftHeight = findHeight(treeNode.lftChild, height);
      rightHeight = findHeight(treeNode.rtChild, height);
      return (leftHeight > rightHeight) ? leftHeight : rightHeight;
   }

   protected void printNode(LazySTNode node)
   {
      if (node != null)
      {
         System.out.println("node.data: " + node.data);
         if (node.rtChild != null)
            System.out.println("\tnode.rtChild: " + node.rtChild.data);
         if (node.lftChild != null)
            System.out.println("\tnode.lftChild: " + node.lftChild.data);
         System.out.println("\tnode.deleted: " + node.deleted);
         System.out.println();
      } else
      {
         System.out.println("this node is null.");
      }
   }

   public void showTreeHard(LazySearchTree<E> tree)
   {
      System.out.println("-----------------------------");
      System.out.println("The showTreeHard method. ");
      if (tree.mRoot != null)
      {
         System.out.println("The mRoot is: " + tree.mRoot.data);
         showTreeHard(this.mRoot);
         System.out.print(" " + mRoot.data);
      } else
      {
         System.out.println("The tree is empty");
      }
      System.out.println("\n-----------------------------");
   }

   protected void showTreeHard(LazySTNode root)
   {
      if (root.lftChild != null)
      {
         showTreeHard(root.lftChild);
         System.out.print(root.lftChild.data + " ");
      }
      if (root.rtChild != null)
      {
         showTreeHard(root.rtChild);
         System.out.print(root.rtChild.data + " ");
      }
   }

//   class DeleteObject implements Traverser<E>
//   {
//      public void visit(E x)
//      {
//
//      }
//
//   }

}
