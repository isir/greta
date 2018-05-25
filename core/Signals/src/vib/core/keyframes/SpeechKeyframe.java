/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.keyframes;

/**
 *
 * @author Quoc Anh Le
 */
public class SpeechKeyframe   extends ParametersKeyframe{
    String wavFilePath;

    public SpeechKeyframe(double time, String wavFilePath){
        modality = "speech";
        this.onset = time;
        this.wavFilePath = wavFilePath;
    }

    public String getFileName(){
        return this.wavFilePath;
    }

    public double getOffset() {
        return this.offset;
    }

    public double getOnset() {
        return this.onset;
    }

    public String getModality() {
        return this.modality;
    }

    public String getId() {
        return this.id;
    }

    public String getPhaseType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getCategory() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSPC() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getTMP() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getPWR() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getFLD() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getSTF() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
