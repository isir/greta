/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.ircamalign;

import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import greta.core.util.speech.Phoneme;
import greta.core.util.speech.TTS;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Michele
 */
public class IrcamConstants {
    
     private IrcamConstants(){}
     
     public static final Map<String,Map<String,Phoneme.PhonemeType[]>> correspondingPhonemes = new HashMap<String,Map<String,Phoneme.PhonemeType[]>>();   // Map of correspondences for all available languages
    
     private static void addGretaIrcamPhonemeCorrespondence(String language, String cerePhoneme, Phoneme.PhonemeType phoneme){
        Phoneme.PhonemeType[] phonemes = {phoneme};
        correspondingPhonemes.get(language).put(cerePhoneme, phonemes);
    }
    private static void addDoubleGretaIrcamPhonemeCorrespondence(String language, String cerePhoneme, Phoneme.PhonemeType phoneme1, Phoneme.PhonemeType phoneme2){
        Phoneme.PhonemeType[] phonemes = {phoneme1, phoneme2};
        correspondingPhonemes.get(language).put(cerePhoneme, phonemes);
    }
     
    static {init();}

    public static void init(){


        correspondingPhonemes.put("en-GB", new HashMap<String,Phoneme.PhonemeType[]>());
    }

     public static void InitPhonemes() {

        // British English
        addGretaIrcamPhonemeCorrespondence("en-GB", "aa", Phoneme.PhonemeType.a);
        addGretaIrcamPhonemeCorrespondence("en-GB", "nn", Phoneme.PhonemeType.n);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ii", Phoneme.PhonemeType.i);
        addGretaIrcamPhonemeCorrespondence("en-GB", "tt", Phoneme.PhonemeType.t);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ll", Phoneme.PhonemeType.l);
        addGretaIrcamPhonemeCorrespondence("en-GB", "mm", Phoneme.PhonemeType.m);
        addGretaIrcamPhonemeCorrespondence("en-GB", "jj", Phoneme.PhonemeType.i1);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ai", Phoneme.PhonemeType.E1);
        addGretaIrcamPhonemeCorrespondence("en-GB", "rr", Phoneme.PhonemeType.r);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ss", Phoneme.PhonemeType.s);
        addGretaIrcamPhonemeCorrespondence("en-GB", "zz", Phoneme.PhonemeType.z);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ei", Phoneme.PhonemeType.E1);
        addGretaIrcamPhonemeCorrespondence("en-GB", "an", Phoneme.PhonemeType.a);
        addGretaIrcamPhonemeCorrespondence("en-GB", "gg", Phoneme.PhonemeType.g);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ee", Phoneme.PhonemeType.e);
        addGretaIrcamPhonemeCorrespondence("en-GB", "eu", Phoneme.PhonemeType.E1);
        addGretaIrcamPhonemeCorrespondence("en-GB", "oe", Phoneme.PhonemeType.E1); // TODO: change for english (now using r as in french)
        addGretaIrcamPhonemeCorrespondence("en-GB", "oo", Phoneme.PhonemeType.o);
        addGretaIrcamPhonemeCorrespondence("en-GB", "kk", Phoneme.PhonemeType.k);
        addGretaIrcamPhonemeCorrespondence("en-GB", "pp", Phoneme.PhonemeType.p);
        addGretaIrcamPhonemeCorrespondence("en-GB", "yy", Phoneme.PhonemeType.y);
        addGretaIrcamPhonemeCorrespondence("en-GB", "vv", Phoneme.PhonemeType.v);
        addGretaIrcamPhonemeCorrespondence("en-GB", "bb", Phoneme.PhonemeType.b);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ou", Phoneme.PhonemeType.u);
        addGretaIrcamPhonemeCorrespondence("en-GB", "dd", Phoneme.PhonemeType.d);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ch", Phoneme.PhonemeType.SS);
        addGretaIrcamPhonemeCorrespondence("en-GB", "in", Phoneme.PhonemeType.a);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ff", Phoneme.PhonemeType.f);
        addGretaIrcamPhonemeCorrespondence("en-GB", "uu", Phoneme.PhonemeType.u1);
        addGretaIrcamPhonemeCorrespondence("en-GB", "au", Phoneme.PhonemeType.o);
        addGretaIrcamPhonemeCorrespondence("en-GB", "on", Phoneme.PhonemeType.O1);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ww", Phoneme.PhonemeType.w);
        addDoubleGretaIrcamPhonemeCorrespondence("en-GB", "uy", Phoneme.PhonemeType.u,Phoneme.PhonemeType.i);
        addGretaIrcamPhonemeCorrespondence("en-GB", "ng", Phoneme.PhonemeType.g);
        addDoubleGretaIrcamPhonemeCorrespondence("en-GB", "un", Phoneme.PhonemeType.e,Phoneme.PhonemeType.n);
        addGretaIrcamPhonemeCorrespondence("en-GB", "##", Phoneme.PhonemeType.pause);
        addDoubleGretaIrcamPhonemeCorrespondence("en-GB", "sp", Phoneme.PhonemeType.s,Phoneme.PhonemeType.p);
         addDoubleGretaIrcamPhonemeCorrespondence("en-GB", "br", Phoneme.PhonemeType.b,Phoneme.PhonemeType.r);
    
     }
     
     public static TTS getIrcamTTS(CharacterManager cm,boolean useNativeTTS) throws IOException, UnsupportedAudioFileException {
        TTS tts = cm.getTTS();
        Logs.info("IrcamTTS : new instance of " + tts.getClass().getName());
        return tts;
    }
     
     public static Phoneme.PhonemeType[] convertPhoneme(String language, String cerePhoneme){
        
        System.out.println(cerePhoneme+"  "+language);

        if (language.equalsIgnoreCase("en-us")) {
            Logs.warning("IrcamAlignTTS: phoneme conversion not supported yet for language [" +  language + "] using conversion for language [en-GB] instead.");
            language = "en-GB";
        }

        Map<String,Phoneme.PhonemeType[]> correspondingPhonemesLanguage = correspondingPhonemes.get(language);
        if (correspondingPhonemesLanguage == null ) {
            System.out.println("CereProcTTS: phoneme conversion failed, language [" +  language + "] is not supported.");
            Logs.error("CereProcTTS: phoneme conversion failed, language [" +  language + "] is not supported.");
            return null;
        }
        
        
        else {
            Phoneme.PhonemeType[] toReturn = correspondingPhonemesLanguage.get(cerePhoneme);
            System.out.println("[TO-RETURN]:"+toReturn.toString()+"   "+cerePhoneme);
            if (toReturn == null) {
                Phoneme.PhonemeType pho;
                try{
                    pho = Phoneme.PhonemeType.valueOf(cerePhoneme);
                }catch(IllegalArgumentException iae){
                    System.out.println("IrcamALignTTS: " + IrcamConstants.class.getName() + " unknown phoneme : " + cerePhoneme);
                    pho = Phoneme.PhonemeType.e; //default value ?
                }
                toReturn = new Phoneme.PhonemeType[] {pho};
            }
            return toReturn;
        }
    }
}
