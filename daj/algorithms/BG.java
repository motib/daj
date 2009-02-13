//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//

//  Byzantine generals algorithm for distributed consensus.
//  Moti Ben-Ari

package daj.algorithms;
import daj.*;
import daj.algorithms.visual.*;

public class BG extends DistAlg {

  public BG(int i, DistAlg[] da, Screen s) {
    super(i, da, s);
    init();
    initialize();
  }

  // Initialize data structures.
  protected void init() {
    for (int r = 0; r < number; r++)
      for (int i = 0; i < number; i++) {
        message[r][i] = blankCode;
        sent[r][i]    = false;
      }
    myDecision = blankCode;
    state = initState;
    bias = (number % 2 == 0) ? 1 : 0;
    traitor = -1;
    // First general initializes the tree window
    if (me == 0 && (bgFrame == null)) 
            bgFrame = new BGFrame(number);
  }

  // Construct button panels.
  protected int constructButtons() {
    initButtons  = addComponents("Choose plan ", ATTACK, RETREAT, null);
    whatButtons  = addComponents("Which is to ", ATTACK, RETREAT, null);
    toButtons    = addressButtons("Send to",     ALL, null);
    aboutButtons = addressButtons("Plan of",     ME, null);
    return initButtons;
  }

  // Implement construction of titles and rows.

  // Display my name, plan and final decision.
  protected String constructTitle1() {
     return " General " + Screen.node[me] +
            ".  Plan is to " + plans[message[me][me]] +
            ".  Decision is to " + plans[myDecision];
  }

  //  Relay my plan to all other nodes.
  protected String constructTitle2() {
    return constructRelayLine(me);
  }

  // Display plan as sent by node g and
  //   what other nodes relay about g.
  protected String constructRow1(int g) {
    String s = " " + Screen.node[g] +
               " sent " + plans[message[g][g]];
    for (int i = 0; i < number; i++)
      if ((i != me) && (i != g))
         s = s + ",   " + Screen.node[i] +
             " relayed " + plans[message[i][g]];
      s = s + ".       Vote " + plans[message[me][g]];
      return s;
  }

  //  Relay g's plan to all other nodes.
  protected String constructRow2(int g) {
    return constructRelayLine(g);
  }

  //  Relay info about g to all nodes except me and g.
  private String constructRelayLine(int g) {
    String s = " Relay plan to ";
    int count = 0;
    if (message[g][g] != blankCode) 
      for (int i = 0; i < number; i++)
        if ((i != me) && (i != g) && (!sent[i][g])) {
          s = s + Screen.node[i] + "  ";
          count++;
        }
    if (count == 0) return " "; else return s + "       ";
  }

  //  Initially choose plan attack or retreat.
  private boolean choosePlan(String b) {
    message[me][me] = b.equals(ATTACK) ? attackCode : retreatCode;
    Vote(me);
    changeState(toState, initButtons, toButtons);
    bgFrame.getPanel(me).createRoot(me, message[me][me], number); //AP, MB
    return true;
  }

  //  Get destination for sending plan to b.
  //  Store node number in toGeneral.
  private boolean SendPlan(String b) {
    boolean found = false;
    toGeneral = FindNode(b);
    for (int j = 0; j < number; j++) {
      found = (toGeneral != j) &&
              (message[j][j] != blankCode) && !sent[toGeneral][j];
      if (found) break;
    }
    if (!found) {
      assess(NONEED + b);
      return false;
    }
    changeState(aboutState, toButtons, aboutButtons);
    return true;
  }

  //  Implements action of loyal general:
  //    if my plan not sent, send to all other nodes.
  //    otherwise, send all outstanding relay obligations.

  private boolean SendAll() {
    boolean relayedMyPlan = false;
    for (int i = 0; i < number; i++)
      if ((i != me) && !sent[i][me]) {
	  send(me, i, message[me][me], me);
	  sent[i][me] = true;
	  relayedMyPlan = true;
      }
    if (!relayedMyPlan)
      for (int i = 0; i < number; i++)
        for (int j = 0; j < number; j++)
          if ((i != me) && (j != i) &&
              (message[j][j] != blankCode) && !sent[i][j]) {
	      send(me, i, message[j][j], j);
	      sent[i][j] = true;
          }
    redisplay(); // Update display, don't change state.
    return true;
  }

  //  For sending b's plan, store node number in aboutGeneral.
  private boolean About(String b) {
    if (b.equals(ME)) aboutGeneral = me;
    else aboutGeneral = FindNode(b);
    if (toGeneral == aboutGeneral) 
        assess(ITSELF);
    else if (message[aboutGeneral][aboutGeneral] == blankCode)
        assess(NOTRECEIVED + b);
    else if (sent[toGeneral][aboutGeneral])
        assess(ALREADY + b);
    else { // OK
      changeState(whatState, aboutButtons, whatButtons);
      return true;
    }
    return false;
  }

  // Send plan b of aboutGeneral to node toGeneral.
  private boolean What(String b) {
    int whichPlan = b.equals(ATTACK) ? attackCode : retreatCode;
    if (message[aboutGeneral][aboutGeneral] != whichPlan) {
      if (traitor < 0) traitor = me;
      else assess(Screen.node[traitor] + TRAITOR);
    }

    sent[toGeneral][aboutGeneral] = true;
    changeState(toState, whatButtons, toButtons);
    send(me, toGeneral, whichPlan, aboutGeneral);
    return true;
  }

  //  Implement state machine.
  protected boolean stateMachine(String b) {
    switch(state) {
      case initState:  return choosePlan(b);
      case toState:
        if (b.equals(ALL)) return SendAll();
        else return SendPlan(b);
      case aboutState: return About(b);
      case whatState:  return What(b);
      default:         return false;
    }
  }

  //  Receive message: "from" souce node sending plan "p" about node "by".
  protected void receive(int from, int p, int by) {
    message[from][by] = p;
    Vote(by);
    redisplay();  // Update display, don't change state.
    bgFrame.getPanel(by).addNode(from, me, p);         //AP, MB
  }

  //  When you have received messages from the other generals
  //    concerning "by"'s plan, vote on what his plan really was.
  private void Vote(int by) {
    int attacks = 0, retreats = 0;
    for (int i = 0; i < number; i++)
      if (i != me)
        if (message[i][by] == attackCode) attacks++;
        else if (message[i][by] == retreatCode) retreats++;
    if (attacks >= (number/2)+(1-bias)) message[me][by] = attackCode;
    else if (retreats >= (number/2)) message[me][by] = retreatCode;
    checkDecision();
  }

  //  When you have voted on all the other generals plan,
  //    make your final decision (including your own plan).
  //  Resolve ties in favor of retreat.
  private void checkDecision() {
    int attacks = 0, retreats = 0;
    myDecision = blankCode;
    for (int i = 0; i < number; i++)
      if (message[me][i] == attackCode) attacks++;
      else if (message[me][i] == retreatCode) retreats++;
    if (attacks >= ((number+1)/2)+bias) myDecision = attackCode;
    else if (retreats >= (number+1)/2) myDecision = retreatCode;
  }

  protected void disposeFrame() { bgFrame.dispose(); bgFrame = null; }
  protected void setFrameVisible(boolean b) { bgFrame.setVisible(b); }
  protected void resetFrame() { bgFrame.reset(); }
  protected boolean hasVisualization() { return true; }
  private static BGFrame bgFrame;  // Frame for message trees //AP

  private int myDecision;    // My final decision.
  private int toGeneral;     // Store number of destination node.
  private int aboutGeneral;  // Store number of "about" node.
  private int bias;          // For even-odd difference of majority vote.
  private int initButtons, toButtons, aboutButtons, whatButtons;
                             // Codes of button panels.
  private static int traitor;// Identity of traitor for assessment.

  // Data structure for messages

  private int[][] message  = new int[number][number];
       // Within node "me", message[i][j] == Code means that:
       //   "me" knows that "i" says that "j" sent him "Code"
       // Special cases:
       //   message[me][me] == my chosen plan
       //   message[i][i]   == what "i" reports about his plan
       //   message[me][i]  == majority vote of what others said about "i"

  private boolean[][] sent  = new boolean[number][number];
       // sent[i][j] == true, if "me" has sent "j"'s plan to "i"

  // State codes.

  private static final int initState   = 0;
  private static final int toState     = 1;
  private static final int aboutState  = 2;
  private static final int whatState   = 3;

  // Codes for plans.

  private static final int blankCode    = 0;
  private static final int attackCode   = 1;
  private static final int retreatCode  = 2;

  // Strings for buttons.
  private static final String
    ATTACK  = "Attack",
    RETREAT = "Retreat",
    ME      = "Me",
    ALL     = "All";

  // Characters for plans
  public static final char[] plans = 
    {'?', ATTACK.charAt(0), RETREAT.charAt(0)};

  // Strings for assessment.
  private static final String
    ITSELF      = "Can't send plan of node to itself",
    NONEED      = "No message needs to be sent to ",
    NOTRECEIVED = "Haven't yet received plan of ",
    ALREADY     = "Already sent plan of ",
    TRAITOR     = " already is a traitor";
}
