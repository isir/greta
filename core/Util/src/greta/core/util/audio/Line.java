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
