/*
 * This file is part of Greta.
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
