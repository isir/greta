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
package greta.core.keyframes;

import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.util.math.Function;

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
