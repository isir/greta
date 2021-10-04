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
import greta.core.util.time.Temporizer;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.TimeUnit;
import java.util.TreeMap;
import java.util.Map;

//greta\core\BehaviorRealizer\src\greta\core\behaviorrealizer\keyframegenerator

/**
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 */
public class SignalForwarder implements SignalPerformer, SignalEmitter{
    
    private double currentStart = 0.0;
    private int currentColorNumber = 1;
    
    List<Signal> currentSignalListed = new ArrayList<>();
    private TreeMap<Double, List<Signal>> treeList = new TreeMap<Double, List<Signal>>();
    private long lastStart = 0; 
    
    //private Signal stockedGesture = null;

    protected List<SignalPerformer> performerList = new ArrayList<>();
    @Override
    public void performSignals(List<Signal> list, ID id, Mode mode) {
        
        //TESTED WITH TEMPORIZE, RESULTS VARY, RECOMMENDED WITHOUT SO FAR
        /*Temporizer temporizer = new Temporizer();
        temporizer.add(list);
        temporizer.temporize();*/
        
        //DEBUG OUTPUT LISTS OF SIGNALS
        list.forEach((currentSignal) -> {
            System.out.println(currentSignal + " --- " + currentSignal.getStart().isConcretized() + " --- " + currentSignal.getStart().getValue() + " --- " + currentSignal.getEnd().getValue());
        });
        
        /*              PARSER              */
        /* Parse the list of signals into a */
        /* TreeMap with keys = start time   */
        list.forEach((currentSignal) -> {
            
            //PARSER V1 --- Gave best results so far (04/10/21)
            if(currentSignal.getStart().getValue() == currentStart || currentSignal.toString().contains("gesture")){
                currentSignalListed.add(currentSignal);
            }
            else{
                treeList.put(currentStart, currentSignalListed);
                currentSignalListed = new ArrayList<>();
                currentSignalListed.add(currentSignal);
                currentStart = currentSignal.getStart().getValue();
            }
            
            //PARSER V2
            /*if(treeList.containsKey(currentSignal.getStart().getValue())){
                currentSignalListed = treeList.get(currentSignal.getStart().getValue());
            }
            if(currentSignal.toString().contains("gesture")){
                stockedGesture = currentSignal;
            }
            else{
            //System.out.println(currentSignal.getStart().getValue());
                if(stockedGesture != null){
                    currentSignalListed.add(stockedGesture);
                    stockedGesture = null;
                }
                currentSignalListed.add(currentSignal);
                treeList.put(currentSignal.getStart().getValue(), currentSignalListed);
                
            }
            currentSignalListed = new ArrayList<>();*/
            
        });
        
        /*              SENDER               */
        /*Goes through the TreeMap and sends */
        /*the signals by burst of start times*/
        /*      TIMING METHOD NOT FINAL      */
        System.out.println("\n\u001b[30m*********** Start of " + id + " **********");
        
        for(Map.Entry<Double, List<Signal>> entry : treeList.entrySet()) {
            try{
                Thread.sleep((long)(entry.getKey() * 1000) - lastStart);
                System.out.println("WAITED : " + ((entry.getKey() * 1000) - lastStart));
                lastStart = (long)(entry.getKey() * 1000);
            }
            catch(Exception e){
                System.out.println("ERROR --- " + e);
            }
            System.out.println("\u001b[3" + currentColorNumber + "m  [" + currentColorNumber + "]        " + entry.getKey() + "         " + entry.getValue());
            currentColorNumber++;
            performerList.get(0).performSignals(entry.getValue(), id, mode);
        }
        
        System.out.println("\u001b[30m*********** End of " + id + " **********\n");
        
        currentColorNumber = 1;
        currentStart = 0.0;
        currentSignalListed = new ArrayList<>();
        treeList = new TreeMap<Double, List<Signal>>();
        lastStart = 0;
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