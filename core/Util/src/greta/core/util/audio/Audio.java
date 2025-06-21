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
package greta.core.util.audio;  
import greta.core.util.log.Logs;
import greta.core.util.math.Functions;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Andre-Marie Pez
 */
public class Audio {

    /**
     * Format used by the Greta's system
     * 
     */
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final AudioFormat GRETA_AUDIO_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            16000, //Hz - sample rate
            16, //bits - sample size
            2, //stereo - channels
            4, //bytes - frame size (sample size * channels)
            16000, //Hz - frame rate
            false); //endianness - true=big, false=little

    public static double normalize(short pcm) {
        //reverseBytes because endianness is false
        return Functions.changeInterval(Short.reverseBytes(pcm), Short.MIN_VALUE + 1, Short.MAX_VALUE, -1, 1);
    }

    public static short unnomalize(double nomalized) {
        //reverseBytes because endianness is false
        //warp in [-1, 1] because short can not be outside of [Short.MIN_VALUE, Short.MAX_VALUE]
        return Short.reverseBytes((short) Functions.changeInterval(Math.max(-1, Math.min(1, nomalized)), -1, 1, Short.MIN_VALUE + 1, Short.MAX_VALUE));
    }

    public static Comparator<Audio> audioComparator = new Comparator<Audio>() {
        @Override
        public int compare(Audio o1, Audio o2) {
            return (int) (o1.time - o2.time);
        }
    };

    public static Audio getAudio(InputStream inputStream) throws IOException, UnsupportedAudioFileException{
        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(inputStream));
        AudioFormat audioformat = ais.getFormat();
        byte[] audiobuffer = new byte[(int)(ais.getFrameLength() * audioformat.getFrameSize())];
        new DataInputStream(ais).readFully(audiobuffer);
        return new Audio(audioformat, audiobuffer);
    }

    public static Audio getAudio(File file) throws IOException, UnsupportedAudioFileException{
        return getAudio(new FileInputStream(file));
    }

    public static Audio getAudio(String fileName) throws IOException, UnsupportedAudioFileException{
        return getAudio(new FileInputStream(fileName));
    }

    public static Audio getEmptyAudio(){
        return new Audio(Audio.GRETA_AUDIO_FORMAT, new byte[Audio.GRETA_AUDIO_FORMAT.getFrameSize()], 0);
    }


    private AudioFormat format;
    private byte[] audioBuffer;
    private long time;
    private double baseVolume;
    private long estimatedEnd;
    private int playingBufferPos;
    public final static int PLAYING_BUFFER_POSITION_SCHEDULED = -1;
    public final static int PLAYING_BUFFER_POSITION_FINISHED = -2;

    public Audio(AudioFormat format, byte[] audioBuffer) {
        this(format, audioBuffer, 1, 0);
    }

    public Audio(AudioFormat format, byte[] audioBuffer, double baseVolume) {
        this(format, audioBuffer,  baseVolume, 0);
    }

    public Audio(AudioFormat format, byte[] audioBuffer, double baseVolume, long time) {
        this.audioBuffer = audioBuffer;
        this.format = format;
        this.time = time;
        this.baseVolume = baseVolume;
        estimateEnd();
        this.playingBufferPos = PLAYING_BUFFER_POSITION_SCHEDULED;
    }
    public Audio(Audio other){
        this.audioBuffer = other.audioBuffer;
        this.format = other.format;
        this.time = other.time;
        this.baseVolume = other.baseVolume;
        this.estimatedEnd = other.estimatedEnd;
        this.playingBufferPos = other.playingBufferPos;
    }

    public synchronized void setPlayingBufferPos(int pos) {
        playingBufferPos = pos;
    }

    public synchronized void addToPlayingBufferPos(int toAdd) {
        playingBufferPos+= toAdd;
    }

    public synchronized int getPlayingBufferPos() {
        return playingBufferPos;
    }

    public AudioFormat getFormat() {
        return format;
    }

    public byte[] getBuffer() {
        return audioBuffer;
    }

    public long getTimeMillis() {
        return time;
    }

    public void setTimeMillis(long millis) {
        this.time = millis;
        estimateEnd();
    }

    public void setTime(double seconds) {
        setTimeMillis((long) (seconds * 1000));
    }

    public double getBaseVolume() {
        return baseVolume;
    }
    /**
     *
     * @param baseVolume Multiplier: >1 increases volume, <1 decreases volume
     */
    public void setBaseVolume(double baseVolume) {
        this.baseVolume = baseVolume;
    }

    public long getEndMillis() {
        return estimatedEnd;
    }

    private void estimateEnd() {
        estimatedEnd = time + getDurationMillis();
    }

    public long getDurationMillis(){
        return (long) (audioBuffer.length / ((format.getFrameRate() / 1000.0) * format.getFrameSize()));
    }

    public double getDuration(){
        return getDurationMillis()/1000.0;
    }

    protected void setEndMillis(long millis) {
        this.estimatedEnd = millis;
    }

    /**
     * Saves this {@code Audio} in a specific {@code OutputStream}.
     * @param outputStream the target output.
     */
    public void save(OutputStream outputStream){
        save(outputStream, null);
    }
    /**
     * Saves this {@code Audio} in a specific {@code OutputStream}.
     * @param outputStream the target output.
     * @param targetFormat the wanted audio format.
     */
    public void save(OutputStream outputStream, AudioFormat targetFormat){
        if(format == null || audioBuffer == null){
            Logs.error(this.getClass().getName()+" : can not write the audio : the audio buffer or audio format is null.");
            return ;
        }
        

        ByteArrayInputStream bais = new ByteArrayInputStream(audioBuffer);
        AudioInputStream ais = new AudioInputStream(bais, format, audioBuffer.length / format.getFrameSize());

        if(targetFormat!=null && !targetFormat.matches(format)){
            ais = AudioSystem.getAudioInputStream(targetFormat, ais);
        }
        try{
            if(ais.getFrameLength() == AudioSystem.NOT_SPECIFIED){
                ais = weMustKnowTheLengthToSave(ais);
            }
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputStream);
            outputStream.close();
        }
        catch(IOException ex){
            Logs.error(this.getClass().getName()+" : "+ex.getMessage());
        }
    }
    /**
     * Saves this {@code Audio} in a specific file.
     * @param file the target file.
     */
    public void save(File file){
        save(file, null);
    }

    /**
     * Saves this {@code Audio} in a specific file.
     * @param file the target file.
     * @param targetFormat the wanted audio format.
     */
    public void save(File file, AudioFormat targetFormat){
        try {
            save(new FileOutputStream(file), targetFormat);
        } catch (FileNotFoundException ex) {
            Logs.error(this.getClass().getName()+" : "+ex.getMessage());
        }
    }
    /**
     * Saves this {@code Audio} in a specific file.
     * @param fileName the name of the target file.
     */
    public void save(String fileName,boolean asap) throws IOException{
        save(fileName, null,asap);
    }

    /**
     * Saves this {@code Audio} in a specific file.
     * @param fileName the name of the target file.
     * @param targetFormat the wanted audio format.
     */
    public void save(String fileName, AudioFormat targetFormat, boolean asap) throws IOException{
        save(new File(fileName), targetFormat);
        save(new File(fileName.replace("output","output1")), targetFormat);
        System.out.println("greta.core.util.audio.Audio.save() "+fileName+"  "+asap+ "  "+ System.getProperty("user.dir")+"\\"+fileName);
        
        if(asap){
        //put opensmile code there 
        Thread thread = new Thread(){
            public void run(){
                
            boolean python=true;
            String result="";
            try{ 
                String[] cmd = {
                        "python -c \"import opensmile\"",
                    };
                    Runtime rt = Runtime.getRuntime();
                try {
                    Process proc = rt.exec(cmd);
                    result = new String(proc.getInputStream().toString());
                    if(result.contains("not available") || result.contains("introuvable") || result.contains("no module"))
                        python=false;
                    
                } catch (IOException ex) {
                    //Logger.getLogger(ChatGPTFrame.class.getName()).log(Level.SEVERE, null, ex);
                    python=false;
                }
                }
                catch(Exception e)
                {
                e.printStackTrace(); 
            }
            
            if(python==false){
                System.out.println(ANSI_YELLOW+"[INFO]This warning appears because it seems that you enabled the ASAP module which is optional. "
                        + "Python and/or opensmile seem to be not installed. You need to install them as well as have the Python ASAP network running in order to use the ASAP module!"+ANSI_RESET);
                
            }
            
            if(python){
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
  };

  thread.start();
 

        // Test with Jython
        //Jython works with Python2 not compatible with opensmile
        //Properties props = new Properties();
        //props.put("python.home",System.getProperty("user.dir")+"\\Common\\Lib\\External\\Jython\\Lib");
        //props.put("python.console.encoding", "UTF-8");
        //props.put("python.security.respectJavaAccessibility", "false");
        //props.put("python.import.site", "false");
        //Properties preprops = System.getProperties();
        //PythonInterpreter.initialize(preprops, props, new String[0]);
        //String[] arguments = {System.getProperty("user.dir")+"\\Scripts\\opensmile_greta.py", System.getProperty("user.dir")+"\\"+fileName,"-S"};
        //PythonInterpreter python = new PythonInterpreter();
        //StringWriter out = new StringWriter();
        //python.setOut(out);
        //python.execfile(System.getProperty("user.dir")+"\\Scripts\\opensmile_greta.py");
        //String outputStr = out.toString();
        //System.out.println(outputStr);
        }
}
        
      

    private AudioInputStream weMustKnowTheLengthToSave(AudioInputStream weWantToKnow) throws IOException{
        long skiped = 0;
        long length = 0;
        weWantToKnow.mark(Integer.MAX_VALUE);
        do {
            skiped = weWantToKnow.skip(1024);
            length += skiped;
        } while (skiped == 1024);
        byte[] audioBufferCopy = new byte[(int) length];
        weWantToKnow.reset();
        weWantToKnow.read(audioBufferCopy);
        ByteArrayInputStream bais = new ByteArrayInputStream(audioBufferCopy);
        return new AudioInputStream(bais,weWantToKnow.getFormat(),length/weWantToKnow.getFormat().getFrameSize());
    }
}
