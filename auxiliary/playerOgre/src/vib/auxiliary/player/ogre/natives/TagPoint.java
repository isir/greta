/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class TagPoint extends _Object_ {

    public TagPoint(long pointer) {
        super(pointer);
    }

    public void scale(double x, double y, double z) {
        _scale(getNativePointer(), x, y, z);
    }
    private native void _scale(long p, double x, double y, double z);

    public void setOrientation(Quaternion convert) {
        _setOrientation(getNativePointer(), convert.getNativePointer());
    }
    private native void _setOrientation(long p, long p2);
    
    @Override
    protected native void delete(long nativePointer);
    
}
