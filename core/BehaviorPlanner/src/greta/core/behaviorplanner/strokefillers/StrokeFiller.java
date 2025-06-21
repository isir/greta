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
package greta.core.behaviorplanner.strokefillers;

import greta.core.behaviorplanner.baseline.DynamicLine;
import greta.core.behaviorplanner.lexicon.BehaviorSet;
import greta.core.signals.Signal;
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
