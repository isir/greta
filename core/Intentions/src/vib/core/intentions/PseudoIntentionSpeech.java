/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.intentions;

import vib.core.util.speech.Speech;

/**
 * This class allows the Intent planner (or other) to use {@code vib.core.util.speech.Speech} has an intention
 * @author Andre-Marie Pez
 * @see vib.core.util.speech.Speech
 */
public class PseudoIntentionSpeech extends Speech implements Intention{

    public PseudoIntentionSpeech(){
        super();
    }

    public PseudoIntentionSpeech(Speech s){
        super(s);
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
