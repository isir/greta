/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class Pass extends _Object_ {

    public Pass(long pointer) {
        super(pointer);
    }
    
    
    public boolean hasFragmentProgram() {
        return _hasFragmentProgram(getNativePointer());
    }
    private native boolean _hasFragmentProgram(long p);

    public GpuProgramParameters getFragmentProgramParameters() {
        return new GpuProgramParameters(_getFragmentProgramParameters(getNativePointer()));
    }
    private native long _getFragmentProgramParameters(long p);
    
    public void setAmbient(ColourValue colour) {
       _setAmbient(getNativePointer(), colour.getNativePointer());
    }
    private native void _setAmbient(long p, long colourp);
    
    @Override
    protected native void delete(long nativePointer);
}
