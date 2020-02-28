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
public class VertexAnimationTrack extends _Object_{

    public VertexAnimationTrack(long pointer) {
        super(pointer);
    }

    public VertexPoseKeyFrame createVertexPoseKeyFrame(float timePos) {
        return new VertexPoseKeyFrame(_createVertexPoseKeyFrame(getNativePointer(), timePos));
    }

    private native long _createVertexPoseKeyFrame(long thisPointer, float timePos);

    @Override
    protected native void delete(long nativePointer);
}
