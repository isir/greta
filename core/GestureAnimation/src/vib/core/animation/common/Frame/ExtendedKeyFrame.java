/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.Frame;

import vib.core.util.math.Function;

/**
 *
 * @author Andre-Marie Pez
 */
public class ExtendedKeyFrame extends KeyFrame{

    private Function function = new vib.core.util.math.easefunctions.Linear();

    public ExtendedKeyFrame(double time){
        super(time);
    }

    public void setFunction(Function function){
        this.function = function;
    }

    public Function getFunction(){
        return function;
    }
}
