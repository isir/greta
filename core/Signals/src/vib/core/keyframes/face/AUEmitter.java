/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes.face;

/**
 *
 * @author Radoslaw Niewiadomski
 */

public interface AUEmitter {

    /**
     *
     * @param performer
     */
    public void addAUPerformer(AUPerformer performer);
    /**
     *
     * @param performer
     */
    public void removeAUPerformer(AUPerformer performer);
}
