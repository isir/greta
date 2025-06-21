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
package greta.auxiliary.socialparameters;

import greta.core.util.animationparameters.AnimationParametersFrame;

/**
 *
 * @author Florian Pecune
 */
public class SocialParameterFrame extends AnimationParametersFrame<SocialParameter> {


    public SocialParameterFrame() {
        super(SocialDimension.values().length);
    }

    public SocialParameterFrame(int frameNum) {
        super(SocialDimension.values().length, frameNum);
    }

    public SocialParameterFrame(SocialParameterFrame spFrame) {
        super(spFrame);
    }

    @Override
    protected SocialParameter newAnimationParameter() {
        return new SocialParameter();
    }

    @Override
    public SocialParameter newAnimationParameter(boolean bln, int i) {
        return new SocialParameter(bln, i);
    }

    @Override
    protected SocialParameter copyAnimationParameter(SocialParameter ap) {
        return new SocialParameter(ap);
    }

    @Override
    public AnimationParametersFrame clone() {
        return new SocialParameterFrame(this);
    }

    public SocialParameter getAnimationParameter(SocialDimension which) {
        return getAnimationParameter(which.ordinal());
    }


    public void setDoubleValue(int which, double value){
        getAnimationParameter(which).setDoubleValue(value);
    }

    public double getDoubleValue(int which){
        return getAnimationParameter(which).getDoubleValue();
    }

    public void setDoubleValue(SocialDimension which, double value){
        setDoubleValue(which.ordinal(), value);
    }

    public double getDoubleValue(SocialDimension which){
        return getAnimationParameter(which).getDoubleValue();
    }

    public void setValue(SocialDimension which, int value) {
        setValue(which.ordinal(), value);
    }

    public void applyValue(SocialDimension which, int value) {
        applyValue(which.ordinal(), value);
    }

    public int getValue(SocialDimension which) {
        return getValue(which.ordinal());
    }

    public void setMask(SocialDimension which, boolean mask) {
        setMask(which.ordinal(), mask);
    }

    public boolean getMask(SocialDimension which) {
        return getMask(which.ordinal());
    }

    public void setMaskAndValue(SocialDimension which, boolean mask, int value) {
        super.setMaskAndValue(which.ordinal(), mask, value);
    }

    public void setAsInvalid(SocialDimension which){
        getAnimationParameter(which).setAsInvalid();
    }

    public boolean isInvalid(SocialDimension which){
        return getAnimationParameter(which).isInvalid();
    }

    public static SocialParameterFrame SubstractFrames (SocialParameterFrame sp1, SocialParameterFrame sp2) {
        SocialParameterFrame result = new SocialParameterFrame();
        result.setFrameNumber(sp1.getFrameNumber()-sp2.getFrameNumber());
        for (SocialDimension sd : SocialDimension.values()){
            result.applyValue(sd, sp1.getValue(sd)-sp2.getValue(sd));
        }
        return result;
    }
}
