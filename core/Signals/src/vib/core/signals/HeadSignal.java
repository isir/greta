/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.signals;

/**
 * This class contains informations about head Signals.
 *
 * @author Andre-Marie Pez
 * @author Brice Donval
 */
public class HeadSignal extends SpineSignal {

    /**
     * Construct a new {@code HeadSignal}.
     *
     * @param id The identifier of this {@code HeadSignal}.
     */
    public HeadSignal(String id) {
        super(id);
    }

    @Override
    public String getModality() {
        return "head";
    }

}
