/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class Material extends _Object_ {

    public Material(long pointer) {
        super(pointer);
    }
    
    public Technique getTechnique(int index){
        return new Technique(_getTechnique(getNativePointer(), index));
    }
    private native long _getTechnique(long p, int index);
    

    public Material clone(String newMaterialName, boolean b, String string) {
        return new Material(_clone(getNativePointer(), newMaterialName, b, string));
    }
    private static native long _clone(long p, String newMaterialName, boolean b, String string);
    
    @Override
    protected native void delete(long nativePointer);

}
