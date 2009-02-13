//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//  Moti Ben-Ari
//  Class AlgList
//    This class is declared to make it easy to add an algorithm:
//      String[] algs   - two-letter algorithm code
//      String[] titles - frame title
//      Allocation of algorithm instance the getAlgObject.

package daj;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import daj.algorithms.*;

class AlgList {

  AlgList(int max, int def) {
    MAXNODES = max;
    DEFAULTNODES = def;
  }

  // Accessor for algorithm names.
  String getAlgName(int index)
  {
    return titles[index];
  }

  // Get number of nodes if entered interactively
  int getNodes(int parm) {
    if (node != 0) return node;
    else return parm;
  }

  // Sentinel search for the algorithm code
  //   or obtain algorithm from GUI is no code entered.
  int getAlg(String s) {
    if (s.equals("")) {
        Interactive frame = new Interactive();
        frame.setup();
        waitObject.waitOK();  // Wait for reply from frame event handler.
        frame.dispose();
        return select;
    }
    int index = algs.length-1;
    algs[0] = s;
    while (! s.equals(algs[index])) index--;
    if (index == 0) badParam();
    return index;
  }

  // Display message and exit if bad algorithm code.
  private void badParam() {
    System.err.println("Usage: java daj alg [num] or " + 
      "java -jar daj.jar alg [num], where");
    System.out.println("  num is the number of nodes 2.." + 
      MAXNODES + " default " + DEFAULTNODES);
    System.out.println("  alg is the algorithm:");
    for (int i = 1; i < algs.length; i++)
      System.out.println("    "+algs[i]+" - "+titles[i]);
  }

  // Inner class to create JFrame for GUI

  class Interactive extends JFrame {

  void setup() {
      setTitle(Screen.VERSION + Screen.VERSIONNUMBER);
      if (Screen.isApplication) setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setSize(450,130);
      setLocationRelativeTo(null);
      setLayout(new BorderLayout());
 
      final JComboBox algBox = new JComboBox();
      algBox.setEditable(false);
      for (int i = 1; i < algs.length; i++)
          algBox.addItem(titles[i]);
      algBox.setSelectedIndex(DEFAULTALG);
      algBox.setBorder(BorderFactory.createEtchedBorder());
      algBox.setMaximumRowCount(MAXROWS);
      add(algBox, BorderLayout.CENTER);

      final JComboBox numBox = new JComboBox();
      numBox.setEditable(false);
      for (int i = 1; i <= MAXNODES; i++)
          numBox.addItem("  " + i + "  ");
      numBox.setSelectedIndex(DEFAULTNODES-1);
      numBox.setBorder(BorderFactory.createEtchedBorder());
      add(numBox, BorderLayout.EAST);

      // Label for title of panel.
      JLabel label = new JLabel(CHOOSE, JLabel.CENTER);
      label.setBorder(BorderFactory.createEtchedBorder());
      add(label, BorderLayout.NORTH);

      // Panel for buttons.
      JPanel buttonPanel = new JPanel();
      JButton OKButton = new JButton(START);
      JButton exitButton = new JButton(EXIT);
      JButton aboutButton = new JButton(ABOUT);
      OKButton.setMnemonic(START.charAt(1));
      exitButton.setMnemonic(EXIT.charAt(1));
      aboutButton.setMnemonic(ABOUT.charAt(0));
      buttonPanel.add(OKButton);
      buttonPanel.add(exitButton);
      buttonPanel.add(aboutButton);
      buttonPanel.setBorder(BorderFactory.createEtchedBorder());
      add(buttonPanel, BorderLayout.SOUTH);

      KeyListener kl = new KeyListener() {
          //public boolean isFocusable() { return true; }
          public void keyReleased(KeyEvent evt) {}
          public void keyPressed(KeyEvent evt) {}
          public void keyTyped(KeyEvent evt) {
            char c = evt.getKeyChar();
            if ((int) c == KeyEvent.VK_ENTER) {
                  select = algBox.getSelectedIndex() + 1;
                  node = numBox.getSelectedIndex() + 1;
                  waitObject.signalOK();
            }
            else if ((int) c == KeyEvent.VK_ESCAPE)
                System.exit(0);
          }
      };
      algBox.addKeyListener(kl);
      numBox.addKeyListener(kl);

      // Listener for buttons.
      ActionListener bl = new ActionListener() { 
          public void actionPerformed(ActionEvent e) {
              String cmd = e.getActionCommand();
              if (cmd.equals(START)) {
                  select = algBox.getSelectedIndex() + 1;
                  node = numBox.getSelectedIndex() + 1;
                  waitObject.signalOK();
              }
              else if (cmd.equals(EXIT)) 
                  System.exit(0);
              else if (cmd.equals(ABOUT)) {
                  String aboutText = 
  "      DAJ - Distributed Algorithms in Java. Version " + 
  Screen.VERSIONNUMBER + ".\n\n" +
  " Developed by Moti Ben-Ari.\n"+
  " Additional programming by:\n"+
  "   Antoine Pineau - University of Joensuu.\n"+
  "   Basil Worrall, Frederick Kemp, Frank Harvie,\n"+
  "   Richard McGladdery, Leoni Lubbinge, Derick Burger, Darrell Newing\n"+ 
  "         - University of Pretoria.\n"+
  "   Maor Zamsky - Givat Brenner High School.\n\n"+
  "   Otto Seppälä, Ville Karavirta - Helsinki University of Technology\n"+
  "Copyright 2003-6 by Mordechai (Moti) Ben-Ari.\n\n"+
  " This program is free software; you can redistribute it and/or\n" +
  " modify it under the terms of the GNU General Public License\n" +
  " as published by the Free Software Foundation; either version 2\n" +
  " of the License, or (at your option) any later version.\n\n" +
  " This program is distributed in the hope that it will be useful\n" +
  " but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
  " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.\n" +
  " See the GNU General Public License for more details.\n\n" +
  " You should have received a copy of the GNU General Public License\n" + 
  " along with this program; if not, write to the Free Software\n" +
  " Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA \n" + 
  " 02111-1307, USA.";
                JOptionPane.showMessageDialog(
                    null, aboutText, Screen.VERSION + Screen.VERSIONNUMBER,
                    JOptionPane.INFORMATION_MESSAGE);
                }
            } // actionPerformed
      };  // anonymous class
  
      OKButton.addActionListener(bl);
      exitButton.addActionListener(bl);
      aboutButton.addActionListener(bl);
      validate();
      setVisible(true);
    } // setup
  } // class Interactive

  // Titles and codes of the algorithms.
  // Ville & Otto: Added the new algorithms to titles and algs arrays.
  private static final String[] titles = {
    "",
    "Berman-Garay (King) algorithm for consensus",
    "Byzantine generals algorithm for consensus (byzantine failures)",
    "Byzantine generals algorithm for consensus (crash failures)",
    "Chandy-Lamport algorithm for global snapshots",
    "Dijkstra-Scholten termination detection",
    "Flooding algorithm for consensus",
    "Lamport algorithm for mutual exclusion",
    "Ricart-Agrawala algorithm for mutual exclusion",
    "Maekawa mutual algorithm for exclusion",
    "Suzuki-Kasami algorithm for mutual exclusion",
    "Huang credit-recovery termination detection",
    "Mattern credit-recovery termination detection",
    "Carvalho-Roucairol token algorithm for mutual exclusion",
    "Neilsen-Mizuno algorithm for mutual exclusion"
  };
  private static String[] algs =
    {"help", "kg", "bg", "cr", "sn", "ds", "fl", "la", "ra", "ma", "sk", "hg","mt", "tk", "nm"};

  // Create and return object for an algorithm.
  DistAlg getAlgObject(int index, int i, DistAlg[] da, Screen s) {
    switch (index) {
        case  1: return new KG(i, da, s);
        case  2: return new BG(i, da, s);
        case  3: return new CR(i, da, s);
        case  4: return new SN(i, da, s);
        case  5: return new DS(i, da, s);
        case  6: return new FL(i, da, s);
        case  7: return new LA(i, da, s);
        case  8: return new RA(i, da, s);
        case  9: return new MA(i, da, s);
        case 10: return new SK(i, da, s);
        // Otto: Replaced the old implementation of the Huang
        case 11: return new HG(i, da, s);
        // Otto & Ville: Added these new algorithms
        case 12: return new MT(i, da, s);
        case 13: return new TK(i, da, s);
        case 14: return new NM(i, da, s);
     }
    return null; // Dummy statement for compiler
  }

  private int     select;          // Algorithm selected by GUI
  private int     node = 0;        // Number of nodes selected by GUI
  private WaitObject waitObject = new WaitObject();  
                                   // For synchronization with event handlers
  private static int MAXNODES;     // Maximum number of nodes
  private static int DEFAULTNODES; // Default number of nodes
  private static int DEFAULTALG = 1; // Default alg is BG
  private static final int MAXROWS = 15; // Maximum algs without scrolling
  private static final String      // GUI strings
    START = "Start",
    ABOUT = "About",
    EXIT  = "Exit",
    CHOOSE = "Choose an algorithm and the number of nodes";
}
