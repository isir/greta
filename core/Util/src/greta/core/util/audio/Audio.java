/*
 * This file is part of Greta.
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
package greta.core.util.audio;

import greta.core.util.log.Logs;
import greta.core.util.math.Functions;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
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
     */
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
    public void save(String fileName){
        save(fileName, null);
    }

    /**
     * Saves this {@code Audio} in a specific file.
     * @param fileName the name of the target file.
     * @param targetFormat the wanted audio format.
     */
    public void save(String fileName, AudioFormat targetFormat){
        save(new File(fileName), targetFormat);
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
