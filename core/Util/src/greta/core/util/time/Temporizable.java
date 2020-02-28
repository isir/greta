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
package greta.core.util.time;

import java.util.List;

/**
 * This interface describes an object that can be temporized.
 * @see greta.core.util.time.TimeMarker TimeMarker
 * @see greta.core.util.time.Temporizer Temporizer
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - + greta.core.util.time.TimeMarker
 */
public interface Temporizable {
    /**
     * Returns the list of all time markers of this.
     * @return the list of all time markers
     */
    public List<TimeMarker> getTimeMarkers();

    /**
     * Returns a specific time marker of this.<br/>
     * If the marker is not found, it must return null.
     * @param name the name of the time marker
     * @return the time marker
     */
    public TimeMarker getTimeMarker(String name);

    /**
     * Returns the identifier of this.
     * @return the identifier of this
     */
    public String getId();

    /**
     * Computes all {@code TimeMarkers} of this.<br/>
     * The calculation must take account of {@code TimeMarkers} already computed.<br/>
     * This function must set a value for non concrete {@code TimeMarkers},
     * it will be call by the {@code Temporizer} to solve the temporization.
     */
    public void schedule();


    public TimeMarker getStart();

    public TimeMarker getEnd();

}
