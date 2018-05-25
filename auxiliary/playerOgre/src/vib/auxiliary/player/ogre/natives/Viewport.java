/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */


public class Viewport extends _Object_{

    public Viewport(long pointer) {
        super(pointer);
    }

    public void setBackgroundColour(ColourValue convertToColor) {
        _setBackgroundColour(getNativePointer(), convertToColor.getNativePointer());
    }
    private native void _setBackgroundColour(long thiPointer, long colourPointer);
    
    @Override
    protected native void delete(long nativePointer);
}
