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
package greta.core.signals;

import greta.core.util.time.TimeMarker;

/**
 * This interface defined a {@code Signal} that can use more than one stroke.
 * @author Andre-Marie Pez
 */
public interface MultiStrokeSignal extends Signal{

    /**
     * Returns the {@code TimeMarker} of a specific stroke.
     * @param index the index of the stroke
     * @return the {@code TimeMarker} of the requested stroke or {@code null} if there is no stroke at the specified index
     */
    public TimeMarker getStroke(int index);

    /**
     * Set a synchPoint reference to a specified stroke.<br/>
     * It is assumed that if no stroke existe at the index, it will be created.<br/>
     * It may use {@code [targetStroke].addReference(synchPoint)}.
     * @param index the index of the stroke
     * @param synchPoint the synchPoint reference of the stroke
     */
    public void setStroke(int index, String synchPoint);
}
