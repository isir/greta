/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.tts.openmary;

import vib.core.util.audio.Audio;
import vib.core.util.log.Logs;
import vib.core.util.speech.Phoneme;
import vib.core.util.speech.Speech;
import vib.core.util.speech.TTS;
import vib.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

//TODO javadoc
/**
 *
 * @author Andre-Marie Pez
 */
public class OpenMarySemaineTTS implements TTS{

    private Speech speech;
    private List<Phoneme> phonemes;
    private double timer;
    private Audio audio;
    private boolean interreuptionReactionSupported = false;

     /**
     * Default constructor.
     */
    public OpenMarySemaineTTS(){
        clean();
    }

    @Override
    public void setSpeech(Speech speech) {
        clean();
        this.speech = speech;
    }
    
    @Override
    public boolean isInterruptionReactionSupported() {
        return interreuptionReactionSupported;
    }

    @Override
    public void  compute(boolean doTemporize, boolean doAudio, boolean doPhonemes) {

        if(doTemporize || doPhonemes){
            //read informations already present in the XML
            XMLTree originalTree = speech.getOriginalXML();
            if(originalTree!=null) {
                extractPhonemes(originalTree);
            }
            else {
                Logs.error(this.getClass().getName()+" : can not read the Speech "+speech.getId());
            }
        }
        if(doAudio) {
            Logs.warning(this.getClass().getName()+" : cant compute audio buffer.");
            audio = Audio.getEmptyAudio();
        }
    }

    @Override
    public List<Phoneme> getPhonemes() {
        return phonemes;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    private void clean(){
        phonemes = new ArrayList<Phoneme>();
        speech = null;
        timer = 0;
        audio = null;
    }

    private void extractPhonemes(XMLTree t){
        //read phonemes
        if(t.getName().equalsIgnoreCase("ph")){
            double duration = t.getAttributeNumber("d") / 1000.0;
            Phoneme.PhonemeType [] phos = OpenMaryConstants.convertPhoneme(t.getAttribute("p"));
            for(Phoneme.PhonemeType pho : phos) {
                phonemes.add(new Phoneme(pho,duration/((double)phos.length)));
            }
            timer += duration;
        }

        //read boundaries
        if(t.getName().equalsIgnoreCase("boundary") && !t.getAttribute("duration").isEmpty()){
            double duration = t.getAttributeNumber("duration")/1000.0;
            phonemes.add(new Phoneme(Phoneme.PhonemeType.pause,duration));
            timer += duration;
        }

        //read time markers
        if(t.getName().equalsIgnoreCase("mark")){
            String realName = t.getAttribute("name").split(":",2)[1];
            speech.getTimeMarker(realName).setValue(timer);
        }

        //same thing on children
        for(XMLTree child : t.getChildrenElement()) {
            extractPhonemes(child);
        }
    }

}
