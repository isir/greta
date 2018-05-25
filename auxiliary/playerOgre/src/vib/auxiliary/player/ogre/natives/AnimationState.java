/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class AnimationState extends _Object_{

    public AnimationState(long pointer) {
        super(pointer);
    }
    
    public void setTimePosition(double i) {
        _setTimePosition(getNativePointer(), i);
    }
    private native void _setTimePosition(long thisPointer, double i);

    public void setEnabled(boolean b) {
        _setEnabled(getNativePointer(), b);
    }
    private native void _setEnabled(long thisPointer, boolean b);

    public void getParent_notifyDirty() {
        _getParent_notifyDirty(getNativePointer());
    }
    private native void _getParent_notifyDirty(long thisPointer);
    
    @Override
    protected native void delete(long nativePointer);
}
