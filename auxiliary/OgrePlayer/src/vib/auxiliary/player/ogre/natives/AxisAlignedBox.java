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
public class AxisAlignedBox extends _Object_ {

    public AxisAlignedBox(long pointer) {
        super(pointer);
    }

    public Vector3 getCenter() {
        Vector3 center = new Vector3(_getCenter(getNativePointer()));
        center.gcMustDeleteThat(true);
        return center;
    }
    private native long _getCenter(long thisPointer);

    public Vector3 getMinimum() {
        return new Vector3(_getMinimum(getNativePointer()));
    }
    private native long _getMinimum(long thisPointer);

    public Vector3 getMaximum() {
        return new Vector3(_getMaximum(getNativePointer()));
    }
    private native long _getMaximum(long thisPointer);

    public void setInfinite() {
        _setInfinite(getNativePointer());
    }
    private native void _setInfinite(long thisPointer);

    @Override
    protected native void delete(long nativePointer);
}
