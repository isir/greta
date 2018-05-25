/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class ResourceGroupManager extends _Object_ {

    public static ResourceGroupManager getSingleton() {
        return new ResourceGroupManager(_getSingleton());
    }
    private static native long _getSingleton();

    public static native String getDEFAULT_RESOURCE_GROUP_NAME();

    public ResourceGroupManager(long pointer) {
        super(pointer);
    }

    public boolean isResourceGroupInitialised(String resourceGroup) {
        return _isResourceGroupInitialised(getNativePointer(), resourceGroup);
    }
    private native boolean _isResourceGroupInitialised(long p, String s);

    public void addResourceLocation(String baseMaterialPath, String fileSystem, String resourceGroup, boolean b) {
        _addResourceLocation(getNativePointer(), baseMaterialPath, fileSystem, resourceGroup, b);
    }
    private native void _addResourceLocation(long p, String baseMaterialPath, String fileSystem, String resourceGroup, boolean b);

    public void initialiseAllResourceGroups() {
        _initialiseAllResourceGroups(getNativePointer());
    }
    private native void _initialiseAllResourceGroups(long p);

    public boolean resourceExists(String resourceGroup, String filename) {
        return _resourceExists(getNativePointer(), resourceGroup, filename);
    }
    private native boolean _resourceExists(long p, String s, String s2);
    
    @Override
    protected native void delete(long nativePointer);
    
}
