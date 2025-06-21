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
