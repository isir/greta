/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.body;

import vib.core.animation.common.Frame.ExtendedKeyFrame;
import vib.core.animation.common.Skeleton;
import vib.core.animation.common.Target;
import vib.core.signals.gesture.TrajectoryDescription;
import vib.core.util.enums.Side;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class Arm extends ExtendedKeyFrame {

    class Wrist{
        public Quaternion _wrist = new Quaternion();
        public boolean _local = false;
    };
    Skeleton _skeleton;
    Target _target = new Target();
    Side _side;
    Wrist _wristGlobal = new Wrist();
    TrajectoryDescription _trajectory;
  //  Function _funtion;
    String _restPosName = "";
    double openness;

    public Skeleton getSkeleton() {
        return _skeleton;
    }

    public void setSkeleton(Skeleton _skeleton) {
        this._skeleton = _skeleton;
    }

    public String getRestPosName() {
        return _restPosName;
    }

    public void setRestPosName(String _restPosName) {
        this._restPosName = _restPosName;
    }

//    public Function getInterpolationFuntion() {
//        return _funtion;
//    }
//
//    public void setInterpolationFuntion(Function _funtion) {
//        this._funtion = _funtion;
//    }

    public TrajectoryDescription getTrajectory() {
        return _trajectory;
    }

    public void setTrajectory(TrajectoryDescription trajectory) {
        this._trajectory = trajectory;
    }

    public Quaternion getWrist() {
        return _wristGlobal._wrist;
    }

    public boolean isWristLocalOrientation() {
        return _wristGlobal._local;
    }

    /**
     *
     * @param wrist  the orientation of wrist, can be global or local, depends on second parameter
     * @param local to define if the orientation is the prefined local parameter,if is, do not need to computer orientation
     */
    public void setWrist(Quaternion wrist, boolean local) {
        this._wristGlobal._wrist = wrist;
        this._wristGlobal._local = local;
    }

    public Target getTarget() {
        return _target;
    }

    public void setTarget(Target _target) {
        this._target = _target;
    }

    public Arm(double time) {
        super(time);
    }

    public Vec3d getPosition() {
        return _target.getPosition();
    }

    public void setPosition(Vec3d position) {
        _target.setPosition(position);
    }

    public Vec3d getUpDirectionVector() {
        return _target.getUpDirectionVector();
    }

    public void setUpDirectionVector(Vec3d v) {
        _target.setUpDirectionVector(v.normalized());
    }

    public void setSide(Side side) {
        _side = side;
    }

    public Side getSide() {
        return _side;
    }

    public Arm(Arm arm) {
        super(arm.getTime());
        _target = arm._target.clone();
        _side = arm.getSide();
        if (_wristGlobal != null) {
            _wristGlobal._wrist = arm.getWrist().clone();
            _wristGlobal._local = arm.isWristLocalOrientation();
        }
        super.clone();
    }

    public double getOpenness() {
        return openness;
    }

    public void setOpenness(double openness) {
        this.openness = openness;
    }

    @Override
    public Arm clone() {
        return new Arm(this);
    }
}
