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
package greta.core.animation.kinematics;

/**
 *
 * @author Jing Huang
 */
public class Dof {

    public Dof(double value){
        _value = value;
    }

    public Dof(double value, double minV, double maxV){
        _value = value;
        _minValue = minV;
        _maxValue = maxV;
    }

    void setName(String name){
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public double getValue() {
        return _value;
    }

    public void setValue(double _value) {
        this._value = _value;
    }

    public double getMinValue() {
        return _minValue;
    }

    public void setMinValue(double _minValue) {
        this._minValue = _minValue;
    }

    public double getMaxValue() {
        return _maxValue;
    }

    public void setMaxValue(double _maxValue) {
        this._maxValue = _maxValue;
    }

    public double getTorque() {
        return _torque;
    }

    public void setTorque(double _torque) {
        this._torque = _torque;
    }

    public double getMinTorque() {
        return _minTorque;
    }

    public void setMinTorque(double _minTorque) {
        this._minTorque = _minTorque;
    }

    public double getMaxTorque() {
        return _maxTorque;
    }

    public void setMaxTorque(double _maxTorque) {
        this._maxTorque = _maxTorque;
    }


    protected String _name;
    protected double _value;
    protected double _minValue = Double.NEGATIVE_INFINITY;
    protected double _maxValue = Double.POSITIVE_INFINITY;
    protected double _torque;
    protected double _minTorque = Double.NEGATIVE_INFINITY;
    protected double _maxTorque = Double.POSITIVE_INFINITY;
}
