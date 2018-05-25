/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class Texture extends _Object_ {

    public Texture(long pointer) {
        super(pointer);
    }

    public HardwarePixelBuffer getBuffer(int i, int i0) {
        return new HardwarePixelBuffer(_getBuffer(getNativePointer(), i, i0));
    }
    private native long _getBuffer(long p, int i, int i0);
    
    @Override
    protected native void delete(long nativePointer);
}
