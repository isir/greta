/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class SubEntity extends _Object_ {

    public SubEntity(long pointer) {
        super(pointer);
    }

    public void setVisible(boolean visible) {
        _setVisible(getNativePointer(), visible);
    }
    private native void _setVisible(long p, boolean b);

    public void setMaterialName(String materialName) {
        _setMaterialName(getNativePointer(), materialName);
    }
    private native void _setMaterialName(long p, String s);
    
    @Override
    protected native void delete(long nativePointer);
}
