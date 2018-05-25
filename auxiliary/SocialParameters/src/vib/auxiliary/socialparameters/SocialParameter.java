/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.socialparameters;

import vib.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Florian Pecune
 */
public class SocialParameter extends AnimationParameter {

    public static final double FLOAT_PRECISION = 1000000;
    private static final double INVALID_VALUE = 2;


    public SocialParameter(){
        super();
    }

    public SocialParameter(boolean mask, int value){
        super(mask, value);
    }

    public SocialParameter(SocialParameter sp){
        super(sp);
    }

    public double getDoubleValue() {
        return getValue()/FLOAT_PRECISION;
    }

    public void setDoubleValue (double value) {
        applyValue((int)(value*FLOAT_PRECISION));
    }

    public void setAsInvalid(){
        setDoubleValue(INVALID_VALUE);//or any value outside [-1, 1]
    }

    public boolean isInvalid(){
        return getValue()>FLOAT_PRECISION || getValue()<-FLOAT_PRECISION;
    }
}
