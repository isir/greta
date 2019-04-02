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

package vib.auxiliary.tts.openmary;

import vib.core.util.IniManager;
import vib.core.util.audio.Audio;
import vib.core.util.log.Logs;
import vib.core.util.speech.Phoneme;
import vib.core.util.speech.Speech;
import vib.core.util.speech.TTS;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import vib.core.util.CharacterManager;


/**
 * Implementation of the interface {@link vib.core.util.speech.TTS TTS} that use <a href="http://mary.dfki.de">Open Mary</a>. <br/>
 * This class call the Open Mary server (version 3.x.x or 4.x.x) to synthetise the Speech.<br/>
 * It is necessary that the Open Mary's sever is already started when the constructor of this class is called.
 * @author Andre-Marie Pez
 */
public class OpenMaryClientTTS implements TTS{


    private marytts.client.MaryClient mary_4_3_0;
    private de.dfki.lt.mary.client.MaryClient mary_3_6_0;
    private int maryVersion;

    private XMLParser maryparser;
    private String voice;
    private String lang;

    private Speech speech;
    private List<Phoneme> phonemes;
    private Audio audio;
    private double timer;
    private boolean interreuptionReactionSupported = false;
    private CharacterManager cm;
    private OpenMaryConstants omc;

    /**
     * Default constructor.<br/>
     * Tries to make a connection with one of Mary server 3.x.x or 4.x.x and determines the version of this one.<br/>
     * Values MARY_HOST and MARY_PORT must be defined in the global {@code IniManager}.
     * @param cm the characterManager associated containing the params
     */
    public OpenMaryClientTTS(CharacterManager cm){
        startClient();
        clean();
        this.cm = cm;
        omc = new OpenMaryConstants(cm);
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
    
    private void startOpenMary() throws IOException {
    	boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    	if (!isWindows) {
    		throw new IllegalArgumentException("We only support Windows");
    	}
    	ProcessBuilder pb = new ProcessBuilder();
    	String maryServerDirectory = IniManager.getGlobals().getValueString("MARY_SERVER_DIRECTORY");
    	if (maryServerDirectory == null) {
    		throw new IllegalArgumentException("MARY_SERVER_DIRECTORY is required to autostart OpenMary");
    	}
    	String maryServerFile = IniManager.getGlobals().getValueString("MARY_SERVER_FILE");
    	if (maryServerFile == null) {
    		throw new IllegalArgumentException("MARY_SERVER_FILE is required to autostart OpenMary");
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
        maryVersion=0;
        Thread starter = new Thread(new Runnable() {
            @Override
            public void run() {
                String host = IniManager.getGlobals().getValueString("MARY_HOST");
                int port = IniManager.getGlobals().getValueInt("MARY_PORT");
                //TODO perhaps make an HTTP client instead of using a MaryClient objects (but how to know the Mary's version?)

                System.setProperty("mary.client.quiet", "true");//to shut up mary
                while(maryVersion==0){
                    try{
                        if (!isSocketOpen(host, port, false)) {
                        	startOpenMary();
                        }
                        
                        //we try to find a server using the target host and port
                        //if it don't exist it's useless to try to connect to mary 4 then mary 3
                        isSocketOpen(host, port, true);

                        //first, we try to connect to mary 4.3
                        try {
                            mary_4_3_0 = marytts.client.MaryClient.getMaryClient(new marytts.client.http.Address(host, port));
                            maryVersion=OpenMaryConstants.MARY_4;
                        }
                        catch (Exception mary4FailExeption) {
                            //if we fail to connect to mary 4.3, we try mary 3.6
                            mary_3_6_0 = new de.dfki.lt.mary.client.MaryClient(host, port);
                            maryVersion=OpenMaryConstants.MARY_3;
                        }
                    }
                    catch (Exception testSocketOrMary3FailException) {
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
        lang = omc.toMaryLang(speech.getLanguage(),maryVersion);
        voice = omc.toMaryVoice(speech.getLanguage(), maryVersion);
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
            if(maryVersion==0){
                //try to start a Mary client
                startClient();
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                //case Mary 4.3
                if(maryVersion==OpenMaryConstants.MARY_4) {
                    mary_4_3_0.process(
                        text,
                        OpenMaryConstants.IN_TYPE_MARYXML,
                        OpenMaryConstants.OUT_TYPE_PARAMS,
                        lang,
                        OpenMaryConstants.AUDIO_TYPE_WAVE,
                        voice,
                        out);
                }
                else
                    //case Mary 3.6
                    if(maryVersion==OpenMaryConstants.MARY_3) {
                    mary_3_6_0.process(
                        text,
                        OpenMaryConstants.IN_TYPE_MARYXML,
                        OpenMaryConstants.OUT_TYPE_PARAMS,
                        OpenMaryConstants.AUDIO_TYPE_WAVE,
                        voice,
                        out);
                }
                XMLTree result = maryparser.parseBuffer(out.toString());
                timer = speech.getStart().getValue();
                extractPhonemes(result);
            }
            catch (Exception ex) {Logs.error(this.getClass().getName()+" Cant receives params from Open Mary server.");}
        }
        if(doAudio){
            if(maryVersion==0){
                //try to start a Mary client
                startClient();
            }
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                //case Mary 4.3
                if(maryVersion==OpenMaryConstants.MARY_4) {
                    mary_4_3_0.process(
                        text,
                        OpenMaryConstants.IN_TYPE_MARYXML,
                        OpenMaryConstants.OUT_TYPE_AUDIO,
                        lang,
                        OpenMaryConstants.AUDIO_TYPE_WAVE,
                        voice,
                        out);
                }
                else
                    //case Mary 3.6
                    if(maryVersion==OpenMaryConstants.MARY_3) {
                    mary_3_6_0.process(
                        text,
                        OpenMaryConstants.IN_TYPE_MARYXML,
                        OpenMaryConstants.OUT_TYPE_AUDIO,
                        OpenMaryConstants.AUDIO_TYPE_WAVE,
                        voice,
                        out);
                }
                audio = Audio.getAudio(new ByteArrayInputStream(out.toByteArray()));
            }
            catch (Exception ex) {Logs.error(this.getClass().getName()+" Cant receives audio from Open Mary server.");}
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
            //TODO add sressingpoint ? (but it's only with openMary) - see maryinterface.cpp line 1138 to 1146
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
