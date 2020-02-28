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
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class JacobianIK {

    double beta = 2f;
    double stepRange = 0.01f;
    int maxLoop = 300;
    double thresholdDis = 0.5f;

    public void compute(ArrayList<Joint> joints, Vec3d target) {
        if (joints.size() < 1) {
            return;
        }
        long time0 = System.currentTimeMillis();

        int loop = 0;
        MatrixMN jacobian = new MatrixMN(3, (joints.size() - 1) * 3);

        Joint end = joints.get(joints.size() - 1);

        MatrixMN t = new MatrixMN(3, 1);
//        t.set(0, 0, target.x());
//        t.set(1, 0, target.y());
//        t.set(2, 0, target.z());
        Vec3d endpos = end.getPosition();
        Vec3d distance = new Vec3d(target.x() - endpos.x(), target.y() - endpos.y(), target.z() - endpos.z());

        while (distance.length() > thresholdDis && loop < maxLoop) {
            t.elements[0][0] = beta * distance.x();
            t.elements[1][0] = beta * distance.y();
            t.elements[2][0] = beta * distance.z();
            int colomn = 0;
            for (int i = 0; i < joints.size() - 1; ++i) {
                Joint current = joints.get(i);
                Vec3d v = Vec3d.substraction(endpos, current.getPosition());
                v.normalize();
                Vec3d[] xyz = new Vec3d[3];

                xyz[0] = Vec3d.cross3(Quaternion.multiplication(current.getWorldOrientation(), new Vec3d(1, 0, 0)), v);
                xyz[1] = Vec3d.cross3(Quaternion.multiplication(current.getWorldOrientation(),new Vec3d(0, 1, 0)), v);
                xyz[2] = Vec3d.cross3(Quaternion.multiplication(current.getWorldOrientation(),new Vec3d(0, 0, 1)), v);

//                xyz[0] = Vec3d.cross3(new Vec3d(1, 0, 0),v);
//                xyz[1] = Vec3d.cross3(new Vec3d(0, 1, 0),v);
//                xyz[2] = Vec3d.cross3(new Vec3d(0, 0, 1),v);
                for (int j = 0; j < 3; ++j) {
//                jacobian.set(0, colomn, xyz[j].x());
//                jacobian.set(1, colomn, xyz[j].y());
//                jacobian.set(2, colomn, xyz[j].z());
                    xyz[j].normalize();
                    jacobian.elements[0][colomn] = xyz[j].x();
                    jacobian.elements[1][colomn] = xyz[j].y();
                    jacobian.elements[2][colomn] = xyz[j].z();
                    ++colomn;
                }
            }
//        Matrix rotations = new Matrix(1, joints.size() * 3);
            MatrixMN inverseJ = jacobian.pseudoInverse();
            //MatrixMN inverseJ = jacobian.transpose();
            MatrixMN rotations = inverseJ.multiply(t);

            int shift = 0;
            for (int i = 0; i < joints.size() - 1; ++i) {
                Joint current = joints.get(i);
                double x = rotations.elements[shift][0];
                ++shift;
                double y = rotations.elements[shift][0];
                ++shift;
                double z = rotations.elements[shift][0];
                ++shift;
                Quaternion q = new Quaternion();
                q.fromEulerXYZ((double) x * stepRange, (double) y * stepRange, (double) z * stepRange);
                q.normalize();
                //current.setLocalRotation(Quaternion.multiplication(q, current.getLocalRotation()).normalized());
                current.rotate(q);
            }

            end.update();
            endpos = end.getPosition();
            distance.set(target.x() - endpos.x(), target.y() - endpos.y(), target.z() - endpos.z());
            ++loop;
            //System.out.print("in:" + loop);
        }
        System.out.println("loop:" + loop);
        long time1 = System.currentTimeMillis();
        long time = time1 - time0;
        System.out.println("time: " + time / 1000.0d);
    }

    public void computeByQuaternion(ArrayList<Joint> joints, Vec3d target) {
        if (joints.size() < 1) {
            return;
        }
        long time0 = System.currentTimeMillis();
        reTargetting(joints, target);
        int loop = 0;
        MatrixMN jacobian = new MatrixMN(3, (joints.size() - 1));

        Joint end = joints.get(joints.size() - 1);
        MatrixMN t = new MatrixMN(3, 1);
        Vec3d endpos = end.getPosition();
        Vec3d distance = new Vec3d(target.x() - endpos.x(), target.y() - endpos.y(), target.z() - endpos.z());
        while (distance.length() > thresholdDis && loop < maxLoop) {
            t.elements[0][0] = beta * distance.x();
            t.elements[1][0] = beta * distance.y();
            t.elements[2][0] = beta * distance.z();

            Vec3d[] axis = new Vec3d[joints.size() - 1];
            for (int i = 0; i < joints.size() - 1; ++i) {
                Joint current = joints.get(i);
                Vec3d currentPos = current.getPosition();
                Vec3d ve = Vec3d.substraction(endpos, currentPos);
                ve.normalize();
                Vec3d vg = Vec3d.substraction(target, currentPos);
                vg.normalize();

                axis[i] = Vec3d.cross3(ve, vg);
                axis[i].normalize();

                Vec3d xyz = Vec3d.cross3(axis[i], ve);
                xyz.normalize();
                jacobian.elements[0][i] = xyz.x();
                jacobian.elements[1][i] = xyz.y();
                jacobian.elements[2][i] = xyz.z();
                axis[i] = Quaternion.multiplication(current.getWorldOrientation().inverse(), axis[i]);

            }

            MatrixMN transpose = jacobian.transpose();
            //MatrixMN inverseJ = jacobian.pseudoInverse();
            //MatrixMN back = inverseJ.multiply(jacobian);
            MatrixMN rotations = transpose.multiply(t);
//            MatrixMN deta = new MatrixMN(jacobian.getNumRows(),jacobian.getNumRows());
//            for(int i = 0; i < jacobian.getNumRows(); ++i){
//                deta.elements[i][i] = 0.02;
//            }
//            rotations = transpose.multiply((jacobian.multiply(transpose).addTo(deta)).pseudoInverse()).multiply(t);

            for (int i = 0; i < joints.size() - 1; ++i) {
                Joint current = joints.get(i);
                double r = rotations.elements[i][0];
                //System.out.println(current.getName() + "  axis: " + axis[i] + " r: " + r);
                Quaternion q = new Quaternion();
                if((double) r * stepRange > 3){
                    System.out.println("tooo large rotation");
                }
                q.setAxisAngle(axis[i], (double) r * stepRange);
                q.normalize();
                current.rotate(q);
                //current.setLocalRotation(Quaternion.multiplication(q, current.getLocalRotation()).normalized());
                current.update();
            }

            end.update();
            endpos = end.getPosition();
            distance.set(target.x() - endpos.x(), target.y() - endpos.y(), target.z() - endpos.z());
            ++loop;

            //System.out.println("in:" + loop + " distance:" + distance);
        }
        System.out.println("loop:" + loop);
        long time1 = System.currentTimeMillis();
        long time = time1 - time0;
        System.out.println("time: " + time / 1000.0d);
    }


    public void computeJacobianJointSpecific(ArrayList<Joint> joints, Vec3d target){
        if (joints.size() < 1) {
            return;
        }
        long time0 = System.currentTimeMillis();
        //reTargetting(joints, target);
        int loop = 0;
        MatrixMN jacobian = new MatrixMN(3, (joints.size() - 1));

        Joint end = joints.get(joints.size() - 1);
        MatrixMN t = new MatrixMN(3, 1);
        Vec3d endpos = end.getPosition();
        Vec3d distance = new Vec3d(target.x() - endpos.x(), target.y() - endpos.y(), target.z() - endpos.z());
        while (distance.length() > thresholdDis && loop < maxLoop) {
            t.elements[0][0] = beta * distance.x();
            t.elements[1][0] = beta * distance.y();
            t.elements[2][0] = beta * distance.z();

            Vec3d[] axis = new Vec3d[joints.size() - 1];
            for (int i = 0; i < joints.size() - 1; ++i) {
                Joint current = joints.get(i);
                Vec3d currentPos = current.getPosition();
                Vec3d ve = Vec3d.substraction(endpos, currentPos);
                ve.normalize();
                Vec3d vg = Vec3d.substraction(target, currentPos);
                vg.normalize();

                axis[i] = Vec3d.cross3(ve, vg);
                axis[i].normalize();
                if(current.getName().equalsIgnoreCase("l_elbow")||current.getName().equalsIgnoreCase("r_elbow")){
                    axis[i] = new Vec3d(1,0,0);
                    axis[i].normalize();
                }
                Vec3d xyz = Vec3d.cross3(axis[i], ve);
                xyz.normalize();
                jacobian.elements[0][i] = xyz.x();
                jacobian.elements[1][i] = xyz.y();
                jacobian.elements[2][i] = xyz.z();
                //if(!current.getName().equalsIgnoreCase("l_elbow")&&!current.getName().equalsIgnoreCase("r_elbow")){
                    axis[i] = Quaternion.multiplication(current.getWorldOrientation().inverse(), axis[i]);
                //}

            }

            MatrixMN transpose = jacobian.transpose();
            //MatrixMN inverseJ = jacobian.pseudoInverse();
            //MatrixMN back = inverseJ.multiply(jacobian);
            MatrixMN rotations = transpose.multiply(t);

            for (int i = 0; i < joints.size() - 1; ++i) {
                Joint current = joints.get(i);
                double r = rotations.elements[i][0];
                //System.out.println(current.getName() + "  axis: " + axis[i] + " r: " + r);
                Quaternion q = new Quaternion();
                q.setAxisAngle(axis[i], (double) r * stepRange);
                q.normalize();
                current.rotate(q);
                if(current.getName().equalsIgnoreCase("l_elbow")||current.getName().equalsIgnoreCase("r_elbow")){
                    Quaternion quaternion = current.getLocalRotation();
                    Vec3d xyz = quaternion.getEulerAngleXYZ();
                    if(xyz.x() > 0) xyz.setX(0);
                    quaternion.setAxisAngle(new Vec3d(1,0,0), xyz.x());
                    current.setLocalRotation(quaternion);
                }
                //current.setLocalRotation(Quaternion.multiplication(q, current.getLocalRotation()).normalized());
                current.update();
            }

            end.update();
            endpos = end.getPosition();
            distance.set(target.x() - endpos.x(), target.y() - endpos.y(), target.z() - endpos.z());
            ++loop;

            //System.out.println("in:" + loop + " distance:" + distance);
        }
        System.out.println("loop:" + loop);
        long time1 = System.currentTimeMillis();
        long time = time1 - time0;
        System.out.println("time: " + time / 1000.0d);
    }

    public void computeByCCD(ArrayList<Joint> joints, Vec3d target) {
        if (joints.size() < 1) {
            return;
        }
        long time0 = System.currentTimeMillis();
        reTargetting(joints, target);
        int loop = 0;


        Joint end = joints.get(joints.size() - 1);
        Joint endLoop = joints.get(joints.size() - 2);
        Joint start = joints.get(0);

        Vec3d endpos = end.getPosition();
        Vec3d distance = new Vec3d(target.x() - endpos.x(), target.y() - endpos.y(), target.z() - endpos.z());
        double cosAngle = 0;

        Joint current = endLoop;
        while (distance.length() > thresholdDis && loop < maxLoop) {

            Vec3d axis = null;
            double turnAngle = 0;

            Vec3d currentPos = current.getPosition();
            Vec3d ve = Vec3d.substraction(endpos, currentPos);
            ve.normalize();
            Vec3d vg = Vec3d.substraction(target, currentPos);
            vg.normalize();
            cosAngle = Math.min(1, ve.dot3(vg));
            cosAngle = Math.max(-1, cosAngle);

            if (cosAngle < 0.999) {
                if (cosAngle < -0.9999999) {
                    axis = new Vec3d(0, ve.x(), -ve.y());
                    if (axis.length() < 1.0e-10) {
                        axis.set(-ve.z(), 0, ve.x());
                    }
                    //System.out.println(current.getName() + " null space ");
                } else {
                    // use the cross product to check which way to rotate
                    axis = Vec3d.cross3(ve, vg);
                }

                axis.normalize();
                turnAngle = (double) Math.acos(cosAngle);	// get the angle
                //System.out.println(current.getName() + " : "+axis +"  " +turnAngle);
                Quaternion q = new Quaternion();
                q.setAxisAngle(axis, (double) turnAngle);
                q.normalize();
                //current.rotate(q);
                current.setLocalRotation(Quaternion.multiplication(q, current.getLocalRotation()).normalized());
            }

            if (current == start) {
                current = endLoop;
            } else {
                current = current.getParent();
            }

            for (Joint j : joints) {
                j.update();
            }
            endpos = end.getPosition();
            distance.set(target.x() - endpos.x(), target.y() - endpos.y(), target.z() - endpos.z());
            ++loop;
            //System.out.println("in:" + loop + " distance:" + distance);
        }
        System.out.println("loop:" + loop);
        long time1 = System.currentTimeMillis();
        long time = time1 - time0;
        System.out.println("time: " + time / 1000.0d);
    }

    Vec3d reTargetting(ArrayList<Joint> joints, Vec3d target) {
        Joint start = joints.get(0);
        Joint end = joints.get(joints.size() - 1);
        Vec3d vectT = Vec3d.substraction(target, start.getPosition());
        double length = 0;
        for (Joint j : joints) {
            length += j.getLocalPosition().length();
        }
        length -= start.getLocalPosition().length();
        double lengthT = vectT.length();

        if (lengthT > length) {
            System.out.print("retarget: " + target);
            target = Vec3d.addition(start.getPosition(), Vec3d.multiplication(vectT, length / lengthT));
            System.out.println("--->" + target);
        }
        return target;
    }

    void computeAxis(Vec3d currentPos, Vec3d endEffectorPos, Vec3d target, Vec3d retureAxis, Vec3d retureEntry) {
        Vec3d ve = Vec3d.substraction(endEffectorPos, currentPos);
        ve.normalize();
        Vec3d vg = Vec3d.substraction(target, currentPos);
        vg.normalize();
        retureAxis = Vec3d.cross3(ve, vg);
        retureEntry = Vec3d.cross3(retureAxis, ve);
    }

    void computeAxis(Vec3d currentPos, Vec3d endEffectorPos, Vec3d inputAxis, Vec3d retureEntry) {
        Vec3d ve = Vec3d.substraction(endEffectorPos, currentPos);
        ve.normalize();
        retureEntry = Vec3d.cross3(inputAxis, ve);
    }
}
