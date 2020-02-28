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

import greta.core.animation.Joint;
import greta.core.animation.Skeleton;
import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.util.enums.Side;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class Arm extends ExpressiveFrame {
    TrajectoryDescription _traj;
    Side _side;
    String _restPosName = "";
    Quaternion _wrist = new Quaternion();
    private boolean _globalOrientation = false;
    Vec3d _target;

    String _touchpoints = "";
    Vec3d _offset;
    //String _handShape = "";
    double openness;

    public Arm(){

    }

    public Arm(Arm arm) {
        _target = arm._target.clone();
        _side = arm.getSide();
        _wrist = arm._wrist.clone();
        _restPosName = arm._restPosName;
        _time = arm._time;
        _globalOrientation = arm._globalOrientation;
        openness = arm.openness;
        super.clone();
    }

    public void useTouchPoint(String name, Vec3d offset){
        _touchpoints = name;
        _offset = offset;
    }

    /*public String getHandShape() {
        return _handShape;
    }

    public void setHandShape(String handShape) {
        this._handShape = handShape;
    }*/

    public String getRestPosName() {
        return _restPosName;
    }

    public void setRestPosName(String _restPosName) {
        this._restPosName = _restPosName;
    }

    public void setWrist(Quaternion wrist, boolean globalOrientation) {
        _wrist = wrist;
        setGlobalOrientation(globalOrientation);
        if(_side == Side.LEFT){
            this.addRotation("l_wrist", wrist);
        }else{
            this.addRotation("r_wrist", wrist);
        }
    }

    public TrajectoryDescription getTraj() {
        return _traj;
    }

    public void setTraj(TrajectoryDescription traj) {
        this._traj = traj;
    }


    public Quaternion getWrist() {
        return _wrist;
    }

    public Vec3d getTarget() {
        return _target;
    }

    public void setTarget(Vec3d _target) {
        this._target = _target;
    }

    public void updateTarget(Skeleton skeleton){
        Joint joint = skeleton.getJoint(_touchpoints);
        if(joint != null && _offset != null){
            _target.add(joint.getWorldPosition());
        }
    }

    public Arm(double time) {
        _time = time;
    }

    public void setSide(Side side) {
        _side = side;
    }

    public Side getSide() {
        return _side;
    }

    public boolean isGlobalOrientation() {
        return _globalOrientation;
    }

    public void setGlobalOrientation(boolean globalOrientation) {
        this._globalOrientation = globalOrientation;
    }


    @Override
    public Arm clone() {
        return new Arm(this);
    }

    public double getOpenness() {
        return openness;
    }

    public void setOpenness(double openness) {
        this.openness = openness;
    }


}
