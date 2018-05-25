/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class HardwarePixelBuffer extends _Object_ {

    public HardwarePixelBuffer(long pointer) {
        super(pointer);
    }

    public RenderTexture getRenderTarget(int i) {
        return new RenderTexture(_getRenderTarget(getNativePointer(), i));
    }
    private native long _getRenderTarget(long p, int i);

    public void blitToMemory(PixelBox pib) {
        _blitToMemory(getNativePointer(), pib.getNativePointer());
    }
    private native void _blitToMemory(long p, long i);
    
    @Override
    protected native void delete(long nativePointer);
    
}
