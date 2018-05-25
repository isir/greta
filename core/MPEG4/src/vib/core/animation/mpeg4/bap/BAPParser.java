/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.bap;

import vib.core.util.animationparameters.AnimationParametersFrameParser;

/**
 *
 * @author Andre-Marie Pez
 */
public class BAPParser extends AnimationParametersFrameParser<BAPFrame> {

    @Override
    protected BAPFrame instanciateFrame() {
        return new BAPFrame();
    }
    
}
