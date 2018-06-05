/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes.face;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import vib.core.keyframes.Keyframe;
import vib.core.keyframes.KeyframePerformer;
import vib.core.repositories.AUAP;
import vib.core.repositories.AUAPFrame;
import vib.core.util.Constants;
import vib.core.util.Mode;
import vib.core.util.enums.CompositionType;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.math.Functions;
import vib.core.util.time.Timer;

/**
 *
 * @author Radoslaw Niewiadomski
 * @author Ken Prepin
 * @author Andre-Marie Pez
 * @author Brice Donval
 */
public class FaceKeyframePerformer implements KeyframePerformer, AUEmitter {

    private boolean interpolate;

    private int frameDelay = 5;
    private int framesPerLaps = 1;//Constants.FRAME_PER_SECOND;

    private static int RESET_DURATION_IN_FRAME_NUM = (int)(0.4 * Constants.FRAME_PER_SECOND);

    private int timeLapsMillis = framesPerLaps * Constants.FRAME_DURATION_MILLIS;
    private Thread blinker = null;

    private final ArrayList<AUPerformer> auPerformers = new ArrayList<AUPerformer>();

    private final AUAPFrameInterpolator interpolator;

    public FaceKeyframePerformer(){
        interpolator = new AUAPFrameInterpolator();
        startInterpolation();
    }

    private void startInterpolation(){
        interpolate = true;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(interpolate){
                    long begin = Timer.getTimeMillis();
                    int timeFrame = (int)(begin/Constants.FRAME_DURATION_MILLIS) + frameDelay;
                    if( ! interpolator.isEmptyAt(timeFrame)){
                        interpolator.cleanOldFrames();
                        List<AUAPFrame> interpolated = new ArrayList<AUAPFrame>(framesPerLaps);
                        for(int i = 0; i<framesPerLaps; ++i){
                            int frameNum = timeFrame + i;
                            interpolated.add(interpolator.getAUAPFrameAt(frameNum));
                        }
                        //TODO : create a correct ID from requests received
                        sendAUs(interpolated,IDProvider.createID("AU Interpolation"));
                        Timer.sleep(timeLapsMillis+begin-Timer.getTimeMillis());
                    } else {
                        Timer.sleep(100);
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void setBlinking(boolean blink){
        if(blink){
            if(blinker == null){
                startBlinking();
            }
        }
        else{
            blinker = null;
        }
    }

    public boolean isBlinking(){
        return blinker!=null;
    }

    private void startBlinking() {
        blinker = new Thread() {
            @Override
            public void run() {
                while (interpolate && this.equals(blinker)) {
                    Timer.sleep((long)Functions.changeInterval(Math.random(), 0, 1, 2500, 5000));
                    blink();
                }
            }
        };
        blinker.setDaemon(true);
        blinker.start();
    }

    private void blink() {
        int currentFrameTime = (int) (Timer.getTime() * Constants.FRAME_PER_SECOND);
        AUAPFrame start = new AUAPFrame(currentFrameTime + Constants.FRAME_PER_SECOND);//delay of 1 second
        AUAPFrame down1 = new AUAPFrame(currentFrameTime + Constants.FRAME_PER_SECOND + 3);
        AUAPFrame down2 = new AUAPFrame(currentFrameTime + Constants.FRAME_PER_SECOND + 4);
        AUAPFrame end = new AUAPFrame(currentFrameTime + Constants.FRAME_PER_SECOND + 7);
        start.setAUAPboth(43, 0);
        down1.setAUAPboth(43, 1);
        down2.setAUAPboth(43, 1);
        end.setAUAPboth(43, 0);
        interpolator.blendSegment(start, down1, down2, end);
    }

    private void stopInterpolate(){
        interpolate = false;
    }

    @Override
    protected void finalize() throws Throwable {
        stopInterpolate();
        super.finalize();
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId, new Mode(CompositionType.replace));
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        //get AUKeyFrame and set the good time from relative to the current time
        LinkedList<AUAPFrame> received = new LinkedList<AUAPFrame>();
        for(Keyframe keyframe : keyframes){
            if(keyframe instanceof AUKeyFrame){
                AUAPFrame auFrame = ((AUKeyFrame)keyframe).getAus();
                auFrame.setFrameNumber((int)(keyframe.getOffset() * Constants.FRAME_PER_SECOND));
                received.add(auFrame);
            }
        }
        updateAUs(received, requestId, mode);
    }

    public void sendAUs(List<AUAPFrame> auAnimation, ID requestId) {
        for (AUPerformer auPerformer : auPerformers) {
            auPerformer.performAUAPFrames(auAnimation, requestId);
        }
    }

    public void updateAUs(LinkedList<AUAPFrame> auAnimation, ID requestId, Mode mode) {

        int currentTime = Timer.getCurrentFrameNumber();

        switch (mode.getCompositionType()) {

            case append: {
                interpolator.blendSegment(auAnimation);
                break;
            }

            case blend: {
                interpolator.blendSegment(auAnimation);
                break;
            }

            case replace: {

                LinkedList<AUAPFrame> returnToNeutral = new LinkedList<>();
                AUAPFrame currentFrame = interpolator.getAUAPFrameAt(currentTime + frameDelay);
                AUAPFrame neutralFrame = new AUAPFrame(currentTime + RESET_DURATION_IN_FRAME_NUM);
                List<AUAP> currentAUs = currentFrame.getAUAPList();
                List<AUAP> neutralAUs = neutralFrame.getAUAPList();
                for (int i = 0; i < currentAUs.size(); ++i) {
                    if (currentAUs.get(i).getMask()){
                        neutralAUs.get(i).applyValue(0);
                    }
                }
                returnToNeutral.add(currentFrame);
                returnToNeutral.add(neutralFrame);

                interpolator.clear();

                interpolator.blendSegment(returnToNeutral);
                interpolator.blendSegment(auAnimation);
                break;
            }

            default: {
                System.out.println("[FaceKeyframePerformer] updateAUs() : mode is wrong");
                break;
            }
        }
    }

    @Override
    public void addAUPerformer(AUPerformer auPerformer) {
        auPerformers.add(auPerformer);
    }

    @Override
    public void removeAUPerformer(AUPerformer auPerformer) {
        auPerformers.remove(auPerformer);
    }
}
