/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.intentions;

import vib.core.util.speech.PitchAccent;

/**
 * This class allows the Intent planner (or other) to use {@code vib.core.util.speech.PitchAccent} has an intention
 * @author Andre-Marie Pez
 * @see vib.core.util.speech.PitchAccent
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
}
