/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.signals;

import vib.core.util.Mode;
import vib.core.util.id.ID;
import java.util.List;

/**
 * This interface descibes an object that can receive and use a list of
 * {@code Signal}.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because they are
 * UmlGraph tags, not javadoc tags.
 * @inavassoc - "used by" * vib.core.signals.Signal
 */
public interface SignalPerformer {

    /**
     * This fuction receives a list of {@code Signal}.<br/> This function is
     * typically call by {@code SignalEmitters}.
     *
     * @param signals the list of {@code Signal}
     * @param requestId the identifier of the request
     * @param mode how the list is added to previous list : blend, replace,
     * append
     */
    public void performSignals(List<Signal> signals, ID requestId, Mode mode);
}
