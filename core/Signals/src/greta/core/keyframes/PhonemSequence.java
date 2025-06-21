/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    
    private String parentId;

    public PhonemSequence(Speech speech){
        this(
                speech.getId()+"_phonems",
                speech.getPhonems(),
                speech.getStart().getValue(),
                speech.getId()
             );
    }

    public PhonemSequence(String id, List<Phoneme> phonems, double startTime){
        this.id = id;
        this.phonems = phonems;
        this.offset = startTime;
        this.onset = 0;
    }
    
    public PhonemSequence(String id, List<Phoneme> phonems, double startTime, String parParentId){
        this.id = id;
        this.phonems = phonems;
        this.offset = startTime;
        this.onset = 0;
        this.parentId = parParentId;
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

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parParentId) {
        this.parentId = parParentId;
    }

}
