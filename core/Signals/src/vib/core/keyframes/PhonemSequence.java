/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

import vib.core.util.speech.Phoneme;
import vib.core.util.speech.Speech;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class PhonemSequence implements Keyframe{

    private String id;
    private double onset;
    private double offset;
    private final List<Phoneme> phonems;

    public PhonemSequence(Speech speech){
        this(
                speech.getId()+"_phonems",
                speech.getPhonems(),
                speech.getStart().getValue()
             );
    }

    public PhonemSequence(String id, List<Phoneme> phonems, double startTime){
        this.id = id;
        this.phonems = phonems;
        this.offset = startTime;
        this.onset = 0;
    }

    public List<Phoneme> getPhonems(){
        return phonems;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double time) {
        offset = time;
    }

    public double getOnset() {
        return onset;
    }

    public void setOnset(double time) {
        onset = time;
    }

    public String getModality() {
        return "speech";
    }

    public String getId() {
        return id;
    }

    public String getPhaseType() {
        return "phonem";
    }

    public String getCategory() {
        return "phonem";
    }

    public String getTrajectoryType() {
        return "phonem";
    }

    public double getDuration(){
        double duration = 0;
        for(Phoneme pho : phonems){
            duration += pho.getDuration();
        }
        return duration;
    }

}
