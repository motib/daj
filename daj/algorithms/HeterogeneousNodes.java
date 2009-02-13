package daj.algorithms;

import daj.DistAlg;
import daj.Screen;

public abstract class HeterogeneousNodes extends DistAlg {

    public interface NodeStrategy{
        
        public void init();
        
        public int constructButtons();
        public String constructTitle1();
        public String constructTitle2();
        public String constructRow1(int node);
        public String constructRow2(int node);

        public boolean stateMachine(String command);
        public void receive(int message, int parm1, int parm2);
    }

    private NodeStrategy strategy;
    
    public abstract NodeStrategy getNodeStrategy(
            int nodeID,
            DistAlg[] allNodes,
            Screen screen);
    
    public HeterogeneousNodes(int i, DistAlg[] d, Screen s) {
        super(i, d, s);
        strategy = getNodeStrategy(i, d, s);
        
        init();
        initialize();
    }
    
    protected void init() {
        strategy.init();
    }

    protected int constructButtons() {
        return strategy.constructButtons();
    }

    protected String constructTitle1() {
        return strategy.constructTitle1();
    }

    protected String constructTitle2() {
        return strategy.constructTitle2();
    }

    protected String constructRow1(int row) {
        return strategy.constructRow1(row);
    }

    protected String constructRow2(int row) {
        return strategy.constructRow2(row);
    }

    protected boolean stateMachine(String command) {
        return strategy.stateMachine(command);
    }

    protected void receive(int message, int parm1, int parm2) {
        strategy.receive(message, parm1, parm2);
    }

}
