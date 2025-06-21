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
    
    private String parentId;

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

    @Override
    public String getParentId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setParentId(String parParentId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
