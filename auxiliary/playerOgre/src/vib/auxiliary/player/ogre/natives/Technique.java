/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class Technique extends _Object_ {

    public Technique(long pointer) {
        super(pointer);
    }
    
    public Pass getPass(int index){
        return new Pass(_getPass(getNativePointer(), index));
    }
    private native long _getPass(long p, int index);
    
    @Override
    protected native void delete(long nativePointer);
}
