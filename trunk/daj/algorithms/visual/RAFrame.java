//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//
//  Visualization of Ricart-Agrawala distributed mutual exclusion
//  Programmed 2005 by Moti-Ben-Ari
//
package daj.algorithms.visual;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
public class RAFrame extends JFrame {
    public RAFrame() {
        init();
    }
    
    public void reset() { init(); }

    public void init() {
		int size = daj.Screen.node.length+1;
        queued = new int[size];
        tickets = new int[size];
        for(int i = 0 ; i < size; i++) { 
            queued[i] = -1;
            tickets[i] = 0;
        }
        for(int i = 0 ; i < size-1; i++) { 
            lines[i] = new Line2D.Double();
            arrows1[i] = new Line2D.Double();
            arrows2[i] = new Line2D.Double();
            circles[i] = new Ellipse2D.Double();
		}
		setTitle(TITLE);
        setLocation(FRAMEX, FRAMEY);
        setPreferredSize(new Dimension(SIZEX, SIZEY));
        add(raPanel);
        pack();
        setVisible(true);
        repaint();
    }
   
    // Add node to end of queue if not already there
    public void addQueued(int node, int num) {
        tickets[node] = num;
        for (int i = 0; i < daj.Screen.node.length+1; i++) {
			int q = queued[i];
            if (q == node) return;
            else if (q == -1) { 
                queued[i] = node;
                break;
            }
            else if ((tickets[node] < tickets[q]) ||
                    ((tickets[node] == tickets[q]) && (node < q))) {
                    for (int j = daj.Screen.node.length; j > i; j--)
                        queued[j] = queued[j-1];
                    queued[i] = node;
                    break;
             }
        }
        setGraphics();
        repaint();
    }

    private void setGraphics() {
        for (int i = 0 ; i < daj.Screen.node.length; i++) {
            int q = queued[i];
            if (q == -1) break;
            lines[i].setLine(
                MARGINX + i * (WIDTH+SPACE) - SPACE, MARGINY + HEIGHT / 2,
                MARGINX + i * (WIDTH+SPACE), MARGINY + HEIGHT / 2);
            arrows1[i].setLine(
                lines[i].getX1(), lines[i].getY1(),
                lines[i].getX1() + ARROWX, lines[i].getY1() + ARROWY);
            arrows2[i].setLine(
                lines[i].getX1(), lines[i].getY1(),
                lines[i].getX1() + ARROWX, lines[i].getY1() - ARROWY);
            circles[i].setFrame(
                MARGINX + i * (WIDTH+SPACE), MARGINY, WIDTH, HEIGHT);
        }
    }

    // Remove head of queue
    public void removeQueued() {
        tickets[queued[0]] = 0;
        for (int i = 1; i < daj.Screen.node.length+1; i++)
            queued[i-1] = queued[i];
        repaint();
    }

    private class RAPanel extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            // sets the antialiasing look for a better look of graphics
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
            // draw the lines and circles
            for (int i = 0 ; i < daj.Screen.node.length; i++) {
                int q = queued[i];
                if (q == -1) break;
                g2d.setColor(daj.Screen.colors[q]);  // Color of receiving node		
                if (i != 0) {   // No line to root
                    g2d.draw(lines[i]);
                    g2d.draw(arrows1[i]);
                    g2d.draw(arrows2[i]);
                }
                g2d.draw(circles[i]);
                g2d.drawString(daj.Screen.node[q],
                    (MARGINX + i * (WIDTH + SPACE + FUDGE) + STRINGX), 
                    (MARGINY + HEIGHT / 2 + STRINGY));
            }
        }
    }

    private int[] queued;       // Queue of nodes
    private int[] tickets;      // Ticket numbers of nodes
    private RAPanel raPanel = new RAPanel();
    private static final String TITLE       = "Virtual queue";
    private static final int    WIDTH       = 80;    
    private static final int    SPACE       = 40;
    private static final int    HEIGHT      = 40;
    private static final int    FUDGE       = 80;
    private static final int    STRINGX     = 26;
    private static final int    STRINGY     = 23;
    private static final int    MARGINX     = 20;
    private static final int    MARGINY     = 20;
    private static final int    FRAMEX      = 400;
    private static final int    FRAMEY      = 0;
    private static final int    SIZEX       = 720;
    private static final int    SIZEY       = 110;
    private static final int    ARROWX      = 5;
    private static final int    ARROWY      = 4;
    private Line2D.Double lines[] = 
		new Line2D.Double[daj.Screen.node.length];
    private Line2D.Double arrows1[] = 
		new Line2D.Double[daj.Screen.node.length];
    private Line2D.Double arrows2[] = 
		new Line2D.Double[daj.Screen.node.length];
    private Ellipse2D.Double circles[] = 
		new Ellipse2D.Double[daj.Screen.node.length];
}
