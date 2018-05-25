/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.bml.timelines;

import vib.tools.editors.TimeLine;
import vib.core.signals.Signal;
import vib.core.signals.SignalProvider;
import vib.core.signals.TorsoSignal;
import java.awt.FontMetrics;
import vib.core.util.time.Temporizable;
import vib.tools.editors.MultiTimeLineEditors;
import vib.tools.editors.TemporizableContainer;

/**
 *
 * @author Andre-Marie
 */
public class TorsoTimeLine extends TimeLine<TorsoSignal>{
    
    private static int torsoSignalCount = 0;

    public TorsoTimeLine(MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(_bmlEditor);    
    }
    
    @Override
    protected TemporizableContainer<TorsoSignal> instanciateTemporizable(double startTime, double endTime) {
        String name = Integer.toString(torsoSignalCount);
        while (name.length() < NUM_DISPLAYIED_DIGITS_ID) name = "0" + name;
        Signal torso = SignalProvider.create("torso", "torso_" + name);
        torsoSignalCount ++;
        
        if(torso instanceof TorsoSignal){
            torso.getStart().setValue(startTime);
            torso.getEnd().setValue(endTime);
            return new TemporizableContainer<TorsoSignal>((TorsoSignal)torso, manager.getLabel());
        }
        else
            return null;
    }

    //Changed return value with ID (IGR students patch)    
    @Override
    protected String getDescription(TemporizableContainer<TorsoSignal> temporizableContainer, FontMetrics metrics, int limitSize) {
        String id = temporizableContainer.getId();
        /*TorsoLib torsoLib = new TorsoLib();
        try{
            id += ":" + torsoLib.getInstanceNameOf(temporizable);
        }
        catch(Exception e){}
        */
        return isGoodSize(id, metrics, limitSize) ? id : null;
    }

}
