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
package greta.auxiliary.ssi;

import greta.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Angelo Cafaro
 */
public class SSI extends AnimationParameter {

    public static final double FLOATING_FACTOR = 1000;

    public SSI(SSI ssi) {
        super(ssi);
    }

    public SSI() {
        super();
    }

    public SSI(int value) {
        super(value);
    }

    public SSI(double value) {
        this((int) (value * FLOATING_FACTOR));
    }

    public SSI(boolean mask, int value) {
        super(mask, value);
    }

    public SSI(boolean mask, double value) {
        this(mask, (int) (value * FLOATING_FACTOR));
    }

    public void set(boolean mask, double value) {
        set(mask, (int) (value * FLOATING_FACTOR));
    }

    public void setValue(double value) {
        setValue((int) (value * FLOATING_FACTOR));
    }

    public void applyValue(double value) {
        applyValue((int) (value * FLOATING_FACTOR));
    }

    public void applyValue(int value) {
        super.applyValue(value);
    }

    public double getNormalizedValue() {
        return getValue() / FLOATING_FACTOR;
    }

    public SSI clone() {
        SSI ssi = new SSI(this);
        return ssi;
    }

}
