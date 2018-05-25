/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.player.ogre.capture;

import java.util.ArrayList;

/**
 * This class is a tool for {@code Capturers} to manage a list of {@code CaptureListener}.
 * @author Andre-Marie Pez
 */
public class CaptureListenerListManager {

    private ArrayList<CaptureListener> listeners = new ArrayList<CaptureListener>();

    /**
     * Adds a {@code CaptureListener} in the list
     * @param captureListener a {@code CaptureListener} to add.
     */
    public synchronized void add(CaptureListener captureListener) {
        if(captureListener!=null) {
            listeners.add(captureListener);
        }
    }

    /**
     * Removes a {@code CaptureListener} from the list
     * @param captureListener a {@code CaptureListener} to remove.
     */
    public synchronized void remove(CaptureListener captureListener) {
        if(captureListener!=null){
            listeners.remove(captureListener);
        }
    }

    /**
     * Notifies to all {@code CaptureListener} added that a capture is started.
     * @param source the {@code Capturer} which starts the capture.
     * @param time the VIB's time in milliseconds.
     */
    public synchronized void notifyCaptureStarted(Capturer source, long time){
        for(CaptureListener listener : listeners){
            listener.captureStarted(source, time);
        }
    }

    /**
     * Notifies to all {@code CaptureListener} added that a new frame is captured.
     * @param source the {@code Capturer} which captures the new frame.
     * @param time the VIB's time in milliseconds.
     */
    public synchronized void notifyCaptureNewFrame(Capturer source, long time){
        for(CaptureListener listener : listeners){
            listener.captureNewFrame(source, time);
        }
    }

    /**
     * Notifies to all {@code CaptureListener} added that a capture is ended.
     * @param source the {@code Capturer} which ends the capture.
     * @param time the VIB's time in milliseconds.
     */
    public synchronized void notifyCaptureEnded(Capturer source, long time){
        for(CaptureListener listener : listeners){
            listener.captureEnded(source, time);
        }
    }
}
