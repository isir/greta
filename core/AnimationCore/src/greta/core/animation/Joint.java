/*
 * This file is part of Greta.
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
package greta.core.animation;

import greta.core.util.math.Matrix4d;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class Joint {

    String _name;
    int _id = -1;
    Skeleton _skeleton;

    public Joint(String name, int id, int parent, Skeleton skeleton) {
        _skeleton = skeleton;
        _name = name;
        _id = id;
        _skeleton._parentJ.set(_id, parent);
    }

    public Joint(Joint j) {
        _skeleton = j._skeleton;
        _name = j._name;
        _id = j._id;
    }

    public Joint clone() {
        return new Joint(this);
    }

    public Joint(String name) {
        this(name, -1, -1, null);
    }

    public void setParent(Joint parent) {
        if (parent != null) {
            _skeleton._parentJ.set(_id, parent._id);
            if (_skeleton._children.get(parent._id) == null) {
                _skeleton._children.set(parent._id, new ArrayList<Integer>());
            }
            _skeleton._children.get(parent._id).add(_id);
        }
    }

    public void setParent(int parent) {
        if (parent != -1) {
            _skeleton._parentJ.set(_id, parent);
            if (_skeleton._children.get(parent) == null) {
                _skeleton._children.set(parent, new ArrayList<Integer>());
            }
            _skeleton._children.get(parent).add(_id);
        }
    }

    public int getId() {
        return _id;
    }

    public Joint getParent() {
        return _skeleton.getJoint(_skeleton._parentJ.get(_id));
    }

    public int getParentId() {
        return _skeleton._parentJ.get(_id);
    }

    public void addChild(Joint child) {
        if (_skeleton._children.get(_id) == null) {
            _skeleton._children.set(_id, new ArrayList<Integer>());
        }
        _skeleton._children.get(_id).add(child._id);
    }

    public ArrayList<Integer> getChildren() {
        return _skeleton._children.get(_id);
    }

    public String getName() {
        return _name;
    }

    public void setLocalRotation(Quaternion local) {
        _skeleton._localRotations.get(_id).setValue(local);
    }

    public Quaternion getLocalRotation() {
        return _skeleton._localRotations.get(_id);
    }

    public Quaternion getWorldOrientation() {
        return _skeleton._globalRotations.get(_id);
    }

    public void setLocalPosition(Vec3d translation) {
        _skeleton._localPosition.get(_id).set(translation.x(), translation.y(), translation.z());
    }

    public Vec3d getLocalPosition() {
        return _skeleton._localPosition.get(_id);
    }

    public Vec3d getWorldPosition() {
        return _skeleton._globalPosition.get(_id);
    }

    public void rotate(Quaternion rotation) {
        Quaternion tmp = rotation;
        tmp.normalize();
        Quaternion r = _skeleton._localRotations.get(_id);
        r.multiply(tmp);
        r.normalize();
    }

    public Vec3d getPosition() {
        return _skeleton._globalPosition.get(_id);
    }

    public void update() {
        if (getParentId() == -1) {
            _skeleton._globalPosition.get(_id).set(_skeleton._localPosition.get(_id));
            _skeleton._globalRotations.get(_id).setValue(_skeleton._localRotations.get(_id));
            _skeleton._globalRotations.get(_id).normalize();

        } else {
            _skeleton._globalPosition.get(_id).set(Quaternion.multiplication(getParent().getWorldOrientation(), _skeleton._localPosition.get(_id)));
            _skeleton._globalPosition.get(_id).add(getParent().getWorldPosition());
            _skeleton._globalRotations.get(_id).setValue(Quaternion.multiplication(getParent().getWorldOrientation(), getLocalRotation()));
            _skeleton._globalRotations.get(_id).normalize();
        }
    }

    public void updateMatrix() {
        _skeleton._localMs.get(_id).set(_skeleton._localRotations.get(_id).matrix());
        _skeleton._localMs.get(_id).set(0, 3, getLocalPosition().x());
        _skeleton._localMs.get(_id).set(1, 3, getLocalPosition().y());
        _skeleton._localMs.get(_id).set(2, 3, getLocalPosition().z());
        if (getParentId() == -1) {
            _skeleton._globalMs.get(_id).set(_skeleton._localMs.get(_id));
        } else {
            _skeleton._globalMs.get(_id).set(Matrix4d.multiplication(getParent().getGlobalMatrix(), getLocalMatrix()));
        }
    }

    public Matrix4d getLocalMatrix() {
        return _skeleton._localMs.get(_id);
    }

    public Matrix4d getGlobalMatrix() {
        return _skeleton._globalMs.get(_id);
    }

}
