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
