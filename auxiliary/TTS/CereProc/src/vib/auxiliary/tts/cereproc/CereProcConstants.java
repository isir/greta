/*
 * This file is part of the auxiliaries of Greta.
 * 
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
import static vib.auxiliary.tts.cereproc.CereProcTTS.cereprocSampleRateFloat;
import vib.core.util.CharacterManager;
import vib.core.util.enums.interruptions.ReactionDuration;
import vib.core.util.enums.interruptions.ReactionType;

/**
 * Class containing phonemes mapping and constants for the CereProc TTS VIB implementation
 * @author Andre-Marie Pez
 * @author Mathieu Chollet
 * @author Angelo Cafaro
 */
public class CereProcConstants {

    /**
     * can not be instanciated
     */
    private CereProcConstants(){}

    public static String DEPENDENCIES_PATH;
    public static String VOICES_PATH;

    public static final String DEFAULT_LANGUAGE = "en-GB";
    public static final String DEFAULT_VOICE = "sarah";

    /** Correspondence map of phonemes between VIB and CereProc */
    public static final Map<String,Map<String,PhonemeType[]>> correspondingPhonemes = new HashMap<String,Map<String,PhonemeType[]>>();   // Map of correspondences for all available languages

    /**
     * Function to add a phoneme-CereProc correspondence in the map of correspondence depending on the specified language<br/>
     * @param language the CereProc's language
     * @param cerePhoneme the CereProc's phoneme
     * @param phoneme the VIB's phoneme
     */
    private static void addVIBCereProcPhonemeCorrespondence(String language, String cerePhoneme, PhonemeType phoneme){
        PhonemeType[] phonemes = {phoneme};
        correspondingPhonemes.get(language).put(cerePhoneme, phonemes);
    }
    /**
     * Function to add a phoneme-CereProc correspondence in the map of correspondence depending on the specified language<br/>
     * This function is used when a CereProc's phoneme correspond to a sequence of two VIB's phonemes
     * @param language the CereProc's language
     * @param cerePhoneme the CereProc's phoneme
     * @param phoneme1 first VIB's phoneme
     * @param phoneme2 second VIB's phoneme
     */
    private static void addDoubleVIBCereProcPhonemeCorrespondence(String language, String cerePhoneme, PhonemeType phoneme1, PhonemeType phoneme2){
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
        addVIBCereProcPhonemeCorrespondence("fr-FR", "sil", PhonemeType.pause);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "aa", PhonemeType.a);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "a", PhonemeType.a1);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "ai", PhonemeType.a, PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "e", PhonemeType.E1);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "an", PhonemeType.a);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "au", PhonemeType.o);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "b", PhonemeType.b);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "ch", PhonemeType.SS);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "sh", PhonemeType.SS);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "d", PhonemeType.d);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "dh", PhonemeType.th);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "@", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "e@", PhonemeType.E1);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "ee", PhonemeType.e);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "ei", PhonemeType.e,PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "ex", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "f", PhonemeType.f);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "g", PhonemeType.g);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "h", PhonemeType.r);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "i", PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "i@", PhonemeType.i1);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "ii", PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "in", PhonemeType.a);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "zh", PhonemeType.SS);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "jh", PhonemeType.SS);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "k", PhonemeType.k);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "l", PhonemeType.l);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "m", PhonemeType.m);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "n", PhonemeType.n);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "ng", PhonemeType.g);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "ny", PhonemeType.n);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "oi", PhonemeType.o, PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "@@", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "on", PhonemeType.O1);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "oo", PhonemeType.o);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "ou", PhonemeType.u);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "o", PhonemeType.o);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "u", PhonemeType.u);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "p", PhonemeType.p);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "r", PhonemeType.r);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "rr", PhonemeType.r);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "s", PhonemeType.s);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "t", PhonemeType.t);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "th", PhonemeType.f);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "u@", PhonemeType.u);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "uh", PhonemeType.e);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "un", PhonemeType.e, PhonemeType.n);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "uu", PhonemeType.u1);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "yy", PhonemeType.u);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "uy", PhonemeType.u, PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "v", PhonemeType.v);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "w", PhonemeType.w);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "y", PhonemeType.y);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "z", PhonemeType.z);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "wa", PhonemeType.o, PhonemeType.a);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "wi", PhonemeType.o, PhonemeType.E1);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "yn", PhonemeType.i, PhonemeType.o);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "ye", PhonemeType.i, PhonemeType.E1);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "ya", PhonemeType.i, PhonemeType.a);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "yo", PhonemeType.i, PhonemeType.o);
        addDoubleVIBCereProcPhonemeCorrespondence("fr-FR", "yz", PhonemeType.i, PhonemeType.z);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "R", PhonemeType.r);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "Z", PhonemeType.z);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "ZZ", PhonemeType.z);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "TT", PhonemeType.t);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "NN", PhonemeType.n);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "KK", PhonemeType.g);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "PP", PhonemeType.p);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "RR", PhonemeType.r);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "HH", PhonemeType.SS);
        addVIBCereProcPhonemeCorrespondence("fr-FR", "EE", PhonemeType.pause);

        // British English
        addVIBCereProcPhonemeCorrespondence("en-GB", "sil", PhonemeType.pause);
        addVIBCereProcPhonemeCorrespondence("en-GB", "aa", PhonemeType.a);
        addVIBCereProcPhonemeCorrespondence("en-GB", "a", PhonemeType.a1);
        addDoubleVIBCereProcPhonemeCorrespondence("en-GB", "ai", PhonemeType.a, PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("en-GB", "e", PhonemeType.E1);
        addDoubleVIBCereProcPhonemeCorrespondence("en-GB", "au", PhonemeType.a, PhonemeType.u);
        addVIBCereProcPhonemeCorrespondence("en-GB", "b", PhonemeType.b);
        addVIBCereProcPhonemeCorrespondence("en-GB", "ch", PhonemeType.tS);
        addVIBCereProcPhonemeCorrespondence("en-GB", "sh", PhonemeType.SS);
        addVIBCereProcPhonemeCorrespondence("en-GB", "d", PhonemeType.d);
        addVIBCereProcPhonemeCorrespondence("en-GB", "dh", PhonemeType.th);
        addVIBCereProcPhonemeCorrespondence("en-GB", "@", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("en-GB", "e@", PhonemeType.E1);
        addDoubleVIBCereProcPhonemeCorrespondence("en-GB", "ei", PhonemeType.e,PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("en-GB", "f", PhonemeType.f);
        addVIBCereProcPhonemeCorrespondence("en-GB", "g", PhonemeType.g);
        addVIBCereProcPhonemeCorrespondence("en-GB", "h", PhonemeType.r); // TODO: change for english (now using r as in french)
        addVIBCereProcPhonemeCorrespondence("en-GB", "i", PhonemeType.i);
        addDoubleVIBCereProcPhonemeCorrespondence("en-GB", "i@", PhonemeType.i,PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("en-GB", "ii", PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("en-GB", "zh", PhonemeType.SS);
        addVIBCereProcPhonemeCorrespondence("en-GB", "jh", PhonemeType.tS);
        addVIBCereProcPhonemeCorrespondence("en-GB", "k", PhonemeType.k);
        addVIBCereProcPhonemeCorrespondence("en-GB", "l", PhonemeType.l);
        addVIBCereProcPhonemeCorrespondence("en-GB", "m", PhonemeType.m);
        addVIBCereProcPhonemeCorrespondence("en-GB", "n", PhonemeType.n);
        addVIBCereProcPhonemeCorrespondence("en-GB", "ng", PhonemeType.g);
        addDoubleVIBCereProcPhonemeCorrespondence("en-GB", "oi", PhonemeType.o, PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("en-GB", "@@", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("en-GB", "oo", PhonemeType.o);
        addVIBCereProcPhonemeCorrespondence("en-GB", "ou", PhonemeType.u);
        addVIBCereProcPhonemeCorrespondence("en-GB", "o", PhonemeType.o);
        addVIBCereProcPhonemeCorrespondence("en-GB", "u", PhonemeType.u);
        addVIBCereProcPhonemeCorrespondence("en-GB", "p", PhonemeType.p);
        addVIBCereProcPhonemeCorrespondence("en-GB", "r", PhonemeType.r);
        addVIBCereProcPhonemeCorrespondence("en-GB", "s", PhonemeType.s);
        addVIBCereProcPhonemeCorrespondence("en-GB", "t", PhonemeType.t);
        addVIBCereProcPhonemeCorrespondence("en-GB", "th", PhonemeType.f);
        addVIBCereProcPhonemeCorrespondence("en-GB", "u@", PhonemeType.u);
        addVIBCereProcPhonemeCorrespondence("en-GB", "uh", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("en-GB", "uu", PhonemeType.u1);
        addVIBCereProcPhonemeCorrespondence("en-GB", "v", PhonemeType.v);
        addVIBCereProcPhonemeCorrespondence("en-GB", "w", PhonemeType.w);
        addVIBCereProcPhonemeCorrespondence("en-GB", "y", PhonemeType.y);
        addVIBCereProcPhonemeCorrespondence("en-GB", "z", PhonemeType.z);
        addVIBCereProcPhonemeCorrespondence("en-GB", "R", PhonemeType.r);

        // German
        addVIBCereProcPhonemeCorrespondence("de-DE", "sil", PhonemeType.pause);
        addVIBCereProcPhonemeCorrespondence("de-DE", "a", PhonemeType.a1);
        addVIBCereProcPhonemeCorrespondence("de-DE", "ah", PhonemeType.a);
        addVIBCereProcPhonemeCorrespondence("de-DE", "ae", PhonemeType.E1);
        addVIBCereProcPhonemeCorrespondence("de-DE", "aeh", PhonemeType.E1);
        addVIBCereProcPhonemeCorrespondence("de-DE", "e", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("de-DE", "eh", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("de-DE", "i", PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("de-DE", "ih", PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("de-DE", "o", PhonemeType.o);
        addVIBCereProcPhonemeCorrespondence("de-DE", "oh", PhonemeType.o);
        addDoubleVIBCereProcPhonemeCorrespondence("de-DE", "oi", PhonemeType.o, PhonemeType.i);
        addVIBCereProcPhonemeCorrespondence("de-DE", "oe", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("de-DE", "oeh", PhonemeType.e);
        addVIBCereProcPhonemeCorrespondence("de-DE", "u", PhonemeType.u);
        addVIBCereProcPhonemeCorrespondence("de-DE", "uh", PhonemeType.u);
        addVIBCereProcPhonemeCorrespondence("de-DE", "ue", PhonemeType.u1);
        addVIBCereProcPhonemeCorrespondence("de-DE", "ueh", PhonemeType.u1);
        addVIBCereProcPhonemeCorrespondence("de-DE", "p", PhonemeType.p);
        addVIBCereProcPhonemeCorrespondence("de-DE", "b", PhonemeType.b);
        addVIBCereProcPhonemeCorrespondence("de-DE", "t", PhonemeType.t);
        addVIBCereProcPhonemeCorrespondence("de-DE", "d", PhonemeType.d);
        addVIBCereProcPhonemeCorrespondence("de-DE", "g", PhonemeType.g);
        addVIBCereProcPhonemeCorrespondence("de-DE", "k", PhonemeType.k);
        addVIBCereProcPhonemeCorrespondence("de-DE", "f", PhonemeType.f);

        // TODO complete mappings for german language
    }


    /**
     * Returns the sequence of VIB's {@code phonenes} corresponding to the specified CereProc's phoneme.
     * @param cerePhoneme the CereProc's phoneme
     * @return a sequence of VIB's {@code phonemes}
     */
    public static PhonemeType[] convertPhoneme(String language, String cerePhoneme){

        if (language.equalsIgnoreCase("en-us")) {
            Logs.warning("CereProcTTS: phoneme conversion not supported yet for language [" +  language + "] using conversion for language [en-GB] instead.");
            language = "en-GB";
        }

        Map<String,PhonemeType[]> correspondingPhonemesLangauge = correspondingPhonemes.get(language);
        if (correspondingPhonemesLangauge == null ) {
            Logs.error("CereProcTTS: phoneme conversion failed, language [" +  language + "] is not supported.");
            return null;
        }
        else {
            PhonemeType[] toReturn = correspondingPhonemesLangauge.get(cerePhoneme);
            if (toReturn == null) {
                PhonemeType pho;
                try{
                    pho = PhonemeType.valueOf(cerePhoneme);
                }catch(IllegalArgumentException iae){
                    Logs.warning("CereProcTTS: " + CereProcConstants.class.getName() + " unknown phoneme : " + cerePhoneme);
                    pho = PhonemeType.e; //default value ?
                }
                toReturn = new PhonemeType[] {pho};
            }
            return toReturn;
        }
    }

    public static TTS getCereProcTTS(CharacterManager cm,boolean useNativeTTS) throws IOException, UnsupportedAudioFileException {
        TTS tts = cm.getTTS();//new CereProcTTS(cm);
        Logs.info("CereProcTTS : new instance of " + tts.getClass().getName());
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
