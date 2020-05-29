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

import greta.core.util.audio.Audio;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import greta.core.util.speech.Phoneme;
import greta.core.util.speech.Phoneme.PhonemeType;
import greta.core.util.speech.Speech;
import greta.core.util.speech.TTS;
import greta.core.util.time.TimeMarker;
import greta.core.util.xml.XMLTree;

//import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
//import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import javax.sound.sampled.*;
import lpl.tools.ContinuousEventIfce;
//import lpl.tts.SpeechDataTools;

import lpl.tts.voxygen.BaratinooSwig;
import lpl.tts.voxygen.EVENT_TYPE;
import lpl.tts.voxygen.LogLevel;
import lpl.tts.voxygen.SpeechResult;
import lpl.tts.voxygen.TimeStampEvent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class manages the Voxygen implementation of TTS for VIB<br/>
 * 
 * @author Brian Ravenet
 * @author Angelo Cafaro
 * @author Grégoire Montcheuil
 */
public class VoxygenTTS extends CharacterDependentAdapter implements TTS {

    private static boolean initialized = false; // TTS library is initialized
    private static boolean functional = false;  // TTS library work
    /** Absolute path to the dynamic libraries.
     * Based on DEPENDENCIES_PATH (program_path + DEPENDENCIES_PATH + arch folder)
     *   define by VOXYGEN_DEPENDENCIES_PATH in the INI file.
     */
    private static String LOAD_ABSOLUTE_PATH = null;
    /**
     * Absolute path for the Voxygen voices configuration,
     *  i.e. where is baratinoo.cfg, then voices data are in the data sub-directory.
     * Based on VOICES_PATH (program_path + VOICES_PATH)
     *   define by VOXYGEN_VOICES_PATH in the INI file.
     */
    private static String VOICES_ABSOLUTE_PATH = null;
    
    /** Voxygen library log file.*/
    private static boolean logBaratinoo = true;
    private static File logDirectory = null;
    protected static String logBaratinooFileName = "BaratinooSSMLtoSpeech.log";
    
    /** The Voxygen library SWIG interface.*/
    private static BaratinooSwig batatinooSwig = null;
    
    /** The Voxygen Config path (from working directory).*/
    protected static String voxygenConfigFile = "baratinoo.cfg";

    // (!) Greta expect  RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 48000 Hz
    /** The wav output frequency.*/
    protected static int wavFrequency=48000;
	
    /** The set of wanted events.*/
    static Set<EVENT_TYPE> wantedEvents = EnumSet.of(EVENT_TYPE.MARKER_EVENT, EVENT_TYPE.VISEME_EVENT);
    /** The minimal milisecond difference to insert a pause phonème */
    protected double phonemMinPause = 0.5;
    
    private boolean interreuptionReactionSupported = false;
    private Audio audio; //audio buffer
    private static AudioFormat baratinooAudioFormat;
    private List<Phoneme> phonemes;//phoneme list computed by the native cereproc library
    private Speech speech; //speech object, input to the TTS engine
    private List<TimeMarker> tmOfSpeechList; //time markers list outputed by the native cereproc library
    //private static long ptr; //pointer to c++ cereproc
    int tmnumber; //time marker number

    private String languageID = VoxygenConstants.DEFAULT_LANGUAGE; //TODO: Character's language specified using <LANGUAGE_CODE_ISO>-<COUNTRY_CODE_ISO> in character's .ini file (e.g. en-GB)
    private String voiceName = VoxygenConstants.DEFAULT_VOICE; //TODO: Character's voice specified in character's .ini file (e.g. en-GB)

    /** The <i>global</i> prosody rate percent value.
     * Speech.toSSML() add a &lt;prosody pitch="pitch" rate="rate"&gt; element around the whole speech.
     * The value is a percent (i.e. 100 for a normal rate, &lt;100 to slow down, &gt;100 to speed up).
     */
    private double rate =100.0;
    /** The <i>global</i> prosody pitch value.
     * Speech.toSSML() add a &lt;prosody pitch="pitch" rate="rate"&gt; element around the whole speech.
     */
    private double pitch =0.0;
    
    // Variable used by the cereproc engine
    //static int channelHandle, cereprocVoiceLoadedFlag;
    //static String cereprocSampleRate;
    //static float cereprocSampleRateFloat;
    //static Float samplingRatePlayer;
    //? static byte[] speechBufferUTF8Bytes;
    //float earliestReactionTimeOffset = 0.1f;
    static byte[] emptyBufferInterruptionFallback;

    static {
        init();
    }

    private static boolean checkFolder(String path) {
        return (path != null) && (!path.isEmpty()) && (Files.isDirectory(Paths.get(path)));
    }

    /*
     * Initialization function, VoxygenConstants.init() reads parameters from bin/Greta.ini file
     */
    private static void init() {

            if (initialized && functional) {
                return;
            }

            initialized = true;

            // Init constants and make phonemes mappings
            VoxygenConstants.init();

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
                LOAD_ABSOLUTE_PATH = IniManager.getProgramPath() + VoxygenConstants.DEPENDENCIES_PATH + ARCH_FOLDER;
                // Check if dependencies folder exist
                if (checkFolder(LOAD_ABSOLUTE_PATH)) {
                    Logs.debug("System.load: "+LOAD_ABSOLUTE_PATH + "libbaratinoo.dll"+" ...");
                    System.load(LOAD_ABSOLUTE_PATH + "libbaratinoo.dll");   // the Baratinoo library
                    Logs.debug("System.load: "+LOAD_ABSOLUTE_PATH + "baratinSwig.dll"+" ...");
                    System.load(LOAD_ABSOLUTE_PATH + "baratinSwig.dll");    // the SWIG interface
                }
                else {
                    Logs.error("VoxygenTTS failed to find required dependencies (libbaratinoo.dll and baratinSwig.dll) at [" + LOAD_ABSOLUTE_PATH + "], please check the VOXYGEN_DEPENDENCIES_PATH option in Greta.ini.");
                    functional = false;
                }

            } else if (osName.contains("mac")) {

                // On Mac OS X a single lib handles both 32 and 64 bits
                ARCH_FOLDER = "MacOSX/";

                // Load dependencies
                LOAD_ABSOLUTE_PATH = IniManager.getProgramPath() + VoxygenConstants.DEPENDENCIES_PATH + ARCH_FOLDER;
                // Check if dependencies folder exist
                if (checkFolder(LOAD_ABSOLUTE_PATH)) {
                    System.load(LOAD_ABSOLUTE_PATH + "libbaratinoo.dylib");   // the Baratinoo library
                    System.load(LOAD_ABSOLUTE_PATH + "baratinSwig.dylib");    // the SWIG interface
                }
                else {
                    Logs.error("VoxygenTTS failed to find required dependencies (libbaratinoo.dylib and baratinSwig.dylib) at [" + LOAD_ABSOLUTE_PATH + "], please check the VOXYGEN_DEPENDENCIES_PATH option in Greta.ini.");
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
                LOAD_ABSOLUTE_PATH = IniManager.getProgramPath() + VoxygenConstants.DEPENDENCIES_PATH + ARCH_FOLDER;
                // Check if dependencies folder exist
                if (checkFolder(LOAD_ABSOLUTE_PATH)) {
                    System.load(LOAD_ABSOLUTE_PATH + "libbaratinoo.so");   // the Baratinoo library
                    System.load(LOAD_ABSOLUTE_PATH + "baratinSwig.so");    // the SWIG interface                }
                } else {
                    Logs.error("VoxygenTTS failed to find required dependencies (libbaratinoo.so and baratinSwig.so) at [" + LOAD_ABSOLUTE_PATH + "], please check the VOXYGEN_DEPENDENCIES_PATH option in Greta.ini.");
                    functional = false;
                }
            } else {
                Logs.error("VoxygenTTS failed to load required dependencies (libraries) for the OS [" + osName + "], Architecture[" + jvmArchitecture + "].");
                functional = false;
            }

            // Init the engine
            boolean initSuccess = initVoxygenEngine();

            if (initSuccess) {
                Logs.info("VoxygenTTS successfully initialized.");
                functional = true;
            }
            else {
                Logs.error("VoxygenTTS failed to initialize.");
                functional = false;
            }
    }

    /**
     * Init the Voxygen engine (after loading the DLL)
     * @return 
     */
    private static boolean initVoxygenEngine() {
        //C. Create the BaratinooEngine & init it
        String logPath = IniManager.getGlobals().getValueString("VOXYGEN_LOG_PATH");
        if(! logPath.isEmpty()) {
            logBaratinoo = true;
            if (".".equals(logPath))
                logDirectory = new File(IniManager.getProgramPath());
            else logDirectory = new File(IniManager.getProgramPath(),logPath);
        } else logBaratinoo = false;
        // check/create the BaratinooEngine log directory
        if (logDirectory!=null && !logDirectory.exists()) {
            try {
                logDirectory.mkdirs();
            } catch (SecurityException e) {
                Logs.warning("Can't create the Voxygen logging directory "+logDirectory+": "+e.getMessage());
            }
        }
        // Create  the BaratinooSwig
        if (logBaratinoo) {
                Logs.info("VoxygenTTS is creating Baratinoo Engine");
        	batatinooSwig = new BaratinooSwig(logDirectory.getPath(), logBaratinooFileName);
        	Logs.debug("(Baratinoo) logs are stored in "+(new File(logDirectory, logBaratinooFileName))); //TODO batatinooSwig.getLogFile()...
        } else {//TMP: no log file
                Logs.info("VoxygenTTS is creating Baratinoo Engine");
        	batatinooSwig = new BaratinooSwig(logDirectory==null? null : logDirectory.getPath(), null);
        	Logs.warning("NO (Baratinoo) log file");
        }
        // set the BaratinooEngine log level
        String logLevelStr = IniManager.getGlobals().getValueString("VOXYGEN_LOG_LEVEL");
        if (!logLevelStr.isEmpty()) {
            try {
                LogLevel logLevel = LogLevel.valueOf(logLevelStr.toUpperCase());
                batatinooSwig.setLogLevel(logLevel);
            } catch (IllegalArgumentException e) {
                Logs.warning("Unknown VOXYGEN_LOG_LEVEL value '"+logLevelStr+"' correct are ERROR, INIT, WARNING, INFO, DEBUG");
            }
        }
        
        // Check if voices folder exist
        VOICES_ABSOLUTE_PATH = IniManager.getProgramPath() + VoxygenConstants.VOICES_PATH;
        if (!checkFolder(VOICES_ABSOLUTE_PATH)) {
            Logs.error("VoxygencTTS failed to find voice folder at [" + VOICES_ABSOLUTE_PATH + "], please check if the VOXYGEN_VOICES_PATH option is correctly set in Greta.ini.");
            return false;
        }
        
        // Init the Engine
        int init = batatinooSwig.init(VOICES_ABSOLUTE_PATH+"/"+voxygenConfigFile);
        if (init!=0) {
        	Logs.error("Baratinoo Engine initialization error (code:"+init+")");
		return false;//	throw new IllegalStateException("Can't init Baratinoo engine");//TODO: exception
        }
        Logs.debug("Baratinoo Engine initialized ;-)");
        
        //D. Configure BaratinooSwig
        /*TODO
        // Load the voice
        String language = CharacterManager.getValueString("CEREPROC_LANG");
        String voice = CharacterManager.getValueString("CEREPROC_VOICE");

        boolean loadDefault = false;
        if ((language == null) || (voice == null) || (language.isEmpty()) || (voice.isEmpty())) {
            Logs.warning("CereprocTTS: missing language or voice setting in character configuration file. "
                    + "Using default voice[" + VoxygenConstants.DEFAULT_VOICE + "] and default language [" + VoxygenConstants.DEFAULT_LANGUAGE + "]");
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
	    Logs.error("CereprocTTS is unable to load the voice file '" + voicePath + "', using default voice[" + VoxygenConstants.DEFAULT_VOICE + "] and default language [" + VoxygenConstants.DEFAULT_LANGUAGE + "]");
            if (!loadDefaultVoiceLanguage())
            {
                Logs.error("CereprocTTS is unable to load the default voice file. Please check that the folder [" + VOICES_ABSOLUTE_PATH + "] exists and the subfolders (e.g. /en-gb-sarah) contain the specified voices and licenses.");
                return false;
            }
            else {
                Logs.info("CereprocTTS default voice [" + VoxygenConstants.DEFAULT_VOICE + "] with language [" + VoxygenConstants.DEFAULT_LANGUAGE + "] succesfeully loaded.");
            }
	}
        */
        
        // Init the Phonemes correspondances
        VoxygenConstants.InitPhonemes();
        
        // (!) Greta expect  RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 48000 Hz
        if (wavFrequency>0) batatinooSwig.setFrequency(wavFrequency); // set the output frequency
        if (wantedEvents!=null) {
        	batatinooSwig.unsetAllWantedEvent();
        	for (EVENT_TYPE type: wantedEvents) batatinooSwig.setWantedEvent(type);
        } // else: let as default
        
        // Init the Sampling Rate of the engine
        //cereprocSampleRate = cerevoice_eng.CPRCEN_channel_get_voice_info(engineCereproc, channelHandle, "SAMPLE_RATE");
        float baratinooSampleRateFloat = (float) batatinooSwig.getFrequency();
        
        // Creates an AudioFomat used later to play the audio buffer (synthetized voice) produced by the engine
        baratinooAudioFormat = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            baratinooSampleRateFloat, //sample rate
            16, //bits - sample size
            1, //mono - channels
            2, //bytes - frame size (sample size * channels)
            baratinooSampleRateFloat, //Hz - frame rate
            false); //endianness - true=big, false=little;
        
        // Init the fallback buffer in case of an interruption call that results in a delay
        //double minimumDuration = 0.002;
        //emptyBufferInterruptionFallback = new byte[(int)(audioFormatCereproc.getFrameSize() * cereprocSampleRateFloat * minimumDuration)];
        
        return true;
    }

    /**
     * Constructor.
     * @param characterManager reference to use
     */
    public VoxygenTTS(CharacterManager characterManager) {
        setCharacterManager(characterManager);
        characterManager.setTTS(this);
        init();
        interreuptionReactionSupported = false;
        setupCharacterLanguageVoiceParameters();
        clean(); // Clean any audio and other data in memory
        tmnumber = 0;
    }
    
    /**
     * Clean data from a previous 
     */
    private void clean() {
        audio = null;
        phonemes = new ArrayList<Phoneme>();
        speech = null;
        tmnumber = 0;
    }

    @Override
    public List<Phoneme> getPhonemes() {
        return phonemes;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }
    
    /**
     * Set new speech object to be computed. </br>
     * Also sets a new temp file path to avoid the speech duplication bug.
     * @param speech Speech object to compute
     */
    public void setSpeech(Speech speech) {
        clean();
        this.speech = speech;
    }

    
    // Relative changes in pitch and rate (used in Mathieu'sampleAudio experiment)
    public void setRateAndPitch(double rate, double pitch)
    {
        this.rate = rate;
        this.pitch = pitch;
    }
    
    
    
    public boolean isInterruptionReactionSupported() {
        return interreuptionReactionSupported;
    }

    /**
     * Computes the latest speech object. </br>
     * The three boolean should all be true: Cereproc only does all at once.
     * @param doTemporize get the audio and marker time
     * @param doAudio get the audio data
     * @param doPhonems get the `phonemes time
     */
    
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonems) {
        
        if (!functional) {
            Logs.error("VoxygenTTS: the module is not fully functional due to initialization errors. The speech that has been sent to be computed is ignored.");
            return;
        }

        if (speech == null) {
            Logs.error("VoxygenTTS: the speech variable is null.");
            return ;
        }

        //if (!doPhonems || !doTemporize || !doAudio) {  Logs.warning("VoxygenTTS: the engine only computes all at once.");  }
        if (!doPhonems && !doTemporize && !doAudio) {  Logs.warning("VoxygenTTS: the engine should computes something.");  }
    
        // Get the list of <tm> in the <speech> element
        tmOfSpeechList = speech.getTimeMarkers();
        Map<String, TimeMarker> tmOfSpeechMap = new HashMap<String, TimeMarker>();
        for (TimeMarker tm : tmOfSpeechList){
            tmOfSpeechMap.put(tm.getName(),tm);
        }
        
        // Set the initial and final time
        TimeMarker start = tmOfSpeechList.get(0);
        TimeMarker end = speech.getEnd();
        if (!start.isConcretized()) {
            start.setValue(0); // set start time to 0 if no time is specified
        }
        double startTime = start.getValue();
        end.setValue(startTime);
        
        if (doPhonems || doTemporize) {
            try {

                String ssmlSpeech = speechToSSMLString();       
                
                // DEBUG
                if (Logs.hasLevelDebug()) {
                    Logs.debug("----------------------------- VOXYGEN DEBUG -----------------------------");
                    Logs.debug("SSML.toString():");
                    Logs.debug(ssmlSpeech);
                    Logs.debug("----------------------------- VOXYGEN DEBUG -----------------------------");
                }
                
                
                
                // Get SSML bytes (endend with a '\0')
                byte[] ssmlUTF8Bytes = ssmlSpeech.getBytes("UTF-8");   // (!) toSSML generate a XML with UTF-8 encoding (header)
                byte[] ssmlWithNullBytes = new byte[ssmlUTF8Bytes.length+1];
                System.arraycopy(ssmlUTF8Bytes, 0, ssmlWithNullBytes, 0, ssmlUTF8Bytes.length);
                ssmlWithNullBytes[ssmlUTF8Bytes.length] = 0;
                // Convert SSML into Speech
                //long timeBeforeVoxygenCall = System.currentTimeMillis();
                // REGULAR TTS CALL
                //currentPlayingSpeech =
                        Speech.getCurrentPlayingScheduledSpeech(false); // just in case
                SpeechResult speechResult = batatinooSwig.textToSpeech(ssmlWithNullBytes);
                //long timeJustAfterVoxygenCall = System.currentTimeMillis();
                //System.err.println("Voxygen call take "+(timeJustAfterVoxygenCall-timeBeforeVoxygenCall)+"ms");
                speech.setGeneratedSSML(ssmlSpeech);
                                
                // Set the <tm> time (<marker> in SSML)
                double baseTime = 0.; //TODO: base of relative time
                double endTime = baseTime + speechResult.getDuration();
                for (TimeStampEvent tsEvent : speechResult.getMarkerEvents())
                {   // (!) TimeMarker take time in second, TimeStampEvent use millisecond
                    tmnumber++;
                    String tsName = tsEvent.getName();
                    double tsTime = baseTime + tsEvent.getMillisecond();
                    if (endTime<tsTime) endTime = tsTime;
                    TimeMarker tsMarker = tmOfSpeechMap.get(tsName);
                    if (tsMarker!=null) {
                        tsMarker.setValue(tsTime/1000); // TimeMarker duration in **second**
                    } else {
                        Logs.error("Any TimeMarker for SSML <marker>'s "+tsName);
                    }
                }
                // Set the end time value
                end.setValue(endTime/1000);
                 
                // Get the phonems information
                phonemes.clear();
                double lastPhoEnd = 0.;
                for (ContinuousEventIfce e : speechResult.getVisemeEvents()) {
                    // Check the gap between last phoneme
                    double phoStart = e.getMillisecond();
                    double delta = phoStart - lastPhoEnd;
                    if (delta > phonemMinPause) {
                        Phoneme p = new Phoneme(PhonemeType.pause, delta/1000);
                        phonemes.add(p);
                    }// else ignore
                    double duration = e.getDuration();
                    lastPhoEnd = phoStart + duration;
                    // Convert Voxygen viseme to VIB phonem(s) 
                    String phonemeType = e.getName();
                    PhonemeType[] pho = VoxygenConstants.convertPhoneme(languageID, phonemeType); //TODO
                    if (pho != null) {
                        for (PhonemeType pt : pho) {
                            Phoneme p = new Phoneme(pt, (duration/1000) / (pho.length));
                            phonemes.add(p);
                        }                        
                    } else {
                        Logs.error("VoxygenConstants: no phonem for "+phonemeType+" ("+languageID+")");
                        Phoneme p = new Phoneme(PhonemeType.pause, duration/1000);
                        phonemes.add(p);
                    }
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
                
                //long timeAfterVoxygenCall = System.currentTimeMillis();
                //earliestReactionTimeOffset = (timeAfterVoxygenCall - timeBeforeVoxygenCall) / 1000f;
                
                // Play the buffered result (i.e. synthetized voice) 
                if (doAudio)
                {    
                    byte[] rawAudioBuffer = speechResult.getRawData();
                                      
                    if (rawAudioBuffer.length <= 0) {
                        rawAudioBuffer = emptyBufferInterruptionFallback;
                    }
                    
                    /*DEBUG{
                        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd-HHmm");
                        String outputBase = "BaratinooSpeech-"+formatter.format(new Date());
                        writeSpeechResult(speechResult, outputBase); //TMP
                        System.err.println("Write VOXYGEN wav in "+outputBase+".wav"); 
                    }*/

                    // Create the Audio object that will be played
                    audio = new Audio(baratinooAudioFormat, rawAudioBuffer);
                }
                

            } catch (Exception e) {
                Logs.error("VoxygenTTS: " + this.getClass().getSimpleName() + " fail to generate audio. " + e.getMessage());
                e.printStackTrace();
            }
        }
 
    }
    
    protected String speechToSSMLString() throws Exception
    {
        // (a) Transform Speech to SSML with the default method
        XMLTree ssml = speech.toSSML(rate, pitch, true, "#!", "!#");    // throws Exception
        // /!\ HERE some SSML elements are in the Text's node
        //     b.e. Hello <voice emotion="happy">how are you ?</voice>
        //     --> Text node : Hello #!voice emotion="happy"!#how are you ?#!/voice!#   (replace=true)
        //      OR Text node : Hello <voice emotion="happy">how are you ?</voice>       (replace=false)
        // (b) FIX some problem
        //- set the global language
        ssml.setAttribute("xml:lang", languageID);
        
        // DEBUG
        if (Logs.hasLevelDebug()) {
            Logs.debug("----------------------------- VOXYGEN DEBUG -----------------------------");
            Logs.debug("Initial SSML:");
            Logs.debug(ssml.toString(true));
            Logs.debug("----------------------------- VOXYGEN DEBUG -----------------------------");
        }
        
        // Convert SSML to DOM (it safer that XMLTree, as mark-up can be split between various text elements)
        Document ssmlDoc = DOMTools.parseString(replaceSpecialCharactersIntoBrackets(ssml.toString(false), "#!", "!#")); // restaure < and >
        Element ssmlElmt = ssmlDoc.getDocumentElement();
        
        // (c) Manage voice
        // insert/change the <voice>
        if (!VoxygenConstants.DEFAULT_VOICE.equals(voiceName)) {
            // search a <voice> child or create one inserted between <ssml> and it's original children
            Element voice = DOMTools.findCreateNode(ssmlElmt, "voice", true);
            // Set the language name
            voice.setAttribute("name", voiceName);
        }
        
        // (d) Adjust CereProc <voice emotion="..."> --> <voice name="">
        DOMTools.processNamedNodes(ssmlElmt, "voice", new VoiceAdjust(voiceName, VoxygenConstants.CEREPROC_EMOTION_MAP, VoxygenConstants.DEFAULT_VOICE_VARIANTS), true);
        // (e) Change CereProc <spurt audio="..."> --> <audio src="">
        DOMTools.processNamedNodes(ssmlElmt, "spurt", new Spurt2Audio(VoxygenConstants.CEREPROC_SPURT_DIR), true);
        
        
        // DEBUG
        if (Logs.hasLevelDebug()) {
            Logs.debug("----------------------------- VOXYGEN DEBUG -----------------------------");
            Logs.debug("Final (DOM) SSML:");
            Logs.debug(DOMTools.toXMLString(ssmlDoc, true));
            Logs.debug("----------------------------- VOXYGEN DEBUG -----------------------------");
        }
                    
        return DOMTools.toXMLString(ssmlDoc, false);
    }
    
    
    
    
    /**
     * Function to change &lt;voice&gt; <i>emotion</i> attribute (CereProc)
     *  by corresponding <i>name</i> attribute (Voxygen).
     */
    public static class VoiceAdjust implements BiFunction<Element, List<Element>, Element> {
        static final String NAME_ATT = "name";
        static final String EMOTION_ATT = "emotion";
        static final String NAMESPACE = null;
        
        protected final String baseVoiceName;
        protected Map<String,String> emotionMap;
        protected Collection<String> voiceNames;
        
        public VoiceAdjust(String baseVoiceName, Map<String,String> emotionMap, Collection<String> voiceNames) {
            super();
            this.baseVoiceName = baseVoiceName;
            this.emotionMap = emotionMap;
            this.voiceNames = voiceNames;
        }

        @Override
        public Element apply(Element n, List<Element> p) {
            // (b) look for "emotion" attribute
            if (n.hasAttributeNS(NAMESPACE, EMOTION_ATT))
            {
                String nEmotion = n.getAttributeNS(NAMESPACE, EMOTION_ATT);
                String mapEmotion = (emotionMap!=null && emotionMap.containsKey(nEmotion))
                        ? emotionMap.get(nEmotion)
                        : nEmotion; //TODO: default (?)
                // look for the englobing voice name
                String pVoiceName = null;
                if (p!=null && !p.isEmpty()) {
                    ListIterator<Element> itP = p.listIterator(p.size());
                    while (itP.hasPrevious()) {
                        Element pi = itP.previous();
                        if (pi.hasAttributeNS(NAMESPACE, NAME_ATT)) {
                           pVoiceName = pi.getAttributeNS(NAMESPACE, NAME_ATT);
                           break;
                        }
                    }
                }
                if (pVoiceName==null) pVoiceName = baseVoiceName;   // default
                // remove part after (1st) '_'
                int iSplit = pVoiceName.indexOf('_');
                if (iSplit>0) pVoiceName = pVoiceName.substring(0, iSplit);
                String nVoiceName = mapEmotion==null || mapEmotion.isEmpty()
                                ? pVoiceName
                                : pVoiceName+"_"+mapEmotion;
                if (voiceNames!=null && !voiceNames.contains(nVoiceName)) nVoiceName = pVoiceName; // remove unknown voice
                // set "name" attribute
                n.setAttributeNS(NAMESPACE, NAME_ATT, nVoiceName);
                // remove "emotion" attribute
                n.removeAttributeNS(NAMESPACE, EMOTION_ATT);
            }
            return n;            
        }
        
    }
    
    /**
     * Function to change the (CereProc) &lt;spurt&gt; element
     *  by (Voxygen,SSML) &lt;audio&gt; element.
     */
    public static class Spurt2Audio implements BiFunction<Element, List<Element>, Element> {
        static final String SPURT_SRC_ATT = "audio";
        static final String SPURT_NAMESPACE = null;
        
        static final String AUDIO_ELEM = "audio";
        static final String AUDIO_SRC_ATT = "src";
        static final String AUDIO_NAMESPACE = null;//"http://www.w3.org/2001/10/synthesis";
        
        static final String AUDIO_EXT=".wav";
        
        protected final String spurtDir;
        
        public Spurt2Audio(String spurtDir) {
            super();
            this.spurtDir = spurtDir;
        }

        @Override
        public Element apply(Element spurt, List<Element> p) {
            // (b) look for <spurt audio="..."> attribute
            if (spurt.hasAttributeNS(SPURT_NAMESPACE, SPURT_SRC_ATT))
            {
                String spurtAudio = spurt.getAttributeNS(SPURT_NAMESPACE, SPURT_SRC_ATT);
                String audioSrc = (spurtDir!=null ? spurtDir : "") + spurtAudio + AUDIO_EXT;
                // create the <audio> node that replace spurt
                Element audio = DOMTools.replaceNode(spurt, AUDIO_ELEM, AUDIO_NAMESPACE, true);
                // set "src" attribute
                audio.setAttributeNS(AUDIO_NAMESPACE, AUDIO_SRC_ATT, audioSrc);
                
                return audio;
            }
            return spurt;
        }
    }
    
    
    /*DEBUG
    protected static ObjectMapper mapper = new ObjectMapper();
    protected static void writeSpeechResult(SpeechResult speechResult, String outBasename
			//, String logPrefix
			) {
		// - Wav output
		{	File out = new File(outBasename+".wav");
			long bytes = SpeechDataTools.writeSoundFile(speechResult, out, true);
			//if (bytes<0) logger.error("{}Can't writting the waw file {}", logPrefix, out); 
			//else logger.info("{}Output wav file {} writted, {} bytes", logPrefix, out, bytes);
		}
		// - Raw output (allow md5sum test)
		{	File out = new File(outBasename+".raw");
			long bytes = SpeechDataTools.writeSoundFile(speechResult, out, false);
			//if (bytes<0) logger.error("{}Can't writting the raw file {}", logPrefix, out); 
			//else logger.info("{}Output raw file {} writted, {} bytes", logPrefix, out, bytes);
		}
		// - list of marker timestamp
		{	File out = new File(outBasename+".markers.json");
		 	Map<String,Double> markers = SpeechDataTools.getMarkersTime(null, speechResult, null);  
			try {
				mapper.writeValue(out, markers);
			//	logger.info("{}Output ({}) markers timestamps {} writted", logPrefix, markers.size(), out);
			} catch (Exception e) {
			//	logger.error("{}Can't writting the ({}) markers timestamps {}: {}", logPrefix, markers.size(), out, e);
			}
		}
		// - list of visemes
		{
			File out = new File(outBasename+".visemes.json");
			ContinuousEventIfce[] visemes = speechResult.getVisemeEvents(); 
			try {
				mapper.writeValue(out, visemes);
			//	logger.info("{}Output ({}) visemes list {} writted", logPrefix, visemes.length, out);
			} catch (Exception e) {
			//	logger.error("{}Can't writting the ({}) visemes list {}: {}", logPrefix, visemes.length, out, e);
			}
			
		}
		
	}
    */
    
    private String replaceSpecialCharactersIntoBrackets(String ssmlSpeech, String openBracketReplacement, String closedBracketReplacement)
    {
        String toReturn = ssmlSpeech;
        toReturn = toReturn.replace(openBracketReplacement, "<");
        toReturn = toReturn.replace(closedBracketReplacement, ">");
        return toReturn;
    }

       
    private void setupCharacterLanguageVoiceParameters() {

        //TODO: Get the language and name of the 1st voice
        /*
        int nbVoices = batatinooSwig.getNumberOfVoices();
        if (nbVoices > 0) {
            BaratinooVoiceInfo voiceInfo = batatinooSwig.getVoiceInfo(0);
            languageID = voiceInfo.getLang();
            voiceName = voiceInfo,getName();
        }
        */
        //voiceName = "Fabienne_tendu";//TMP
        String newLanguage = getCharacterManager().getValueString("VOXYGEN_LANG").trim(); //TODO: get a "default" character LANG if no VOXYGEN_LANG
        String newVoiceName = getCharacterManager().getValueString("VOXYGEN_VOICE").trim();
        
        setVoiceAndLang(newVoiceName, newLanguage);
   }

    
    @Override
    public void onCharacterChanged() {
        String newLanguage = getCharacterManager().getValueString("VOXYGEN_LANG").trim(); //TODO: get a "default" character LANG if no VOXYGEN_LANG
        String newVoiceName = getCharacterManager().getValueString("VOXYGEN_VOICE").trim();
        
        setVoiceAndLang(newVoiceName, newLanguage);
    }
    
    public void setVoiceAndLang(String newVoiceName, String newLanguage) {

        if (newVoiceName.trim().isEmpty() || newLanguage.trim().isEmpty()) {
            voiceName = VoxygenConstants.DEFAULT_VOICE;
            languageID = VoxygenConstants.DEFAULT_LANGUAGE;
            Logs.info("VoxygenTTS: the character has not a voice/language set in the config file, using default voice '"+voiceName+"' and language "+languageID);
        }
        else {
            voiceName = newVoiceName.trim();
            languageID = newLanguage.trim();
            Logs.info("VoxygenTTS: the character has voice '"+voiceName+"' and language "+languageID);
        }
        //TODO : check if the voice exist and support this language
    }

    @Override
    protected void finalize() throws Throwable {
        getCharacterManager().remove(this);
        super.finalize();
    }
    
}
