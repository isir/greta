/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class ColourValue extends _Object_ {

    public ColourValue(long pointer){
        super(pointer);
    }
    public ColourValue(double r, double g, double b, double alpha) {
        super(_instanciate(r, g, b, alpha));
        this.gcMustDeleteThat(true);
    }
    
    private static native long _instanciate(double r, double g, double b, double alpha);
    
    @Override
    protected native void delete(long nativePointer);
    
}
