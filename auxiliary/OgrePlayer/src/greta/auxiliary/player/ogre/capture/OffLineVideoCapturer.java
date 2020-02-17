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
                captureOutput.newFrame(capturable.getCaptureData(), currentTime);
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
    public void startCapture(String id){
        if(!alreadyStarted && capturable!=null && captureOutput!=null){
            alreadyStarted = true;
            stop = false;
            currentId = id;
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
