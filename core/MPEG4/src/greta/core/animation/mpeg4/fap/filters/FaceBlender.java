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
package greta.core.animation.mpeg4.fap.filters;

import greta.core.animation.mpeg4.fap.CancelableFAPFramePerformer;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitter;
import greta.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.time.Timer;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 * @author Takeshi Saga (bug fix, frame flickering problem)
 */
public class FaceBlender extends FAPFrameEmitterImpl implements CancelableFAPFramePerformer {

    public int blendingDelay = 0;
    private CharacterManager characterManager;

    /*private static Comparator<FAPFrame> fapComp = new Comparator<>(){
        @Override
        public int compare(FAPFrame o1, FAPFrame o2) {
            return o2.getFrameNumber()-o1.getFrameNumber();
        }
    };*/ // TODO FIXME fapComp is never used

    private List<FAPIDPair> face1Animation;
    private List<FAPIDPair> face2Animation;

    /**
     * this enclosed {@code FAPFramePerformer} is used to collect FAPs from the lips.
     */
    private FAPFramePerformer face2Receiver;
    private Blender blender;

    public FaceBlender (CharacterManager cm) {
        this.characterManager = cm;
        face1Animation = new LinkedList<>();
        face2Animation = new LinkedList<>();
        face2Receiver = new FAPFramePerformer(){
            @Override
            public void performFAPFrames(List<FAPFrame> fap_animation, ID requestId) {
                synchronized(FaceBlender.this){
                    for(FAPFrame frame : fap_animation) {
                        face2Animation.add(new FAPIDPair(requestId,frame));
                    }
                }
            }
            @Override
            public void performFAPFrame(FAPFrame fap_anim, ID requestId) {
                synchronized(FaceBlender.this){
                    face2Animation.add(new FAPIDPair(requestId,fap_anim));
                }
            }
        };
        blender = new Blender();
        blender.setDaemon(true);
        startBlending();
    }

    private class Blender extends Thread {
        boolean end = false;

        @Override
        public void run() {
            while(!end){
                
                    //
                    //synchronized (this) {
                    //    computeAndSend();
                    //    popLatestFrame();
                    //    try { sleep(10); } catch (Throwable t) {}                    
                    //}
                    //
                    ////////////////
                    // Why this (computeAndSend, popLatestFrame) caused lip/face flickering?
                    ////////////////
                    // Because this loop alows registration of new frames to face1Animation only at the transition between computeAndSend() and popLatestFrameInList(),
                    // those functions block the registration while running.
                    // In fact, this caused hugely delayed frame execution.
                    //
                    
                    frameByFrame();

                    try { sleep(10); } catch (Throwable t) {}
                    
//                }
            }
        }
    }    
    
    private void startBlending(){
        blender.start();
    }

    @Override
    public void performFAPFrames(List<FAPFrame> fap_animation, ID requestId) {
        synchronized (FaceBlender.this) {
            for(FAPFrame frame : fap_animation) {
                face1Animation.add(new FAPIDPair(requestId,frame));
            }            
        }
    }

    @Override
    public void performFAPFrame(FAPFrame fap_anim, ID requestId) {
        synchronized (FaceBlender.this) {
            face1Animation.add(new FAPIDPair(requestId,fap_anim));
        }
    }

    @Override
    public void cancelFAPFramesById(ID requestId) {
        face1Animation.removeIf(fapIdPair -> fapIdPair.animId == requestId);
        cancelFramesWithIDInLinkedPerformers(requestId);
    }

    @Override
    protected void finalize() throws Throwable {
        blender.end = false;
        super.finalize();
    }
    
    private void computeAndSend(){
        Map<ID, List<FAPFrame>> blendedFapFramesWithId = new HashMap<>();
        int face1AnimationIndex = 0;
        int face2AnimationIndex = 0;
        synchronized (this) {
            while (face1AnimationIndex < face1Animation.size() && face2AnimationIndex < face2Animation.size()) {
                FAPIDPair face1AnimationPair = face1Animation.get(face1AnimationIndex);
                int face1FrameNb = face1AnimationPair.frame.getFrameNumber();
                FAPIDPair face2AnimationPair = face2Animation.get(face2AnimationIndex);
                int face2FrameNb = face2AnimationPair.frame.getFrameNumber();
                if (face1FrameNb < face2FrameNb){
                    ++face1AnimationIndex;
                } else if (face1FrameNb > face2FrameNb){
                    ++face2AnimationIndex;
                } else {
                    // We choose to consider face1 movement more important than lip movement.
                    // If IDs are equal, the common id is used, otherwise the face1 id is used : the face1 id is always used.
                    if (!blendedFapFramesWithId.containsKey(face1AnimationPair.animId)) {
                        blendedFapFramesWithId.put(face1AnimationPair.animId, new ArrayList<>());
                    }
                    blendedFapFramesWithId.get(face1AnimationPair.animId).add(blend(face1AnimationPair.frame, face2AnimationPair.frame));
                    face1Animation.remove(face1AnimationIndex);
                    face2Animation.remove(face2AnimationIndex);
                }
            }
        }
        for (Map.Entry<ID, List<FAPFrame>> idWithCorrespondingFrames : blendedFapFramesWithId.entrySet()) {
            sendFAPFrames(idWithCorrespondingFrames.getKey(), idWithCorrespondingFrames.getValue());
        }
    }

    private void popLatestFrame(){
        popLatestFrameInList(face1Animation);
        popLatestFrameInList(face2Animation);
    }

    private void popLatestFrameInList(List<FAPIDPair> animation) {
        synchronized (FaceBlender.this) {
            FAPIDPair firstFapIdPair = animation.isEmpty() ? null : animation.get(0);
            while(firstFapIdPair != null && firstFapIdPair.frame.getFrameNumber()<Timer.getTime()*Constants.FRAME_PER_SECOND+blendingDelay){
                sendFAPFrame(firstFapIdPair.animId, firstFapIdPair.frame);
                animation.remove(0);
                firstFapIdPair = animation.isEmpty() ? null : animation.get(0);
            }
        }
    }

    private void frameByFrame(){
        
        FAPIDPair face1AnimationPair;
        FAPIDPair face2AnimationPair;

        // Not work
//        int face1FrameNb;
//        int face2FrameNb;

        // Work
        int face1FrameNb = 0;
        int face2FrameNb = 0;

        
        while (!face1Animation.isEmpty() || !face2Animation.isEmpty()) {
            synchronized (this) {
 
                if (!face1Animation.isEmpty() && !face2Animation.isEmpty()) {
                    
                    face1AnimationPair = face1Animation.get(0);
                    face2AnimationPair = face2Animation.get(0);
                    face1FrameNb = face1AnimationPair.frame.getFrameNumber();
                    face2FrameNb = face2AnimationPair.frame.getFrameNumber();
                    
                    if (face1FrameNb < face2FrameNb) {

                        sendFAPFrame(face1AnimationPair.animId, face1AnimationPair.frame);
                        face1Animation.remove(0);

                    } else if (face1FrameNb == face2FrameNb) {

                        sendFAPFrame(face1AnimationPair.animId, blend(face1AnimationPair.frame, face2AnimationPair.frame));
                        face1Animation.remove(0);
                        face2Animation.remove(0);

                    } else if (face1FrameNb > face2FrameNb) {

                        sendFAPFrame(face2AnimationPair.animId, face2AnimationPair.frame);
                        face2Animation.remove(0);

                    } else {

                        System.out.println("[LipBlender] blend frame index match: something wrong...");

                    }
 
                } else if (!face1Animation.isEmpty()) {
                    
                    face1AnimationPair = face1Animation.get(0);
                    sendFAPFrame(face1AnimationPair.animId, face1AnimationPair.frame);
                    face1Animation.remove(0);
 
                } else if (!face2Animation.isEmpty()) {
 
                    face2AnimationPair = face2Animation.get(0);
                    sendFAPFrame(face2AnimationPair.animId, face2AnimationPair.frame);
                    face2Animation.remove(0);                    
 
                }
                
//                FAPIDPair faceAnimationPair = faceAnimation.get(faceAnimationIndex);
//                int face1FrameNb = faceAnimationPair.frame.getFrameNumber();
//                FAPIDPair lipAnimationPair = lipAnimation.get(lipAnimationIndex);
//                int face2FrameNb = lipAnimationPair.frame.getFrameNumber();
//                if (face1FrameNb < face2FrameNb){
//                    ++faceAnimationIndex;
//                } else if (face1FrameNb > face2FrameNb){
//                    ++lipAnimationIndex;
//                } else {
//                    // We choose to consider face movement more important than lip movement.
//                    // If IDs are equal, the common id is used, otherwise the face id is used : the face id is always used.
//                    if (!blendedFapFramesWithId.containsKey(faceAnimationPair.animId)) {
//                        blendedFapFramesWithId.put(faceAnimationPair.animId, new ArrayList<>());
//                    }
//                    blendedFapFramesWithId.get(faceAnimationPair.animId).add(blend(faceAnimationPair.frame, lipAnimationPair.frame));
//                    faceAnimation.remove(faceAnimationIndex);
//                    lipAnimation.remove(lipAnimationIndex);
//                }
 
            }
        }
//        for (Map.Entry<ID, List<FAPFrame>> idWithCorrespondingFrames : blendedFapFramesWithId.entrySet()) {
//            sendFAPFrames(idWithCorrespondingFrames.getKey(), idWithCorrespondingFrames.getValue());
//        }
    }

    private FAPFrame blend(FAPFrame face1, FAPFrame face2){
        FAPFrame blended = new FAPFrame(face1);
        

        // TODO: should implement other facial parameters as well
        // need to add the following indexes (starting from 0): 14-15, 18-22, 39-40, 43-50, 64-68
        
        int tmpStartIndex;
        int tmpEndIndex;
        int tmpIndex;

        // jaw
        tmpStartIndex = 14;
        tmpEndIndex = 15;
        tmpIndex = tmpStartIndex;
        while (tmpIndex<=tmpEndIndex) {
            // blendValueFrom(3, blended, face2);
            blendValueFrom(tmpIndex, blended, face2);
            tmpIndex++;
        }

//    depress_chin(Side.BOTH),//18
//    close_t_l_eyelid(Side.LEFT),
//    close_t_r_eyelid(Side.RIGHT), //20
//    close_b_l_eyelid(Side.LEFT),
//    close_b_r_eyelid(Side.RIGHT),//22
        tmpStartIndex = 18;
        tmpEndIndex = 22;
        tmpIndex = tmpStartIndex;
        while (tmpIndex<=tmpEndIndex) {
            // blendValueFrom(3, blended, face2);
            blendValueFrom(tmpIndex, blended, face2);
            tmpIndex++;
        }
        
        // eyeball: 23-38
        tmpStartIndex = 23;
        tmpEndIndex = 38;
        tmpIndex = tmpStartIndex;
        while (tmpIndex<=tmpEndIndex) {
            // blendValueFrom(3, blended, face2);
            blendValueFrom(tmpIndex, blended, face2);
            tmpIndex++;
        }

//    puff_l_cheek(Side.LEFT),//39
//    puff_r_cheek(Side.RIGHT), //40
        tmpStartIndex = 39;
        tmpEndIndex = 40;
        tmpIndex = tmpStartIndex;
        while (tmpIndex<=tmpEndIndex) {
            // blendValueFrom(3, blended, face2);
            blendValueFrom(tmpIndex, blended, face2);
            tmpIndex++;
        }

//    shift_tongue_tip(Side.BOTH),//43
//    raise_tongue_tip(Side.BOTH),
//    thrust_tongue_tip(Side.BOTH),
//    raise_tongue(Side.BOTH),
//    tongue_roll(Side.BOTH),
        tmpStartIndex = 43;
        tmpEndIndex = 47;
        tmpIndex = tmpStartIndex;
        while (tmpIndex<=tmpEndIndex) {
            // blendValueFrom(3, blended, face2);
            blendValueFrom(tmpIndex, blended, face2);
            tmpIndex++;
        }

//    head_pitch(Side.BOTH),//48
//    head_yaw(Side.BOTH),
//    head_roll(Side.BOTH), //50
        tmpStartIndex = 48;
        tmpEndIndex = 50;
        tmpIndex = tmpStartIndex;
        while (tmpIndex<=tmpEndIndex) {
            // blendValueFrom(3, blended, face2);
            blendValueFrom(tmpIndex, blended, face2);
            tmpIndex++;
        }

//    bend_nose(Side.BOTH),//64
//    raise_l_ear(Side.LEFT),//65
//    raise_r_ear(Side.RIGHT),
//    pull_l_ear(Side.LEFT),
//    pull_r_ear(Side.RIGHT); //68        
        tmpStartIndex = 64;
        tmpEndIndex = 68;
        tmpIndex = tmpStartIndex;
        while (tmpIndex<=tmpEndIndex) {
            // blendValueFrom(3, blended, face2);
            blendValueFrom(tmpIndex, blended, face2);
            tmpIndex++;
        }
        
        return blended;
    }
    
    private void addValueFrom(int fapIndex, FAPFrame target, FAPFrame source){
//        target.applyValue(fapIndex, target.getValue(fapIndex) + source.getValue(fapIndex));  
        
        target.applyValue(fapIndex, ( 7/6 * target.getValue(fapIndex) + source.getValue(fapIndex))/2);
        System.out.println(" IT WORKED aaaaaaaa!!dfsfsfd!!!!!!!!!");
    }

    private void blendValueFrom(int fapIndex, FAPFrame target, FAPFrame source) {
        // double blendCoef = ((double) source.getValue(FAPType.viseme)) / 1000; //BLEND COEF VALUE IS 0.5
        double blendCoef = 0.5; //BLEND COEF VALUE IS 0.5
        target.applyValue(fapIndex, (int) (target.getValue(fapIndex)*(1-blendCoef) + source.getValue(fapIndex)*(blendCoef)));
        //target.applyValue(fapIndex, (int) ( ( 7/6 * target.getValue(fapIndex)+ source.getValue(fapIndex))/2));
        
    }

    public void setFace2Source(FAPFrameEmitter face2Source){
        face2Source.addFAPFramePerformer(face2Receiver);
    }

    public void dettachFace2Source(FAPFrameEmitter face2Source){
        face2Source.removeFAPFramePerformer(face2Receiver);
    }

    private class FAPIDPair {
        ID animId;
        FAPFrame frame;
        FAPIDPair (ID animId, FAPFrame frame) {
            this.animId = animId;
            this.frame = frame;
        }
    }

}
