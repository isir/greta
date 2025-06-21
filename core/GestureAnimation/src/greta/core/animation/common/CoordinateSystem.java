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
package greta.core.animation.common;

import greta.core.util.math.Quaternion;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Jing Huang
 */
public abstract class CoordinateSystem {

    public CoordinateSystem() {
        init();
    }

    public void rotate(Quaternion rotation) {
        Iterator<Axis> itor = _axes.iterator();
        while (itor.hasNext()) {
            Axis axis = itor.next();
            axis._vect = Quaternion.multiplication(rotation, axis._vect);
            axis.normalize();
        }
    }

    public abstract void drawAxes(double length);

    public abstract void init();

    public abstract void reset();
    public ArrayList<Axis> _axes = new ArrayList<Axis>();
}
