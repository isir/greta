/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.fap;

import vib.core.util.animationparameters.AnimationParametersFrameParser;

/**
 *
 * @author Andre-Marie Pez
 */
public class FAPParser extends AnimationParametersFrameParser<FAPFrame> {

    @Override
    protected FAPFrame instanciateFrame() {
        return new FAPFrame();
    }

}
