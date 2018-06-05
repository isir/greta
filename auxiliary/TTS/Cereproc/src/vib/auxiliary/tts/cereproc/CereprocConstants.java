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
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.tts.cereproc;

import com.cereproc.cerevoice_eng.CPRCEN_INTERRUPT_BOUNDARY_TYPE;
import com.cereproc.cerevoice_eng.CPRCEN_INTERRUPT_INTERRUPT_TYPE;
import vib.core.util.IniManager;
import vib.core.util.log.Logs;
import vib.core.util.speech.Phoneme.PhonemeType;
import vib.core.util.speech.TTS;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.UnsupportedAudioFileException;
import static vib.auxiliary.tts.cereproc.CereprocTTS.cereprocSampleRateFloat;
import vib.core.util.enums.interruptions.ReactionDuration;
import vib.core.util.enums.interruptions.ReactionType;

/**
 * Class containing phonemes mapping and constants for the Cereproc TTS VIB implementation
 * @author Andre-Marie Pez
 * @author Mathieu Chollet
 * @author Angelo Cafaro
 */
public class CereprocConstants {

    /**
     * can not be instanciated
     */
    private CereprocConstants(){}

    public static String DEPENDENCIES_PATH;
    public static String VOICES_PATH;

    public static final String DEFAULT_LANGUAGE = "en-GB";
    public static final String DEFAULT_VOICE = "sarah";

    /** Correspondence map of phonemes between VIB and Cereproc */
    public static final Map<String,Map<String,PhonemeType[]>> correspondingPhonemes = new HashMap<String,Map<String,PhonemeType[]>>();   // Map of correspondences for all available languages

    /**
     * Function to add a phoneme-Cereproc correspondence in the map of correspondence depending on the specified language<br/>
     * @param language the Cereproc's language
     * @param cerePhoneme the Cereproc's phoneme
     * @param phoneme the VIB's phoneme
     */
    private static void addVIBCereprocPhonemeCorrespondence(String language, String cerePhoneme, PhonemeType phoneme){
        PhonemeType[] phonemes = {phoneme};
        correspondingPhonemes.get(language).put(cerePhoneme, phonemes);
    }
    /**
     * Function to add a phoneme-Cereproc correspondence in the map of correspondence depending on the specified language<br/>
     * This function is used when a Cereproc's phoneme correspond to a sequence of two VIB's phonemes
     * @param language the Cereproc's language
     * @param cerePhoneme the Cereproc's phoneme
     * @param phoneme1 first VIB's phoneme
     * @param phoneme2 second VIB's phoneme
     */
    private static void addDoubleVIBCereprocPhonemeCorrespondence(String language, String cerePhoneme, PhonemeType phoneme1, PhonemeType phoneme2){
        PhonemeType[] phonemes = {phoneme1, phoneme2};
        correspondingPhonemes.get(language).put(cerePhoneme, phonemes);
    }
    private static boolean initialized = false;

    static {init();}

    public static void init(){

        if(initialized) {
            return;
        }

        DEPENDENCIES_PATH = IniManager.getGlobals().getValueString("CEREPROC_DEPENDENCIES_PATH");
        VOICES_PATH = IniManager.getGlobals().getValueString("CEREPROC_VOICES_PATH");

        correspondingPhonemes.put("en-GB", new HashMap<String,PhonemeType[]>());
        correspondingPhonemes.put("fr-FR", new HashMap<String,PhonemeType[]>());
        correspondingPhonemes.put("de-DE", new HashMap<String,PhonemeType[]>());

        initialized = true;
    }

    public static void InitPhonemes() {

        // French
        addVIBCereprocPhonemeCorrespondence("fr-FR", "sil", PhonemeType.pause);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "aa", PhonemeType.a);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "a", PhonemeType.a1);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "ai", PhonemeType.a, PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "e", PhonemeType.E1);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "an", PhonemeType.a);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "au", PhonemeType.o);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "b", PhonemeType.b);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "ch", PhonemeType.SS);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "sh", PhonemeType.SS);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "d", PhonemeType.d);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "dh", PhonemeType.th);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "@", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "e@", PhonemeType.E1);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "ee", PhonemeType.e);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "ei", PhonemeType.e,PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "ex", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "f", PhonemeType.f);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "g", PhonemeType.g);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "h", PhonemeType.r);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "i", PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "i@", PhonemeType.i1);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "ii", PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "in", PhonemeType.a);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "zh", PhonemeType.SS);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "jh", PhonemeType.SS);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "k", PhonemeType.k);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "l", PhonemeType.l);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "m", PhonemeType.m);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "n", PhonemeType.n);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "ng", PhonemeType.g);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "ny", PhonemeType.n);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "oi", PhonemeType.o, PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "@@", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "on", PhonemeType.O1);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "oo", PhonemeType.o);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "ou", PhonemeType.u);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "o", PhonemeType.o);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "u", PhonemeType.u);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "p", PhonemeType.p);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "r", PhonemeType.r);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "rr", PhonemeType.r);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "s", PhonemeType.s);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "t", PhonemeType.t);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "th", PhonemeType.f);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "u@", PhonemeType.u);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "uh", PhonemeType.e);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "un", PhonemeType.e, PhonemeType.n);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "uu", PhonemeType.u1);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "yy", PhonemeType.u);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "uy", PhonemeType.u, PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "v", PhonemeType.v);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "w", PhonemeType.w);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "y", PhonemeType.y);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "z", PhonemeType.z);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "wa", PhonemeType.o, PhonemeType.a);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "wi", PhonemeType.o, PhonemeType.E1);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "yn", PhonemeType.i, PhonemeType.o);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "ye", PhonemeType.i, PhonemeType.E1);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "ya", PhonemeType.i, PhonemeType.a);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "yo", PhonemeType.i, PhonemeType.o);
        addDoubleVIBCereprocPhonemeCorrespondence("fr-FR", "yz", PhonemeType.i, PhonemeType.z);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "R", PhonemeType.r);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "Z", PhonemeType.z);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "ZZ", PhonemeType.z);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "TT", PhonemeType.t);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "NN", PhonemeType.n);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "KK", PhonemeType.g);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "PP", PhonemeType.p);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "RR", PhonemeType.r);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "HH", PhonemeType.SS);
        addVIBCereprocPhonemeCorrespondence("fr-FR", "EE", PhonemeType.pause);

        // British English
        addVIBCereprocPhonemeCorrespondence("en-GB", "sil", PhonemeType.pause);
        addVIBCereprocPhonemeCorrespondence("en-GB", "aa", PhonemeType.a);
        addVIBCereprocPhonemeCorrespondence("en-GB", "a", PhonemeType.a1);
        addDoubleVIBCereprocPhonemeCorrespondence("en-GB", "ai", PhonemeType.a, PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("en-GB", "e", PhonemeType.E1);
        addDoubleVIBCereprocPhonemeCorrespondence("en-GB", "au", PhonemeType.a, PhonemeType.u);
        addVIBCereprocPhonemeCorrespondence("en-GB", "b", PhonemeType.b);
        addVIBCereprocPhonemeCorrespondence("en-GB", "ch", PhonemeType.tS);
        addVIBCereprocPhonemeCorrespondence("en-GB", "sh", PhonemeType.SS);
        addVIBCereprocPhonemeCorrespondence("en-GB", "d", PhonemeType.d);
        addVIBCereprocPhonemeCorrespondence("en-GB", "dh", PhonemeType.th);
        addVIBCereprocPhonemeCorrespondence("en-GB", "@", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("en-GB", "e@", PhonemeType.E1);
        addDoubleVIBCereprocPhonemeCorrespondence("en-GB", "ei", PhonemeType.e,PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("en-GB", "f", PhonemeType.f);
        addVIBCereprocPhonemeCorrespondence("en-GB", "g", PhonemeType.g);
        addVIBCereprocPhonemeCorrespondence("en-GB", "h", PhonemeType.r); // TODO: change for english (now using r as in french)
        addVIBCereprocPhonemeCorrespondence("en-GB", "i", PhonemeType.i);
        addDoubleVIBCereprocPhonemeCorrespondence("en-GB", "i@", PhonemeType.i,PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("en-GB", "ii", PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("en-GB", "zh", PhonemeType.SS);
        addVIBCereprocPhonemeCorrespondence("en-GB", "jh", PhonemeType.tS);
        addVIBCereprocPhonemeCorrespondence("en-GB", "k", PhonemeType.k);
        addVIBCereprocPhonemeCorrespondence("en-GB", "l", PhonemeType.l);
        addVIBCereprocPhonemeCorrespondence("en-GB", "m", PhonemeType.m);
        addVIBCereprocPhonemeCorrespondence("en-GB", "n", PhonemeType.n);
        addVIBCereprocPhonemeCorrespondence("en-GB", "ng", PhonemeType.g);
        addDoubleVIBCereprocPhonemeCorrespondence("en-GB", "oi", PhonemeType.o, PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("en-GB", "@@", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("en-GB", "oo", PhonemeType.o);
        addVIBCereprocPhonemeCorrespondence("en-GB", "ou", PhonemeType.u);
        addVIBCereprocPhonemeCorrespondence("en-GB", "o", PhonemeType.o);
        addVIBCereprocPhonemeCorrespondence("en-GB", "u", PhonemeType.u);
        addVIBCereprocPhonemeCorrespondence("en-GB", "p", PhonemeType.p);
        addVIBCereprocPhonemeCorrespondence("en-GB", "r", PhonemeType.r);
        addVIBCereprocPhonemeCorrespondence("en-GB", "s", PhonemeType.s);
        addVIBCereprocPhonemeCorrespondence("en-GB", "t", PhonemeType.t);
        addVIBCereprocPhonemeCorrespondence("en-GB", "th", PhonemeType.f);
        addVIBCereprocPhonemeCorrespondence("en-GB", "u@", PhonemeType.u);
        addVIBCereprocPhonemeCorrespondence("en-GB", "uh", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("en-GB", "uu", PhonemeType.u1);
        addVIBCereprocPhonemeCorrespondence("en-GB", "v", PhonemeType.v);
        addVIBCereprocPhonemeCorrespondence("en-GB", "w", PhonemeType.w);
        addVIBCereprocPhonemeCorrespondence("en-GB", "y", PhonemeType.y);
        addVIBCereprocPhonemeCorrespondence("en-GB", "z", PhonemeType.z);
        addVIBCereprocPhonemeCorrespondence("en-GB", "R", PhonemeType.r);

        // German
        addVIBCereprocPhonemeCorrespondence("de-DE", "sil", PhonemeType.pause);
        addVIBCereprocPhonemeCorrespondence("de-DE", "a", PhonemeType.a1);
        addVIBCereprocPhonemeCorrespondence("de-DE", "ah", PhonemeType.a);
        addVIBCereprocPhonemeCorrespondence("de-DE", "ae", PhonemeType.E1);
        addVIBCereprocPhonemeCorrespondence("de-DE", "aeh", PhonemeType.E1);
        addVIBCereprocPhonemeCorrespondence("de-DE", "e", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("de-DE", "eh", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("de-DE", "i", PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("de-DE", "ih", PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("de-DE", "o", PhonemeType.o);
        addVIBCereprocPhonemeCorrespondence("de-DE", "oh", PhonemeType.o);
        addDoubleVIBCereprocPhonemeCorrespondence("de-DE", "oi", PhonemeType.o, PhonemeType.i);
        addVIBCereprocPhonemeCorrespondence("de-DE", "oe", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("de-DE", "oeh", PhonemeType.e);
        addVIBCereprocPhonemeCorrespondence("de-DE", "u", PhonemeType.u);
        addVIBCereprocPhonemeCorrespondence("de-DE", "uh", PhonemeType.u);
        addVIBCereprocPhonemeCorrespondence("de-DE", "ue", PhonemeType.u1);
        addVIBCereprocPhonemeCorrespondence("de-DE", "ueh", PhonemeType.u1);
        addVIBCereprocPhonemeCorrespondence("de-DE", "p", PhonemeType.p);
        addVIBCereprocPhonemeCorrespondence("de-DE", "b", PhonemeType.b);
        addVIBCereprocPhonemeCorrespondence("de-DE", "t", PhonemeType.t);
        addVIBCereprocPhonemeCorrespondence("de-DE", "d", PhonemeType.d);
        addVIBCereprocPhonemeCorrespondence("de-DE", "g", PhonemeType.g);
        addVIBCereprocPhonemeCorrespondence("de-DE", "k", PhonemeType.k);
        addVIBCereprocPhonemeCorrespondence("de-DE", "f", PhonemeType.f);

        // TODO complete mappings for german language
    }


    /**
     * Returns the sequence of VIB's {@code phonenes} corresponding to the specified Cereproc's phoneme.
     * @param cerePhoneme the Cereproc's phoneme
     * @return a sequence of VIB's {@code phonemes}
     */
    public static PhonemeType[] convertPhoneme(String language, String cerePhoneme){

        if (language.equalsIgnoreCase("en-us")) {
            Logs.warning("CereprocTTS: phoneme conversion not supported yet for language [" +  language + "] using conversion for language [en-GB] instead.");
            language = "en-GB";
        }

        Map<String,PhonemeType[]> correspondingPhonemesLangauge = correspondingPhonemes.get(language);
        if (correspondingPhonemesLangauge == null ) {
            Logs.error("CereprocTTS: phoneme conversion failed, language [" +  language + "] is not supported.");
            return null;
        }
        else {
            PhonemeType[] toReturn = correspondingPhonemesLangauge.get(cerePhoneme);
            if (toReturn == null) {
                PhonemeType pho;
                try{
                    pho = PhonemeType.valueOf(cerePhoneme);
                }catch(IllegalArgumentException iae){
                    Logs.warning("CereprocTTS: " + CereprocConstants.class.getName() + " unknown phoneme : " + cerePhoneme);
                    pho = PhonemeType.e; //default value ?
                }
                toReturn = new PhonemeType[] {pho};
            }
            return toReturn;
        }
    }

    public static TTS getCereprocTTS(boolean useNativeTTS) throws IOException, UnsupportedAudioFileException {
        TTS tts = new CereprocTTS();
        Logs.info("CereprocTTS : new instance of " + tts.getClass().getName());
        return tts;
    }

    public static CPRCEN_INTERRUPT_BOUNDARY_TYPE fromVIBReactionDurationToCEREPROC(ReactionDuration reactionDuration) {
        switch (reactionDuration) {
            case EXTRA_SHORT: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_PHONE;
            case SHORT: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_NATURAL;
            case MEDIUM: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_WORD;
            case LONG: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_LEGACY_SPURT;
            default: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_DEFAULT;
        }
    }

    public static CPRCEN_INTERRUPT_INTERRUPT_TYPE fromVIBReactionTypeToCEREPROC(ReactionType reactionType) {
        switch (reactionType) {
            case HALT: return CPRCEN_INTERRUPT_INTERRUPT_TYPE.CPRCEN_INTERRUPT_INTERRUPT_HALT;
            case OVERLAP: return CPRCEN_INTERRUPT_INTERRUPT_TYPE.CPRCEN_INTERRUPT_INTERRUPT_OVERLAP;
            case REPLAN: return CPRCEN_INTERRUPT_INTERRUPT_TYPE.CPRCEN_INTERRUPT_INTERRUPT_REPLAN;
            default: return CPRCEN_INTERRUPT_INTERRUPT_TYPE.CPRCEN_INTERRUPT_INTERRUPT_HALT;
        }
    }

    public static float fromVIBBufferPositionToCEREPROC(int bufferPositionVIB) {
        if (bufferPositionVIB != 0) {
            return ((bufferPositionVIB / 2) / cereprocSampleRateFloat);
        }
        else {
            return 0;
        }
    }
}
