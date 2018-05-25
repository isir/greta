/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.speech;

import vib.core.util.audio.Audio;
import vib.core.util.log.Logs;
import vib.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is not a real TTS. It can not computes audio or phonems. It can juste estimate {@code TimeMarkers} of a {@code Speech}.
 * @author Andre-Marie Pez
 */
public class FakeTTS implements TTS{

    private static final double vowelDuration = 0.12;
    private static final double consonantDuration = 0.04;
    private static final double digitDuration = 0.3;

    private Speech speech;
    private boolean interreuptionReactionSupported = false;

    @Override
    public void setSpeech(Speech speech) {
        this.speech = speech;
    }

    @Override
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonems) {
        if(doTemporize){
            TimeMarker start = speech.getStart();
            double startTime = start.getValue();
            double duration = 0;
            for(Object o : speech.getSpeechElements()){
                if(o instanceof TimeMarker && o != start) {
                    ((TimeMarker)o).setValue(startTime + duration);
                }
                if(o instanceof Boundary){
                    duration += ((Boundary)o).getDuration();
                }
                if(o instanceof String){
                    for(int i=0; i<((String) o).length();++i) {
                        duration += durationOf(((String) o).charAt(i));
                    }
                }
            }
        }
        if(doAudio) {
            Logs.warning(this.getClass().getName()+" : can not compute audio buffer");
        }
        if(doPhonems) {
            Logs.warning(this.getClass().getName()+" : can not compute phonems");
        }
    }

    @Override
    public List<Phoneme> getPhonemes() {
        Logs.warning(this.getClass().getName()+" does not return any phonem.");
        return new ArrayList<Phoneme>();
    }

    @Override
    public Audio getAudio() {
        return Audio.getEmptyAudio();
    }
    
    @Override
    public boolean isInterruptionReactionSupported() {
        return interreuptionReactionSupported;
    }


    /**
     * Returns the duration of letter or a digit.<br/>
     * If {@code c} is neither a letter nor a digit, it returns 0.
     * @param c the letter or the digit
     * @return the duration of {@code c}
     */
    public static double durationOf(char c){
        //letter :
        //upper case :
        if('A'<=c && c<='Z'){
            //vovel :
            if(c=='A' || c=='E' || c=='I' || c=='O' || c=='U' || c=='Y') {
                return vowelDuration;
            }
            //consonant :
            else {
                return consonantDuration;
            }
        }
        //lower case :
        else {
            if('a'<=c && c<='z'){
                //vovel :
                if(c=='a' || c=='e' || c=='i' || c=='o' || c=='u' || c=='y') {
                    return vowelDuration;
                }
                //consonant :
                else {
                    return consonantDuration;
                }
            }
            //accents
            else{
                if(191<c && c<256 && c!=247 && c!=215){
                    //consonant :
                    if(c==199 || c==208 || c==209 || c==222 || c==223 || c==231 || c==240 || c==241 || c==254) {
                        return consonantDuration;
                    }
                    //vovel :
                    else{
                        if(c!=247 && c!=215) {
                            return vowelDuration;
                        }
                    }
                }
                //non letter :
                //digit :
                else{
                    if('0'<=c && c<='9') {
                        return digitDuration;
                    }
                }
            }
        }
        return 0;
    }

}
