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
