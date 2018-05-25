/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.animationparameters;

/**
 *
 * @author Ken Prepin
 */
public class AnimationParameter {

    int value;
    boolean mask;

    public AnimationParameter(boolean mask, int value) {
        this.mask = mask;
        this.value = value;
    }

    public AnimationParameter() {
        this(false, 0);
    }

    public AnimationParameter(AnimationParameter ap) {
        this(ap.mask, ap.value);
    }

    public AnimationParameter(int value) {
        this(true, value);
    }

    public void set(boolean mask, int value){
        this.value = value;
        this.mask = mask;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void applyValue(int value) {
        this.value = value;
        this.mask = true;
    }

    public void setMask(boolean mask) {
        this.mask = mask;
    }

    public boolean getMask() {
        return this.mask;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return mask?""+value:"-";
    }

    /**
     * CAUTION!
     * Here, the default Oject.clone() function is overwritten as a copy constructor!
     * @return a copy of {@code this}
     */
    @Override
    public AnimationParameter clone() {
        return new AnimationParameter(this);
    }
}
