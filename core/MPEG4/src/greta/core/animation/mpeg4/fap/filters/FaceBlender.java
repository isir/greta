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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
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

    private void startBlending(){
        blender.start();
    }

    @Override
    public synchronized void performFAPFrames(List<FAPFrame> fap_animation, ID requestId) {
        for(FAPFrame frame : fap_animation) {
            face1Animation.add(new FAPIDPair(requestId,frame));
        }
    }

    @Override
    public synchronized void performFAPFrame(FAPFrame fap_anim, ID requestId) {
        face1Animation.add(new FAPIDPair(requestId,fap_anim));
    }

    @Override
    public void cancelFAPFramesById(ID requestId) {
        face1Animation.removeIf(fapIdPair -> fapIdPair.animId == requestId);
        cancelFramesWithIDInLinkedPerformers(requestId);
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

    public void setFace2Source(FAPFrameEmitter face2Source){
        face2Source.addFAPFramePerformer(face2Receiver);
    }

    public void dettachFace2Source(FAPFrameEmitter face2Source){
        face2Source.removeFAPFramePerformer(face2Receiver);
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

    private class Blender extends Thread {
        boolean end = false;

        @Override
        public void run() {
            while(!end){
                
                synchronized (this) {

                    popLatestFrame();
                    computeAndSend();
                    try { sleep(10); } catch (Throwable t) {}
                    
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        blender.end = false;
        super.finalize();
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
