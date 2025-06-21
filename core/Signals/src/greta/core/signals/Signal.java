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

import greta.core.util.time.Temporizable;

/**
 * This Interface describes functions of a {@code Signal}.<br/>
 * {@code Signals} are also Temporizable. They can contains many TimeMarkers as point of synchronization.<br/>
 * For exemple, in BML specification, their is 7 time markers per behavior :
 * {@code start}, {@code ready}, {@code stroke-start}, {@code stroke}, {@code stroke-end}, {@code relax}
 * and {@code end}.<br/>
 * According to BML specification :<br/>
 * speech : {@code start=ready=stroke-start=stroke} and {@code stroke-end=relax=end}.<br/>
 * face : {@code start}, {@code ready=stroke-start=stroke}, {@code stroke-end}, {@code relax} and {@code end}.<br/>
 * gaze : {@code start}, {@code ready=stroke-start=stroke}, {@code stroke-end=relax} and {@code end}.<br/>
 * head direction : {@code start}, {@code ready=stroke-start=stroke}, {@code stroke-end=relax} and {@code end}.<br/>
 * head movement : {@code start=ready=stroke-start=stroke} and {@code stroke-end=relax=end}.<br/>
 * torso or gesture : all are potentially different.
 * @author Andre-Marie Pez
 */
public interface Signal extends Temporizable{

    /**
     * Returns the name of the modality used by the {@code Signal}.<br/>
     * This name can optionally specify the submodality used by separating the two names by a dot.
     * @return the name of the modality
     */
    public String getModality();

}
