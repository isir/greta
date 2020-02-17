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
