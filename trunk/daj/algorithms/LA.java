//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//

//  Lamport algorithm for distributed mutual exclusion
//  Leoni Lubbinge
//  Modified 2005 by Moti Ben-Ari

package daj.algorithms;
import daj.*;
import daj.algorithms.visual.*; 

public class LA extends DistAlg {
    public LA(int i, DistAlg[] da, Screen s) {
        super(i, da, s);
        init();
        initialize();
    }

    protected void init() {
        for (int i = 0; i < number; i++) {
            needToSendRequest[i] = false;
            needToSendReply[i] = false;
            needToSendRelease[i] = false;
            replyReceived[i] = false;
            queueToEnter[i] = 0;
        }
        state = enterState;
        currentClock = 0;
        chosenClock = 0;
        highClock = 0;
        wantToEnter = false;
        if ((me == 0) && (raFrame == null)) 
            raFrame = new RAFrame();
    }

    protected int constructButtons() {
        enterButtons   = addComponents(TRY, ENTERREQUEST, ENTERREPLY, null);
        requestButtons = addComponents("", SENDREQUEST, SENDREPLY,  null);
        changeMnemonic(0, 2);
        leaveButtons   = addComponents(CS, LEAVE, null, null);
        releaseButtons = addComponents("", SENDRELEASE, null, null);
        toButtons      = addressButtons(TO, ALL, null);
        return enterButtons;
    }

    protected String constructTitle1() {
        String s = " " + Screen.node[me] +
                   ".  Highest timestamp received: " + highClock + ".";
        if (wantToEnter) s = s + "   Timestamp: " + chosenClock + ".";
        return s;
    }

    protected String constructTitle2() {
        String s = " Send request to ";
        for (int i = 0; i < number; i++)
            if (needToSendRequest[i]) 
                s = s + Screen.node[i] + " ";
        if (wantToEnter && any(needToSendRequest))
            return s + "    ";
        else return " ";
    }

    protected String constructRow1(int g) {
        String s = " " + Screen.node[g];
        if (queueToEnter[g] != 0)
            s = s + " sent " + queueToEnter[g] + ".   ";
        if (replyReceived[g])
            s = s + " Reply received.";
        return s;
    }

    protected String constructRow2(int g) {
        String s = " ";
        if (needToSendReply[g])
            s = s + " Reply to " + Screen.node[g] + ".   ";
        else if (needToSendRelease[g])
            s = s + " Release to " + Screen.node[g] + ".   ";
        return s;
    }

    protected boolean stateMachine(String b) {
        switch (state) {
        case enterState:
            if (b.equals(ENTERREQUEST)) {
                currentClock++;
                chosenClock = currentClock;
                wantToEnter = true;
                queueToEnter[me] = currentClock;
                for (int i = 0; i < number; i++)
                    if (i != me)
                        needToSendRequest[i] = true;
                changeState(requestState, enterButtons, requestButtons);
                return true;
            } else { // if (b.equals(ENTERREPLY))
                  if (any(needToSendReply)) {
                    message = replyMessage;
                    changeState(toState, enterButtons, toButtons);
                    return true;
                  } else {
                    assess(NOREPLY + ANYNODE);
                    return false;
                  }
            }
        case requestState:
            if (b.equals(SENDREQUEST)) {
                if (any(needToSendRequest)) {
                    message = requestMessage;
                    changeState(toState, requestButtons, toButtons);
                    return true;
                }
                else {
                    assess(NOREQUEST + ANYNODE);
                    return false;
                }
            } else { // if (b.equals(SENDREPLY)) {
                if (any(needToSendReply)) {
                    message = replyMessage;
                    changeState(toState, requestButtons, toButtons);
                    return true;
                }
                else {
                    assess(NOREPLY + ANYNODE);
                    return false;
                }
            }
        case leaveState:
            // if (b.equals(LEAVE)) {
            currentClock++;
            wantToEnter = false;
            queueToEnter[me] = 0;
            for (int i = 0; i < number; i++) {
                needToSendRelease[i] = true;
                replyReceived[i] = false;
            }
            changeState(releaseState, leaveButtons, releaseButtons);
            return true;
        case releaseState:
            message = releaseMessage;
            changeState(toState, releaseButtons, toButtons);
            return true;
        case toState:
            if (b.equals(ALL)) {
                for (int i = 0; i < number; i++)
                    if (i != me) sendMessage(i, true);
            }
            else
              sendMessage(FindNode(b), false);
            return true;
        default:
            return false;
        }
    }

    private void sendMessage(int to, boolean all) { 
        switch (message) {
            case requestMessage:
                if (!needToSendRequest[to]) {
                    if (!all) assess(NOREQUEST + Screen.node[to]);
                    return;
                }
                currentClock++;
                needToSendRequest[to] = false;
                send(requestMessage, to, me, chosenClock);
                changeState(requestState, toButtons, requestButtons);
                break;
            case replyMessage:
                if (!needToSendReply[to]) {
                    if (!all) assess(NOREPLY + Screen.node[to]);
                    return;
                }
                currentClock++;
                needToSendReply[to] = false;
                send(replyMessage, to, me, currentClock);
                if (wantToEnter)
                    changeState(requestState, toButtons, requestButtons);
                else
                    changeState(enterState, toButtons, enterButtons);
                break;
            case releaseMessage:
                if (!needToSendRelease[to]) {
                    if (!all) assess(NORELEASE + Screen.node[to]);
                    return;
                }
                currentClock++;
                needToSendRelease[to] = false;
                send(releaseMessage, to, me, currentClock);
                if (all) {
                    if (!any(needToSendRelease))
                        changeState(enterState, toButtons, enterButtons);
                }
                else if (any(needToSendRelease))
                    changeState(releaseState, toButtons, releaseButtons);
                else
                    changeState(enterState, toButtons, enterButtons);
                break;
            default: break;
            }
    }
    
    private boolean any(boolean[] a) {
        for (int i = 0; i < number; i++)
            if ((i != me) && a[i])
                return true;
        return false;
    }

    private boolean ableToEnter() {
        int count = 0;
        int min = me;
        int clock = queueToEnter[me];
        for (int i = 0; i < number; i++)
            if ((i != me) && (replyReceived[i]))
                count++;
        for (int i = 0; i < number; i++)
            if (queueToEnter[i] != 0 && queueToEnter[i] <= clock) {
                min = i;
                clock = queueToEnter[i];
            }
        return ((count == number - 1) && (min == me));
    }

    protected void receive(int m, int by, int num) {
        if (currentClock < num)
            currentClock = num + 1;
        else
            currentClock++;

        switch (m) {
        case requestMessage:
            queueToEnter[by] = num;
          raFrame.addQueued(by, num);
            if (num > highClock)
                highClock = num;
            if (!wantToEnter)
                needToSendReply[by] = true;
            else if (num <= chosenClock)
                needToSendReply[by] = true;
            break;
        case replyMessage:
            replyReceived[by] = true;
            if (ableToEnter()) {
                raFrame.removeQueued();
                changeState(leaveState, requestButtons, leaveButtons);
                return;
            }
            break;
        case releaseMessage:
            queueToEnter[by] = 0;
            if (wantToEnter) {
                replyReceived[by] = true;
                if (ableToEnter()) {
                    raFrame.removeQueued();
                    changeState(leaveState, requestButtons, leaveButtons);
                    return;
                }
            }
            break;
        }

        redisplay();
    }

   protected void disposeFrame() { raFrame.dispose(); raFrame = null; }
   protected void setFrameVisible(boolean b) { raFrame.setVisible(b); }
   protected void resetFrame() { raFrame.reset(); }
   protected boolean hasVisualization() { return true; }
   private static RAFrame raFrame;
  
   // Process status
    private boolean wantToEnter = false;
    private int[] queueToEnter = new int[number];

    // Clock status
    private int currentClock = 0;
    private int chosenClock = 0;
    private int highClock = 0;

    // State constants
    private static final int enterState    = 0;
    private static final int requestState  = 1;
    private static final int leaveState    = 2;
    private static final int releaseState  = 3;
    private static final int toState      = 4;

    // State values
    //private int state = enterState;

    // Message constants
    private static final int requestMessage  = 1;
    private static final int replyMessage    = 2;
    private static final int releaseMessage  = 3;

    // Message values
    private int message = 0;
    private boolean[] needToSendRequest  = new boolean[number];
    private boolean[] needToSendReply    = new boolean[number];
    private boolean[] needToSendRelease  = new boolean[number];
    private boolean[] replyReceived      = new boolean[number];

    // User interface components
    private int enterButtons, requestButtons, 
        leaveButtons, releaseButtons, toButtons;

    private static final String
        TRY = "Try to enter CS",
        ENTERREQUEST  = "Get timestamp", ENTERREPLY    = "Reply", 
        SENDREQUEST    = "Request", SENDREPLY    = "Reply",
        SENDRELEASE    = "Release", CS      = "In critical section",
        LEAVE      = "Leave", TO = "To", ALL = "All";
        
    // Strings for assessment.
    private static final String
        NOREPLY   = "No need to send answer to ",
        NOREQUEST = "No need to send request to ",
        NORELEASE = "No need to send release to ",
        ANYNODE   = "any node";
}
