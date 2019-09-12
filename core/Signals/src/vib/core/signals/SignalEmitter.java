/*
 * This file is part of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 */

package vib.core.signals;

/**
 * This interface describes an object that can send a list of {@code Signal} to all {@code SignalPerformer} added.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - * vib.core.signals.SignalPerformer
 * @navassoc - "emmits" * vib.core.signals.Signal
 */
public interface SignalEmitter {
    /**
     * Adds a {@code SignalPerformer}.<br/>
     * The function {@code performSignals} of all {@code SignalPerformer}
     * added will be called when this emmits a list of {@code Signal}.
     * @param performer the {@code SignalPerformer} to add
     * @see vib.core.signals.SignalPerformer#performSignals(java.util.List, vib.core.util.id.ID, vib.core.util.Mode) performeSignals
     */
    void addSignalPerformer(SignalPerformer performer);

    /**
     * Removes the first occurence of a {@code SignalPerformer}.
     * @param performer the {@code SignalPerformer} to remove
     */
    void removeSignalPerformer(SignalPerformer performer);
}
