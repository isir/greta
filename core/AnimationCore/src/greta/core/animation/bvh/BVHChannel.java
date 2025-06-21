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
package greta.core.animation.bvh;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jing HUANG
 */
public class BVHChannel {

    public final static String BVH_CHANNEL_X_POSITION = "Xposition";
    public final static String BVH_CHANNEL_Y_POSITION = "Yposition";
    public final static String BVH_CHANNEL_Z_POSITION = "Zposition";
    public final static String BVH_CHANNEL_Z_ROTATION = "Zrotation";
    public final static String BVH_CHANNEL_X_ROTATION = "Xrotation";
    public final static String BVH_CHANNEL_Y_ROTATION = "Yrotation";
    private ArrayList<String> _order = new ArrayList<String>();
    private ArrayList<Double> _values = new ArrayList<Double>();

    public BVHChannel() {
    }

    public BVHChannel(BVHChannel r) {
        for (String name : r.getOrder()) {
            _order.add(name);
        }
        for (double v : r.getValues()) {
            _values.add(v);
        }

    }

    @Override
    public BVHChannel clone() {
        return new BVHChannel(this);
    }

    public List<String> getOrder() {
        return _order;
    }

    public void setOrder(ArrayList<String> order) {
        this._order = order;
    }

    public ArrayList<Double> getValues() {
        return _values;
    }

    public void setValues(ArrayList<Double> values) {
        this._values = values;
    }

    @Override
    public String toString() {
        return "\nBVHChannel{" + " (" + _order.size() + ")" + "values=" + _order + '}';
    }

    public boolean has6DOF(){
        if(_order.size() == 6)
            return true;
        return false;
    }
    // euler x - y - z  =>  z * y * x * v
    //http://en.wikipedia.org/wiki/Tait%E2%80%93Bryan_angles  they talk about  x y z, that the last rotation is z which is the most local rotation
    public Quaternion convert() {
        Quaternion rotation = new Quaternion();
        for (int i = 0; i < _order.size(); ++i) {
            String order = _order.get(i);
            double rotationValue = (double) (_values.get(i) / 180.0 * 3.14159265);
            if (order.equalsIgnoreCase(BVH_CHANNEL_X_ROTATION)) {
                Quaternion q = new Quaternion();
                q.setAxisAngle(new Vec3d(1, 0, 0), rotationValue);
                rotation = Quaternion.multiplication(rotation, q);
                //rotation = Quaternion.multiplication(q, rotation);
            } else if (order.equalsIgnoreCase(BVH_CHANNEL_Y_ROTATION)) {
                Quaternion q = new Quaternion();
                q.setAxisAngle(new Vec3d(0, 1, 0), rotationValue);
                rotation = Quaternion.multiplication(rotation, q);
                //rotation = Quaternion.multiplication(q, rotation);
            } else if (order.equalsIgnoreCase(BVH_CHANNEL_Z_ROTATION)) {
                Quaternion q = new Quaternion();
                q.setAxisAngle(new Vec3d(0, 0, 1), rotationValue);
                rotation = Quaternion.multiplication(rotation, q);
                //rotation = Quaternion.multiplication(q, rotation);
            }
        }
        return rotation;
    }

    public Quaternion getRotation() {
        return convert().normalized();
    }

    public Vec3d getTranslation() {
        Vec3d t = new Vec3d();
        for (int i = 0; i < _order.size(); ++i) {
            String order = _order.get(i);
            double value = (double) (_values.get(i).doubleValue());
            if (order.equalsIgnoreCase(BVH_CHANNEL_X_POSITION)) {
                t.set(0, value);
            } else if (order.equalsIgnoreCase(BVH_CHANNEL_Y_POSITION)) {
                t.set(1, value);
            } else if (order.equalsIgnoreCase(BVH_CHANNEL_Z_POSITION)) {
                t.set(2, value);
            }
        }
        return t;
    }
}
