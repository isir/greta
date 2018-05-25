/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.fap;

import vib.core.util.animationparameters.AnimationParametersFrame;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class FAPFrame extends AnimationParametersFrame<FAP> {

    public FAPFrame() {
        super(FAPType.NUMFAPS);
    }

    public FAPFrame(int frameNum) {
        super(FAPType.NUMFAPS, frameNum);
    }

    public FAPFrame(FAPFrame fapFrame) {
        super(fapFrame);
    }

    @Override
    public FAPFrame clone(){
        return new FAPFrame(this);
    }

    @Override
    protected FAP newAnimationParameter() {
        return new FAP();
    }

    @Override
    protected FAP copyAnimationParameter(FAP ap) {
        return new FAP(ap);
    }

    @Override
    public FAP newAnimationParameter(boolean mask, int value) {
        return new FAP(mask, value);
    }

    public void setValue(FAPType which, int value) {
        setValue(which.ordinal(), value);
    }

    public void applyValue(FAPType which, int value) {
        applyValue(which.ordinal(), value);
    }

    public int getValue(FAPType which) {
        return getValue(which.ordinal());
    }

    public void setMask(FAPType which, boolean mask) {
        setMask(which.ordinal(), mask);
    }

    public boolean getMask(FAPType which) {
        return getMask(which.ordinal());
    }

    public void setMaskAndValue(FAPType which, boolean mask, int value) {
        super.setMaskAndValue(which.ordinal(), mask, value);
    }

}
