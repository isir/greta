/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.capture;

/**
 *
 * @author Andre-Marie Pez
 */
public interface Capturer{

    public void startCapture(String id);

    public void stopCapture();

    public void setCapturable(Capturable capturable);

    public void setCaptureOutput(CaptureOutput captureOutput);

    public void addCaptureListener(CaptureListener captureListener);

    public void removeCaptureListener(CaptureListener captureListener);

}
