/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.tts.marytts;

import java.util.HashMap;
import java.util.Map;
import greta.core.util.CharacterManager;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.log.Logs;
import greta.core.util.speech.Boundary;
import greta.core.util.speech.Phoneme.PhonemeType;
import greta.core.util.speech.PitchAccent;
import greta.core.util.speech.Speech;
import greta.core.util.time.SynchPoint;
import greta.core.util.time.TimeMarker;

/**
 * Contains useful constants and functions with <a href="http://mary.dfki.de">MaryTTS</a>.
 * @author Andre-Marie Pez
 */
public class MaryTTSConstants extends CharacterDependentAdapter{


    /** Mary's parameter : used when sending a simple text */
    public static final String IN_TYPE_TEXT = "TEXT";

    /** Mary's parameter : used when sending the text in MaryXML format */
    public static final String IN_TYPE_MARYXML = "RAWMARYXML";

    /** Mary's parameter : used to obtain an audio stream */
    public static final String OUT_TYPE_AUDIO = "AUDIO";

    /** Mary's parameter : used to obtain an XML tree containing phonemes and timing informations */
    public static final String OUT_TYPE_PARAMS =
            "REALISED_ACOUSTPARAMS";
            //"ACOUSTPARAMS";

    /** Mary's parameter : used to obtain the audio stream in Wave format */
    public static final String AUDIO_TYPE_WAVE = "WAVE";

    /** Mary's parameter : used to obtain the audio stream in MP3 format */
    public static final String AUDIO_TYPE_MP3 = "MP3";

    /** Correspondence array of boundaries between Greta and Mary */
    public static final String[] BOUNDARY = {"L-", "H-", "L-%", "L-H%", "H-%", "H-^H%"};

    /** Correspondence array of pitch accents between Greta and Mary */
    public static final String[] PITCHACCENT = {"L*", "L*+H", "L+H*", "H*+L", "H+L*", "H*"};

    /** Correspondence map of phoneme between Greta and Mary */
    private static final Map<String,PhonemeType[]> correspondingPhonemes = new HashMap<String,PhonemeType[]>();

    /**
     * Static constructor.<br/>
     * Fills the phoneme corresponding map.
     */
    static{
        pho("E", PhonemeType.E1);
        pho("A", PhonemeType.O1);
        pho("EI", PhonemeType.e, PhonemeType.i);
        pho("V", PhonemeType.a1);
        pho("@U", PhonemeType.o1);
        pho("U", PhonemeType.u1);
        pho("O", PhonemeType.a, PhonemeType.o);
        pho("r=", PhonemeType.e);
        pho("j", PhonemeType.y);
        pho("u", PhonemeType.u);
        pho("T", PhonemeType.th);
        pho("{", PhonemeType.e1);
        pho("i", PhonemeType.i1);
        pho("D", PhonemeType.d);
        pho("dZ", PhonemeType.tS);
        pho("AI", PhonemeType.a, PhonemeType.i);
        pho("aU", PhonemeType.a1);
        pho("I", PhonemeType.i);
        pho("@", PhonemeType.a);
        pho("S", PhonemeType.SS);
        pho("N", PhonemeType.g);
        pho("Z", PhonemeType.tS);
        pho("h", PhonemeType.pause); // ?
        pho("OI", PhonemeType.o, PhonemeType.i);
        pho("?", PhonemeType.pause);
        pho("x", PhonemeType.r);// correspond to the german "ch"
        pho("a:", PhonemeType.a1);
        pho("o:", PhonemeType.o1);
        pho("e:", PhonemeType.e);
        pho("o~", PhonemeType.o);//correspond to french "on"
        pho("R", PhonemeType.r);//correspond to french "r"
        pho("H", PhonemeType.u);//correspond to french semi-vowel "hu" in "huit"
        pho("9", PhonemeType.E1);//correspond to french "eu" in "neuf"
        pho("9~", PhonemeType.E1);
        pho("e~", PhonemeType.e);
        pho("a~", PhonemeType.a);
        pho("0", PhonemeType.O1);
    }


    public MaryTTSConstants(CharacterManager cm){
        setCharacterManager(cm);
    };

    /**
     * Converts a Greta's {@code Speech} object to MaryXML format.
     * @param s the {@code Speech}
     * @param lang the Mary's language-code
     * @return the corresponding MaryXML
     * @see #toMaryTTSLang(java.lang.String, int) to get the Mary's language-code
     */
    //spike REVERIE pitch +50 rate +10
    public static String toMaryXML(Speech s, String lang){
        String maryXML = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<maryxml version=\"0.4\"\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\nxmlns=\"http://mary.dfki.de/2002/MaryXML\"\nxml:lang=\""+lang+"\">\n<p><prosody rate=\"+0%\" pitch=\"+0%\" range=\"+0%\" volume=\"loud\">\n";
        for(Object o : s.getSpeechElements()){
            if(o instanceof String) {
                maryXML += (String)o + "\n";
            }
            if(o instanceof TimeMarker){
                TimeMarker tm = (TimeMarker)o;
                //skip the TimeMarker "start"
                if(tm.getName().equalsIgnoreCase("start")) {
                    continue;
                }
                //end of a pitch accent ?
                for(PitchAccent p : s.getPitchAccent()){
                    if(isref(s, tm, p.getEnd())){
                        maryXML += "</t>\n";
                        break;
                    }
                }

                //add the marker
                maryXML += "\t<mark name='"+tm.getName()+"'/>\n";

                //begin of a boubary
                for(Boundary b : s.getBoundaries()){
                    if(isref(s, tm, b.getStart())){
                        maryXML += "<boundary tone='"+BOUNDARY[b.getBoundaryType()]+"' duration='"+(int)(b.getDuration()*1000)+"'/>\n";
                        break;
                    }
                }
                //begin of a pitch accent
                for(PitchAccent p : s.getPitchAccent()){
                    if(isref(s, tm, p.getStart())){
                        maryXML += "<t accent='"+PITCHACCENT[p.getPitchAccentType()]+"'>\n";
                        break;
                    }
                }
            }
        }
        maryXML += "</prosody></p></maryxml>";
        return maryXML;
    }

    /**
     * Call the {@code CharacterManager} to get the MaryTTS's language-code corresponding to the voice of the current character.<br/>
     * the parameter called is : {@code "MARYTTS_"+language.substring(0, 2)+"_LANG"}<br/>
     * i.e. {@code MARYTTS_EN_LANG}<br/>
     * If no definition for this parameter is found, it returns the english language-code of the corresponding version.
     * @param language the language of the {@code Speech}
     * @return the MaryTTS's language-code of the current character
     */
    public String toMaryTTSLang(String language){
        String lang = getCharacterManager().getValueString("MARYTTS_"+language.substring(0, 2)+"_LANG");
        return lang.isEmpty() ? getCharacterManager().getValueString("MARYTTS_EN_LANG") : lang;
    }

    /**
     * Call the {@code CharacterManager} to get the MaryTTS's voice corresponding to the current character.<br/>
     * the parameter called is : {@code "MARYTTS_"+language.substring(0, 2)+"_VOICE"}<br/>
     * i.e. {@code MARYTTS_EN_VOICE}<br/>
     * If no definition for this parameter is found, it returns the english voice of the corresponding version.
     * @param language the language of the {@code Speech}
     * @return the MaryTTS's voice of the current character
     */
    public  String toMaryTTSVoice(String language){
        String voice = getCharacterManager().getValueString("MARYTTS_"+language.substring(0, 2)+"_VOICE");
        return voice.isEmpty() ? getCharacterManager().getValueString("MARYTTS_EN_VOICE") : voice;
    }

    /**
     * Returns the sequence of Greta's {@code phonenes} corresponding to the specified Mary's phoneme.
     * @param maryPhoneme the Mary's phoneme
     * @return a sequence of Greta's {@code phonemes}
     */
    public static PhonemeType[] convertPhoneme(String maryPhoneme){
        PhonemeType[] toReturn = correspondingPhonemes.get(maryPhoneme);
        if(toReturn==null){
            PhonemeType pho;
            try{
                pho = PhonemeType.valueOf(maryPhoneme);
            }catch(IllegalArgumentException iae){
                Logs.warning(MaryTTSConstants.class.getName()+" unknown phoneme : "+maryPhoneme);
                pho = PhonemeType.pause; //default value ?
            }
            toReturn = new PhonemeType[] {pho};
        }
        return toReturn;

    }

    /**
     * function to add corresponding phonemes in the map
     * @param maryPhoneme the Mary's phoneme
     * @param phoneme the Greta's phoneme
     */
    private static void pho(String maryPhoneme, PhonemeType phoneme){
        PhonemeType[] phonemes = {phoneme};
        correspondingPhonemes.put(maryPhoneme, phonemes);
    }
    /**
     * function to add corresponding phonemes in the map<br/>
     * used when a Mary's one correspond to a sequence of two Greta's phonemes
     * @param maryPhoneme the Mary's phoneme
     * @param phoneme1 first Greta's phoneme
     * @param phoneme2 second Greta's phoneme
     */
    private static void pho(String maryPhoneme, PhonemeType phoneme1, PhonemeType phoneme2){
        PhonemeType[] phonemes = {phoneme1, phoneme2};
        correspondingPhonemes.put(maryPhoneme, phonemes);
    }

    /**
     * Check if a {@code TimeMarker} refer to an other {@code TimeMarker} in the speech.
     * @param s the {@code Speech} object where the speech {@code TimeMarker} comes from.
     * @param speechTM the speech {@code TimeMarker}.
     * @param other the {@code TimeMarker} to check.
     * @return {@code true} if the other {@code TimeMarker} refer to the speech {@code TimeMarker}, {@code false} oherwise.
     */
    private static boolean isref(Speech s, TimeMarker speechTM, TimeMarker other){
        SynchPoint sp = other.getFirstSynchPointWithTarget();
        if(sp==null) {
            return false;
        }
        String targetName = sp.getTargetName();
        String sourceName = targetName.substring(0,targetName.indexOf(':'));
        if(s.getId().equalsIgnoreCase(sourceName)){
            String name = targetName.substring(targetName.indexOf(':')+1);
            if(speechTM.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCharacterChanged() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
