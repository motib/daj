//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//
//  Visualization of Dijkstra-Scholten algorithm for distributed termination
//  Programmed 2005 by Maor Zamsky with help from Shmuel Schwarz, Moti-Ben-Ari
//
package daj.algorithms.visual;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
public class DSFrame extends JFrame {
    public DSFrame(int num) {
        number = num;
        init();
    }
    
    public void reset() { init(); }

    public void init() {
        setPreferredSize(new Dimension(SIZEX, SIZEY));
        for(int i = 1 ; i < daj.Screen.node.length; i++) {
            lines[i] = new Line2D.Double();
            circles[i] = new Ellipse2D.Double();
            dots[i] = new Ellipse2D.Double();
        }
        circles[0] = 
            new Ellipse2D.Double(coords[0][0], coords[0][1], RADIUS, RADIUS);
        setTitle(TITLE);
        setLocation(FRAMEX, FRAMEY);
        add(dsPanel);
        pack();
        setVisible(true);
    }
   
    // Create new line and new circle from sender to receiver
    // At most one line/circle per receiver
    public void createNode(int sender, int receiver) {
        circles[receiver].setFrame(
            coords[receiver][2], coords[receiver][3], RADIUS, RADIUS);
        lines[receiver].setLine(
            coords[sender][0]+deltas[sender][0], 
            coords[sender][1]+deltas[sender][1], 
            coords[receiver][2]+deltas[receiver][2], 
            coords[receiver][3]+deltas[receiver][3]);
        dots[receiver].setFrame(
            coords[sender][2] + deltas[sender][2] - DOT/2.0, 
            coords[sender][3]+deltas[sender][3] - DOT/2.0, DOT, DOT);
        repaint();
    }

    // Erase line and circle for terminated node
    public void eraseNode(int terminated) {
        lines[terminated].setLine(0, 0, 0, 0);
        circles[terminated].setFrame(0, 0, 0, 0);
        dots[terminated].setFrame(0, 0, 0, 0);
        repaint();
    }

    private class DSPanel extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            // sets the antialiasing look for a better look of graphics
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            // draw the lines and circles
            for(int i = 0 ; i < number; i++) {
                g2d.setColor(daj.Screen.colors[i]);  // Color of receiving node		
                if (i != 0) g2d.draw(lines[i]);  // No line to root
                if (i != 0) g2d.fill(dots[i]);  // No line to root
                g2d.draw(circles[i]);
                g2d.drawString(daj.Screen.node[i], 
                    (float) (coords[i][0]+STRINGX), (float) (coords[i][1]+STRINGY));
            }
        }
    }

    private int number;  // Number of nodes
    private DSPanel dsPanel = new DSPanel();
    private static final String TITLE       = "Spanning tree";
    private static final int    FRAMEX      = 500;
    private static final int    FRAMEY      = 0;
    private static final int    SIZEX       = 400;
    private static final int    SIZEY       = 450;
    private static final double RADIUS      = 60.0;
    private static final double STRINGX     = 17.0;
    private static final double STRINGY     = RADIUS / 2.0;
    private static final double DOT         = 6.0;
    private static final double[][] coords  = {
            {170.0, 50.0, 170.0, 50.0},
            {320.0, 200.0, 320.0, 200.0},
            {20.0, 200.0, 20.0, 200.0},
            {276.07, 306.07, 276.07, 306.07},
            {63.3, 306.07, 63.3, 306.07},
            {170.0, 350.0, 170.0, 350.0} };
    private static final double[][] deltas  = {
            {30.0, 60.0, 30.0, 60.0},
            {0.0, 30.0, 0.0, 30.0},
            {60.0, 30.0, 60.0, 30.0},
            {8.79, 8.79, 8.79, 8.79},
            {51.21, 8.79, 51.21, 8.79},
            {30.0, 0.0, 30.0, 0.0} };
    private Line2D.Double lines[] = 
		new Line2D.Double[daj.Screen.node.length];
    private Ellipse2D.Double circles[] = 
		new Ellipse2D.Double[daj.Screen.node.length];
    private Ellipse2D.Double dots[] = 
		new Ellipse2D.Double[daj.Screen.node.length];
}
