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
package greta.core.animation.common.body;

import greta.core.animation.common.Frame.ExtendedKeyFrame;
import greta.core.animation.common.Skeleton;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class Torse extends ExtendedKeyFrame {
    Skeleton _skeleton;
    Quaternion r = new Quaternion();

    public Torse(double time) {
        super(time);
    }
    public Skeleton getSkeleton() {
        return _skeleton;
    }

    public void setSkeleton(Skeleton _skeleton) {
        this._skeleton = _skeleton;
    }

    public void setRotation(Quaternion q) {
        r = new Quaternion(q);
    }

    public Quaternion getRotation() {
        return r;
    }

    public void pointToDirection(Vec3d direction) {
        Vec3d original = new Vec3d(0, 0, 1);
        Vec3d n = Vec3d.cross3(original, direction);
        double cosTheta = original.dot3(direction);
        cosTheta = 1 < cosTheta ? 1 : cosTheta;
        double angle = (double) java.lang.Math.acos(cosTheta);
        Quaternion global = new Quaternion(n, angle);

    }

    @Override
    public Torse clone() {
        return (Torse) super.clone();
    }
}
