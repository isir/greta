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
package greta.core.animation;

import greta.core.util.math.Vec3d;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class PositionFrame {
    HashMap<String, Vec3d> _points = new HashMap<String, Vec3d>();

    public PositionFrame(){}

    public void setPoint(String name, Vec3d value){
        _points.put(name, value);
    }

    public Vec3d getValue(String name){
        return _points.get(name);
    }

    public HashMap<String, Vec3d> getValues(){
        return _points;
    }
}
