/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorplanner.strokefillers;

import vib.core.behaviorplanner.baseline.DynamicLine;
import vib.core.behaviorplanner.lexicon.BehaviorSet;
import vib.core.behaviorplanner.lexicon.Shape;
import vib.core.behaviorplanner.lexicon.SignalItem;
import vib.core.signals.MultiStrokeSignal;
import vib.core.signals.ParametricSignal;
import vib.core.signals.Signal;
import vib.core.util.log.Logs;
import vib.core.util.time.SynchPoint;
import vib.core.util.time.TimeMarker;
import java.util.List;

/**
 * This implementation of {@code StrokeFiller} distributed a number of stroke regularly.<br/>
 * This number depends on the REP parameter and the length of the {@code Signal}.
 * @author Andre-Marie Pez
 */
public class SmoothStrokeFiller implements StrokeFiller{

    /**
     * The default threshold used in the case of an invalid value is used
     */
    private static final double DEFAULT_THRESHOLD = 1; //it must be strictly positive !

    /**
     * The threshold used
     */
    private double threshold;

    /**
     * Creates a {@code SmoothStrokeFiller} with a specific threshold.<br/>
     * It defines a minimum time between strokes, when a minimum duration for a {@code Signal} is not specified or to short.<br/>
     * It must be more than zero.
     * @param threshold the threshold to use
     */
    public SmoothStrokeFiller(double threshold){
        if(threshold>0){
            this.threshold = threshold;
        }
        else{
            this.threshold = DEFAULT_THRESHOLD;
            Logs.warning(this.getClass().getName()+" the threshold must be more than 0. The default threshold is used ("+DEFAULT_THRESHOLD+")");
        }
    }

    /**
     * Default constructor.
     */
    public SmoothStrokeFiller(){
        this(DEFAULT_THRESHOLD);
    }


    @Override
    public void fill(Signal signalToFill, BehaviorSet behaviorSet, DynamicLine dynamicLine, List<Signal> otherSignals) {
        //check if we can add stroke in this signal
        if(!(signalToFill instanceof MultiStrokeSignal)) {
            return;//we must add more than one stroke
        }

        //check if we have repetitions, else we let the others compute the unic stroke time
        double rep = dynamicLine.getParameter(signalToFill.getModality(), "REP").getValueIn(0, 1);
        if(rep==0) {
            return; // no repetition, no stroke to add
        }

        //get the duration of the signal
        double duration = Double.NEGATIVE_INFINITY;
        boolean found = false;
        TimeMarker end = signalToFill.getEnd();
        TimeMarker start = signalToFill.getStart();
        if(end.isConcretized()){
            if(start.isConcretized()){
                duration = end.getValue()-start.getValue();
                found = true;
            }
        }
        if(!found){
            //check if end has a synchpoint that refere to start
            //so will be start+duration
            for(SynchPoint sp : end.getReferences()){
                if(sp.hasTargetTimeMarker()){
                    if(
                       (sp.getTargetName() != null && sp.getTargetName().equalsIgnoreCase("start"))  ||
                       (sp.getTarget() != null && sp.getTarget().getName().equalsIgnoreCase("start"))
                      ){
                        duration = sp.getOffset();
                        found = true;
                        break;
                    }
                }
            }
        }
        if(!found || duration<=0) {
            return;//unkown or negative duration
        }

        //get min duration in the behaviorSet
        double min_duration = Double.NEGATIVE_INFINITY;
        if(signalToFill instanceof ParametricSignal){//we need a reference name to retrieve the min duration
            for(SignalItem item : behaviorSet.getBaseSignals()){
                Shape target = item.getShape(((ParametricSignal)signalToFill).getReference());
                if(target!=null){
                    min_duration = target.getMin();
                    break;
                }
            }
        }
        //floor the min duration with the threshold
        min_duration = Math.max(min_duration, threshold);

        //computes the number of strokes :
        System.out.println("duration="+min_duration + " : rep="+rep +" modality"+signalToFill.getModality());
        int nb_strokes = (int)(duration/min_duration*rep);

        if(nb_strokes<2) {
            return;//0 or 1 stroke : we let the others compute the unic stroke time
        }

        //the duration between 2 strokes
        double stroke_duration = duration/((double)nb_strokes);

        //time of the first stroke
        //if start TimeMarker is concret we will use absolute time else we use relative time
        double cumul_duration = stroke_duration/2.0 + (start.isConcretized() ? start.getValue() : 0);
        String ref_start = start.isConcretized() ? "" : "start+";

        //add all strokes
        for(int i=0;i<nb_strokes;++i){
            ((MultiStrokeSignal)signalToFill).setStroke(i, ref_start+cumul_duration);
            cumul_duration += stroke_duration; //time of the next stroke
        }
    }
}
