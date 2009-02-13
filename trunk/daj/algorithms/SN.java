//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//

//  Chandy-Lamport algorithm for distributed snapshot.
//  Moti Ben-Ari

package daj.algorithms;
import daj.*;

import java.util.Vector;

public class SN extends DistAlg {
  public SN(int i, DistAlg[] da, Screen s) {
    super(i, da, s);
    init();
    initialize();
  }

  // Initialize data structures.
  protected void init() {
    for (int i = 0; i < number; i++) {
      LastMessage[R][i] = 0;
      LastMessage[S][i] = 0;
      Recorded[R][i] = 0;
      Recorded[S][i] = 0;
      Marker[R][i] = -1;
      Marker[S][i] = -1;
      Buffer[i] = new Vector();
    }
    state = runningState;
    StateRecorded = false;
  }

  // Construct button panels.
  protected int constructButtons() {
    runningButtons = addComponents("", SEND, GET,    RECORD);
    recordButtons  = addComponents("", GET,  MARKER, null);
    finalButtons   = addComponents(SNAP, null, null,   null);
    toButtons      = addressButtons(null, null, null);
    return runningButtons;
  }

  // Implement construction of titles and rows.

  protected String constructTitle1() {
    String s = " " + Screen.node[me];
    if (StateRecorded) s = s + " has recorded its state";
    return s;
  }

  protected String constructTitle2() { return " "; }

  protected String constructRow1(int g) {
    String s = " " + Screen.node[g] + "   ";

    // Display messages sent and received.
    int r;
    if (StateRecorded) r = Recorded[R][g]; else r = LastMessage[R][g];
    if (LastMessage[S][g] != 0)
      s = s + "Sent 1.." + LastMessage[S][g] + ".  ";
    if (r != 0)
      s = s + "Received 1.." + r + ".  ";

    // Display messages in the channel.
    if (StateRecorded)
      if (Marker[R][g] > r)
        s = s + " Channel " + (r+1) + ".." + Marker[R][g] + ".  ";
      else if (Marker[R][g] < 0)
        if (LastMessage[R][g] > r)
        s = s + " Channel " + (r+1) + ".." + LastMessage[R][g] + ".  ";

    // Display marker received.
    if (Marker[R][g] >= 0)
      s = s + "Marker received.";
    return s;
  }

  // Display prompts: messages to get from channel, marker to send.
  protected String constructRow2(int g) {
    String s = " ";
    if (StateRecorded && (Marker[S][g] < 0))
      s = s + " Send marker.   ";
    if (!Buffer[g].isEmpty())
      s = s + "Get " + Buffer[g].size() + " message(s) from channel.";
    return s;
  }

  // Implement state machine.
  protected boolean stateMachine(String b) {
    switch (state) {
    case runningState: return SGR(b);
    case recordState:  return GM(b);
    case toState:      return getAddress(b);
    default:           return false;
    }
  }

  // Send:   get address.
  // Get:    if some channel is not empty, get address.
  // Record: call recordMyState.
  private boolean SGR(String b) {
    if (b.equals(SEND)) {
      WhichPlan = messageCode;
      changeState(toState, runningButtons, toButtons);
      return true;
    }
    else if (b.equals(GET)) {
      if (SomeChannelNotEmpty()) {
        WhichPlan = receiveCode;
        changeState(toState, runningButtons, toButtons);
        return true;
      }
      else {
          assess(NOGET);
          return false;
      }
    }
    else if (b.equals(RECORD)) {
      recordMyState();
      changeState(recordState, runningButtons, recordButtons);
      return true;
    }
    else return false;
  }

  // After state is recorded:
  //   Get:    if some channel is not empty, get address.
  //   Marker: if some marker must be sent, get address.

  private boolean GM(String b) {
    if (b.equals(GET)) {
      if (SomeChannelNotEmpty()) {
        WhichPlan = receiveCode;
        changeState(toState, recordButtons, toButtons);
        return true;
      }
      else {
          assess(NOGET);
          return false;
      }
    }
    else if (b.equals(MARKER)) {
      if (SomeMarkerNotSent()) {
        WhichPlan = markerCode;
        changeState(toState, recordButtons, toButtons);
        return true;
      }
      else {
          assess(MARKERSENT + ALLNODES);
          return false;
      }
    }
    else return false;
  }

  // Get address of destination node
  private boolean getAddress(String b) {
    // Get node index corresponding to b.
    toNode = FindNode(b);
    // Message: increment last message sent to the node and send.
    if (WhichPlan == messageCode)
      send(++LastMessage[S][toNode], toNode, me, 0);

    // Marker: send if not already sent.
    else if (WhichPlan == markerCode) {
      if (Marker[S][toNode] >= 0) {
          assess(MARKERSENT + Screen.node[toNode]);
            return false;
      }
      Marker[S][toNode] = 1;  // Set that marker has been sent.
      send(MarkerMessage, toNode, me, 0);
    }

    // Get message.
    else { // if (WhichPlan == receiveCode)
      // If there is a message in the channel, get it.
      if (Buffer[toNode].isEmpty()) {
          assess(NOGET + Screen.node[toNode]);
          return false;
      }
      Integer I = (Integer) Buffer[toNode].firstElement();
      int m = I.intValue();
      Buffer[toNode].removeElement(I);

      // If marker, save last message received before marker,
      //   and record if not already recorded.
      // Otherwise, store message in LastMessage.
      if (m == MarkerMessage) {
        if (Marker[R][toNode] < 0)
          Marker[R][toNode] = LastMessage[R][toNode];
        if (!StateRecorded) recordMyState();
      }
      else LastMessage[R][toNode] = m;
    }

    // Change state as appropriate.
    if (finishedSnapshot())
      changeState(state, toButtons, finalButtons);
    else if (StateRecorded)
      changeState(recordState, toButtons, recordButtons);
    else
      changeState(runningState, toButtons, runningButtons);
    return true;
  }

  // Is there a non-empty channel.
  private boolean SomeChannelNotEmpty() {
    for (int i = 0; i < number; i++)
      if ((i != me) && (!Buffer[i].isEmpty()))
        return true;
    return false;
  }

  // Must a marker still be sent.
  private boolean SomeMarkerNotSent() {
    for (int i = 0; i < number; i++)
      if ((i != me) && (Marker[S][i] < 0))
        return true;
    return false;
  }

  // The snapshop has been taken,
  //   if the state has been recorded and
  //   the outgoing marker has been sent and
  //   the incoming marker has been received.
  private boolean finishedSnapshot() {
    if (!StateRecorded) return false;
    for (int i = 0; i < number; i++)
      if (i != me)
        if ((Marker[R][i] < 0) || (Marker[S][i] < 0))
          return false;
    return true;
  }

  // Record the state by saving the last messages
  //   both sent and received in Recorded.
  private void recordMyState() {
    if (StateRecorded) return;
    for (int i = 0; i < number; i++)
      if (i != me) {
        Recorded[S][i] = LastMessage[S][i];
        Recorded[R][i] = LastMessage[R][i];
      }
    StateRecorded = true;
  }

  // Receive message and add to buffer for this channel.
  protected void receive(int m, int by, int num) {
    Buffer[by].addElement(new Integer(m));
    redisplay();
  }

  private int WhichPlan;          // Message, received or marker.
  private int toNode;             // Address of destination node.
  private boolean StateRecorded;  // Has state been recorded.
  private int runningButtons, toButtons, recordButtons, finalButtons;
                                  // Codes for button panels.

  // Data structures.

  private int[][]  LastMessage = new int[2][number];
    // Last messages received from / sent to each node.
  private int[][]  Recorded    = new int[2][number];
    // Save last messages received/sent, when recording.
  private int[][]  Marker      = new int[2][number];
    // -1 if no marker has been received,
    // otherwise, the last message before the marker was received.
  private Vector[] Buffer      = new Vector[number];
    // Buffer for messages from each node.

  private static final int R             = 0;  // Received index
  private static final int S             = 1;  // Sent index

  private static final int messageCode   = 1;
  private static final int receiveCode   = 2;
  private static final int markerCode    = 3;
//  private static final int recordCode    = 4;

  private static final int MarkerMessage = -1;

  // State machine

  private static final int runningState  = 0;
  private static final int recordState   = 1;
  private static final int toState       = 2;

  private static final String
    SEND   = "Send",
    GET    = "Get",
    RECORD = "Record",
    MARKER = "Marker",
    SNAP   = "Snapshot recorded";
  private static final String
    NOGET  = "Cannot get from empty channel ",
    MARKERSENT = "Marker sent already to ",
    ALLNODES = " all nodes";
}
