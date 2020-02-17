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

import greta.core.animation.Skeleton;
import greta.core.animation.math.SpatialVector6d;
import greta.core.animation.math.Vector3d;
import greta.core.animation.rbdl.DBody;
import greta.core.animation.rbdl.DJoint;
import greta.core.animation.rbdl.DModel;
import greta.core.animation.rbdl.Dynamics;
import greta.core.animation.rbdl.HingeJoinController;
import greta.core.animation.rbdl.SpatialTransform;
import greta.core.util.Constants;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author Jing Huang
 */
public class RelaxArmDynamicsMotion {

    String _side = "left";
    double _start = 0;
    double _end = 0;
    double dt = 0.03;
    double _input_X_shoulder;
    ArrayList<Double> _output_X_shoulder = new ArrayList<Double>();

    double _input_X_elbow;
    ArrayList<Double> _output_X_elbow = new ArrayList<Double>();

    HingeJoinController _shouldercontroller = new HingeJoinController();
    HingeJoinController _elbowcontroller = new HingeJoinController();
    DModel _model;

    public RelaxArmDynamicsMotion(String side, Skeleton sk) {
        _side = side;
        _shouldercontroller.setDesireAngle(0.1);
        _shouldercontroller.setkProportional(1000);
        _shouldercontroller.setkDerivative(800);
        _shouldercontroller.setFriction(800);

        _elbowcontroller.setDesireAngle(-0.5);
        _elbowcontroller.setkProportional(1000);
        _elbowcontroller.setkDerivative(100);
        _elbowcontroller.setFriction(500);
        setSkeleton(sk);
    }

    public void setSkeleton(Skeleton sk) {
        _model = new DModel();
        Vec3d posShoulder = null;
        Vec3d posElbow = null;
        Vec3d posWrist = null;
        if (_side.equalsIgnoreCase("left")) {
            greta.core.animation.Joint j_shoulder = sk.getJoint("l_shoulder");
            posShoulder = j_shoulder.getWorldPosition();
            greta.core.animation.Joint j_elbow = sk.getJoint("l_elbow");
            posElbow = j_elbow.getWorldPosition();
            greta.core.animation.Joint j_wrist = sk.getJoint("l_wrist");
            posWrist = j_wrist.getWorldPosition();

        } else {
            greta.core.animation.Joint j_shoulder = sk.getJoint("r_shoulder");
            posShoulder = j_shoulder.getWorldPosition();
            greta.core.animation.Joint j_elbow = sk.getJoint("r_elbow");
            posElbow = j_elbow.getWorldPosition();
            greta.core.animation.Joint j_wrist = sk.getJoint("r_wrist");
            posWrist = j_wrist.getWorldPosition();
        }
        double length0 = Vec3d.substraction(posShoulder, posElbow).length();
        Vec3d centerMassBody0 = Vec3d.addition(posShoulder, posElbow);
        centerMassBody0.divide(2);
        double length1 = Vec3d.substraction(posElbow, posWrist).length();
        Vec3d centerMassBody1 = Vec3d.addition(posWrist, posElbow);
        centerMassBody1.divide(2);
        DBody body0 = new DBody(10, new Vector3d(centerMassBody0.x() - posShoulder.x(), centerMassBody0.y() - posShoulder.y(), centerMassBody0.z() - posShoulder.z()), new Vector3d(1, length0, 1));
        DJoint joint0 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
        _model.addBody(0, SpatialTransform.translate(new Vector3d(posShoulder.x(), posShoulder.y(), posShoulder.z())), joint0, body0, "testbody0");

        DBody body1 = new DBody(10, new Vector3d(centerMassBody1.x() - posElbow.x(), centerMassBody1.y() - posElbow.y(), centerMassBody1.z() - posElbow.z()), new Vector3d(1, length1, 1));
        DJoint joint1 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
        _model.addBody(1, SpatialTransform.translate(new Vector3d(posElbow.x() - posShoulder.x(), posElbow.y() - posShoulder.y(), posElbow.z() - posShoulder.z())), joint1, body1, "testbody1");
    }

    public void generate() {
        int framenb = (int) ((_end - _start) * Constants.FRAME_PER_SECOND);

        ArrayRealVector q = new ArrayRealVector(2);
        q.setEntry(0, _input_X_shoulder);
        _shouldercontroller.setInitialAngle(_input_X_shoulder, _input_X_shoulder);
        q.setEntry(1, _input_X_elbow);
        _elbowcontroller.setInitialAngle(_input_X_elbow, _input_X_elbow);
        ArrayRealVector qDot = new ArrayRealVector(_model.getDofCount());
        ArrayRealVector out_qDDot = new ArrayRealVector(_model.getDofCount());
        ArrayRealVector tau = new ArrayRealVector(_model.getDofCount());
        ArrayList<SpatialVector6d> f_ext = new ArrayList<SpatialVector6d>();
        ArrayRealVector tau2 = new ArrayRealVector(_model.getDofCount());

        double elbow = 0;
        double shoulder = 0;
        _output_X_shoulder.clear();
        _output_X_elbow.clear();
        // Dynamics.inverseDynamics(_model, q, qDot, out_qDDot, tau,  f_ext);
        for (int i = 0; i < framenb; ++i) {
            for (int n = 0; n < 4; ++n) {
                generateOneFrame(q, qDot, tau, out_qDDot, f_ext);
                if (n == 0) {
                    elbow = q.getEntry(1);
                    _output_X_elbow.add(elbow);
                    shoulder = q.getEntry(0);
                    _output_X_shoulder.add(shoulder);
                }
            }
        }

    }

    public void generateOneFrame(ArrayRealVector q, ArrayRealVector qDot, ArrayRealVector tau, ArrayRealVector out_qDDot, ArrayList<SpatialVector6d> f_ext) {
        //Dynamics.inverseDynamics(_model, q, qDot, out_qDDot, tau2,  f_ext);
        Dynamics.forwardDynamics(_model, q, qDot, tau, out_qDDot, f_ext);
        qDot = qDot.add(out_qDDot.mapMultiply(dt));
        q = q.add(qDot.mapMultiply(dt));
        out_qDDot.set(0);

        _shouldercontroller.setCurrentAngle(q.getEntry(0));
        _shouldercontroller.update(dt);
        double tauShoulder = _shouldercontroller.getTorqueOutput();
        _elbowcontroller.setCurrentAngle(q.getEntry(1));
        _elbowcontroller.update(dt);
        double tauElbow = _elbowcontroller.getTorqueOutput();
        tau.setEntry(0, tauShoulder);
        tau.setEntry(1, tauElbow);
    }

    public void setTime(double _start, double _end) {
        this._start = _start;
        this._end = _end;
    }

    public void setInputs(double _input_X_shoulder, double _input_X_elbow) {
        this._input_X_shoulder = _input_X_shoulder;
        this._input_X_elbow = _input_X_elbow;
    }

    public ArrayList<Double> getOutput_X_shoulder() {
        return _output_X_shoulder;
    }

    public ArrayList<Double> getOutput_X_elbow() {
        return _output_X_elbow;
    }

    public void setDesireAngles(double shoulder, double elbow) {
        _shouldercontroller.setDesireAngle(shoulder);
        _elbowcontroller.setDesireAngle(elbow);
    }
}
