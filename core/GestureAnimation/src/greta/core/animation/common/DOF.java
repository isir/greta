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
