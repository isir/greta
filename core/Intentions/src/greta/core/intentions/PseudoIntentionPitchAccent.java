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
package greta.core.intentions;

import greta.core.util.speech.PitchAccent;

/**
 * This class allows the Intent planner (or other) to use {@code greta.core.util.speech.PitchAccent} has an intention
 * @author Andre-Marie Pez
 * @see greta.core.util.speech.PitchAccent
 */
public class PseudoIntentionPitchAccent extends PitchAccent implements Intention{

    public PseudoIntentionPitchAccent(PitchAccent p){
        super(p);
    }

    @Override
    public String getName() {
        return "pitchaccent";
    }

    @Override
    public String getType() {
        return PitchAccent.stringOfType(this.getPitchAccentType());
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

    @Override
    public String getTarget (){
        return null;
    }

}
