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
