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

import greta.auxiliary.player.ogre.Ogre;
import greta.auxiliary.player.ogre.OgreEnvironementListener;
import greta.auxiliary.player.ogre.OgreThread;
import greta.auxiliary.player.ogre.agent.MPEG4Agent;
import greta.core.util.Constants;
import greta.core.util.time.ManualTimeController;
import greta.core.util.time.Timer;

/**
 *
 * @author Andre-Marie Pez
 */
public class OffLineVideoCapturer implements Runnable, Capturer{

    private Capturable capturable;
    private CaptureOutput captureOutput;
    private ManualTimeController myController;
    private boolean stop;
    private boolean alreadyStarted;
    private CaptureListenerListManager captureListeners;
    private String currentId;
    
    private boolean useFixedIndex;

    public OffLineVideoCapturer(){
        this(null,null);
    }

    public OffLineVideoCapturer(Capturable capturable, CaptureOutput captureOutput){
        this.capturable = capturable;
        this.captureOutput = captureOutput;
        myController = new ManualTimeController();
        stop = false;
        alreadyStarted = false;
        captureListeners = new CaptureListenerListManager();
    }

    @Override
    public void run(){

        Timer.setTimeController(myController);
        OgreThread.Callback oldWait = OgreThread.getSingleton().getWaitCallback();
        OgreThread.getSingleton().setWaitCallback(OgreThread.NO_WAIT);
        boolean oldRealTime = Ogre.realTime;
        Ogre.realTime = false;
        synchronized (capturable){
            capturable.getCamera().getMic().stopPlaying();
            greta.core.util.audio.Mixer.requestNotBlocking();
            long startTime = Timer.getTimeMillis();
            captureListeners.notifyCaptureStarted(this, startTime);
            capturable.prepareCapture();
            captureOutput.begin(capturable.getCaptureWidth(), capturable.getCaptureHeight(), startTime, currentId);
        }
        while(!stop && !capturable.isSizeChanged()){
            synchronized (capturable){
                long currentTime = Timer.getTimeMillis();

                //capture audio
                capturable.getCamera().getMic().update();
                captureOutput.newAudioPacket(capturable.getCamera().getMic().getCurrentAudioFrame(), currentTime);

                //capture video
                for(MPEG4Agent agent : OgreEnvironementListener.agents){
                    agent.update();
                }

                Ogre.callSync(new OgreThread.Callback() {
                    @Override
                    public void run() {
                        Ogre.getRoot().renderOneFrame();
                    }
                });
//                Ogre.call(new OgreThread.Callback() {
//                    @Override
//                    public void run() {
//                        Ogre.getRoot().renderOneFrame();
//                    }
//                });
 
                captureOutput.newFrame(capturable.getCaptureData(), currentTime, useFixedIndex);
                captureListeners.notifyCaptureNewFrame(this, currentTime);
            }
            myController.stepMillis(Constants.FRAME_DURATION_MILLIS);
        }
        captureOutput.end();
        captureListeners.notifyCaptureEnded(this, Timer.getTimeMillis());
        greta.core.util.audio.Mixer.releaseNotBlocking();
        capturable.getCamera().getMic().startPlaying();
        OgreThread.getSingleton().setWaitCallback(oldWait);
        Ogre.realTime = oldRealTime;
        Timer.setTimeController(null);
        alreadyStarted = false;
        System.gc();
    }

    @Override
    public void startCapture(String id, boolean useFixedIndexLocal){
        useFixedIndex = useFixedIndexLocal;
        if(!alreadyStarted && capturable!=null && captureOutput!=null){
            alreadyStarted = true;
            stop = false;
            currentId = id;
//            captureOutput.setBaseFileName(currentId);
            Thread t = new Thread(this);
            t.start();
        }
    }

    @Override
    public void stopCapture(){
        stop = true;
    }

    private void stopAndWait(){
        stopCapture();
        while(alreadyStarted){
            try{
                Thread.sleep(1);
            }
            catch(Exception e){}
        }
    }

    @Override
    public void setCapturable(Capturable capturable) {
        stopAndWait();
        this.capturable = capturable;
    }

    @Override
    public void setCaptureOutput(CaptureOutput captureOutput) {
        stopAndWait();
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
