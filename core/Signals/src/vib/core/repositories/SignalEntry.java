/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.repositories;

import vib.core.signals.Signal;
import vib.core.util.parameter.Parameter;

/**
 *
 * @author Andre-Marie Pez
 */
public class SignalEntry <S extends Signal> implements Parameter<SignalEntry<S>>{

    private String name;
    private S signal;

    public SignalEntry(String name, S signal){
        this.name = name;
        this.signal = signal;
    }
    @Override
    public String getParamName() {
        return name;
    }

    @Override
    public void setParamName(String string) {
        name = string;
    }

    public S getSignal(){
        return signal;
    }

    public void setSignal(S signal){
        this.signal = signal;
    }

    @Override
    public boolean equals(SignalEntry<S> other) {
        return name.equalsIgnoreCase(other.name) && signal==other.signal;
    }
}
