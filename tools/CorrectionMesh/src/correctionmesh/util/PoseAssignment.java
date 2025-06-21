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
package correctionmesh.util;

import greta.core.util.math.Vec3d;

/**
 *
 * @author Andre-Marie Pez
 */
public class PoseAssignment {

    public Vec3d offset;
    public String poseName;

    public PoseAssignment(String pose, double x, double y, double z) {
        offset = new Vec3d(x, y, z);
        poseName = pose;
    }

    public PoseAssignment(PoseAssignment toCopy) {
        offset = new Vec3d(toCopy.offset);
        poseName = toCopy.poseName;
    }
}
