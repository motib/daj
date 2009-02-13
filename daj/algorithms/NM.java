package daj.algorithms;

import daj.DistAlg;
import daj.Screen;

public class NM extends DistAlg {

	public static final String RECEIVE		 = "Receive request"; 
	public static final String RELAY_REQUEST = "Relay request";
	public static final String DEFER		 = "Defer";
    public static final String SEND_REQUEST  = "Send request";
    public static final String ENTER_CS      = "Enter CS";
    public static final String LEAVE_CS      = "Leave CS";
    public static final String SEND_TOKEN	 = "Send Token";

    // Buttons
    private int requestButton;
    private int request_receive_buttons;
    private int enter_receive_buttons;
    private int addressButtonsTo;
    private int addressButtonsFrom;
	private int enterButton;
	private int leaveButton;
    private int receivingFrom;
    private int currButtons;
    private int leave_receive_buttons;
    private int sendTokenButton;
    private int defer_relay_token_buttons;
    private int receiveButton;
    private int noButtons;

	private int[] pending; 
    private boolean holding;
    private int parent;
    private int deferred;
    private int action;

    
    public static final int REQUESTING = 0;
    public static final int IN_CS = 1;
    public static final int IN_NONCS = 2;

    private String[] status = new String[] { "waiting for token.",
            "in critical section.", "in non-critical section."
    };

    // Actions / Messages
    public final static int TOKEN                 = 0;    
    public final static int REQUEST               = 1;
    public final static int RECEIVING             = 2;
    
    public NM(int i, DistAlg[] d, Screen s) {
        super(i, d, s);
        init();
        initialize();
    }

    protected void init() {
        state = IN_NONCS;
        holding = false;
        parent = me - 1;
        deferred = -1;
        if (me == 0)
        	holding = true;
        pending = new int[number];
        for (int i = 0; i < number; i++) 
			pending[i] = -1;
        receivingFrom = -1;
        if (me == 0)
            currButtons = enterButton;
        else
            currButtons = requestButton;
   }

    protected int constructButtons() {
        requestButton = addComponents("", SEND_REQUEST, null, null);
        receiveButton = addComponents("", RECEIVE, null, null);
        enterButton = addComponents("", ENTER_CS, null, null);
        leaveButton = addComponents("", LEAVE_CS, null, null);
        noButtons = addComponents("", null, null, null);
        request_receive_buttons = addComponents("", SEND_REQUEST, RECEIVE, null);
        enter_receive_buttons = addComponents("", ENTER_CS, RECEIVE, null);
        leave_receive_buttons = addComponents("", LEAVE_CS, RECEIVE, null);
        sendTokenButton = addComponents("", SEND_TOKEN, null, null);
        defer_relay_token_buttons = addComponents("Action", DEFER, RELAY_REQUEST, SEND_TOKEN);
        addressButtonsTo   = addressButtons("To", null, null);
        addressButtonsFrom = addressButtons("From", null, null);
        if (me == 0) {
            currButtons = enterButton;
        	return enterButton;
        }
        currButtons = requestButton;
        return requestButton;
    }

    protected String constructTitle1() {
        return Screen.node[me] + " " + status[state];
    }

    protected String constructTitle2() {
    	String s = "";
        if (deferred != -1)
            s += "Deferred: " + Screen.node[deferred] + "  ";        
        if (state == IN_CS)
            return s + "Root in CS";
    	if (parent != -1)
    		return s + "Parent: "+Screen.node[parent];
    	else
    		return s + (holding?"Root holding token":"New root");
    }

    protected String constructRow1(int row) {
        return Screen.node[row];
    }

    protected String constructRow2(int row) {
    	if (pending[row] != -1)
    		return "Pending request from this node";
        return "-";
    }

    protected boolean stateMachine(String command) {
    	if (command.equals(SEND_REQUEST)) {
    		changeState(REQUESTING, addressButtonsTo);
    		redisplay();
    		return false;
    	} else if (command.equals(ENTER_CS)) {
            holding = false;
            if (!isPending())
                changeState(IN_CS, leaveButton);
            else
                changeState(IN_CS, leave_receive_buttons);
    		redisplay();
    		return false;
    	} else if (command.equals(LEAVE_CS)) {
            if (deferred != -1) {
                changeState(IN_NONCS, sendTokenButton);
            } else {
                holding = true;
                if (!isPending())
                    changeState(IN_NONCS, enterButton);
                else
                    changeState(IN_NONCS, enter_receive_buttons);
            }
            redisplay();
            return false;
        } else if (command.equals(RECEIVE)) {
            action = RECEIVING;
            changeState(state, addressButtonsFrom);
            redisplay();
            return false;
        } else if (command.equals(DEFER)) {
            if (parent != -1) {
                assess("Cannot defer [not the root node]");
                return false;
            } else if (parent == -1 && holding) {
                assess("Cannot defer [node has the token]");
                return false;
            }
            deferred = pending[receivingFrom];
            pending[receivingFrom] = -1;
            parent = receivingFrom;
            if (isPending() && state == IN_CS)
                changeState(state, leave_receive_buttons);
            else if (!isPending() && state == IN_CS)
                changeState(state, leaveButton);
            else if (isPending())//(state == IN_CS)
                changeState(state, receiveButton);
            else
                changeState(state, noButtons);
            action = -1;
             redisplay();
            return false;
        } else if (command.equals(SEND_TOKEN)) {
            if (action == RECEIVING) {
                if (!holding) {
                    if (state == IN_CS)
                        assess("Cannot send token [node in critical section]");
                    else
                        assess("Cannot send token [node does not have it]");
                    return false;
                }
                holding = false;
                send(TOKEN, pending[receivingFrom], 0, 0);
                parent = receivingFrom;
                pending[receivingFrom] = -1;
                if (isPending())
                    changeState(state, request_receive_buttons);
                else
                    changeState(state, requestButton);
                redisplay();
                action = -1;
                return false;
            } else {
                send(TOKEN, deferred, 0, 0);
                deferred = -1;
                if (isPending())
                    changeState(IN_NONCS, request_receive_buttons);
                else
                    changeState(IN_NONCS, requestButton);
                redisplay();
                return false;                
            }
        } else if (command.equals(RELAY_REQUEST)) {
            if (parent == -1) {
                assess("Cannot relay request [node is the root]");
                return false;
            }
            send(REQUEST, parent, me, pending[receivingFrom]);
            parent = receivingFrom;
            pending[receivingFrom] = -1;
            action = -1;
            if (isPending() && state == REQUESTING)
                changeState(state, receiveButton);
            else if (isPending() && state == IN_NONCS)
                changeState(state, request_receive_buttons);
            else if (!isPending() && state == REQUESTING)
                changeState(state, noButtons);
            else if (!isPending() && state == IN_NONCS)
                changeState(state, requestButton);
            else if (isPending() && state == IN_CS)
                changeState(state, leave_receive_buttons);
            else 
                changeState(state, leaveButton);
            redisplay();
            return false;
        }
        
    	int destination = FindNode(command);
        if (destination == -1)
            return false;
    	if (action == RECEIVING) {
            if (pending[destination] == -1) {
                assess("Cannot receive [no pending request from this node]");
                return false;
            }
            changeState(state, defer_relay_token_buttons);
            receivingFrom = destination;
            redisplay();
            return false;
        } else if (state == REQUESTING && destination == parent) {            
        	send(REQUEST, parent, me, me);
            parent = -1;
            if (isPending())
                changeState(REQUESTING, receiveButton);
            else
                changeState(REQUESTING, noButtons);
            redisplay();
            return false;
    	} else if (state == REQUESTING) {
    		assess("Cannot send request [request should only be sent to parent]");
    		return false;
        }
        return false;
    }
    
    protected void receive(int messageType, int source, int originator) {
        if (messageType == TOKEN) {
            holding = true;
            if (isPending())
                changeState(IN_NONCS, enter_receive_buttons);
            else
                changeState(IN_NONCS, enterButton);
        } else if (messageType == REQUEST) {
        	pending[source] = originator;
            if (!holding && state == IN_NONCS)
                changeState(state, request_receive_buttons);
            else if (!holding && state == REQUESTING)
                changeState(state, receiveButton);
            else if (holding && state != IN_CS)
                changeState(state, enter_receive_buttons);
            else //(state == IN_CS)
                changeState(state, leave_receive_buttons);
        }
        redisplay();
    }
    
    private void changeState(int newState, int newButtons) {
        changeState(newState, currButtons, newButtons);
        currButtons = newButtons;
    }
    
    private boolean isPending() {
        boolean pend = false;
        for (int j = 0; j < number; j++) {
            if (pending[j] != -1)
                pend = true;
        }
        return pend;
    }
}
