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
public class PIDController {

    public PIDController() {
    }

    public double updatePID(PIDState state, double currentObservation, double desire) {
        double error = desire - currentObservation;
        double pTerm = updateProportional(state.getProportionalGain(), error);
        double iTerm = updateIntegral(state.getIntegralGain(), state.getIntegralErrorState(), error, state.getMinIntegralErrorState(), state.getMaxIntegralErrorState());
        double dTerm = updateDerivative(state.getDerivativeGain(), state.getState(), currentObservation);
        double finalo = pTerm + iTerm - dTerm;  // dTerm need to close to Zero, so if +, then next time need to -
        state.updateState(currentObservation);
        return finalo;
    }

    /*
     * @finalo is the variation, not the final state
     */
    public double updatePD(PIDState state, double currentObservation, double desire) {
        double error = desire - currentObservation;
        double pTerm = updateProportional(state.getProportionalGain(), error);
        double dTerm = updateDerivative(state.getDerivativeGain(), state.getState(), currentObservation);
        double finalo = pTerm - dTerm;  // dTerm need to close to Zero, so if +, then next time need to -
        state.updateState(currentObservation);
        return finalo;
    }

    public double updateProportional(double pGain, double error) {
        double pTerm = error * pGain;
        return pTerm;
    }

    public double updateIntegral(double iGain, double iStateError, double error, double iStateMin, double iStateMax) {
        iStateError += error;
        if (iStateError > iStateMax) {
            iStateError = iStateMax;
        } else if (iStateError < iStateMin) {
            iStateError = iStateMin;
        }
        double iTerm = iGain * iStateError;
        return iTerm;
    }

    public double updateDerivative(double dGain, double lastObservation, double currentObservation) {
        double dTerm = dGain * (currentObservation - lastObservation);
        return dTerm;
    }
}
