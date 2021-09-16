/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 */
public class SignalForwarder implements SignalPerformer, SignalEmitter{
    
    private double currentStart = 0.0;
    private int currentColorNumber = 1;

    protected List<SignalPerformer> performerList = new ArrayList<>();
    @Override
    public void performSignals(List<Signal> list, ID id, Mode mode) {
        
        System.out.println("\u001b[30m*********** Start of " + id + " ********** V2");
        currentStart = 0.0;
        currentColorNumber = 1;
        
        list.forEach((currentSignal) -> {
            if(currentSignal.getStart().getValue() > currentStart){
                currentStart = currentSignal.getStart().getValue();
                currentColorNumber += 1;
            }
            System.out.println("\u001b[3" + currentColorNumber + "m" + currentSignal + " --- " + currentSignal.getStart() + " --- " + currentSignal.getEnd());
            List<Signal> currentSignalListed = new ArrayList<>();
            currentSignalListed.add(currentSignal);
            performerList.get(0).performSignals(currentSignalListed, id, mode);
        }); 
        
        //performerList.get(0).performSignals(list, id, mode);
        System.out.println("\u001b[30m*********** End of " + id + " **********\n");
        /*for(SignalPerformer sp : performerList){
            System.out.println(String.format("SignalForwarder: sending %s to performer %s",id.toString(),sp.toString()));
            sp.performSignals(list, id, mode);
        }*/
    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        performerList.add(sp);        
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        performerList.remove(sp);
    }
    
}
