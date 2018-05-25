/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.signals;


/**
 * This interface should be only used to send the BML to Cantoche agent.
 * Use (@code SignalEmitter) instead
 *
 * @author Radoslaw Niewiadomski
 * @deprecated useless interface
 */

public interface FMLIntentionEmitter {

    /**
     * @param performer
     * @deprecated useless interface
     */
    public void add(FMLIntentionPerformer performer);


}
