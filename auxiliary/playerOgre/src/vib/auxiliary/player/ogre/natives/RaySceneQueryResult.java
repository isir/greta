/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class RaySceneQueryResult extends _Object_ {

    public RaySceneQueryResult(long pointer) {
        super(pointer);
    }

    public int size() {
        return _size(getNativePointer());
    }
    private native int _size(long p);

    public RaySceneQueryResultEntry at(int i) {
        return new RaySceneQueryResultEntry(_at(getNativePointer(), i));
    }
    private native long _at(long p, int i);
    
    @Override
    protected native void delete(long nativePointer);
    
}
