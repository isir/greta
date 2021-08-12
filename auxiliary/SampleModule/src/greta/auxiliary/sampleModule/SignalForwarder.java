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

    protected List<SignalPerformer> performerList = new ArrayList<>();
    @Override
    public void performSignals(List<Signal> list, ID id, Mode mode) {
        for(SignalPerformer sp : performerList){
            System.out.println(String.format("SignalForwarder: sending %s to performer %s",id.toString(),sp.toString()));
            sp.performSignals(list, id, mode);
            
        }
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
