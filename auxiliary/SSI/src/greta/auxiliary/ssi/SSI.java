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
