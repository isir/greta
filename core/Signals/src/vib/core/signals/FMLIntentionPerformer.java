/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.signals;

import vib.core.util.xml.XMLTree;

/**
 * This interface should be only used to send the BML to Cantoche agent.
 * Use (@code SignalEmitter) instead
 *
 * @author Radoslaw Niewiadomski
 * @deprecated useless interface
 */


public interface FMLIntentionPerformer {


    /**
     * @param fml
     * @param requestId
     * @deprecated useless interface
     */
    public void performeIntentions(XMLTree fml, String requestId);
}
