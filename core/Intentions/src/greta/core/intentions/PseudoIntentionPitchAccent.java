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
