/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorrealizer.keyframegenerator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import vib.core.keyframes.Keyframe;
import vib.core.signals.Signal;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class KeyframeGenerator {

    private Class signalClass;
    protected List<Signal> signals;

    public KeyframeGenerator(Class clazz){ //ensure that all signals used here are instances the specified class (we cannot use generic)
        signalClass=clazz;
        signals = new java.util.LinkedList<Signal>();
    }

    public boolean accept(Signal s){
        if(signalClass.isInstance(s)){
            signals.add(s);
            return true;
        }
        return false;
    }

    public List<Keyframe> generateKeyframes(){
        List<Keyframe> result = new java.util.LinkedList<Keyframe>();
        if( ! signals.isEmpty()){
            Collections.sort(signals, getComparator());
            generateKeyframes(signals, result);
            signals.clear();
        }
        return result;
    }

    protected abstract void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframe);
    protected abstract Comparator<Signal> getComparator();

    protected static Comparator<Signal> emptyComparator = new Comparator<Signal>() {
        @Override
        public int compare(Signal o1, Signal o2) {
            return 0;
        }
    };
    protected static Comparator<Signal> endComparator = new Comparator<Signal>() {
        @Override
        public int compare(Signal o1, Signal o2) {
            return (int) Math.signum(o1.getEnd().getValue() - o2.getEnd().getValue());
        }
    };
    protected static Comparator<Signal> startComparator = new Comparator<Signal>() {
        @Override
        public int compare(Signal o1, Signal o2) {
            return (int) Math.signum(o1.getStart().getValue() - o2.getStart().getValue());
        }
    };
}
