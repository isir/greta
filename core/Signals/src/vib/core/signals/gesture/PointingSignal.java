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
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.signals.gesture;

import vib.core.signals.SignalTargetable;
import vib.core.util.CharacterManager;
import vib.core.util.enums.GazeDirection;
import vib.core.util.enums.Side;

/**
 *
 * @author Elisabetta
 */
public class PointingSignal extends GestureSignal implements SignalTargetable {
    private String origin;
    private String target;
    private Side mode;
    private GazeDirection offsetDirection = null;
    private Double offsetAngle = null;

    public PointingSignal(String id) {
        super(id);
        this.target = "";
        this.mode = Side.RIGHT;
        origin=CharacterManager.currentCharacterId;
        target="";
        offsetDirection=GazeDirection.FRONT;
        offsetAngle=0.0;
    }
    
    // constructeur par recopie ?

    @Override
    public String getTarget() {
        return target;
    }
    
    public String getOrigin() {
        return origin;
    }

    @Override
    public void setTarget(String target) {
        this.target = target;
    }
    
    public Side getMode() {
        return mode;
    }

    public void setMode(Side mode) {
        this.mode = mode;
    }
    
}
