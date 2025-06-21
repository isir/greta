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

/**
 *
 * @author Quoc Anh Le
 */
public class HeadKeyframe extends ParametersKeyframe {

    /** Rotation around the y(up) axis */
    public SpineDirection verticalTorsion;

    /** Rotation around the x(left) axis */
    public SpineDirection sagittalTilt;

    /** Rotation around the z(forward) axis */
    public SpineDirection lateralRoll;

    private String parentId;
    /**
     * Constructs a new {@code HeadKeyframe}
     * @param id the identifier of this {@code HeadKeyframe}.
     * @param phase the corresponding {@code SpinePhase}
     * @param category ?
     */
    public HeadKeyframe(String id, SpinePhase phase, String category) {
        this.id = id;
        this.modality = "head";
//        this.category = phase.verticalTorsion.direction.toString() + phase.sagittalTilt.direction + phase.lateralRoll.direction;
//        if (this.category.isEmpty()) {
            this.category = "Neutral";
//        }
        this.onset = phase.getStartTime();
        this.offset = phase.getEndTime();
        verticalTorsion = new SpineDirection(phase.verticalTorsion);
        sagittalTilt = new SpineDirection(phase.sagittalTilt);
        lateralRoll = new SpineDirection(phase.lateralRoll);
    }

    /**
     * Copy constructor.
     * @param other the {@code HeadKeyframe} to copy.
     */
    public HeadKeyframe(HeadKeyframe other) {
        id = other.id;
        modality = other.modality;
        category = other.category;
        onset = other.onset;
        offset = other.offset;
        verticalTorsion = new SpineDirection(other.verticalTorsion);
        sagittalTilt = new SpineDirection(other.sagittalTilt);
        lateralRoll = new SpineDirection(other.lateralRoll);
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
    public String getModality() {
        return this.modality;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getPhaseType() {
        return this.phaseType;
    }

    @Override
    public String getCategory() {
        return this.category;
    }

    public double getSignedSagittalTilt() {
        return sagittalTilt.direction == SpineDirection.Direction.FORWARD ? sagittalTilt.value : -(sagittalTilt.value);
    }

    public double getSignedVerticalTorsion() {
        return verticalTorsion.direction == SpineDirection.Direction.LEFTWARD ? verticalTorsion.value : -(verticalTorsion.value);
    }

    public double getSignedLateralRoll() {
        return lateralRoll.direction == SpineDirection.Direction.RIGHTWARD ? lateralRoll.value : -(lateralRoll.value);
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
