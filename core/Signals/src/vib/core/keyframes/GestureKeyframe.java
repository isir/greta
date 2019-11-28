/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.core.keyframes;

import vib.core.signals.gesture.Hand;
import vib.core.signals.gesture.TrajectoryDescription;
import vib.core.util.enums.Side;

/**
 *
 * @author Quoc Anh Le
 */
public class GestureKeyframe extends ParametersKeyframe{


    Side handSide; // LEFT or RIGHT or BOTH
    Hand hand;
    boolean isScript;
    String scriptName;
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
        modality = "gesture";
        this.onset = onset;
        this.offset = offset;
        this.isScript = isScript;
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


}
