/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class _MovableObject extends _Object_ implements MovableObject{

    public _MovableObject(long pointer) {
        super(pointer);
    }

    @Override
    public void detatchFromParent() {
        _detatchFromParent(getNativePointer());
    }
    private native void _detatchFromParent(long thisPointer);
    
    @Override
    protected native void delete(long nativePointer);
}
