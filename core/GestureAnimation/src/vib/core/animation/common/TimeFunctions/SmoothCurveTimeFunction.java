/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.TimeFunctions;

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
