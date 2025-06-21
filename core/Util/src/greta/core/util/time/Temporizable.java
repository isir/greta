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
