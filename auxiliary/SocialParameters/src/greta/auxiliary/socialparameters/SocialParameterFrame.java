/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
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
