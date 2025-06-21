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
