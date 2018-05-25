/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.bml.timelines;

import vib.tools.editors.TimeLine;
import vib.core.signals.GazeSignal;
import vib.core.signals.Signal;
import vib.core.signals.SignalProvider;
import java.awt.FontMetrics;
import vib.core.util.time.Temporizable;
import vib.tools.editors.MultiTimeLineEditors;
import vib.tools.editors.TemporizableContainer;

/**
 *
 * @author Andre-Marie
 */
public class GazeTimeLine extends TimeLine<GazeSignal>{
    private static int gazeSignalCount = 0;
    
    public GazeTimeLine(MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(_bmlEditor);    
    }
    @Override
    protected TemporizableContainer<GazeSignal> instanciateTemporizable(double startTime, double endTime) {
        String name = Integer.toString(gazeSignalCount);
        while (name.length() < NUM_DISPLAYIED_DIGITS_ID) name = "0" + name;
        Signal gaze = SignalProvider.create("gaze", "gaze_" + name);
        gazeSignalCount ++;
        if(gaze instanceof GazeSignal){
            gaze.getStart().setValue(startTime);
            gaze.getEnd().setValue(endTime);
            return new TemporizableContainer<GazeSignal>((GazeSignal)gaze, manager.getLabel());
        }
            return null;
    }

        //Changed return value with ID (IGR students patch)
    @Override
    protected String getDescription(TemporizableContainer<GazeSignal> temporizableContainer, FontMetrics metrics, int limitSize) {
        String id = temporizableContainer.getId();
        /*GazeLib gazeLib = new GazeLib();
        try{
            id += ":" + gestLib.getInstanceNameOf(temporizable);
        }
        catch(Exception e){}
        */
        return isGoodSize(id, metrics, limitSize) ? id : null;
    }

}
