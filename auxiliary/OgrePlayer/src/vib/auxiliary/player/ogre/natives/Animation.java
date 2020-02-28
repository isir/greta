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
public class Animation extends _Object_{

    public Animation(long pointer) {
        super(pointer);
    }

    public VertexAnimationTrack createVertexTrack_VAT_POSE(int targetSubmesh) {
        return new VertexAnimationTrack(_createVertexTrack_VAT_POSE(getNativePointer(), targetSubmesh));
    }

    private native long _createVertexTrack_VAT_POSE(long thisPointer, int targetSubmesh);

    public VertexAnimationTrack getVertexTrack(int targetSubmesh) {
        return new VertexAnimationTrack(_getVertexTrack(getNativePointer(), targetSubmesh));
    }

    private native long _getVertexTrack(long thisPointer, int targetSubmesh);

    @Override
    protected native void delete(long nativePointer);

}
