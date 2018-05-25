/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class RaySceneQueryResultEntry extends _Object_{

    public RaySceneQueryResultEntry(long pointer) {
        super(pointer);
    }
    public double distance() {
        return _distance(getNativePointer());
    }
    private native double _distance(long p);
    
    @Override
    protected native void delete(long nativePointer);
}
