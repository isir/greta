/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.bml.timelines;

import vib.tools.editors.TimeLine;
import vib.core.signals.HeadSignal;
import vib.core.signals.Signal;
import vib.core.signals.SignalProvider;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Frame;
import vib.core.util.time.Temporizable;
import vib.tools.editors.MultiTimeLineEditors;
import vib.tools.editors.TemporizableContainer;

/**
 *
 * @author Andre-Marie
 */
public class HeadTimeLine extends TimeLine<HeadSignal>{
    
    private static int headSignalCount = 0;

    public HeadTimeLine(MultiTimeLineEditors<? extends Temporizable> _bmlEditor) {
        super(_bmlEditor);    
    }

    @Override
    protected TemporizableContainer<HeadSignal> instanciateTemporizable(double startTime, double endTime) {
        String name = Integer.toString(headSignalCount);
        while (name.length() < NUM_DISPLAYIED_DIGITS_ID) name = "0" + name;
        Signal head = SignalProvider.create("head", "head_" + name);
        headSignalCount ++;
        
        if(head instanceof HeadSignal){
            head.getStart().setValue(startTime);
            head.getEnd().setValue(endTime);
            return new TemporizableContainer<HeadSignal>((HeadSignal)head, manager.getLabel());
        }
        else
            return null;
    }

    //Changed return value with ID (IGR students patch)
    @Override
    protected String getDescription(TemporizableContainer<HeadSignal> temporizableContainer, FontMetrics metrics, int limitSize) {
        String id = temporizableContainer.getId();
        HeadLib headLib = new HeadLib();
        try{
            id += ":" + headLib.getInstanceNameOf(temporizableContainer.getTemporizable());
        }
        catch(Exception e){}
        return isGoodSize(id, metrics, limitSize) ? id : null;
    }


    @Override
    protected TemporizableContainer<HeadSignal> editTemporizable(TemporizableContainer<HeadSignal> temporizableContainer){
        Container parent = this.getParent();
        while(parent!=null && !(parent instanceof Frame)) {
            parent = parent.getParent();
        }
        if(parent!=null){
//            List<SignalEntry<HeadSignal>> entries = HeadLibrary.getGlobalLibrary().getAll();
//            ArrayList<HeadSignal> heads = new ArrayList<HeadSignal>(entries.size());
//            for(SignalEntry<HeadSignal> entry : entries){
//                heads.add(entry.getSignal());
//            }
            ParametricSignalEditor<HeadSignal> dialog =
                    new ParametricSignalEditor<HeadSignal>(
                            (Frame)parent,
                            true,
                            temporizableContainer,
                            new HeadLib(),
                            false,
                            this.multiTimeLineEditor
                            );
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
            return dialog.edited;
        }

        return temporizableContainer;
    }

}

