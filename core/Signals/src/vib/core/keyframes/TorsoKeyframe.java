/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

import java.util.HashMap;
import vib.core.signals.SpineDirection;
import vib.core.signals.SpinePhase;
import vib.core.util.math.Quaternion;

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

}
