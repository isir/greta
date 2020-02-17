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
 * This implementation of {@code StrokeFiller} adds no stroke in {@code Signals}, never.
 * @author Andre-Marie Pez
 */
public class EmptyStrokeFiller implements StrokeFiller{

    @Override
    public void fill(Signal signalToFill, BehaviorSet behaviorSet, DynamicLine dynamicLine, List<Signal> otherSignals) {
        // nothing to do in this class
    }

}
