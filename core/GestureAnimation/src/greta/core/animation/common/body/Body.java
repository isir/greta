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
package greta.core.animation.common.body;

import greta.core.animation.common.HumanAgent;
import greta.core.animation.common.Target;

/**
 *
 * class for mixing body parts for one keyframe posture
 * and use IK to generate final posture
 *  1. FK  forward kinematics   to define the initial state
 *  2. IK  inverse kinematics   to retarget the posture
 * @author Jing Huang
 */
public class Body {

    double _time;
    Torse _torse;
    Arm _leftArm;
    Arm _rightArm;


    public Body(double starttime) {
        _time = starttime;
    }

    public Body() {
        _time = 0;
    }
    public Body(Body kf) {
        // _isRestPos = kf._isRestPos;

        if (kf._torse != null) {
            _torse = kf._torse.clone();
        }
        if (kf._leftArm != null) {
            _leftArm = kf._leftArm.clone();
        }
        if (kf._rightArm != null) {
            _rightArm = kf._rightArm.clone();
        }
        _time = kf._time;

    }

    public Arm getLeftArm() {
        return _leftArm;
    }

    public void setLeftArm(Arm _leftarm) {

        this._leftArm = _leftarm;

    }

    public Arm getRightArm() {
        return _rightArm;
    }

    public void setRightArm(Arm _rightarm) {

        this._rightArm = _rightarm;

    }

    public void setTorse(Torse torseKeyframe) {
        this._torse = torseKeyframe;
    }

    public Torse getTorse() {
        return _torse;
    }

    public double getTime() {
        return _time;
    }

    /**
     *
     * @param agent
     * the way to compute keyframe use the agent, agent has full body IK PROCESS in it
     * if you want, you can change the order, depends on you
     * expressive torse is torso modification by center of hands, it is forward method,
     * torso also is modified in agent, by full body ik , by analytic solution
     */
    public void computeKeyFrame(HumanAgent agent) {

        Target gaze = null;
       /* if (_head != null) {
            Quaternion q = _head.getRotation();
            HashMap<String, JointFrame> headr = new HashMap<String, JointFrame>();

            Vec3d angles = q.getEulerAngleXYZ();

            Quaternion qvc1 = new Quaternion(new Vec3d(0,1,0), angles.y()/3);
            qvc1.multiply(new Quaternion(new Vec3d(1,0,0), angles.x()*0.7f));
            qvc1.multiply(new Quaternion(new Vec3d(0,0,1), angles.z()*0.1f));

            Quaternion qvc4 = new Quaternion(new Vec3d(0,1,0), angles.y()/3);
            qvc4.multiply(new Quaternion(new Vec3d(1,0,0), angles.x()*0.2f));
            qvc4.multiply(new Quaternion(new Vec3d(0,0,1), angles.z()*0.3f));

            Quaternion qvc7 = new Quaternion(new Vec3d(0,1,0), angles.y()/3);
            qvc7.multiply(new Quaternion(new Vec3d(1,0,0), angles.x()*0.1f));
            qvc7.multiply(new Quaternion(new Vec3d(0,0,1), angles.z()*0.6f));


            JointFrame jf1 = new JointFrame();
            jf1._localrotation = qvc1;
            headr.put("vc1", jf1);

            JointFrame jf4 = new JointFrame();
            jf4._localrotation = qvc4;
            headr.put("vc4", jf4);

            JointFrame jf7 = new JointFrame();
            jf7._localrotation = qvc7;
            headr.put("vc7", jf7);

        }*/

        if(_torse == null)
            _torse = new Torse(_time);
        agent.compute(_leftArm, _rightArm, _torse);

    }

}
