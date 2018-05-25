/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class NameValuePairList extends _Object_ {

    public NameValuePairList(long pointer) {
        super(pointer);
    }

    public NameValuePairList() {
        super(_instanciate());
    }
    private static native long _instanciate();

    public void insert(String externalWindowHandle, String windowHandlePointer) {
        _insert(getNativePointer(), externalWindowHandle, windowHandlePointer);
    }
    private native void _insert(long p, String s1, String s2);
    
    @Override
    protected native void delete(long nativePointer);
}
