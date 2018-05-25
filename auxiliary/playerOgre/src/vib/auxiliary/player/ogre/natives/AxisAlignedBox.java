/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class AxisAlignedBox extends _Object_ {

    public AxisAlignedBox(long pointer) {
        super(pointer);
    }

    public Vector3 getCenter() {
        Vector3 center = new Vector3(_getCenter(getNativePointer()));
        center.gcMustDeleteThat(true);
        return center;
    }
    private native long _getCenter(long thisPointer);

    public Vector3 getMinimum() {
        return new Vector3(_getMinimum(getNativePointer()));
    }
    private native long _getMinimum(long thisPointer);

    public Vector3 getMaximum() {
        return new Vector3(_getMaximum(getNativePointer()));
    }
    private native long _getMaximum(long thisPointer);

    public void setInfinite() {
        _setInfinite(getNativePointer());
    }
    private native void _setInfinite(long thisPointer);
    
    @Override
    protected native void delete(long nativePointer);
}
