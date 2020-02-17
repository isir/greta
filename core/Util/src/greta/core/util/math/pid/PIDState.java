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
