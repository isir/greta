/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class ByteBuffer extends _Object_{
    private byte[] javaBuffer;
    public ByteBuffer(int size) {
        super(_instanciate(size));
        javaBuffer = new byte[size];
    }
    
    private static native long _instanciate(int size);
    
    public byte[] getBuffer(){
        return javaBuffer;
    }
    
    public void updateJavaBuffer(){
        _updateJavaBuffer(getNativePointer(), javaBuffer, javaBuffer.length);
    }
    
    private static native void _updateJavaBuffer(long thispointer, byte[] buffer, int size);
    
    public void setIndex(int index, byte i) {
        _setIndex(getNativePointer(), index, i);
    }
    private native void _setIndex(long thispointer, int index, byte value);
    
    @Override
    protected native void delete(long nativePointer);
}
