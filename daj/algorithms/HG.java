package daj.algorithms;

import daj.DistAlg;
import daj.Screen;

public class HG extends DistAlg {

    public static final int CONTROLLING_AGENT_ID = 0;
    
    /**
     * These two constants control the number and size of the slots the weight is divided in 
     */
    
    public static final int WEIGHT_WINDOW_SIZE = 4;
    public static final int MAX_SLOT_NUMBER = 3;

    /**
     * These constants are used for comparisons
     */
    public static final Weight ZERO_WEIGHT = new Weight(0,0); 
    public static final Weight FULL_WEIGHT = new Weight(-1,1); 

    /**
     * Strings displayed on buttons
     */
    public static final String FORK_STRING        = "Fork new";
    public static final String TERMINATE_STRING   = "Terminate";
    public static final String RQ_SUPPLY_STRING   = "Request supply";
    public static final String SEND_SUPPLY_STRING = "Send supply";    
    
    /**
     * State numbering
     */
    public static final int FORK_PROCESS   = 1;
    public static final int SUPPLY         = 2;
    public static final int SUPPLY_REQUEST = 3;
    public static final int TERMINATION    = 4;
    public static final int SURPLUS        = 5;
    
    /**
     * Button panel IDs
     */
    private int controllingAgentBPanelID;
    private int inactiveProcessBPanelID;
    private int activeProcessBPanelID;
    private int addressBPanelID;
    
    private DistAlg[] otherNodes;
    
    //Every node also has the controllingAgentWeights, because
    //the framework creates the nodes as nodes of equal type using
    //a constructor. 
    
    private Weight   nodeWeight;
    private Weight[] controllingAgentWeights;     
    private int controllingAgentMaxWindow;
    private boolean[] pendingSupplyRequests;
    
    private static class CannotSplit extends Exception{}
   
    private static class Weight{
        
        
        private int weightMantissa;
        private int weightExponent;
        
        public Weight(int weightExponent, int weight){
            this.weightMantissa = weight;
            this.weightExponent = weightExponent;
        }
        
        public int getExponent(){
            return weightExponent;
        }

        public int getMantissa(){
            return weightMantissa;
        }

        
        public Weight split() throws CannotSplit{
            if (weightMantissa == 1){
              if (weightExponent == MAX_SLOT_NUMBER)
                  throw new CannotSplit();
                  
              weightExponent++;
              weightMantissa = WEIGHT_WINDOW_SIZE >> 1;

              //To ensure correct behavior when window size is not divisible by 2
              int splitMantissa = WEIGHT_WINDOW_SIZE - weightMantissa;

              return new Weight(weightExponent, splitMantissa);                  
            }
            else{
                int splitMantissa = weightMantissa >> 1;
                weightMantissa -= splitMantissa;
                
                return new Weight(weightExponent, splitMantissa);                
            }
        }
                
        
        //Returns the smaller portion
        public Weight join(Weight otherWeight){
            if (this.equals(ZERO_WEIGHT)){
                weightExponent = otherWeight.weightExponent;
                weightMantissa = otherWeight.weightMantissa;
                return new Weight(0,0);
            }
            
            if (otherWeight.weightExponent == this.weightExponent){
                weightMantissa += otherWeight.weightMantissa;
                
                if (weightMantissa >= WEIGHT_WINDOW_SIZE){
                    weightExponent--;
                    int splitMantissa = weightMantissa - WEIGHT_WINDOW_SIZE;
                    weightMantissa = 1;
                    
                    return new Weight(weightExponent+1, splitMantissa);
                }
                else{                    
                    return new Weight(0,0);
                }                
            }
            else{
                if (this.weightExponent > otherWeight.weightExponent){
                    int swapExp = this.weightExponent;
                    int swapMan = this.weightMantissa;
                    
                    this.weightExponent = otherWeight.weightExponent;
                    this.weightMantissa = otherWeight.weightMantissa;
                    
                    otherWeight.weightExponent = swapExp;
                    otherWeight.weightMantissa = swapMan;
                }
                
                return otherWeight;                
            }
        }
        
        public String toString(){
            StringBuffer result = new StringBuffer();
            result.append("[Slot ");
            result.append(weightExponent);
            
            int filter = 2;
            for (;filter<WEIGHT_WINDOW_SIZE;filter*=2);
            filter/=2;
            result.append(':');
            for (;filter>0;filter/=2)
                result.append((char)(((weightMantissa&filter)==0)?'0':'1'));
            result.append("]");
            
            return result.toString();
        }
        
        public boolean equals(Object o){
            return
                ((Weight)o).weightExponent == weightExponent &&
                ((Weight)o).weightMantissa == weightMantissa;
            
        }
        
    }
    
    public HG(int nodeID, DistAlg[] otherNodes, Screen screen) {
        super(nodeID, otherNodes, screen);
        this.otherNodes = otherNodes;
        init();
        initialize();
    }

    
    protected void init() {
        if (me == CONTROLLING_AGENT_ID){
            pendingSupplyRequests = new boolean[otherNodes.length];
            controllingAgentWeights = new Weight[MAX_SLOT_NUMBER + 1];
            for (int i=0; i<MAX_SLOT_NUMBER + 1; i++){
                controllingAgentWeights[i]= new Weight(i,0);
            }
            controllingAgentWeights[0] = new Weight(0,1);
            controllingAgentMaxWindow = 0;
        }
        
        nodeWeight = new Weight(0,0);
    }

    protected int constructButtons() {
        controllingAgentBPanelID = addComponents("", FORK_STRING, SEND_SUPPLY_STRING, null);
        inactiveProcessBPanelID  = addComponents("[Inactive]", null, null, null);
        activeProcessBPanelID    = addComponents("", FORK_STRING, RQ_SUPPLY_STRING, TERMINATE_STRING);
        addressBPanelID          = addressButtons(null, null, null);

        if (me == CONTROLLING_AGENT_ID) 
          return controllingAgentBPanelID;
        
        return inactiveProcessBPanelID;
    }

    //Node information area
    protected String constructTitle1() {
        if (me == CONTROLLING_AGENT_ID){
            return "Controlling Agent ("+Screen.node[me]+")";
        }
        
        return "Process ("+Screen.node[me]+")";
    }

    protected String constructTitle2() {
        StringBuffer result = new StringBuffer();
        
        if (me == CONTROLLING_AGENT_ID){
            for (int i=0; i<MAX_SLOT_NUMBER+1; i++){
                result.append(controllingAgentWeights[i]);
                result.append(" ");
            }
        }
        else{
            if (nodeWeight.equals(ZERO_WEIGHT)){
                result.append("Inactive");
            }
            else{
                result.append("Active with weight : ");        
                result.append(nodeWeight.toString());
            }
        }
        return result.toString();
    }

    //These are not applicable to his algorithm (only used to mark supply requests in the contirlling agent)
    protected String constructRow1(int row) {
        if (me == CONTROLLING_AGENT_ID && pendingSupplyRequests[row])
            return "Pending supply request from "+Screen.node[row];
        return "";
    }

    protected String constructRow2(int row) {
        return "";
    }

    protected boolean stateMachine(String command) {
        try{
        if (command.equals(FORK_STRING)){
            if (me==CONTROLLING_AGENT_ID)
                changeState(FORK_PROCESS, controllingAgentBPanelID, addressBPanelID);
            else
                changeState(FORK_PROCESS, activeProcessBPanelID, addressBPanelID);

            return false;
        }else if (command.equals(TERMINATE_STRING)){
            changeState(TERMINATION, activeProcessBPanelID, addressBPanelID);
            return false;
        }else if (command.equals(SEND_SUPPLY_STRING)){
            changeState(SUPPLY, controllingAgentBPanelID, addressBPanelID);
            return false;
        }else if(command.equals(RQ_SUPPLY_STRING)){
            changeState(SUPPLY_REQUEST, activeProcessBPanelID, addressBPanelID);
            return false;
        }

        int destination = FindNode(command);
        
        switch (state){
        case 0:
            return false;
        case SUPPLY:
        case FORK_PROCESS:
            if (destination == CONTROLLING_AGENT_ID){
                assess("Controlling agent should not be the target of a new fork or a supply message");
                return false;
            }
            
            if (state == SUPPLY && pendingSupplyRequests[destination]==false){
                assess("The selected receiver has not requested for supply of weight");
                changeState(state, addressBPanelID, controllingAgentBPanelID);

                return false;
            }
    
                
            if (me == CONTROLLING_AGENT_ID){
                try{
                    Weight messageWeight = controllingAgentSplit();

                    if (state == SUPPLY)
                        pendingSupplyRequests[destination]=false;
                        
                    send(state, destination, me, messageWeight);

                }catch (CannotSplit cs){
                    assess("There is not enough weight to split");
                }
                changeState(state, addressBPanelID, controllingAgentBPanelID);
            }
            else{
                try{
                    Weight messageWeight = nodeWeight.split();
                        
                    send(state, destination, me, messageWeight);

                }catch (CannotSplit cs){
                    assess("There is not enough weight to split");
                }
                changeState(state, addressBPanelID, activeProcessBPanelID);
            }

            return false;
        case SUPPLY_REQUEST:
            if (destination != CONTROLLING_AGENT_ID){
                assess("Supply requests should be made to the controlling agent");
                return false;
            }
            
            send(state, destination, me, ZERO_WEIGHT);

            changeState(state, addressBPanelID, activeProcessBPanelID);
            return false;
        case TERMINATION:
            if (destination != CONTROLLING_AGENT_ID){
                assess("Termination messages should be sent to the controlling agent");
                return false;
            }
            
            send(state, destination, me, nodeWeight);            
            nodeWeight = new Weight(0,0);
            changeState(state, addressBPanelID, inactiveProcessBPanelID);
            return false;        
        }
        
        return false;
        }finally{
            redisplay();
        }
    }

    protected void receive(int messageType, int source, Object messageKey) {
        Weight receivedWeight  = (Weight)messageKey;
        switch(messageType){
        case SURPLUS:
        case TERMINATION:
            controllingAgentJoin(receivedWeight);
            break;

        case SUPPLY_REQUEST:
            pendingSupplyRequests[source] = true;
            break;
                        
        case FORK_PROCESS:
        case SUPPLY:
            //If an inactive node receives a supply, the weight is sent back
            if (nodeWeight.equals(ZERO_WEIGHT) && messageType == SUPPLY ){
                send(SURPLUS, CONTROLLING_AGENT_ID, me, receivedWeight);
                assess("Inactive nodes automatically send back received supply of weight");
                break;
            }
                
            Weight sendBack = nodeWeight.join(receivedWeight);

            if (messageType == FORK_PROCESS)
                changeState(state, inactiveProcessBPanelID, activeProcessBPanelID);
            
            if (sendBack.equals(ZERO_WEIGHT) == false){
                send(SURPLUS, CONTROLLING_AGENT_ID, me, sendBack);
                assess("Surplus weight "+ sendBack +" was sent to the controlling agent");
            }
            break;
        }
        redisplay();
    }

    
    //The following two methods only apply to the controlling agent
    public Weight controllingAgentSplit() throws CannotSplit{
        Weight highestWeight = controllingAgentWeights[controllingAgentMaxWindow];
        Weight messageWeight = highestWeight.split();
        
        //Did the window slide?
        if (highestWeight.getExponent() > controllingAgentMaxWindow){
            //Now the highest weight is in a wrong place. Proceed with the fix.        
            //Join it with the existing smaller weight        
            Weight extra = controllingAgentWeights[controllingAgentMaxWindow+1].join(highestWeight);

            if (controllingAgentWeights[controllingAgentMaxWindow+1].getExponent() == controllingAgentMaxWindow+1){
                //The smaller weight did not change, so we only have to fix the larger one
                controllingAgentWeights[controllingAgentMaxWindow] = new Weight(controllingAgentMaxWindow, 0);
                controllingAgentMaxWindow++;
            }
            else{
                //The smaller weight changed
                controllingAgentWeights[controllingAgentMaxWindow]   = controllingAgentWeights[controllingAgentMaxWindow+1];
                controllingAgentWeights[controllingAgentMaxWindow+1] = extra;
            }
        }
        return messageWeight;
            
    }
    
    public boolean controllingAgentJoin(Weight joinThis){
        int windowCursor  = joinThis.getExponent();

        Weight extra = controllingAgentWeights[windowCursor].join(joinThis);

        if (controllingAgentWeights[windowCursor].getExponent() == windowCursor){
            //The window did not slide, no fix needed
        }
        else{
            //The window sled
            boolean result = controllingAgentJoin(controllingAgentWeights[windowCursor]);
            controllingAgentWeights[windowCursor]   = extra;
            return result;
        }

        if (windowCursor < controllingAgentMaxWindow)
            controllingAgentMaxWindow = windowCursor;
        return windowCursor == 0;        
    }
    
}
