/*
 * This file is part of Greta.
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
package greta.core.animation.performer;

import greta.core.animation.Joint;
import greta.core.animation.Skeleton;
import greta.core.animation.math.SpatialVector6d;
import greta.core.animation.math.Vector3d;
import greta.core.animation.rbdl.DBody;
import greta.core.animation.rbdl.DJoint;
import greta.core.animation.rbdl.DModel;
import greta.core.animation.rbdl.Dynamics;
import greta.core.animation.rbdl.HingeJoinController;
import greta.core.animation.rbdl.SpatialTransform;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author Jing Huang
 */
public class ArmsRelativeTorsoDynamicsMotion {

    public enum Mode {

        DEFAUT_DYNAMICS,
        TORSO_ROOT_KINEMATICS,
    };

    DModel _modelTorso;
    DModel _leftArmModel;
    DModel _rightArmModel;
    DModel _headModel;

    HingeJoinController _vt10_X_controller = new HingeJoinController();
    HingeJoinController _vt10_Y_controller = new HingeJoinController();
    HingeJoinController _vt10_Z_controller = new HingeJoinController();
    HingeJoinController _vl4_X_controller = new HingeJoinController();
    HingeJoinController _vl4_Y_controller = new HingeJoinController();
    HingeJoinController _vl4_Z_controller = new HingeJoinController();
    double dt = 0.03;
    Mode _mode = Mode.DEFAUT_DYNAMICS;

    public ArmsRelativeTorsoDynamicsMotion(Skeleton sk) {
        setSkeleton(sk);
    }

    public void setSkeleton(Skeleton sk) {
        //        Joint l_shoulder = sk.getJoint("l_shoulder");
//        Vec3d pos_l_shoulder = l_shoulder.getWorldPosition();
//        Joint l_elbow = sk.getJoint("l_elbow");
//        Vec3d pos_l_elbow = l_elbow.getWorldPosition();
//        Joint l_wrist = sk.getJoint("l_wrist");
//        Vec3d pos_l_wrist = l_wrist.getWorldPosition();
//        Joint r_shoulder = sk.getJoint("r_shoulder");
//        Vec3d pos_r_shoulder = r_shoulder.getWorldPosition();
//        Joint r_elbow = sk.getJoint("r_elbow");
//        Vec3d pos_r_elbow = r_elbow.getWorldPosition();
//        Joint r_wrist = sk.getJoint("r_wrist");
//        Vec3d pos_r_wrist = r_wrist.getWorldPosition();

        createBodyModel(sk);
    }

    void createBodyModel(Skeleton sk) {
        _modelTorso = new DModel();

        Joint vt1 = sk.getJoint("vt1");
        Vec3d pos_vt1 = vt1.getWorldPosition();
        Joint vt10 = sk.getJoint("vt10");
        Vec3d pos_vt10 = vt10.getWorldPosition();
        Joint vl4 = sk.getJoint("vl4");
        Vec3d pos_vl4 = vl4.getWorldPosition();

        Vec3d centerMassBody0 = Vec3d.substraction(pos_vt10, pos_vl4);
        double length0 = centerMassBody0.length();
        centerMassBody0.divide(2);
        DBody body0 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint0 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 0, 1));
        _modelTorso.addBody(0, SpatialTransform.translate(new Vector3d(pos_vl4.x(), pos_vl4.y(), pos_vl4.z())), joint0, body0, "vl4Z");

        DBody body01 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint01 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 1, 0));
        _modelTorso.addBody(1, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint01, body01, "vl4Y");

        DBody body02 = new DBody(10, new Vector3d(centerMassBody0.x(), centerMassBody0.y(), centerMassBody0.z()), new Vector3d(1, length0, 1));
        DJoint joint02 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
        _modelTorso.addBody(2, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint02, body02, "vl4X");

        Vec3d centerMassBody1 = Vec3d.substraction(pos_vt1, pos_vt10);
        double length1 = centerMassBody1.length();
        centerMassBody1.divide(2);
        DBody body1 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint1 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 0, 1));
        _modelTorso.addBody(3, SpatialTransform.translate(new Vector3d(pos_vt10.x() - pos_vl4.x(), pos_vt10.y() - pos_vl4.y(), pos_vt10.z() - pos_vl4.z())), joint1, body1, "vt10Z");

        DBody body11 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint11 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 1, 0));
        _modelTorso.addBody(4, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint11, body11, "vt10Y");

        DBody body12 = new DBody(10, new Vector3d(centerMassBody1.x(), centerMassBody1.y(), centerMassBody1.z()), new Vector3d(1, length1, 1));
        DJoint joint12 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
        _modelTorso.addBody(5, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint12, body12, "vt10X");
    }

    void createArmModel(Skeleton sk) {
        _leftArmModel = new DModel();
        {
            Vec3d posShoulder = null;
            Vec3d posElbow = null;
            Vec3d posWrist = null;
            greta.core.animation.Joint j_shoulder = sk.getJoint("l_shoulder");
            posShoulder = j_shoulder.getWorldPosition();
            greta.core.animation.Joint j_elbow = sk.getJoint("l_elbow");
            posElbow = j_elbow.getWorldPosition();
            greta.core.animation.Joint j_wrist = sk.getJoint("l_wrist");
            posWrist = j_wrist.getWorldPosition();


            Vec3d centerMassBody0 = Vec3d.substraction(posShoulder, posElbow);
            double length0 = centerMassBody0.length();
            centerMassBody0.divide(2);
            Vec3d centerMassBody1 = Vec3d.substraction(posElbow, posWrist);
            double length1 = centerMassBody1.length();
            centerMassBody1.divide(2);

            DBody body0 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
            DJoint joint0 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 0, 1));
            _leftArmModel.addBody(0, SpatialTransform.translate(new Vector3d(posShoulder.x(), posShoulder.y(), posShoulder.z())), joint0, body0, "shoulderZ");

            DBody body01 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
            DJoint joint01 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 1, 0));
            _leftArmModel.addBody(1, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint01, body01, "shoulderY");

            DBody body02 = new DBody(10, new Vector3d(centerMassBody0.x(), centerMassBody0.y(), centerMassBody0.z()), new Vector3d(1, length0, 1));
            DJoint joint02 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
            _leftArmModel.addBody(2, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint02, body02, "shoulderX");


            DBody body1 = new DBody(10, new Vector3d(centerMassBody1.x(), centerMassBody1.y(), centerMassBody1.z()), new Vector3d(1, length1, 1));
            DJoint joint1 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
            _leftArmModel.addBody(1, SpatialTransform.translate(new Vector3d(posElbow.x() - posShoulder.x(), posElbow.y() - posShoulder.y(), posElbow.z() - posShoulder.z())), joint1, body1, "elbowX");

        }

        _rightArmModel = new DModel();
        {
            Vec3d posShoulder = null;
            Vec3d posElbow = null;
            Vec3d posWrist = null;
            greta.core.animation.Joint j_shoulder = sk.getJoint("r_shoulder");
            posShoulder = j_shoulder.getWorldPosition();
            greta.core.animation.Joint j_elbow = sk.getJoint("r_elbow");
            posElbow = j_elbow.getWorldPosition();
            greta.core.animation.Joint j_wrist = sk.getJoint("r_wrist");
            posWrist = j_wrist.getWorldPosition();


            Vec3d centerMassBody0 = Vec3d.substraction(posShoulder, posElbow);
            double length0 = centerMassBody0.length();
            centerMassBody0.divide(2);
            Vec3d centerMassBody1 = Vec3d.substraction(posElbow, posWrist);
            double length1 = centerMassBody1.length();
            centerMassBody1.divide(2);

            DBody body0 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
            DJoint joint0 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 0, 1));
            _rightArmModel.addBody(0, SpatialTransform.translate(new Vector3d(posShoulder.x(), posShoulder.y(), posShoulder.z())), joint0, body0, "shoulderZ");

            DBody body01 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
            DJoint joint01 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 1, 0));
            _rightArmModel.addBody(1, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint01, body01, "shoulderY");

            DBody body02 = new DBody(10, new Vector3d(centerMassBody0.x(), centerMassBody0.y(), centerMassBody0.z()), new Vector3d(1, length0, 1));
            DJoint joint02 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
            _rightArmModel.addBody(2, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint02, body02, "shoulderX");


            DBody body1 = new DBody(10, new Vector3d(centerMassBody1.x(), centerMassBody1.y(), centerMassBody1.z()), new Vector3d(1, length1, 1));
            DJoint joint1 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
            _rightArmModel.addBody(1, SpatialTransform.translate(new Vector3d(posElbow.x() - posShoulder.x(), posElbow.y() - posShoulder.y(), posElbow.z() - posShoulder.z())), joint1, body1, "elbowX");

        }
    }

    void createHeadModel(Skeleton sk) {
        _headModel = new DModel();
        Joint vc7 = sk.getJoint("vc7");
        Vec3d pos_vc7 = vc7.getWorldPosition();
        Joint vc4 = sk.getJoint("vc4");
        Vec3d pos_vc4 = vc4.getWorldPosition();
        Joint skullbase = sk.getJoint("skullbase");
        Vec3d pos_skullbase = skullbase.getWorldPosition();

        Vec3d centerMassBody0 = Vec3d.substraction(pos_vc4, pos_vc7);
        double length0 = centerMassBody0.length();
        centerMassBody0.divide(2);
        DBody body0 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint0 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 0, 1));
        _headModel.addBody(0, SpatialTransform.translate(new Vector3d(pos_vc7.x(), pos_vc7.y(), pos_vc7.z())), joint0, body0, "vc7Z");

        DBody body01 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint01 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 1, 0));
        _headModel.addBody(1, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint01, body01, "vc7Y");

        DBody body02 = new DBody(10, new Vector3d(centerMassBody0.x(), centerMassBody0.y(), centerMassBody0.z()), new Vector3d(1, length0, 1));
        DJoint joint02 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
        _headModel.addBody(2, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint02, body02, "vc7X");

        Vec3d centerMassBody1 = Vec3d.substraction(pos_skullbase, pos_vc4);
        double length1 = centerMassBody1.length();
        centerMassBody1.divide(2);
        DBody body1 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint1 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 0, 1));
        _headModel.addBody(3, SpatialTransform.translate(new Vector3d(pos_skullbase.x() - pos_vc4.x(), pos_skullbase.y() - pos_vc4.y(), pos_skullbase.z() - pos_vc4.z())), joint1, body1, "vc4Z");

        DBody body11 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint11 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 1, 0));
        _headModel.addBody(4, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint11, body11, "vc4Y");

        DBody body12 = new DBody(10, new Vector3d(centerMassBody1.x(), centerMassBody1.y(), centerMassBody1.z()), new Vector3d(1, length1, 1));
        DJoint joint12 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
        _headModel.addBody(5, SpatialTransform.translate(new Vector3d(0, 0, 0)), joint12, body12, "vc4X");
    }

    public void generate() {
        int framenb = 100;
        ArrayRealVector q = new ArrayRealVector(_modelTorso.getDofCount());
        ArrayRealVector qDot = new ArrayRealVector(_modelTorso.getDofCount());
        ArrayRealVector out_qDDot = new ArrayRealVector(_modelTorso.getDofCount());
        ArrayRealVector tau = new ArrayRealVector(_modelTorso.getDofCount());
        ArrayList<SpatialVector6d> f_ext = new ArrayList<SpatialVector6d>();

        for (int i = 0; i < framenb; ++i) {
            for (int n = 0; n < 4; ++n) {
                generateOneFrame(q, qDot, tau, out_qDDot, f_ext);
                if (n == 0) {
//                    elbow = q.getEntry(1);
//                    _output_X_elbow.add(elbow);
//                    shoulder = q.getEntry(0);
//                    _output_X_shoulder.add(shoulder);
                }
            }
        }

    }

    public void generateOneFrame(ArrayRealVector q, ArrayRealVector qDot, ArrayRealVector tau, ArrayRealVector out_qDDot, ArrayList<SpatialVector6d> f_ext) {
        if (_mode == Mode.DEFAUT_DYNAMICS) {

            //Dynamics.inverseDynamics(_model, q, qDot, out_qDDot, tau2,  f_ext);
            Dynamics.forwardDynamics(_modelTorso, q, qDot, tau, out_qDDot, f_ext);
            qDot = qDot.add(out_qDDot.mapMultiply(dt));
            q = q.add(qDot.mapMultiply(dt));
            out_qDDot.set(0);

            {
                _vl4_Z_controller.setCurrentAngle(q.getEntry(0));
                _vl4_Z_controller.update(dt);
                double t = _vl4_Z_controller.getTorqueOutput();
                tau.setEntry(0, t);
            }
            {
                _vl4_Y_controller.setCurrentAngle(q.getEntry(1));
                _vl4_Y_controller.update(dt);
                double t = _vl4_Y_controller.getTorqueOutput();
                tau.setEntry(1, t);
            }
            {
                _vl4_X_controller.setCurrentAngle(q.getEntry(2));
                _vl4_X_controller.update(dt);
                double t = _vl4_X_controller.getTorqueOutput();
                tau.setEntry(2, t);
            }
            {
                _vt10_Z_controller.setCurrentAngle(q.getEntry(3));
                _vt10_Z_controller.update(dt);
                double t = _vt10_Z_controller.getTorqueOutput();
                tau.setEntry(3, t);
            }
            {
                _vt10_Y_controller.setCurrentAngle(q.getEntry(4));
                _vt10_Y_controller.update(dt);
                double t = _vt10_Y_controller.getTorqueOutput();
                tau.setEntry(4, t);
            }
            {
                _vt10_X_controller.setCurrentAngle(q.getEntry(5));
                _vt10_X_controller.update(dt);
                double t = _vt10_X_controller.getTorqueOutput();
                tau.setEntry(5, t);
            }

        } else if (_mode == Mode.TORSO_ROOT_KINEMATICS) {

            Dynamics.forwardDynamics(_modelTorso, q, qDot, tau, out_qDDot, f_ext);
            qDot = qDot.add(out_qDDot.mapMultiply(dt));
            q = q.add(qDot.mapMultiply(dt));
            out_qDDot.set(0);

            {
                _vt10_Z_controller.setCurrentAngle(q.getEntry(3));
                _vt10_Z_controller.update(dt);
                double t = _vt10_Z_controller.getTorqueOutput();
                tau.setEntry(3, t);
            }
            {
                _vt10_Y_controller.setCurrentAngle(q.getEntry(4));
                _vt10_Y_controller.update(dt);
                double t = _vt10_Y_controller.getTorqueOutput();
                tau.setEntry(4, t);
            }
            {
                _vt10_X_controller.setCurrentAngle(q.getEntry(5));
                _vt10_X_controller.update(dt);
                double t = _vt10_X_controller.getTorqueOutput();
                tau.setEntry(5, t);
            }

        }
    }
}
