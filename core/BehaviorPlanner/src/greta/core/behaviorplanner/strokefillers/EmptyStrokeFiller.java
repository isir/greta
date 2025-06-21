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
 * This implementation of {@code StrokeFiller} adds no stroke in {@code Signals}, never.
 * @author Andre-Marie Pez
 */
public class EmptyStrokeFiller implements StrokeFiller{

    @Override
    public void fill(Signal signalToFill, BehaviorSet behaviorSet, DynamicLine dynamicLine, List<Signal> otherSignals) {
        // nothing to do in this class
    }

}
