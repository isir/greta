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
package greta.core.animation.mpeg4.bap;

import greta.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Jing Huang
 */
public class BAP extends AnimationParameter {

    private static int radianFactor = 100000;

    public BAP(boolean mask, int value) {
        super(mask, value);
    }

    public BAP() {
        super();
    }

    public BAP(int value) {
        super(value);
    }

    public BAP(BAP bap) {
        super(bap);
    }

    @Override
    public BAP clone() {
        BAP bap = new BAP(this);
        return bap;
    }

    /**
     * convert from angle in radian and set BAP value
     */
    public void setRadianValue(double value) {
        applyValue((int) (value * radianFactor));
    }

    /**
     * convert from angle in degree and set BAP value
     */
    public void setDegreeValue(double value){
        setRadianValue(Math.toRadians(value));
    }

    /**
     * @return BAP value in radians
     */
    public double getRadianValue() {
        return ((double)getValue()) / radianFactor;
    }

    /**
     * @return BAP value in degrees
     */
    public double getDegreeValue() {
        return Math.toDegrees(getRadianValue());
    }
}
