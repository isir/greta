/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class Pose extends _Object_ {

    public Pose(long pointer) {
        super(pointer);
    }
    
    public String getName(){
        return _getName(getNativePointer());
    }

    private native String _getName(long nativePointer);
    
    @Override
    protected native void delete(long nativePointer);
    
}
