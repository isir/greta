/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;


/**
 *
 * @author Quoc Anh Le
 */
public interface Keyframe{
    public double getOffset();
    public void setOffset(double time);

    public double getOnset();
    public void setOnset(double time);

    public String getModality();
    public String getId();

    public String getPhaseType();
    public String getCategory();

}
