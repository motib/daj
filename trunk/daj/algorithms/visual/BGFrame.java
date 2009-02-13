//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//
//  Antoine Pineau, Moti Ben-Ari
// Class BGFrame is used to display the message tree.
// It contains and manages all the BGPanels
package daj.algorithms.visual;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

public class BGFrame extends JFrame {
	public BGFrame(int num) {
		number = num;
		init();
	}

	//Initializes the panels
	private void init() {
		if (generals == null) {
			generals = new BGPanel[number];
			for (int i = 0; i < number; i++) {
				generals[i] = new BGPanel(i);
				// Insert the BGPanel in a JScrollPane
				JScrollPane scrollPane = new JScrollPane();
				scrollPane.setVerticalScrollBarPolicy(
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setHorizontalScrollBarPolicy(
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollPane.setViewportView(generals[i]);
				//add the scrollPane in a tabbedPane
				tabbedPane.addTab(daj.Screen.node[i], null, scrollPane,null);
			}
			// Add a listener on the tabbedPane:
			//   bring pane selected to front and change title
			tabbedPane.addChangeListener(
			    new ChangeListener() {
				    public void stateChanged( ChangeEvent event ) {
					    JScrollPane scroll = 
							(JScrollPane) tabbedPane.getSelectedComponent();
					    BGPanel g = 
							(BGPanel) scroll.getViewport().getView();
					    setTitle(TITLE + daj.Screen.node[g.getIndex()]);
				    }
			    }
			);
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
			initSlider();
			getContentPane().add(slider, BorderLayout.SOUTH);
			setTitle(TITLE + daj.Screen.node[0]);
			setSize(FRAMEWIDTH,  FRAMEHEIGHT);
			setLocation(FRAMEX, FRAMEY);
		}
		setVisible(true);
	}

	// Creates the slider which is used to change the width of the tree
	private JSlider initSlider() {
		slider.addChangeListener(
		    new ChangeListener() {
			    public void stateChanged( ChangeEvent event ) {
				    int value = (int)((JSlider)event.getSource()).getValue();
				    for (int i=0; i<number; i++)
					    generals[i].setTreeWidth(value);
			    }
		    }
		);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(false);
		slider.setPaintLabels(false);
		slider.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
		return slider;
	}

	// Accessor for general's panel, also selects panel.
	public BGPanel getPanel(int i) {
		tabbedPane.setSelectedIndex(i);
		return generals[i];
	}

	public void reset() {
		for (int i = 0; i<number; i++)
			generals[i].clear();
	}

	private int number;               // Number of panels
	private BGPanel[] generals;  // Array of panels for each general
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JSlider slider = new JSlider(JSlider.HORIZONTAL,50, 250, 100);

	private static final String TITLE       = "Message tree about ";
	private static final int    FRAMEWIDTH  = 400;
	private static final int    FRAMEHEIGHT = 250;
	private static final int    FRAMEX      = 600;
	private static final int    FRAMEY      = 0;
}
