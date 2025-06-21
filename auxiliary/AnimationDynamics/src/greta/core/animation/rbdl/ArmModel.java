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

import greta.core.animation.math.SpatialVector6d;
import greta.core.animation.math.Vector3d;
import java.util.ArrayList;
import org.apache.commons.math3.linear.ArrayRealVector;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class ArmModel {
    DModel _model = new DModel();
    double[] _lastFrameQ = new double[4];
    double[] _lastFrameDQ = new double[4];
    double[] _dq = new double[4];
    double[] _ddq = new double[4];
    double _dt = 0.04;
    boolean first = true;

    public ArmModel(){
        DBody body = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 0, 1));
        _model.addBody(0, SpatialTransform.translate(new Vector3d()), joint, body, "shoulderZ");

        DBody body1 = new DBody(0, new Vector3d(0, 0, 0), new Vector3d(1, 1, 1));
        DJoint joint1 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(0, 1, 0));
        _model.addBody(1, SpatialTransform.translate(new Vector3d(0,0,0)), joint1, body1, "shoulderY");

        DBody body2 = new DBody(5, new Vector3d(0, 2, 0), new Vector3d(1, 1, 1));
        DJoint joint2 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
        _model.addBody(2, SpatialTransform.translate(new Vector3d(0,0,0)), joint2, body2, "shoulderX");

        DBody body3 = new DBody(3, new Vector3d(0, 2, 0), new Vector3d(1, 1, 1));
        DJoint joint3 = new DJoint(DJoint.JointType.JointTypeRevolute, new Vector3d(1, 0, 0));
        _model.addBody(3, SpatialTransform.translate(new Vector3d(0,4,0)), joint3, body3, "elbowX");

    }
    public void reset(){
        first = true;
        _lastFrameQ = new double[4];
        _lastFrameDQ = new double[4];
        _dq = new double[4];
        _ddq = new double[4];
    }

    ArrayList<SpatialVector6d> fx = new ArrayList<SpatialVector6d>();

    void computeTorque(ArrayRealVector q, ArrayRealVector qDot, ArrayRealVector qDDot, ArrayRealVector out_tau){
        Dynamics.inverseDynamics(_model, q, qDot, qDDot, out_tau, fx);
    }

    public double[] computeShoulderTorque(double[] currentFrame){
        for(int i = 0; i < _lastFrameQ.length; ++i){
            _dq[i] = (currentFrame[i] - _lastFrameQ[i]) / _dt;
        }

        for(int i = 0; i < _lastFrameQ.length; ++i){
            _ddq[i] = (_dq[i] - _lastFrameDQ[i]) / _dt;
        }

        ArrayRealVector q = new ArrayRealVector(currentFrame);
        ArrayRealVector qD = new ArrayRealVector(_dq);
        ArrayRealVector qDD = new ArrayRealVector(_ddq);
        ArrayRealVector qT = new ArrayRealVector(4);

        //System.out.println(q.getEntry(2) + " "+ qD.getEntry(2) +" "+qDD.getEntry(2) +" "+qT.getEntry(2) +" ");
        //System.out.println(qD.getEntry(2) +" ");
        for(int i = 0; i < _lastFrameQ.length; ++i){
            _lastFrameQ[i] = currentFrame[i];
            _lastFrameDQ[i] = _dq[i];
        }
        if(first){
            first=false;
            return qT.toArray();
        }
        computeTorque(q, qD, qDD, qT);
        return qT.toArray();
    }

    public double[] getLastFrameQ() {
        return _lastFrameQ;
    }

    public double[] getLastFrameDQ() {
        return _lastFrameDQ;
    }

    public double[] getDq() {
        return _dq;
    }

    public double[] getDdq() {
        return _ddq;
    }

    public double getDt() {
        return _dt;
    }


    public static void main(String[] arg){
        ArmModel arm = new ArmModel();
        for(int i = 1; i < 5; i++ ){
            double[] f = new double[]{0,0,0.1 * i,0};
            double[] t = arm.computeShoulderTorque(f);
            //System.out.println(t[2]);
        }
    }
}
