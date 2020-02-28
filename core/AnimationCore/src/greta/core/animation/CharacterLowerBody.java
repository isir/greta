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
package greta.core.animation;

import greta.core.util.math.MatrixMN;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class CharacterLowerBody {

    Skeleton _sk_original = null;
    Skeleton _leftleg, _rightleg;
    Vec3d _rootOffset = new Vec3d();
    Vec3d _l_ankleOffset = new Vec3d();
    Vec3d _r_ankleOffset = new Vec3d();

    Joint l_hip;
    Joint l_knee;
    Joint l_ankle;
    Joint r_hip;
    Joint r_knee;
    Joint r_ankle;

    int maxLoop = 500;
    double thresholdDis = 0.01f;
    double beta = 3f;
    double stepRange = 0.01f;

    HashMap<String, Quaternion> _rotations = new HashMap<String, Quaternion>();

    public CharacterLowerBody() {
        _leftleg = new Skeleton("leftleg");
         l_hip = _leftleg.createJoint("l_hip", -1);
         l_knee = _leftleg.createJoint("l_knee", 0);
         l_ankle = _leftleg.createJoint("l_ankle", 1);


        _rightleg = new Skeleton("rightleg");
         r_hip = _rightleg.createJoint("r_hip", -1);
         r_knee = _rightleg.createJoint("r_knee", 0);
         r_ankle = _rightleg.createJoint("r_ankle", 1);

    }

    public void setSkeleton(Skeleton sk) {
        _sk_original = sk.clone();
        l_hip.setLocalPosition(sk.getJoint("l_hip").getWorldPosition());
        l_knee.setLocalPosition(sk.getJoint("l_knee").getLocalPosition());
        l_ankle.setLocalPosition(sk.getJoint("l_ankle").getLocalPosition());
        r_hip.setLocalPosition(sk.getJoint("r_hip").getWorldPosition());
        r_knee.setLocalPosition(sk.getJoint("r_knee").getLocalPosition());
        r_ankle.setLocalPosition(sk.getJoint("r_ankle").getLocalPosition());

        _leftleg.update();
        _rightleg.update();
    }

    public void computeAnalytical(){
        Vec3d p_rooto = _sk_original.getJoint("HumanoidRoot").getWorldPosition();
        Vec3d p_l_hipo = _sk_original.getJoint("l_hip").getWorldPosition();
        Vec3d p_r_hipo = _sk_original.getJoint("r_hip").getWorldPosition();
        Vec3d p_l_kneeo = _sk_original.getJoint("l_knee").getWorldPosition();
        Vec3d p_r_kneeo = _sk_original.getJoint("r_knee").getWorldPosition();
        Vec3d p_l_ankleo = _sk_original.getJoint("l_ankle").getWorldPosition();
        Vec3d p_r_ankleo = _sk_original.getJoint("r_ankle").getWorldPosition();

        Vec3d p_root = Vec3d.addition(p_rooto, _rootOffset);
        Vec3d centerHipO = Vec3d.addition(p_l_hipo, p_r_hipo);
        centerHipO.divide(2);
        Vec3d centerHip = null;
        Vec3d p_r_hip = null;
        {
            //right hip
            Vec3d xO = Vec3d.substraction(centerHipO, p_r_hipo);
            Vec3d zO = new Vec3d(0,0,1);
            Vec3d yO = Vec3d.cross3(zO, xO);

            Vec3d x = Vec3d.substraction(centerHip, p_r_hip);
            Vec3d z = new Vec3d(0,0,1);
            Vec3d y = Vec3d.cross3(z, x);

        }
    }

    public void compute(){
        //left
        {
            Vec3d currentEndL = l_ankle.getWorldPosition();// Vec3d.substraction(l_ankle.getWorldPosition(), _rootOffset);
            Vec3d targetL = Vec3d.substraction(Vec3d.addition(l_ankle.getWorldPosition(),  _l_ankleOffset), _rootOffset);
            int loop = 0;
            MatrixMN jacobian = new MatrixMN(3, 2);
            MatrixMN t = new MatrixMN(3, 1);
            Vec3d distance = Vec3d.substraction(_l_ankleOffset, _rootOffset);
             while (distance.length() > thresholdDis && loop < maxLoop) {
                t.elements[0][0] = beta * distance.x();
                t.elements[1][0] = beta * distance.y();
                t.elements[2][0] = beta * distance.z();

                Vec3d[] axis = new Vec3d[2];
                {
                    Joint current = l_hip;
                    Vec3d currentPos = current.getPosition();
                    Vec3d ve = Vec3d.substraction(currentEndL, currentPos);
                    ve.normalize();
                    Vec3d vg = Vec3d.substraction(targetL, currentPos);
                    vg.normalize();
                    axis[0] = Vec3d.cross3(ve, vg);
                    axis[0].normalize();
                    Vec3d xyz = Vec3d.cross3(axis[0], ve);
                    xyz.normalize();
                    jacobian.elements[0][0] = xyz.x();
                    jacobian.elements[1][0] = xyz.y();
                    jacobian.elements[2][0] = xyz.z();
                    axis[0] = Quaternion.multiplication(current.getWorldOrientation().inverse(), axis[0]);
                }
                 {
                    Joint current = l_knee;
                    Vec3d currentPos = current.getPosition();
                    Vec3d ve = Vec3d.substraction(currentEndL, currentPos);
                    ve.normalize();
                    Vec3d vg = Vec3d.substraction(targetL, currentPos);
                    vg.normalize();

                    axis[1] = new Vec3d(1,0,0);
                    axis[1].normalize();
                    //axis[1] = Vec3d.cross3(ve, vg);
                    //axis[1].normalize();

                    Vec3d xyz = Vec3d.cross3(axis[1], ve);
                    xyz.normalize();
                    jacobian.elements[0][1] = xyz.x();
                    jacobian.elements[1][1] = xyz.y();
                    jacobian.elements[2][1] = xyz.z();

                    axis[1] = Quaternion.multiplication(current.getWorldOrientation().inverse(), axis[1]);
                }

                MatrixMN transpose = jacobian.transpose();
                MatrixMN rotations = transpose.multiply(t);

                {
                    Joint current = l_hip;
                    double r = rotations.elements[0][0];
                    Quaternion q = new Quaternion();
                    q.setAxisAngle(axis[0], (double) r * stepRange);
                    q.normalize();
                    current.rotate(q);
                    current.update();
                }
                {
                    Joint current = l_knee;
                    double r = rotations.elements[1][0];
                    Quaternion q = new Quaternion();
                    q.setAxisAngle(axis[1], (double) r * stepRange);
                    q.normalize();
                    current.rotate(q);
                    current.update();
                    applyConstraint(current, 0f, 3.14f,0.01f,0.00f,0.01f,0.00f);
                    current.update();
                }

                _leftleg.update();
                currentEndL = l_ankle.getPosition();
                distance.set(targetL.x() - currentEndL.x(), targetL.y() - currentEndL.y(), targetL.z() - currentEndL.z());
                 //System.out.println("distance: " +distance.length());
                ++loop;
            }
            // System.out.println("loop left: " +loop);
             _rotations.put("l_hip", l_hip.getLocalRotation());
             _rotations.put("l_knee", l_knee.getLocalRotation());

             _rotations.put("l_ankle", l_ankle.getWorldOrientation().inverse());
        }


        //right
        {
            Vec3d currentEndR = r_ankle.getWorldPosition();// Vec3d.substraction(r_ankle.getWorldPosition(), _rootOffset);
            Vec3d targetR = Vec3d.substraction(Vec3d.addition(r_ankle.getWorldPosition(),  _r_ankleOffset), _rootOffset);
            int loop = 0;
            MatrixMN jacobian = new MatrixMN(3, 2);
            MatrixMN t = new MatrixMN(3, 1);
            Vec3d distance = Vec3d.substraction(_r_ankleOffset, _rootOffset);
             while (distance.length() > thresholdDis && loop < maxLoop) {
                t.elements[0][0] = beta * distance.x();
                t.elements[1][0] = beta * distance.y();
                t.elements[2][0] = beta * distance.z();

                Vec3d[] axis = new Vec3d[2];
                {
                    Joint current = r_hip;
                    Vec3d currentPos = current.getPosition();
                    Vec3d ve = Vec3d.substraction(currentEndR, currentPos);
                    ve.normalize();
                    Vec3d vg = Vec3d.substraction(targetR, currentPos);
                    vg.normalize();
                    axis[0] = Vec3d.cross3(ve, vg);
                    axis[0].normalize();
                    Vec3d xyz = Vec3d.cross3(axis[0], ve);
                    xyz.normalize();
                    jacobian.elements[0][0] = xyz.x();
                    jacobian.elements[1][0] = xyz.y();
                    jacobian.elements[2][0] = xyz.z();
                    axis[0] = Quaternion.multiplication(current.getWorldOrientation().inverse(), axis[0]);
                }
                 {
                    Joint current = r_knee;
                    Vec3d currentPos = current.getPosition();
                    Vec3d ve = Vec3d.substraction(currentEndR, currentPos);
                    ve.normalize();
                    Vec3d vg = Vec3d.substraction(targetR, currentPos);
                    vg.normalize();

                    axis[1] = new Vec3d(1,0,0);
                    axis[1].normalize();

                    Vec3d xyz = Vec3d.cross3(axis[1], ve);
                    xyz.normalize();
                    jacobian.elements[0][1] = xyz.x();
                    jacobian.elements[1][1] = xyz.y();
                    jacobian.elements[2][1] = xyz.z();

                    axis[1] = Quaternion.multiplication(current.getWorldOrientation().inverse(), axis[1]);
                }

                MatrixMN transpose = jacobian.transpose();
                MatrixMN rotations = transpose.multiply(t);

                {
                    Joint current = r_hip;
                    double r = rotations.elements[0][0];
                    Quaternion q = new Quaternion();
                    q.setAxisAngle(axis[0], (double) r * stepRange);
                    q.normalize();
                    current.rotate(q);
                    current.update();
                }
                {
                    Joint current = r_knee;
                    double r = rotations.elements[1][0];
                    Quaternion q = new Quaternion();
                    q.setAxisAngle(axis[1], (double) r * stepRange);
                    q.normalize();
                    current.rotate(q);
                    current.update();
                    applyConstraint(current, 0f, 3.14f,0.01f,0.00f,0.01f,0.00f);
                    current.update();
                }

                _rightleg.update();
                currentEndR = r_ankle.getPosition();
                distance.set(targetR.x() - currentEndR.x(), targetR.y() - currentEndR.y(), targetR.z() - currentEndR.z());
                ++loop;
            }
             //System.out.println("loop right: " +loop);
             _rotations.put("r_hip", r_hip.getLocalRotation());
             _rotations.put("r_knee", r_knee.getLocalRotation());
             _rotations.put("r_ankle", r_ankle.getWorldOrientation().inverse());
        }


    }

    public Quaternion getL_ankleQuaternion() {
        return l_ankle.getLocalRotation();
    }

    public Quaternion getL_hipQuaternion() {
        return l_hip.getLocalRotation();
    }

    public Quaternion getL_kneeQuaternion() {
        return l_knee.getLocalRotation();
    }

    public Quaternion getR_ankleQuaternion() {
        return r_ankle.getLocalRotation();
    }

    public Quaternion getR_hipQuaternion() {
        return r_hip.getLocalRotation();
    }

    public Quaternion getR_kneeQuaternion() {
        return r_knee.getLocalRotation();
    }


    public Vec3d getL_ankleOffset() {
        return _l_ankleOffset;
    }

    public void setL_ankleOffset(Vec3d _l_ankleOffset) {
        this._l_ankleOffset = _l_ankleOffset;
    }

    public Vec3d getR_ankleOffset() {
        return _r_ankleOffset;
    }

    public void setR_ankleOffset(Vec3d _r_ankleOffset) {
        this._r_ankleOffset = _r_ankleOffset;
    }

    public Vec3d getRootOffset() {
        return _rootOffset;
    }

    public void setRootOffset(Vec3d _rootOffset) {
        this._rootOffset = _rootOffset;
    }

    public HashMap<String, Quaternion> getRotations(){
        return _rotations;
    }

    public Frame getFrame(){
        Frame f = new Frame();
        f.addRotations(_rotations);
        f.setRootTranslation(_rootOffset);
        return f;
    }

    public void applyConstraint(Joint j, double xMin, double xMax, double yMin, double yMax, double zMin, double zMax){
        Quaternion q = j.getLocalRotation();
        Vec3d v = q.getEulerAngleXYZ();
        if(v.x() < xMin){
            v.setX(xMin);
        }
        if(v.y() < yMin){
            v.setY(yMin);
        }
        if(v.z() < zMin){
            v.setZ(zMin);
        }
        if(v.x() > xMax){
            v.setX(xMax);
        }
        if(v.y() > yMax){
            v.setY(yMax);
        }
        if(v.z() > zMax){
            v.setZ(zMin);
        }
        q.fromEulerXYZ(v.x(), v.y(), v.z());
        j.setLocalRotation(q);
    }
}
