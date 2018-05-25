/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class RaySceneQuery extends _Object_ {

    public RaySceneQuery(long pointer) {
        super(pointer);
    }

    public void setSortByDistance(boolean b, int i) {
        _setSortByDistance(getNativePointer(), b, i);
    }
    private native void _setSortByDistance(long p, boolean b, int i);

    public RaySceneQueryResult execute() {
        return new RaySceneQueryResult(_execute(getNativePointer()));
    }
    private native long _execute(long p);
    
    @Override
    protected native void delete(long nativePointer);
    
}
