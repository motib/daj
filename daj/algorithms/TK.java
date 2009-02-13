package daj.algorithms;

import daj.DistAlg;
import daj.Screen;

public class TK extends DistAlg {
    
    
    private int highestNumber;    // Highest known number.
    private int ownNumber;        // Our own number
    
    // States
    public final static int NON_CS_STATE          = 0;
    public final static int REQUEST_CS_STATE      = 1;
    public final static int IN_CS_STATE           = 2; 
    
    // This variable is used in conjuction with the state variable to
    // better define what is happening in the UI
    private int action;
    
    // Actions / Messages
    public final static int NOTHING               = 0;    
    public final static int REQUEST               = 1;
    public final static int REPLY                 = 2;
    
    public final static String[] STATE_DESCRIPTIONS = {
        "in non-critical section",
        "requesting entry to critical section",
        "in critical section"
    };
    
    // Variables to hold the IDs of different button panels    
    private int nonCSButtons;
    private int requestCSButtons;
    private int inCSButtons;
    
    private int[] buttonPanels;
    
    private int addressButtons; 
    
    private boolean[] replyDeferred;
    private boolean[] authorization;
    
    private boolean[] nonRepliedRequests;
    private boolean[] requestsNotSent;
    
    
    public static final String REQUEST_ENTRY = "Request CS";
    public static final String SEND_REPLY    = "Send reply";
    public static final String SEND_REQUEST  = "Send request";
    public static final String ENTER_CS      = "Enter CS";
    public static final String LEAVE_CS      = "Leave CS";
    
    public TK(int i, DistAlg[] da, Screen s) {
        super(i, da, s);
        init();
        initialize();
    }
    
    // Initialize data structures.
    protected void init() {
        replyDeferred      = new boolean[number];
        authorization      = new boolean[number];
        nonRepliedRequests = new boolean[number];
        requestsNotSent    = new boolean[number];
        
        state = NON_CS_STATE;
        highestNumber = 0;
        ownNumber     = 0;
        action        = 0;
    }
    
    // Construct button panels.
    protected int constructButtons() {
        nonCSButtons     = addComponents("",  REQUEST_ENTRY, SEND_REPLY, null);
        requestCSButtons = addComponents("",  SEND_REQUEST,  SEND_REPLY, ENTER_CS);
        inCSButtons      = addComponents("",  SEND_REPLY,    LEAVE_CS,   null); 
        
        buttonPanels     = new int[]{nonCSButtons, requestCSButtons, inCSButtons};
        
        addressButtons   = addressButtons("", null, null);
        return nonCSButtons;
    }
    
    //  Node name and highest/chosen number.
    protected String constructTitle1() {
        return "Process "+ Screen.node[me] +
        " " + STATE_DESCRIPTIONS[state];
    }
    
    protected String constructTitle2() {
        return "Highest received number = "+ highestNumber
        + " Our own number " + ownNumber;
    }
    
    protected String constructRow1(int nodeID) {
        return Screen.node[nodeID] + (authorization[nodeID] ? " - We hold the authorization" : "");
    }
    
    protected String constructRow2(int nodeID) {
        if (replyDeferred[nodeID]){
            if (requestsNotSent[nodeID] && !authorization[nodeID])
                return "Reply deferred + Request not sent";
            else
                return "Reply to this node deferred";
            
        }
        if (requestsNotSent[nodeID] && state == REQUEST_CS_STATE){
            if (nonRepliedRequests[nodeID])
                return "Pending request from this node + Request not sent";
            
            if (authorization[nodeID]) //If authorization is held, there is no need to send a request
                return "";
            else
                return "Request to this node not yet sent";
        }
        if (nonRepliedRequests[nodeID])
            return "Pending request from this node";
        
        return "-";
        
    }
    
    protected boolean stateMachine(String command) {          
        if (command.equals(REQUEST_ENTRY)){
            ownNumber = highestNumber +1;
            
            for (int i=0; i<requestsNotSent.length; i++)
                requestsNotSent[i] = true;
            
            changeState(REQUEST_CS_STATE, nonCSButtons, requestCSButtons);
            redisplay();
            return false;              
        }else if (command.equals(SEND_REPLY)){
            
            changeState(state, buttonPanels[state], addressButtons);
                        
            action = REPLY;
            redisplay();
            return false;
        }else if (command.equals(SEND_REQUEST)){
            changeState(REQUEST_CS_STATE, requestCSButtons, addressButtons);
            action = REQUEST;
            redisplay();
            return false;
        }else if (command.equals(ENTER_CS)){
            for (int i = 0; i < number; i++)
                if (authorization[i]==false && i!=me){
                    assess("Not authorized to enter CS");
                    return false;
                }
            
            changeState(IN_CS_STATE, requestCSButtons, inCSButtons);              
            redisplay();
            return false;
        }else if (command.equals(LEAVE_CS)){
            for (int i = 0; i< number; i++)
                if (replyDeferred[i])
                    authorization[i] = replyDeferred[i] = false;
            
            changeState(NON_CS_STATE, inCSButtons, nonCSButtons);              
            redisplay();
            return false;
        }
        
        int destination = FindNode(command);
        
        if (action == REQUEST){
            //Check whether a request should even be sent
            
            if (requestsNotSent[destination] == false){
                assess("Request should not be sent to this node [already sent]");
                changeState(state, addressButtons, requestCSButtons);              
                return false;                 
            }
            
            send(REQUEST, destination, me, ownNumber);            
            
            requestsNotSent[destination] = false;
            changeState(REQUEST_CS_STATE, addressButtons, requestCSButtons);              
            redisplay();
            return false;              
            
        }else{  //REPLY
            
            //Check whether a reply should even be sent
            if (replyDeferred[destination] || nonRepliedRequests[destination] == false){
                
                if (replyDeferred[destination])
                    assess("Reply to this node should not be sent [deferred]");
                if (nonRepliedRequests[destination] == false)
                    assess("Reply to this node should not be sent [not requested]");
                
                changeState(state, addressButtons, buttonPanels[state]);
                
                return false;
            }
            
            send(REPLY, destination, me, ownNumber);
            nonRepliedRequests[destination] = false;
            
            changeState(state, addressButtons, buttonPanels[state]);            
            redisplay();
            return false;
            
        }
    }
    
    protected void receive(int messageType, int source, int number) {
        if (messageType == REQUEST) {
            highestNumber = max(highestNumber, number);
            nonRepliedRequests[source] = true;
            
            boolean weHavePriority = 
                (ownNumber < number || (ownNumber == number && me < source));
            
            if (state == IN_CS_STATE || (state == REQUEST_CS_STATE && weHavePriority)){
                replyDeferred[source] = true;
            }
            
            if (state == NON_CS_STATE){
                authorization[source] = false;
                
                //SENDING THE REPLY IS TO BE DONE BY THE STUDENT
            } else if (weHavePriority == false && state == REQUEST_CS_STATE){            
                if (authorization[source]){
                    //SENDING THIS REQUEST IS TO BE DONE BY THE STUDENT
                }
                
                authorization[source] = false;
                //SENDING THIS REPLY IS TO BE DONE BY THE STUDENT
                
            }            
            
        }
        else if (messageType == REPLY) {
            authorization[source] = true;
        }
        redisplay();  // Update display, don't change state.
    }
    
    public static final int max(int a, int b){
        return a>b ? a : b;
    }
    
}
