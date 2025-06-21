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
package greta.auxiliary.tts.marytts;

import greta.core.util.IniManager;
import greta.core.util.audio.Audio;
import greta.core.util.log.Logs;
import greta.core.util.speech.Phoneme;
import greta.core.util.speech.Speech;
import greta.core.util.speech.TTS;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import greta.core.util.CharacterManager;

/**
 * Implementation of the interface {@link greta.core.util.speech.TTS TTS} that use <a href="http://mary.dfki.de">MaryTTS</a>. <br/>
 * This class call the MaryTTS Server (version 5.x.x) to synthetise the Speech.<br/>
 * It is necessary that the MaryTTS's sever is already started when the constructor of this class is called.
 * @author Andre-Marie Pez
 */
public class MaryTTSClient implements TTS{

    // private marytts.client.MaryClient mary_5_2_0;  // MaryTTS dependency not available

    private XMLParser maryparser;
    private String voice;
    private String lang;

    private Speech speech;
    private List<Phoneme> phonemes;
    private Audio audio;
    private double timer;
    private boolean interreuptionReactionSupported = false;
    private CharacterManager cm;
    private MaryTTSConstants omc;

    /**
     * Default constructor.<br/>
     * Tries to make a connection with one of Mary server 5.x.x and determines the version of this one.<br/>
     * Values MARY_HOST and MARY_PORT must be defined in the global {@code IniManager}.
     * @param cm the characterManager associated containing the params
     */
    public MaryTTSClient(CharacterManager cm){
        startClient();
        clean();
        this.cm = cm;
        omc = new MaryTTSConstants(cm);
        maryparser = XML.createParser();
        maryparser.setValidating(false);
    }

    private boolean isSocketOpen(String host, int port, boolean throwIOException) throws UnknownHostException, IOException {
    	boolean output;
    	try (java.net.Socket test = new java.net.Socket(host, port)) {
    		output = true;
    	} catch (UnknownHostException e) {
			throw e;
		} catch (IOException e) {
			if (throwIOException) {
				throw e;
			} else {
				output = false;
			}

		}
    	return output;
    }

    private void startMaryTTS() throws IOException {
    	boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    	if (!isWindows) {
    		throw new IllegalArgumentException("We only support Windows");
    	}
    	ProcessBuilder pb = new ProcessBuilder();
    	String maryServerDirectory = IniManager.getGlobals().getValueString("MARY_SERVER_DIRECTORY");
    	if (maryServerDirectory == null || maryServerDirectory.equals("<MARY TTS SERVER DIRECTORY>")) {
    		throw new IllegalArgumentException("MARY_SERVER_DIRECTORY is required to autostart MaryTTS");
    	}
    	String maryServerFile = IniManager.getGlobals().getValueString("MARY_SERVER_FILE");
    	if (maryServerFile == null || maryServerFile.equals("<MARY TTS SERVER FILE>")) {
    		throw new IllegalArgumentException("MARY_SERVER_FILE is required to autostart MaryTTS");
    	}
    	pb.directory(new File(maryServerDirectory));
    	pb.command(new String[] {"cmd.exe", "/C", maryServerFile});
    	Process proc = pb.start();
    	Runtime.getRuntime().addShutdownHook(new Thread() {
    		@Override
    		public void run() {
    			proc.destroy();
    			try {
					proc.waitFor();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
    	});
    }

    private void startClient(){
        Thread starter = new Thread(new Runnable() {
            @Override
            public void run() {
                String host = IniManager.getGlobals().getValueString("MARY_HOST");
                int port = IniManager.getGlobals().getValueInt("MARY_PORT");
                //TODO perhaps make an HTTP client instead of using a MaryClient objects (but how to know the Mary's version?)

                System.setProperty("mary.client.quiet", "true");//to shut up mary
                // while(mary_5_2_0==null){  // MaryTTS not available
                boolean maryAvailable = false;
                while(!maryAvailable){
                    try{
                        if (!isSocketOpen(host, port, false)) {
                        	startMaryTTS();
                        }

                        //we try to find a server using the target host and port
                        //if it don't exist it's useless to try to connect to mary 4 then mary 3
                        isSocketOpen(host, port, true);

                        // mary_5_2_0 = marytts.client.MaryClient.getMaryClient(new marytts.util.http.Address(host, port));  // MaryTTS not available
                        maryAvailable = true; // Mark as available (stub implementation)
                    }
                    catch (Exception testSocketOrMaryFailException) {
                        try{
                            Thread.sleep(500);
                        }
                        catch (Exception sleepExeption) {
                        }
                    }
                }
            }
        });
        starter.start();
    }

    @Override
    public void setSpeech(Speech speech) {
        clean();
        this.speech = speech;
        lang = omc.toMaryTTSLang(speech.getLanguage());
        voice = omc.toMaryTTSVoice(speech.getLanguage());
        System.out.println(String.format("%s: %s.%s",cm.toString(),lang,voice));
    }

    @Override
    public boolean isInterruptionReactionSupported() {
        return interreuptionReactionSupported;
    }

    @Override
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonems) {
        String text = omc.toMaryXML(speech, lang);
        if(doTemporize || doPhonems){
            // if(mary_5_2_0==null){  // MaryTTS not available
            if(true){  // Always return null (MaryTTS not available)
                //try to start a Mary client
                startClient();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                // mary_5_2_0.process(  // MaryTTS not available
                System.out.println("MaryTTS not available - returning empty result"); 
                return; 
                /*
                // Original MaryTTS code (commented out):
                mary_5_2_0.process(
                    text,
                    MaryTTSConstants.IN_TYPE_MARYXML,
                    MaryTTSConstants.OUT_TYPE_PARAMS,
                    lang,
                    MaryTTSConstants.AUDIO_TYPE_WAVE,
                    voice,
                    out);
                XMLTree result = maryparser.parseBuffer(out.toString());
                timer = speech.getStart().getValue();
                extractPhonemes(result);
                */
            }
            catch (Exception ex) {Logs.error(this.getClass().getName()+" Cant receives params from MaryTTS Server.");}
        }
        if(doAudio){
            // if(mary_5_2_0==null){  // MaryTTS not available
            if(true){  // Always return null (MaryTTS not available)
                //try to start a Mary client
                startClient();
            }
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                // mary_5_2_0.process(  // MaryTTS not available
                System.out.println("MaryTTS not available - returning empty result"); 
                return;
                /*
                // Original MaryTTS code (commented out):
                mary_5_2_0.process(
                    text,
                    MaryTTSConstants.IN_TYPE_MARYXML,
                    MaryTTSConstants.OUT_TYPE_AUDIO,
                    lang,
                    MaryTTSConstants.AUDIO_TYPE_WAVE,
                    voice,
                    out);
                audio = Audio.getAudio(new ByteArrayInputStream(out.toByteArray()));
                */
            }
            catch (Exception ex) {Logs.error(this.getClass().getName()+" Cant receives audio from MaryTTS Server.");}
        }
    }

    @Override
    public List<Phoneme> getPhonemes() {
        return phonemes;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    private void clean(){
        audio = null;
        phonemes = new ArrayList<Phoneme>();
        speech = null;
        timer = 0;
    }

    private void extractPhonemes(XMLTree t){
        //read phonemes
        if(t.getName().equalsIgnoreCase("ph")){
            double duration = t.getAttributeNumber("d") / 1000.0;
            Phoneme.PhonemeType [] phos = omc.convertPhoneme(t.getAttribute("p"));
            for(Phoneme.PhonemeType pho : phos) {
                phonemes.add(new Phoneme(pho,duration/((double)phos.length)));
            }
            //TODO add sressingpoint ? (but it's only with MaryTTS) - see maryinterface.cpp line 1138 to 1146
            timer += duration;
        }

        //read boundaries
        if(t.getName().equalsIgnoreCase("boundary") && !t.getAttribute("duration").isEmpty()){
            double duration = t.getAttributeNumber("duration")/1000.0;
            phonemes.add(new Phoneme(Phoneme.PhonemeType.pause,duration));
            timer += duration;
        }

        //read time markers
        if(t.getName().equalsIgnoreCase("mark")) {
            speech.getTimeMarker(t.getAttribute("name")).setValue(timer);
        }

        //same thing on children
        for(XMLTree child : t.getChildrenElement()) {
            extractPhonemes(child);
        }
    }
}
