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

/**
 *
 * @author Jing Huang
 */
public class HingeJoinController implements BaseController{
    String _name;
    int _index;
    double _friction = 500;
    double _desireAngle;
    double _desireVelocity;
    double _lastAngle;
    double _currentAngle;
    double _kP;  //propotional parameter
    double _kD; //derivative parameter
    double _torqueOutput;
    boolean _active = true;

    private static final float KP_DEFAULT = 1000;
    private static final float KD_DEFAULT = 100.5f;

    public HingeJoinController(String name, int index, double desireAngle, double desireVelocity, double kP, double kD) {
        this._name = name;
        this._index = index;
        this._desireAngle = desireAngle;
        this._desireVelocity = desireVelocity;
        this._kP = kP;
        this._kD = kD;
    }


    public HingeJoinController(){
        this("",0,0,0,KP_DEFAULT,KD_DEFAULT);
    }

    public HingeJoinController(double desireAngle){
        this("",0,desireAngle,0,KP_DEFAULT,KD_DEFAULT);
    }

    @Override
    public void update(double dt) {
        if(!_active) return;
        double velocity = (_currentAngle - _lastAngle) / dt;
        //_torque = (_desireAngle - _currentAngle) * _kP - (velocity - _desireVelocity) * _kD;
        _torqueOutput = (_desireAngle - _currentAngle) * _kP + (_desireVelocity - velocity) * _kD - _friction * velocity;
        _lastAngle = _currentAngle;
    }

    public void setInitialAngle(double angle, double lastangle){
        _currentAngle = angle;
        _lastAngle = lastangle;
    }

    public double getDesireAngle() {
        return _desireAngle;
    }

    public void setDesireAngle(double _desireAngle) {
        this._desireAngle = _desireAngle;
    }

    public double getDesireVelocity() {
        return _desireVelocity;
    }

    public void setDesireVelocity(double _desireVelocity) {
        this._desireVelocity = _desireVelocity;
    }

    public double getCurrentAngle() {
        return _currentAngle;
    }

    public void setCurrentAngle(double _currentAngle) {
        this._currentAngle = _currentAngle;
    }

    public double getkProportional() {
        return _kP;
    }

    public void setkProportional(double _kP) {
        this._kP = _kP;
    }

    public double getkDerivative() {
        return _kD;
    }

    public void setkDerivative(double _kD) {
        this._kD = _kD;
    }

    public double getTorqueOutput() {
        return _torqueOutput;
    }

    public double getFriction() {
        return _friction;
    }

    public void setFriction(double friction) {
        this._friction = friction;
    }


    @Override
    public String getName() {
        return _name;
    }

    @Override
    public void setName(String name) {
        _name = name;
    }

    @Override
    public int getIndex() {
        return _index;
    }

    @Override
    public void setIndex(int index) {
        _index = index;
    }

    @Override
    public void setActive(boolean active) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isActive() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
