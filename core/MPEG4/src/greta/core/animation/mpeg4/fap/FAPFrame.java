/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.animation.mpeg4.fap;

import greta.core.util.animationparameters.AnimationParametersFrame;

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
