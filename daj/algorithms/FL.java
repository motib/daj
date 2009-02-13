//
//  Distributed Algorithms in Java
//  See file copyright.txt for credits and copyright.
//

//  Distributed consensus by flooding
//  Moti Ben-Ari

package daj.algorithms;
import daj.*;

public class FL extends DistAlg {
    public FL(int i, DistAlg[] da, Screen s) {
        super(i, da, s);
        init();
        initialize();
    }

    // Initialize data structures.
    protected void init() {
        round = 1;
        planSet[0] = 0; planSet[1] = 0;
        for (int r = 0; r < number; r++) {
            sent[r]    = false;
            received[r][0] = 0; received[r][1] = 0;
        }
        myDecision = blankCode;
        state = initState;
    }

    // Construct button panels.
    protected int constructButtons() {
        initButtons  = addComponents("Choose plan ", ATTACK, RETREAT, null);
        toButtons    = addressButtons("Send to",     ALL, CRASH);
        crashButtons = addComponents("Crashed!", null, null, null);
        return initButtons;
    }

    // Display titles and rows
    protected String constructTitle1() {
        return " General " + Screen.node[me] +
               ". Round = " + round +
               ".  Plans are " + displayPlans(planSet) +
               ".  Decision is to " + plans[myDecision];
    }

    // Interpret data structure as a set
    private String displayPlans(int[] m) {
        if ((m[0] == 1) && (m[1] == 1)) return "{R,A}";
        else if (m[0] == 1) return "{R}";
        else if (m[1] == 1) return "{A}";
        else return "{ }";
    }

    //  Send plan set to other nodes.
    protected String constructTitle2() {
        String s = "";
        if ((state != crashedState) && 
            ((planSet[0]!=0) || (planSet[1]!=0))) // Plan chosen already
            for (int i = 0; i < number; i++)
                if ((i != me) && (!sent[i]))
                    s = s + Screen.node[i] + "  ";
        if (!s.equals("")) return " Send plan set to " + s; 
        else return " ";
    }

    protected String constructRow1(int g) {
        return " " + Screen.node[g] + " sent " + displayPlans(received[g]);
    }

    protected String constructRow2(int g) {
        return "";
    }

    //  Initially choose plan attack or retreat.
    private boolean choosePlan(String b) {
        if (b.equals(ATTACK)) planSet[1] = 1;
        else if (b.equals(RETREAT)) planSet[0] = 1;
        else return false;
        Vote();
        changeState(toState, initButtons, toButtons);
        return true;
    }

    //  Get destination for sending my plan to b.
    private boolean SendPlan(String b) {
        boolean allSent = true;
        int toGeneral = FindNode(b);
        if (!sent[toGeneral]) {
            sent[toGeneral] = true;
            send(me, toGeneral, planSet[0], planSet[1]);
            for (int i = 0; i < number; i++)
                if ((i != me) && !sent[i])
                    allSent = false;
            if (allSent) {
                for (int j = 0; j < number; j++)
                    sent[j] = false;
                round++;
            }
            redisplay();
            return true;
        }
        else return false;
    }

    private boolean SendAll() {
        for (int i = 0; i < number; i++)
            if ((i != me) && !sent[i])
                send(me, i, planSet[0], planSet[1]);
        round++;
        redisplay();
        return true;
    }

    //  Implement state machine.
    protected boolean stateMachine(String b) {
        switch(state) {
        case initState: return choosePlan(b);
        case toState: 
            if (b.equals(ALL)) return SendAll();
            else if (b.equals(CRASH)) {
                changeState(crashedState, toButtons, crashButtons);
                return true;
            }
            else return SendPlan(b);
        default: return false;
        }
    }

    //  Receive planSet
    protected void receive(int from, int m0, int m1) {
        if (state == crashedState) return;
        if (planSet[0] == 0) planSet[0] = m0;
        if (planSet[1] == 0) planSet[1] = m1;
        received[from][0] = m0; received[from][1] = m1;
        Vote();
        redisplay(); 
    }

    private void Vote() {
        myDecision = ( (planSet[0] == 0) && (planSet[1] == 1) ? 
            attackCode : retreatCode);
    }

    private int myDecision;     // My final decision.
    private int round;          // Number of rounds of the algorithm
    private int initButtons, toButtons, crashButtons;  // Codes of button panels.

    // Data structure for planSets
    private int[] planSet = new int[2];             // Set of plans at this node
    private boolean[] sent  = new boolean[number];  // Have plans been sent?
    private int[][] received = new int[number][2];  // Plans received from this general

    // State codes.
    private static final int initState    = 0;
    private static final int toState      = 1;
    private static final int crashedState = 2;

    // Codes and strings for plans.
    private static final int blankCode    = 0;
    private static final int attackCode   = 1;
    private static final int retreatCode  = 2;
    private static final String[] plans   = {"?", "A", "R", "-"};

    // Strings for buttons.
    private static final String
        ATTACK  = "Attack", RETREAT = "Retreat", 
        ALL     = "All", CRASH   = "Crash";
}

