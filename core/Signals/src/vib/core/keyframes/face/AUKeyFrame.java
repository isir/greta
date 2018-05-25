/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes.face;

import vib.core.keyframes.Keyframe;
import vib.core.repositories.AUAPFrame;

/**
 *
 * @author Andre-Marie Pez
 */
public class AUKeyFrame implements Keyframe{

    //private double time;
    private double onset;
    private double offset;
    private AUAPFrame frame;
    private String id;

    public AUKeyFrame(String id, double time, AUAPFrame frame) {
        this.id = id;
        //this.time = time;
        this.onset=0;
        this.offset=time;
        this.frame = frame;
    }

    @Override
    public double getOffset() {
        return offset;
    }

    @Override
    public void setOffset(double time) {
        this.offset = time;
    }

    @Override
    public double getOnset() {
        return onset;
    }

    @Override
    public void setOnset(double time) {
        this.onset = time;
    }

    @Override
    public String getModality() {
        return "face";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPhaseType() {
        return "";
    }

    @Override
    public String getCategory() {
        return "";
    }

    public AUAPFrame getAus(){
        return frame;
    }
}
