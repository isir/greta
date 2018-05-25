/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.capture;

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
     * @param beginTime the VIB's time in milliseconds.
     */
    public void begin(int w, int h, long beginTime, String id);

    /**
     * Adds a new frame.<br/>
     * It is supposed that the pixel are in 3 bytes RGB.
     * @param data the pixel data of the frame.
     * @param time the VIB's time in milliseconds of the frame.
     */
    public void newFrame(byte[] data, long time);

    /**
     * Adds a new audio packet.<br/>
     * It is supposed that the audio is in VIB's audio format.
     * @param data the audio data of the frame.
     * @param time the VIB's time in milliseconds of the frame.
     * @see vib.core.util.audio.Audio#VIB_AUDIO_FORMAT VIB's audio format
     */
    public void newAudioPacket(byte[] data, long time);

    /**
     * Notifies that the capture ends.
     */
    public void end();

}
