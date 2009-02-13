//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//

//  Ricart-Agrawala algorithm for distributed mutual exclusion
//  Moti Ben-Ari.

package daj.algorithms;
import daj.*;
import daj.algorithms.visual.*; 

public class RA extends DistAlg {

  public RA(int i, DistAlg[] da, Screen s) {
    super(i, da, s);
    init();
    initialize();
  }

  // Initialize data structures.
  protected void init() {
    for (int i = 0; i < number; i++) {
      deferred[i]  = false;
      needToSendRequest[i] = false;
      needToSendReply[i] = false;
      replyReceived[i] = false;
      numberReceived[i] = 0;
    }
    state = enterState;
    highNumber = 0;
    chosenNumber = 0;
    wantToEnter = false;
    if ((me == 0) && (raFrame == null)) 
		raFrame = new RAFrame();
  }

  // Construct button panels.
  protected int constructButtons() {
    chooseButtons  = addComponents(TRY,  CHOOSE,  REPLY, null);
    sendButtons    = addComponents("",  REQUEST, REPLY, null);
    changeMnemonic(0, 2);
    deferredButtons= addComponents("",  REPLY, null, null);
    leaveButtons   = addComponents(CS,    LEAVE,   null,  null);
    toButtons      = addressButtons(TO, ALL, null);
    return chooseButtons;
  }

  // Implement construction of titles and rows.

  //  Node name and highest/chosen number.
  protected String constructTitle1() {
    String s = " " + Screen.node[me] +
               ".  Highest num received: " + highNumber + ".";
    if (wantToEnter)
      s = s + "   Num chosen: " + chosenNumber + ".";
    return s;
  }

  //  Send request to other nodes.
  protected String constructTitle2() {
    String s = " Send request to ";
    for (int i = 0; i < number; i++)
      if (needToSendRequest[i])
        s = s + Screen.node[i] + " ";
    if (wantToEnter && any(needToSendRequest))
      return s + "    ";
    else
      return " ";
  }

  //  Display requests sent and replies received for node g.
  protected String constructRow1(int g) {
    String s = " " + Screen.node[g];
    if (numberReceived[g] != 0)
      s = s + " sent " + numberReceived[g] + ".   ";
    if (replyReceived[g])
      s = s + " Reply received.";
    return s;
  }

  //  Display replies that must be sent to g or are deferred.
  protected String constructRow2(int g) {
    String s = " ";
    if (needToSendReply[g])
      s = s + " Reply to " + Screen.node[g] + ".   ";
    else if (deferred[g])
      s = s + " Defer reply to " + Screen.node[g] + ".   ";
    return s;
  }

  //  Choose number and set flags for requests.
  private boolean choose() {
    chosenNumber = highNumber+1;
    wantToEnter = true;
    for (int i = 0; i < number; i++)
      if (i != me)
        needToSendRequest[i] = true;
    changeState(requestreplyState, chooseButtons, sendButtons);
    return true;
  }

  //  Common processing to send a request or reply.
  private boolean sendRR(int Plan, int newState, int oldButtons) {
    whichPlan = Plan;
    changeState(newState, oldButtons, toButtons);
    return true;
  }

  //  Get address of "b" to send message.
  //  If ALL, send to all nodes,
  //    else find number and store in toNodes.

  private boolean address(String b) {
    if (b.equals(ALL))
      for (int i = 0; i < number; i++) {
        if (i != me) Send(i);
    }
    else
      Send(FindNode(b));

    //  State changes:
    //  If request, request/reply state and panel.
    if (whichPlan == requestCode) {
      changeState(requestreplyState, toButtons, sendButtons);
      return true;
    }
    //  If reply:
    //    Deferred reply: deferred state and request/reply panel.
    //    Reply after request: request/reply state and panel.
    //    Reply before request: choose state and panel.
    else {  // whichPlan == replyCode
      if ((state == todeferredState) && any(needToSendReply))
        changeState(senddeferredState, toButtons, deferredButtons);
      else if (wantToEnter || any(deferred))
        changeState(requestreplyState, toButtons, sendButtons);
      else
        changeState(enterState, toButtons, chooseButtons);
      return true;
    }
  }

  //  Send "toNode" request or reply in "whichPlan".
  private boolean Send(int toNode) {
    if (whichPlan == requestCode) {
      if (! needToSendRequest[toNode]) {
        assess(NOREQUEST + Screen.node[toNode]);
        return false;
      }
      needToSendRequest[toNode] = false;
      send(requestCode, toNode, me, chosenNumber);
      return true;
    }
    else {  // whichPlan == replyCode
      if (needToSendReply[toNode])
        send(replyCode, toNode, me, 0);
      else {
        assess(NOREPLY + Screen.node[toNode]);
        return false;
      }
      needToSendReply[toNode] = false;

      // See constructRow1:
      //   do not display requests which have been replied to.
      numberReceived[toNode] = 0;
      return true;
    }
  }

  //  Leave critical section.
  //  Mark all deferred replies so they will be sent.
  private boolean leave() {
    wantToEnter = false;
    for (int i = 0; i < number; i++) {
      if (deferred[i])
        needToSendReply[i] = true;
      deferred[i] = false;
      replyReceived[i] = false;
    }
    if (any(needToSendReply))
      changeState(senddeferredState, leaveButtons, deferredButtons);
    else
      changeState(enterState, leaveButtons, chooseButtons);
    return true;
  }

  //  Implement state machine.
  protected boolean stateMachine(String b) {
    switch (state) {
    case enterState:
      if (b.equals(CHOOSE)) 
        return choose();
      else if (b.equals(REPLY))
        if (any(needToSendReply))
          return sendRR(replyCode, toState, chooseButtons);
        else {
          assess(NOREPLY + ANYNODE);
          return false;
        }

    case requestreplyState:
      if (b.equals(REQUEST)) 
        if (any(needToSendRequest))
          return sendRR(requestCode, toState, sendButtons);
        else {
          assess(NOREQUEST + ANYNODE);
          return false;
        }
      else if (b.equals(REPLY))
        if (any(needToSendReply))
          return sendRR(replyCode, toState, sendButtons);
        else {
          assess(NOREPLY + ANYNODE);
          return false;
        }

    case senddeferredState:
      return sendRR(replyCode, todeferredState, deferredButtons);
    case toState:
    case todeferredState:
      return address(b);
    case leaveState:
      return leave();
    default:
      return false;
    }
  }

  //  Check flag array to see if any flags are set.
  private boolean any(boolean[] a) {
    for (int i = 0; i < number; i++)
      if ((i != me) && a[i])
        return true;
    return false;
  }

  //  Count number of flags received,
  //    return true iff equal number-1.
  private boolean allRepliesReceived() {
    int count = 0;
    for (int i = 0; i < number; i++)
      if ((i != me) && (replyReceived[i])) count++;
    return count == number - 1;
  }

  //  Message received.
  //  If request, update high sequence number and received number,
  //    and decide to send reply or defer.

  protected void receive(int m, int by, int num) {
    if (m == requestCode) {
      numberReceived[by] = num;
      raFrame.addQueued(by, num);
      if (num > highNumber) {
        highNumber = num;
      }
      if (!wantToEnter)
        needToSendReply[by] = true;
      else if ( (num > chosenNumber) ||
            ((num == chosenNumber) && (me < by)) ) {
        deferred[by] = true;
      }
      else
        needToSendReply[by] = true;
    }

    //  If reply, check if all replies received.
    else if (m == replyCode) {
      replyReceived[by] = true;
      if (allRepliesReceived()) {
          raFrame.removeQueued();
          changeState(leaveState, sendButtons, leaveButtons);
        return;
      }
    }
    redisplay();  // Update display, don't change state.
  }

  protected void disposeFrame() { raFrame.dispose(); raFrame = null; }
  protected void setFrameVisible(boolean b) { raFrame.setVisible(b); }
  protected void resetFrame() { raFrame.reset(); }
  protected boolean hasVisualization() { return true; }
  private static RAFrame raFrame;
  
  private int     highNumber;   // Highest number received.
  private int     chosenNumber; // Number chosen to enter CS.
  private boolean wantToEnter;  // Want to enter CS.
//  private int     toNode;       // Node to send message.
  private int     whichPlan;    // Send request or reply.
  private int chooseButtons, toButtons, 
      leaveButtons, sendButtons, deferredButtons;
                                // Button panel codes.

  // Information on each node.
  private boolean[] deferred          = new boolean[number];
  private boolean[] needToSendRequest = new boolean[number];
  private boolean[] needToSendReply   = new boolean[number];
  private int[]     numberReceived    = new int[number];
  private boolean[] replyReceived     = new boolean[number];

  // Message codes.
  private static final int requestCode = 1;
  private static final int replyCode   = 2;

  // State codes.
  // Special states for reply/address of deferred node.
  private static final int enterState        = 0;
  private static final int leaveState        = 1;
  private static final int requestreplyState = 2;
  private static final int toState           = 3;
  private static final int senddeferredState = 4;
  private static final int todeferredState   = 5;

  // Strings for buttons.
  private static final String
    TRY = "Try to enter CS", TO = "To",
    REQUEST = "Request", REPLY   = "Reply", CHOOSE  = "Choose",
    CS      = "In critical section", LEAVE   = "Leave", ALL     = "All";

  // Strings for assessment.
  private static final String
    NOREPLY   = "No need to send answer to ",
    NOREQUEST = "No need to send request to ",
    ANYNODE   = "any node";
}
