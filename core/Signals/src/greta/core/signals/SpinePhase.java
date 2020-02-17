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
