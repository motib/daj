//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//

//  Distributed consensus with crash failure of a node.
//  Moti Ben-Ari

package daj.algorithms;
import daj.*;
import daj.algorithms.visual.*;

public class CR extends DistAlg {

  public CR(int i, DistAlg[] da, Screen s) {
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
    // First general initializes the tree window
    if (me == 0 && (bgFrame == null)) 
            bgFrame = new BGFrame(number);
  }

  // Construct button panels.
  protected int constructButtons() {
    initButtons  = addComponents("Choose plan ", ATTACK, RETREAT, null);
    toButtons    = addressButtons("Send to",     ALL, CRASH);
    aboutButtons = addressButtons("Plan of",     ME,  null);
    crashButtons = addComponents("", CRASH, null, null);
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
    if (b.equals(ATTACK)) message[me][me] = attackCode;
    else if (b.equals(RETREAT)) message[me][me] = retreatCode;
    else return false;
    Vote(me);
    changeState(toState, initButtons, toButtons);
    bgFrame.getPanel(me).createRoot(me, message[me][me], number);
    return true;
  }

  //  Get destination for sending my plan to b.
  //  Store node number in toGeneral.
  private boolean SendPlan(String b) {
    boolean found = false;
    toGeneral = FindNode(b);
    for (int j = 0; j < number; j++) {
      found = (toGeneral != j) &&
              (message[j][j] != blankCode) && !sent[toGeneral][j];
      if (found) break;
    }
    if (!found) return false;
    changeState(aboutState, toButtons, aboutButtons);
    return true;
  }

  //  Implements action of loyal general:
  //    if my plan not sent, send to all other nodes.
  //    otherwise, send all outstanding relay obligations.
  //    if crashed, send crash code instead of plan,
  //       and change buttons so only crash is possible.
  private boolean SendAll(boolean crashed) {
    boolean relayedMyPlan = false;
    for (int i = 0; i < number; i++)
      if ((i != me) && !sent[i][me]) {
        send(me, i, crashed ? crashCode : message[me][me], me);
        sent[i][me] = true;
        relayedMyPlan = true;
      }

    if (!relayedMyPlan)
      for (int i = 0; i < number; i++)
        for (int j = 0; j < number; j++)
          if ((i != me) && (j != i) &&
              (message[j][j] != blankCode) && !sent[i][j]) {
            send(me, i, crashed ? crashCode : message[j][j], j);
            sent[i][j] = true;
          }
    if (crashed)
      changeState(toState, toButtons, crashButtons);
    else
      redisplay(); // Update display, don't change state.
    return true;
  }

  //  For sending b's plan, store node number in aboutGeneral.
  //  As long as you don't crash, send what aboutGeneral told you. 
  private boolean About(String b) {
    boolean found = false;
    if (b.equals(ME)) {
      aboutGeneral = me;
      found = true;
    }
    else {
      aboutGeneral = FindNode(b);
      found = (toGeneral != aboutGeneral) &&
              (message[aboutGeneral][aboutGeneral] != blankCode) &&
                  !sent[toGeneral][aboutGeneral];
    }
    if (!found) return false;
    sent[toGeneral][aboutGeneral] = true;
    changeState(toState, aboutButtons, toButtons);
    send(me, toGeneral, message[aboutGeneral][aboutGeneral], aboutGeneral);
    return true;
  }

  //  Implement state machine.
  protected boolean stateMachine(String b) {
    switch(state) {
      case initState:  return choosePlan(b);
      case toState:
        if (b.equals(ALL) || b.equals(CRASH))
          return SendAll(b.equals(CRASH));
        else return SendPlan(b);
      case aboutState: return About(b);
      default:         return false;
    }
  }

  //  Receive message: "from" souce node sending plan "p" about node "by".
  protected void receive(int from, int p, int by) {
    message[from][by] = p;
    Vote(by);
    redisplay();  // Update display, don't change state.
    bgFrame.getPanel(by).addNode(from, me, p);
  }

  //  When you have received messages from the other generals
  //    concerning "by"'s plan, vote on what his plan really was.
  //  For crash:
  //    there will be no different plans,
  //    so it is enough if one there is one plan.
  //    If all others say that a general has crashed,
  //      set "crash" as his plan.
  private void Vote(int by) {
    int crashes = 0;
    for (int i = 0; i < number; i++)
      if (i != me)
        if (message[i][by] == attackCode)
          message[me][by] = attackCode;
        else if (message[i][by] == retreatCode)
          message[me][by] = retreatCode;
        else if (message[i][by] == crashCode)
          crashes++;
    if (crashes == (number-1)) message[me][by] = crashCode;
    checkDecision();
  }

  //  When you have voted on all the other generals plan,
  //    make your final decision (including your own plan).
  //  Resolve ties in favor of retreat.
  //  Crashed nodes are not taken into account in computing majority.
  private void checkDecision() {
    int attacks = 0, retreats = 0, crashes = 0;
    myDecision = blankCode;
    for (int i = 0; i < number; i++)
      if      (message[me][i] == attackCode)  attacks++;
      else if (message[me][i] == retreatCode) retreats++;
      else if (message[me][i] == crashCode)   crashes++;
    int running = number - crashes;        // Number of non-crashed nodes.
    int bias = (running % 2 == 0) ? 0 : 1; // Bias for odd.
    if (attacks > running/2) myDecision = attackCode;
    else if (retreats >= (running/2)+bias)  myDecision = retreatCode;
  }

  protected void disposeFrame() { bgFrame.dispose(); bgFrame = null; }
  protected void setFrameVisible(boolean b) { bgFrame.setVisible(b); }
  protected void resetFrame() { bgFrame.reset(); }
  protected boolean hasVisualization() { return true; }
  private static BGFrame bgFrame;  // Frame for message trees //AP

  private int myDecision;    // My final decision.
  private int toGeneral;     // Store number of destination node.
  private int aboutGeneral;  // Store number of "about" node.
  private int initButtons, toButtons, aboutButtons, crashButtons;  
                            // Codes of button panels.

  // Data structure for messages

  private int[][] message  = new int[number][number];
       // Within applet "me", message[i][j] == Code means that:
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

  // Codes and strings for plans.

  private static final int blankCode    = 0;
  private static final int attackCode   = 1;
  private static final int retreatCode  = 2;
  private static final int crashCode    = 3;
  private static final String[] plans   = {"?", "A", "R", "-"};

  // Strings for buttons.
  private static final String
    ATTACK  = "Attack",
    RETREAT = "Retreat",
    ME      = "Me",
    ALL     = "All",
    CRASH   = "Crash";
}

