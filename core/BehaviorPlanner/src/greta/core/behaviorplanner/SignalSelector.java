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
package greta.core.behaviorplanner;

import greta.core.behaviorplanner.baseline.DynamicLine;
import greta.core.behaviorplanner.lexicon.BehaviorSet;
import greta.core.intentions.Intention;
import greta.core.signals.Signal;
import greta.core.util.CharacterManager;
import java.util.List;

/**
 * This interface encapsulates algorithms of signal selection from a communicative intention.<br/>
 * Classes that implement this interface will be used by the {@code Planner}.
 * @see greta.core.behaviorplanner.Planner Pnanner
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 */
public interface SignalSelector {

   public String getType();

    /**
     * This function selects {@code Signals} corresponding to a specific {@code Intention} and returns the list of the selected {@code Signals}.<br/>
     * The ids of resulted {@code Signals} must equals {@code intention.getId()+"_"+ an-identifier-without-underscore}.<br/>
     * This id structure will be used to synchronise theire {@code TimeMarkers}.
     * @param intention the {@code Intention to refer}
     * @param behaviorSet a set of {@code SignalItem} that correspond to the {@code Intention}. It can be use to choose {@code Signals} or not.
     * @param dynamicLine the state of the agent during the {@code Intention}
     * @param existingSignals the list of {@code Signals} that already exist during the {@code Iintention}. They must not be changed.
     * @return the list of selected {@code Signal}. If no signal was selected (because time constraint etc.), the list is empty. It returns {@code null} if this cannot oparate with this {@code Intention}.
     */

    public List<Signal> selectFrom(Intention intention, BehaviorSet behaviorSet, DynamicLine dynamicLine, List<Signal> existingSignals, CharacterManager cm);

    /**
     * Allows to know if this {@code SignalSelector} can select {@code Signals} from a specific {@code Intention} within a specific context.
     * @param intention the intention
     * @param context the context of the {@code Intention}
     * @return {@code true} if this accepts to select {@code Signals} from the {@code Intention} within the context. {@code false} otherwise.
     */

    public boolean acceptIntention(Intention intention, List<Intention> context);
}
