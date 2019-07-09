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

package vib.core.animation.mpeg4.fap.filters;

import java.util.*;

import vib.core.animation.mpeg4.fap.*;
import vib.core.util.CharacterManager;
import vib.core.util.Constants;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.time.Timer;

/**
 *
 * @author Andre-Marie Pez
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

    private void startBlending(){
        blender.start();
    }

    @Override
    public synchronized void performFAPFrames(List<FAPFrame> fap_animation, ID requestId) {
        for(FAPFrame frame : fap_animation) {
            faceAnimation.add(new FAPIDPair(requestId,frame));
        }
    }

    @Override
    public synchronized void performFAPFrame(FAPFrame fap_anim, ID requestId) {
        faceAnimation.add(new FAPIDPair(requestId,fap_anim));
    }


    private void computeAndSend(){
        //check identique frame numbers in both list
        ArrayList<FAPFrame> blended = new ArrayList<FAPFrame>();
        int f=0;
        int l=0;
        ArrayList<ID> animIDs = new ArrayList<ID>(2);
        synchronized (this){
            while(f<faceAnimation.size() && l<lipAnimation.size()){
                int f_num = faceAnimation.get(f).frame.getFrameNumber();
                int l_num = lipAnimation.get(l).frame.getFrameNumber();
                if(f_num<l_num){
                    ++f;
                }
                else{
                    if(f_num>l_num){
                        ++l;
                    }
                    else{
                        //they are equal
                        blended.add(blend(faceAnimation.get(f).frame, lipAnimation.get(l).frame));
                        if( ! animIDs.contains(faceAnimation.get(f).animId)){
                            animIDs.add(faceAnimation.get(f).animId);
                        }
                        if( ! animIDs.contains(lipAnimation.get(l).animId)){
                            animIDs.add(lipAnimation.get(l).animId);
                        }
                        faceAnimation.remove(f);
                        lipAnimation.remove(l);
                    }
                }
            }
        }
        if(!blended.isEmpty()){
            ID id = animIDs.size()>1 ?
                        IDProvider.createID("LipBlender", animIDs) :
                        animIDs.get(0);
            sendFAPFrames(id, blended);
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

    public void setLipSource(FAPFrameEmitter lipSource){
        lipSource.addFAPFramePerformer(lipReceiver);
    }

    public void dettachLipSource(FAPFrameEmitter lipSource){
        lipSource.removeFAPFramePerformer(lipReceiver);
    }

    private void addValueFrom(int fapIndex, FAPFrame target, FAPFrame source){
        target.applyValue(fapIndex, target.getValue(fapIndex) + source.getValue(fapIndex));
    }

    private void blendValueFrom(int fapIndex, FAPFrame target, FAPFrame source) {
        double blendCoef = ((double) source.getValue(FAPType.viseme)) / 1000;
        target.applyValue(fapIndex, (int) (target.getValue(fapIndex)*(1-blendCoef) + source.getValue(fapIndex)*(blendCoef)));
    }

    private FAPFrame blend(FAPFrame face, FAPFrame lip){
        FAPFrame blended = new FAPFrame(face);
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

    private class Blender extends Thread {
        boolean end = false;

        @Override
        public void run() {
            while(!end){
                popLatestFrame();
                computeAndSend();
                try { sleep(10); } catch (Throwable t) {}
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
