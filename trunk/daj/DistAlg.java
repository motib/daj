//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//  Moti Ben-Ari
//  Class DistAlg is the abstract class from which
//    the classes of the algorithms are derived.
package daj;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*; 

public abstract class DistAlg extends JPanel implements ActionListener {

  // Abstract methods which must be implemented.

  // Algorithm-specific initialization.
  abstract protected void    init();

  // Construct the button panels.
  //   Calls addComponents for each button panel
  //   and returns the initial button panel.
  abstract protected int     constructButtons();

  // Construct the text for the two title lines
  //   and the two lines for each row.
  abstract protected String  constructTitle1();
  abstract protected String  constructTitle2();
  abstract protected String  constructRow1(int row);
  abstract protected String  constructRow2(int row);

  // Implement the state machine.
  abstract protected boolean stateMachine(String command);

  //-------- SEND and RECEIVE ---------//
  // Otto Seppälä added receive and send methods that accept
  // messages with objects
  
  // Receive a message sent by another node.
  protected void receive(int message, int parm1, int parm2){}
  
  // Alternative form of the receive method which carries an object as the messgae
  protected void receive(int message, int parm1, Object parm2){}

  // Send a message to another nodes.
  protected void send(int message, int to, int parm1, int parm2) {
    ((DistAlg) da[to]).receive(message, parm1, parm2);
  }

  // Send a message to another nodes.
  protected void send(int message, int to, int parm1, Object parm2) {
    ((DistAlg) da[to]).receive(message, parm1, parm2);
  }
  
  // Null methods that can be implemented by visualization classes
  protected void disposeFrame() { }
  protected void setFrameVisible(boolean b) { }
  protected void resetFrame() { }
  protected boolean hasVisualization() { return false; }
  
  // Protected methods called by the derived classes.

  // Initialize the parent class after alg-specific initializaton.
  // protected void initialize()

  // Create button panel, address button panel, return panel code.
  // protected int addComponents(
  //     String c1, String c2, String c3, String c4)
  // protected int addressButtons(String l, String special)

  // Called from state machine to update state, button panel.
  //   The call changeState(newState, -2, newButtons)
  //     removes the old buttons and replaces with the new ones.
  //     (With this improvement, the second parameter is
  //      actually superfluous, but remains for historical reasons.

  // protected void changeState(
  //     int newState, int oldButtons, int newButtons)

  // Send message to another node.
  // protected void send(int message, int to, int parm1, int parm2)

  // Get node index from node name.
  // protected int FindNode(String b)

  // Constructor
  protected DistAlg(int i, DistAlg[] d, Screen s) {
    me      = i;
    number  = d.length;
    da      = d;
    screen  = s;
  }

  // Reset: alg-specific initialization and initial button panel.
  void reset() {
    init();
    updateDisplay(currentButtons, initialButtons);
  }

  // Construct a button panel which consists of
  //   an optional label and up to three buttons.
  // The method returns the code of the constructed panel.

  protected int addComponents(
          String c1, String c2, String c3, String c4) {
    JPanel p = initButtons(c1);
    int code = buttonPanels-1;
    if (c2 != null) addButton(p, code, c2, 0);
    if (c3 != null) addButton(p, code, c3, 1);
    if (c4 != null) addButton(p, code, c4, 2);
    return code;
  }

  // Create an button panel with the addresses of the
  //   other nodes and optionally with a special address.
  protected int addressButtons(String l, String special1, String special2) {
    JPanel p = initButtons(l);
    int code = buttonPanels-1;
    int n = 0;
    for (int i = 0; i < number; i++)
      if (i != me) addButton(p, code, Screen.node[i], n++);
    if (special1 != null) addButton(p, code, special1, n++);
    if (special2 != null) addButton(p, code, special2, n);
    return code;
  }

  // Change mnemonic of "index" button within previous panel to character "at".
  protected void changeMnemonic(int index, int at) {
    JButton b = translate[buttonPanels-1][index];
    b.setMnemonic(b.getText().charAt(at));
  }

  // Construct the panel for the button and add the label.
  // codeToPanel implements a translation between
  //   integer codes and the panels.
  private JPanel initButtons(String label) {
    JPanel p = new JPanel();
    p.setBorder(buttonBorder);
    p.setLayout(new FlowLayout());
    codeToPanel[buttonPanels++] = p;
    if (label == null) label = "";
    JLabel l = new JLabel(label, JLabel.LEFT);
    l.setForeground(Screen.LABELCOLOR);
    p.add(l);
    return p;
   }

  //  Add a button to a panel and set its listener and mnemonic.
  //  Parameters:
  //    panel itself and its code,
  //    the text of the button,
  //    the code of the button within the panel.
  //  translate maps from a panel code and a button code
  //    to a button object.
  private void addButton(JPanel p, int code, String b, int i) {
    JButton button = new JButton(b);
    button.addActionListener(this);
    button.setMnemonic(b.charAt(0));
    translate[code][i] = button;
    p.add(button);
  }

  // Initialize the user interface.

  protected void initialize() {

    // Properties of a panel.package daj.algorithms.tree;
    setFont(new Font("SansSerif", Font.BOLD, 14));
    setSize(400,280);
    setLayout(new BorderLayout());

    // Title panel has two panels for the rows.
    // Call algorithm-specific constructTitle.

    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new BorderLayout());
    title1.setText(constructTitle1());
    titlePanel.add("North", title1);
    title2.setText(constructTitle2());
    titlePanel.add("South", title2);
    title1.setForeground(Screen.colors[me]);
    title2.setForeground(Screen.colors[me]);
    add("North", titlePanel);

    // Data panels: one pair of panels for each node.
    // Call algorithm-specific constructRow.

    JPanel gp = new JPanel();
    gp.setLayout(new GridLayout(number-1, 1));
    row1 = new JLabel[number];
    row2 = new JLabel[number];
    for (int i = 0; i < number; i++)
      if (i != me) {
        row1[i] = new JLabel(constructRow1(i), JLabel.LEFT);
        row2[i] = new JLabel(constructRow2(i), JLabel.RIGHT);
        row1[i].setForeground(Screen.colors[i]);
        row2[i].setForeground(Screen.colors[i]);

		// Otto : Changed the code to add borders around the other nodes
		// to better separate between them
		 
        JPanel nodePanel = new JPanel(new GridLayout(2,1));
        nodePanel.setBorder(BorderFactory.createEtchedBorder());
        
        nodePanel.add(row1[i]);
        nodePanel.add(row2[i]);
        
        gp.add(nodePanel);
      }
    add("Center", gp);

    // Call alg-specific constructButtons for button panel.

    initialButtons = constructButtons();
    currentButtons = initialButtons;
    add("South", codeToPanel[initialButtons]);
  }

  // Action listener. Get string and call doAction.
  public void actionPerformed(ActionEvent evt) {
    doAction(evt.getActionCommand(), ' ');
  }

  // Perform the action.
  // Called with either a string or a character mnemonic.
  void doAction(String b, char c) {

    // Clicked on me, so set me as current panel.
    screen.setCurrentPanel(me);

    // Get label of current button panel.
    String buttonPanelLabel = 
	((JLabel) codeToPanel[currentButtons].getComponent(0)).getText();

    // If called with mnemonic, search translate array for the string.
    if (b == null) {
      for (int i = 0; i < translate[currentButtons].length; i++) {
        JButton button = translate[currentButtons][i];
        if ( (button != null) &&
             (button.getMnemonic() == Character.toUpperCase(c)) )
          b = button.getText();
      }
      if (b == null) return;
    }

    // Call algorithm-specific state machine with the string.
    // Call screen.trace to log and trace if required.
    reason = "";    
    stateMachine(b);
    screen.trace(me, buttonPanelLabel, b, reason);
  }

  // Assess an action by storing a string
  protected void assess(String s) {
    reason = s;
  }

  // Called by the state machine.
  // Store new state and update the display.
  protected void changeState(int newState, int oldButtons, int newButtons) {
    state = newState;
    updateDisplay(oldButtons, newButtons);
  }

  // Search for a node whose name matches a string.
  protected int FindNode(String b) {
    int toNode = 0;
    while ((toNode < number) &&
      !( b.trim().equals(Screen.node[toNode].trim())))
      toNode++;
    return toNode;
  }

  // Called upon reset and (indirectly) by the state machine to
  //   update the display by call constructTitle/Row,
  //   and to remove the old button panel and display the new one.
  private void updateDisplay(int oldPanel, int newPanel) {
    title1.setText(constructTitle1());
    if (screen.displayPrompts()) title2.setText(constructTitle2());
    for (int i = 0; i < number; i++)
      if (i != me) {
        row1[i].setText(constructRow1(i));
        if (screen.displayPrompts()) row2[i].setText(constructRow2(i));
    }

    if (oldPanel == -2)
      remove(codeToPanel[currentButtons]);
    else if (oldPanel >= 0)
      remove(codeToPanel[oldPanel]);
      
    if (newPanel >= 0) {
      add("South", codeToPanel[newPanel]);
      currentButtons = newPanel;
    }
    validate();
    repaint();
  }

  void clearPrompts() {
    title2.setText("");
    for (int i = 0; i < number; i++)
      if (i != me) 
        row2[i].setText("");
    repaint();
  }
  
  public void redisplay() {
	  updateDisplay(-1, -1);
  }

  //return the current screen
  Screen getScreen() {
      return screen;
  }
 
  // Variables passed from Screen in the constructor.

  protected int       number;      // Number of nodes.
  private   DistAlg[] da;          // References to the nodes
  protected Screen    screen;      // Screen object.

  // Variables used by this class.

  protected int me;                // ID of this node.
  protected int state;             // Current state of this node.
  private   int buttonPanels = 0;  // Number of button panels.
  private   int currentButtons;    // Current button panel (for reset).
  private   int initialButtons;    // Initial button panel (for reset).
  private   String reason;         // Reason for assessment.

  // User interface objects.

  private static final Border buttonBorder =
    BorderFactory.createEtchedBorder();

  private JLabel    title1 = new JLabel("", JLabel.LEFT);
  private JLabel    title2 = new JLabel("", JLabel.RIGHT);
  private JLabel[]  row1;
  private JLabel[]  row2;

  // Translation from code to panel and code to button within panel.

  // Ville: changed this to 15 because Neilsen-Mizuno uses more than 
  // 10 panels
  private static final int MAXPANELS  = 15;
  private static final int MAXBUTTONS = 8;
  private JPanel[]    codeToPanel = new JPanel[MAXPANELS];
  private JButton[][] translate   = new JButton[MAXPANELS][MAXBUTTONS];
}
