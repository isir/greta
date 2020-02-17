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
     * @param time the Greta's time in milliseconds.
     */
    public synchronized void notifyCaptureStarted(Capturer source, long time){
        for(CaptureListener listener : listeners){
            listener.captureStarted(source, time);
        }
    }

    /**
     * Notifies to all {@code CaptureListener} added that a new frame is captured.
     * @param source the {@code Capturer} which captures the new frame.
     * @param time the Greta's time in milliseconds.
     */
    public synchronized void notifyCaptureNewFrame(Capturer source, long time){
        for(CaptureListener listener : listeners){
            listener.captureNewFrame(source, time);
        }
    }

    /**
     * Notifies to all {@code CaptureListener} added that a capture is ended.
     * @param source the {@code Capturer} which ends the capture.
     * @param time the Greta's time in milliseconds.
     */
    public synchronized void notifyCaptureEnded(Capturer source, long time){
        for(CaptureListener listener : listeners){
            listener.captureEnded(source, time);
        }
    }
}
