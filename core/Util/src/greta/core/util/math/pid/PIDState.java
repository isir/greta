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
package greta.core.util.math.pid;

/**
 *
 * @author Jing Huang
 */
public class PIDState {

    double _iGain, // integral gain
            _pGain, // proportional gain
            _dGain;     // derivative gain
    double _state;
    double _iStateError;  // Integrator Error state
    double _iMin, _iMax;

    public PIDState() {
        _pGain = 0.1;
        _iGain = 0.05;
        _dGain = 0.05;
    }

    public double getMinIntegralErrorState() {
        return _iMin;
    }

    public double getMaxIntegralErrorState() {
        return _iMax;
    }

    public void setMinIntegralErrorState(double min) {
        _iMin = min;
    }

    public void setMaxIntegralErrorState(double max) {
        _iMax = max;
    }

    public void setProportionalGain(double pGain) {
        _pGain = pGain;
    }

    public double getProportionalGain() {
        return _pGain;
    }

    public void setIntegralGain(double iGain) {
        _iGain = iGain;
    }

    public double getIntegralGain() {
        return _iGain;
    }

    public void setDerivativeGain(double dGain) {
        _dGain = dGain;
    }

    public double getDerivativeGain() {
        return _dGain;
    }

    public double getIntegralErrorState() {
        return _iStateError;
    }

    public void updateIntegralErrorState(double currentError) {
        _iStateError = _iStateError + currentError;
    }

    public double getState() {
        return _state;
    }

    public void updateState(double state) {
        _state = state;
    }
}
