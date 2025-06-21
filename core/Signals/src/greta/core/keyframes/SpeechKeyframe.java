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

/**
 *
 * @author Quoc Anh Le
 */
public class SpeechKeyframe   extends ParametersKeyframe{
    String wavFilePath;
    
    private String parentId;

    public SpeechKeyframe(double time, String wavFilePath){
        this.modality = "speech";
        this.category = "speech";
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
        // throw new UnsupportedOperationException("Not supported yet.");
        return this.category;
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

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parParentId) {
        this.parentId = parParentId;
    }

}
