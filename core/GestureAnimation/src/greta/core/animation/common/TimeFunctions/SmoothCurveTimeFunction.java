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

/**
 *
 * @author Jing Huang
 */
public class SmoothCurveTimeFunction implements TimeFunction {

    private double x_control = 0.5f;
    private double y_control = 0.99f;

    @Override
    public double getTime(double original) {
        if (x_control == 0.5 && y_control == 0.5) {
            return original;
        }
        double time_output = 0;
        if (y_control < -1 || y_control > 1 || x_control < -1 || x_control > 1) {
            System.out.println("SmoothCurveTimeFunction control point out of range");
        }

        //linear variation
        if (original < x_control) {
            time_output = original / x_control * y_control;
        } else {
            time_output = y_control + (1 - y_control) * (original - x_control) / (1 - x_control);
        }
        //smooth curve
//        time_output = 0 * 1 + time_output * 9 + y_control * 9 + 1 * 1;
//        time_output /= 20.0f;
        time_output *= time_output;
        return time_output;
    }
}
