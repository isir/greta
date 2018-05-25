/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.utilx.gui;

import vib.core.util.IniManager;

/**
 *
 * @author Andre-Marie Pez
 */
public class GlobalIniManagerFrame extends IniManagerFrame {

    public GlobalIniManagerFrame() {
        setIniManager(IniManager.getGlobals());
        connect(new IniFileLoader());
    }
}
