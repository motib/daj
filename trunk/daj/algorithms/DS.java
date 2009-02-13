//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//

//  Dijkstra-Scholten algorithm for distributed termination
//  Moti Ben-Ari.
//  Modified 2005 by Maor Zamsky for display of tree

package daj.algorithms;
import daj.*;
import daj.algorithms.visual.*;  // MZ

public class DS extends DistAlg {

    public DS(int i, DistAlg[] da, Screen s) {
        super(i, da, s);
        init();
        initialize();
    }

    // Initialize data structures.
    protected void init() {
        for (int i = 0; i < number; i++) {
            MessagesReceived[i] = 0;
            SignalsSent[i] = 0;
            Deficit[i] = 0;
        }
        FirstEdge = -1;
        OutgoingDeficit = 0;
        if (me == SourceNode) state = runningState;
        else state = quietState;
        // MZ First node initializes display
        if ((me == 0) && (dsFrame == null))
			dsFrame = new DSFrame(number);
    }

    // Construct button panels.
    protected int constructButtons() {
        runningButtons   = addComponents("",   MESSAGE, SIGNAL, TERMINATE);
        terminateButtons = addComponents("",   SIGNAL,  null,   null);
        quietButtons     = addComponents("",   null,    null,   null);
        finalButtons     = addComponents(SYSTEM, null,    null,   null);
        toButtons        = addressButtons(null, null, null);
        if (me == SourceNode) return runningButtons;
        else return quietButtons;
    }

    // Implement construction of titles and rows.
    protected String constructTitle1() {
        String s = " " + Screen.node[me];
        if (me == SourceNode) s = s + ".  Source node.       ";
        else if (FirstEdge == -1) s = s + ".                     ";
        else s = s + ".  First message from  " + Screen.node[FirstEdge];
        s = s + ".  Outgoing deficit " + OutgoingDeficit + ".";
        return s;
    }

    protected String constructTitle2() {
        String s = "";
        switch (state) {
            case runningState:
            case toState: s = "active.    "; break;
            case terminateState:
            case signalterminateState: s = "signalling."; break;
            case quietState: s = "quiet.     "; break;
        }
        return "Process is " + s + "           ";
    }

    protected String constructRow1(int g) {
        return
            " " + Screen.node[g] +
            "    Message received " + MessagesReceived[g] +
            ".   Signals sent " + SignalsSent[g] +
            ".   Deficit " + Deficit[g];
    }

    protected String constructRow2(int g) {
        return " ";
    }

    //  Implement state machine.
    protected boolean stateMachine(String b) {
        switch (state) {
        case runningState: return MST(b);  // Message, signal or terminate.
        case toState:
        case signalterminateState: return getAddress(b);
        case terminateState:
            WhichPlan = signalCode;
            if (moreSignals())
                changeState(signalterminateState, terminateButtons, toButtons);
            return true;
        default:
            return false;
        }
    }

    // Message, signal or terminate.
    //   Message:   get address.
    //   Signal:    if signal required, get address.
    //   Terminate: if source node with no deficit,
    //              terminate the system.
    private boolean MST(String b) {
        if (b.equals(MESSAGE)) {
            WhichPlan = messageCode;
            changeState(toState, runningButtons, toButtons);
            return true;
        } else if (b.equals(SIGNAL)) {
            if (moreSignals()) {
                WhichPlan = signalCode;
                changeState(toState, runningButtons, toButtons);
                return true;
            }
            else {
                assess(NOSIGNAL + ANYNODE);
                return false;
            }
        } else if (b.equals(TERMINATE)) {
            if ((me == SourceNode) && (OutgoingDeficit == 0))
                changeState(quietState, runningButtons, finalButtons);
            else
                changeState(terminateState, runningButtons, terminateButtons);
            return true;
        } else
            return false;
    }

    // Get address of destination node
    //   toState during MST processing.
    //   signalterminateState to send signal after terminate pressed.
    private boolean getAddress(String b) {
        // Get node index corresponding to b.
        toNode = FindNode(b);
        // If message, send it and incremenet deficit.
        if (WhichPlan == messageCode) {
            OutgoingDeficit++;
            send(messageCode, toNode, me, 0);
        }

        // If signal, send it and decrement deficit.
        else   // WhichPlan == signalCode
            if (OKtoSignal(toNode)) {
                SignalsSent[toNode]++;
                Deficit[toNode]--;
                send(signalCode, toNode, me, 0);
            } else {
                assess(NOSIGNAL + Screen.node[toNode]);
                return false;
            }

        // Return to MST or Terminate buttons,
        //   unless this was the last signal to the first edge.
        if (state == toState)
            changeState(runningState, toButtons, runningButtons);
        else if ((toNode == FirstEdge) && (Deficit[FirstEdge] == 0)) {
            FirstEdge = -1;
            changeState(quietState, toButtons, quietButtons);
            dsFrame.eraseNode(me);  // MZ
        }
        else
            changeState(terminateState, toButtons, terminateButtons);
        return true;
    }

    // Message or signal received.
    protected void receive(int m, int by, int num) {
        // Message received.
        // If first message, set first edge.
        // Increment message and deficit counts.
        // In terminate or quiet state,
        //   message returns the node to running.
        if (m == messageCode) {
            if ((FirstEdge == -1) && (me != SourceNode)) {
                FirstEdge = by;
                dsFrame.createNode(by,me);  // MZ
            }
            MessagesReceived[by]++;
            Deficit[by]++;
            if (state == terminateState)
                changeState(runningState, terminateButtons, runningButtons);
            else if (state == quietState)
                changeState(runningState, quietButtons, runningButtons);
            else
                redisplay();   // Update display.
        }

        // Signal received.
        // Decrement deficit.
        // If all deficits sent in terminate state, become quiet.

        else if (m == signalCode) {
            OutgoingDeficit--;
            if ((me == SourceNode) &&
                    (OutgoingDeficit == 0)  &&
                    (state == terminateState)) {
                changeState(quietState, terminateButtons, finalButtons);
            } else
                redisplay();  // Update display.
        }
    }

    // Count total number of incoming deficits

    private int totalDeficit() {
        int def = 0;
        for (int i = 0; i < number; i++)
            def = def + Deficit[i];
        return def;
    }

    // Send signal if deficit to the node is non-zero.
    // Do not send last signal to first edge,
    // unless terminating and there are no more signals to send or receive
    private boolean OKtoSignal(int i) {
        if ((i != FirstEdge) || (Deficit[i] > 1))
            return (Deficit[i] > 0);
        else // Here if i = FirstEdge and Deficit[FirstEdge] <= 1
            return
                (totalDeficit() == 1) &&
                (OutgoingDeficit == 0) &&
                ((state == terminateState) || (state == signalterminateState));
    }

    // Check if there are signals that need to be sent.
    private boolean moreSignals() {
        for (int i = 0; i < number; i++)
            if ( (i != me) && OKtoSignal(i) )
                return true;
        return false;
    }

	protected void disposeFrame() { dsFrame.dispose(); dsFrame = null; }
    protected void setFrameVisible(boolean b) { dsFrame.setVisible(b); }
    protected void resetFrame() { dsFrame.reset(); }
    protected boolean hasVisualization() { return true; }
  	private static DSFrame dsFrame; // MZ frame for spanning tree

    private int toNode;             // Save address of destination.
    private int WhichPlan;          // Message or signal.
    private int FirstEdge;          // First incoming edge (-1 = none).
    private int OutgoingDeficit;    // Total outgoing edge deficits.
    private int runningButtons, toButtons,
        terminateButtons, quietButtons, finalButtons;
    // Codes of button panels.

    private static final int SourceNode    = 0;  // Nodes 0 is source.
    private static final int messageCode   = 1;  // Codes for messages.
    private static final int signalCode    = 2;
//    private static final int terminateCode = 3;

    // Incoming edge data
    private int[] MessagesReceived = new int[number];
    private int[] SignalsSent      = new int[number];
    private int[] Deficit          = new int[number];

    // State machine
    private static final int runningState         = 0;
    private static final int terminateState       = 1;
    private static final int quietState           = 2;
    private static final int signalterminateState = 3;
    private static final int toState              = 4;

    private static final String
    MESSAGE   = "Message",
    SIGNAL    = "Signal",
    TERMINATE = "Terminate",
    SYSTEM    = "System has terminated.";

    // Strings for assessment.
    private static final String
    NOSIGNAL   = "No need to send signal to ",
    ANYNODE   = "any node";
}

