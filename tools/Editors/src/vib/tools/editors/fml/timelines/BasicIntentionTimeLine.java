/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.fml.timelines;

import vib.tools.editors.TimeLine;
import vib.core.intentions.BasicIntention;
import vib.core.util.time.TimeMarker;
import java.awt.Container;
import java.awt.FontMetrics;
import java.awt.Frame;
import vib.tools.editors.TemporizableContainer;

/**
 *
 * @author Andre-Marie
 */
public class BasicIntentionTimeLine extends TimeLine<BasicIntention>{

    private String functionName;
    private String defaultType;
    public BasicIntentionTimeLine(String functionName, String defaultType){
        this.functionName = functionName;
        this.defaultType = defaultType;
    }

    @Override
    protected TemporizableContainer<BasicIntention> instanciateTemporizable(double startTime, double endTime) {
        return new TemporizableContainer<BasicIntention>(new BasicIntention(functionName, functionName+"_"+System.currentTimeMillis(), defaultType, new TimeMarker("start",startTime), new TimeMarker("end",endTime), 0.5), manager.getLabel());
    }

    @Override
    protected String getDescription(TemporizableContainer<BasicIntention> temporizableContainer, FontMetrics metrics, int limitSize) {
        return isGoodSize(temporizableContainer.getTemporizable().getType(), metrics, limitSize) ? temporizableContainer.getTemporizable().getType() : null;
    }

    @Override
    protected TemporizableContainer<BasicIntention> editTemporizable(TemporizableContainer<BasicIntention> temporizableContainer){
        Container parent = this.getParent();
        while(parent!=null && !(parent instanceof Frame)) {
            parent = parent.getParent();
        }
        if(parent!=null){
            BasicIntentionEditor dialog =
                    new BasicIntentionEditor(
                            (Frame)parent,
                            true,
                            temporizableContainer,
                            IntentionsAviable.getAviableFor(functionName)
                            );
            dialog.setLocationRelativeTo(parent);
            dialog.setVisible(true);
            return dialog.edited;
        }

        return temporizableContainer;
    }
}
