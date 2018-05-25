/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class Entity extends _Object_ implements MovableObject {

    public Entity(long pointer) {
        super(pointer);
    }

    public void setVisible(boolean visible) {
        _setVisible(getNativePointer(), visible);
    }
    private native void _setVisible(long thisPointer, boolean b);

    public void setMaterialName(String materialName) {
        _setMaterialName(getNativePointer(), materialName);
    }
    private native void _setMaterialName(long thisPointer, String b);

    public int getNumSubEntities() {
        return _getNumSubEntities(getNativePointer());
    }
    private native int _getNumSubEntities(long thisPointer);

    public SceneManager _getManager() {
        return new SceneManager(__getManager(getNativePointer()));
    }
    private native long __getManager(long thisPointer);

    public SkeletonInstance getSkeleton() {
        return new SkeletonInstance(_SkeletonInstance(getNativePointer()));
    }
    private native long _SkeletonInstance(long thisPointer);

    public Mesh getMesh() {
        return new Mesh(_getMesh(getNativePointer()));
    }
    private native long _getMesh(long thisPointer);

    public void setCastShadows(boolean b) {
        _setCastShadows(getNativePointer(), b);
    }
    private native void _setCastShadows(long thisPointer, boolean b);

    public String getName() {
        return _getName(getNativePointer());
    }
    private native String _getName(long thisPointer);

    @Override
    public void detatchFromParent() {
        _detatchFromParent(getNativePointer());
    }
    private native void _detatchFromParent(long thisPointer);

    public TagPoint attachObjectToBone(String name, MovableObject bone) {
        return new TagPoint(_attachObjectToBone(getNativePointer(), name, bone.getNativePointer()));
    }
    
    private native long _attachObjectToBone(long thisPointer, String name, long boneP);

    public SubEntity getSubEntity(int i) {
        return new SubEntity(_getSubEntity(getNativePointer(), i));
    }
    private native long _getSubEntity(long thisPointer, int i);

    public AxisAlignedBox getWorldBoundingBox(boolean b) {
        return new AxisAlignedBox(_getWorldBoundingBox(getNativePointer(), b));
    }
    private native long _getWorldBoundingBox(long thisPointer, boolean i);

    public AnimationState getAnimationState(String fapAnimationName) {
        return new AnimationState(_getAnimationState(getNativePointer(), fapAnimationName));
    }
    private native long _getAnimationState(long thisPointer, String i);
    
    @Override
    protected native void delete(long nativePointer);
    
}
