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
