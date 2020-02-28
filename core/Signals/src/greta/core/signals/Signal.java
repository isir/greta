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
