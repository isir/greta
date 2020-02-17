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
package greta.core.animation.locomotion;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class Biped {

    public State _state = new State();
    int _currentStateId = 0;
    double _time = 0.03;
    double _timeIntegral = 0;
    double _timechange = .3;
    double rootspeed = 0.00010;
    double rootTX = 0;
    double rootTY = 0;
    double rootTZ = 0;

    double speedScale = 0;
    double _signS = 0;
    double _rotY = 0;

    public Biped() {
        State.setWalk();
        State.setStop();
    }

    public void walk() {
        if (speedScale == 0) {
            stopWalk();
        } else {
            boolean change = false;
            for (int i = 0; i < 7; ++i) {
                int idx = _currentStateId * 7 + i;
                double desir = State._desireState[idx] * Math.abs(speedScale);
                double oldState = _state._currentState[i];
                double t = 0;
                if (i == 0) {
                    t = compute(desir, oldState, 0, 0);
                } else {
                    t = compute(desir, oldState);
                }
                _state._currentState[i] += t * _time;
                double newState = _state._currentState[i];
            }
            _timeIntegral += _time;
            if (change == true || _timeIntegral >= _timechange) {
                _currentStateId -= Math.signum(speedScale);
                if (_currentStateId < 0) {
                    _currentStateId = 3;
                } else if (_currentStateId > 3) {
                    _currentStateId = 0;
                }
                _timeIntegral = 0;
            }
            if (_currentStateId == 1 || _currentStateId == 3) {
                rootspeed = 0.00028 * speedScale;
                rootTY += -0.00001;
            } else {
                rootspeed = 0.00025 * speedScale;
                rootTY -= -0.00001;
            }
            _signS += Math.PI * _time / _timechange * 0.5;
            if (_signS >= Math.PI * 2) {
                _signS = 0;
            }
            rootTZ += rootspeed * Math.cos(_rotY);
            rootTX += rootspeed * Math.sin(_rotY);
        }
        //System.out.println(_state._currentState[1]);
    }

    public void setSpeed(double speed) {
        speedScale = speed;
    }

    public void turnLeft() {
        _rotY += 0.01;
        if (_rotY >= Math.PI) {
            _rotY -= Math.PI * 2;
        }
    }

    public void turnRight() {
        _rotY -= 0.01;
        if (_rotY <= -Math.PI) {
            _rotY += Math.PI * 2;
        }
    }

    public void stopWalk() {
        for (int i = 0; i < 7; ++i) {
            int idx = 4 * 7 + i;
            double desir = State._desireState[idx];
            double oldState = _state._currentState[i];
            double t = compute(desir, oldState, 0, 0);
            _state._currentState[i] += t * _time;
            double newState = _state._currentState[i];
        }
        if (rootspeed > 0) {
            rootspeed -= 0.0001;
        } else {
            rootspeed = 0;
        }
        //System.out.println(rootspeed);
        rootTZ += rootspeed;
    }

    double _friction = 0;
    double _kP = 4;
    double _kD = 2 * Math.sqrt(_kP);

    public double compute(double desireAngle, double currentAngle, double desireVelocity, double currentvelocity) {
        double _torqueOutput = (desireAngle - currentAngle) * _kP + (desireVelocity - currentvelocity) * _kD - _friction * currentvelocity;
        //double _torqueOutput = Math.signum(desireAngle - currentAngle) * _kP ;
        return _torqueOutput;
    }

    public double compute(double desireAngle, double currentAngle) {
        double _torqueOutput = (desireAngle - currentAngle) / (_timechange - _timeIntegral);
        //double _torqueOutput = Math.signum(desireAngle - currentAngle) * _kP ;
        return _torqueOutput;
    }

    public double getRootTranslateY() {
        return rootTY;
    }

    public double getRootTranslateX() {
        return rootTX;
    }

    public double getRootTranslateZ() {
        return rootTZ;
    }

    public double getRootZ() {
        return Math.cos(_signS) / 100.0;
    }

    public double getRootY() {
        return _rotY;
    }

    public double getRootX() {
        return _state._currentState[0];
    }

    public double getL_HipX() {
        return _state._currentState[3];
    }

    public double getR_HipX() {
        return _state._currentState[1];
    }

    public double getL_KneeX() {
        return _state._currentState[4];
    }

    public double getR_KneeX() {
        return _state._currentState[2];
    }

    public double getR_AnkleX() {
        return _state._currentState[5];
    }

    public double getL_AnkleX() {
        return _state._currentState[6];
    }

    public static void main(String[] arg) {
        Biped ped = new Biped();

        for (int i = 0; i < 100; i++) {
            ped.walk();
        }

    }
}
