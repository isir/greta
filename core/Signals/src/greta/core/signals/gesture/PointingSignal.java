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
package greta.core.signals.gesture;

import greta.core.signals.SignalTargetable;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.enums.GazeDirection;
import greta.core.util.enums.Side;

/**
 *
 * @author Elisabetta
 */
public class PointingSignal extends GestureSignal implements SignalTargetable, CharacterDependent {
    private String origin;
    private String target;
    private Side mode;
    private GazeDirection offsetDirection = null;
    private Double offsetAngle = null;

    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        this.characterManager = characterManager;
    }

    @Override
    public void onCharacterChanged() {
        //set the current library to use :
        origin= getCharacterManager().getCurrentCharacterId();
    }

    public PointingSignal(String id) {
        super(id);
        this.target = "";
        this.mode = Side.RIGHT;
        origin=getCharacterManager().getCurrentCharacterId();
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
