/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class Mesh extends _Object_ {

    public Mesh(long pointer) {
        super(pointer);
    }

    public int getPoseCount() {
        return _getPoseCount(getNativePointer());
    }
    private native int _getPoseCount(long nativePointer);

    public Pose getPose(int index){
        return new Pose(_getPose(getNativePointer(), index));
    }
    private native long _getPose(long nativePointer, int index);

    public boolean hasAnimation(String AnimationName) {
        return _hasAnimation(getNativePointer(), AnimationName);
    }
    private native boolean _hasAnimation(long nativePointer, String AnimationName);

    public Animation createAnimation(String AnimationName, int i) {
        return new Animation(_createAnimation(getNativePointer(), AnimationName, i));
    }
    private native long _createAnimation(long nativePointer, String AnimationName, int i);

    public String getName() {
        return _getName(getNativePointer());
    }
    private native String _getName(long nativePointer);

    public Animation getAnimation(String AnimationName) {
        return new Animation(_getAnimation(getNativePointer(), AnimationName));
    }
    private native long _getAnimation(long nativePointer, String AnimationName);

    public VertexData getsharedVertexData() {
        return new VertexData(_getsharedVertexData(getNativePointer()));
    }
    private native long _getsharedVertexData(long nativePointer);

    public SubMesh getSubMesh(int i) {
        return new SubMesh(_getSubMesh(getNativePointer(), i));
    }
    private native long _getSubMesh(long nativePointer, int index);

    
    @Override
    protected native void delete(long nativePointer);
    
}
