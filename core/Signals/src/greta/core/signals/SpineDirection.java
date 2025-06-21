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
