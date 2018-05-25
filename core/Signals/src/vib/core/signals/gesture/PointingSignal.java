/*
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
