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

package vib.core.intentions;

import vib.core.util.CharacterManager;
import vib.core.util.speech.Speech;

/**
 * This class allows the Intent planner (or other) to use {@code vib.core.util.speech.Speech} has an intention
 * @author Andre-Marie Pez
 * @see vib.core.util.speech.Speech
 */
public class PseudoIntentionSpeech extends Speech implements Intention{

    public PseudoIntentionSpeech(CharacterManager cm){
        super(cm);
    }

    public PseudoIntentionSpeech(CharacterManager cm,Speech s){
        super(cm,s);
    }

    @Override
    public String getName() {
        return "speech";
    }

    @Override
    public String getType() {
        return "speech";
    }

    @Override
    public double getImportance() {
        return 0.5;
    }

    @Override
    public boolean hasCharacter() {
        return false;
    }

    @Override
    public String getCharacter() {
        return null;
    }

    public String getMode() {
        return "replace";
    }
}
