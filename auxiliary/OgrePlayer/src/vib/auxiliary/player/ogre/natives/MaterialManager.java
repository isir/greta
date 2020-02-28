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
