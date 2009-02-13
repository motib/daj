//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//
//  Antoine Pineau, Moti Ben-Ari
// BGPanel is a panel in which the message tree of
// the general is displayed.
package daj.algorithms.visual;

import java.awt.*;
import javax.swing.*;

public class BGPanel extends JPanel {
	// Construct panel for general i.
	public BGPanel(int i) {
		index = i;
		setBorder(BorderFactory.createCompoundBorder(
		              BorderFactory.createLoweredBevelBorder(),
		              BorderFactory.createEmptyBorder(10,10,10,10)));
	}

	//  Paint method
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		drawTree(g);
	}

	// Creates root of the tree
	// rootNode - value of the root node
	// plan - plan of the node
	// nbGenerals - number of Generals
	public void createRoot(int rootNode, int plan, int numGenerals) {
		tree = new BGNode[1 + (numGenerals-1) * (1 + (numGenerals-2))];
		tree[size++] = new BGNode(rootNode, plan, numGenerals);
		repaint();
	}

	// Adds node toGeneral to the tree and links it to node prevGeneral.
	// Nodes for all of the previous generals will be at level zero or one in the tree.
	public void addNode(int prevGeneral, int toGeneral, int plan) {
		BGNode node = null;
		for (int i = 0; i < size; i++)
			if ( ( tree[i].getValue() == prevGeneral ) &&
			        ( tree[i].getLevel() <= 1     ) ) {
				node =  tree[i];
				break;
			}
		tree[size++] = new BGNode(node, toGeneral, node.addChild(), node.getLevel()+1, plan);
		repaint();
	}

	//  Draws a node in the panel and links it to its parent.
	//  Anti-aliasing is used.
	private void drawNode(BGNode node, Graphics g) {
		//retrieve the graphic context
		Graphics2D g2d = (Graphics2D)g;
		//Enable antialiasing for shapes
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		                     RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(NORMALCOLOR);
		if (node.getParent() != null) { // this is not the root
			drawNodeLine(node, g2d);
			if (node.isTraitor()) {  // draw plan for traitor
                // Change positions to make clear with many generals - Moti
                int posX = (node.getX()+width/3);
				int posY = (node.getY()-height/2);
//				int posX = (node.getX()+node.getParent().getX()+width)/2;
//				int posY = (node.getParent().getY()+height+node.getY())/2;
				g2d.setColor(EMPHASIZECOLOR);
				g2d.drawString("" + daj.algorithms.BG.plans[node.getPlan()], 
					posX + 3, posY + 15);
			}
		}
		else {  // draw plan of the root
			g2d.drawString("" + daj.algorithms.BG.plans[node.getPlan()],
			               node.getX()+ width/2, node.getY() + height - 3);
		}
		g2d.setColor(daj.Screen.colors[node.getValue()]);
		g2d.drawOval(node.getX(), node.getY(), width, height);
		g2d.drawString(daj.Screen.node[node.getValue()],
		               node.getX()+ width/3, node.getY() + (height*6)/10);
	}

	// Draws the line between nodes.
	private void drawNodeLine(BGNode node, Graphics2D g2d) {
		g2d.setColor(daj.Screen.colors[node.getValue()]);
		g2d.drawLine(node.getParent().getX()+ width/2 ,
	        node.getParent().getY()+height, node.getX()+ width/2, node.getY());
	}

	// Draws all nodes of the tree
	private void drawTree(Graphics g) {
		for (int i = 0; i < size; i++)
			drawNode(tree[i], g);
	}

	// Clear the panel
	public void clear() {
		size = 0;
		repaint();
	}

	// Set width of the subtree inside the panel
	public void setTreeWidth(int newWidth) {
		if (size > 1) {
			tree[0].setWidth(newWidth);
			for (int i = 0; i < size; i++)
				tree[i].calculate();
			repaint();
		}
	}

	public int getIndex() {
		return index;
	}

	private int index;		// Number of the general
	private int size = 0;    // Nodes in the tree
	private int width = 70;  // Width of a node
	private int height = 40; // Height of a node
	private BGNode[] tree;     // Nodes in each panel

	private static final Color NORMALCOLOR    = Color.black;
	private static final Color EMPHASIZECOLOR = Color.red;
}