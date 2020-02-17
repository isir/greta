/*
 * This file is part of Greta.
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
package greta.core.keyframes.face;

import greta.core.keyframes.CancelableKeyframePerformer;
import greta.core.keyframes.Keyframe;
import greta.core.repositories.AUAP;
import greta.core.repositories.AUAPFrame;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.math.Functions;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Radoslaw Niewiadomski
 * @author Ken Prepin
 * @author Andre-Marie Pez
 * @author Brice Donval
 * @author Nawhal Sayarh
 */
public class FaceKeyframePerformer implements CancelableKeyframePerformer, AUEmitter {
    private CharacterManager characterManager;
    private boolean interpolate;

    private int frameDelay = 5;
    private int framesPerLaps = 1;//Constants.FRAME_PER_SECOND;

    private static int RESET_DURATION_IN_FRAME_NUM = (int)(0.4 * Constants.FRAME_PER_SECOND);

    private int timeLapsMillis = framesPerLaps * Constants.FRAME_DURATION_MILLIS;
    private Thread blinker = null;

    private final ArrayList<AUPerformer> auPerformers = new ArrayList<>();

    private final AUAPFrameInterpolator interpolator;

    public FaceKeyframePerformer (CharacterManager cm) {
        this.characterManager = cm;
        interpolator = new AUAPFrameInterpolator();
        startInterpolation();
    }

    private void startInterpolation () {
        interpolate = true;
        Thread t = new Thread(() -> {
            while(interpolate){
                long begin = Timer.getTimeMillis();
                int timeFrame = (int)(begin/Constants.FRAME_DURATION_MILLIS) + frameDelay;
                if(!interpolator.isEmptyAt(timeFrame)){
                    Map<ID, List<AUAPFrame>> auapFramesWithId = interpolator.getAUAPFramesWithIdAt(timeFrame, timeFrame+framesPerLaps);
                    for (Map.Entry<ID, List<AUAPFrame>> idWithCorrespondingFrames : auapFramesWithId.entrySet()) {
                        ID framesID = idWithCorrespondingFrames.getKey();
                        if (framesID == null) {
                            framesID = IDProvider.createID("AU Interpolation");
                        }
                        sendAUs(idWithCorrespondingFrames.getValue(), framesID);
                    }
                    Timer.sleep(timeLapsMillis+begin-Timer.getTimeMillis());
                } else {
                    Timer.sleep(100);
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void setBlinking (boolean blink) {
        if (blink) {
            if(blinker == null){
                startBlinking();
            }
        } else {
            blinker = null;
        }
    }

    public boolean isBlinking () {
        return blinker != null;
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

    private void blink () {
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

    private void stopInterpolate () {
        interpolate = false;
    }

    @Override
    protected void finalize () throws Throwable {
        stopInterpolate();
        super.finalize();
    }

    @Override
    public void performKeyframes (List<Keyframe> keyframes, ID requestId) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId, new Mode(CompositionType.replace));
    }

    @Override
    public void performKeyframes (List<Keyframe> keyframes, ID requestId, Mode mode) {
        //get AUKeyFrame and set the good time from relative to the current time
        LinkedList<AUAPFrame> received = new LinkedList<>();
        for(Keyframe keyframe : keyframes){
            if(keyframe instanceof AUKeyFrame){
                AUAPFrame auFrame = ((AUKeyFrame)keyframe).getAus();
                auFrame.setFrameNumber((int)(keyframe.getOffset() * Constants.FRAME_PER_SECOND));
                received.add(auFrame);
            }
        }
        updateAUs(received, requestId, mode);
    }

    public void sendAUs (List<AUAPFrame> auAnimation, ID requestId) {
        for (AUPerformer auPerformer : auPerformers) {
            auPerformer.performAUAPFrames(auAnimation, requestId);
        }
    }

    public void updateAUs (LinkedList<AUAPFrame> auAnimation, ID requestId, Mode mode) {
        switch (mode.getCompositionType()) {
            case append:
            case blend:
                interpolator.blendSegment(auAnimation, requestId);
                break;

            case replace:
                int currentTime = Timer.getCurrentFrameNumber();
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
                interpolator.blendSegment(auAnimation, requestId);
                break;

            default:
                System.err.println("[FaceKeyframePerformer] updateAUs() : mode is wrong");
                break;
        }
    }

    @Override
    public void cancelKeyframesById (ID requestId) {
        interpolator.cleanKeysWithId(requestId);
        for (AUPerformer auPerformer : auPerformers) {
            if (auPerformer instanceof CancelableAUPerformer) {
                ((CancelableAUPerformer) auPerformer).cancelAUKeyFramesById(requestId);
            }
        }
    }

    @Override
    public void addAUPerformer (AUPerformer auPerformer) {
        auPerformers.add(auPerformer);
    }

    @Override
    public void removeAUPerformer (AUPerformer auPerformer) {
        auPerformers.remove(auPerformer);
    }
}
