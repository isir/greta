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
package greta.auxiliary.tts.voxygen;

import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import greta.core.util.speech.Phoneme.PhonemeType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class containing phonemes mapping and constants for the Voxygen TTS VIB implementation
 * @author Andre-Marie Pez
 * @author Mathieu Chollet
 * @author Angelo Cafaro
 * @author Grégoire Montcheuil
 */
public class VoxygenConstants {

    /**
     * can not be instanciated
     */
    private VoxygenConstants(){}

    /**
     * Relative path to JNI JAR (and DLL/libraries),
     *  default /Common/Lib/External/voxygen/.
     * Conatains:
     *  - voxygen.jar : the JNI JAR for the DLL
     *  - Win64,Win32,.. : the libraries files per arch
     *    - libbaratinoo.dll : the Voxygen library
     *    - baratinSwig.dll : the SWIG interface
     */
    public static String DEPENDENCIES_PATH;
    public static final String DEFAULT_DEPENDENCIES_PATH = "/Common/Lib/External/voxygen/";
    
    /**
     * Relative path to voice data,
     *  default /Common/Data/Voxygen/ .
     * Contains :
     *  - baratinoo.cfg : baratinoo config file
     *  - V8.1_1748.lic : the licence file
     *  - data/ : the voices directory
     */
    public static String VOICES_PATH;
    public static final String DEFAULT_VOICES_PATH = "/Common/Data/Voxygen/";


    public static final String DEFAULT_LANGUAGE = "fr-FR";
    public static final String DEFAULT_VOICE = "Fabienne";
    public static final List<String> DEFAULT_VOICE_VARIANTS = new ArrayList<String>();
    static {
        DEFAULT_VOICE_VARIANTS.add(DEFAULT_VOICE);
        DEFAULT_VOICE_VARIANTS.add(DEFAULT_VOICE+"_fatigue");
        DEFAULT_VOICE_VARIANTS.add(DEFAULT_VOICE+"_tendu");
    }
    
    public static final Map<String,String> CEREPROC_EMOTION_MAP = new HashMap<String,String>();
    static {
        CEREPROC_EMOTION_MAP.put("", "");
        CEREPROC_EMOTION_MAP.put("happy", "");
        CEREPROC_EMOTION_MAP.put("cross", "tendu");
        CEREPROC_EMOTION_MAP.put("sad", "tendu");
        CEREPROC_EMOTION_MAP.put("calm", "fatigue");
    }
    
    public static final String CEREPROC_SPURT_DIR="Common/Data/Voxygen/audio/spurt/";

    /** Correspondence mapping lang &rarr; Voxygen viseme &rarr; VIB phonemes */
    public static final Map<String,Map<String,PhonemeType[]>> correspondingPhonemes = new HashMap<String,Map<String,PhonemeType[]>>();   // Map of correspondences for all available languages

    /**
     * Function to add a phoneme-Cereproc correspondence in the map of correspondence depending on the specified language<br/>
     * @param language the Cereproc's language
     * @param voxygenPhoneme the Cereproc's phoneme
     * @param phoneme the VIB's phoneme
     */
    private static void addVoxygenVIBPhonemeCorrespondence(String language, String voxygenPhoneme, PhonemeType phoneme){
        PhonemeType[] phonemes = {phoneme};
        Map<String,PhonemeType[]> langMap = correspondingPhonemes.get(language);
        if (langMap==null) {
            langMap = new HashMap<String,PhonemeType[]>();
            correspondingPhonemes.put(language, langMap);
        }
        langMap.put(voxygenPhoneme, phonemes);
    }
    /**
     * Function to add a phoneme-Cereproc correspondence in the map of correspondence depending on the specified language<br/>
     * This function is used when a Cereproc's phoneme correspond to a sequence of two VIB's phonemes
     * @param language the Cereproc's language
     * @param voxygenPhoneme the Cereproc's phoneme
     * @param phoneme1 first VIB's phoneme
     * @param phoneme2 second VIB's phoneme
     */
    private static void addVoxygenVIBDoublePhonemesCorrespondence(String language, String voxygenPhoneme, PhonemeType phoneme1, PhonemeType phoneme2){
        PhonemeType[] phonemes = {phoneme1, phoneme2};
        Map<String,PhonemeType[]> langMap = correspondingPhonemes.get(language);
        if (langMap==null) {
            langMap = new HashMap<String,PhonemeType[]>();
            correspondingPhonemes.put(language, langMap);
        }
        langMap.put(voxygenPhoneme, phonemes);
    }
    private static boolean initialized = false;

    static {init();}

    public static void init(){

        if(initialized) {
            return;
        }

        DEPENDENCIES_PATH = IniManager.getGlobals().getValueString("VOXYGEN_DEPENDENCIES_PATH"); // "/Common/Lib/External/voxygen/";
        if (DEPENDENCIES_PATH.isEmpty()) DEPENDENCIES_PATH = DEFAULT_DEPENDENCIES_PATH; // just in case
        VOICES_PATH = IniManager.getGlobals().getValueString("VOXYGEN_VOICES_PATH"); //  "/Common/Data/Voxygen/";
        if (VOICES_PATH.isEmpty()) VOICES_PATH = DEFAULT_VOICES_PATH; // just in case
        
        //TODO  correspondingPhonemes.put("en-GB", new HashMap<String,PhonemeType[]>());
        correspondingPhonemes.put("fr-FR", new HashMap<String,PhonemeType[]>());
        //TODO  correspondingPhonemes.put("de-DE", new HashMap<String,PhonemeType[]>());

        initialized = true;
    }

    public static void InitPhonemes() {

        // French
	//        index 	nom 	FR 	EN
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "#", PhonemeType.pause); //	0 	# 	# ^ 
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "A", PhonemeType.a1);//	1 	A 	A AN E 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "aa", PhonemeType.a);//	2 	aa 		aa (dart → d aa t)
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "O", PhonemeType.o);//	3 	O 	O AU EU OE ON UN (mode → M O D) 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "AI", PhonemeType.E1);//	4 	AI 	AI EI IN (procès → P R AU S AI) 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "@", PhonemeType.e);//	5 	@ 		@ (about → @ b au t)
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "Y", PhonemeType.y);//	6 	Y 	Y I 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "W", PhonemeType.u);//	7 	W 	W U OU UI 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "ou", PhonemeType.o);//	8 	ou 		ou (go → g ou)
	addVoxygenVIBDoublePhonemesCorrespondence("fr-FR", "au", PhonemeType.a,  PhonemeType.u);//	9 	au 		au (how → h au)
	addVoxygenVIBDoublePhonemesCorrespondence("fr-FR", "oi", PhonemeType.o, PhonemeType.y);//	10 	oi 		oi (boy → b oi)
	addVoxygenVIBDoublePhonemesCorrespondence("fr-FR", "ai", PhonemeType.a, PhonemeType.y);//	11 	ai 		ai (buy → b ai)
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "%", PhonemeType.r);//	12 	% 	% (???) 	H % (hat → h a t)
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "r", PhonemeType.r);//	13 	r 		r
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "L", PhonemeType.l);//	14 	L 	L 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "Z", PhonemeType.s);//	15 	Z 	Z S 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "J", PhonemeType.SS);//	16 	J 	J CH 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "dh", PhonemeType.SS);//	17 	dh 		dh th (then → dh e n; thin → th i n)
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "V", PhonemeType.v);//	18 	V 	V F 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "T", PhonemeType.t);//	19 	T 	T D N 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "K", PhonemeType.k);//	20 	K 	K G R 	
	addVoxygenVIBPhonemeCorrespondence("fr-FR", "P", PhonemeType.p);//	21 	P 	P B M 
    }


    /**
     * Returns the sequence of VIB's {@code phonenes} corresponding to the specified Cereproc's phoneme.
     * @param language  the language (p.e. "fr-FR")
     * @param voxygenPhoneme the Voxygen's phoneme
     * @return a sequence of VIB's {@code phonemes}
     */
    public static PhonemeType[] convertPhoneme(String language, String voxygenPhoneme){

        if (language.equalsIgnoreCase("en-us")) {
            Logs.warning("VoxygenTTS: phoneme conversion not supported yet for language [" +  language + "] using conversion for language [en-GB] instead.");
            language = "en-GB";
        }

        Map<String,PhonemeType[]> correspondingPhonemesLangauge = correspondingPhonemes.get(language);
        if (correspondingPhonemesLangauge == null ) {
            Logs.error("VoxygenTTS: phoneme conversion failed, language [" +  language + "] is not supported.");
            return null;
        }
        else {
            PhonemeType[] toReturn = correspondingPhonemesLangauge.get(voxygenPhoneme);
            if (toReturn == null) {
                PhonemeType pho;
                try{
                    pho = PhonemeType.valueOf(voxygenPhoneme);
                }catch(IllegalArgumentException iae){
                    Logs.warning("VoxygenTTS: " + VoxygenConstants.class.getName() + " unknown phoneme : " + voxygenPhoneme);
                    pho = PhonemeType.e; //default value ?
                }
                toReturn = new PhonemeType[] {pho};
            }
            return toReturn;
        }
    }

}
