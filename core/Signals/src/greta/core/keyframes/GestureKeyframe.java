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

import greta.core.signals.gesture.Hand;
import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.util.enums.Side;

/**
 *
 * @author Quoc Anh Le
 */
public class GestureKeyframe extends ParametersKeyframe{


    Side handSide; // LEFT or RIGHT or BOTH
    Hand hand;
    boolean isScript;
    String scriptName;
    
    String parentId;
//    Hand leftHand;
//    Hand rightHand;

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public GestureKeyframe(String id, String phaseType, TrajectoryDescription trajectory, double onset, double offset, Hand hand, String scriptName, boolean isScript){
        this.id = id; // gesture id, not keyframe id
        this.phaseType = phaseType;
        this.trajectoryType = trajectory;

        if(this.phaseType.equalsIgnoreCase("pre-stroke-hold")||this.phaseType.equalsIgnoreCase("post-stroke-hold")){
            this.trajectoryType.setName("LINEAR");
        }

        this.hand = hand;
        this.handSide = hand.getSide();
        this.modality = "gesture";
        this.category = "gesture";
        this.onset = onset;
        this.offset = offset;
        this.isScript = isScript;
    }
    
    public GestureKeyframe(String id, String phaseType, TrajectoryDescription trajectory, double onset, double offset, Hand hand, String scriptName, boolean isScript, String parParentId){
        this.id = id; // gesture id, not keyframe id
        this.phaseType = phaseType;
        this.trajectoryType = trajectory;

        if(this.phaseType.equalsIgnoreCase("pre-stroke-hold")||this.phaseType.equalsIgnoreCase("post-stroke-hold")){
            this.trajectoryType.setName("LINEAR");
        }

        this.hand = hand;
        this.handSide = hand.getSide();
        modality = "gesture";
        this.onset = onset;
        this.offset = offset;
        this.isScript = isScript;
        
        this.parentId = parParentId;
    }

    public boolean isIsScript() {
        return isScript;
    }

    public void setIsScript(boolean isScript) {
        this.isScript = isScript;
    }

    public Hand getHand(){
        return this.hand;
    }

    public Side getSide(){
        return this.handSide;
    }

    /*
    public GestureKeyframe(String phaseType, String category, double onset, double offset, Hand lHand, Hand rHand){
        this.handSide = "BOTH";
        modality = "GESTURE";
        this.phaseType = phaseType;
        this.category = category;
        leftHand = lHand;
        rightHand = rHand;
        hand = lHand;
        this.onset = onset;
        this.offset = offset;
        this.id = modality.concat(String.valueOf(this.getOffset()));
    }

    public Hand getLeftHand(){
        return this.leftHand;
    }

    public Hand getRightHand(){
        return this.rightHand;
    }
    */
    public void setId(){
        id = modality.concat(String.valueOf(this.getOffset()));
    }

    @Override
    public String getModality() {
        return modality;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public double getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(double time) {
        this.offset = time;
    }

    @Override
    public double getOnset() {
        return this.onset;
    }

    @Override
    public void setOnset(double time) {
        this.onset = time;
    }

    @Override
    public String getPhaseType() {
        return this.phaseType;
    }

    @Override
    public String getCategory() {
        return this.category;
    }

    @Override
    public String toString() {
        return handSide+":  t="+offset+" "+hand;
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
