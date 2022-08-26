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

import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 * andre marie pez knows better this class.
 * use to generate special trajectory : such as circle, spiral, line, etc
 */
public class TrajectoryDescription {
    protected boolean isUsed = false;
    static double PIx2 = (java.lang.Math.PI * 2.0);
    static double PIon2 = (java.lang.Math.PI / 2);
    static double PIon2x3 = (3 * PIon2);
    protected String _name = "LINEAR";
    protected double[] amplitude = {0, 0, 0};
    protected double[] frequency = {1, 1, 1};
    protected double[] shift = {0, 0, 0};
    protected int levelofDetail = 0;
    protected RotationDirection rotation = RotationDirection.CLOCKWISE;
    protected Direction startDirection = Direction.UP; //not use for a CUSTOM movement, only UP and DOWN for WAVE (else, it's like UP)
    protected Variation[] spatialVariation = {Variation.NONE, Variation.NONE, Variation.NONE};
    protected Variation[] temporalVariation = {Variation.NONE, Variation.NONE, Variation.NONE};//temporal variation. SMALLER is slacken, GREATER is quicken
    protected Vec3d startPosition;
    protected Vec3d endPosition;

    public TrajectoryDescription(){}

    public TrajectoryDescription(TrajectoryDescription t){
        _name = t._name;
        amplitude = t.amplitude;
        frequency = t.frequency;
        shift = t.shift;
        levelofDetail = t.levelofDetail;
        rotation = t.rotation;
        startDirection = t.startDirection;
        spatialVariation = t.spatialVariation;
        temporalVariation = t.temporalVariation;
        startPosition = t.startPosition;
        endPosition = t.endPosition;
        isUsed = t.isUsed;
    }

    public enum Variation {

        SMALLER, NONE, GREATER
    };

    public enum SideType {

        r, l, no_side, both_sides, assym
    };

    enum Direction {

        UP, DOWN, INTERN, EXTERN
    };

    public enum Movement {

        WAVE, CIRCLE, SPIRAL_OUT, SPIRAL_IN, CUSTOM
    };

    public enum Orientation {

        SAGITTAL, TRANSVERSAL, FRONTAL
    }; //plane

    public enum RotationDirection {

        CLOCKWISE,
        COUNTCLOCKWISE
    };


    public String getName() {
        return _name;
    }

    public void setName(String _name) {
        this._name = new String(_name);
    }

    public double[] getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(double[] amplitude) {
        this.amplitude = amplitude.clone();
    }

    public double[] getFrequency() {
        return frequency;
    }

    public void setFrequency(double[] frequency) {
        this.frequency = frequency.clone();
    }

    public int getLevelofDetail() {
        return levelofDetail;
    }

    public void setLevelofDetail(int levelofDetail) {
        this.levelofDetail = levelofDetail;
    }

    public RotationDirection getRotation() {
        return rotation;
    }

    public void setRotation(RotationDirection rotation) {
        this.rotation = rotation;
    }

    public double[] getShift() {
        return shift;
    }

    public void setShift(double[] shift) {
        this.shift = shift.clone();
    }

    public Variation[] getSpatialVariation() {
        return spatialVariation;
    }

    public void setSpatialVariation(Variation[] spatialVariation) {
        this.spatialVariation = spatialVariation.clone();
    }

    public Direction getStartDirection() {
        return startDirection;
    }

    public void setStartDirection(Direction startDirection) {
        this.startDirection = startDirection;
    }

    public Variation[] getTemporalVariation() {
        return temporalVariation;
    }

    public void setTemporalVariation(Variation[] temporalVariation) {
        this.temporalVariation = temporalVariation.clone();
    }

    public Vec3d getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Vec3d endPosition) {
        this.endPosition = endPosition;
    }

    public Vec3d getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Vec3d startPosition) {
        this.startPosition = startPosition;
    }

    double S(float x, int axe) { //Computes the Spatial variation
        float s = 1;
        if (spatialVariation[axe] == Variation.NONE) {
            s = 1;
        }
        if (spatialVariation[axe] == Variation.GREATER) {
            s = x;
        }
        if (spatialVariation[axe] == Variation.SMALLER) {
            s = 1.0f - x;
        }
        return amplitude[axe] * s;
    }

    float T(float x, int axe) {     //Computes the Temporal variation

        float t = x;
        if (temporalVariation[axe] == Variation.NONE) {
            t = x;
        }
        if (temporalVariation[axe] == Variation.GREATER) {
            t = (float) (1.0f - java.lang.Math.sqrt(1.0d - x)); // quicken
        }
        if (temporalVariation[axe] == Variation.SMALLER) {
            t = (float) java.lang.Math.sqrt(x); // slacken
        }
        return (float) (PIx2 * (frequency[axe] *  t + shift[axe]));
    }

    float F(float x, int axe) {
        return (float) (S(x, axe) * java.lang.Math.sin(T(x, axe)));
    } //Computes the wave forme on one axe

    public float getForAxisX(float x){
        return F(x, 0);
    }

    public float getForAxisY(float y){
        return F(y, 1);
    }

    public float getForAxisZ(float z){
        return F(z, 2);
    }

    public Vec3d getPosition(float frame, SideType side) {
        float relativeTime = frame;
        if (relativeTime < 0) {
            relativeTime = 0;
        }
        Vec3d position = new Vec3d();
        position = Vec3d.addition(
                Vec3d.multiplication(Vec3d.substraction(endPosition, startPosition), relativeTime),
                startPosition);
        Vec3d relativeS =  new Vec3d(F(0, 0), F(0, 1), F(0, 2));
        Vec3d relativeE =  new Vec3d(F(1, 0), F(1, 1), F(1, 2));
        if (side == SideType.l) {
            Vec3d relative = new Vec3d(F(relativeTime, 0), F(relativeTime, 1), F(relativeTime, 2));
            relative.add(Vec3d.multiplication(relativeS, relativeTime - 1));
            relative.add(Vec3d.multiplication(relativeE, - relativeTime));
            position = Vec3d.addition(position, relative);
           // System.out.println("relative pos l :  " + relative);
        } else {
            Vec3d relative = new Vec3d(-F(relativeTime, 0), F(relativeTime, 1), F(relativeTime, 2));
            position = Vec3d.addition(position, relative);
            //System.out.println("relative pos r :  " + relative);
        }
        return position;
    }

    public ArrayList<Vec3d> computeCustomCurve(Vec3d start, Vec3d end, ArrayList<Vec3d> ref) {
        ArrayList<Vec3d> list = new ArrayList<Vec3d>();
        Vec3d dif = Vec3d.substraction(end, start);
        for (Vec3d a : ref) {
            Vec3d newpos = Vec3d.addition(new Vec3d(dif.x() * a.x(), dif.y() * a.y(), dif.z() * a.z()), start);
            list.add(newpos);
        }
        return list;
    }


    public ArrayList<Vec3d> compute(Vec3d start, Vec3d end, int slides, Variation timeVariation) {
        setStartPosition(start);
        setEndPosition(end);
        ArrayList<Vec3d> list = new ArrayList<Vec3d>();
        if (timeVariation == Variation.GREATER) {
            for (int i = 0; i < slides + 1; i++) {
                float time = easeInQuad((float) i / (float) slides);
                Vec3d pos = getPosition(time, SideType.l);
                list.add(pos);
            }

        } else if (timeVariation == Variation.SMALLER) {
            for (int i = 0; i < slides + 1; i++) {
                float time = easeOutQuad((float) i / (float) slides);
                Vec3d pos = getPosition(time, SideType.l);
                list.add(pos);
            }
        } else {
            for (int i = 0; i < slides + 1; i++) {
                Vec3d pos = getPosition((float) i / (float) slides, SideType.l);
                list.add(pos);
            }
        }
        return list;
    }

    public static float easeInQuad(float t) {
        return t * t;
    }

    public static float easeOutQuad(float t) {
        return -t * (t - 2);
    }

    public void setAxisParameter(){

    }

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    public void makeCircle(int A, int B, float radius) {
        amplitude[A] = radius;
        amplitude[B] = radius;
        temporalVariation[0] = Variation.NONE;
        temporalVariation[1] = Variation.NONE;
        temporalVariation[2] = Variation.NONE;
        if (startDirection == Direction.UP) {
            shift[A] = (float) ((3 - 2 * rotation.ordinal()) * PIon2);
        }
        if (startDirection == Direction.EXTERN) {
            shift[B] = (float) ((1 + 2 * rotation.ordinal()) * PIon2);
        }
        if (startDirection == Direction.DOWN) {
            shift[A] = (float) ((1 + 2 * rotation.ordinal()) * PIon2);
            shift[B] = (float) java.lang.Math.PI;
        }
        if (startDirection == Direction.INTERN) {
            shift[A] = (float) java.lang.Math.PI;
            shift[B] = (float) ((3 - 2 * rotation.ordinal()) * PIon2);
        }
    }
}
