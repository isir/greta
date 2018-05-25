/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.natives;

/**
 *
 * @author André-Marie
 */
public class MeshManager {

    public static Mesh load(String meshFileName, String resourceGroup) {
        return new Mesh(_load(meshFileName, resourceGroup));
    }
    
//        MeshManager.getSingleton().load(meshFileName, resourceGroup, Usage.HBU_STATIC_WRITE_ONLY, Usage.HBU_STATIC_WRITE_ONLY, true, true);
    private static native long _load(String meshFileName, String resourceGroup);

    public static native float getBoundsPaddingFactor();
    
}
