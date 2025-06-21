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
package greta.core.animation.common.TimeFunctions;

import greta.core.animation.common.easingcurve.EquationFunctions;

/**
 *
 * @author Jing Huang
 */
public class OutBackTimeFunction implements TimeFunction{


    private double amplitude = 0.2f;
    private double overshoot =  0.20158f;
    @Override
    public double getTime(double original) {
        return EquationFunctions.easeOutBack(original, overshoot);
    }

}
