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
public class CurveTimeFunction implements TimeFunction{

    private double _power = 0.0f;

    public double getPower() {
        return _power;
    }

    public void setPower(double power) {
        this._power = power;
    }

    @Override
    public double getTime(double original) {
        if(_power == 0) return original;
        double time_output = 0;
        if(_power < -1 || _power > 1)
            System.out.println("CurveTimeFunction expressivity power out of range");

        double x_control = (double) (-0.5f * _power + 0.5f);
        double y_control = 1 - x_control;

        //linear variation
        if(original < x_control){
            time_output = original / x_control * y_control;
        }else{
            time_output = y_control + (1 - y_control) * (original - x_control) / (1 - x_control);
        }
        //smooth curve
        time_output = 0 * 1 +  time_output * 9 + y_control * 9 + 1 * 1;
        time_output /= 20.0f;
        time_output *= time_output;
        return time_output;
    }
}
