/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.utilx.gui;

import javax.swing.JPanel;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class IniLoader extends JPanel{
    protected IniManagerFrame managerFrame;

    protected void setManagerFrame(IniManagerFrame imf){
        managerFrame = imf;
    }

    public abstract void fireIniChanged();

    public abstract void fireIniDefinitionChanged(String name);

}
