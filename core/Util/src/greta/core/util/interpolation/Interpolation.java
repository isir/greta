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
package greta.core.util.interpolation;

import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public abstract class Interpolation<T> {

    protected ArrayList<Double> _segmentLengths = new ArrayList<Double>();           // world space segment length
    protected ArrayList<Double> _normalizedSegmentLengths = new ArrayList<Double>();    // segment length normalized to the whole trajectory being in [0,1]
    protected ArrayList<Double> _normalizedPointPositions = new ArrayList<Double>();    // normalized position of each control point
    protected ArrayList<T> _controlPoints = new ArrayList<T>();

    public Interpolation() {
    }

    public void setControlPoints(ArrayList<T> controlPoints) {
        _controlPoints = controlPoints;
    }

    public ArrayList<T> getControlPoints() {
        return _controlPoints;
    }

    protected abstract double calculateDistance(T p1, T p2);

    protected double calculateLength() {
        double length = 0;
        for (int i = 0; i < _controlPoints.size() - 1; ++i) {
            double lengthTmp = java.lang.Math.max(calculateDistance(_controlPoints.get(i), _controlPoints.get(i + 1)), 1e-6f);
            //System.out.println("lengthTmp"+lengthTmp);
            _segmentLengths.add(lengthTmp);
            _normalizedSegmentLengths.add(lengthTmp);
            length += lengthTmp;
        }
        _normalizedPointPositions.add(0.0); // first point is at t = 0

        for (int i = 0; i < _normalizedSegmentLengths.size(); ++i) {
            _normalizedSegmentLengths.set(i, _normalizedSegmentLengths.get(i) / length);
            _normalizedPointPositions.add(_normalizedPointPositions.get(i) + _normalizedSegmentLengths.get(i));
        }

        assert (_normalizedPointPositions.size() == _controlPoints.size());

        return length;

    }

    public abstract T getPosition(double t);
    //public abstract T getReflectedPosition(double t);
}
