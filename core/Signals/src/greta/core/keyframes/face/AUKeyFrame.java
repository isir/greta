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
package greta.core.keyframes.face;

import greta.core.keyframes.Keyframe;
import greta.core.repositories.AUAPFrame;

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
    
    private String parentId;
    
    private String modality;
    private String category;
    private String phase;

    public AUKeyFrame(String id, double time, AUAPFrame frame) {

        this(id, time, frame, null);
        
    }
    
    public AUKeyFrame(String id, double time, AUAPFrame frame, String parParentId) {
        this.id = id;
        //this.time = time;
        this.onset=0;
        this.offset=time;
        this.frame = frame;
        this.parentId = parParentId;
        
        this.modality = "face";
        this.category = "face";
        
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
        return this.modality;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPhaseType() {
        return this.phase;
    }

    @Override
    public String getCategory() {
        return this.category;
    }

    public AUAPFrame getAus(){
        return frame;
    }
    
    @Override
    public String getParentId(){
        return parentId;
    }
    
    @Override
    public void setParentId(String parParentId){
        this.parentId = parParentId;
    }
}
