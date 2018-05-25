/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.fap;

import vib.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class FAP extends AnimationParameter {

    public FAP(FAP fap) {
        super(fap);
    }

    public FAP() {
        super();
    }

    public FAP(boolean mask, int value) {
        super(mask, value);
    }

    @Override
    public FAP clone() {
        FAP fap = new FAP(this);
        return fap;
    }
}
