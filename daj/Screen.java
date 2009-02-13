//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//  Moti Ben-Ari
//  Class Screen is the frame on which the nodes are displayed.
//    Global interaction (Reset, Exit, etc.) are processed here.
//    Screen is the KeyListener for the program.
//  Individual algorithm classes directly access the static constants:
//    String[] node  - the names  of the nodes
//    Color[]  color - the colors of the nodes
package daj;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
//import daj.algorithms.*;

public class Screen extends JFrame implements ActionListener, KeyListener {
  // Constructor called with an algorithm code (bg, ra, etc.),
  //   the number of nodes and a flag if it is an application or not.
  Screen(String s, String num, WaitObject w) {
    waitObject = w;

    // Search for the algorithm code.
    AlgList alg = new AlgList(MAXNODES, DEFAULTNODES);
    int index = alg.getAlg(s);
	if (index == 0) System.exit(0);
    // Get number of nodes from parameter or default.
    try {
      numNodes = Integer.parseInt(num);
      if ((numNodes < 2) || (numNodes > MAXNODES))
        numNodes = DEFAULTNODES;
    }
    catch (NumberFormatException e) {
      numNodes = DEFAULTNODES;
    }
    numNodes = alg.getNodes(numNodes);

    if (isApplication) {
      logfile = new LogFile(numNodes);
      enableButtons = enableApplication;
    }
    else
      enableButtons = enableApplet;

    // Allocate global array of references to the algorithms
    //  for passing data from one node to another.
    da = new DistAlg[numNodes];

    //  Create instances of the algorithm and
    //    highlight the border of the first instance.
    for (int i = 0; i < numNodes; i++) {
      da[i] = alg.getAlgObject(index, i, da, this);
      if (i == 0) da[i].setBorder(currentBorder);
      else da[i].setBorder(otherBorder);
    }

    // Display algorithm title and initialize display.
    initDisplay(VERSION + VERSIONNUMBER + "    " + alg.getAlgName(index));
  }

  // Initialize the display
  private void initDisplay(String title) {
    // Set properties of main frame.
    setFont(new Font(FONT, FONTWEIGHT, FONTSIZE));
    setTitle(title);
    setSize(FRAMEWIDTH, FRAMEHEIGHT);
    if (isApplication) setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addKeyListener(this);
    setLocationRelativeTo(this);
    Container contentPane = getContentPane();
    contentPane.setLayout(new BorderLayout());

    // Set up grid for nodes.
    int rows = (numNodes + (numNodes % 2)) / 2;
    mainPanel.setLayout(new GridLayout(rows, 2, 2, 2));
    for (int i = 0; i < numNodes; i++)
      mainPanel.add(da[i]);
    contentPane.add(mainPanel, "Center");

    // Set up panel for global buttons.
    int buttonsPerRow = buttons.length/2;
    global.setLayout(new GridLayout(2, buttonsPerRow, 2, 2));

    // Set each button in panel
    // For algorithms with visualization, add an extra pair of buttons
    if (da[0].hasVisualization()) {
	    enableButtons[buttonsPerRow-1]  = true;
	    enableButtons[buttons.length-1] = true;
    } else {
	    enableButtons[buttonsPerRow-1]  = false;
	    enableButtons[buttons.length-1] = false;
    }
    for (int i = 0; i < buttons.length; i++) 
      if (enableButtons[i]) {
        global.add(buttons[i]);
        buttons[i].setMnemonic(buttons[i].getText().charAt(mnemonics[i]));
        buttons[i].addActionListener(this);
      }
      else {
        JButton emptyButton = new JButton();
        emptyButton.setBorder(new EmptyBorder(0,0,0,0));
        global.add(emptyButton);
      }

    // Make pairs of radio buttons
    for (int i = 0; i < bgroups.length; i++) {
        bgroups[i] = new ButtonGroup();
        bgroups[i].add(buttons[groupIndices[i]]);
        bgroups[i].add(buttons[groupIndices[i]+buttonsPerRow]);
      }

    //change the border color of the global panel
    global.setBorder(BorderFactory.createLineBorder(BORDER1COLOR));
    contentPane.add(global, "South");

    // Create frame for trace
    traceFrame.setTitle(TRACE);
    traceFrame.setSize(FRAMEWIDTH / 2, FRAMEHEIGHT / 4);
    traceFrame.getContentPane().add(scroll);
    traceFrame.setVisible(true);

    setVisible(true);
  }

  // Trace an action for a node me.
  // Parameters are the button panel label, the button and 
  //   a reason for assessment.
  // Log the action if necessary with the trace as a comment
  void trace(int me, String buttonPanelLabel, String button, String reason) {
    String width = (stepcount < 10) ? "  " : ((stepcount < 100) ? " " : "");
    String s = width + stepcount + ". " + node[me] + " " + buttonPanelLabel + 
               " " + button + " : " + reason + "\n";
    traceArea.append(s);
    if (loggingOn)
      logfile.write(me + " " + button + 
        BLANKS.substring(0, BLANKS.length()-button.length()) +
        " // " + s);
    stepcount++;
  }

  // Remove special border from currentPanel,
  //  and set special border for a newPanel,
  void setCurrentPanel(int newPanel) {
    da[currentPanel].setBorder(otherBorder);
    da[newPanel].setBorder(currentBorder);
    currentPanel = newPanel;
  }

  // Increments currentPanel cyclically through the nodes.
  private void nextCurrentPanel() {
    setCurrentPanel((currentPanel + 1) % numNodes);
  }

  // Get command string of event and call doAction.
  public void actionPerformed(ActionEvent evt) {
    doAction(evt.getActionCommand());
    requestFocusInWindow();
  }

  // Perform action depending on button pressed.
  private void doAction(String b) {
    if (b.equals(EXIT)) {
      if (loggingOn) logfile.CloseWriter();
      if (autorunOn) logfile.CloseReader();
      traceFrame.dispose();
	  da[0].disposeFrame();
      dispose();
      waitObject.signalOK();
    }
    else if (b.equals(STEP)) {
      if (autorunOn) 
        if (! logfile.step(da)) {
          logfile.CloseReader();
        }
    }
    else if (b.equals(FILE)) {
      if (loggingOn) logfile.CloseWriter();
      if (autorunOn) logfile.CloseReader();
      logfile.RenameFiles();
      doAction(RESET);
    }
    else if (b.equals(RESET)) {
      for (int i = 0; i < numNodes; i++) da[i].reset();
      stepcount = 1;
      setCurrentPanel(0);
      if (loggingOn) logfile.OpenWriter();
      if (autorunOn) logfile.OpenReader();
      da[0].resetFrame();
      repaint();
    }
    else if (b.equals(LOGON)) {
      loggingOn = true;
      logfile.OpenWriter();
    }
    else if (b.equals(LOGOFF)) {
      loggingOn = false; 
      logfile.CloseWriter();
    }
    else if (b.equals(AUTOON)) {
      autorunOn = true;
      logfile.OpenReader();
    }
    else if (b.equals(AUTOOFF)) {
      autorunOn = false;
      logfile.CloseReader();
    }
    else if (b.equals(PROMPTON)) {
      promptsOn = true;
      for (int i = 0; i < da.length; i++) 
        da[i].redisplay();
    }
    else if (b.equals(PROMPTOFF)) {
      promptsOn = false;
      for (int i = 0; i < da.length; i++) 
        da[i].clearPrompts();
    }
    else if (b.equals(TRACEON)) {
      traceOn = true;
      traceFrame.setVisible(true);
    }
    else if (b.equals(TRACEOFF)) {
      traceOn = false;
      traceFrame.setVisible(false);
    }
    else if (b.equals(TREEON)) { //AP
	  treeOn = true;
	  da[0].setFrameVisible(true);
    } //AP
    else if (b.equals(TREEOFF)) { //AP
	  treeOn = false;
	  da[0].setFrameVisible(false);
    } //AP
  }

  // Accessors for attributes.
  boolean displayPrompts() { return promptsOn; }
  
  // Implementation of the KeyListener interface.
  public boolean isFocusable() { return true; }
  public void keyReleased(KeyEvent evt) {}
  public void keyPressed(KeyEvent evt) {}
  public void keyTyped(KeyEvent evt) {
    char c = evt.getKeyChar();
    if ((int) c == KeyEvent.VK_ENTER)     // Enter cycles among nodes
      nextCurrentPanel();
    else
      da[currentPanel].doAction(null, c);
  }

  // Variables

  // Names and colors of the nodes (public constants).
  public static final String[] node  =
    {  "Basil", "Ivan ", "John ", "Leo  ", "Mike ", "Zoe  " };
  public static final Color[]  colors  = {
      new Color(0,0,230),   new Color(230,0,200),
      new Color(180,70,0),  new Color(230,0,0),
      new Color(255,100,0), new Color(0,80,0)
  };
  public static final Color  LABELCOLOR   = Color.black;
  public static boolean isApplication;     // Application or applet?
  
  private int      stepcount = 1;    // Count of algorithm steps.
  private int      currentPanel = 0; // Current panel for keypressed.
  private LogFile  logfile;          // The logfile object.
  private DistAlg[] da;              // A reference to each node.
  private int      numNodes;         // Number of nodes.
  private WaitObject waitObject;     // For signalling on Exit.

  // Options with initial values.
  private boolean  promptsOn = true;  // Display prompts or not?
  private boolean  traceOn   = true;  // Display trace or not?
  private boolean  loggingOn = false; // Write to log file or not?
  private boolean  autorunOn = false; // Auto run from log file or not?
  private boolean  treeOn    = true;  //Display the tree or not

  private static final int DEFAULTNODES = 4;
  private static final int MAXNODES = node.length;

  // Properties of frame
  private static final String FONT       = "SansSerif";
  private static final int    FONTWEIGHT = Font.BOLD;
  private static final int    FONTSIZE   = 12;
  private static final int    FRAMEWIDTH  = 800;
  private static final int    FRAMEHEIGHT = 600;
  private static final Color  BORDER1COLOR = Color.blue;
  private static final Color  BORDER2COLOR = new Color(128,128,128);
  private static final int inset = 4;
  private static final Border currentBorder =
    BorderFactory.createMatteBorder(inset,inset,inset,inset,BORDER1COLOR);
  private static final Border otherBorder =
    BorderFactory.createMatteBorder(inset,inset,inset,inset,BORDER2COLOR);

  // GUI components
  private JFrame  traceFrame  = new JFrame();
  private JPanel  mainPanel   = new JPanel();
  private JPanel  global      = new JPanel();
  private JTextArea traceArea = new JTextArea();
  private JScrollPane scroll  = new JScrollPane(traceArea);

  // Strings for buttons
  public static final String VERSIONNUMBER = "3.4";
  public static final String VERSION   = "DAJ V";
  private static final String
    TRACE     = "Action trace",
    RESET     = "Reset",
    EXIT      = "New",  // Returns to select algorithm instead of exit
    STEP      = "Step",
    FILE      = "FileX",
//    ON        = "On",
//    OFF       = "Off",
    LOGON     = "Log on",
    AUTOON    = "Auto on",
    PROMPTON  = "Prompt on",
    TRACEON   = "Trace on",
    TREEON    = "Graphics on",   //AP //MB change name
    LOGOFF    = "Log off",
    AUTOOFF   = "Auto off",
    PROMPTOFF = "Prompt off",
    TRACEOFF  = "Trace off",
    TREEOFF   = "Graphics off",  //AP // MB change name
    BLANKS    = "          ";

  // Array of buttons; order determines position in GridLayout.
  // Tree on/off button pair should be last (see initDisplay).
  private AbstractButton[] buttons = { 
    new JButton(RESET), 
    new JButton(FILE),
    new JRadioButton(LOGON,      loggingOn), 
    new JRadioButton(AUTOON,     autorunOn),
    new JRadioButton(PROMPTON,   promptsOn), 
    new JRadioButton(TRACEON,    traceOn),
    new JRadioButton(TREEON,     treeOn),  // AP
    new JButton(EXIT), 
    new JButton(STEP), 
    new JRadioButton(LOGOFF,    !loggingOn), 
    new JRadioButton(AUTOOFF,   !autorunOn),
    new JRadioButton(PROMPTOFF, !promptsOn),
    new JRadioButton(TRACEOFF,  !traceOn),
    new JRadioButton(TREEOFF,   !treeOn)  // AP
  };

  // The following data structures use same ordering as buttons array.
  // Individual buttons may be removed from the GUI
  // Different set for application and applet.
  private static boolean[] enableButtons;
  private static boolean[] enableApplication =
    {true,true,true,true,true,true, true,
     true,true,true,true,true,true, true}; 
  private static final boolean[] enableApplet =
    {true,false,false,false,true,true, true,
     true,false,false,false,true,true, true}; 

  // Mnemonics as indices in button label.
  private static final int   mnemonics[]  = 
    {0,0,0,0,0,0,4,
     0,0,2,1,3,3,9};

  // Button groups and indices of radio buttons for creating groups.
  private ButtonGroup[] bgroups = new ButtonGroup[5];
  private static final int[] groupIndices = {2, 3, 4, 5, 6};
}
