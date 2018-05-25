/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;


/**
 *
 * @author André-Marie
 */


public class PixelBox extends _Object_{

    private ByteBuffer javaBuffer;
    
    public PixelBox(int captureWidth, int captureHeight) {
        super(0);
        javaBuffer = new ByteBuffer(captureWidth * captureHeight * 3);
        setNativePointer(_instanciate(captureWidth, captureHeight, javaBuffer.getNativePointer()));
        javaBuffer.gcMustDeleteThat(true);
    }
    
//    new PixelBox(captureWidth, captureHeight, 1, PixelFormat.PF_BYTE_RGB, new VoidPointer(buff.getInstancePointer()));
    private static native long _instanciate(int captureWidth, int captureHeight, long buffp);

    public byte[] getByteBuffer() {
        javaBuffer.updateJavaBuffer();
        return javaBuffer.getBuffer();
    }
    
    @Override
    protected native void delete(long nativePointer);
}
