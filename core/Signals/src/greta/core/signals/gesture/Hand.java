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
package greta.core.signals.gesture;

import greta.core.util.enums.Side;
import greta.core.util.math.Quaternion;

/**
 *
 * @author Quoc Anh Le
 * @author Jing Huang
 * @author Andre-Marie Pez
 */
public class Hand {

    private Side side;
    private String shapeName;
    private Position position;
    private Quaternion wristOrientation;
    private TrajectoryDescription trajectory;

    private boolean orientationGlobal;
    private double openness;

    private boolean shapeOverridable;
    private boolean wristOrientationOverridable;

    public Hand(Side side, String shapeName, Position position, Quaternion wristOrientation, TrajectoryDescription trajectory) {
        this.side = side;
        this.shapeName = shapeName;
        this.position = position;
        this.wristOrientation = wristOrientation;
        this.trajectory = trajectory;

        this.orientationGlobal = false;
        this.openness = 0;

        this.shapeOverridable = false;
        this.wristOrientationOverridable = false;
    }

    public Hand(Side side) {
        this(side, null, null, null, null);
    }

    public Hand(Hand other) {
        this(
                other.side,
                other.shapeName,
                other.position==null ? null : other.position.getCopy(),
                other.wristOrientation==null ? null : new Quaternion(other.wristOrientation),
                other.trajectory==null ? null : new TrajectoryDescription(other.trajectory)
        );

        orientationGlobal = other.orientationGlobal;
        openness = other.openness;

        shapeOverridable = other.shapeOverridable;
        wristOrientationOverridable = other.wristOrientationOverridable;
    }

    public Side getSide() {
        return side;
    }

    public void setSide(Side side) {
        this.side = side;
    }

    public String getHandShape() {
        return shapeName;
    }

    public void setHandShape(String shapeName) {
        this.shapeName = shapeName;
    }

    public Position getPosition() {
        return position;
    }

    public String getStringPosition() {
        return String.valueOf(position.getStringPosition());
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Quaternion getWristOrientation() {
        return wristOrientation;
    }

    public String getStringWristOrientation() {
        return String.valueOf(wristOrientation);
    }

    public void setWristOrientation(Quaternion wristOrientation) {
        this.wristOrientation = wristOrientation;
    }

    public TrajectoryDescription getTrajectory() {
        return trajectory;
    }

    public void setTrajectory(TrajectoryDescription trajectory) {
        this.trajectory = trajectory;
    }

    public boolean isWristOrientationGlobal() {
        return orientationGlobal;
    }

    public void setWristOrientationGlobal(boolean b) {
        orientationGlobal = b;
    }

    public double getOpenness() {
        return openness;
    }

    public void setOpenness(double openness) {
        this.openness = openness;
    }

    public boolean isHandShapeOverridable() {
        return shapeOverridable;
    }

    public void setHandShapeOverridable(boolean b) {
        shapeOverridable = b;
    }

    public boolean isWristOrientationOverridable() {
        return wristOrientationOverridable;
    }

    public void setWristOrientationOverridable(boolean b) {
        wristOrientationOverridable = b;
    }

    @Override
    public String toString() {
        return (position!=null?position.toString():"")+" - "+shapeName;
    }
}
