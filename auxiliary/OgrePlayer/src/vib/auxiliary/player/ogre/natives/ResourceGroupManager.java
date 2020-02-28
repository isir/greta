/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
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

    public boolean resourceExists(String resourceGroup, String fileName) {
        return _resourceExists(getNativePointer(), resourceGroup, fileName);
    }
    private native boolean _resourceExists(long p, String s, String s2);

    @Override
    protected native void delete(long nativePointer);

}
