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

import greta.core.animation.common.Frame.ExtendedKeyFrame;
import greta.core.animation.common.Skeleton;
import greta.core.animation.common.Target;
import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.util.enums.Side;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

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
