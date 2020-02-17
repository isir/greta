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

/**
 *
 * @author Quoc Anh Le
 * @author Andre-Marie Pez
 * @author Brice Donval
 */
public class SpineDirection {

    public static enum Direction {
        LEFTWARD,
        RIGHTWARD,
        FORWARD,
        BACKWARD;
    }

    /**
     * The direction.<br/>
     * It may be :<br/> {@code "LEFTWARD"}, {@code "RIGHTWARD"},<br/>
     * {@code "FORWARD"}, {@code "BACKWARD"}.
     */
    public Direction direction;

    /**
     * The proportional value in the {@code direction}.
     */
    public double value;
    public double valueMin;
    public double valueMax;

    /**
     * Check if this {@code SpineDirection} is used.<br/> {@code true} if it is
     * used.<br/> {@code false} orherwise.
     */
    public boolean flag;

    /**
     * Default constructor.
     */
    public SpineDirection() {
        flag = false;
    }

    /**
     * Copy constructor.
     *
     * @param spineDirection the {@code SpineDirection} to copy.
     */
    public SpineDirection(SpineDirection spineDirection) {

        direction = spineDirection.direction;

        value = spineDirection.value;
        valueMin = spineDirection.valueMin;
        valueMax = spineDirection.valueMax;

        flag = spineDirection.flag;
    }

    /**
     * Adds the value of a {@code SpineDirection} to this
     * {@code SpineDirection}.
     *
     * @param other the {@code SpineDirection} to add.
     */
    public void add(SpineDirection other) {
        if (direction == other.direction) {
            value += other.value;
        } else {
            value -= other.value;
            if (value < 0) {
                direction = other.direction;
                value = -value;
            }
        }
        flag = true;
    }

    /**
     * Multiplies the value of this {@code SpineDirection} by a factor.
     *
     * @param x the factor.
     */
    public void multiply(double x) {
        value *= x;
        if (value < 0) {
            inverse();
            value = -value;
        }
    }

    /**
     * Inverses the direction of this {@code SpineDirection}.
     */
    public void inverse() {
        if (direction == Direction.LEFTWARD) {
            direction = Direction.RIGHTWARD;
        } else if (direction == Direction.RIGHTWARD) {
            direction = Direction.LEFTWARD;
        } else if (direction == Direction.FORWARD) {
            direction = Direction.BACKWARD;
        } else if (direction == Direction.BACKWARD) {
            direction = Direction.FORWARD;
        }
    }

}
