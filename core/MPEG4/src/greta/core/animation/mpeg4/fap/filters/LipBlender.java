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

    // Latest known state from each stream. The blender thread emits exactly
    // one blended frame per tick at the current time, so frame numbers are
    // always monotonic and both sources are always combined.
    private FAPIDPair currentFaceState = null;
    private FAPIDPair currentLipState = null;
    private int lastEmittedFrameNb = -1;

    // How many frames after the last lip frame we keep blending with it
    // before considering speech finished (~200ms at 25fps).
    private static final int LIP_STATE_HOLD_FRAMES = 5;

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
            // Clock-driven: emit one blended frame per Greta frame tick.
            // This avoids out-of-order frame numbers that caused AU/lip flicker.
            long sleepMs = Math.max(1, 1000 / Constants.FRAME_PER_SECOND);
            while (!end) {
                tick();
                try { sleep(sleepMs); } catch (Throwable t) {}
            }
        }
    }
    
    private void startBlending(){
        blender.start();
    }

    /**
     * One output tick:
     *  1) absorb all pending face frames (MODIFF is real-time → keep latest)
     *  2) absorb lip frames whose scheduled time has been reached
     *  3) emit exactly one blend stamped with the current frame number
     */
    private void tick() {
        synchronized (LipBlender.this) {
            int nowFrame = (int) (Timer.getTime() * Constants.FRAME_PER_SECOND) + blendingDelay;

            // Face stream (MODIFF / AU): always take the latest pending frame
            while (!faceAnimation.isEmpty()) {
                currentFaceState = faceAnimation.remove(0);
            }

            // Lip stream: apply frames that are due for the current time
            while (!lipAnimation.isEmpty()
                    && lipAnimation.get(0).frame.getFrameNumber() <= nowFrame) {
                currentLipState = lipAnimation.remove(0);
            }

            // Speech ended: drop stale lip state so face AUs are not stuck
            // blended with the last viseme.
            if (currentLipState != null
                    && lipAnimation.isEmpty()
                    && currentLipState.frame.getFrameNumber() + LIP_STATE_HOLD_FRAMES < nowFrame) {
                currentLipState = null;
            }

            if (currentFaceState == null && currentLipState == null) {
                return;
            }

            // Enforce monotonic frame numbers for the MPEG4 player
            int outFrameNb = nowFrame;
            if (outFrameNb <= lastEmittedFrameNb) {
                outFrameNb = lastEmittedFrameNb + 1;
            }

            ID outId;
            FAPFrame out;
            if (currentFaceState != null && currentLipState != null) {
                out = blend(currentFaceState.frame, currentLipState.frame);
                outId = currentFaceState.animId;
            } else if (currentFaceState != null) {
                out = new FAPFrame(currentFaceState.frame);
                outId = currentFaceState.animId;
            } else {
                out = new FAPFrame(currentLipState.frame);
                outId = currentLipState.animId;
            }
            out.setFrameNumber(outFrameNb);
            sendFAPFrame(outId, out);
            lastEmittedFrameNb = outFrameNb;
        }
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
        synchronized (LipBlender.this) {
            faceAnimation.removeIf(fapIdPair -> fapIdPair.animId == requestId);
            if (currentFaceState != null && currentFaceState.animId == requestId) {
                currentFaceState = null;
            }
        }
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
        addValueFrom(4, blended, lip);
        addValueFrom(8, blended, lip);
        addValueFrom(9, blended, lip);
        addValueFrom(51, blended, lip);
        addValueFrom(55, blended, lip);
        addValueFrom(56, blended, lip);

        //UPPER LIP PROTRUSION - 2Faps 17, 63
        addValueFrom(17, blended, lip);
        addValueFrom(63, blended, lip);

        //LOWER LIP OPENING - 6Faps 5, 10, 11, 52, 57, 58
        addValueFrom(5, blended, lip);
        addValueFrom(10, blended, lip);
        addValueFrom(11, blended, lip);
        addValueFrom(52, blended, lip);
        addValueFrom(57, blended, lip);
        addValueFrom(58, blended, lip);

        //LOWER LIP PROTRUSION - 1Faps 16
        addValueFrom(16, blended, lip);

        //JAW - 3Faps 3, 41, 42
        addValueFrom(3, blended, lip);
        
        
        
        
        blendValueFrom(41, blended, lip);
        blendValueFrom(42, blended, lip);

        //LIP WIDTH - 6Faps 6, 7, 53, 54, 61, 62
        blendValueFrom(6, blended, lip);
        blendValueFrom(7, blended, lip);
        blendValueFrom(53, blended, lip);
        blendValueFrom(54, blended, lip);
        blendValueFrom(61, blended, lip);
        blendValueFrom(62, blended, lip);

        //CORNER LIP - 4Faps 12, 13, 59, 60
        blendValueFrom(12, blended, lip);
        blendValueFrom(13, blended, lip);
        blendValueFrom(59, blended, lip);
        blendValueFrom(60, blended, lip);

        return blended;
    }

    private void addValueFrom(int fapIndex, FAPFrame target, FAPFrame source){
//        target.applyValue(fapIndex, target.getValue(fapIndex) + source.getValue(fapIndex));  
        
        target.applyValue(fapIndex, ( 7/6 * target.getValue(fapIndex) + source.getValue(fapIndex))/2);
    }

    private void blendValueFrom(int fapIndex, FAPFrame target, FAPFrame source) {
        double blendCoef = ((double) source.getValue(FAPType.viseme)) / 1000; //BLEND COEF VALUE IS 0.5
        target.applyValue(fapIndex, (int) (target.getValue(fapIndex)*(1-blendCoef) + source.getValue(fapIndex)*(blendCoef)));
        //target.applyValue(fapIndex, (int) ( ( 7/6 * target.getValue(fapIndex)+ source.getValue(fapIndex))/2));
        
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
