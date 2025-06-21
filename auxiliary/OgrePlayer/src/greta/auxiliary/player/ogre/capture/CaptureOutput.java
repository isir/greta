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
package greta.auxiliary.player.ogre.capture;

/**
 * This interface describes an object that can use the result of the capture.
 * @author Andre-Marie Pez
 */
public interface CaptureOutput {

    /**
     * Notifies that the capture starts.<br/>
     * It specifies the size of the image during the whole capture, and the begin time.
     * @param w the width of the image in pixel
     * @param h the height of the image in pixel
     * @param beginTime the Greta's time in milliseconds.
     */
    public void begin(int w, int h, long beginTime, String id);

    /**
     * Adds a new frame.<br/>
     * It is supposed that the pixel are in 3 bytes RGB.
     * @param data the pixel data of the frame.
     * @param time the Greta's time in milliseconds of the frame.
     */
//    public void newFrame(byte[] data, long time);

    /**
     * Adds a new frame.<br/>
     * It is supposed that the pixel are in 3 bytes RGB.
     * @param data the pixel data of the frame.
     * @param time the Greta's time in milliseconds of the frame.
     * @param useFixedIndex the flag to fix output frame index.
     */
    public void newFrame(byte[] data, long time, boolean useFixedIndex);
    
    
    /**
     * Adds a new audio packet.<br/>
     * It is supposed that the audio is in Greta's audio format.
     * @param data the audio data of the frame.
     * @param time the Greta's time in milliseconds of the frame.
     * @see greta.core.util.audio.Audio#GRETA_AUDIO_FORMAT Greta's audio format
     */
    public void newAudioPacket(byte[] data, long time);

    /**
     * Notifies that the capture ends.
     */
    public void end();
    
    /**
     * Sets base file name of saved image file.<br/>
     * @param fileName
     */    
    public void setBaseFileName(String fileName);
    
}
