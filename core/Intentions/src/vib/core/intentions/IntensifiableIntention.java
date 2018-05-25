/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.intentions;

/**
 *
 * @author Angelo Cafaro
 */
public interface IntensifiableIntention extends Intention {
    
    public double getIntensity();
    
    public void setIntensity(double intensity);
}
