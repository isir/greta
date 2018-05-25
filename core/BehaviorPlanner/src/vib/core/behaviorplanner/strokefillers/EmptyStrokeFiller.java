/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorplanner.strokefillers;

import vib.core.behaviorplanner.baseline.DynamicLine;
import vib.core.behaviorplanner.lexicon.BehaviorSet;
import vib.core.signals.Signal;
import java.util.List;

/**
 * This implementation of {@code StrokeFiller} adds no stroke in {@code Signals}, never.
 * @author Andre-Marie Pez
 */
public class EmptyStrokeFiller implements StrokeFiller{

    @Override
    public void fill(Signal signalToFill, BehaviorSet behaviorSet, DynamicLine dynamicLine, List<Signal> otherSignals) {
        // nothing to do in this class
    }

}
