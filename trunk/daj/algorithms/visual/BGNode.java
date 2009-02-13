//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//
//  Antoine Pineau

//  Class BGNode is used to build our tree in the General panel
//  Here is the main representation of a Node.
//  
//  A node contains: 
//      - parent node (previous)
//      - value (to get its name and color)
//      - plan, represented as an integer
//      - position as a child of its parent
//      - level in the tree
//      - number of children
package daj.algorithms.visual;

public class BGNode {
    //  Constructor for the root node
    //    rootNode - value representing the root node
    //    mainplan - the general's plan (?,A,R)
    //    number of generals
    public BGNode(int rootNode, int mainplan, int number) {
	parent = null;
	value = rootNode;
	position = 1;
	level = 0;
	plan = mainplan;
	nbGenerals = number;
	width = nbGenerals * subtreeWidth;
	axis = width / 2;
    }

    // Constructor for other nodes
    //   parentNode - parent of the node
    //   currentNode - the name of the node
    //   pos - position of the node
    //   lev - level of the node
    //   p - plan of the node
    public BGNode(BGNode parentNode, int currentNode, int pos, int lev, int p) {
	parent = parentNode;
	value = currentNode;
    position = pos;
	level = lev;
	plan = p;
	calculate();
	}

    // Calculate width and axis of Node according to its parent.
    public void calculate() {
	if (parent == null) 
	   axis = width / 2;  //root
	else {
	    nbGenerals = parent.getGenerals();
	    width = parent.getWidth() / (nbGenerals-level);
	    axis = ((position * width) - (width / 2)) + 
		(parent.getWidth()*(parent.getPosition()-1));
	}
    }

    // Accessors  
    public int getValue()     { return value; }
    public int getPosition()  { return position; }
    public BGNode getParent() { return parent; }
    public int getLevel()     { return level;  }
    public int getGenerals()  { return nbGenerals; }
    public int getPlan()      { return plan; }
    public int getChildren()  { return children; }
    public int getX()         { return axis; }
    public int getY()         {	return 20 + height * getLevel(); }
    public int getWidth()     { return width; }

    // Set new plan.
    public void setPlan(int newPlan) { plan = newPlan; } 

    // Increment number of children.
    public int addChild() { return ++children; }

    // Set width of tree
    public void setWidth(int newWidth) { width = nbGenerals * newWidth; }

    // Is the general a traitor?
    // Yes, if the plan he sends is different from the plan he received
    public boolean isTraitor() { return plan != parent.getPlan(); } 
    
    private BGNode parent;	// Parent node
    private int value;		// Current value (name) 
    private int plan;		// Plan sent by the previous general
    private int level;		// Level in the Tree
    private int children = 0;	// Number of children
    private int position;	// Position according to the parent

    private int nbGenerals;	// Number of different generals
    private int axis;		// Position on the axis(nb pixels)
    private int width;		// Width of the subtree
    private static final int height 	  = 70;		// Height of the subtree
    private static final int subtreeWidth = 100;	// Width of the subtree
}
