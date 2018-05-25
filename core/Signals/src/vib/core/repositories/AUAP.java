/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.repositories;

import vib.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Ken Prepin
 */
public class AUAP extends AnimationParameter {

    public static final double FLOATING_FACTOR = 1000;

    public AUAP() {
        super();
    }

    public AUAP(int value) {
        super(value);
    }

    public AUAP(double value) {
        this((int) (value * FLOATING_FACTOR));
    }

    public AUAP(boolean mask, int value) {
        super(mask, value);
    }

    public AUAP(boolean mask, double value) {
        this(mask, (int) (value * FLOATING_FACTOR));
    }

    public AUAP(AUAP auap) {
        super(auap);
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

    public double getNormalizedValue() {
        return getValue() / FLOATING_FACTOR;
    }

    @Override
    public AUAP clone() {
        return new AUAP(this);
    }
}
