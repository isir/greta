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
