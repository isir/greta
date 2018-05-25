/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.signals;


/**
 * This interface describes an object that can send a list of {@code Signal} to all {@code SignalPerformer} added.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - * vib.core.signals.SignalPerformer
 * @navassoc - "emmits" * vib.core.signals.Signal
 */
public interface SignalEmitter {

    /**
     * Adds a {@code SignalPerformer}.<br/>
     * The function {@code performSignals} of all {@code SignalPerformer}
     * added will be called when this emmits a list of {@code Signal}.
     * @param performer the {@code SignalPerformer} to add
     * @see vib.core.signals.SignalPerformer#performSignals(java.util.List, vib.core.util.id.ID, vib.core.util.Mode) performeSignals
     */
    public void addSignalPerformer(SignalPerformer performer);

    /**
     * Removes the first occurence of a {@code SignalPerformer}.
     * @param performer the {@code SignalPerformer} to remove
     */
    public void removeSignalPerformer(SignalPerformer performer);
}
