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
