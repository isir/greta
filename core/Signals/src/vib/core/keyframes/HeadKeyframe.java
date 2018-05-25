/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.keyframes;

import vib.core.signals.SpineDirection;
import vib.core.signals.SpinePhase;

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

}
