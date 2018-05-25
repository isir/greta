/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorplanner.strokefillers;

import vib.core.behaviorplanner.baseline.DynamicLine;
import vib.core.behaviorplanner.lexicon.BehaviorSet;
import vib.core.signals.Signal;
import java.util.List;

/**
 * Interface to define a tool used by the behavior planner to add strokes in a specific {@code Signal}
 * @author Andre-Marie Pez
 */
public interface StrokeFiller {

    /**
     * This function adds stroke(s) time(s) in one {@code Signal}.<br/>
     * It may use the current {@code DynamicLine} to know the value of the REP parameter
     * and the list of the others known {@code Signals} for synchronisation (or not).<br/>
     * It must not change the others {@code Signals}
     * @param signalToFill the {@code Signal} where strokes can be added
     * @param behaviorSet the {@code BehaviorSet} where the {@code signal} commes from
     * @param dynamicLine the current {@code DynamicLine} that contains the REP values
     * @param otherSignals a list of known {@code Signals}
     */
    public void fill(Signal signalToFill, BehaviorSet behaviorSet, DynamicLine dynamicLine, List<Signal> otherSignals);
}
