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
    public void newFrame(byte[] data, long time);

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

}
