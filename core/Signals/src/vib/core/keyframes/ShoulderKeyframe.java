/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class ShoulderKeyframe extends ParametersKeyframe {

    //two directions are possible
    //eqch direction takes a value
    //up ... 0...1
    //back front ... -1...1
    /**
     * relative value of displacement on Y
     */
    public double front;

    /**
     * relative value of displacement on X
     */
    public double up;

    /**
     * Symmetric, Assymetric, Left, Right
     */
    public String side;

    public ShoulderKeyframe(String id, String category, String phase, double time, String side, double front, double up) {

        this.id = id;
        this.modality = "shoulder";
        this.category = category;
        this.phaseType = phase;
        this.onset = 0;
        this.offset = time;

        this.side = side;

        this.front = front;
        this.up = up;

    }

    public ShoulderKeyframe(String id, String category, String phase, double time) {

        this.id = id;
        this.modality = "shoulder";
        this.category = category;
        this.phaseType = phase;
        this.onset = 0;
        this.offset = time;

        this.side = "both";

        this.front = 0;
        this.up = 0;
    }

    public ShoulderKeyframe(String id, String category, String phase, double time, String side) {

        this.id = id;
        this.modality = "shoulder";
        this.category = category;
        this.phaseType = phase;
        this.onset = 0;
        this.offset = time;

        this.side = side;

        this.front = 0;
        this.up = 0;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public void setFront(double front) {
        this.front = front;
    }

    public void setUp(double up) {
        this.up = up;
    }

    public double getFront() {
        return front;
    }

    public double getUp() {
        return up;
    }

    public String getSide() {
        return side;
    }
}//end of class