/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public interface Node {

    public void _update(boolean updateChildren, boolean parentHasChanged);

    public Quaternion _getDerivedOrientation();
    
    public boolean isNull();
}
