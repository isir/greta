/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
