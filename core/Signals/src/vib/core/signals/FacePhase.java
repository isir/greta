/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.signals;

/**
 *
 * @author Quoc Anh Le
 */
public class FacePhase {
    double startTime;
    double endTime;
    String type; // start, attack, decay, sustain, end


    //NOT HERE: PHASE does not contains the action units!
    //action units are for a whole facial expressions!
    //i cannot have a action without start, apex or end - nonsense
    //private String visem;
    //private String actionUnit;

    public FacePhase(String type, double startTime, double endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }

    public double getEndTime() {
        return endTime;
    }

    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public void setStartTime(double startTime) {
        this.startTime = startTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
