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

import java.util.concurrent.TimeUnit;
import java.util.TreeMap;
import java.util.Map;
/**
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 */
public class SignalForwarder implements SignalPerformer, SignalEmitter{
    
    private double currentStart = 0.0;
    private int currentColorNumber = 1;
    
    List<Signal> currentSignalListed = new ArrayList<>();
    private TreeMap<Double, List<Signal>> treeList = new TreeMap<Double, List<Signal>>();

    protected List<SignalPerformer> performerList = new ArrayList<>();
    @Override
    public void performSignals(List<Signal> list, ID id, Mode mode) {
        
        /*              PARSER              */
        /* Parse the list of signals into a */
        /* TreeMap with keys = start time   */
        list.forEach((currentSignal) -> {
            if(currentSignal.getStart().getValue() == currentStart || currentSignal.getStart().getValue() == 0.0){
                currentSignalListed.add(currentSignal);
            }
            else{
                treeList.put(currentStart, currentSignalListed);
                currentSignalListed = new ArrayList<>();
                currentSignalListed.add(currentSignal);
                currentStart = currentSignal.getStart().getValue();
            }
        });
        
        /*              SENDER               */
        /*Goes through the TreeMap and sends */
        /*the signals by burst of start times*/
        System.out.println("\n\u001b[30m*********** Start of " + id + " **********");
        
        for (Map.Entry<Double, List<Signal>> entry : treeList.entrySet()) {
            System.out.println("\u001b[3" + currentColorNumber + "m  [" + currentColorNumber + "]        " + entry.getKey() + "         " + entry.getValue());
            currentColorNumber++;
            performerList.get(0).performSignals(entry.getValue(), id, mode);
        }
        
        System.out.println("\u001b[30m*********** End of " + id + " **********\n");
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
