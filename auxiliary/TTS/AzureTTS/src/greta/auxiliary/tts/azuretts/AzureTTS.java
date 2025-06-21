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

/*
################################################
Main flow of TTS process

1. schedule function is called in Speech object
2. tts.setSpeech(Speech object) is called
3. tts.compute() is called to generate actual audio data

What we need for further process

Audio audio
List<Phoneme> phonemes

################################################
*/

package greta.auxiliary.tts.azuretts;

import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.IniManager;
import greta.core.util.audio.Audio;
import greta.core.util.enums.interruptions.ReactionType;
import greta.core.util.log.Logs;
import greta.core.util.speech.Phoneme;
import greta.core.util.speech.Phoneme.PhonemeType;
import greta.core.util.speech.Speech;
import greta.core.util.speech.TTS;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XMLTree;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioFormat;

// import greta.furhat.activemq.GretaFurhatSpeechTextSender;  // Package not available
// import greta.furhat.activemq.GretaFurhatAudioSender;     // Package not available
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.jms.JMSException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

// import greta.core.behaviorrealizer.ClientPhoneme;  // Package not available
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.sound.sampled.AudioFormat;

/**
 * This class manages the CereProc implementation of TTS for Greta<br/>
 * CereProc has a native library, this class uses a Java wrapper provided by CereProc (com.cereproc.cerevoice_eng) to access the native interface. <br/>
 *
 * @author Andre-Marie Pez
 * @author Mathieu Chollet
 * @author Angelo Cafaro
 */
public class AzureTTS extends CharacterDependentAdapter implements TTS {

    private boolean initialized = false;
    private boolean functional = false;
    private String LOAD_ABSOLUTE_PATH = null;
    private String VOICES_ABSOLUTE_PATH = null;

    private boolean interreuptionReactionSupported;
    private Audio audio; //audio buffer
    private byte[] rawAudioBuffer;
    private static AudioFormat audioFormatCereProc;
    private List<Phoneme> phonemes;//phoneme list computed by the native cereproc library
    private Speech speech; //speech object, input to the TTS engine
    private List<TimeMarker> tmOfSpeechList; //time markers list outputed by the native cereproc library

    int tmnumber; //time marker number

    private String languageID; // Character's language specified using <LANGUAGE_CODE_ISO>-<COUNTRY_CODE_ISO> in character's .ini file (e.g. en-GB)
    private String voiceName; // Character's voice specified in character's .ini file (e.g. en-GB)
    
    // private GretaFurhatSpeechTextSender speechtextserver;  // Class not available
    // private GretaFurhatAudioSender audioserver;           // Class not available
    
    private String ssml_path = "Common\\Data\\AzureTTS\\ssml.txt";
    private String audio_path = "Common\\Data\\AzureTTS\\output.wav";
    private String AzureTTS_bat_path = "Common\\Data\\AzureTTS\\run_azuretts.bat";
    private Process main_process;
    
    // private ClientPhoneme clientPhoneme = new ClientPhoneme();  // Class not available
    
    private int numGeneration = 0;
    
    private boolean isRunning = false;
    
    private final double generationLatency = 1.0;
    
    private double s_time = 0;
    private double e_time = 0;
    private double excution_time = 0;

    
    static{
        // Init constants and make phonemes mappings
        AzureTTSConstants.init();
    }

    private static boolean checkFolder(String path) {
        return (path != null) && (!path.isEmpty()) && (Files.isDirectory(Paths.get(path)));
    }

    /*
     * Initialization function, CereProcConstants.init() reads parameters from {Greta}/bin/Greta.ini file
     */
    public void init() {
        if (initialized && functional) {
            return;
        }
        
        functional = true;

        initialized = true;
    }

    /**
     * Constructor.
     * @param characterManager reference to use
     */
    public AzureTTS(CharacterManager characterManager) {
        setCharacterManager(characterManager);
        characterManager.setTTS(this);
        init();
        interreuptionReactionSupported = true;
        setupCharacterLanguageVoiceParameters();
        clean();
        tmnumber = 0;
    /*====================================================================================================*/
    /*                     This block is part of the Greta Furhat Interface       
                               Author: Fousseyni Sangaré 04/2024-09/2024                                  */
    /*====================================================================================================*/
        // speechtextserver = new GretaFurhatSpeechTextSender("localhost", "61616", "greta.furhat.SpeechText");  // Class not available
        // audioserver = new GretaFurhatAudioSender("localhost", "61616", "greta.furhat.Audio");               // Class not available
        
    /*====================================================================================================*/
    /*====================================================================================================*/
    }

    /**
     * Set new speech object to be computed. </br>
     * Also sets a new temp file path to avoid the speech duplication bug.
     * @param speech Speech object to compute
     */
    @Override
    public void setSpeech(Speech speech) {
//        if (isRunning) {
//            
//            Thread th = new Thread(){
//                @Override
//                public void run(){
//                    while (!isRunning) {
//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException ex) {
//                            Logger.getLogger(AzureTTS.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    }
//                }
//            };
//            th.start();
//            
//        }
        clean();
        this.speech = speech;
    }

    // Relative changes in pitch and rate (used in Mathieu'sampleAudio experiment)
    private double rate =0.0;
    private double pitch =0.0;
    public void setRateAndPitch(double rate, double pitch)
    {
        this.rate = rate;
        this.pitch = pitch;
    }

    @Override
    public boolean isInterruptionReactionSupported() {
        return interreuptionReactionSupported;
    }

    /**
     * Computes the latest speech object. </br>
     * The three boolean should all be true: CereProc only does all at once.
     * @param doTemporize Temporize or not
     * @param doAudio Compute audio or not
     * @param doPhonems Compute phonems or not
     */
    @Override
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonems) {
        
    }
    
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonems, String selectedVoiceLocal) {
        
        System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute(): start");
        
        isRunning = true;

        if (!functional) {
            Logs.error("AzureTTS: the module is not fully functional due to initialization errors. The speech that has been sent to be computed is ignored.");
            return;
        }

        if (speech == null) {
            Logs.error("AzureTTS: the speech variable is null.");
            return ;
        }

        if (!doPhonems || !doTemporize || !doAudio) {
            Logs.warning("AzureTTS: the engine only computes all at once.");
        }

        tmOfSpeechList = speech.getTimeMarkers();
        Map tmOfSpeechMap = new HashMap<String, TimeMarker>();
        for (TimeMarker tm : tmOfSpeechList){
            tmOfSpeechMap.put(tm.getName(),tm);
        }

        TimeMarker start = tmOfSpeechList.get(0);
        TimeMarker end = speech.getEnd();
        if (!start.isConcretized()) {
            start.setValue(0);
        }
        double startTime = start.getValue();
        end.setValue(startTime);
        if (doPhonems || doTemporize) {
            try {

                XMLTree ssml = speech.toSSML(rate, pitch, true, "#!", "!#");

                // DEBUG
                if (Logs.hasLevelDebug()) {
                    Logs.debug("----------------------------- AzureTTS DEBUG -----------------------------");
                    Logs.debug("SSML:");
                    Logs.debug(ssml.toString(true));
                    Logs.debug("----------------------------- AzureTTS DEBUG -----------------------------");
                }

                String ssmlSpeech = ssml.toString(false);
                ssmlSpeech = replaceSpecialCharactersIntoBrackets(ssmlSpeech, "#!", "!#");
                String ssmlSpeechAzureTTS = ssmlSpeech.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
                
                System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute: ssml:\n" + ssmlSpeechAzureTTS);

                int currentPlayingBufferPos = 0;
                Speech currentPlayingSpeech;
                boolean reactionToInterruptionCall = false;
                    
                /*
                ###########################################################
                ### Main excution of AzureTTS call
                ###########################################################
                */
                
                ArrayList<String> unprocessedEventsBuffer = new ArrayList<>();
                
                try {
                    
                    String tmp_ssml_path = ssml_path.replace(".txt", Integer.toString(numGeneration) + ".txt");
                    String tmp_audio_path = audio_path.replace(".wav", Integer.toString(numGeneration) + ".wav");
                    
                    s_time = greta.core.util.time.Timer.getTime();
                    
                    FileOutputStream fos = new FileOutputStream(tmp_ssml_path);
                    DataOutputStream outStream = new DataOutputStream(new BufferedOutputStream(fos));
                    outStream.writeUTF(ssmlSpeechAzureTTS);
                    outStream.close();
                    
                    System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute(): language: " + this.getCharacterManager().language);
                    
                    OutputStreamWriter writer;
                    if (this.getCharacterManager().language.contains("FR")) {
                        writer = new OutputStreamWriter(new FileOutputStream(tmp_ssml_path), StandardCharsets.ISO_8859_1);                    
                    }
                    else {
                        writer = new OutputStreamWriter(new FileOutputStream(tmp_ssml_path), StandardCharsets.UTF_8);                        
                    }
                    try {
                        writer.write(ssmlSpeechAzureTTS);
                    } finally {
                        writer.close();
                    }
                        
                    
                    if (this.getCharacterManager().language.contains("FR")) {
                        main_process = new ProcessBuilder(AzureTTS_bat_path, tmp_ssml_path, tmp_audio_path, selectedVoiceLocal, "ISO-8859-1").redirectErrorStream(true).start();                    
                    }
                    else {
                        main_process = new ProcessBuilder(AzureTTS_bat_path, tmp_ssml_path, tmp_audio_path, selectedVoiceLocal, "UTF-8").redirectErrorStream(true).start();                                            
                    }
                    main_process.waitFor();
                    
                    e_time = greta.core.util.time.Timer.getTime();
                    
                    excution_time = e_time - s_time;
                    
                    System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute(): python excution time: " + excution_time);
                    
                    if (doAudio) {
                        
                        rawAudioBuffer = loadAudioBufferFromFile(tmp_audio_path);
                        audio = new Audio(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false), rawAudioBuffer); //endianness - true=big, false=little), rawAudioBuffer);

                    }
                    
//                    System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute: python completed");
                    
//                    InputStream inputStream = main_process.getInputStream();
//                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
//                    while (true) {
//                        try {
//                            String line = reader.readLine();
//    //                        if (line.contains("viseme")) {
//    //                            convertViseme2phoneme(line);
//    //                        }
//    //                        else if (line.contains("bookmark")) {
//    //                            convertBookmark2timemarker(line);
//    //                        }
//                            System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute: output from python: " + line);
//                            unprocessedEventsBuffer.add(line);
//                            
//                        }
//                        catch (IOException e) {
//                            break;
//                        }
//                    }

                    BufferedReader reader = 
                                    new BufferedReader(new InputStreamReader(main_process.getInputStream()));
//                    StringBuilder builder = new StringBuilder();
                    String line = null;
                    while ( (line = reader.readLine()) != null) {
                            System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute(): output from python: " + line);
                            unprocessedEventsBuffer.add(line);
                    }
//                    String result = builder.toString();                    
                    
//                    InputStream inputStream = main_process.getInputStream();
//                    String result = new BufferedReader(
//                            new InputStreamReader(inputStream, StandardCharsets.UTF_8))
//                            .lines()
//                            .collect(Collectors.joining("\n")
//                            );
                    
                    currentPlayingSpeech = Speech.getCurrentPlayingScheduledSpeech(false);
                    speech.setGeneratedSSML(ssmlSpeech);
                    
                } catch (IOException ex) {
                    Logger.getLogger(AzureTTS.class.getName()).log(Level.SEVERE, null, ex);
                }


//                ArrayList<String> unprocessedEventsBuffer = fromABufToEvent(speakResultBuffer);
//                ArrayList<String> unprocessedEventsBuffer = prepareEvent();

                unprocessedEventsBuffer = trimUnprocessedEventsBuffer(unprocessedEventsBuffer);

                // Process the received phonems and markers
                
                for (String eventBuffer : unprocessedEventsBuffer) {
                    
                    System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute: eventBuffer: " + eventBuffer);

                    // The eventCereProcBuffer is a sting in the form "start time/end time/time marker name"
                    // The time marker name can start with:
                    // - tmarkercptk (special cereproc)
                    // - tmarkercprc (special cereproc)
                    // - tmarker<greta name> (e.g. tmarkertm1, tmarkertm2, tmarkerend, etc...)
                    String[] parts = eventBuffer.split(" ");
                    double startT = Double.valueOf(parts[0]);
                    double endT = Double.valueOf(parts[1]);
                    double tmTime;

                    tmTime = startTime + startT;

                    // Process time markers
                    if (parts[2].startsWith("bookmark")) {
                        // Process Greta Time Markers
                        tmnumber++;
                        if (tmnumber != 0 && tmnumber < tmOfSpeechList.size()) { // (tmnumber != 0) so we do not touch the start time marker
                            tmOfSpeechList.get((int) tmnumber).setValue(tmTime);
                            
                            System.out.format("greta.auxiliary.tts.azuretts.AzureTTS.compute(): TimeMarker %s, %.2f%n", tmOfSpeechList.get((int) tmnumber), tmTime);
                            
                            if (end.getValue() < tmTime) {
                                end.setValue(tmTime);
                            }
                        }
                    }
                    // Else Process Phonemes
                    else
                    {
                        String visemeID = parts[2].replace("viseme","");
                        // System.out.println("CHECK:    "+languageID+"   "+phonemeType);
//                        PhonemeType[] pho = AzureTTSConstants.convertPhoneme(languageID, phonemeType);
                        PhonemeType[] pho = convertViseme2phoneme(visemeID);
                        

                        if (pho != null) {

                            double duration = (endT - startT);
//                            System.out.println("TTS: duration " + duration);

                            for (int i = 0; i < pho.length; i++) {
                                PhonemeType pt = pho[i];
                                if (pt == PhonemeType.h) {
                                    pt = PhonemeType.a;
                                }
                                Phoneme p = new Phoneme(pt, duration / (pho.length));
                                phonemes.add(p);

                                System.out.format("greta.auxiliary.tts.azuretts.AzureTTS.compute(): phoneme %s, start %.2f, end %.2f%n", pt, startT, endT);

                            }

                        }
                    }
                }
                
//                double tmp = end.getValue() + excution_time;
//                end.setValue(tmp);

                System.out.format("greta.auxiliary.tts.azuretts.AzureTTS.compute(): start: %.2f, end: %.2f, excution_time: %.2f%n", start.getValue(), end.getValue(), excution_time);
                
                // DEBUG
                if (Logs.hasLevelDebug()) {
                    Logs.debug(TimeMarker.getTimeMarkersListDebug(speech.getTimeMarkers(), "SPEECH", speech.getId()));
                }

                // PATCH: Adds dummy pause phonemes if the list of phonemes has less than 3 phonemes to avod bugs in the LipSync model
                int phonemesListSize = phonemes.size();
                int minSize = 3;
                if (phonemesListSize < minSize) {
                    int numPausePhonemesToAdd = Math.abs(phonemesListSize - minSize);
                    for (int i = 0; i < numPausePhonemesToAdd; i++) {
                        phonemes.add(new Phoneme(PhonemeType.pause, 0.1f));
                    }
                }

                /* Play the buffered result (i.e. synthetized voice) */
                if (doAudio)
                {
                                       
                    /*=================================================================================================================*/
                    /*                     This block is part of the Greta Furhat Interface       
                                              Author: Fousseyni Sangaré 04/2024-09/2024 
                            the following lines send the speech text and the audio over the activemq topic for furhat                  */
                    /*=================================================================================================================*/
             
                    try{
                        String speechText = speech.getOriginalText();
                        String speechTextNoSpace = speechText.replace(" ", "");
                        if (!speechTextNoSpace.isEmpty()) {
                            System.out.println("Sending speech text over the topic: " + speechText);
                            // audioserver.send(rawAudioBuffer, phonemes, speech.getSpeechElements(), audio);       // Class not available
                            // speechtextserver.send(speechText);                                                 // Class not available
                            System.out.println("Speech elements: ");
                            phonemes.forEach(element->System.out.print("("+element.getPhonemeType()+" ; "+element.getDuration()+" ) ;"));

                            //speech.getSpeechElements().forEach(element->System.out.print(element.toString()+" ; "));
                            System.out.println();
                            
                        }
                    }
                    catch(Exception e){
                        System.err.println("Error when sending audiobuffer: "+e.getMessage());
                    }

                }


            } catch (Exception e) {
                Logs.error("CereProcTTS: " + this.getClass().getSimpleName() + " fail to load audio. " + e.getMessage());
                e.printStackTrace();
            }
        }
        // numGeneration += 1;
    }
    
    private ArrayList<String> trimUnprocessedEventsBuffer(ArrayList<String> unprocessedEventsBuffer) {
        
        ArrayList<String> output = new ArrayList<>();
        
        ArrayList<String> tmpVisemes = new ArrayList<>();
        ArrayList<String> tmpBookmarks = new ArrayList<>();
        
        // V1: START /////////////////////////////////////////////////////////////////////////////////////////////////////

//        String line;
//        for (int index = 0; index < unprocessedEventsBuffer.size(); index++) {
//            line = unprocessedEventsBuffer.get(index);
//            if (line.contains("viseme")) {
//                tmpVisemes.add(line);
//            }
//            else if (line.contains("bookmark")) {
//                tmpBookmarks.add(line);
//            }
//        }
//
//        for (int index = 0; index < tmpVisemes.size(); index++) {
//            line = tmpVisemes.get(index);
//            String[] tmp = line.split(" ");
//            double offset = Double.parseDouble(tmp[1]) / 1000.0;
//            if (tmpVisemes.size() == (index+1)) {
////                System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute: offset: " + offset);
////                System.out.println("greta.auxiliary.tts.azuretts.AzureTTS.compute: audio duration: " + audio.getDurationMillis());
//                output.add(offset + " " + audio.getDuration() + " viseme" + tmp[2]);
//            }
//            else {
//                String[] tmp2 = tmpVisemes.get(index+1).split(" ");
//                double nextOffset = Double.parseDouble(tmp2[1]) / 1000.0;
//                output.add(offset + " " + nextOffset + " viseme" + tmp[2]);
//            }
//        }
//        
//        for (int index = 0; index < tmpBookmarks.size(); index++) {
//            line = tmpBookmarks.get(index);
//            String[] tmp = line.split(" ");
//            double offset = Double.parseDouble(tmp[2]) / 1000.0;
//            output.add(offset + " " + 0.0 + " bookmark");
//        }
        // V1: END /////////////////////////////////////////////////////////////////////////////////////////////////////
        
        // V2: START /////////////////////////////////////////////////////////////////////////////////////////////////////
        
        String srcLine;
        double offset = 9.9;
        
        for (int index = 0; index < unprocessedEventsBuffer.size(); index++) {
            srcLine = unprocessedEventsBuffer.get(index);
            if (srcLine.contains("viseme")) {
                String[] tmp = srcLine.split(" ");
                offset = Double.parseDouble(tmp[1]) / 1000.0;
                output.add(offset + " " + 0.0 + " viseme" + tmp[2]);
            }
            else if (srcLine.contains("bookmark")) {
                String[] tmp = srcLine.split(" ");
                offset = Double.parseDouble(tmp[2]) / 1000.0;
                output.add(offset + " " + 0.0 + " bookmark");
            }
        }

        output.sort((s1, s2) -> Double.compare(Double.parseDouble(s1.split(" ")[0]), Double.parseDouble(s2.split(" ")[0])));
        
        for (int srcIndex = 0; srcIndex < output.size(); srcIndex++) {
            srcLine = output.get(srcIndex);
            if (srcLine.contains("viseme")) {
                String[] srcTmp = srcLine.split(" ");
                boolean found = false;
                for (int tgtIndex = srcIndex+1; tgtIndex < output.size(); tgtIndex++) {
                    String nextLine = output.get(tgtIndex);
                    if (nextLine.contains("viseme")) {
                        String[] tgtTmp = nextLine.split(" ");
                        output.set(srcIndex, srcTmp[0] + " " + tgtTmp[0] + " " + srcTmp[2]);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    output.set(srcIndex, srcTmp[0] + " " + audio.getDuration() + " " + srcTmp[2]);
                }
            }
        }
        
        // add final bookmark
        output.add(audio.getDuration() + " " + 0.0 + " bookmark");
        
        // Collections.sort(output);
        output.sort((s1, s2) -> Double.compare(Double.parseDouble(s1.split(" ")[0]), Double.parseDouble(s2.split(" ")[0])));

        // V2: END /////////////////////////////////////////////////////////////////////////////////////////////////////
        
        return output;
    }

    private PhonemeType[] convertViseme2phoneme(String input) {
        
        // create mapping list to convert viseme id to IPA phoneme
        // convert viseme id to IPA phoneme
        // convert IPA phoneme to Greta phoneme
        // add those phonemes into phonemes variable
        
        String IPAPhoneme = AzureTTSConstants.visemeId2IPAPhoneme.get(input);
        // PhonemeType phonemeType = clientPhoneme.convertIPAPhoneme2GretaPhoneme(IPAPhoneme);  // Class not available
        PhonemeType phonemeType = PhonemeType.pause; // Default fallback
        
        PhonemeType[] output = new PhonemeType[]{phonemeType};
        
        // System.out.format("greta.auxiliary.tts.azuretts.AzureTTS.convertViseme2phoneme(): from %s to %s(%s)%n",input,IPAPhoneme,phonemeType);
        
        return output;
        
    }
    
    private byte[] loadAudioBufferFromFile(String audioPath) throws FileNotFoundException, IOException {
        
        // Ref: https://stackoverflow.com/questions/10397272/wav-file-convert-to-byte-array-in-java
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(audioPath));

        int read;
        byte[] buff = new byte[2048];
        while ((read = in.read(buff)) > 0)
        {
            out.write(buff, 0, read);
        }
        out.flush();
        byte[] rawAudioBuffer = out.toByteArray();
        
        return rawAudioBuffer;
    }
    
    @Override
    public List<Phoneme> getPhonemes() {
        return phonemes;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    public void clean() {
        audio = null;
        phonemes = new ArrayList<Phoneme>();
        speech = null;
        tmnumber = 0;
    }

    private void setupCharacterLanguageVoiceParameters() {
        languageID = "To be implemented";
        voiceName = "To be implemented";
    }

    private String replaceSpecialCharactersIntoBrackets(String ssmlSpeech, String openBracketReplacement, String closedBracketReplacement)
    {
        String toReturn = ssmlSpeech;
        toReturn = toReturn.replace(openBracketReplacement, "<");
        toReturn = toReturn.replace(closedBracketReplacement, ">");
        return toReturn;
    }

     /**
     * Builds the path to the CereProc's voice for the current version of the TTS (6.0.0_48k_standard).<br/>
     * @param characterLanguage the language parameter (e.g. en-GB) retrieved from the {@code CharacterManager}
     * @param characterVoice the voice name retrieved from the {@code CharacterManager}
     * @return the path to the CereProc's voice for the current character
     */
    public String toCereProcVoicePath(String characterLanguage, String characterVoice) {
        return VOICES_ABSOLUTE_PATH + characterLanguage.toLowerCase() + "-" + characterVoice.toLowerCase() + "/cerevoice_" + characterVoice.toLowerCase() + "_48k_standard.voice";
    }

         /**
     * Builds the path to the CereProc's license for the current version of the TTS (6.0.0_48k_standard).<br/>
     * @param characterLanguage the language parameter (e.g. en-GB) retrieved from the {@code CharacterManager}
     * @param characterVoice the voice name retrieved from the {@code CharacterManager}
     * @return the path to the CereProc's license for the current character's voice
     */
    public String toCereProcLicensePath(String characterLanguage, String characterVoice) {
        return VOICES_ABSOLUTE_PATH + characterLanguage.toLowerCase() + "-" + characterVoice.toLowerCase() + "/" + characterVoice.toLowerCase() + ".lic";
    }
    
    @Override
    public void onCharacterChanged() {
        String newLanguage = getCharacterManager().getLanguage();
        String newVoiceName = "To be implemented";        
    }

//    @Override
//    public void onCharacterChanged() {
//        String newLanguage = getCharacterManager().getLanguage();
//        String newVoiceName = "To be implemented";
//
//        if (newVoiceName.trim().isEmpty() || newLanguage.trim().isEmpty()) {
//            // No voice or language definition found in character configuration, loads the default voice
//
//            Logs.info("CereProcTTS: the character has not a voice/language set in the config file, using default voice/language.");
//            if (voiceName.equalsIgnoreCase(CereProcConstants.DEFAULT_VOICE) && (languageID.equalsIgnoreCase(CereProcConstants.DEFAULT_LANGUAGE))) {
//                // Nothing to do, current voice and language are the default ones and they are already loaded
//            }
//            else {
//                // Unload current voice
//                Logs.info("CereProcTTS: unloading voice [" + voiceName + "] with language [" + languageID + "]");
//                cerevoice_eng.CPRCEN_engine_unload_voice(engineCereProc, 0);
//
//                // Load defaults
//                if (!loadDefaultVoiceLanguage())
//                {
//                    Logs.error("CereProcTTS is unable to load the default voice file. Please check that the folder [" + VOICES_ABSOLUTE_PATH + "] exists and the subfolders (e.g. /en-GB-Sarah) contain the specified voices and licenses.");
//                }
//                else {
//                    Logs.info("CereProcTTS: default voice [" + CereProcConstants.DEFAULT_VOICE + "] with language [" + CereProcConstants.DEFAULT_LANGUAGE + "] succesfeully loaded.");
//                    channelHandle = cerevoice_eng.CPRCEN_engine_open_default_channel(engineCereProc);
//                    if (channelHandle == 0) {
//                        Logs.error("CereProcTTS: unable to re-open default channel");
//                    }
//                    this.setupCharacterLanguageVoiceParameters();
//                }
//            }
//        }
//        else {
//            if ( (!newVoiceName.equalsIgnoreCase(voiceName)) || (!newLanguage.equalsIgnoreCase(languageID))){
//                // Unload current voice and load new one
//                Logs.info("CereProcTTS: unloading voice [" + voiceName + "] with language [" + languageID + "]");
//                cerevoice_eng.CPRCEN_engine_unload_voice(engineCereProc, 0);
//
//                Logs.info("CereProcTTS: loading voice [" + newVoiceName + "] with language [" + newLanguage + "]");
//                String licensePath = toCereProcLicensePath(newLanguage, newVoiceName);
//                String voicePath = toCereProcVoicePath(newLanguage, newVoiceName);
//                cereprocVoiceLoadedFlag = cerevoice_eng.CPRCEN_engine_load_voice(engineCereProc, voicePath, "", CPRC_VOICE_LOAD_TYPE.CPRC_VOICE_LOAD, licensePath, "", "", "");
//
//                if (cereprocVoiceLoadedFlag != 0) {
//                    // Re-open the default synthesis channel
//                    Logs.info("CereProcTTS: re-opening default channel");
//                    channelHandle = cerevoice_eng.CPRCEN_engine_open_default_channel(engineCereProc);
//                    if (channelHandle == 0) {
//                        Logs.error("CereProcTTS: unable to re-open default channel");
//                    }
//                }
//
//                if ((cereprocVoiceLoadedFlag == 0) || (channelHandle == 0)) {
//                    Logs.error("CereProcTTS is unable to load voice file '" + voicePath + "' reverting to previous voice [" + voiceName + "]");
//                    licensePath = toCereProcLicensePath(languageID, voiceName);
//                    voicePath = toCereProcVoicePath(languageID, voiceName);
//                    cerevoice_eng.CPRCEN_engine_load_voice(engineCereProc, voicePath, "", CPRC_VOICE_LOAD_TYPE.CPRC_VOICE_LOAD, licensePath, "", "", "");
//
//                    channelHandle = cerevoice_eng.CPRCEN_engine_open_default_channel(engineCereProc);
//                    if (channelHandle == 0) {
//                        Logs.error("CereProcTTS: unable to re-open default channel");
//                    }
//                }
//                else {
//                    Logs.info("CereProcTTS: voice [" + newVoiceName + "] succesfully loaded");
//                    this.setupCharacterLanguageVoiceParameters();
//                }
//            }
//        }
//    }

    @Override
    protected void finalize() throws Throwable {
        getCharacterManager().remove(this);
        super.finalize();
    }

    private String debugPrintUnprocessedEvents(ArrayList<String> eventsList) {

        String toReturn = "----------------------------- CEREPROC DEBUG -----------------------------\n"
                        + "Unprocessed Events list:\n";
        for (String s : eventsList) {
            String[] parts = s.split("/");

            double startT = Double.valueOf(parts[0]);
            double endT = Double.valueOf(parts[1]);
            String item = parts[2];
            // System.out.println("GRETA CHECK CERE VAL:"+startT+"  "+endT);
            toReturn+="Start [" + startT + "] End [" + endT + "] Item [" + item + "]\n";
        }

        toReturn+="----------------------------- CEREPROC DEBUG -----------------------------";

        return toReturn;
    }
}
