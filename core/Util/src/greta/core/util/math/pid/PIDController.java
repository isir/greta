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
