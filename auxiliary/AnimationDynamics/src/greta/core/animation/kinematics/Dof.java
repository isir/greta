/*
 * This file is part of the auxiliaries of Greta.
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
