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
package greta.core.intentions;

import greta.core.util.time.TimeMarker;

/**
 * This {@code BasicIntention} contains informations about an emotion.
 * @author Andre-Marie Pez
 */
public class CertaintyIntention extends BasicIntention implements IntensifiableIntention {

    private double intensity;

    public CertaintyIntention(String id, String type, TimeMarker start, TimeMarker end, double importance, double intensity){
        super("certainty", id, type, start, end, importance);
        this.intensity = intensity;
    }

    public CertaintyIntention(String id, String type, TimeMarker start, TimeMarker end, double intensity){
        this(id, type, start, end, 0.5, intensity);
    }

    /**
     * Returns the intensity of this CertaintyIntention.
     * @return the intensity of this CertaintyIntention
     */
    public double getIntensity(){
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }
}
