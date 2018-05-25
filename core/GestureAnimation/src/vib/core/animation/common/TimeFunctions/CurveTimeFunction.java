/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.TimeFunctions;

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
