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
package greta.core.animation.struct;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class SkeletonModel {
    public class Joint{
        public String _name;
    }

    public class Body{
        public String _name;
    }

    //joint info
    ArrayList<Joint> _joints = new ArrayList<Joint>();
    HashMap<String, Integer> _jointNameIds = new HashMap<String, Integer>();
    ArrayList<Integer> _parentJ = new ArrayList<Integer>();
    ArrayList<Quaternion> _localRotations = new ArrayList<Quaternion>();
    ArrayList<Quaternion> _globalRotations = new ArrayList<Quaternion>();
    ArrayList<Vec3d> _localPosition = new ArrayList<Vec3d>();
    ArrayList<Vec3d> _globalPosition = new ArrayList<Vec3d>();

    //body info
    ArrayList<Body> _bodys = new ArrayList<Body>();
    ArrayList<Integer> _connectedParentJoint = new ArrayList<Integer>();
    ArrayList<Vec3d> _localCOM = new ArrayList<Vec3d>();
    ArrayList<Float> _mass = new ArrayList<Float>();
}
