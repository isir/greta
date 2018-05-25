/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.ssi;

import vib.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Angelo Cafaro
 */
public class SSI extends AnimationParameter {

    public static final double FLOATING_FACTOR = 1000;

    public SSI(SSI ssi) {
        super(ssi);
    }

    public SSI() {
        super();
    }

    public SSI(int value) {
        super(value);
    }

    public SSI(double value) {
        this((int) (value * FLOATING_FACTOR));
    }

    public SSI(boolean mask, int value) {
        super(mask, value);
    }

    public SSI(boolean mask, double value) {
        this(mask, (int) (value * FLOATING_FACTOR));
    }

    public void set(boolean mask, double value) {
        set(mask, (int) (value * FLOATING_FACTOR));
    }

    public void setValue(double value) {
        setValue((int) (value * FLOATING_FACTOR));
    }

    public void applyValue(double value) {
        applyValue((int) (value * FLOATING_FACTOR));
    }

    public void applyValue(int value) {
        super.applyValue(value);
    }

    public double getNormalizedValue() {
        return getValue() / FLOATING_FACTOR;
    }

    public SSI clone() {
        SSI ssi = new SSI(this);
        return ssi;
    }

}
