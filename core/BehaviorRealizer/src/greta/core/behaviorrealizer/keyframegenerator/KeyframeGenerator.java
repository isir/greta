/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.behaviorrealizer.keyframegenerator;

import greta.core.keyframes.Keyframe;
import greta.core.signals.Signal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
