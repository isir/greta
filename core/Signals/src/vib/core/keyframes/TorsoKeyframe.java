/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
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
