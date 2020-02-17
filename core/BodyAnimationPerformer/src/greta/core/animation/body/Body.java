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

import greta.core.animation.CharacterBody;
import greta.core.animation.Frame;
import greta.core.animation.PositionFrame;
import greta.core.animation.SequenceIKCharacterBody;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * class for mixing body parts for one keyframe posture and use IK to generate
 * final posture 1. FK forward kinematics to define the initial state 2. IK
 * inverse kinematics to retarget the posture
 *
 * @author Jing Huang
 */
public class Body {

    double _time;
    Arm _leftArm;
    Arm _rightArm;

    public Body(double starttime) {
        _time = starttime;
    }

    public Body() {
    }

    public Frame getInitFrame() {
        Frame f = new Frame();
        if (_leftArm != null) {
            f.addRotations(_leftArm.getRotations());
        }
        if (_rightArm != null) {
            f.addRotations(_rightArm.getRotations());
        }
        return f;
    }

    public Body(Body kf) {
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

    public void setLeftArm(Arm leftarm) {

        this._leftArm = leftarm;

    }

    public Arm getRightArm() {
        return _rightArm;
    }

    public void setRightArm(Arm rightarm) {
        this._rightArm = rightarm;
    }

    public void setTime(double time) {
        _time = time;
    }

    public double getTime() {
        return _time;
    }

    @Override
    public Body clone() {
        return new Body(this);
    }

    /**
     *
     * @param agent the way to compute keyframe use the agent, agent has full
     * body IK PROCESS in it if you want, you can change the order, depends on
     * you expressive torse is torso modification by center of hands, it is
     * forward method, torso also is modified in agent, by full body ik , by
     * analytic solution
     */
    public Frame computeKeyFrame(CharacterBody cb, Frame init, ExpressiveTorso exTorso) {
        if (exTorso != null) {
            exTorso.setArms(_leftArm, _rightArm);
            exTorso.compute(0.1);
//            Quaternion expTorsoR = exTorso.getRotation();
        }
        Frame frame = new Frame();
        cb.resetSkeleton();
        if (init != null) {
            HashMap<String, Quaternion> rotations = new HashMap<String, Quaternion>(init.getRotations());
//            cb.setSkeletonValues(init);
            if (_leftArm != null) {
//                cb.setSkeletonValues(_leftArm.getRotations());
                rotations.putAll(_leftArm.getRotations());
            }
            if (_rightArm != null) {
//                cb.setSkeletonValues(_rightArm.getRotations());
                rotations.putAll(_rightArm.getRotations());
            }
            cb.setSkeletonValues(rotations);
        }
        cb.initMassSystemBySkeleton();
        cb.enableBody(false);
        if (_leftArm != null) {
            if (_leftArm._restPosName.isEmpty()) {
                cb.applyLeftHandByTrack(_leftArm.getTarget(), _leftArm.getOpenness());
            }
            frame.addRotations(_leftArm.getRotations());
        }
        if (_rightArm != null) {
            if (_rightArm._restPosName.isEmpty()) {
                cb.applyRightHandByTrack(_rightArm.getTarget(), _rightArm.getOpenness());
            }
            frame.addRotations(_rightArm.getRotations());
        }
        frame.addRotations(cb.updateFinalFrame().getRotations());
        if (_leftArm != null) {
           if(_leftArm.isGlobalOrientation())
           {
               Quaternion q = cb.getLeftElbowGlobalOrientation();
               frame.addRotation("l_wrist", Quaternion.multiplication( q.inverse(), _leftArm.getWrist()));
           }
        }
        if (_rightArm != null) {
           if(_rightArm.isGlobalOrientation())
           {
               Quaternion q = cb.getRightElbowGlobalOrientation();
               frame.addRotation("r_wrist", Quaternion.multiplication(q.inverse(), _rightArm.getWrist()));
           }
        }
        return frame;
    }

    public ArrayList<Frame> computeSequence(SequenceIKCharacterBody cb, PositionFrame input, Frame previous) {
        cb.resetSkeleton();
        cb.setStartFrame(input);
        Frame frame = new Frame();
        boolean usingkey = true;
        if (_leftArm != null) {
            if (_leftArm._restPosName.equalsIgnoreCase("")) {
                cb.applyLeftHandByTrack(_leftArm.getTarget(), _leftArm.getOpenness());
                usingkey = false;
            }
            frame.addRotations(_leftArm.getRotations());
        }
        if (_rightArm != null) {
            if (_rightArm._restPosName.equalsIgnoreCase("")) {
                cb.applyRightHandByTrack(_rightArm.getTarget(), _rightArm.getOpenness());
                usingkey = false;
            }
            frame.addRotations(_rightArm.getRotations());
        }
        if (!usingkey) {
            ArrayList<Frame> torso = new ArrayList<Frame>();
            ArrayList<Frame> left = new ArrayList<Frame>();
            ArrayList<Frame> right = new ArrayList<Frame>();
            ArrayList<Vec3d> lefthand = new ArrayList<Vec3d>();
            ArrayList<Vec3d> righthand = new ArrayList<Vec3d>();

            cb.updateAllFrames(torso, left, right, lefthand, righthand);
            ArrayList<Frame> warpframes = cb.warp(torso, left, right, lefthand, righthand, 0.5f);
            //warpframes = cb.output(torso, left, right);

//            frame.addRotations(cb.updateFinalFrame().getRotations());
//            ArrayList<Frame> frames = new ArrayList<Frame>();
//            frames.add(previous);
//            frames.add(frame);
            return warpframes;
        }
        ArrayList<Frame> frames = new ArrayList<Frame>();
        frames.add(previous);
        frames.add(frame);
        //System.out.println("key " + o);
        return frames;
    }
}
