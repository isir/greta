/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.intentions;

import vib.core.util.time.TimeMarker;

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
