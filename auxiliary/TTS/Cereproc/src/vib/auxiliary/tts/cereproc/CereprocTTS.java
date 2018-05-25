/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.tts.cereproc;

import com.cereproc.cerevoice_eng.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.*;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.Constants;
import vib.core.util.IniManager;
import vib.core.util.audio.Audio;
import vib.core.util.enums.interruptions.ReactionType;
import vib.core.util.log.Logs;
import vib.core.util.speech.Phoneme;
import vib.core.util.speech.Phoneme.PhonemeType;
import vib.core.util.speech.Speech;
import vib.core.util.speech.TTS;
import vib.core.util.time.TimeMarker;
import vib.core.util.time.Timer;
import vib.core.util.xml.XMLTree;

/**
 * This class manages the Cereproc implementation of TTS for VIB<br/>
 * Cereproc has a native library, this class uses a Java wrapper provided by Cereproc (com.cereproc.cerevoice_eng) to access the native interface. <br/>
 *
 * @author Andre-Marie Pez
 * @author Mathieu Chollet
 * @author Angelo Cafaro
 */
public class CereprocTTS implements TTS, CharacterDependent {

    private static boolean initialized = false;
    private static boolean functional = false;
    private static String LOAD_ABSOLUTE_PATH = null;
    private static String VOICES_ABSOLUTE_PATH = null;

    private boolean interreuptionReactionSupported;
    private Audio audio; //audio buffer
    private static AudioFormat audioFormatCereproc;
    private List<Phoneme> phonemes;//phoneme list computed by the native cereproc library
    private Speech speech; //speech object, input to the TTS engine
    private List<TimeMarker> tmOfSpeechList; //time markers list outputed by the native cereproc library
    private static long ptr; //pointer to c++ cereproc
    int tmnumber; //time marker number

    private String languageID; // Character's language specified using <LANGUAGE_CODE_ISO>-<COUNTRY_CODE_ISO> in character's .ini file (e.g. en-GB)
    private String voiceName; // Character's voice specified in character's .ini file (e.g. en-GB)

    // Variable used by the cereproc engine
    static SWIGTYPE_p_CPRCEN_engine engineCereproc;
    static int channelHandle, cereprocVoiceLoadedFlag;
    static String cereprocSampleRate;
    static float cereprocSampleRateFloat;
    static Float samplingRatePlayer;
    static byte[] speechBufferUTF8Bytes;
    float earliestReactionTimeOffset = 0.1f;
    static byte[] emptyBufferInterruptionFallback;

    static {
        init();
    }

    private static boolean checkFolder(String path) {
        return (path != null) && (!path.isEmpty()) && (Files.isDirectory(Paths.get(path)));
    }

    /*
     * Initialization function, CereprocConstants.init() reads parameters from bin/vib.ini file
     */
    private static void init() {

            if (initialized && functional) {
                return;
            }

            initialized = true;

            // Init constants and make phonemes mappings
            CereprocConstants.init();

            // Get System Architecture and OS
            int jvmArchitecture = Integer.parseInt(System.getProperty("sun.arch.data.model"));
            String osName = System.getProperty("os.name").toLowerCase();
            String ARCH_FOLDER = "";
            LOAD_ABSOLUTE_PATH = "";

            // Prepare paths to load dependencies
            if (osName.contains("windows")) {

                if (jvmArchitecture == 32) {
                    ARCH_FOLDER = "Win32/";
                }
                else if (jvmArchitecture == 64) {
                    ARCH_FOLDER = "Win64/";
                }

                // Load dependencies
                LOAD_ABSOLUTE_PATH = IniManager.getProgramPath() + CereprocConstants.DEPENDENCIES_PATH + ARCH_FOLDER;
                // Check if dependencies folder exist
                if (checkFolder(LOAD_ABSOLUTE_PATH)) {
                    System.load(LOAD_ABSOLUTE_PATH + "libgcc_s_sjlj-1.dll");
                    System.load(LOAD_ABSOLUTE_PATH + "libstdc++-6.dll");
                    System.load(LOAD_ABSOLUTE_PATH + "cerevoice_eng.dll");
                }
                else {
                    Logs.error("CereprocTTS failed to find required dependencies (libgcc_s_sjlj-1.dll, libstdc++-6.dll and cerevoice_eng.dll) at [" + LOAD_ABSOLUTE_PATH + "], please check the CEREPROC_DEPENDENCIES_PATH option in vib.ini.");
                    functional = false;
                }

            } else if (osName.contains("mac")) {

                // On Mac OS X a single lib handles both 32 and 64 bits
                ARCH_FOLDER = "MacOSX/";

                // Load dependencies
                LOAD_ABSOLUTE_PATH = IniManager.getProgramPath() + CereprocConstants.DEPENDENCIES_PATH + ARCH_FOLDER;
                // Check if dependencies folder exist
                if (checkFolder(LOAD_ABSOLUTE_PATH)) {
                    System.load(LOAD_ABSOLUTE_PATH + "libcerevoice_eng.dylib");
                }
                else {
                    Logs.error("CereprocTTS failed to find required dependency (libcerevoice_eng.dylib) at [" + LOAD_ABSOLUTE_PATH + "], please check the CEREPROC_DEPENDENCIES_PATH option in vib.ini.");
                    functional = false;
                }

            } else if (osName.contains("linux")) {

                if (jvmArchitecture == 32) {
                    ARCH_FOLDER = "Linux32/";
                }
                else if (jvmArchitecture == 64) {
                    ARCH_FOLDER = "Linux64/";
                }

                // Load dependencies
                LOAD_ABSOLUTE_PATH = IniManager.getProgramPath() + CereprocConstants.DEPENDENCIES_PATH + ARCH_FOLDER;
                // Check if dependencies folder exist
                if (checkFolder(LOAD_ABSOLUTE_PATH)) {
                    System.load(LOAD_ABSOLUTE_PATH + "libcerevoice_eng.so");
                }
                else {
                    Logs.error("CereprocTTS failed to find required dependency (libcerevoice_eng.so) at [" + LOAD_ABSOLUTE_PATH + "], please check the CEREPROC_DEPENDENCIES_PATH option in vib.ini.");
                    functional = false;
                }
            } else {
                Logs.error("CereprocTTS failed to load required dependencies (libraries) for the OS [" + osName + "], Architecture[" + jvmArchitecture + "].");
                functional = false;
            }

            // Init the engine
            boolean initSuccess = initCereprocEngine();

            if (initSuccess) {
                Logs.info("CereprocTTS successfully initialized.");
                functional = true;
            }
            else {
                Logs.error("CereprocTTS failed to initialize.");
                functional = false;
            }
    }

    private static boolean initCereprocEngine() {

        // Creates the engine
	Logs.info("CereprocTTS is creating CereVoice Engine");
	engineCereproc = cerevoice_eng.CPRCEN_engine_new();

        // Check if voices folder exist
        VOICES_ABSOLUTE_PATH = IniManager.getProgramPath() + CereprocConstants.VOICES_PATH;
        if (!checkFolder(VOICES_ABSOLUTE_PATH)) {
            Logs.error("CereprocTTS failed to find voice folder at [" + VOICES_ABSOLUTE_PATH + "], please check if the CEREPROC_VOICES_PATH option is correctly set in vib.ini.");
            return false;
        }

        // Load the voice
        String language = CharacterManager.getValueString("CEREPROC_LANG");
        String voice = CharacterManager.getValueString("CEREPROC_VOICE");

        boolean loadDefault = false;
        if ((language == null) || (voice == null) || (language.isEmpty()) || (voice.isEmpty())) {
            Logs.warning("CereprocTTS: missing language or voice setting in character configuration file. "
                    + "Using default voice[" + CereprocConstants.DEFAULT_VOICE + "] and default language [" + CereprocConstants.DEFAULT_LANGUAGE + "]");
            loadDefault = true;
        }
        else {
            Logs.info("CereprocTTS is loading voice [" + voice + "] with language [" + language + "]");
        }

        String licensePath = null;
        String voicePath = null;
        if (!loadDefault) {
            licensePath = toCereprocLicensePath(language, voice);
            voicePath = toCereprocVoicePath(language, voice);
            cereprocVoiceLoadedFlag = cerevoice_eng.CPRCEN_engine_load_voice(engineCereproc, licensePath, "", voicePath, CPRC_VOICE_LOAD_TYPE.CPRC_VOICE_LOAD);
        } else {
            loadDefaultVoiceLanguage();
        }

	if (cereprocVoiceLoadedFlag == 0) {
	    Logs.error("CereprocTTS is unable to load the voice file '" + voicePath + "', using default voice[" + CereprocConstants.DEFAULT_VOICE + "] and default language [" + CereprocConstants.DEFAULT_LANGUAGE + "]");
            if (!loadDefaultVoiceLanguage())
            {
                Logs.error("CereprocTTS is unable to load the default voice file. Please check that the folder [" + VOICES_ABSOLUTE_PATH + "] exists and the subfolders (e.g. /en-gb-sarah) contain the specified voices and licenses.");
                return false;
            }
            else {
                Logs.info("CereprocTTS default voice [" + CereprocConstants.DEFAULT_VOICE + "] with language [" + CereprocConstants.DEFAULT_LANGUAGE + "] succesfeully loaded.");
            }
	}

	// Open the default synthesis channel
	Logs.info("CereprocTTS is opening default channel");
	channelHandle = cerevoice_eng.CPRCEN_engine_open_default_channel(engineCereproc);
	if (channelHandle == 0) {
	    Logs.error("CereprocTTS is unable to open default channel");
	    return false;
        }

        // Init the Phonemes correspondances
        CereprocConstants.InitPhonemes();

        // Init the Sampling Rate of the engine
        cereprocSampleRate = cerevoice_eng.CPRCEN_channel_get_voice_info(engineCereproc, channelHandle, "SAMPLE_RATE");
        cereprocSampleRateFloat = Float.parseFloat(cereprocSampleRate);
        
        // Creates an AudioFomat used later to play the audio buffer (synthetized voice) produced by the engine
        audioFormatCereproc = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            cereprocSampleRateFloat, //sample rate
            16, //bits - sample size
            1, //mono - channels
            2, //bytes - frame size (sample size * channels)
            Float.parseFloat(cereprocSampleRate), //Hz - frame rate
            false); //endianness - true=big, false=little;
        
        // Init the fallback buffer in case of an interruption call that results in a delay
        double minimumDuration = 0.002;
        emptyBufferInterruptionFallback = new byte[(int)(audioFormatCereproc.getFrameSize() * cereprocSampleRateFloat * minimumDuration)];
        
        return true;
    }

    /**
     * Constructor.
     */
    public CereprocTTS() {

        init();

        interreuptionReactionSupported = true;
        
        setupCharacterLanguageVoiceParameters();

        clean();

        tmnumber = 0;

        CharacterManager.add(this);
    }

    /**
     * Set new speech object to be computed. </br>
     * Also sets a new temp file path to avoid the speech duplication bug.
     * @param speech Speech object to compute
     */
    @Override
    public void setSpeech(Speech speech) {
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
     * The three boolean should all be true: Cereproc only does all at once.
     * @param doTemporize Temporize or not
     * @param doAudio Compute audio or not
     * @param doPhonems Compute phonems or not
     */
    @Override
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonems) {

        if (!functional) {
            Logs.error("CereprocTTS: the module is not fully functional due to initialization errors. The speech that has been sent to be computed is ignored.");
            return;
        }

        if (speech == null) {
            Logs.error("CereprocTTS: the speech variable is null.");
            return ;
        }

        if (!doPhonems || !doTemporize || !doAudio) {
            Logs.warning("CereprocTTS: the engine only computes all at once.");
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
                    Logs.debug("----------------------------- CEREPROC DEBUG -----------------------------");
                    Logs.debug("SSML:");
                    Logs.debug(ssml.toString(true));
                    Logs.debug("----------------------------- CEREPROC DEBUG -----------------------------");
                }
                
                String ssmlSpeech = ssml.toString(false);
                ssmlSpeech = replaceSpecialCharactersIntoBrackets(ssmlSpeech, "#!", "!#");
                
                // Clear  the callback
                cerevoice_eng.CPRCEN_engine_clear_callback(engineCereproc,channelHandle);

                // Send processing to native code
                speechBufferUTF8Bytes = ssmlSpeech.getBytes("UTF-8");

                // Get the buffer structure
                SWIGTYPE_p_CPRC_abuf speakResultBuffer;
                float interruptionTime_s = 0;
                float earliestReactionTime_s = 0;
                long timeBeforeCerengineCall = System.currentTimeMillis();
                
                int currentPlayingBufferPos = 0;
                Speech currentPlayingSpeech;
                boolean reactionToInterruptionCall = false;
                
                if (speech.getInterruptionReactionType() != ReactionType.NONE) {
                    
                    // REACTION TTS CALL
                    reactionToInterruptionCall = true;
                    currentPlayingSpeech = Speech.getCurrentPlayingScheduledSpeech(true);
                    currentPlayingBufferPos = -1;
                    String currentPlayingBufferSSML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                        "<speak version=\"1.0\" xml:lang=\"en-US\" " +
                        "xmlns=\"http://www.w3.org/2001/10/synthesis\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.w3.org/2001/10/synthesis http://www.w3.org/TR/speech-synthesis/synthesis.xsd\"> " +
                        "<prosody pitch=\"0.0%\" rate=\"0.0%\"> " +
                        "<mark name=\"" + Constants.TIME_MARKER_INTERRUPTION_REACTION_END_ID +"\"/>\n" +
                        "<mark name=\"end\"/>" +
                        "</prosody> " +
                        "</speak>";
                    
                    if (currentPlayingSpeech != null) {
                        currentPlayingBufferPos = currentPlayingSpeech.getAudio().getPlayingBufferPos();
                        currentPlayingBufferSSML = currentPlayingSpeech.getGeneratedSSML();
                        interruptionTime_s = CereprocConstants.fromVIBBufferPositionToCEREPROC(currentPlayingBufferPos);
                    }
                    
                    // Add the offset for the earliest interruption time
                    earliestReactionTime_s = interruptionTime_s + earliestReactionTimeOffset;
                                       
                    speakResultBuffer = cerevoice_eng.CPRCEN_engine_channel_interrupt_legacy(engineCereproc, 
                            channelHandle, 
                            currentPlayingBufferSSML, 
                            currentPlayingBufferSSML.length(),
                            earliestReactionTime_s,
                            CereprocConstants.fromVIBReactionDurationToCEREPROC(speech.getInterruptionReactionDuration()),
                            CereprocConstants.fromVIBReactionTypeToCEREPROC(speech.getInterruptionReactionType()),
                            1
                    );
                    
                    // Append to the speakResultBuffer (containing the transition) a new abuf that contains the synthetized speech replanned
                    if (speech.getInterruptionReactionType() == ReactionType.REPLAN) {
                        // The append to speakResultBuffer (containing the reaction to interruption) of the newly created buffer (containing the replanned speech)
                        // is automatically done internally by cereproc when calling CPRCEN_engine_channel_speak
                        speakResultBuffer = cerevoice_eng.CPRCEN_engine_channel_speak(engineCereproc, channelHandle, ssmlSpeech, speechBufferUTF8Bytes.length, 1);
                    }                    
                    
                }
                else {
                    
                    // REGULAR TTS CALL
                    currentPlayingSpeech = Speech.getCurrentPlayingScheduledSpeech(false);
                    speakResultBuffer = cerevoice_eng.CPRCEN_engine_channel_speak(engineCereproc, channelHandle, ssmlSpeech, speechBufferUTF8Bytes.length, 1);
                }
                
                speech.setGeneratedSSML(ssmlSpeech);
                         
                ArrayList<String> unprocessedEventsBufferCereproc = fromABufToEvent(speakResultBuffer);
                
                // DEBUG
                if (Logs.hasLevelDebug()) {
                    Logs.debug(this.debugPrintUnprocessedEvents(unprocessedEventsBufferCereproc));
                }
                
                int newcurrentPlayingBufferPos = 0;
                if (currentPlayingSpeech != null) {
                    newcurrentPlayingBufferPos = currentPlayingSpeech.getAudio().getPlayingBufferPos();
                }
                float currentPlayingTime_s = CereprocConstants.fromVIBBufferPositionToCEREPROC(newcurrentPlayingBufferPos);
                double interruptionReactionStartTime_s = 0;
                double interruptionReactionEndTime_s = 0;
                double interruptionBufferEndTime_s = 0;
                
                // Process the received phonems and markers
                for (String eventCereprocBuffer : unprocessedEventsBufferCereproc) {
                    
                    // The eventCereprocBuffer is a sting in the form "start time/end time/time marker name"
                    // The time marker name can start with:
                    // - tmarkercptk (special cereproc)
                    // - tmarkercprc (special cereproc)
                    // - tmarker<vib name> (e.g. tmarkertm1, tmarkertm2, tmarkerend, etc...)
                    String[] parts = eventCereprocBuffer.split("/");
                    double startT = Double.valueOf(parts[0]);
                    double endT = Double.valueOf(parts[1]);
                    double tmTime;
                            
                    if (reactionToInterruptionCall) {
                        tmTime = startTime + startT - ((currentPlayingTime_s!=0)?currentPlayingTime_s:earliestReactionTime_s);
                    }
                    else {
                        tmTime = startTime + startT;
                    }
                    
                    // Process time markers
                    if (parts[2].startsWith("tmarker")) {
                        
                        // Process Cereproc Time Markers
                        if (parts[2].startsWith("tmarkercptk") || parts[2].startsWith("tmarkercprc")) {
                            
                            // Time markers for reactions to interruptions
                            if (parts[2].trim().startsWith("tmarkercprc_interrupt_")) {
                                
                                // Time marker idicating the end of an interruption reaction
                                if (parts[2].trim().equals("tmarkercprc_interrupt_end")) {
                                    interruptionReactionEndTime_s = tmTime;
                                }
                                else {
                                    // We do not use it but tmarkercprc_interrupt_X marks the beginning of the reaction at phoneme X
                                    // We only store its value and, instead, we use our calculated earliestReactionTime_s to mark the reaction start
                                    interruptionReactionStartTime_s = tmTime;
                                }
                            }
                        }                        
                        // Process VIB Time Markers
                        else {
                            // Process the Time Markers received in the buffer received by cereproc
                            
                            // In case of a reaction to an interruption the given speech has:
                            // - Only START, END and SPECIAL time markers for HALT AND OVERLAP
                            // - START, END, OTHER(s) (e.g. tm1,tm2...) and SPECIAL time markers for REPLAN
                            if (reactionToInterruptionCall) {
                                
                                // Takes the end of speech time marker in any case
                                if (parts[2].trim().equals("tmarkerend")) {
                                    interruptionBufferEndTime_s = tmTime;
                                }
                                
                                // Discard if the absolute time refers to a time before the interruption occured (negative time)
                                if (endT < currentPlayingTime_s) {
                                    continue;
                                }
                                else {
                                    // In the REPLAN case the reaction has only START and SPECIAL time markers
                                    // but the new FML to use for replan can have more time markers that we need to concretize
                                    if (parts[2].trim().startsWith("tmarkertm")) {
                                        String timeMarkerName = parts[2].substring(7);
                                        if ( tmOfSpeechMap.containsKey(timeMarkerName) && timeMarkerName.startsWith("tm") ) {
                                            TimeMarker tm = (TimeMarker) tmOfSpeechMap.get(timeMarkerName);
                                            tm.setValue(tmTime);
                                        }
                                    }
                                }
                            }
                            // In the case of a regular TTS call there can be more time markers
                            else {
                                tmnumber++;
                                if (tmnumber != 0 && tmnumber < tmOfSpeechList.size()) { // (tmnumber != 0) so we do not touch the start time marker
                                    tmOfSpeechList.get((int) tmnumber).setValue(tmTime);
                                    if (end.getValue() < tmTime) {
                                        end.setValue(tmTime);
                                    }
                                }
                            }
                        } // End else VIB Time Marker only
                    }
                    // Else Process Phonemes
                    else 
                    {
                        String phonemeType = parts[2];
                        PhonemeType[] pho = CereprocConstants.convertPhoneme(languageID, phonemeType);
                        if (pho != null) {
                            
                            // Discard it the absolute time refers to a time before the interruption occured (negative time)
                            if ((reactionToInterruptionCall) && (endT < currentPlayingTime_s)) {
                                continue;
                            }
                            double duration = (endT - startT);
                            if ((reactionToInterruptionCall) && (startT < currentPlayingTime_s)) {
                                duration = endT - currentPlayingTime_s;
                            }
                            for (int i = 0; i < pho.length; i++) {
                                PhonemeType pt = pho[i];
                                Phoneme p = new Phoneme(pt, duration / (pho.length));
                                phonemes.add(p);
                            }
                        }
                    }
                }
                
                // DEBUG
                if (Logs.hasLevelDebug()) {
                    Logs.debug("----------------------------- CEREPROC DEBUG -----------------------------\n");
                    Logs.debug("interruptionTime_s [" + interruptionTime_s + "]\n"
                             + "currentPlayingTime [" + currentPlayingTime_s + "]\n"
                             + "earliestReactionTime_s [" + earliestReactionTime_s + "]\n"
                             + "interruptionReactionStartTime_s [" + interruptionReactionStartTime_s + "]\n"
                             + "interruptionReactionEndTime_s [" + interruptionReactionEndTime_s + "]\n"
                             + "interruptionBufferEndTime_s [" + interruptionBufferEndTime_s + "]\n"
                    );
                    Logs.debug("----------------------------- CEREPROC DEBUG -----------------------------\n");
                }
                
                // Reposition the special time markers with concretized values
                TimeMarker tmInterruptionDetected = speech.getTimeMarker(Constants._TIME_MARKER_INTERRUPTION_DETECTED_ID);
                TimeMarker tmReactionStarted = speech.getTimeMarker(Constants._TIME_MARKER_INTERRUPTION_REACTION_STARTED_ID);
                TimeMarker tmReactionEnd = speech.getTimeMarker(Constants.TIME_MARKER_INTERRUPTION_REACTION_END_ID);
                
                if (tmInterruptionDetected != null) {
                    tmInterruptionDetected.setValue(interruptionTime_s);
                }
                
                if (tmReactionStarted != null) {
                    if (currentPlayingTime_s != 0) {
                        tmReactionStarted.setValue(currentPlayingTime_s);
                    }
                    else {
                        tmReactionStarted.setValue(earliestReactionTime_s);
                    }
                }
                
                if (tmReactionEnd != null) {
                    if (interruptionReactionEndTime_s != 0) {
                        tmReactionEnd.setValue(interruptionReactionEndTime_s);
                    }
                    else {
                        tmReactionEnd.setValue(speech.getEnd().getValue());
                    }
                }
                
                 // Reposition the END time marker with a concretized value in case of reaction to interruptions
                if ((reactionToInterruptionCall) && (interruptionBufferEndTime_s != 0)) {
                    end.setValue(interruptionBufferEndTime_s);
                }
                
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
                
                long timeAfterCerengineCall = System.currentTimeMillis();
                earliestReactionTimeOffset = (timeAfterCerengineCall - timeBeforeCerengineCall) / 1000f;
                
                /* Play the buffered result (i.e. synthetized voice) */
                if (doAudio)
                {    
                    // samples16bitNum is the number of 16-bits samples
                    int samples16bitNum =  cerevoice_eng.CPRC_abuf_wav_sz(speakResultBuffer);
                    short sampleAudio;

                    // Now we extract the audio, convert it, send it to java audio playback
                    // This is not the most elegant way to do this conversion, but shows
                    // how e.g. audio effects could be applied.
                    int sampleBegin = 0;
                                       
                    if ((interruptionTime_s != 0) && (newcurrentPlayingBufferPos != 0)) {
                        sampleBegin = (int) (newcurrentPlayingBufferPos / 2);
                        
                        //Debug
                        /*Audio aa = currentPlayingSpeech.getAudio();
                        byte[] rawAudioBuffer = new byte[newcurrentPlayingBufferPos];
                        for(int i=0; i<rawAudioBuffer.length; ++i){
                            rawAudioBuffer[i] = aa.getBuffer()[i];
                        }
                        Audio audio2 = new Audio(aa.getFormat(), rawAudioBuffer);
                        audio2.save("111-"+ vib.core.util.id.IDProvider.createID("CEREPROC_TEST").toString()+ ".wav");
                        */
                    }
                    
                    byte[] rawAudioBuffer = new byte[Math.max(0, samples16bitNum-sampleBegin) * 2];
                    for(int i = sampleBegin; i < samples16bitNum; i++) {
                        // Sample at position i, it is a short
                        sampleAudio = cerevoice_eng.CPRC_abuf_wav(speakResultBuffer, i);
                        // The sample is written in Little Endian to the buffer
                        int offset = i-sampleBegin;
                        rawAudioBuffer[offset * 2 + 1] = (byte) ((sampleAudio & 0xff00) >> 8);
                        rawAudioBuffer[offset * 2] = (byte) (sampleAudio & 0x00ff);
                    }
                    
                    if (rawAudioBuffer.length <= 0) {
                        rawAudioBuffer = emptyBufferInterruptionFallback;
                    }

                    // Create the Audio object that will be played
                    audio = new Audio(audioFormatCereproc, rawAudioBuffer);
                    
                    /*  DEBUG                 
                    rawAudioBuffer = new byte[(samples16bitNum) * 2];
                    for(int i = 0; i < samples16bitNum; i++) {
                        // Sample at position i, it is a short
                        sampleAudio = cerevoice_eng.CPRC_abuf_wav(speakResultBuffer, i);
                        // The sample is written in Little Endian to the buffer
                        int offset = i;
                        rawAudioBuffer[offset * 2 + 1] = (byte) ((sampleAudio & 0xff00) >> 8);
                        rawAudioBuffer[offset * 2] = (byte) (sampleAudio & 0x00ff);
                    }
                    Audio audio2 = new Audio(audioFormatCereproc, rawAudioBuffer);
                    audio2.save("000-"+ vib.core.util.id.IDProvider.createID("CEREPROC_TEST").toString()+ ".wav");
                    */
 
                    /*Logs.info("CereprocTTS: currentPlayingBufferPos[" + currentPlayingBufferPos + "] "
                            + "earliestReactionTime_s[" + earliestReactionTime_s + "] "
                            + "newcurrentPlayingBufferPos[" + newcurrentPlayingBufferPos + "] "
                            + "samples16bitNum[" + samples16bitNum + "] "
                            + "XML [" + speech.getGeneratedSSML()+ "]");
                    */
                }
                

            } catch (Exception e) {
                Logs.error("CereprocTTS: " + this.getClass().getSimpleName() + " fail to load audio. " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static ArrayList<String> fromABufToEvent(SWIGTYPE_p_CPRC_abuf abuf){
        SWIGTYPE_p_CPRC_abuf_trans trans;
	CPRC_ABUF_TRANS_TYPE transtype;
	float start, end;
	String name;

        ArrayList<String> returnEvents = new ArrayList();

	// First process the transcription information and print it
	// Process the transcription buffer items and print information.
	for(int i = 0; i < cerevoice_eng.CPRC_abuf_trans_sz(abuf); i++) {
	    trans = cerevoice_eng.CPRC_abuf_get_trans(abuf, i);
	    transtype = cerevoice_eng.CPRC_abuf_trans_type(trans);
	    start = cerevoice_eng. CPRC_abuf_trans_start(trans);
	    end = cerevoice_eng.CPRC_abuf_trans_end(trans);
	    name = cerevoice_eng.CPRC_abuf_trans_name(trans);
	    if (transtype == CPRC_ABUF_TRANS_TYPE.CPRC_ABUF_TRANS_PHONE) {
                returnEvents.add(start + "/" + end + "/" + name);
	    }
	    else if (transtype == CPRC_ABUF_TRANS_TYPE.CPRC_ABUF_TRANS_WORD) {
                // Ignore this
	    }
	    else if (transtype == CPRC_ABUF_TRANS_TYPE.CPRC_ABUF_TRANS_MARK) {
                //ss << start << "/" << end <<"/"<<"tmarker"<<name;
		returnEvents.add(start + "/" + end + "/tmarker" + name);
	    }
	    else if (transtype == CPRC_ABUF_TRANS_TYPE.CPRC_ABUF_TRANS_ERROR) {
		Logs.error("CereprocTTS: could not retrieve transcription at " + i);
	    }
	}

        return returnEvents;
    }

    @Override
    public List<Phoneme> getPhonemes() {
        return phonemes;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    private void clean() {
        audio = null;
        phonemes = new ArrayList<Phoneme>();
        speech = null;
        tmnumber = 0;
    }

    private void setupCharacterLanguageVoiceParameters() {

        // Get language and country code from the voice currently loaded
        String languageCode = cerevoice_eng.CPRCEN_channel_get_voice_info(engineCereproc, channelHandle, "LANGUAGE_CODE_ISO");
        String countryCode = cerevoice_eng.CPRCEN_channel_get_voice_info(engineCereproc, channelHandle, "COUNTRY_CODE_ISO");
        languageID = languageCode + "-" + countryCode;
        voiceName = cerevoice_eng.CPRCEN_engine_get_voice_info(engineCereproc, 0, "VOICE_NAME");
    }

    private String replaceSpecialCharactersIntoBrackets(String ssmlSpeech, String openBracketReplacement, String closedBracketReplacement)
    {
        String toReturn = ssmlSpeech;
        toReturn = toReturn.replace(openBracketReplacement, "<");
        toReturn = toReturn.replace(closedBracketReplacement, ">");
        return toReturn;
    }

     /**
     * Builds the path to the Cereproc's voice for the current version of the TTS (3.2.0_48k).<br/>
     * @param characterLanguage the language parameter (e.g. en-GB) retrieved from the {@code CharacterManager}
     * @param characterVoice the voice name retrieved from the {@code CharacterManager}
     * @return the path to the Cereproc's voice for the current character
     */
    public static String toCereprocVoicePath(String characterLanguage, String characterVoice) {
        return VOICES_ABSOLUTE_PATH + characterLanguage.toLowerCase() + "-" + characterVoice.toLowerCase() + "/cerevoice_" + characterVoice.toLowerCase() + "_3.2.0_48k.voice";
    }

         /**
     * Builds the path to the Cereproc's license for the current version of the TTS (3.2.0_48k).<br/>
     * @param characterLanguage the language parameter (e.g. en-GB) retrieved from the {@code CharacterManager}
     * @param characterVoice the voice name retrieved from the {@code CharacterManager}
     * @return the path to the Cereproc's license for the current character's voice
     */
    public static String toCereprocLicensePath(String characterLanguage, String characterVoice) {
        return VOICES_ABSOLUTE_PATH + characterLanguage.toLowerCase() + "-" + characterVoice.toLowerCase() + "/" + characterVoice.toLowerCase() + ".lic";
    }

    @Override
    public void onCharacterChanged() {
        String newLanguage = CharacterManager.getValueString("CEREPROC_LANG");
        String newVoiceName = CharacterManager.getValueString("CEREPROC_VOICE").toLowerCase().trim();

        if (newVoiceName.trim().isEmpty() || newLanguage.trim().isEmpty()) {
            // No voice or language definition found in character configuration, loads the default voice

            Logs.info("CereprocTTS: the character has not a voice/language set in the config file, using default voice/language.");
            if (voiceName.equalsIgnoreCase(CereprocConstants.DEFAULT_VOICE) && (languageID.equalsIgnoreCase(CereprocConstants.DEFAULT_LANGUAGE))) {
                // Nothing to do, current voice and language are the default ones and they are already loaded
            }
            else {
                // Unload current voice
                Logs.info("CereprocTTS: unloading voice [" + voiceName + "] with language [" + languageID + "]");
                cerevoice_eng.CPRCEN_engine_unload_voice(engineCereproc, 0);

                // Load defaults
                if (!loadDefaultVoiceLanguage())
                {
                    Logs.error("CereprocTTS is unable to load the default voice file. Please check that the folder [" + VOICES_ABSOLUTE_PATH + "] exists and the subfolders (e.g. /en-gb-sarah) contain the specified voices and licenses.");
                }
                else {
                    Logs.info("CereprocTTS: default voice [" + CereprocConstants.DEFAULT_VOICE + "] with language [" + CereprocConstants.DEFAULT_LANGUAGE + "] succesfeully loaded.");
                    channelHandle = cerevoice_eng.CPRCEN_engine_open_default_channel(engineCereproc);
                    if (channelHandle == 0) {
                        Logs.error("CereprocTTS: unable to re-open default channel");
                    }
                    this.setupCharacterLanguageVoiceParameters();
                }
            }
        }
        else {
            if ( (!newVoiceName.equalsIgnoreCase(voiceName)) || (!newLanguage.equalsIgnoreCase(languageID))){
                // Unload current voice and load new one
                Logs.info("CereprocTTS: unloading voice [" + voiceName + "] with language [" + languageID + "]");
                cerevoice_eng.CPRCEN_engine_unload_voice(engineCereproc, 0);

                Logs.info("CereprocTTS: loading voice [" + newVoiceName + "] with language [" + newLanguage + "]");
                String licensePath = toCereprocLicensePath(newLanguage, newVoiceName);
                String voicePath = toCereprocVoicePath(newLanguage, newVoiceName);
                cereprocVoiceLoadedFlag = cerevoice_eng.CPRCEN_engine_load_voice(engineCereproc, licensePath, "", voicePath, CPRC_VOICE_LOAD_TYPE.CPRC_VOICE_LOAD);

                if (cereprocVoiceLoadedFlag != 0) {
                    // Re-open the default synthesis channel
                    Logs.info("CereprocTTS: re-opening default channel");
                    channelHandle = cerevoice_eng.CPRCEN_engine_open_default_channel(engineCereproc);
                    if (channelHandle == 0) {
                        Logs.error("CereprocTTS: unable to re-open default channel");
                    }
                }

                if ((cereprocVoiceLoadedFlag == 0) || (channelHandle == 0)) {
                    Logs.error("CereprocTTS is unable to load voice file '" + voicePath + "' reverting to previous voice [" + voiceName + "]");
                    licensePath = toCereprocLicensePath(languageID, voiceName);
                    voicePath = toCereprocVoicePath(languageID, voiceName);
                    cerevoice_eng.CPRCEN_engine_load_voice(engineCereproc, licensePath, "", voicePath, CPRC_VOICE_LOAD_TYPE.CPRC_VOICE_LOAD);

                    channelHandle = cerevoice_eng.CPRCEN_engine_open_default_channel(engineCereproc);
                    if (channelHandle == 0) {
                        Logs.error("CereprocTTS: unable to re-open default channel");
                    }
                }
                else {
                    Logs.info("CereprocTTS: voice [" + newVoiceName + "] succesfully loaded");
                    this.setupCharacterLanguageVoiceParameters();
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        CharacterManager.remove(this);
        super.finalize();
    }

    private static boolean loadDefaultVoiceLanguage() {
        String licensePath = toCereprocLicensePath(CereprocConstants.DEFAULT_LANGUAGE, CereprocConstants.DEFAULT_VOICE);
        String voicePath = toCereprocVoicePath(CereprocConstants.DEFAULT_LANGUAGE, CereprocConstants.DEFAULT_VOICE);
        if (cerevoice_eng.CPRCEN_engine_load_voice(engineCereproc, licensePath, "", voicePath, CPRC_VOICE_LOAD_TYPE.CPRC_VOICE_LOAD) == 0)
        {
            return false;
        }
        else {
            return true;
        }
    }
    
    private String debugPrintUnprocessedEvents(ArrayList<String> eventsList) {
        
        String toReturn = "----------------------------- CEREPROC DEBUG -----------------------------\n"
                        + "Unprocessed Events list:\n";
        for (String s : eventsList) {
            String[] parts = s.split("/");
            
            double startT = Double.valueOf(parts[0]);
            double endT = Double.valueOf(parts[1]);
            String item = parts[2];
            
            toReturn+="Start [" + startT + "] End [" + endT + "] Item [" + item + "]\n";
        }
        
        toReturn+="----------------------------- CEREPROC DEBUG -----------------------------";
        
        return toReturn;
    }
}
