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
