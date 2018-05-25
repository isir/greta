/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.signals;

import vib.core.util.speech.Speech;

/**
 *
 * @author Andre-Marie Pez
 */
public class SpeechSignal extends Speech implements Signal{
    

    public SpeechSignal(){
        super();
    }

    public SpeechSignal(Speech s){
        super(s);
    }

    public String getModality() {
        return "speech";
    }
}
