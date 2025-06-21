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
