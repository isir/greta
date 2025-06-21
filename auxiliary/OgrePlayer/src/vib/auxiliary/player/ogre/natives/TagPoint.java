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
public class TagPoint extends _Object_ {

    public TagPoint(long pointer) {
        super(pointer);
    }

    public void scale(double x, double y, double z) {
        _scale(getNativePointer(), x, y, z);
    }
    private native void _scale(long p, double x, double y, double z);

    public void setOrientation(Quaternion convert) {
        _setOrientation(getNativePointer(), convert.getNativePointer());
    }
    private native void _setOrientation(long p, long p2);

    @Override
    protected native void delete(long nativePointer);

}
