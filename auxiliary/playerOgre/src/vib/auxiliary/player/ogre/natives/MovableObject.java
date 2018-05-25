/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public interface MovableObject {

    long getNativePointer();
    public void detatchFromParent();
    
}
