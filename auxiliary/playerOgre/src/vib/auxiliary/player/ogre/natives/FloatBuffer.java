/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class FloatBuffer extends _Object_{
    
    private float[] javaBuffer;
    public FloatBuffer(int size) {
        super(_instanciate(size));
        javaBuffer = new float[size];
    }
    
    private static native long _instanciate(int size);
    
    public float[] getBuffer(){
        return javaBuffer;
    }
    
    public void updateJavaBuffer(){
        _updateJavaBuffer(getNativePointer(), javaBuffer, javaBuffer.length);
    }
    
    private static native void _updateJavaBuffer(long thispointer, float[] buffer, int size);
    
    public void setIndex(int index, float i) {
        _setIndex(getNativePointer(), index, i);
    }
    private native void _setIndex(long thispointer, int index, float value);
    
    @Override
    protected native void delete(long nativePointer);
}