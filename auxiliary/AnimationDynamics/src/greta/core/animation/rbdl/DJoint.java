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

import greta.core.animation.math.SpatialVector6d;
import greta.core.animation.math.Vector3d;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 * dynamics model from featherstone http://royfeatherstone.org/spatial/
 */
public class DJoint {

    public enum JointType {

        JointTypeUndefined,
        JointTypeRevolute,
        JointTypePrismatic,
        JointTypeSpherical, ///< 3 DoF joint using Quaternions for joint positional variables and angular velocity for joint velocity variables.
        JointTypeEulerZYX, ///< Experimental 3 DoF joint that uses Euler ZYX convention (not using virtual bodies).
        JointTypeFixed, ///< Fixed joint which causes the inertial properties to be merged with the parent body.
        JointType1DoF,
        JointType2DoF, ///< Emulated 2 DoF joint.
        JointType3DoF, ///< Emulated 3 DoF joint.
        JointType4DoF, ///< Emulated 4 DoF joint.
        JointType5DoF, ///< Emulated 5 DoF joint.
        JointType6DoF ///< Emulated 6 DoF joint.
    }

    /// \brief The spatial axis of the joint
    SpatialVector6d[] mJointAxes;
    /// \brief Type of joint (rotational or prismatic)
    JointType mJointType;
    int mDoFCount;
    int q_index;

    /**
     * \brief Describes a joint relative to the predecessor body.
     *
     * This class contains all information required for one single joint. This
     * contains the joint type and the axis of the joint. See \ref
     * joint_description for detailed description.
     *
     */
    public DJoint() {
        mJointAxes = null;
        mJointType = JointType.JointTypeUndefined;
        mDoFCount = 0;
        q_index = 0;
    }

    public DJoint(JointType type) {
        mJointAxes = null;
        mJointType = type;
        mDoFCount = 0;
        q_index = 0;
        if (type == JointType.JointTypeSpherical) {
            mDoFCount = 3;
            mJointAxes = new SpatialVector6d[mDoFCount];
            mJointAxes[0] = new SpatialVector6d(0., 0., 1., 0., 0., 0.);
            mJointAxes[1] = new SpatialVector6d(0., 1., 0., 0., 0., 0.);
            mJointAxes[2] = new SpatialVector6d(1., 0., 0., 0., 0., 0.);
        } else if (type == JointType.JointTypeEulerZYX) {
            mDoFCount = 3;
            mJointAxes = new SpatialVector6d[mDoFCount];
            mJointAxes[0] = new SpatialVector6d(0., 0., 1., 0., 0., 0.);
            mJointAxes[1] = new SpatialVector6d(0., 1., 0., 0., 0., 0.);
            mJointAxes[2] = new SpatialVector6d(1., 0., 0., 0., 0., 0.);
        } else if (type != JointType.JointTypeFixed) {
            System.out.println("Error: Invalid use of Joint constructor Joint(JointType type). Only allowed when type == JointTypeFixed or JointTypeSpherical.");

        }
    }

    public DJoint(DJoint joint) {
        mJointType = joint.mJointType;
        mDoFCount = joint.mDoFCount;
        q_index = joint.q_index;
        mJointAxes = new SpatialVector6d[mDoFCount];
        System.arraycopy(joint.mJointAxes, 0, mJointAxes, 0, mDoFCount);
    }

    /**
     * \brief Constructs a joint from the given cartesian parameters.
     *
     * This constructor creates all the required spatial values for the given
     * cartesian parameters.
     *
     * \param joint_type whether the joint is revolute or prismatic \param
     * joint_axis the axis of rotation or translation
     *
     * @param joint_type
     * @param joint_axis
     */
    public DJoint(JointType joint_type, Vector3d joint_axis) {
        mDoFCount = 1;
        mJointAxes = new SpatialVector6d[mDoFCount];

        mJointType = joint_type;

        if (joint_type == JointType.JointTypeRevolute) {
            // make sure we have a unit axis
            mJointAxes[0] = new SpatialVector6d();
            mJointAxes[0].set(
                    joint_axis.getEntry(0),
                    joint_axis.getEntry(1),
                    joint_axis.getEntry(2),
                    0., 0., 0.
            );

        } else if (joint_type == JointType.JointTypePrismatic) {
            joint_axis.normalize();
            mJointAxes[0] = new SpatialVector6d();
            mJointAxes[0].set(
                    0., 0., 0.,
                    joint_axis.getEntry(0),
                    joint_axis.getEntry(1),
                    joint_axis.getEntry(2)
            );
        }
    }

    /**
     * \brief Constructs a 1 DoF joint with the given motion subspaces.
     *
     * The motion subspaces are of the format: \f[ (r_x, r_y, r_z, t_x, t_y,
     * t_z) \f]
     *
     * \note So far only pure rotations or pure translations are supported.
     *
     * \param axis_0 Motion subspace for axis 0
     */
    public DJoint(SpatialVector6d axis_0) {
        mJointType = JointType.JointType1DoF;
        mDoFCount = 1;
        mJointAxes = new SpatialVector6d[mDoFCount];
        mJointAxes[0] = axis_0;
        validate_spatial_axis(mJointAxes[0]);
    }

    /**
     * \brief Constructs a 2 DoF joint with the given motion subspaces.
     *
     * The motion subspaces are of the format: \f[ (r_x, r_y, r_z, t_x, t_y,
     * t_z) \f]
     *
     * \note So far only pure rotations or pure translations are supported.
     *
     * \param axis_0 Motion subspace for axis 0 \param axis_1 Motion subspace
     * for axis 1
     */
    public DJoint(SpatialVector6d axis_0, SpatialVector6d axis_1) {
        mJointType = JointType.JointType2DoF;
        mDoFCount = 2;
        mJointAxes = new SpatialVector6d[mDoFCount];
        mJointAxes[0] = axis_0;
        mJointAxes[1] = axis_1;
        validate_spatial_axis(mJointAxes[0]);
        validate_spatial_axis(mJointAxes[1]);
    }

    /**
     * \brief Constructs a 3 DoF joint with the given motion subspaces.
     *
     * The motion subspaces are of the format: \f[ (r_x, r_y, r_z, t_x, t_y,
     * t_z) \f]
     *
     * \note So far only pure rotations or pure translations are supported.
     *
     * \param axis_0 Motion subspace for axis 0 \param axis_1 Motion subspace
     * for axis 1 \param axis_2 Motion subspace for axis 2
     */
    public DJoint(SpatialVector6d axis_0, SpatialVector6d axis_1, SpatialVector6d axis_2) {
        mJointType = JointType.JointType3DoF;
        mDoFCount = 3;
        mJointAxes = new SpatialVector6d[mDoFCount];
        mJointAxes[0] = axis_0;
        mJointAxes[1] = axis_1;
        mJointAxes[2] = axis_2;
        validate_spatial_axis(mJointAxes[0]);
        validate_spatial_axis(mJointAxes[1]);
        validate_spatial_axis(mJointAxes[2]);
    }

    /**
     * \brief Constructs a 4 DoF joint with the given motion subspaces.
     *
     * The motion subspaces are of the format: \f[ (r_x, r_y, r_z, t_x, t_y,
     * t_z) \f]
     *
     * \note So far only pure rotations or pure translations are supported.
     *
     * \param axis_0 Motion subspace for axis 0 \param axis_1 Motion subspace
     * for axis 1 \param axis_2 Motion subspace for axis 2 \param axis_3 Motion
     * subspace for axis 3
     */
    public DJoint(SpatialVector6d axis_0, SpatialVector6d axis_1, SpatialVector6d axis_2, SpatialVector6d axis_3) {
        mJointType = JointType.JointType4DoF;
        mDoFCount = 4;
        mJointAxes = new SpatialVector6d[mDoFCount];
        mJointAxes[0] = axis_0;
        mJointAxes[1] = axis_1;
        mJointAxes[2] = axis_2;
        mJointAxes[3] = axis_3;
        validate_spatial_axis(mJointAxes[0]);
        validate_spatial_axis(mJointAxes[1]);
        validate_spatial_axis(mJointAxes[2]);
        validate_spatial_axis(mJointAxes[3]);
    }

    /**
     * \brief Constructs a 5 DoF joint with the given motion subspaces.
     *
     * The motion subspaces are of the format: \f[ (r_x, r_y, r_z, t_x, t_y,
     * t_z) \f]
     *
     * \note So far only pure rotations or pure translations are supported.
     *
     * \param axis_0 Motion subspace for axis 0 \param axis_1 Motion subspace
     * for axis 1 \param axis_2 Motion subspace for axis 2 \param axis_3 Motion
     * subspace for axis 3 \param axis_4 Motion subspace for axis 4
     */
    public DJoint(SpatialVector6d axis_0, SpatialVector6d axis_1, SpatialVector6d axis_2, SpatialVector6d axis_3, SpatialVector6d axis_4) {
        mJointType = JointType.JointType5DoF;
        mDoFCount = 5;
        mJointAxes = new SpatialVector6d[mDoFCount];
        mJointAxes[0] = axis_0;
        mJointAxes[1] = axis_1;
        mJointAxes[2] = axis_2;
        mJointAxes[3] = axis_3;
        mJointAxes[4] = axis_4;

        validate_spatial_axis(mJointAxes[0]);
        validate_spatial_axis(mJointAxes[1]);
        validate_spatial_axis(mJointAxes[2]);
        validate_spatial_axis(mJointAxes[3]);
        validate_spatial_axis(mJointAxes[4]);
    }

    /**
     * \brief Constructs a 6 DoF joint with the given motion subspaces.
     *
     * The motion subspaces are of the format: \f[ (r_x, r_y, r_z, t_x, t_y,
     * t_z) \f]
     *
     * \note So far only pure rotations or pure translations are supported.
     *
     * \param axis_0 Motion subspace for axis 0 \param axis_1 Motion subspace
     * for axis 1 \param axis_2 Motion subspace for axis 2 \param axis_3 Motion
     * subspace for axis 3 \param axis_4 Motion subspace for axis 4 \param
     * axis_5 Motion subspace for axis 5
     */
    public DJoint(SpatialVector6d axis_0, SpatialVector6d axis_1, SpatialVector6d axis_2, SpatialVector6d axis_3, SpatialVector6d axis_4, SpatialVector6d axis_5) {
        mJointType = JointType.JointType6DoF;
        mDoFCount = 6;
        mJointAxes = new SpatialVector6d[mDoFCount];
        mJointAxes[0] = axis_0;
        mJointAxes[1] = axis_1;
        mJointAxes[2] = axis_2;
        mJointAxes[3] = axis_3;
        mJointAxes[4] = axis_4;
        mJointAxes[5] = axis_5;

        validate_spatial_axis(mJointAxes[0]);
        validate_spatial_axis(mJointAxes[1]);
        validate_spatial_axis(mJointAxes[2]);
        validate_spatial_axis(mJointAxes[3]);
        validate_spatial_axis(mJointAxes[4]);
        validate_spatial_axis(mJointAxes[5]);
    }

    /**
     * \brief Checks whether we have pure rotational or translational axis.
     *
     * This function is mainly used to print out warnings when specifying an
     * axis that might not be intended.
     */
    boolean validate_spatial_axis(SpatialVector6d axis) {
        if (Math.abs(axis.getNorm() - 1.0) > 1.0e-8) {
            System.out.println("Warning: joint axis is not unit!");
        }

        boolean axis_rotational = false;
        boolean axis_translational = false;

        Vector3d rotation = new Vector3d(axis.getEntry(0), axis.getEntry(1), axis.getEntry(2));
        Vector3d translation = new Vector3d(axis.getEntry(3), axis.getEntry(4), axis.getEntry(5));

        if (Math.abs(translation.getNorm()) < 1.0e-8) {
            axis_rotational = true;
        }
        if (Math.abs(rotation.getNorm()) < 1.0e-8) {
            axis_translational = true;
        }
        return axis_rotational || axis_translational;
    }
}
