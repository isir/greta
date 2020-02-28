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

import greta.core.util.laugh.Laugh;
import greta.core.util.laugh.LaughPhoneme;
import java.util.List;

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
