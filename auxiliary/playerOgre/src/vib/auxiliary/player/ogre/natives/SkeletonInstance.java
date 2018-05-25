/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class SkeletonInstance extends _Object_ {

    public SkeletonInstance(long pointer) {
        super(pointer);
    }

    public boolean hasBone(String leftEye) {
        return _hasBone(getNativePointer(), leftEye);
    }
    private native boolean _hasBone(long p, String name);

    public Bone getBone(String leftEye) {
        return new Bone(_getBone(getNativePointer(), leftEye));
    }
    private native long _getBone(long p, String name);

    public Bone getRootBone() {
        return new Bone(_getRootBone(getNativePointer()));
    }
    private native long _getRootBone(long p);

    public int getNumBones() {
        return _getNumBones(getNativePointer());
    }
    private native int _getNumBones(long p);

    public Bone getBone(int i) {
        return new Bone(_getBone(getNativePointer(), i));
    }
    private native long _getBone(long p, int index);
    
    @Override
    protected native void delete(long nativePointer);
}
