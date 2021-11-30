/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sampleModule;

import greta.core.behaviorrealizer.keyframegenerator.FaceKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.GazeKeyframeGenerator;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.time.Temporizer;
import java.util.ArrayList;
import java.util.List;

import greta.core.behaviorrealizer.keyframegenerator.GestureKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.HeadKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.KeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.LaughKeyframeGenerator;
import greta.core.behaviorrealizer.keyframegenerator.SpeechKeyframeGenerator;
import greta.core.repositories.SignalFiller;
import greta.core.signals.gesture.PointingSignal;

import java.util.TreeMap;
import java.util.Map;

//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.CyclicBarrier;

/**
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 */
public class SignalForwarder implements SignalPerformer, SignalEmitter{
    
    private double currentStart = 0.0;
    private int currentBurstNumber = 1;
    
    List<Signal> currentSignalList = new ArrayList<>();
    private TreeMap<Double, List<Signal>> treeList = new TreeMap<Double, List<Signal>>();
    private long lastStart = 0; 

    protected List<SignalPerformer> performerList = new ArrayList<>();
    
    private List<KeyframeGenerator> generators;
    private GazeKeyframeGenerator gazeGenerator;
    private FaceKeyframeGenerator faceGenerator;
    private GestureKeyframeGenerator gestureGenerator;
    
    //private CyclicBarrier gate = new CyclicBarrier(1);
    
    @Override
    public void performSignals(List<Signal> list, ID id, Mode mode) {
                
        /*      REALIZER STEP ONE      */
        for (Signal signal : list) {
            if(signal instanceof PointingSignal)
                gestureGenerator.fillPointing((PointingSignal)signal);
            else {
                SignalFiller.fillSignal(signal);
            }
        }
        Temporizer temporizer = new Temporizer();
        temporizer.add(list);
        temporizer.temporize();
        
        //DEBUG OUTPUT LISTS OF SIGNALS
        list.forEach((currentSignal) -> {
            System.out.println(currentSignal + " --- " + currentSignal.getStart().isConcretized() + " --- " + currentSignal.getStart().getValue() + " --- " + currentSignal.getEnd().getValue());
        });
        
        /*              PARSER              */
        /* Parse the list of signals into a */
        /* TreeMap with keys = start time   */
        /* and values = list of signals     */
        list.forEach((currentSignal) -> {

            //DEBUG
            //System.out.println(currentSignal + " --- " + currentSignal.getStart().getValue());
            
            //if current startTime (key) already in the treemap, get corresponding signalList (value)
            if(treeList.containsKey(currentSignal.getStart().getValue())){
                currentSignalList = treeList.get(currentSignal.getStart().getValue());
            }
            
            //append current signal to signalList (either empty list or found list - see above) and put into treeMap
            currentSignalList.add(currentSignal);
            treeList.put(currentSignal.getStart().getValue(), currentSignalList);
            currentSignalList = new ArrayList<>();
                    
        });
        
        /* THREAD TESTING */
        /*    NOT USED    */
        /*gate = new CyclicBarrier(treeList.size() + 1);
        
        for(Map.Entry<Double, List<Signal>> entry : treeList.entrySet()) {
            Thread thread = new Thread(new BurstRunnable(entry.getKey(), entry.getValue(), id, mode, performerList.get(0), gate));
            thread.start();
        }
        
        System.out.println("---------- Threads all created ! -----------");
        System.out.println("OPENING GATE ... \n");
        
        try{
            gate.await();
        }
        catch(Exception e){
            
        }*/
        
        /*              SENDER               */
        /*Goes through the TreeMap and sends */
        /*the signals by burst of start times*/
        /*      TIMING METHOD NOT FINAL      */
        System.out.println("\n*********** Start of " + id + " **********");
        
        //Get the first entry to later adjust times
        double firstEntryKey = treeList.firstEntry().getKey();
        long startTime = System.currentTimeMillis();
        
        for(Map.Entry<Double, List<Signal>> entry : treeList.entrySet()) {
            //Adjust the current key to account for negative starting times
            double currentKeyAdjusted = entry.getKey() + Math.abs(firstEntryKey);
            //wait the amount of time between current start and last start
            //NOTE: That timing method might introduce delay and therefor isn't final
            try{
                long sleepTime = (long)(currentKeyAdjusted * 1000) - lastStart;
                Thread.sleep(sleepTime);
                System.out.println("WAITED : " + sleepTime);
                lastStart = (long)(currentKeyAdjusted * 1000);
            }
            catch(Exception e){
                System.out.println("ERROR --- " + e);
            }
            
            //DEBUG: output burst number, start time and list of signals sent
            System.out.println("[" + currentBurstNumber + "]        " + currentKeyAdjusted + "         " + entry.getValue());
            currentBurstNumber++;
            
            //send list of signals
            performerList.get(0).performSignals(entry.getValue(), id, mode);
        }
        
        //DEBUG: calculate elapsed time to monitor any big delay
        long endTime = System.currentTimeMillis();
        System.out.println("elapsed time = " + (endTime - startTime));
        
        System.out.println("*********** End of " + id + " **********\n");
        
        currentBurstNumber = 1;
        currentStart = 0.0;
        currentSignalList = new ArrayList<>();
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
