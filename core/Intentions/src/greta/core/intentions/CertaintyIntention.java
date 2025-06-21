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
