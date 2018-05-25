/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class MaterialManager extends _Object_ {

    public static MaterialManager getSingleton() {
        return new MaterialManager(_getSingleton());
    }
    private static native long _getSingleton();

    public MaterialManager(long pointer) {
        super(pointer);
    }

    public Material getByName(String materialName) {
        return new Material(_getByName(getNativePointer(), materialName));
    }
    private native long _getByName(long p, String materialName);

    public boolean resourceExists(String materialName) {
        return _resourceExists(getNativePointer(), materialName);
    }
    private native boolean _resourceExists(long p, String materialName);
    
    @Override
    protected native void delete(long nativePointer);
    
}
