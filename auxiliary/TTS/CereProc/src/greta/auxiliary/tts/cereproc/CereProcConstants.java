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
package greta.auxiliary.tts.cereproc;

import com.cereproc.cerevoice_eng.CPRCEN_INTERRUPT_BOUNDARY_TYPE;
import com.cereproc.cerevoice_eng.CPRCEN_INTERRUPT_INTERRUPT_TYPE;
import static greta.auxiliary.tts.cereproc.CereProcTTS.cereprocSampleRateFloat;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.enums.interruptions.ReactionDuration;
import greta.core.util.enums.interruptions.ReactionType;
import greta.core.util.log.Logs;
import greta.core.util.speech.Phoneme.PhonemeType;
import greta.core.util.speech.TTS;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Class containing phonemes mapping and constants for the CereProc TTS Greta implementation
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
    public static final String DEFAULT_VOICE = "Sarah";

    /** Correspondence map of phonemes between Greta and CereProc */
    public static final Map<String,Map<String,PhonemeType[]>> correspondingPhonemes = new HashMap<String,Map<String,PhonemeType[]>>();   // Map of correspondences for all available languages

    /**
     * Function to add a phoneme-CereProc correspondence in the map of correspondence depending on the specified language<br/>
     * @param language the CereProc's language
     * @param cerePhoneme the CereProc's phoneme
     * @param phoneme the Greta's phoneme
     */
    private static void addGretaCereProcPhonemeCorrespondence(String language, String cerePhoneme, PhonemeType phoneme){
        PhonemeType[] phonemes = {phoneme};
        correspondingPhonemes.get(language).put(cerePhoneme, phonemes);
    }
    /**
     * Function to add a phoneme-CereProc correspondence in the map of correspondence depending on the specified language<br/>
     * This function is used when a CereProc's phoneme correspond to a sequence of two Greta's phonemes
     * @param language the CereProc's language
     * @param cerePhoneme the CereProc's phoneme
     * @param phoneme1 first Greta's phoneme
     * @param phoneme2 second Greta's phoneme
     */
    private static void addDoubleGretaCereProcPhonemeCorrespondence(String language, String cerePhoneme, PhonemeType phoneme1, PhonemeType phoneme2){
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
        addGretaCereProcPhonemeCorrespondence("fr-FR", "sil", PhonemeType.pause);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "aa", PhonemeType.a);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "a", PhonemeType.a1);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "ai", PhonemeType.a, PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "e", PhonemeType.E1);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "an", PhonemeType.a);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "au", PhonemeType.o);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "b", PhonemeType.b);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "ch", PhonemeType.SS);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "sh", PhonemeType.SS);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "d", PhonemeType.d);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "dh", PhonemeType.th);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "@", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "e@", PhonemeType.E1);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "ee", PhonemeType.e);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "ei", PhonemeType.e,PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "ex", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "f", PhonemeType.f);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "g", PhonemeType.g);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "h", PhonemeType.r);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "i", PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "i@", PhonemeType.i1);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "ii", PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "in", PhonemeType.a);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "zh", PhonemeType.SS);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "jh", PhonemeType.SS);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "k", PhonemeType.k);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "l", PhonemeType.l);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "m", PhonemeType.m);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "n", PhonemeType.n);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "ng", PhonemeType.g);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "ny", PhonemeType.n);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "oi", PhonemeType.o, PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "@@", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "on", PhonemeType.O1);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "oo", PhonemeType.o);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "ou", PhonemeType.u);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "o", PhonemeType.o);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "u", PhonemeType.u);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "p", PhonemeType.p);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "r", PhonemeType.r);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "rr", PhonemeType.r);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "s", PhonemeType.s);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "t", PhonemeType.t);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "th", PhonemeType.f);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "u@", PhonemeType.u);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "uh", PhonemeType.e);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "un", PhonemeType.e, PhonemeType.n);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "uu", PhonemeType.u1);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "yy", PhonemeType.u);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "uy", PhonemeType.u, PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "v", PhonemeType.v);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "w", PhonemeType.w);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "y", PhonemeType.y);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "z", PhonemeType.z);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "wa", PhonemeType.o, PhonemeType.a);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "wi", PhonemeType.o, PhonemeType.E1);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "yn", PhonemeType.i, PhonemeType.o);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "ye", PhonemeType.i, PhonemeType.E1);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "ya", PhonemeType.i, PhonemeType.a);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "yo", PhonemeType.i, PhonemeType.o);
        addDoubleGretaCereProcPhonemeCorrespondence("fr-FR", "yz", PhonemeType.i, PhonemeType.z);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "R", PhonemeType.r);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "Z", PhonemeType.z);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "ZZ", PhonemeType.z);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "TT", PhonemeType.t);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "NN", PhonemeType.n);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "KK", PhonemeType.g);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "PP", PhonemeType.p);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "RR", PhonemeType.r);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "HH", PhonemeType.SS);
        addGretaCereProcPhonemeCorrespondence("fr-FR", "EE", PhonemeType.pause);

        // British English
        addGretaCereProcPhonemeCorrespondence("en-GB", "sil", PhonemeType.pause);
        addGretaCereProcPhonemeCorrespondence("en-GB", "aa", PhonemeType.a);
        addGretaCereProcPhonemeCorrespondence("en-GB", "a", PhonemeType.a1);
        addDoubleGretaCereProcPhonemeCorrespondence("en-GB", "ai", PhonemeType.a, PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("en-GB", "e", PhonemeType.E1);
        addDoubleGretaCereProcPhonemeCorrespondence("en-GB", "au", PhonemeType.a, PhonemeType.u);
        addGretaCereProcPhonemeCorrespondence("en-GB", "b", PhonemeType.b);
        addGretaCereProcPhonemeCorrespondence("en-GB", "ch", PhonemeType.tS);
        addGretaCereProcPhonemeCorrespondence("en-GB", "sh", PhonemeType.SS);
        addGretaCereProcPhonemeCorrespondence("en-GB", "d", PhonemeType.d);
        addGretaCereProcPhonemeCorrespondence("en-GB", "dh", PhonemeType.th);
        addGretaCereProcPhonemeCorrespondence("en-GB", "@", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("en-GB", "e@", PhonemeType.E1);
        addDoubleGretaCereProcPhonemeCorrespondence("en-GB", "ei", PhonemeType.e,PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("en-GB", "f", PhonemeType.f);
        addGretaCereProcPhonemeCorrespondence("en-GB", "g", PhonemeType.g);
        addGretaCereProcPhonemeCorrespondence("en-GB", "h", PhonemeType.r); // TODO: change for english (now using r as in french)
        addGretaCereProcPhonemeCorrespondence("en-GB", "i", PhonemeType.i);
        addDoubleGretaCereProcPhonemeCorrespondence("en-GB", "i@", PhonemeType.i,PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("en-GB", "ii", PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("en-GB", "zh", PhonemeType.SS);
        addGretaCereProcPhonemeCorrespondence("en-GB", "jh", PhonemeType.tS);
        addGretaCereProcPhonemeCorrespondence("en-GB", "k", PhonemeType.k);
        addGretaCereProcPhonemeCorrespondence("en-GB", "l", PhonemeType.l);
        addGretaCereProcPhonemeCorrespondence("en-GB", "m", PhonemeType.m);
        addGretaCereProcPhonemeCorrespondence("en-GB", "n", PhonemeType.n);
        addGretaCereProcPhonemeCorrespondence("en-GB", "ng", PhonemeType.g);
        addDoubleGretaCereProcPhonemeCorrespondence("en-GB", "oi", PhonemeType.o, PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("en-GB", "@@", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("en-GB", "oo", PhonemeType.o);
        addGretaCereProcPhonemeCorrespondence("en-GB", "ou", PhonemeType.u);
        addGretaCereProcPhonemeCorrespondence("en-GB", "o", PhonemeType.o);
        addGretaCereProcPhonemeCorrespondence("en-GB", "u", PhonemeType.u);
        addGretaCereProcPhonemeCorrespondence("en-GB", "p", PhonemeType.p);
        addGretaCereProcPhonemeCorrespondence("en-GB", "r", PhonemeType.r);
        addGretaCereProcPhonemeCorrespondence("en-GB", "s", PhonemeType.s);
        addGretaCereProcPhonemeCorrespondence("en-GB", "t", PhonemeType.t);
        addGretaCereProcPhonemeCorrespondence("en-GB", "th", PhonemeType.f);
        addGretaCereProcPhonemeCorrespondence("en-GB", "u@", PhonemeType.u);
        addGretaCereProcPhonemeCorrespondence("en-GB", "uh", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("en-GB", "uu", PhonemeType.u1);
        addGretaCereProcPhonemeCorrespondence("en-GB", "v", PhonemeType.v);
        addGretaCereProcPhonemeCorrespondence("en-GB", "w", PhonemeType.w);
        addGretaCereProcPhonemeCorrespondence("en-GB", "y", PhonemeType.y);
        addGretaCereProcPhonemeCorrespondence("en-GB", "z", PhonemeType.z);
        addGretaCereProcPhonemeCorrespondence("en-GB", "R", PhonemeType.r);

        // German
        addGretaCereProcPhonemeCorrespondence("de-DE", "sil", PhonemeType.pause);
        addGretaCereProcPhonemeCorrespondence("de-DE", "a", PhonemeType.a1);
        addGretaCereProcPhonemeCorrespondence("de-DE", "ah", PhonemeType.a);
        addGretaCereProcPhonemeCorrespondence("de-DE", "ae", PhonemeType.E1);
        addGretaCereProcPhonemeCorrespondence("de-DE", "aeh", PhonemeType.E1);
        addGretaCereProcPhonemeCorrespondence("de-DE", "e", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("de-DE", "eh", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("de-DE", "i", PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("de-DE", "ih", PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("de-DE", "o", PhonemeType.o);
        addGretaCereProcPhonemeCorrespondence("de-DE", "oh", PhonemeType.o);
        addDoubleGretaCereProcPhonemeCorrespondence("de-DE", "oi", PhonemeType.o, PhonemeType.i);
        addGretaCereProcPhonemeCorrespondence("de-DE", "oe", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("de-DE", "oeh", PhonemeType.e);
        addGretaCereProcPhonemeCorrespondence("de-DE", "u", PhonemeType.u);
        addGretaCereProcPhonemeCorrespondence("de-DE", "uh", PhonemeType.u);
        addGretaCereProcPhonemeCorrespondence("de-DE", "ue", PhonemeType.u1);
        addGretaCereProcPhonemeCorrespondence("de-DE", "ueh", PhonemeType.u1);
        addGretaCereProcPhonemeCorrespondence("de-DE", "p", PhonemeType.p);
        addGretaCereProcPhonemeCorrespondence("de-DE", "b", PhonemeType.b);
        addGretaCereProcPhonemeCorrespondence("de-DE", "t", PhonemeType.t);
        addGretaCereProcPhonemeCorrespondence("de-DE", "d", PhonemeType.d);
        addGretaCereProcPhonemeCorrespondence("de-DE", "g", PhonemeType.g);
        addGretaCereProcPhonemeCorrespondence("de-DE", "k", PhonemeType.k);
        addGretaCereProcPhonemeCorrespondence("de-DE", "f", PhonemeType.f);

        // TODO complete mappings for german language
    }


    /**
     * Returns the sequence of Greta's {@code phonenes} corresponding to the specified CereProc's phoneme.
     * @param cerePhoneme the CereProc's phoneme
     * @return a sequence of Greta's {@code phonemes}
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

    public static CPRCEN_INTERRUPT_BOUNDARY_TYPE fromGRETAReactionDurationToCEREPROC(ReactionDuration reactionDuration) {
        switch (reactionDuration) {
            case EXTRA_SHORT: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_PHONE;
            case SHORT: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_NATURAL;
            case MEDIUM: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_WORD;
            case LONG: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_LEGACY_SPURT;
            default: return CPRCEN_INTERRUPT_BOUNDARY_TYPE.CPRCEN_INTERRUPT_BOUNDARY_DEFAULT;
        }
    }

    public static CPRCEN_INTERRUPT_INTERRUPT_TYPE fromGRETAReactionTypeToCEREPROC(ReactionType reactionType) {
        switch (reactionType) {
            case HALT: return CPRCEN_INTERRUPT_INTERRUPT_TYPE.CPRCEN_INTERRUPT_INTERRUPT_HALT;
            case OVERLAP: return CPRCEN_INTERRUPT_INTERRUPT_TYPE.CPRCEN_INTERRUPT_INTERRUPT_OVERLAP;
            case REPLAN: return CPRCEN_INTERRUPT_INTERRUPT_TYPE.CPRCEN_INTERRUPT_INTERRUPT_REPLAN;
            default: return CPRCEN_INTERRUPT_INTERRUPT_TYPE.CPRCEN_INTERRUPT_INTERRUPT_HALT;
        }
    }

    public static float fromGRETABufferPositionToCEREPROC(int bufferPositionGreta) {
        if (bufferPositionGreta != 0) {
            return ((bufferPositionGreta / 2) / cereprocSampleRateFloat);
        }
        else {
            return 0;
        }
    }
}
