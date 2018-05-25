/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.capture;

import vib.core.util.time.Timer;


/**
 *
 * @author Andre-Marie Pez
 */
public class OneShotCapturer implements Capturer{

    private Capturable capturable;
    private CaptureOutput captureOutput;
    private CaptureListenerListManager captureListeners;

    public OneShotCapturer(){
        this(null,null);
    }

    public OneShotCapturer(Capturable capturable, CaptureOutput captureOutput){
        this.capturable = capturable;
        this.captureOutput = captureOutput;
        captureListeners = new CaptureListenerListManager();
    }

    @Override
    public synchronized void startCapture(String id){
        if(capturable!=null && captureOutput!=null){
            long time = Timer.getTimeMillis();

            captureListeners.notifyCaptureStarted(this, time);
            capturable.prepareCapture();
            captureOutput.begin(capturable.getCaptureWidth(), capturable.getCaptureHeight(), time, id);

            captureOutput.newFrame(capturable.getCaptureData(), time);
            captureListeners.notifyCaptureNewFrame(this, time);

            captureOutput.end();
            captureListeners.notifyCaptureEnded(this, time);
        }
    }

    @Override
    public void stopCapture(){}

    @Override
    public synchronized void setCapturable(Capturable capturable) {
        this.capturable = capturable;
    }

    @Override
    public synchronized void setCaptureOutput(CaptureOutput captureOutput) {
        this.captureOutput = captureOutput;
    }

    @Override
    public void addCaptureListener(CaptureListener captureListener) {
        captureListeners.add(captureListener);
    }

    @Override
    public void removeCaptureListener(CaptureListener captureListener) {
        captureListeners.remove(captureListener);
    }
}
