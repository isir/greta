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
package greta.core.animation.rbdl;

import greta.core.animation.math.SpatialMatrix6d;
import greta.core.animation.math.Vector3d;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 * dynamics model from featherstone http://royfeatherstone.org/spatial/
 */
public class FixedBody {

    double mMass;
    /// \brief The position of the center of mass in body coordinates
    Vector3d mCenterOfMass;
    /// \brief The spatial inertia that contains both mass and inertia information
    SpatialMatrix6d mSpatialInertia;

    /// \brief Id of the movable body that this fixed body is attached to.
    int mMovableParent;
    /// \brief Transforms spatial quantities expressed for the parent to the
    // fixed body.
    SpatialTransform mParentTransform;
    SpatialTransform mBaseTransform;

    public static FixedBody createFromBody(DBody body) {
        FixedBody fbody = new FixedBody();
        fbody.mMass = body.mMass;
        fbody.mCenterOfMass = body.mCenterOfMass;
        fbody.mSpatialInertia = body.mSpatialInertia;
        return fbody;
    }
}
