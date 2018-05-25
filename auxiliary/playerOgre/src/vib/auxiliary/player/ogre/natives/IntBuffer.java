/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class IntBuffer extends _Object_{
    
    private int[] javaBuffer;
    public IntBuffer(int size) {
        super(_instanciate(size));
        javaBuffer = new int[size];
    }
    
    private static native long _instanciate(int size);
    
    public int[] getBuffer(){
        return javaBuffer;
    }
    
    public void updateJavaBuffer(){
        _updateJavaBuffer(getNativePointer(), javaBuffer, javaBuffer.length);
    }
    
    private static native void _updateJavaBuffer(long thispointer, int[] buffer, int size);

    public void setIndex(int index, int i) {
        _setIndex(getNativePointer(), index, i);
    }
    
    private native void _setIndex(long thispointer, int index, int value);
    
    @Override
    protected native void delete(long nativePointer);
}
