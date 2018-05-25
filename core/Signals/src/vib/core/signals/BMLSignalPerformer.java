/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.signals;

import vib.core.util.xml.XMLTree;

/**
 * This interface should be only used to send the BML to Cantoche agent.
 * Use (@code SignalPerformer) instead
 *
 * @author Radoslaw Niewiadomski
 * @deprecated useless interface
 */

public interface BMLSignalPerformer {

    /**
     *
     * @param bml
     * @param string
     * @deprecated useless interface
     */
    public void performeBMLSignals(XMLTree bml, String string);
}