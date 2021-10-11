package greta.auxiliary.sampleModule;

import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

/**
 *
 * @author Sean
 */

public class BurstRunnable implements Runnable, SignalPerformer, SignalEmitter{
    
    private double burstStart;
    private List<Signal> signalList;
    private ID id;
    private Mode mode;
    private SignalPerformer performer;
    private CyclicBarrier gate;
    
    public BurstRunnable(double parBurstStart, List<Signal> parSignalList, ID parID, Mode parMode, SignalPerformer parPerformer, CyclicBarrier parGate){
        this.burstStart = parBurstStart;
        this.signalList = parSignalList;
        this.id = parID;
        this.mode = parMode;
        this.performer = parPerformer;
        this.gate = parGate;
    }
    
    @Override
    public void run(){
        double waitTime = burstStart;
        try{
            gate.await();
            if(burstStart > 2.0){
                waitTime = waitTime - 1.0;
            }
            Thread.sleep((long)(this.burstStart * 1000));
            System.out.println("Thread with start = " + this.burstStart + " --- list = " + this.signalList);
            performSignals(signalList, id, mode);
        }
        catch(Exception e){
            System.out.println("Exception caught : " + e);
        }
    }

    @Override
    public void performSignals(List<Signal> list, ID id, Mode mode) {
        performer.performSignals(list, id, mode);
    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
