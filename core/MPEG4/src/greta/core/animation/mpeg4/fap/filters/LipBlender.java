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
public class LipBlender extends FAPFrameEmitterImpl implements CancelableFAPFramePerformer {

    public int blendingDelay = 0;
    private CharacterManager characterManager;

    /*private static Comparator<FAPFrame> fapComp = new Comparator<>(){
        @Override
        public int compare(FAPFrame o1, FAPFrame o2) {
            return o2.getFrameNumber()-o1.getFrameNumber();
        }
    };*/ // TODO FIXME fapComp is never used

    private List<FAPIDPair> faceAnimation;
    private List<FAPIDPair> lipAnimation;

    /**
     * this enclosed {@code FAPFramePerformer} is used to collect FAPs from the lips.
     */
    private FAPFramePerformer lipReceiver;
    private Blender blender;
    
    private int frameNumberProbe = 0;

    public LipBlender (CharacterManager cm) {
        this.characterManager = cm;
        faceAnimation = new LinkedList<>();
        lipAnimation = new LinkedList<>();
        lipReceiver = new FAPFramePerformer(){
            @Override
            public void performFAPFrames(List<FAPFrame> fap_animation, ID requestId) {
                synchronized(LipBlender.this){
                    for(FAPFrame frame : fap_animation) {
                        lipAnimation.add(new FAPIDPair(requestId,frame));
                    }
                }
            }
            @Override
            public void performFAPFrame(FAPFrame fap_anim, ID requestId) {
                synchronized(LipBlender.this){
                    lipAnimation.add(new FAPIDPair(requestId,fap_anim));
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
                    //}
                    //
                    // CHECK Blender() in FaceBlender for tech note for this modification
                    //


                    frameByFrame();

                    try { sleep(10); } catch (Throwable t) {}
                    
            }
        }
    }
    
    private void startBlending(){
        blender.start();
    }

    @Override
    public void performFAPFrames(List<FAPFrame> fap_animation, ID requestId) {
        synchronized (LipBlender.this) {
            for(FAPFrame frame : fap_animation) {
                faceAnimation.add(new FAPIDPair(requestId,frame));
            }
        }
    }

    @Override
    public void performFAPFrame(FAPFrame fap_anim, ID requestId) {
        synchronized (LipBlender.this) {
            faceAnimation.add(new FAPIDPair(requestId,fap_anim));        
        }
    }

    @Override
    public void cancelFAPFramesById(ID requestId) {
        faceAnimation.removeIf(fapIdPair -> fapIdPair.animId == requestId);
        cancelFramesWithIDInLinkedPerformers(requestId);
    }

    @Override
    protected void finalize() throws Throwable {
        blender.end = false;
        super.finalize();
    }
    
    private void computeAndSend(){
        Map<ID, List<FAPFrame>> blendedFapFramesWithId = new HashMap<>();
        int faceAnimationIndex = 0;
        int lipAnimationIndex = 0;
        synchronized (this) {
            while (faceAnimationIndex < faceAnimation.size() && lipAnimationIndex < lipAnimation.size()) {
                FAPIDPair faceAnimationPair = faceAnimation.get(faceAnimationIndex);
                int faceFrameNb = faceAnimationPair.frame.getFrameNumber();
                FAPIDPair lipAnimationPair = lipAnimation.get(lipAnimationIndex);
                int lipFrameNb = lipAnimationPair.frame.getFrameNumber();
                if (faceFrameNb < lipFrameNb){
                    ++faceAnimationIndex;
                } else if (faceFrameNb > lipFrameNb){
                    ++lipAnimationIndex;
                } else {
                    // We choose to consider face movement more important than lip movement.
                    // If IDs are equal, the common id is used, otherwise the face id is used : the face id is always used.
                    if (!blendedFapFramesWithId.containsKey(faceAnimationPair.animId)) {
                        blendedFapFramesWithId.put(faceAnimationPair.animId, new ArrayList<>());
                    }
                    blendedFapFramesWithId.get(faceAnimationPair.animId).add(blend(faceAnimationPair.frame, lipAnimationPair.frame));
                    faceAnimation.remove(faceAnimationIndex);
                    lipAnimation.remove(lipAnimationIndex);
                }
            }
        }
        for (Map.Entry<ID, List<FAPFrame>> idWithCorrespondingFrames : blendedFapFramesWithId.entrySet()) {
            sendFAPFrames(idWithCorrespondingFrames.getKey(), idWithCorrespondingFrames.getValue());
        }
    }

    private void popLatestFrame(){
        popLatestFrameInList(faceAnimation);
        popLatestFrameInList(lipAnimation);
    }

    private void popLatestFrameInList(List<FAPIDPair> animation) {
        synchronized (LipBlender.this) {
            FAPIDPair firstFapIdPair = animation.isEmpty() ? null : animation.get(0);
            while(firstFapIdPair != null && firstFapIdPair.frame.getFrameNumber()<Timer.getTime()*Constants.FRAME_PER_SECOND+blendingDelay){
                sendFAPFrame(firstFapIdPair.animId, firstFapIdPair.frame);
                animation.remove(0);
                firstFapIdPair = animation.isEmpty() ? null : animation.get(0);
            }
        }
    }

    private void frameByFrame(){
        FAPIDPair faceAnimationPair;
        FAPIDPair lipAnimationPair;

        // Not work
        // int faceFrameNb;
        //int lipFrameNb;

        // Work
        int faceFrameNb = 0;
        int lipFrameNb = 0;

        long duration;
        while (!faceAnimation.isEmpty() || !lipAnimation.isEmpty()) {
            synchronized (LipBlender.this) {

                duration = Timer.getTimeMillis();
 
                if (!faceAnimation.isEmpty() && !lipAnimation.isEmpty()) {
                    
                    faceAnimationPair = faceAnimation.get(0);
                    lipAnimationPair = lipAnimation.get(0);
                    faceFrameNb = faceAnimationPair.frame.getFrameNumber();
                    lipFrameNb = lipAnimationPair.frame.getFrameNumber();

                    if (faceFrameNb < lipFrameNb) {
                        
//                        if (faceFrameNb < Timer.getTime()*Constants.FRAME_PER_SECOND) {
                            sendFAPFrame(faceAnimationPair.animId, faceAnimationPair.frame);                        
//                        }
                        faceAnimation.remove(0);

                    } else if (faceFrameNb == lipFrameNb) {
                        
//                        if (faceFrameNb < Timer.getTime()*Constants.FRAME_PER_SECOND) {
                            sendFAPFrame(faceAnimationPair.animId, blend(faceAnimationPair.frame, lipAnimationPair.frame));                        
//                        }
                        faceAnimation.remove(0);
                        lipAnimation.remove(0);

                    } else if (faceFrameNb > lipFrameNb) {
                        
//                        if (lipFrameNb < Timer.getTime()*Constants.FRAME_PER_SECOND) {
                            sendFAPFrame(lipAnimationPair.animId, lipAnimationPair.frame);
//                        }
                        lipAnimation.remove(0);

                    } else {

                        System.out.println("[LipBlender] blend frame index match: something wrong...");

                    }
 
                } else if (!faceAnimation.isEmpty()) {
                    
                    faceAnimationPair = faceAnimation.get(0);
//                    if (faceAnimationPair.frame.getFrameNumber() < Timer.getTime()*Constants.FRAME_PER_SECOND) {
                        sendFAPFrame(faceAnimationPair.animId, faceAnimationPair.frame);
//                    }
                    faceAnimation.remove(0);                        
 
                } else if (!lipAnimation.isEmpty()) {
 
                    lipAnimationPair = lipAnimation.get(0);
//                    if (lipAnimationPair.frame.getFrameNumber() < Timer.getTime()*Constants.FRAME_PER_SECOND) {
                        sendFAPFrame(lipAnimationPair.animId, lipAnimationPair.frame);
//                    }
                    lipAnimation.remove(0);
 
                }
                
                duration = Timer.getTimeMillis() - duration;
                //System.out.println("[LipBlender] duration - " + duration + "/ faceFrameNb - " + faceFrameNb + " / lipFrameNb - " + lipFrameNb);
                
//                try { sleep((int) 1 / Constants.FRAME_PER_SECOND * 1000);} catch (Throwable t) {}
                
                
                
//                FAPIDPair faceAnimationPair = faceAnimation.get(faceAnimationIndex);
//                int faceFrameNb = faceAnimationPair.frame.getFrameNumber();
//                FAPIDPair lipAnimationPair = lipAnimation.get(lipAnimationIndex);
//                int lipFrameNb = lipAnimationPair.frame.getFrameNumber();
//                if (faceFrameNb < lipFrameNb){
//                    ++faceAnimationIndex;
//                } else if (faceFrameNb > lipFrameNb){
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
    
    private FAPFrame blend(FAPFrame face, FAPFrame lip){
        FAPFrame blended = new FAPFrame(face);
        
//        int fapIndex = 0;
//        fapIndex = 4;
//        System.out.println("index: " + fapIndex + ", face: " + face.getValue(fapIndex) + ", lip: " + lip.getValue(fapIndex));
//        fapIndex = 17;
//        System.out.println("index: " + fapIndex + ", face: " + face.getValue(fapIndex) + ", lip: " + lip.getValue(fapIndex));
//        fapIndex = 5;
//        System.out.println("index: " + fapIndex + ", face: " + face.getValue(fapIndex) + ", lip: " + lip.getValue(fapIndex));

        
        //UPPER LIP OPENING - 6Faps 4, 8, 9, 51, 55, 56
        blendValueFrom(4, blended, lip);
        blendValueFrom(8, blended, lip);
        blendValueFrom(9, blended, lip);
        blendValueFrom(51, blended, lip);
        blendValueFrom(55, blended, lip);
        blendValueFrom(56, blended, lip);

        //UPPER LIP PROTRUSION - 2Faps 17, 63
        blendValueFrom(17, blended, lip);
        blendValueFrom(63, blended, lip);

        //LOWER LIP OPENING - 6Faps 5, 10, 11, 52, 57, 58
        blendValueFrom(5, blended, lip);
        blendValueFrom(10, blended, lip);
        blendValueFrom(11, blended, lip);
        blendValueFrom(52, blended, lip);
        blendValueFrom(57, blended, lip);
        blendValueFrom(58, blended, lip);

        //LOWER LIP PROTRUSION - 1Faps 16
        blendValueFrom(16, blended, lip);

        //JAW - 3Faps 3, 41, 42
        blendValueFrom(3, blended, lip);
        addValueFrom(41, blended, lip);
        addValueFrom(42, blended, lip);

        //LIP WIDTH - 6Faps 6, 7, 53, 54, 61, 62
        addValueFrom(6, blended, lip);
        addValueFrom(7, blended, lip);
        addValueFrom(53, blended, lip);
        addValueFrom(54, blended, lip);
        addValueFrom(61, blended, lip);
        addValueFrom(62, blended, lip);

        //CORNER LIP - 4Faps 12, 13, 59, 60
        addValueFrom(12, blended, lip);
        addValueFrom(13, blended, lip);
        addValueFrom(59, blended, lip);
        addValueFrom(60, blended, lip);

        return blended;
    }

    private void addValueFrom(int fapIndex, FAPFrame target, FAPFrame source){
//        target.applyValue(fapIndex, target.getValue(fapIndex) + source.getValue(fapIndex));  
        
        target.applyValue(fapIndex, ( 7/6 * target.getValue(fapIndex) + source.getValue(fapIndex))/2);
    }

    private void blendValueFrom(int fapIndex, FAPFrame target, FAPFrame source) {
        double blendCoef = ((double) source.getValue(FAPType.viseme)) / 1000; //BLEND COEF VALUE IS 0.5
        //target.applyValue(fapIndex, (int) (target.getValue(fapIndex)*(1-blendCoef) + source.getValue(fapIndex)*(blendCoef)));
        target.applyValue(fapIndex, (int) ( ( 7/6 * target.getValue(fapIndex)+ source.getValue(fapIndex))/2));
        
    }

    public void setLipSource(FAPFrameEmitter lipSource){
        lipSource.addFAPFramePerformer(lipReceiver);
    }

    public void dettachLipSource(FAPFrameEmitter lipSource){
        lipSource.removeFAPFramePerformer(lipReceiver);
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
