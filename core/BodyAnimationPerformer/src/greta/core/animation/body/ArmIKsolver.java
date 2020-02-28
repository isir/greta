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
package greta.core.animation.body;

import greta.core.animation.DirectionalMassConstraint;
import greta.core.animation.Mass;
import greta.core.animation.MassConstraint;
import greta.core.animation.MassSpringConstraint;
import greta.core.animation.PositionMassConstraint;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class ArmIKsolver {

    public static double EnergyEpsilon = 0.001f;
    Vec3d _shoulder;
    Vec3d _elbow;
    Vec3d _wrist;
    ArrayList<Mass> _massList = new ArrayList<Mass>();
    HashMap<String, MassConstraint> _externalConstraintList = new HashMap<String, MassConstraint>();
    String _side = "right";
    ArrayList<Quaternion> _rotations = new ArrayList<Quaternion>();
    Quaternion _shoulderR = new Quaternion();
    Quaternion _elbowR = new Quaternion();
    Quaternion _wristR = new Quaternion();

    void init() {
        _rotations.clear();
        _rotations.add(_shoulderR);
        _rotations.add(_elbowR);
        _rotations.add(_wristR);
        _massList.clear();
        {
            Mass mass_shoulder = new Mass("shoulder");
            _massList.add(mass_shoulder);
            mass_shoulder.setMass(2);
            mass_shoulder.setPosition(_shoulder);
            mass_shoulder.setMovable(false);

            Mass mass_elbow = new Mass("elbow");
            _massList.add(mass_elbow);
            mass_elbow.setPosition(_elbow);

            Mass mass_wrist = new Mass("wrist");
            _massList.add(mass_wrist);
            mass_wrist.setPosition(_wrist);

            MassSpringConstraint mc1 = new MassSpringConstraint(mass_shoulder,
                    mass_elbow);
            mass_shoulder.addInternalConstraint(mc1);
            MassSpringConstraint mc2 = new MassSpringConstraint(mass_elbow,
                    mass_shoulder);
            mass_elbow.addInternalConstraint(mc2);

            MassSpringConstraint mc3 = new MassSpringConstraint(mass_elbow,
                    mass_wrist);
            mass_elbow.addInternalConstraint(mc3);
            MassSpringConstraint mc4 = new MassSpringConstraint(mass_wrist,
                    mass_elbow);
            mass_wrist.addInternalConstraint(mc4);
        }
    }

    Vec3d oldX_shoulder;
    Vec3d oldY_shoulder;
    Vec3d oldZ_shoulder;

    Vec3d oldX_elbow;
    Vec3d oldY_elbow;
    Vec3d oldZ_elbow;

    public void setOriginal(Vec3d shoulder, Vec3d elbow, Vec3d wrist, String side) {
        _shoulder = shoulder.clone();
        _elbow = elbow.clone();
        _wrist = wrist.clone();
        _side = side;
        init();

        {
            oldY_shoulder = Vec3d.substraction(_shoulder, _elbow);
            oldY_shoulder.normalize();
            Vec3d lower = Vec3d.substraction(_wrist, _shoulder);
            lower.normalize();

            double v = oldY_shoulder.dot3(lower);
            if (v > 0.99 || v < -0.99) {
                oldX_shoulder = new Vec3d(1, 0, 0);
                oldZ_shoulder = Vec3d.cross3(oldX_shoulder, oldY_shoulder);
            } else {
                oldX_shoulder = Vec3d.cross3(oldY_shoulder, lower);
                oldZ_shoulder = Vec3d.cross3(oldX_shoulder, oldY_shoulder);
            }
            oldX_shoulder.normalize();
            oldZ_shoulder.normalize();
        }

        {
            oldY_elbow = Vec3d.substraction(_elbow, _wrist);
            oldY_elbow.normalize();
            Vec3d lower = Vec3d.substraction(_wrist, _shoulder);
            lower.normalize();

            double v = oldY_elbow.dot3(lower);
            if (v > 0.99 || v < -0.99) {
                oldX_elbow = new Vec3d(1, 0, 0);
                oldZ_elbow = Vec3d.cross3(oldX_elbow, oldY_elbow);
            } else {
                oldX_elbow = Vec3d.cross3(oldY_elbow, lower);
                oldZ_elbow = Vec3d.cross3(oldX_elbow, oldY_elbow);
            }
            oldX_elbow.normalize();
            oldZ_elbow.normalize();

        }
    }


    void resetMassSystem(){
        Mass wrist = _massList.get(2);
        Mass elbow = _massList.get(1);
        Mass shoulder = _massList.get(0);
        shoulder.setPosition(_shoulder);
        elbow.setPosition(_elbow);
        wrist.setPosition(_wrist);
    }

    public void compute(Vec3d target, Quaternion wristOrientation, double openness) {
        resetMassSystem();
        Mass wrist = _massList.get(2);
        Mass elbow = _massList.get(1);
        PositionMassConstraint mc = new PositionMassConstraint(wrist, target, 0);
        wrist.setMovable(true);
        int side = 1;
        if (_side == "right") {
            side = -1;
        }
        DirectionalMassConstraint dmc = new DirectionalMassConstraint(new Vec3d(1 * side, 0, 0), (double) (0.2f * openness));

        double energy = 10000;
        int loop = 0;
        int maxLoop = 150;
        while (energy > EnergyEpsilon && loop < maxLoop) {
            energy = 0;
            wrist.addForce(mc.getForce());
            elbow.addForce(dmc.getForce());

            for (Mass mass : _massList) {
                mass.applyConstraints();
                energy += Math.abs(mass.getAcceleration().length());
                mass.move();
                mass.resetAcceleration();
            }
            loop++;
        }
        //System.out.println(energy + " " + loop);
        computeAngles();
        if (wristOrientation != null) {
            Quaternion globalP = Quaternion.multiplication(_shoulderR, _elbowR);
            _wristR.setValue(Quaternion.multiplication(globalP.inverse(), wristOrientation));
        }
    }

    public ArrayList<Quaternion> getRotations() {
        return _rotations;
    }

    private void computeAngles() {
        Mass wrist = _massList.get(2);
        Mass elbow = _massList.get(1);
        Mass shoulder = _massList.get(0);
        {
            Vec3d Y = Vec3d.substraction(shoulder.getPosition(), elbow.getPosition());
            Y.normalize();
            Vec3d lower = Vec3d.substraction(wrist.getPosition(), shoulder.getPosition());
            lower.normalize();

            double v = Y.dot3(lower);
            Vec3d Z = null;
            Vec3d X = null;
            if (v > 0.99 || v < -0.99) {
                X = new Vec3d(1, 0, 0);
                Z = Vec3d.cross3(X, Y);
            } else {
                X = Vec3d.cross3(Y, lower);
                Z = Vec3d.cross3(X, Y);
            }
            X.normalize();
            Z.normalize();
            _shoulderR.fromRotatedBasis(X, Y, Z, oldX_shoulder, oldY_shoulder, oldZ_shoulder);
        }

        {
            Vec3d upper = Vec3d.substraction(elbow.getPosition(), shoulder.getPosition());
            Vec3d lower = Vec3d.substraction(wrist.getPosition(), elbow.getPosition());
            upper.normalize();
            lower.normalize();
            double p = upper.dot3(lower);
            if(p > 1) p = 1;
            if(p < -1) p = -1;
            double angle = Math.acos(p);
            _elbowR.setAxisAngle(new Vec3d(1, 0, 0), -angle);
        }

        //            Vec3d Y = Vec3d.substraction(elbow.getPosition(), wrist.getPosition());
//            Y.normalize();
//            Vec3d lower = Vec3d.substraction(wrist.getPosition(), shoulder.getPosition());
//            lower.normalize();
//
//            double v = Y.dot3(lower);
//            Vec3d Z = null;
//            Vec3d X = null;
//            if (v > 0.99 || v < -0.99) {
//                X = new Vec3d(1, 0, 0);
//                Z = Vec3d.cross3(X, Y);
//            } else {
//                X = Vec3d.cross3(Y, lower);
//                Z = Vec3d.cross3(X, Y);
//            }
//            X.normalize();
//            Z.normalize();
//
    }

}
