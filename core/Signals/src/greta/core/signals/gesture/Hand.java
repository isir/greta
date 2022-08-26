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
