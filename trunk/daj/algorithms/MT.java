package daj.algorithms;

import daj.DistAlg;
import daj.Screen;
//import daj.algorithms.HeterogeneousNodes.NodeStrategy;

import java.util.TreeSet;

public class MT extends HeterogeneousNodes{

    public static final int ENVIRONMENT_NODE_ID =  0;
    
    public static final int CREDIT_MESSAGE      =  0;
    public static final int ZERO_CREDIT         = -1;    

    private int environmentButtons;
    private int inactiveButtons;
    private int activeButtons;
    private int addressButtons;
    
    public static String FORK       = "Fork new process";
    public static String TERMINATE  = "Terminate";
    
    public static final int FORK_STATE      = 0;
    public static final int TERMINATE_STATE = 1;    
    
    private class EnvironmentNode implements NodeStrategy{

        private TreeSet<Integer> debt;
        
        public void init(){
            debt = new TreeSet<Integer>();
        }
        
        public boolean isQuiescent(){
            return debt.size() == 0;
        }
        
        public int constructButtons(){
            environmentButtons = addComponents("Choose action : ", FORK, null, null);
            addressButtons     = addressButtons("Which node : ", null, null);
         
            return environmentButtons;
        }
        
        public String constructTitle1(){
            return "Environment node (" +
                Screen.node[me]+") "+
                (isQuiescent()?"Quiescent":"Not quiescent");
        }
        
        public String constructTitle2(){
            
            if (isQuiescent())
                return "Debt set empty";
            
            StringBuffer result = new StringBuffer();
            result.append("<HTML>Debt set : {");
            
            for (Integer cursor:debt){
                result.append("2<SUP>");
                result.append("-"+cursor);
                result.append("</SUP>, ");
            }
            
            result.delete(result.length()-2,result.length());
            result.append(" }</HTML>");
            
            return result.toString();
        }
        
        public String constructRow1(int node){
            return ""; //Not applicable
        }
        
        public String constructRow2(int node){
            return ""; //Not applicable
        }

        public boolean stateMachine(String command){
            if (command.equals(FORK)){
                changeState(FORK_STATE, environmentButtons, addressButtons);
                redisplay();
                return false;//Do nothing (Only one type of button)
            }
            
            //The button must be an address button
            int destination = FindNode(command);
            
            int creditGiven;

            for (creditGiven = 1; debt.contains(creditGiven); creditGiven++);
            debt.add(creditGiven);
            
            send(CREDIT_MESSAGE, destination, me, creditGiven);
            changeState(state, addressButtons, environmentButtons);
            redisplay();
            return false;
        }
  
        public void receive(int messageType, int sender, int credit){
            if (credit == ZERO_CREDIT)
                return;
            
            while (debt.contains(credit) == false){
                debt.add(credit);
                credit--;
            }
            
            debt.remove(credit);
            redisplay();
        }

    }
    
    private class NormalNode implements NodeStrategy{
        
        private int credit;
    
        public void init(){
            credit = -1;
        }
        
        public int constructButtons(){
            activeButtons      = addComponents("", FORK, TERMINATE, null);
            inactiveButtons    = addComponents("[Inactive] ", null, null, null);

            addressButtons     = addressButtons(null, null, null);
            return inactiveButtons;
        }
        
        public String constructTitle1(){
            return "Process ("+Screen.node[me]+")";
        }
        
        public String constructTitle2(){
            if (credit == -1){        
                return "Inactive";
            }
            else{
                return "<HTML>Active with weight : 2<SUP>-" + credit+"</SUP></HTML>";
            }
        }
            
        public String constructRow1(int node){
            //Not applicable
            return "";
        }

        public String constructRow2(int node){
            //Not applicable
            return "";
        }

        public boolean stateMachine(String command){
            if (command.equals(FORK)){
                changeState(FORK_STATE, activeButtons, addressButtons);
                redisplay();
                return false;//Do nothing (Only one type of button)
            }else if (command.equals(TERMINATE)){
                changeState(TERMINATE_STATE, activeButtons, addressButtons);
                redisplay();
            }
            
            //The button must be an address button
            int destination = FindNode(command);
            
            switch (state){
            case FORK_STATE:
                if (destination == ENVIRONMENT_NODE_ID){
                    assess("The environment should not be the source of a fork");
                    return false;
                }
                this.credit++;
                send(CREDIT_MESSAGE, destination, me, this.credit);
                changeState(state, addressButtons, activeButtons);
                redisplay();
                return false;
                
            case TERMINATE_STATE:
                if (destination != ENVIRONMENT_NODE_ID){
                    assess("Only the environment should receive termination messages");
                    return false;
                }
                send(CREDIT_MESSAGE, destination, me, this.credit);
                this.credit = -1;
                changeState(state, addressButtons, inactiveButtons);
                redisplay();
                return false;
                
            }
            return false;
        }

        public void receive(int messageType, int sender, int credit){
            if (credit == ZERO_CREDIT)
                return;
            if (this.credit == ZERO_CREDIT){
                this.credit = credit;
                changeState(state, inactiveButtons, activeButtons);
            }else if (this.credit == credit)
                this.credit--;
            else
                send(CREDIT_MESSAGE, ENVIRONMENT_NODE_ID, me, credit);
            redisplay();
        }
        
    }    
    
    public MT(int i, DistAlg[] d, Screen s) {
        super(i, d, s);
    }

    public NodeStrategy getNodeStrategy(int nodeID, DistAlg[] allNodes, Screen screen) {
        if (nodeID == ENVIRONMENT_NODE_ID)
            return new EnvironmentNode();
        else
            return new NormalNode();
    }


    
}
