/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.util.math.pid;

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
