/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */

package vib.auxiliary.player.ogre.capture;

/**
 *
 * @author Andre-Marie Pez
 */
public interface CaptureListener {

    /**
     * Called when a capture is started by a {@code Capturer}.
     * @param source the {@code Capturer} which starts the capture.
     * @param time the VIB's time in milliseconds.
     */
    public void captureStarted(Capturer source, long time);

    /**
     * Called when a new frame is captured by a {@code Capturer}.
     * @param source the {@code Capturer} which captures the new frame.
     * @param time the VIB's time in milliseconds.
     */
    public void captureNewFrame(Capturer source, long time);

    /**
     * Called when a capture is ended by a {@code Capturer}.
     * @param source the {@code Capturer} which ends the capture.
     * @param time the VIB's time in milliseconds.
     */
    public void captureEnded(Capturer source, long time);
}
