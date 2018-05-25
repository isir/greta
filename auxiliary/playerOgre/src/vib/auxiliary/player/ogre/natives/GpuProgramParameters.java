/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class GpuProgramParameters extends _Object_ {

    public GpuProgramParameters(long pointer) {
        super(pointer);
    }

    public void setNamedConstant(String textureIndex, IntBuffer textureIndex0, int index, int i) {
        _setNamedConstant_int_star(getNativePointer(), textureIndex, textureIndex0.getNativePointer(), index, i);
    }
    private native void _setNamedConstant_int_star(long pointer, String s, long intarray, int index, int i);

    public void setNamedConstant(String textureValue, FloatBuffer textureValue0, int index, int i) {
        _setNamedConstant_float_star(getNativePointer(), textureValue, textureValue0.getNativePointer(), index, i);
    }
    private native void _setNamedConstant_float_star(long pointer, String s, long intarray, int index, int i);
    
    public void setNamedConstant(String textureValue, int i) {
        _setNamedConstant(getNativePointer(), textureValue, i);
    }
    private native void _setNamedConstant(long pointer, String s, int i);
    
    @Override
    protected native void delete(long nativePointer);
    
}
