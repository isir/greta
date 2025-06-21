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
package greta.core.util.animationparameters;

/**
 *
 * @author Ken Prepin
 */
public class AnimationParameter {

    int value;
    boolean mask;

    public AnimationParameter(boolean mask, int value) {
        this.mask = mask;
        this.value = value;
    }

    public AnimationParameter() {
        this(false, 0);
    }

    public AnimationParameter(AnimationParameter ap) {
        this(ap.mask, ap.value);
    }

    public AnimationParameter(int value) {
        this(true, value);
    }

    public void set(boolean mask, int value){
        this.value = value;
        this.mask = mask;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void applyValue(int value) {
        this.value = value;
        this.mask = true;
    }

    public void setMask(boolean mask) {
        this.mask = mask;
    }

    public boolean getMask() {
        return this.mask;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return mask?""+value:"-";
    }

    /**
     * CAUTION!
     * Here, the default Oject.clone() function is overwritten as a copy constructor!
     * @return a copy of {@code this}
     */
    @Override
    public AnimationParameter clone() {
        return new AnimationParameter(this);
    }
}
