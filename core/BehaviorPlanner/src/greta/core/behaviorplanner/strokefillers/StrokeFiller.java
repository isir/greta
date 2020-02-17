/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
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
