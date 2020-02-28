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
public class SceneNode extends _Object_ implements Node {

    public SceneNode(long pointer) {
        super(pointer);
    }
    //greta.auxiliary.player.ogre.natives.Node.TransformSpace.TS_LOCAL

    public void yaw(double radian) {
        _yaw(getNativePointer(), radian);
    }
    private native void _yaw(long p, double a);

    public void pitch(double radian) {
        _pitch(getNativePointer(), radian);
    }
    private native void _pitch(long p, double a);

    public void roll(double radian) {
        _roll(getNativePointer(), radian);
    }
    private native void _roll(long p, double a);

    @Override
    public void _update(boolean updateChildren, boolean parentHasChanged) {
        __update(getNativePointer(), updateChildren, parentHasChanged);
    }
    private native void __update(long thisPointer, boolean updateChildren, boolean parentHasChanged);

    @Override
    public Quaternion _getDerivedOrientation() {
        return new Quaternion(__getDerivedOrientation(getNativePointer()));
    }
    private native long __getDerivedOrientation(long thisPointer);

    public void setPosition(greta.core.util.math.Vec3d v) {
        _setPosition(getNativePointer(), v.x(), v.y(), v.z());
    }
    public void setPosition(double x, double y, double z) {
        _setPosition(getNativePointer(), x, y, z);
    }
    private native void _setPosition(long p, double x, double y, double z);

    public void attachObject(MovableObject mo) {
        _attachObject(getNativePointer(), mo.getNativePointer());
    }
    private native void _attachObject(long p, long p2);

    public Quaternion getOrientation() {
        return new Quaternion(_getOrientation(getNativePointer()));
    }
    private native long _getOrientation(long thisPointer);

    public void setVisible(boolean visible, boolean b) {
        _setVisible(getNativePointer(), visible, b);
    }
    private native void _setVisible(long p, boolean b, boolean b2);

    public SceneNode getParentSceneNode() {
        return new SceneNode(_getParentSceneNode(getNativePointer()));
    }
    private native long _getParentSceneNode(long p);

    public Vector3 _getDerivedPosition() {
        return new Vector3(__getDerivedPosition(getNativePointer()));
    }
    private native long __getDerivedPosition(long p);

    public Vector3 getScale() {
        return new Vector3(_getScale(getNativePointer()));
    }
    private native long _getScale(long p);

    public SceneNode createChildSceneNode() {
        return new SceneNode(_createChildSceneNode(getNativePointer()));
    }
    private native long _createChildSceneNode(long p);

    public void translate(Vector3 vect) {
        _translate(getNativePointer(), vect.getNativePointer());
    }
    private native void _translate(long p, long p2);

    public void setOrientation(Quaternion orientation) {
        _setOrientation(getNativePointer(), orientation.getNativePointer());
    }
    private native void _setOrientation(long p, long p2);

    public Vector3 getPosition() {
        return new Vector3(_getPosition(getNativePointer()));
    }
    private native long _getPosition(long p);

    public void removeAllChildren() {
        _removeAllChildren(getNativePointer());
    }
    private native void _removeAllChildren(long p);

    public void scale(Vector3 vect) {
        _scale(getNativePointer(), vect.getNativePointer());
    }
    private native void _scale(long p, long p2);

    public SceneNode createChildSceneNode(String id) {
        return new SceneNode(_createChildSceneNode(getNativePointer(), id));
    }
    private native long _createChildSceneNode(long p, String id);

    public SceneNode createChildSceneNode(String id, Vector3 vect) {
        return new SceneNode(_createChildSceneNode(getNativePointer(), id, vect.getNativePointer()));
    }
    private native long _createChildSceneNode(long p, String id, long p2);

    public void setScale(greta.core.util.math.Vec3d v) {
        _setScale(getNativePointer(), v.x(), v.y(), v.z());
    }

    public void setScale(double d, double d0, double d1) {
        _setScale(getNativePointer(), d, d0, d1);
    }
    private native void _setScale(long p, double d, double d0, double d1);

    public void setPosition(Vector3 pivot) {
        _setPosition(getNativePointer(), pivot.getNativePointer());
    }
    private native void _setPosition(long p, long p2);

    public void setScale(Vector3 convert) {
        _setScale(getNativePointer(), convert.getNativePointer());
    }
    private native void _setScale(long p, long p2);

    public void removeChild(SceneNode child) {
        _removeChild(getNativePointer(), child.getNativePointer());
    }
    private native void _removeChild(long p, long p2);

    public void addChild(SceneNode child) {
        _addChild(getNativePointer(), child.getNativePointer());
    }
    private native void _addChild(long p, long p2);

    public String getChild_getName(int i) {
        return _getChild_getName(getNativePointer(), i);
    }
    private native String _getChild_getName(long nativePointer, int i);

    public int numChildren() {
        return _numChildren(getNativePointer());
    }
    private native int _numChildren(long nativePointer);

    public String getName() {
        return _getName(getNativePointer());
    }
    private native String _getName(long nativePointer);

    public void removeAndDestroyAllChildren() {
        _removeAndDestroyAllChildren(getNativePointer());
    }
    private native void _removeAndDestroyAllChildren(long p);

    public MovableObject getAttachedObject(int i) {
        return new _MovableObject(_getAttachedObject(getNativePointer(), i));
    }
    private native long _getAttachedObject(long p, int i);

    public int numAttachedObjects() {
        return _numAttachedObjects(getNativePointer());
    }
    private native int _numAttachedObjects(long p);

    @Override
    protected native void delete(long nativePointer);
}
