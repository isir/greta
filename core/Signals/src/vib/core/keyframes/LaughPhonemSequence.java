/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

import java.util.List;
import vib.core.util.laugh.Laugh;
import vib.core.util.laugh.LaughPhoneme;

/**
 *
 * @author Andre-Marie Pez
 * @author Yu Ding
 */
public class LaughPhonemSequence implements Keyframe{

    private String id;
    private double onset;
    private double offset;
    private final List<LaughPhoneme> phonems;

    public LaughPhonemSequence(Laugh laugh){
        this(
                laugh.getId()+"_laugh_phonems",
                laugh.getLaughPhonemes(),
                laugh.getStart().getValue()
             );
    }

    public LaughPhonemSequence(String id, List<LaughPhoneme> phonems, double startTime){
        this.id = id;
        this.phonems = phonems;
        this.offset = startTime;
        this.onset = 0;
    }

    public List<LaughPhoneme> getLaughPhonems(){
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
        return "laugh";
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
        for(LaughPhoneme pho : phonems){
            duration += pho.getDuration();
        }
        return duration;
    }

}

