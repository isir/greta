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
public class Bone extends _Object_ implements Node{

    public Bone(long pointer) {
        super(pointer);
    }

    @Override
    public void _update(boolean updateChildren, boolean parentHasChanged) {
        __update(getNativePointer(), updateChildren, parentHasChanged);
    }
    private native void __update(long thisPointer, boolean updateChildren, boolean parentHasChanged);

    public Vector3 getPosition() {
        return new Vector3(_getPosition(getNativePointer()));
    }
    private native long _getPosition(long thisPointer);

    public Quaternion _getDerivedOrientation() {
        return new Quaternion(__getDerivedOrientation(getNativePointer()));
    }
    private native long __getDerivedOrientation(long thisPointer);

    public Quaternion getOrientation() {
        return new Quaternion(_getOrientation(getNativePointer()));
    }
    private native long _getOrientation(long thisPointer);

    public void setOrientation(Quaternion convert) {
        _setOrientation(getNativePointer(), convert.getNativePointer());
    }
    private native void _setOrientation(long thisPointer, long quaternionPointer);

    public void setOrientation(greta.core.util.math.Quaternion q) {
        _setOrientation(getNativePointer(), q.w(), q.x(), q.y(), q.z());
    }

    public void setOrientation(double w, double x, double y, double z) {
        _setOrientation(getNativePointer(), w, x, y, z);
    }
    private native void _setOrientation(long thisPointer, double w, double x, double y, double z);

    public void setManuallyControlled(boolean b) {
        _setManuallyControlled(getNativePointer(), b);
    }
    private native void _setManuallyControlled(long thisPointer, boolean b);

    public void setPosition(greta.core.util.math.Vec3d vec) {
        setPosition(vec.x(), vec.y(), vec.z());
    }

    public void setPosition(double d, double d0, double d1) {
        _setPosition(getNativePointer(),d, d0, d1);
    }
    private native void _setPosition(long thisPointer, double d, double d0, double d1);

    public void setScale(double d, double d0, double d1) {
        _setScale(getNativePointer(),d, d0, d1);
    }
    private native void _setScale(long thisPointer, double d, double d0, double d1);

    public String getName() {
        return _getName(getNativePointer());
    }
    private native String _getName(long thisPointer);

    public int numChildren() {
        return _numChildren(getNativePointer());
    }
    private native int _numChildren(long thisPointer);

    public String getChild_getName(int i) {
        return _getChild_getName(getNativePointer(), i);
    }
    private native String _getChild_getName(long thisPointer, int index);

    public Vector3 _getDerivedPosition() {
        return new Vector3(__getDerivedPosition(getNativePointer()));
    }
    private native long __getDerivedPosition(long thisPointer);

    public Node getParent() {
        return new _Node(_getParent(getNativePointer()));
    }
    private native long _getParent(long thisPointer);

    @Override
    protected native void delete(long nativePointer);
}
