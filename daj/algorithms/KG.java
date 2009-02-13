//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//

//  Byzantine generals algorithm for distributed consensus.
//  Polynomial algorithm with a king.
//  Moti Ben-Ari

package daj.algorithms;
import daj.*;

public class KG extends DistAlg {

//  Compiled constants for the algorithm.
  private static final int TRAITORS  = 1;
  private static final int FIRSTKING = 0;

  public KG(int i, DistAlg[] da, Screen s) {
    super(i, da, s);
    init();
    initialize();
  }

  // Initialize data structures.
  protected void init() {
    initData();
    choice[me] = blankCode;  // Initially no choice
    state = initState;
    if (me == FIRSTKING) currentKing = FIRSTKING;
  }

  // Initialize data before round.
  // Do not initialize currentKing or my choice.
  private void initData() {
    for (int r = 0; r < number; r++) {
      if (r != me) choice[r] = blankCode;
      sent[r]   = false;
    }
    majorityDecision = blankCode;
    votes = 0;
    kingDecision = blankCode;
  }

  // Construct button panels.
  protected int constructButtons() {
    initButtons  = addComponents("Choose plan ", ATTACK, RETREAT, null);
    whatButtons  = addComponents("Which is to ", ATTACK, RETREAT, null);
    toButtons    = addressButtons("Send to", ALL, null);
    kingButtons  = addressButtons("King to", ALL, null);
    waitButtons  = addComponents("Waiting for king", null, null, null);
    goButtons    = addComponents("", "Continue", null, null);
    nullButtons  = addComponents("", null, null, null);
    return initButtons;
  }

  // Implement construction of titles and rows.

  // Display my name, plan, votes and king's plan.
  protected String constructTitle1() {
     return " General " + Screen.node[me] +
            ".  Plan "  + plans[choice[me]] +
            ".  Majority " + plans[majorityDecision] +
            ".  Votes " + ((votes == 0) ? "" : (votes + "")) +
            ( (currentKing == me) ?
                  ".  I am King" :
                  (".  King says " + plans[kingDecision]) ) +
            ".";
  }

  //  Relay my plan to all other nodes.
  protected String constructTitle2() {
    return constructRelayLine();
  }

  // Display plan as sent by node g and
  //   what other nodes relay about g.
  protected String constructRow1(int g) {
    String s = " " + Screen.node[g] +
               " sent " + plans[choice[g]];
    return s;
  }

  //  Relay g's plan to all other nodes.
  protected String constructRow2(int g) {
    return "";
  }

  //  Relay info to all nodes.
  private String constructRelayLine() {
    if (choice[me] == blankCode) return "";
    String s = " Relay plan to ";
    int count = 0;
    for (int i = 0; i < number; i++)
      if ((i != me) && (!sent[i])) {
        s = s + Screen.node[i] + "  ";
        count++;
      }
    if (count == 0) return " "; else return s + "       ";
  }

  //  Initially choose plan attack or retreat.
  private boolean choosePlan(String b) {
    if (b.equals(ATTACK)) choice[me] = attackCode;
    else if (b.equals(RETREAT)) choice[me] = retreatCode;
    else return false;
    changeState(toState, initButtons, toButtons);
    return true;
  }

  //  Get destination for sending my plan to b.
  //  Store node number in toGeneral.
  private boolean SendPlan(String b) {
    toGeneral = FindNode(b);
    if (sent[toGeneral]) {
        assess(NONEED + b);
        return false;
    }
    if (state == toStateK)
      changeState(whatStateK, kingButtons, whatButtons);
    else
      changeState(whatState, toButtons, whatButtons);
    return true;
  }

  //  Implements action of loyal general:
  //    if my plan not sent, send to all other nodes.
  //    otherwise, send all outstanding relay obligations.

  private boolean SendAll() {
    int planToSend = choice[me];
    if (state == toStateK) planToSend = majorityDecision;
    for (int i = 0; i < number; i++)
      if ((i != me) && !sent[i]) {
        send(me, i, planToSend, me);
        sent[i] = true;
      }
    if (state == toStateK) finalKing();
    else if (roundOver()) changeToKingState();
    else redisplay();
    return true;
  }

  // Send plan b of aboutGeneral to node toGeneral.
  private boolean What(String b) {
    int whichPlan;
    if (b.equals(ATTACK)) whichPlan = attackCode;
    else if (b.equals(RETREAT)) whichPlan = retreatCode;
    else return false;
    sent[toGeneral] = true;
    send(me, toGeneral, whichPlan, me);
    if (state == whatStateK)
      if (allSent()) finalKing();
      else changeState(toStateK, whatButtons, kingButtons);
    else if (roundOver()) changeToKingState();
    else changeState(toState, whatButtons, toButtons);
    return true;
  }

  //  Implement state machine.
  protected boolean stateMachine(String b) {
    switch(state) {
      case initState:  return choosePlan(b);
      case toState:
      case toStateK:
        if (b.equals(ALL)) return SendAll();
        else return SendPlan(b);
      case whatState:  return What(b);
      case whatStateK: return What(b);
      case waitState:  return false;
      case goStateK:   return resetData();
      default:         return false;
    }
  }

  // The king sends endRoundCode to other generals
  //   so they can wait for "Continue".
  private void finalKing() {
    choice[me] = majorityDecision;
    for (int i = 0; i < number; i++)
      if (i != me) send(me, i, endRoundCode, me);
    changeState(goStateK, -2, goButtons); 
  }

  // The king passes on the kingship and sends messages for new round.
  private boolean resetData() {
    currentKing = (currentKing +  1) % number;
    for (int i = 0; i < number; i++)
      send(me, i, newRoundCode, me);
    return true;
  }

  private void changeToKingState() {
    if (currentKing == me) {
      kingDecision = choice[me];
      for (int i = 0; i < number; i++) sent[i] = false;
      changeState(toStateK, -2, kingButtons);
    }
    else
      changeState(waitState, -2, waitButtons);
  }

  //  Receive message:
  //    "from" souce node sending plan "p".
  //    "by" is historical from regular BG algorithm.
  //    Also process endRound and newRound codes.
  protected void receive(int from, int p, int by) {
    if (p == endRoundCode)         // End of round: wait for continue.
      changeState(goState, -2, nullButtons);
    else if (p == newRoundCode) {  // New round: initialize.
      initData();
      changeState(toState, -2, toButtons);
    }
    else if (state == waitState) { // King's plan: decide if to accept.
      kingDecision = p;
      if (votes > (number/2 + TRAITORS))
        choice[me] = majorityDecision;
      else
        choice[me] = kingDecision;
      changeState(toState, waitButtons, toButtons);
    }
    else {                         // Normal processing: store plan and
      choice[from] = p;            //   check if last plan.
      if (roundOver()) changeToKingState();
      else redisplay();
    }
  }

  //  Check if all plans sent.
  private boolean allSent() {
    for (int i = 0; i < number; i++)
      if ((i != me) && !sent[i]) return false;
    return true;
  }

  // A round is over if plan sent to all other generals
  //   and all their plans received.
  private boolean roundOver() {
    if (!allSent()) return false;
    changeState(state, -2, nullButtons);
    for (int i = 0; i < number; i++)
      if (choice[i] == blankCode) return false;
    computeMajority();
    return true;
  }

  // Computer majority and vote count of my plan and all plans received.
  // Call only when all plans have been received.
  private void computeMajority() {
    int attacks = 0, retreats = 0;
    for (int i = 0; i < number; i++) {
      if (choice[i] == attackCode)  attacks++;
      else retreats++;
    }
    if (retreats >= attacks)
      { majorityDecision = retreatCode; votes = retreats; }
    else
      { majorityDecision = attackCode; votes = attacks; }
  }

  private int toGeneral;     // Store number of destination node.
//  private int aboutGeneral;  // Store number of "about" node.

  private int initButtons, toButtons, whatButtons,
              kingButtons, waitButtons, goButtons, nullButtons;
                             // Codes of button panels.

  // Data structure for algorithm

  private int[]  choice = new int[number]; // Choice of each general
  private int    majorityDecision;         // Majority decision
  private int    votes;                    // Votes for majority
  private int    kingDecision;             // Decision received from king
  private boolean[] sent  = new boolean[number];
                                           // true if plan sent to "i"
  private static int currentKing;

  // State codes.
  // Suffix K indicates states that the current king enters.

  private static final int initState   = 0;
  private static final int toState     = 1;
  private static final int toStateK    = 2;
  private static final int whatState   = 3;
  private static final int whatStateK  = 4;
  private static final int waitState   = 5;
  private static final int goState     = 6;
  private static final int goStateK    = 7;
  
  // Codes and strings for plans.
  // Special codes for reception of messages indicating
  //   end of round and start of new round.
  // The end of round allows the user to examine the data
  //   before the reset of the new round.

  private static final int endRoundCode = -2;
  private static final int newRoundCode = -1;
  private static final int blankCode    = 0;
  private static final int attackCode   = 1;
  private static final int retreatCode  = 2;
  private static final String[] plans   = {"?", "A", "R"};

  // Strings for buttons.
  private static final String
    ATTACK  = "Attack", RETREAT = "Retreat", ALL     = "All";

  private static final String
//    ITSELF      = "Can't send plan of node to itself",
    NONEED      = "No message needs to be sent to ";
//    NOTRECEIVED = "Haven't yet received plan of ",
//    ALREADY     = "Already sent plan of ",
//    TRAITOR     = " already is a traitor";
}

