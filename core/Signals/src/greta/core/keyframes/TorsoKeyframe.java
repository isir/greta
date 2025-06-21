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

import greta.core.signals.SpineDirection;
import greta.core.signals.SpinePhase;
import greta.core.util.math.Quaternion;
import java.util.HashMap;

/**
 *
 * @author Quoc Anh Le
 * @author Brice Donval
 */
public class TorsoKeyframe  extends ParametersKeyframe{

    public SpineDirection verticalTorsion ;
    public SpineDirection sagittalTilt;
    public SpineDirection lateralRoll;
    public SpineDirection collapse;
    public HashMap<String, Quaternion> _rotations = new HashMap<String, Quaternion>();
    boolean onlyshoulder = false;
    
    private String parentId;

    public TorsoKeyframe(String id, SpinePhase phase, String category)
    {
        this.id = id;
        this.modality = "torso";
//        this.category = phase.verticalTorsion.direction.toString() + phase.sagittalTilt.direction + phase.lateralRoll.direction;
//        if (this.category.isEmpty()) {
            this.category = "Neutral";
//        }
        this.onset = phase.getStartTime();
        this.offset = phase.getEndTime();
        this.verticalTorsion = phase.verticalTorsion;
        this.sagittalTilt = phase.sagittalTilt;
        this.lateralRoll = phase.lateralRoll;
        this.collapse = phase.collapse;
        this._rotations.putAll(phase._rotations);
    }

    public TorsoKeyframe(TorsoKeyframe other) {
        id = other.id;
        modality = other.modality;
        category = other.category;

        onset = other.onset;
        offset = other.offset;

        this.verticalTorsion = other.verticalTorsion;
        this.sagittalTilt = other.sagittalTilt;
        this.lateralRoll = other.lateralRoll;
        this.collapse = other.collapse;
        this._rotations.putAll(other._rotations);
    }

    public TorsoKeyframe() {
        id = null;
        modality = null;
        category = null;

        onset = 0.0;
        offset = 0.0;

        this.verticalTorsion = new SpineDirection();
        this.sagittalTilt = new SpineDirection();
        this.lateralRoll = new SpineDirection();
        this.collapse = new SpineDirection();
        this._rotations.putAll(new HashMap<String, Quaternion>());
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
        return this.phaseType;
    }

    public String getCategory() {
        return this.category;
    }

    public double getSignedVerticalTorsion() {
        return verticalTorsion.direction == SpineDirection.Direction.LEFTWARD ? verticalTorsion.value : -(verticalTorsion.value);
    }

    public boolean getonlyshoulder(){
        return onlyshoulder;
    }


    public void setOnlytheShoulder () {
        this.onlyshoulder = true;
    }

    @Override
    public TorsoKeyframe clone() throws CloneNotSupportedException {
        TorsoKeyframe cloneobj = (TorsoKeyframe) super.clone();
        return cloneobj;
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
