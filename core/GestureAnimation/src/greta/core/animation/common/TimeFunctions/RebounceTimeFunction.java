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

import greta.core.animation.common.easingcurve.EquationFunctions;

/**
 *
 * @author Jing Huang
 */
public class RebounceTimeFunction implements TimeFunction {

    private double jumpTime = 0.80f;
    //private double rebounceTime = 0.95f;
    private double rebounceDuration = 0.08f;
    private double amplitude = 0.2f;
    private boolean _smooth = false;

    public boolean isSmooth() {
        return _smooth;
    }

    public void setSmooth(boolean smooth) {
        this._smooth = smooth;
    }

    public double getJumpTime() {
        return jumpTime;
    }

    public void setJumpTime(double jumpTime) {
        this.jumpTime = jumpTime;
    }

    public double getRebounceDuration() {
        return rebounceDuration;
    }

    public void setRebounceDuration(double rebounceTime) {
        this.rebounceDuration = rebounceTime;
    }

    public double getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    /**
     * smooth is not well defined yet
     * @param original
     * @return time of rebounce
     */
    @Override
    public double getTime(double original) {
        //System.out.println("time:" + original);
        return EquationFunctions.easeOutBounce(original, amplitude);

        /*
        double time_output = 0;
        if (original <= jumpTime) {
            time_output = original / jumpTime;
            if (_smooth) {
                time_output = 0 * 1 + time_output * 9 + 1 * 9 + (1 - amplitude) * 1;
                time_output /= 20.0f;
            }
            time_output *= time_output;
            return time_output;
        } else if (original > jumpTime && original <= jumpTime + rebounceDuration * 0.5f) {
            time_output = 1 - (original - jumpTime) / (rebounceDuration * 0.5f) * amplitude;
            if (_smooth) {
                time_output = 1 * 1 + time_output * 9 + (1 - amplitude) * 9 + 1 * 1;
                time_output /= 20.0f;
            }
            time_output *= time_output;
            return time_output;
        } else if (original > jumpTime + rebounceDuration * 0.5f && original <= jumpTime + rebounceDuration) {
            time_output = 1 - amplitude + (original - jumpTime - rebounceDuration * 0.5f) / (1 - jumpTime - rebounceDuration * 0.5f) * amplitude;
            if (_smooth) {
                time_output = (1 - amplitude) * 1 + time_output * 9 + 1 * 9 + 1 * 1;
                time_output /= 20.0f;
            }
            time_output *= time_output;
            return time_output;
        } else {
            return 1;
        }*/
    }
}
