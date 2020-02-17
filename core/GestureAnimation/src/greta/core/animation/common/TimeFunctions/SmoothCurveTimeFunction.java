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
