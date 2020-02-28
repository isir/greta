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

import greta.core.animation.math.Matrix3d;
import greta.core.animation.math.SpatialMatrix6d;
import greta.core.animation.math.Vector3d;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 * dynamics model from featherstone http://royfeatherstone.org/spatial/
 */
public class DBody {

    /// \brief The mass of the body
    double mMass;
    /// \brief The position of the center of mass in body coordinates
    Vector3d mCenterOfMass;
    /// \brief Inertia matrix at the center of mass
    Matrix3d mInertia;
    /// \brief The spatial inertia that contains both mass and inertia information
    SpatialMatrix6d mSpatialInertia;

    boolean mIsVirtual;

    public DBody() {
        mMass = 1;
        mCenterOfMass = new Vector3d(0., 0., 0.);
        mInertia = new Matrix3d();
        mInertia.toIdentity();
        mSpatialInertia = new SpatialMatrix6d();
        mIsVirtual = false;
    }

    public DBody(DBody body) {
        mMass = 1;
        mCenterOfMass = body.mCenterOfMass;
        mInertia = body.mInertia;
        mSpatialInertia = body.mSpatialInertia;
        mIsVirtual = body.mIsVirtual;
    }

    /**
     * \brief Constructs a body from mass, center of mass and radii of gyration
     *
     * This constructor eases the construction of a new body as all the required
     * parameters can be specified as parameters to the constructor. These are
     * then used to generate the spatial inertia matrix which is expressed at
     * the origin.
     *
     * \param mass the mass of the body \param com the position of the center of
     * mass in the bodies coordinates \param gyration_radii the radii of
     * gyration at the center of mass of the body
     *
     */
    /***
     *
     * @param mass
     * @param com  the center of mass is the position in local space
     * @param gyration_radii pages 171 John J.Craig  Introduction to robotics mechanics and control     relative to x y z unidisribution with mass
     */
    public DBody(double mass, Vector3d com, Vector3d gyration_radii) {
        mMass = mass;
        mCenterOfMass = com;
        mIsVirtual = false;
        Matrix3d com_cross = com.toCrossMatrix();
        Matrix3d parallel_axis = com_cross.multiple(com_cross.transpose()).multiple(mass);

        mInertia = new Matrix3d(
                gyration_radii.getEntry(0), 0., 0.,
                0., gyration_radii.getEntry(1), 0.,
                0., 0., gyration_radii.getEntry(2)
        );

        Matrix3d pa = new Matrix3d(parallel_axis);
        Matrix3d mcc = com_cross.multiple(mass);
        Matrix3d mccT = mcc.transpose();

        Matrix3d inertia_O = mInertia.add(pa);
        mSpatialInertia = new SpatialMatrix6d();
        mSpatialInertia.set(
                inertia_O.getEntry(0, 0), inertia_O.getEntry(0, 1), inertia_O.getEntry(0, 2), mcc.getEntry(0, 0), mcc.getEntry(0, 1), mcc.getEntry(0, 2),
                inertia_O.getEntry(1, 0), inertia_O.getEntry(1, 1), inertia_O.getEntry(1, 2), mcc.getEntry(1, 0), mcc.getEntry(1, 1), mcc.getEntry(1, 2),
                inertia_O.getEntry(2, 0), inertia_O.getEntry(2, 1), inertia_O.getEntry(2, 2), mcc.getEntry(2, 0), mcc.getEntry(2, 1), mcc.getEntry(2, 2),
                mccT.getEntry(0, 0), mccT.getEntry(0, 1), mccT.getEntry(0, 2), mass, 0., 0.,
                mccT.getEntry(1, 0), mccT.getEntry(1, 1), mccT.getEntry(1, 2), 0., mass, 0.,
                mccT.getEntry(2, 0), mccT.getEntry(2, 1), mccT.getEntry(2, 2), 0., 0., mass
        );
    }

    public void set(double mass, Vector3d com, Vector3d gyration_radii) {
        mMass = mass;
        mCenterOfMass = com;
        mIsVirtual = false;
        Matrix3d com_cross = com.toCrossMatrix();
        Matrix3d parallel_axis = com_cross.multiple(com_cross.transpose()).multiple(mass);

        mInertia = new Matrix3d(
                gyration_radii.getEntry(0), 0., 0.,
                0., gyration_radii.getEntry(1), 0.,
                0., 0., gyration_radii.getEntry(2)
        );

        Matrix3d pa = new Matrix3d(parallel_axis);
        Matrix3d mcc = com_cross.multiple(mass);
        Matrix3d mccT = mcc.transpose();

        Matrix3d inertia_O = mInertia.add(pa);

        mSpatialInertia.set(
                inertia_O.getEntry(0, 0), inertia_O.getEntry(0, 1), inertia_O.getEntry(0, 2), mcc.getEntry(0, 0), mcc.getEntry(0, 1), mcc.getEntry(0, 2),
                inertia_O.getEntry(1, 0), inertia_O.getEntry(1, 1), inertia_O.getEntry(1, 2), mcc.getEntry(1, 0), mcc.getEntry(1, 1), mcc.getEntry(1, 2),
                inertia_O.getEntry(2, 0), inertia_O.getEntry(2, 1), inertia_O.getEntry(2, 2), mcc.getEntry(2, 0), mcc.getEntry(2, 1), mcc.getEntry(2, 2),
                mccT.getEntry(0, 0), mccT.getEntry(0, 1), mccT.getEntry(0, 2), mass, 0., 0.,
                mccT.getEntry(1, 0), mccT.getEntry(1, 1), mccT.getEntry(1, 2), 0., mass, 0.,
                mccT.getEntry(2, 0), mccT.getEntry(2, 1), mccT.getEntry(2, 2), 0., 0., mass
        );
    }

    /**
     * \brief Constructs a body from mass, center of mass, and a 3x3 inertia
     * matrix
     *
     * This constructor eases the construction of a new body as all the required
     * parameters can simply be specified as parameters to the constructor.
     * These are then used to generate the spatial inertia matrix which is
     * expressed at the origin.
     *
     * \param mass the mass of the body \param com the position of the center of
     * mass in the bodies coordinates \param inertia_C the inertia at the center
     * of mass
     */
    public DBody(double mass, Vector3d com, Matrix3d inertia_C) {
        mMass = mass;
        mCenterOfMass = com;
        mIsVirtual = false;
        mInertia = inertia_C;

        Matrix3d com_cross = com.toCrossMatrix();
        Matrix3d parallel_axis = com_cross.multiple(com_cross.transpose()).multiple(mass);
        System.out.println("parrallel axis = " + parallel_axis);

        Matrix3d pa = new Matrix3d(parallel_axis);
        Matrix3d mcc = com_cross.multiple(mass);
        Matrix3d mccT = mcc.transpose();

        mSpatialInertia.set(
                inertia_C.getEntry(0, 0) + pa.getEntry(0, 0), inertia_C.getEntry(0, 1) + pa.getEntry(0, 1), inertia_C.getEntry(0, 2) + pa.getEntry(0, 2), mcc.getEntry(0, 0), mcc.getEntry(0, 1), mcc.getEntry(0, 2),
                inertia_C.getEntry(1, 0) + pa.getEntry(1, 0), inertia_C.getEntry(1, 1) + pa.getEntry(1, 1), inertia_C.getEntry(1, 2) + pa.getEntry(1, 2), mcc.getEntry(1, 0), mcc.getEntry(1, 1), mcc.getEntry(1, 2),
                inertia_C.getEntry(2, 0) + pa.getEntry(2, 0), inertia_C.getEntry(2, 1) + pa.getEntry(2, 1), inertia_C.getEntry(2, 2) + pa.getEntry(2, 2), mcc.getEntry(2, 0), mcc.getEntry(2, 1), mcc.getEntry(2, 2),
                mccT.getEntry(0, 0), mccT.getEntry(0, 1), mccT.getEntry(0, 2), mass, 0., 0.,
                mccT.getEntry(1, 0), mccT.getEntry(1, 1), mccT.getEntry(1, 2), 0., mass, 0.,
                mccT.getEntry(2, 0), mccT.getEntry(2, 1), mccT.getEntry(2, 2), 0., 0., mass
        );
    }

    public void set(double mass, Vector3d com, Matrix3d inertia_C) {
        mMass = mass;
        mCenterOfMass = com;
        mIsVirtual = false;
        mInertia = inertia_C;

        Matrix3d com_cross = com.toCrossMatrix();
        Matrix3d parallel_axis = com_cross.multiple(com_cross.transpose()).multiple(mass);
        System.out.println("parrallel axis = " + parallel_axis);

        Matrix3d pa = new Matrix3d(parallel_axis);
        Matrix3d mcc = com_cross.multiple(mass);
        Matrix3d mccT = mcc.transpose();

        mSpatialInertia.set(
                inertia_C.getEntry(0, 0) + pa.getEntry(0, 0), inertia_C.getEntry(0, 1) + pa.getEntry(0, 1), inertia_C.getEntry(0, 2) + pa.getEntry(0, 2), mcc.getEntry(0, 0), mcc.getEntry(0, 1), mcc.getEntry(0, 2),
                inertia_C.getEntry(1, 0) + pa.getEntry(1, 0), inertia_C.getEntry(1, 1) + pa.getEntry(1, 1), inertia_C.getEntry(1, 2) + pa.getEntry(1, 2), mcc.getEntry(1, 0), mcc.getEntry(1, 1), mcc.getEntry(1, 2),
                inertia_C.getEntry(2, 0) + pa.getEntry(2, 0), inertia_C.getEntry(2, 1) + pa.getEntry(2, 1), inertia_C.getEntry(2, 2) + pa.getEntry(2, 2), mcc.getEntry(2, 0), mcc.getEntry(2, 1), mcc.getEntry(2, 2),
                mccT.getEntry(0, 0), mccT.getEntry(0, 1), mccT.getEntry(0, 2), mass, 0., 0.,
                mccT.getEntry(1, 0), mccT.getEntry(1, 1), mccT.getEntry(1, 2), 0., mass, 0.,
                mccT.getEntry(2, 0), mccT.getEntry(2, 1), mccT.getEntry(2, 2), 0., 0., mass
        );
    }

//    public Body(double mMass, Vector3d mCenterOfMass, Matrix3d mInertia, SpatialMatrix6d mSpatialInertia, boolean mIsVirtual) {
//        this.mMass = mMass;
//        this.mCenterOfMass = mCenterOfMass;
//        this.mInertia = mInertia;
//        this.mSpatialInertia = mSpatialInertia;
//        this.mIsVirtual = mIsVirtual;
//    }
    /**
     * \brief Joins inertial parameters of two bodies to create a composite
     * body.
     *
     * This function can be used to joint inertial parameters of two bodies to
     * create a composite body that has the inertial properties as if the two
     * bodies were joined by a fixed joint.
     *
     * \note Both bodies have to have their inertial parameters expressed in the
     * same orientation.
     *
     * \param transform The frame transformation from the origin of the original
     * body to the origin of the added body \param other_body The other body
     * that will be merged with *this.
     */
    void join(SpatialTransform transform, DBody other_body) {
        // nothing to do if we join a massles body to the current.
        if (other_body.mMass == 0. && other_body.mInertia.equals(new Matrix3d())) {
            return;
        }

        double other_mass = other_body.mMass;
        double new_mass = mMass + other_mass;

        if (new_mass == 0.) {
            System.out.println("Error: cannot join bodies as both have zero mass!");
        }

        Vector3d other_com = transform.E.transpose().multiple(other_body.mCenterOfMass).add(transform.r);
        Vector3d new_com = (mCenterOfMass.multiple(mMass).add(other_com.multiple(other_mass))).multiple(1 / new_mass);

        System.out.println("other_com = " + other_com.toString());
        System.out.println("rotation = " + transform.E);

		// We have to transform the inertia of other_body to the new COM. This
        // is done in 4 steps:
        //
        // 1. Transform the inertia from other origin to other COM
        // 2. Rotate the inertia that it is aligned to the frame of this body
        // 3. Transform inertia of other_body to the origin of the frame of
        // this body
        // 4. Sum the two inertias
        // 5. Transform the summed inertia to the new COM
        Matrix3d inertia_other = new Matrix3d(other_body.mSpatialInertia.getSubMatrix(0, 2, 0, 2));
        System.out.println("inertia_other = " + inertia_other);

        // 1. Transform the inertia from other origin to other COM
        Matrix3d other_com_cross = other_body.mCenterOfMass.toCrossMatrix();
        Matrix3d inertia_other_com = inertia_other.substract(other_com_cross.multiple(other_com_cross.transpose()).multiple(other_mass));
        System.out.println("inertia_other_com = " + inertia_other_com);

        // 2. Rotate the inertia that it is aligned to the frame of this body
        Matrix3d inertia_other_com_rotated = transform.E.transpose().multiple(inertia_other_com.multiple(transform.E));
        System.out.println("inertia_other_com_rotated = " + inertia_other_com_rotated);

        // 3. Transform inertia of other_body to the origin of the frame of this body
        Matrix3d inertia_other_com_rotated_this_origin = parallel_axis(inertia_other_com_rotated, other_mass, other_com);
        System.out.println("inertia_other_com_rotated_this_origin = " + inertia_other_com_rotated_this_origin);

        // 4. Sum the two inertias
        Matrix3d inertia_summed = new Matrix3d(mSpatialInertia.getSubMatrix(0, 2, 0, 2)).add(inertia_other_com_rotated_this_origin);
        System.out.println("inertia_summed  = " + inertia_summed);

        // 5. Transform the summed inertia to the new COM
        Matrix3d new_inertia = inertia_summed.substract(new_com.toCrossMatrix().multiple(new_com.toCrossMatrix().transpose()).multiple(new_mass));

        System.out.println("new_mass = " + new_mass);
        System.out.println("new_com  = " + new_com);
        System.out.println("new_inertia  = " + new_inertia);

        this.set(new_mass, new_com, new_inertia);
    }

    public Matrix3d parallel_axis(Matrix3d inertia, double mass, Vector3d com) {
        Matrix3d com_cross = com.toCrossMatrix();
        return inertia.add(com_cross.multiple(com_cross.transpose()).multiple(mass));
    }


    public static Matrix3d generateRotationInertia(double x, double y, double z, double mass, Matrix3d rotation){
        Matrix3d inertia = new Matrix3d();
        inertia.setEntry(0, 0, mass /12.0 * (y * y + z * z));
        inertia.setEntry(1, 1, mass /12.0 * (z * z + x * x));
        inertia.setEntry(2, 2, mass /12.0 * (x * x + y * y));
        inertia = rotation.multiple(inertia.multiple(rotation.transpose()));
        return inertia;
    }

    public static Matrix3d generateRotationInertia(double x, double y, double z, double mass, Vector3d dir, Vector3d dirOriginal){
        dir = dir.divide(dir.getNorm());
        dirOriginal = dirOriginal.divide(dirOriginal.getNorm());
        Matrix3d rotation = new Matrix3d();
        rotation.setEntry(0, 0, dirOriginal.dotProduct(dir));
        rotation.setEntry(0, 1, -dirOriginal.cross(dir).getNorm());
        rotation.setEntry(1, 0, dirOriginal.cross(dir).getNorm());
        rotation.setEntry(1, 1, dirOriginal.dotProduct(dir));
        rotation.setEntry(3, 3, 1);

        Matrix3d inertia = new Matrix3d();
        inertia.setEntry(0, 0, mass /12.0 * (y * y + z * z));
        inertia.setEntry(1, 1, mass /12.0 * (z * z + x * x));
        inertia.setEntry(2, 2, mass /12.0 * (x * x + y * y));
        inertia = rotation.multiple(inertia.multiple(rotation.transpose()));
        return inertia;
    }
}
