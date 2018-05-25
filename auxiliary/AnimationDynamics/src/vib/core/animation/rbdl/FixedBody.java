/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.animation.rbdl;

import vib.core.animation.math.SpatialMatrix6d;
import vib.core.animation.math.Vector3d;

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
