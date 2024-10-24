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
