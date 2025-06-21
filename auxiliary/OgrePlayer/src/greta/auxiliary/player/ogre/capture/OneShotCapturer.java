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

import greta.core.util.time.Timer;

/**
 *
 * @author Andre-Marie Pez
 */
public class OneShotCapturer implements Capturer{

    private Capturable capturable;
    private CaptureOutput captureOutput;
    private CaptureListenerListManager captureListeners;
    
    private boolean useFixedIndex;

    public OneShotCapturer(){
        this(null,null);
    }

    public OneShotCapturer(Capturable capturable, CaptureOutput captureOutput){
        this.capturable = capturable;
        this.captureOutput = captureOutput;
        captureListeners = new CaptureListenerListManager();
    }

    @Override
    public synchronized void startCapture(String id, boolean useFixedIndexLocal){
        useFixedIndex = useFixedIndexLocal;
        if(capturable!=null && captureOutput!=null){
            long time = Timer.getTimeMillis();

            captureListeners.notifyCaptureStarted(this, time);
            capturable.prepareCapture();
            captureOutput.begin(capturable.getCaptureWidth(), capturable.getCaptureHeight(), time, id);

            captureOutput.newFrame(capturable.getCaptureData(), time, useFixedIndex);
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
