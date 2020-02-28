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

import java.util.concurrent.ArrayBlockingQueue;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author Andre-Marie Pez
 */
public class Line implements AudioOutput{

    public static final int AUDIO_BUFFER_QUEUE_SIZE = 1;

    private SourceDataLine sdl;
    private ArrayBlockingQueue<byte[]> audioBufferQueue;
    private Thread lineUpdater;

    public Line() {
        try {
            sdl = AudioSystem.getSourceDataLine(Audio.GRETA_AUDIO_FORMAT);
            sdl.open(Audio.GRETA_AUDIO_FORMAT, Mixer.BUFFER_SIZE);
            audioBufferQueue = new ArrayBlockingQueue<byte[]>(AUDIO_BUFFER_QUEUE_SIZE);
            lineUpdater = new Thread() {
                @Override
                public void run() {
                    sdl.start();
                    while (true) {
                        try {
                            sdl.write(audioBufferQueue.take(), 0, Mixer.BUFFER_SIZE);
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                }
            };
            lineUpdater.setDaemon(true);
            lineUpdater.start();
        } catch (LineUnavailableException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex){
            ex.printStackTrace();
        }
    }
    @Override
    public void setCurrentAudio(byte[] current) {
        try {
            if (Mixer.blocking) {
               this.audioBufferQueue.put(current);
            }
            else {
                boolean res = this.audioBufferQueue.offer(current);
                if (!res) {
                    this.audioBufferQueue.poll();
                    this.audioBufferQueue.offer(current);
                }
            }
        }
        catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        lineUpdater.interrupt();
        sdl.stop();
        sdl.close();
        audioBufferQueue.clear();
        super.finalize();
    }

}
