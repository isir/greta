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
public class SkeletonInstance extends _Object_ {

    public SkeletonInstance(long pointer) {
        super(pointer);
    }

    public boolean hasBone(String leftEye) {
        return _hasBone(getNativePointer(), leftEye);
    }
    private native boolean _hasBone(long p, String name);

    public Bone getBone(String leftEye) {
        return new Bone(_getBone(getNativePointer(), leftEye));
    }
    private native long _getBone(long p, String name);

    public Bone getRootBone() {
        return new Bone(_getRootBone(getNativePointer()));
    }
    private native long _getRootBone(long p);

    public int getNumBones() {
        return _getNumBones(getNativePointer());
    }
    private native int _getNumBones(long p);

    public Bone getBone(int i) {
        return new Bone(_getBone(getNativePointer(), i));
    }
    private native long _getBone(long p, int index);

    @Override
    protected native void delete(long nativePointer);
}
