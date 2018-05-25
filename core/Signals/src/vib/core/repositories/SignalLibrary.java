/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.repositories;

import vib.core.signals.Signal;
import vib.core.util.parameter.ParameterSet;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class SignalLibrary<S extends Signal> extends ParameterSet<SignalEntry<S>> {

    public SignalLibrary(String defaultDefinitionName) {
        super(defaultDefinitionName);
    }

    public S getSignal(String paramName){
        SignalEntry<S> entry = get(paramName);
        return entry == null ? null : entry.getSignal();
    }
}
