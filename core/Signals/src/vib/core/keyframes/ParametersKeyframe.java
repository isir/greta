/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

import vib.core.signals.gesture.TrajectoryDescription;
import vib.core.util.math.Function;

/**
 *
 * @author Quoc Anh Le
 */
public abstract class ParametersKeyframe implements Keyframe{
    /* At this time (global) the agent's state should be at keyframe's configuration */
    protected double offset = 0;
    /* the beginning of movement to keyframe's position */
    protected double onset = 0;
    /** modality: gesture, face, toros,... */
    protected String modality;
    /** type: preparation, stroke, retraction.. */
    protected String phaseType;
    /** category */
    protected String category; //beat, iconic,...sadness, joy,..


    /** trajectory type: how to reach the next key-frame from this key-frame */
    protected TrajectoryDescription trajectoryType; // the names to be defined

    protected Function _interpolation;

    public Function getInterpolationFunction() {
        return _interpolation;
    }

    public void setInterpolationFunction(Function interpolation) {
        this._interpolation = interpolation;
    }
    /** keyframes which belong to one gesture have the same id */
    protected String id;

    ExpressivityParameters p = new ExpressivityParameters();

    public void setParameters(ExpressivityParameters p){
        this.p = p;
    }

    public ExpressivityParameters getParameters(){
        return this.p;
    }

    public TrajectoryDescription getTrajectoryType(){
        return this.trajectoryType;
    }

    public void setTrajectoryType(TrajectoryDescription value){
        this.trajectoryType = value;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    public void setOnset(double onset) {
        this.onset = onset;
    }

    public void setPhaseType(String phaseType) {
        this.phaseType = phaseType;
    }

    public double getOffset() {
        return offset;
    }

    public double getOnset() {
        return onset;
    }

    public String getModality() {
        return modality;
    }

    public String getId() {
        return id;
    }

    public String getPhaseType() {
        return phaseType;
    }

    public String getCategory() {
        return category;
    }

}
