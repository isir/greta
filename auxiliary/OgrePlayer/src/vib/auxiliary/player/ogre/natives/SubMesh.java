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
public class SubMesh extends _Object_ {

    public SubMesh(long pointer) {
        super(pointer);
    }

    public VertexData getvertexData() {
        return new VertexData(_getvertexData(getNativePointer()));
    }
    private native long _getvertexData(long p);

    public boolean getuseSharedVertices() {
        return _getuseSharedVertices(getNativePointer());
    }
    private native boolean _getuseSharedVertices(long p);

    @Override
    protected native void delete(long nativePointer);
}
