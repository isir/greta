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
package greta.core.signals;

import greta.core.util.math.Quaternion;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author Quoc Anh Le
 * @author Brice Donval
 */
public class SpinePhase {

    /**
     * the type
     */
    private String type;

    /**
     * The start time
     */
    private double startTime;

    /**
     * the end time
     */
    private double endTime;

    /**
     * Rotation around the x(left) axis
     */
    public SpineDirection sagittalTilt;

    /**
     * Rotation around the y(up) axis
     */
    public SpineDirection verticalTorsion;

    /**
     * Rotation around the z(forward) axis
     */
    public SpineDirection lateralRoll;

    public SpineDirection collapse;

    public SortedMap<String, Quaternion> _rotations;

    /**
     * Constructor.
     *
     * @param type the type.
     * @param startTime the start time in seconds.
     * @param endTime the end time in seconds.
     */
    public SpinePhase(String type, double startTime, double endTime) {

        this.type = type;

        this.startTime = startTime;
        this.endTime = endTime;

        sagittalTilt = new SpineDirection();
        verticalTorsion = new SpineDirection();
        lateralRoll = new SpineDirection();
        collapse = new SpineDirection();

        _rotations = new TreeMap<>();
    }

    /**
     * Copy constructor.
     *
     * @param phase the {@code SpinePhase} to copy.
     */
    public SpinePhase(SpinePhase phase) {

        type = phase.type;

        startTime = phase.startTime;
        endTime = phase.endTime;

        sagittalTilt = new SpineDirection(phase.sagittalTilt);
        verticalTorsion = new SpineDirection(phase.verticalTorsion);
        lateralRoll = new SpineDirection(phase.lateralRoll);
        collapse = new SpineDirection(phase.collapse);

        _rotations = phase._rotations;
    }

    /**
     * @return the Type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the starting time of this {@code SpinePhase}.
     *
     * @return the start time in seconds.
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * Set the starting time of this {@code SpinePhase}.
     *
     * @param startTime the start time in seconds.
     */
    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    /**
     * Returns the ending time of this {@code SpinePhase}.
     *
     * @return the end time in seconds.
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * Set the ending time of this {@code SpinePhase}.
     *
     * @param endTime the end time in seconds.
     */
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

}
