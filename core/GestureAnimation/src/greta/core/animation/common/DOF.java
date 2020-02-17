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
package greta.core.animation.common;

/**
 *
 * @author Jing Huang
 */
public class DOF {

    double _maxValue = 2 * (double) java.lang.Math.PI;
    double _minValue = -2 * (double) java.lang.Math.PI;

    static public enum DOFType {
        ROTATION_X,
        ROTATION_Y,
        ROTATION_Z,
        TRANSLATION_X,
        TRANSLATION_Y,
        TRANSLATION_Z
    }


    public DOF(double min, double max) {
        _maxValue = max;
        _minValue = min;
    }

    public double maxValue() {
        return _maxValue;
    }

    public void maxValue(double val) {
        _maxValue = val;
    }

    public double minValue() {
        return _minValue;
    }

    public void minValue(double val) {
        _minValue = val;
    }
}
