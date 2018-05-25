/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.signals;

import vib.core.util.time.TimeMarker;

/**
 * This interface defined a {@code Signal} that can use more than one stroke.
 * @author Andre-Marie Pez
 */
public interface MultiStrokeSignal extends Signal{

    /**
     * Returns the {@code TimeMarker} of a specific stroke.
     * @param index the index of the stroke
     * @return the {@code TimeMarker} of the requested stroke or {@code null} if there is no stroke at the specified index
     */
    public TimeMarker getStroke(int index);

    /**
     * Set a synchPoint reference to a specified stroke.<br/>
     * It is assumed that if no stroke existe at the index, it will be created.<br/>
     * It may use {@code [targetStroke].addReference(synchPoint)}.
     * @param index the index of the stroke
     * @param synchPoint the synchPoint reference of the stroke
     */
    public void setStroke(int index, String synchPoint);
}
