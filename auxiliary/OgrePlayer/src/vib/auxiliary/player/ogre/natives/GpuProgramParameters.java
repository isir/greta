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
public class GpuProgramParameters extends _Object_ {

    public GpuProgramParameters(long pointer) {
        super(pointer);
    }

    public void setNamedConstant(String textureIndex, IntBuffer textureIndex0, int index, int i) {
        _setNamedConstant_int_star(getNativePointer(), textureIndex, textureIndex0.getNativePointer(), index, i);
    }
    private native void _setNamedConstant_int_star(long pointer, String s, long intarray, int index, int i);

    public void setNamedConstant(String textureValue, FloatBuffer textureValue0, int index, int i) {
        _setNamedConstant_float_star(getNativePointer(), textureValue, textureValue0.getNativePointer(), index, i);
    }
    private native void _setNamedConstant_float_star(long pointer, String s, long intarray, int index, int i);

    public void setNamedConstant(String textureValue, int i) {
        _setNamedConstant(getNativePointer(), textureValue, i);
    }
    private native void _setNamedConstant(long pointer, String s, int i);

    @Override
    protected native void delete(long nativePointer);

}
