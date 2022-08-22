/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.ircamalign;

import greta.core.util.CharacterManager;
import greta.core.util.audio.Audio;
import greta.core.util.speech.Phoneme;
import greta.core.util.speech.Speech;
import greta.core.util.speech.TTS;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Michele
 */
public class IrcamAlignTTS implements TTS {

    private CharacterManager cm;
    private String xmlfile;
    private Audio audio;

    private String textfile="ircamAlign_input_textfile.txt";
    private String speechfile;
    private XMLParser fmlparser = XML.createParser();
    private boolean interreuptionReactionSupported = false;
    private Speech speech;
    private List<Phoneme> phonemes;
    private String languageID="en-GB";
    static{
        // Init constants and make phonemes mappings
        IrcamConstants.init();
    }
    


    public String getTextfile() {
        return textfile;
    }

    public void setTextfile(String textfile) {
        this.textfile = textfile;
    }

    public String getSpeechfile() {
        return speechfile;
    }

    public void setSpeechfile(String speechfile) {
        this.speechfile = speechfile;
    }

    public CharacterManager getCm() {
        return cm;
    }

    public void setCm(CharacterManager cm) {
        this.cm = cm;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    private boolean enabled = false;

    public IrcamAlignTTS(CharacterManager cm) {
        this.cm = cm;
    }

    public void processTextAndAudio() throws IOException {
        
        System.out.println("[INFO]: IRCAM->:"+enabled);
        if (enabled) {
            System.out.println("IrcamALIGN TEST" + "  "+System.getProperty("user.dir"));
            
            String[] cmd = {"wsl", "/mnt/c/Users/Michele/Desktop/greta-gpl-2/greta-gpl/bin/ircamAlign/ircamAlign","-t",textfile.replace("C:\\","mnt/"),"./"+speechfile, "-r"};
            Runtime rt = Runtime.getRuntime();
            Process pb = rt.exec(cmd);
            //ProcessBuilder pb = new ProcessBuilder("wsl");
            //pb.redirectErrorStream(true);
            //Process p = pb.start();
            OutputStreamWriter osw = new OutputStreamWriter(pb.getOutputStream());
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(pb.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(pb.getErrorStream()));
            System.out.println(System.getProperty("user.dir")+"//ircamAlign//");
            osw.write("cd ircamAlign");
            osw.flush();
            String command = "./ircamAlign -t " + textfile + " " + speechfile+" -r";
            osw.write(command);
            osw.flush();
            // Read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s = null;
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                    }
                    
                    // Read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                        System.out.println(s);
                    
                }
            /*
            try {
                    String[] cmd = {
                        "python",
                        System.getProperty("user.dir")+"\\Scripts\\opensmile_greta.py",
                    };
                    Runtime rt = Runtime.getRuntime();
                    Process proc = rt.exec(cmd);
                    
                    BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(proc.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new
                InputStreamReader(proc.getErrorStream()));
                    
                    // Read the output from the command
                    System.out.println("Here is the standard output of the command:\n");
                    String s = null;
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                    }
                    
                    // Read any errors from the attempted command
                    System.out.println("Here is the standard error of the command (if any):\n");
                    while ((s = stdError.readLine()) != null) {
                        System.out.println(s);
                    }       } catch (IOException ex) {
                    Logger.getLogger(Audio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
            */
            }
    }

    public void speech_from_xml(String fmlFileName) throws IOException {
        String base = (new File(fmlFileName)).getName().replaceAll("\\.xml$", "");
        String fml_id = "";
        boolean text_brut = false;
        //get the intentions of the FML file
        fmlparser.setValidating(true);
        BufferedReader reader;
        String text = "";
        boolean flag = false;
        try {
            File myObj = new File(fmlFileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                //System.out.println(data);
                if (!data.contains("<?xml") && !(data.contains("<tm"))) {
                    System.out.println(data);
                    text += data;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        FileWriter fw = new FileWriter(textfile);
        fw.write(text);
	fw.close();
    }




    public List<Phoneme> getPhonemes() {
        return phonemes;
    }
    
    
    public Audio getAudio() {
        return audio;
    }
    
    private void clean(){
        audio = null;
        phonemes = new ArrayList<Phoneme>();
        speech = null;
    }

    @Override
    public void setSpeech(Speech speech) {
        clean();
        this.speech = speech;
    }

    @Override
    public void compute(boolean bln, boolean bln1, boolean bln2) {
        

            try {                
                processTextAndAudio();
                IrcamConstants.InitPhonemes();
                BufferedInputStream in = null;
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    in = new BufferedInputStream(new FileInputStream(speechfile));
                    int read;
                    byte[] buff = new byte[1024];
                    while ((read = in.read(buff)) > 0)
                    {
                        out.write(buff, 0, read);
                    }
                    out.flush();
                    // Create the Audio object that will be played
                    audio = Audio.getAudio(new ByteArrayInputStream(out.toByteArray()));
                     extract_phonemes();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(IrcamAlignTTS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(IrcamAlignTTS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(IrcamAlignTTS.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        Logger.getLogger(IrcamAlignTTS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (IOException ex) {
            Logger.getLogger(IrcamAlignTTS.class.getName()).log(Level.SEVERE, null, ex);
        }
        
            
        
            

            
    }

    @Override
    public boolean isInterruptionReactionSupported() {
        return interreuptionReactionSupported;
    }
    
    public void extract_phonemes(){
        
        /*
            String phonemeType ="";
            phonemeType= parts[2];
            double duration;
            if (pho != null) {
                for (int i = 0; i < pho.length; i++) {
                    Phoneme.PhonemeType pt = pho[i];
                    Phoneme p = new Phoneme(pt, duration / (pho.length));
                    phonemes.add(p);
               }
            }
        */
        Double duration;
        BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(
					"./ircamAlign/test_ircamAlign/test.labSemiphonLIA"));
			String line = reader.readLine();
			while (line != null) {
				
                                String parts[]=line.split(" ");
                                duration = Double.valueOf(parts[1])/10000000-Double.valueOf(parts[0])/10000000;
                                System.out.println("LINE: "+line+"  "+parts[2]+"  "+parts[1]);
                                Phoneme.PhonemeType[] pho= IrcamConstants.convertPhoneme(languageID, parts[2]);
                                System.out.println("PHO LENGTH:"+pho.length);
                                if (pho != null) {
                                    for (int i = 0; i < pho.length; i++) {
                                        Phoneme.PhonemeType pt = pho[i];
                                        Phoneme p = new Phoneme(pt, duration / (pho.length));
                                        System.out.println("PHONEMES:"+pt+"   "+duration/(pho.length));
                                        phonemes.add(p);
                                    }
                                }
                                
				// read next line
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}      
    

}

