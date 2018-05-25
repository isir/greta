/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.capture;

import vib.auxiliary.player.ogre.Camera;

/**
 * This interface describes an object that can be captured.
 * @author Andre-Marie Pez
 */
public interface Capturable {

    public Camera getCamera();

    /**
     * Call when the capture begin.<br/>
     * Must compute the width and the height of the catured image.
     */
    public void prepareCapture();

    /**
     * Returns the width of the image.<br/>
     * It must be a multiple of 4
     * @return the width of the image.
     */
    public int getCaptureWidth();

    /**
     * Returns the height of the image.<br/>
     * It must be a multiple of 4
     * @return the height of the image.
     */
    public int getCaptureHeight();

    /**
     * Checks if the width or the height have changed since the last call of {@code prepareCapture()}.
     * @return {@code true} if the width or the height have changed. {@code false} otherwise.
     */
    public boolean isSizeChanged();

    /**
     * Returns the pixel data of the current image.<br/>
     * It is supposed that the pixel are in 3 bytes RGB.
     * @return the pixel data of the current image.
     */
    public byte[] getCaptureData();
}
