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
