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
import greta.core.animation.math.Matrix63d;
import greta.core.animation.math.SpatialMatrix6d;
import greta.core.animation.math.SpatialVector6d;
import greta.core.animation.math.Vector3d;
import greta.core.animation.rbdl.DJoint.JointType;
import static greta.core.animation.rbdl.DJoint.JointType.JointTypeEulerZYX;
import static greta.core.animation.rbdl.DJoint.JointType.JointTypePrismatic;
import static greta.core.animation.rbdl.DJoint.JointType.JointTypeRevolute;
import static greta.core.animation.rbdl.DJoint.JointType.JointTypeSpherical;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 * dynamics model from featherstone http://royfeatherstone.org/spatial/
 */
public class DModel {
    // Structural information

    /// \brief The id of the parents body
    ArrayList<Integer> lambda = new ArrayList<Integer>();
    /// \brief Contains the ids of all the children of a given body
    ArrayList<ArrayList<Integer>> mu = new ArrayList<ArrayList<Integer>>();

    /**
     * \brief number of degrees of freedoms of the model
     *
     * This value contains the number of entries in the generalized state (q)
     * velocity (qdot), acceleration (qddot), and force (tau) vector.
     */
    int dof_count;

    /**
     * \brief The size of the \f$\mathbf{q}\f$-vector. For models without
     * spherical joints the value is the same as Model::dof_count, otherwise
     * additional values for the w-component of the Quaternion is stored at the
     * end of \f$\mathbf{q}\f$.
     *
     * \sa \ref joint_description for more details.
     */
    int q_size;
    /**
     * \brief The size of the \f$\mathbf{\dot{q}}, \mathbf{\ddot{q}}\f$, and
     * \f$\mathbf{\tau}\f$-vector.
     *
     * \sa \ref joint_description for more details.
     */
    int qdot_size;

    /// \brief Id of the previously added body, required for Model::AppendBody()
    int previously_added_body_id;

    /// \brief the cartesian vector of the gravity
    Vector3d gravity;

    // State information
    /// \brief The spatial velocity of the bodies
    ArrayList<SpatialVector6d> v = new ArrayList<SpatialVector6d>();
    /// \brief The spatial acceleration of the bodies
    ArrayList<SpatialVector6d> a = new ArrayList<SpatialVector6d>();

    ////////////////////////////////////
    // Joints
    /// \brief All joints
    ArrayList<DJoint> mJoints = new ArrayList<DJoint>();
    /// \brief The joint axis for joint i
    ArrayList<SpatialVector6d> S = new ArrayList<SpatialVector6d>();
    /// \brief Transformations from the parent body to the frame of the joint.
    // It is expressed in the coordinate frame of the parent.
    ArrayList<SpatialTransform> X_T = new ArrayList<SpatialTransform>();
    /// \brief The number of fixed joints that have been declared before each joint.
    ArrayList<Integer> mFixedJointCount = new ArrayList<Integer>();

    ////////////////////////////////////
    // Special variables for joints with 3 degrees of freedom
    /// \brief Motion subspace for joints with 3 degrees of freedom
    ArrayList<Matrix63d> multdof3_S = new ArrayList<Matrix63d>();
    ArrayList<Matrix63d> multdof3_U = new ArrayList<Matrix63d>();
    ArrayList<Matrix3d> multdof3_Dinv = new ArrayList<Matrix3d>();
    ArrayList<Vector3d> multdof3_u = new ArrayList<Vector3d>();
    ArrayList<Integer> multdof3_w_index = new ArrayList<Integer>();

    ////////////////////////////////////
    // Dynamics variables
    /// \brief The velocity dependent spatial acceleration
    ArrayList<SpatialVector6d> c = new ArrayList<SpatialVector6d>();
    /// \brief The spatial inertia of the bodies
    ArrayList<SpatialMatrix6d> IA = new ArrayList<SpatialMatrix6d>();
    /// \brief The spatial bias force
    ArrayList<SpatialVector6d> pA = new ArrayList<SpatialVector6d>();
    /// \brief Temporary variable U_i (RBDA p. 130)
    ArrayList<SpatialVector6d> U = new ArrayList<SpatialVector6d>();
    /// \brief Temporary variable D_i (RBDA p. 130)
    ArrayRealVector d;
    /// \brief Temporary variable u (RBDA p. 130)
    ArrayRealVector u;
    /// \brief Internal forces on the body (used only InverseDynamics())
    ArrayList<SpatialVector6d> f = new ArrayList<SpatialVector6d>();
    /// \brief The spatial inertia of body i (used only in CompositeRigidBodyAlgorithm())
    ArrayList<SpatialRigidBodyInertia> Ic = new ArrayList<SpatialRigidBodyInertia>();
    ArrayList<SpatialVector6d> hc = new ArrayList<SpatialVector6d>();

    ////////////////////////////////////
    // Bodies
    /**
     * \brief Transformation from the parent body to the current body \f[
     * X_{\lambda(i)} = {}^{i} X_{\lambda(i)} \f]
     */
    ArrayList<SpatialTransform> X_lambda = new ArrayList<SpatialTransform>();
    /// \brief Transformation from the base to bodies reference frame
    ArrayList<SpatialTransform> X_base = new ArrayList<SpatialTransform>();

    /// \brief All bodies that are attached to a body via a fixed joint.
    ArrayList<FixedBody> mFixedBodies = new ArrayList<FixedBody>();
    /**
     * \brief Value that is used to discriminate between fixed and movable
     * bodies.
     *
     * Bodies with id 1 .. (fixed_body_discriminator - 1) are moving bodies
     * while bodies with id fixed_body_discriminator .. max (unsigned int) are
     * fixed to a moving body. The value of max(unsigned int) is determined via
     * std::numeric_limits<unsigned int>::max() and the default value of
     * fixed_body_discriminator is max (unsigned int) / 2.
     *
     * On normal systems max (unsigned int) is 4294967294 which means there
     * could be a total of 2147483646 movable and / or fixed bodies.
     */
    int fixed_body_discriminator;

    /**
     * \brief All bodies 0 ... N_B, including the base
     *
     * mBodies[0] - base body <br>
     * mBodies[1] - 1st moveable body <br>
     * ... <br>
     * mBodies[N_B] - N_Bth moveable body <br>
     */
    ArrayList<DBody> mBodies = new ArrayList<DBody>();

    /// \brief Human readable names for the bodies
    HashMap<String, Integer> mBodyNameMap = new HashMap<String, Integer>();

    public DModel() {
        DBody root_body = new DBody();
        DJoint root_joint = new DJoint();

        Vector3d zero_position = new Vector3d(0., 0., 0.);
        SpatialVector6d zero_spatial = new SpatialVector6d(0., 0., 0., 0., 0., 0.);

        // structural information
        lambda.add(0);
        mu.add(new ArrayList<Integer>());
        dof_count = 0;
        q_size = 0;
        qdot_size = 0;
        previously_added_body_id = 0;

        gravity = new Vector3d(0., -9.81, 0.);

        // state information
        v.add(new SpatialVector6d(zero_spatial));
        a.add(new SpatialVector6d(zero_spatial));

        // Joints
        mJoints.add(root_joint);
        S.add(new SpatialVector6d(zero_spatial));
        X_T.add(new SpatialTransform());

        // Spherical joints
        multdof3_S.add(Matrix63d.zero());
        multdof3_U.add(Matrix63d.zero());
        multdof3_Dinv.add(Matrix3d.zero());
        multdof3_u.add(Vector3d.zero());
        multdof3_w_index.add(0);

        // Dynamic variables
        c.add(new SpatialVector6d(zero_spatial));
        IA.add(SpatialMatrix6d.identity());
        pA.add(new SpatialVector6d(zero_spatial));
        U.add(new SpatialVector6d(zero_spatial));

        u = new ArrayRealVector(1);
        u.setEntry(0, 0);
        d = new ArrayRealVector(1);
        d.setEntry(0, 0);

        f.add(new SpatialVector6d(zero_spatial));
        Ic.add(new SpatialRigidBodyInertia(
                0.,
                new Vector3d(0., 0., 0.),
                new Matrix3d()
        )
        );
        hc.add(new SpatialVector6d(zero_spatial));

        // Bodies
        X_lambda.add(new SpatialTransform());
        X_base.add(new SpatialTransform());

        mBodies.add(root_body);
        mBodyNameMap.put("ROOT", 0);

        fixed_body_discriminator = Integer.MAX_VALUE / 2;

    }

    public int addBodyFixedJoint(
            int parent_id,
            SpatialTransform joint_frame,
            DJoint joint,
            DBody body,
            String body_name) {
        FixedBody fbody = FixedBody.createFromBody(body);
        fbody.mMovableParent = parent_id;
        fbody.mParentTransform = joint_frame;

        if (isFixedBodyId(parent_id)) {
            FixedBody fixed_parent = mFixedBodies.get(parent_id - fixed_body_discriminator);
            fbody.mMovableParent = fixed_parent.mMovableParent;
            fbody.mParentTransform = joint_frame.multiple(fixed_parent.mParentTransform);
        }

        // merge the two bodies
        DBody parent_body = mBodies.get(fbody.mMovableParent);
        parent_body.join(fbody.mParentTransform, body);
        mBodies.set(fbody.mMovableParent, parent_body);

        mFixedBodies.add(fbody);

        if (mFixedBodies.size() > Integer.MAX_VALUE - fixed_body_discriminator) {
            System.out.println("Error: cannot add more than " + (Integer.MAX_VALUE - mFixedBodies.size()) + " fixed bodies. You need to modify Model::fixed_body_discriminator for this.");
        }

        if (body_name.length() != 0) {
            if (mBodyNameMap.containsKey(body_name)) {
                System.out.println("Error: Body with name '" + body_name + "' already exists!");
            }
            mBodyNameMap.put(body_name, mFixedBodies.size() + fixed_body_discriminator - 1);
        }
        return mFixedBodies.size() + fixed_body_discriminator - 1;
    }

    public int addBodyMultiDofJoint(int parent_id, SpatialTransform joint_frame, DJoint joint, DBody body, String body_name) {
        // Here we emulate multi DoF joints by simply adding nullbodies. This
        // allows us to use fixed size elements for S,v,a, etc. which is very
        // fast in Eigen.
        int joint_count = 0;
        if (joint.mJointType == JointType.JointType1DoF) {
            joint_count = 1;
        } else if (joint.mJointType == JointType.JointType2DoF) {
            joint_count = 2;
        } else if (joint.mJointType == JointType.JointType3DoF) {
            joint_count = 3;
        } else if (joint.mJointType == JointType.JointType4DoF) {
            joint_count = 4;
        } else if (joint.mJointType == JointType.JointType5DoF) {
            joint_count = 5;
        } else if (joint.mJointType == JointType.JointType6DoF) {
            joint_count = 6;
        } else {
            System.out.println("Error: Invalid joint type: " + joint.mJointType);
            assert false;
        }

        DBody null_body = new DBody(0., new Vector3d(0., 0., 0.), new Vector3d(0., 0., 0.));
        null_body.mIsVirtual = true;

        int null_parent = parent_id;
        SpatialTransform joint_frame_transform = null;

        DJoint single_dof_joint = null;
        int j;

        // Here we add multiple virtual bodies that have no mass or inertia for
        // which each is attached to the model with a single degree of freedom
        // joint.
        for (j = 0; j < joint_count; j++) {
            Vector3d rotation = new Vector3d(
                    joint.mJointAxes[j].getEntry(0),
                    joint.mJointAxes[j].getEntry(1),
                    joint.mJointAxes[j].getEntry(2));
            Vector3d translation = new Vector3d(
                    joint.mJointAxes[j].getEntry(3),
                    joint.mJointAxes[j].getEntry(4),
                    joint.mJointAxes[j].getEntry(5));

            if (rotation == new Vector3d(0., 0., 0.)) {
                single_dof_joint = new DJoint(JointType.JointTypePrismatic, translation);
            } else if (translation == new Vector3d(0., 0., 0.)) {
                single_dof_joint = new DJoint(JointType.JointTypeRevolute, rotation);
            }

            // the first joint has to be transformed by joint_frame, all the
            // others must have a null transformation
            if (j == 0) {
                joint_frame_transform = joint_frame;
            } else {
                joint_frame_transform = new SpatialTransform();
            }

            if (j == joint_count - 1) // if we are at the last we must add the real body
            {
                break;
            } else {
                // otherwise we just add an intermediate body
                null_parent = addBody(null_parent, joint_frame_transform, single_dof_joint, null_body, "intermediate body");
            }
        }

        return addBody(null_parent, joint_frame_transform, single_dof_joint, body, body_name);
    }

    /**
     * \brief Connects a given body to the model
     *
     * When adding a body there are basically informations required: - what kind
     * of body will be added? - where is the new body to be added? - by what
     * kind of joint should the body be added?
     *
     * The first information "what kind of body will be added" is contained in
     * the Body class that is given as a parameter.
     *
     * The question "where is the new body to be added?" is split up in two
     * parts: first the parent (or successor) body to which it is added and
     * second the transformation to the origin of the joint that connects the
     * two bodies. With these two informations one specifies the relative
     * positions of the bodies when the joint is in neutral position.gk
     *
     * The last question "by what kind of joint should the body be added?" is
     * again simply contained in the Joint class.
     *
     * \param parent_id id of the parent body \param joint_frame the
     * transformation from the parent frame to the origin of the joint frame
     * (represents X_T in RBDA) \param joint specification for the joint that
     * describes the connection \param body specification of the body itself
     * \param body_name human readable name for the body (can be used to
     * retrieve its id with GetBodyId())
     *
     * \returns id of the added body
     */
    /***
     *
     * @param parent_id
     * @param joint_frame  local transform of the joint in the parent frame
     * @param joint
     * @param body
     * @param body_name
     * @return
     */
    public int addBody(int parent_id, SpatialTransform joint_frame, DJoint joint, DBody body, String body_name) {
        assert (lambda.size() > 0);
        assert (joint.mJointType != JointType.JointTypeUndefined);
        if (joint.mJointType == JointType.JointTypeFixed) {
            previously_added_body_id = addBodyFixedJoint(parent_id, joint_frame, joint, body, body_name);
            return previously_added_body_id;
        } else if ((joint.mJointType == JointType.JointTypeSpherical) || (joint.mJointType == JointType.JointTypeEulerZYX)) {
            // no action required
        } else if (joint.mJointType != JointType.JointTypePrismatic
                && joint.mJointType != JointType.JointTypeRevolute) {
            previously_added_body_id = addBodyMultiDofJoint(parent_id, joint_frame, joint, body, body_name);
            return previously_added_body_id;
        }

        // If we add the body to a fixed body we have to make sure that we
        // actually add it to its movable parent.
        int movable_parent_id = parent_id;
        SpatialTransform movable_parent_transform = new SpatialTransform();

        if (isFixedBodyId(parent_id)) {
            int fbody_id = parent_id - fixed_body_discriminator;
            movable_parent_id = mFixedBodies.get(fbody_id).mMovableParent;
            movable_parent_transform = mFixedBodies.get(fbody_id).mParentTransform;
        }

        // structural information
        lambda.add(movable_parent_id);
        mu.add(new ArrayList<Integer>());
        mu.get(movable_parent_id).add(mBodies.size());

        // Bodies
        X_lambda.add(new SpatialTransform());
        X_base.add(new SpatialTransform());
        mBodies.add(body);

        if (body_name.length() != 0) {
            if (mBodyNameMap.containsKey(body_name)) {
                System.out.println("Error: Body with name '" + body_name + "' already exists!");
                assert false;
            }
            mBodyNameMap.put(body_name, mBodies.size() - 1);
        }

        // state information
        v.add(new SpatialVector6d(0., 0., 0., 0., 0., 0.));
        a.add(new SpatialVector6d(0., 0., 0., 0., 0., 0.));

        // Joints
        int last_q_index = mJoints.size() - 1;
        mJoints.add(joint);
        mJoints.get(mJoints.size() - 1).q_index = mJoints.get(last_q_index).q_index + mJoints.get(last_q_index).mDoFCount;

        S.add(joint.mJointAxes[0]);

        // workspace for joints with 3 dof
        multdof3_S.add(Matrix63d.zero());
        multdof3_U.add(Matrix63d.zero());
        multdof3_Dinv.add(Matrix3d.zero());
        multdof3_u.add(Vector3d.zero());
        multdof3_w_index.add(0);

        dof_count = dof_count + joint.mDoFCount;

        // update the w components of the Quaternions. They are stored at the end
        // of the q vector
        int multdof3_joint_counter = 0;
        for (int i = 1; i < mJoints.size(); i++) {
            if (mJoints.get(i).mJointType == JointType.JointTypeSpherical) {
                multdof3_w_index.add(i, dof_count + multdof3_joint_counter);
                multdof3_joint_counter++;
            }
        }

        q_size = dof_count + multdof3_joint_counter;
        qdot_size = qdot_size + joint.mDoFCount;

        // we have to invert the transformation as it is later always used from the
        // child bodies perspective.
        X_T.add(joint_frame.multiple(movable_parent_transform));

        // Dynamic variables
        c.add(new SpatialVector6d(0., 0., 0., 0., 0., 0.));
        IA.add(body.mSpatialInertia);
        pA.add(new SpatialVector6d(0., 0., 0., 0., 0., 0.));
        U.add(new SpatialVector6d(0., 0., 0., 0., 0., 0.));

        d = new ArrayRealVector(mBodies.size());
        d.set(0);
        u = new ArrayRealVector(mBodies.size());
        u.set(0);

        f.add(new SpatialVector6d(0., 0., 0., 0., 0., 0.));
        Ic.add(new SpatialRigidBodyInertia(
                body.mMass,
                body.mCenterOfMass,
                body.mInertia
        )
        );
        hc.add(new SpatialVector6d(0., 0., 0., 0., 0., 0.));

        if (mBodies.size() == fixed_body_discriminator) {
            System.out.println("Error: cannot add more than " + fixed_body_discriminator + " movable bodies. You need to modify Model::fixed_body_discriminator for this.");
            assert false;
        }

        previously_added_body_id = mBodies.size() - 1;

        return previously_added_body_id;
    }

    public int appendBody(SpatialTransform joint_frame, DJoint joint, DBody body, String body_name) {
        return addBody(previously_added_body_id, joint_frame, joint, body, body_name);
    }

    /**
     * \brief Returns the id of a body that was passed to AddBody()
     *
     * Bodies can be given a human readable name. This function allows to
     * resolve its name to the numeric id.
     *
     * \note Instead of querying this function repeatedly, it might be advisable
     * to query it once and reuse the returned id.
     *
     * \returns the id of the body or \c std::numeric_limits<unsigned
     * int>::max() if the id was not found.
     */
    public int getBodyId(String body_name) {
        if (mBodyNameMap.containsKey(body_name)) {
            return Integer.MAX_VALUE;
        }
        return mBodyNameMap.get(body_name);
    }

    /**
     * \brief Returns the name of a body for a given body id
     */
    public String getBodyName(int body_id) {
        if (mBodyNameMap.containsValue(body_id)) {
            for (Map.Entry<String, Integer> entryv : mBodyNameMap.entrySet()) {
                if (entryv.getValue() == body_id) {
                    return entryv.getKey();
                }
            }
        }
        return "";
    }

    /**
     * \brief Checks whether the body is rigidly attached to another body.
     */
    public boolean isFixedBodyId(int body_id) {
        if (body_id >= fixed_body_discriminator
                && body_id < Integer.MAX_VALUE
                && body_id - fixed_body_discriminator < mFixedBodies.size()) {
            return true;
        }
        return false;
    }

    public boolean isBodyId(int id) {
        if (id > 0 && id < mBodies.size()) {
            return true;
        }
        if (id >= fixed_body_discriminator && id < Integer.MAX_VALUE) {
            if (id - fixed_body_discriminator < mFixedBodies.size()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines id the actual parent body.
     *
     * When adding bodies using joints with multiple degrees of freedom,
     * additional virtual bodies are added for each degree of freedom. This
     * function returns the id of the actual non-virtual parent body.
     */
    public int getParentBodyId(int id) {
        if (id >= fixed_body_discriminator) {
            return mFixedBodies.get(id - fixed_body_discriminator).mMovableParent;
        }

        int parent_id = lambda.get(id);

        while (mBodies.get(parent_id).mIsVirtual) {
            parent_id = lambda.get(parent_id);
        }
        return parent_id;
    }

    /**
     * Returns the joint frame transformtion, i.e. the second argument to
     * Model::AddBody().
     */
    public SpatialTransform getJointFrame(int id) {
        if (id >= fixed_body_discriminator) {
            return mFixedBodies.get(id - fixed_body_discriminator).mParentTransform;
        }

        int child_id = id;
        int parent_id = lambda.get(id);
        if (mBodies.get(parent_id).mIsVirtual) {
            while (mBodies.get(parent_id).mIsVirtual) {
                child_id = parent_id;
                parent_id = lambda.get(child_id);
            }
            return X_T.get(child_id);
        } else {
            return X_T.get(id);
        }
    }

    /**
     * Sets the joint frame transformtion, i.e. the second argument to
     * Model::AddBody().
     */
    public void setJointFrame(int id, SpatialTransform transform) {
        if (id >= fixed_body_discriminator) {
            System.out.println("Error: setting of parent transform not supported for fixed bodies!");
        }

        int child_id = id;
        int parent_id = lambda.get(id);
        if (mBodies.get(parent_id).mIsVirtual) {
            while (mBodies.get(parent_id).mIsVirtual) {
                child_id = parent_id;
                parent_id = lambda.get(child_id);
            }
            X_T.set(child_id, transform);
        } else if (id > 0) {
            X_T.set(id, transform);
        }
    }

    /**
     * \brief Computes all variables for a joint model
     *
     * By appropriate modification of this function all types of joints can be
     * modeled. See RBDA Section 4.4 for details.
     *
     * \param model the rigid body model \param joint_id the id of the joint we
     * are interested in (output) \param XJ the joint transformation (output)
     * \param v_J joint velocity (output) \param c_J joint acceleration for
     * rhenomic joints (output) \param q joint state variables \param qdot joint
     * velocity variables
     */
    /**
     * *
     *
     * @param model
     * @param joint_id
     * @param XJ joint transformation (output)
     * @param v_J joint velocity (output)
     * @param c_J joint acceleration for rhenomic joints (output)
     * @param q joint state variables
     * @param qdot velocity variables use joint state 3d to compute the vector6
     * transformation etc infos
     */
    public static void jcalc(DModel model, int joint_id, SpatialTransform XJ, SpatialVector6d v_J, SpatialVector6d c_J, ArrayRealVector q, ArrayRealVector qdot) {
        // exception if we calculate it for the root body
        assert (joint_id > 0);

        if (model.mJoints.get(joint_id).mDoFCount == 1) {
            XJ.set(jcalc_XJ(model, joint_id, q));  //compute joint transform

            // Set the joint axis
            model.S.set(joint_id, model.mJoints.get(joint_id).mJointAxes[0]);

            // the velocity dependent spatial acceleration is != 0 only for rhenomic
            // constraints (see RBDA, p. 55)
            c_J.setZero();

            v_J.set(model.S.get(joint_id).multiple(qdot.getEntry(model.mJoints.get(joint_id).q_index)));
        } else if (model.mJoints.get(joint_id).mJointType == JointTypeSpherical) {
            XJ.set(jcalc_XJ(model, joint_id, q));

            model.multdof3_S.get(joint_id).toZero();

            model.multdof3_S.get(joint_id).setEntry(0, 0, 1.);
            model.multdof3_S.get(joint_id).setEntry(1, 1, 1.);
            model.multdof3_S.get(joint_id).setEntry(2, 2, 1.);

            Vector3d omega = new Vector3d(qdot.getEntry(model.mJoints.get(joint_id).q_index),
                    qdot.getEntry(model.mJoints.get(joint_id).q_index + 1),
                    qdot.getEntry(model.mJoints.get(joint_id).q_index + 2));

            v_J.set(new SpatialVector6d(
                    omega.getEntry(0), omega.getEntry(1), omega.getEntry(2),
                    0., 0., 0.));

            c_J.setZero();
        } else if (model.mJoints.get(joint_id).mJointType == JointTypeEulerZYX) {
            double q0 = q.getEntry(model.mJoints.get(joint_id).q_index);
            double q1 = q.getEntry(model.mJoints.get(joint_id).q_index + 1);
            double q2 = q.getEntry(model.mJoints.get(joint_id).q_index + 2);

            double s0 = Math.sin(q0);
            double c0 = Math.cos(q0);
            double s1 = Math.sin(q1);
            double c1 = Math.cos(q1);
            double s2 = Math.sin(q2);
            double c2 = Math.cos(q2);

            XJ.E = new Matrix3d(
                    c0 * c1, s0 * c1, -s1,
                    c0 * s1 * s2 - s0 * c2, s0 * s1 * s2 + c0 * c2, c1 * s2,
                    c0 * s1 * c2 + s0 * s2, s0 * s1 * c2 - c0 * s2, c1 * c2
            );

            model.multdof3_S.get(joint_id).toZero();

            model.multdof3_S.get(joint_id).setEntry(0, 0, -s1);
            model.multdof3_S.get(joint_id).setEntry(0, 2, 1.);

            model.multdof3_S.get(joint_id).setEntry(1, 0, c1 * s2);
            model.multdof3_S.get(joint_id).setEntry(1, 1, c2);

            model.multdof3_S.get(joint_id).setEntry(2, 0, c1 * c2);
            model.multdof3_S.get(joint_id).setEntry(2, 1, -s2);

            double qdot0 = qdot.getEntry(model.mJoints.get(joint_id).q_index);
            double qdot1 = qdot.getEntry(model.mJoints.get(joint_id).q_index + 1);
            double qdot2 = qdot.getEntry(model.mJoints.get(joint_id).q_index + 2);

            Matrix63d mx = model.multdof3_S.get(joint_id);
            v_J.set(new SpatialVector6d(mx.operate(new Vector3d(qdot0, qdot1, qdot2))));

            c_J.set(-c1 * qdot0 * qdot1,
                    -s1 * s2 * qdot0 * qdot1 + c1 * c2 * qdot0 * qdot2 - s2 * qdot1 * qdot2,
                    -s1 * c2 * qdot0 * qdot1 - c1 * s2 * qdot0 * qdot2 - c2 * qdot1 * qdot2,
                    0., 0., 0.
            );
        } else {
            // Only revolute joints supported so far
            assert (false);
        }
    }

    /**
     *
     * @param model
     * @param joint_id
     * @param q
     * @return create the joint transformation (the angle x axis)
     */
    public static SpatialTransform jcalc_XJ(DModel model, int joint_id, ArrayRealVector q) {
        assert (joint_id > 0);
        if (model.mJoints.get(joint_id).mDoFCount == 1) {
            if (model.mJoints.get(joint_id).mJointType == JointTypeRevolute) {
                return SpatialTransform.rot(q.getEntry(model.mJoints.get(joint_id).q_index), new Vector3d(
                        model.mJoints.get(joint_id).mJointAxes[0].getEntry(0),
                        model.mJoints.get(joint_id).mJointAxes[0].getEntry(1),
                        model.mJoints.get(joint_id).mJointAxes[0].getEntry(2)
                ));
            } else if (model.mJoints.get(joint_id).mJointType == JointTypePrismatic) {
                return SpatialTransform.translate(new Vector3d(
                        model.mJoints.get(joint_id).mJointAxes[0].getEntry(3) * q.getEntry(model.mJoints.get(joint_id).q_index),
                        model.mJoints.get(joint_id).mJointAxes[0].getEntry(4) * q.getEntry(model.mJoints.get(joint_id).q_index),
                        model.mJoints.get(joint_id).mJointAxes[0].getEntry(5) * q.getEntry(model.mJoints.get(joint_id).q_index)
                )
                );
            }
            //} else if (model.mJoints.get(joint_id).mJointType == JointTypeSpherical) {
            //	return new SpatialTransform ( model.GetQuaternion (joint_id, q).toMatrix(), Vector3d (0., 0., 0.));
        } else if (model.mJoints.get(joint_id).mJointType == JointTypeEulerZYX) {
            double q0 = q.getEntry(model.mJoints.get(joint_id).q_index);
            double q1 = q.getEntry(model.mJoints.get(joint_id).q_index + 1);
            double q2 = q.getEntry(model.mJoints.get(joint_id).q_index + 2);

            double s0 = Math.sin(q0);
            double c0 = Math.cos(q0);
            double s1 = Math.sin(q1);
            double c1 = Math.cos(q1);
            double s2 = Math.sin(q2);
            double c2 = Math.cos(q2);

            return new SpatialTransform(new Matrix3d(
                    c0 * c1, s0 * c1, -s1,
                    c0 * s1 * s2 - s0 * c2, s0 * s1 * s2 + c0 * c2, c1 * s2,
                    c0 * s1 * c2 + s0 * s2, s0 * s1 * c2 - c0 * s2, c1 * c2
            ), new Vector3d(0., 0., 0.));
        }

        System.out.println("Error: invalid joint type!");
        return new SpatialTransform();
    }

   /***
    * compute
    * @param model
    * @param joint_id
    * @param q
    * to set the inverse transform, from the parent body to the current body transform   (usually transform is current to base)
    */
    public static void jcalc_X_lambda_S(DModel model, int joint_id, ArrayRealVector q) {
        // exception if we calculate it for the root body
        assert (joint_id > 0);

        if (model.mJoints.get(joint_id).mDoFCount == 1) {
            //Transformation from the parent body to the current body  =  joint transformation * parentToJoint
            model.X_lambda.set(joint_id, jcalc_XJ(model, joint_id, q).multiple(model.X_T.get(joint_id)));

            // Set the joint axis
            model.S.set(joint_id, model.mJoints.get(joint_id).mJointAxes[0]);
        } else if (model.mJoints.get(joint_id).mJointType == JointTypeSpherical) {
            model.X_lambda.set(joint_id, jcalc_XJ(model, joint_id, q).multiple(model.X_T.get(joint_id)));
            model.multdof3_S.get(joint_id).toZero();

            model.multdof3_S.get(joint_id).setEntry(0, 0, 1.);
            model.multdof3_S.get(joint_id).setEntry(1, 1, 1.);
            model.multdof3_S.get(joint_id).setEntry(2, 2, 1.);
        } else if (model.mJoints.get(joint_id).mJointType == JointTypeEulerZYX) {
            double q0 = q.getEntry(model.mJoints.get(joint_id).q_index);
            double q1 = q.getEntry(model.mJoints.get(joint_id).q_index + 1);
            double q2 = q.getEntry(model.mJoints.get(joint_id).q_index + 2);

            double s0 = Math.sin(q0);
            double c0 = Math.cos(q0);
            double s1 = Math.sin(q1);
            double c1 = Math.cos(q1);
            double s2 = Math.sin(q2);
            double c2 = Math.cos(q2);

            model.X_lambda.set(joint_id, new SpatialTransform(
                    new Matrix3d(
                            c0 * c1, s0 * c1, -s1,
                            c0 * s1 * s2 - s0 * c2, s0 * s1 * s2 + c0 * c2, c1 * s2,
                            c0 * s1 * c2 + s0 * s2, s0 * s1 * c2 - c0 * s2, c1 * c2
                    ), new Vector3d(0., 0., 0.)
            ).multiple(model.X_T.get(joint_id)));

            model.multdof3_S.get(joint_id).toZero();
            model.multdof3_S.get(joint_id).setEntry(0, 0, -s1);
            model.multdof3_S.get(joint_id).setEntry(0, 2, 1.);

            model.multdof3_S.get(joint_id).setEntry(1, 0, c1 * s2);
            model.multdof3_S.get(joint_id).setEntry(1, 1, c2);
            model.multdof3_S.get(joint_id).setEntry(2, 0, c1 * c2);
            model.multdof3_S.get(joint_id).setEntry(2, 1, -s2);
        } else {
            // Only revolute joints supported so far
            assert (false);
        }
    }

    public int getDofCount(){return dof_count;}
}
