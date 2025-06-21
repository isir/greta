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
package greta.core.repositories;

import greta.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Ken Prepin
 */
public class AUAP extends AnimationParameter {

    public static final double FLOATING_FACTOR = 1000;

    public AUAP() {
        super();
    }

    public AUAP(int value) {
        super(value);
    }

    public AUAP(double value) {
        this((int) (value * FLOATING_FACTOR));
    }

    public AUAP(boolean mask, int value) {
        super(mask, value);
    }

    public AUAP(boolean mask, double value) {
        this(mask, (int) (value * FLOATING_FACTOR));
    }

    public AUAP(AUAP auap) {
        super(auap);
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

    public double getNormalizedValue() {
        return getValue() / FLOATING_FACTOR;
    }

    @Override
    public AUAP clone() {
        return new AUAP(this);
    }
}
