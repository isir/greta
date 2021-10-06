package greta.auxiliary.sampleModule;

import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;

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
    
    public BurstRunnable(double parBurstStart, List<Signal> parSignalList, ID parID, Mode parMode, SignalPerformer parPerformer){
        this.burstStart = parBurstStart;
        this.signalList = parSignalList;
        this.id = parID;
        this.mode = parMode;
        this.performer = parPerformer;
    }
    
    @Override
    public void run(){
        System.out.println("Thread with start = " + this.burstStart + " --- list = " + this.signalList);
        //performSignals(signalList, id, mode);
        performer.performSignals(signalList, id, mode);
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
