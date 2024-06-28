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

import greta.core.util.speech.Phoneme;
import greta.core.util.speech.Speech;
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
