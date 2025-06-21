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
package greta.auxiliary.tts.azuretts;

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
public class AzureTTSConstants {

    /**
     * can not be instanciated
     */
    private AzureTTSConstants(){}

    public static String DEPENDENCIES_PATH;
    public static String VOICES_PATH;

    public static final String DEFAULT_LANGUAGE = "en-GB";
    public static final String DEFAULT_VOICE = "Sarah";

    /** Correspondence map of phonemes between Greta and CereProc */
    public static final Map<String,Map<String,PhonemeType[]>> correspondingPhonemes = new HashMap<String,Map<String,PhonemeType[]>>();   // Map of correspondences for all available languages
    
    // Correspondance map between MS Azure TTS VisemeID and IPA phoneme letter
    public static final Map<String,String> visemeId2IPAPhoneme = new HashMap<String,String>();
    
    private static final void initVisemeId2IPAPhoneme(){

        visemeId2IPAPhoneme.put("0", "sil");
        visemeId2IPAPhoneme.put("1", "æ"); // æ, ə, ʌ
        visemeId2IPAPhoneme.put("2", "ɑ"); // ɑ
        visemeId2IPAPhoneme.put("3", "ɔ"); // ɔ
        visemeId2IPAPhoneme.put("4", "ɛ"); // ɛ, ʊ
        visemeId2IPAPhoneme.put("5", "ɝ"); // ɝ
        visemeId2IPAPhoneme.put("6", "j"); // j, i, ɪ
        visemeId2IPAPhoneme.put("7", "w"); // w, u
        visemeId2IPAPhoneme.put("8", "o"); // o
        visemeId2IPAPhoneme.put("9", "aʊ"); // aʊ
        visemeId2IPAPhoneme.put("10", "ɔɪ"); // ɔɪ
        visemeId2IPAPhoneme.put("11", "aɪ"); // aɪ
        visemeId2IPAPhoneme.put("12", "h"); // h
        visemeId2IPAPhoneme.put("13", "ɹ"); // ɹ
        visemeId2IPAPhoneme.put("14", "l"); // l
        visemeId2IPAPhoneme.put("15", "s"); // s, z
        visemeId2IPAPhoneme.put("16", "ʃ"); // ʃ, tʃ, dʒ, ʒ
        visemeId2IPAPhoneme.put("17", "ð"); // ð
        visemeId2IPAPhoneme.put("18", "f"); // f, v
        visemeId2IPAPhoneme.put("19", "d"); // d, t, n, θ
        visemeId2IPAPhoneme.put("20", "k"); // k, g, ŋ
        visemeId2IPAPhoneme.put("21", "p"); // p, b, m
        


    }

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
        
        initVisemeId2IPAPhoneme();

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
        
        // System.out.println(cerePhoneme);

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
                    Logs.warning("CereProcTTS: " + AzureTTSConstants.class.getName() + " unknown phoneme : " + cerePhoneme);
                    pho = PhonemeType.e; //default value ?
                }
                toReturn = new PhonemeType[] {pho};
            }
            return toReturn;
        }
    }

}
