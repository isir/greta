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
package greta.core.animation.mpeg4.bap.filters;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class BodyBlender extends BAPFrameEmitterImpl implements BAPFramePerformer {

    public int blending_delay = 0;
    public double k_factor = 1;

    public double getK_factor() {
        return k_factor;
    }

    public void setK_factor(double k_factor) {
        this.k_factor = k_factor;
    }

    private static Comparator<BAPFrame> bapComp = new Comparator<BAPFrame>() {
        @Override
        public int compare(BAPFrame o1, BAPFrame o2) {
            return o2.getFrameNumber() - o1.getFrameNumber();
        }
    };
    private List<BAPIDPair> bodyAnimation;
    private List<BAPIDPair> laughterAnimation;

    /**
     * this enclosed {@code FAPFramePerformer} is used to collect FAPs from the
     * lips.
     */
    private BAPFramePerformer laughterReceiver;
    private Blender blender;

    public BodyBlender(){
        bodyAnimation = new LinkedList<BAPIDPair>();
        laughterAnimation = new LinkedList<BAPIDPair>();
        laughterReceiver = new BAPFramePerformer(){
            @Override
            public void performBAPFrames(List<BAPFrame> bap_animation, ID requestId) {
                synchronized(BodyBlender.this){
                    for(BAPFrame frame : bap_animation) {
                        //System.out.println(frame.getFrameNumber());
                        laughterAnimation.add(new BAPIDPair(requestId,frame));
                    }
                }
            }
            public void performBAPFrame(BAPFrame bap_anim, ID requestId) {
                synchronized(BodyBlender.this){
                    laughterAnimation.add(new BAPIDPair(requestId,bap_anim));
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
    public void performBAPFrames(List<BAPFrame> bap_animation, ID requestId) {
        for (BAPFrame frame : bap_animation) {
            bodyAnimation.add(new BAPIDPair(requestId, frame));
        }
    }

    public synchronized void performBAPFrame(BAPFrame bap_anim, ID requestId) {
        bodyAnimation.add(new BAPIDPair(requestId, bap_anim));
    }

    BAPFrame _last = null;
    private void computeAndSend() {
        //check identique frame numbers in both list
        ArrayList<BAPFrame> blended = new ArrayList<BAPFrame>();
        int f = 0;
        int l = 0;
        ArrayList<ID> animIDs = new ArrayList<ID>(2);
        synchronized (this) {
            while (f < bodyAnimation.size() && l < laughterAnimation.size()) {
                int f_num = bodyAnimation.get(f).frame.getFrameNumber();
                int l_num = laughterAnimation.get(l).frame.getFrameNumber();
                if (f_num < l_num) {
                    ++f;
                } else {
                    if (f_num > l_num) {
                        ++l;
                    } else {
                        //they are equal
                        blended.add(blend(bodyAnimation.get(f).frame, laughterAnimation.get(l).frame));
                        _last=bodyAnimation.get(f).frame;
                        if (!animIDs.contains(bodyAnimation.get(f).animId)) {
                            animIDs.add(bodyAnimation.get(f).animId);
                        }
                        if (!animIDs.contains(laughterAnimation.get(l).animId)) {
                            animIDs.add(laughterAnimation.get(l).animId);
                        }
                        bodyAnimation.remove(f);
                        laughterAnimation.remove(l);
                    }
                }
            }
        }
        if (!blended.isEmpty()) {
            ID id = animIDs.size() > 1
                    ? IDProvider.createID("LipBlender", animIDs)
                    : animIDs.get(0);
            sendBAPFrames(id, blended);
        }
    }

    private void expulseLatedFrame() {
        synchronized (BodyBlender.this) {
            while (!bodyAnimation.isEmpty() && bodyAnimation.get(0).frame.getFrameNumber() < Timer.getTime() * Constants.FRAME_PER_SECOND + blending_delay) {
                sendBAPFrame(bodyAnimation.get(0).animId, bodyAnimation.get(0).frame);
                bodyAnimation.remove(0);
            }
        }
        synchronized (BodyBlender.this) {
            while (!laughterAnimation.isEmpty() && laughterAnimation.get(0).frame.getFrameNumber() < Timer.getTime() * Constants.FRAME_PER_SECOND + blending_delay) {
                laughterAnimation.get(0).frame.setMask(BAPType.l_shoulder_abduct, false);
                laughterAnimation.get(0).frame.setMask(BAPType.l_shoulder_flexion, false);
                laughterAnimation.get(0).frame.setMask(BAPType.l_shoulder_twisting, false);
                laughterAnimation.get(0).frame.setMask(BAPType.r_shoulder_abduct, false);
                laughterAnimation.get(0).frame.setMask(BAPType.r_shoulder_flexion, false);
                laughterAnimation.get(0).frame.setMask(BAPType.r_shoulder_twisting, false);
                sendBAPFrame(laughterAnimation.get(0).animId, laughterAnimation.get(0).frame);
                laughterAnimation.remove(0);
            }
        }
    }

    public void setLaughterSource(BAPFrameEmitter laughterSource) {
        laughterSource.addBAPFramePerformer(laughterReceiver);
    }

    public void dettachLaughterSource(BAPFrameEmitter laughterSource) {
        laughterSource.removeBAPFramePerformer(laughterReceiver);
    }

    private void addValueFrom(int bapIndex, BAPFrame target, BAPFrame source){
        //System.out.println("start: "+ target.getRadianValue(bapIndex) + " " +target.getMask(bapIndex)+ " " +source.getRadianValue(bapIndex));
        target.applyValue(bapIndex, target.getValue(bapIndex) + source.getValue(bapIndex));
        //System.out.println("end: "+target.getRadianValue(bapIndex));
    }

    private void replaceValueFrom(int bapIndex, BAPFrame target, BAPFrame source){
        target.applyValue(bapIndex, source.getValue(bapIndex));
    }

    private void addRadianValueFrom(int bapIndex, BAPFrame target, double value){
        //target.setRadianValue(bapIndex, target.getRadianValue(bapIndex) + value);
        //if(target.getMask(bapIndex))
        //System.out.println("start: "+ target.getRadianValue(bapIndex) + " " +target.getMask(bapIndex)+" " +value);
        target.setRadianValue(bapIndex, target.getRadianValue(bapIndex)  + value);
        //System.out.println("end: "+target.getRadianValue(bapIndex));
    }

//    private BAPFrame simpleblend(BAPFrame body, BAPFrame laughter) {
//
//    }


    double factor_torso = 1;

    private void addValueFromTorso(int bapIndex, BAPFrame target, BAPFrame source, double fac){
        //System.out.println("start: "+ target.getRadianValue(bapIndex) + " " +target.getMask(bapIndex)+ " " +source.getRadianValue(bapIndex));
        target.applyValue(bapIndex, (int)(/*target.getValue(bapIndex) * (1 - fac) +*/ (source.getValue(bapIndex) * fac)));
        //System.out.println("end: "+target.getRadianValue(bapIndex));
    }
    private BAPFrame reversedblend(BAPFrame body, BAPFrame laughter) {
        //System.out.println("start:" + body.getRadianValue(JointType.l_shoulder.rotationX.ordinal()));
        BAPFrame blended = new BAPFrame(body);
        for(int i = 0; i < body.APVector.size(); ++i){
            if(blended.getMask(i) && laughter.getMask(i)){
                addValueFromTorso(i, blended, laughter, factor_torso);
                blended.setMask(i, true);
            }else if(blended.getMask(i)){
                blended.setMask(i, true);
            }else if(laughter.getMask(i)){
                addValueFromTorso(i, blended, laughter, factor_torso);
                blended.setMask(i, true);
            }
        }
        //head
       /* addValueFrom(BAPType.vc1_tilt.ordinal(), blended, laughter);
        addValueFrom(BAPType.vc1_torsion.ordinal(), blended, laughter);
        addValueFrom(BAPType.vc1_roll.ordinal(), blended, laughter);
        addValueFrom(BAPType.vc3_tilt.ordinal(), blended, laughter);
        addValueFrom(BAPType.vc3_torsion.ordinal(), blended, laughter);
        addValueFrom(BAPType.vc3_roll.ordinal(), blended, laughter);
        addValueFrom(BAPType.vc6_tilt.ordinal(), blended, laughter);
        addValueFrom(BAPType.vc6_torsion.ordinal(), blended, laughter);
        addValueFrom(BAPType.vc6_roll.ordinal(), blended, laughter);

        //shoulder
        //System.out.println("start: shoulder");
        addValueFrom(BAPType.l_sternoclavicular_abduct.ordinal(), blended, laughter);//Z
        addValueFrom(BAPType.r_sternoclavicular_abduct.ordinal(), blended, laughter);
        addValueFrom(BAPType.l_sternoclavicular_rotate.ordinal(), blended, laughter);//y
        addValueFrom(BAPType.r_sternoclavicular_rotate.ordinal(), blended, laughter);//y

        addValueFrom(JointType.l_shoulder.rotationX.ordinal() ,blended, laughter);
        //System.out.println(laughter.getDegreeValue(JointType.l_shoulder.rotationX.ordinal()));
        addValueFrom(JointType.l_shoulder.rotationY.ordinal() ,blended, laughter);
        //System.out.println(laughter.getDegreeValue(JointType.l_shoulder.rotationY.ordinal()));
        addValueFrom(JointType.l_shoulder.rotationZ.ordinal() ,blended, laughter);
        //System.out.println(laughter.getDegreeValue(JointType.l_shoulder.rotationZ.ordinal()));

        addValueFrom(JointType.r_shoulder.rotationX.ordinal() ,blended, laughter);
        addValueFrom(JointType.r_shoulder.rotationY.ordinal() ,blended, laughter);
        addValueFrom(JointType.r_shoulder.rotationZ.ordinal() ,blended, laughter);

        //System.out.println("end: shoulder");*/

      /*  double globalLeftX = laughter.getRadianValue(JointType.vl1.rotationX) + laughter.getRadianValue(JointType.vt4.rotationX) + laughter.getRadianValue(JointType.vt1.rotationX)
                + laughter.getRadianValue(JointType.vt8.rotationX);
        double globalLeftY = laughter.getRadianValue(JointType.vl1.rotationY) + laughter.getRadianValue(JointType.vt4.rotationY) + laughter.getRadianValue(JointType.vt1.rotationY)
                + laughter.getRadianValue(JointType.vt8.rotationY) + laughter.getRadianValue(BAPType.l_sternoclavicular_rotate);
        double globalLeftZ = laughter.getRadianValue(JointType.vl1.rotationZ) + laughter.getRadianValue(JointType.vt4.rotationZ) + laughter.getRadianValue(JointType.vt1.rotationZ)
                + laughter.getRadianValue(JointType.vt8.rotationZ) + laughter.getRadianValue(BAPType.l_sternoclavicular_abduct);

        globalLeftX *= factor_torso;
        globalLeftY *= factor_torso;
        globalLeftZ *= factor_torso;

        globalLeftX = edgePiCutting(globalLeftX)* k_factor;
        globalLeftY = edgePiCutting(globalLeftY)* k_factor;
        globalLeftZ = edgePiCutting(globalLeftZ)* k_factor;

        double globalRightX = laughter.getRadianValue(JointType.vl1.rotationX) + laughter.getRadianValue(JointType.vt4.rotationX) + laughter.getRadianValue(JointType.vt1.rotationX)
                + laughter.getRadianValue(JointType.vt8.rotationX);
        double globalRightY = laughter.getRadianValue(JointType.vl1.rotationY) + laughter.getRadianValue(JointType.vt4.rotationY) + laughter.getRadianValue(JointType.vt1.rotationY)
                + laughter.getRadianValue(JointType.vt8.rotationY) + laughter.getRadianValue(BAPType.r_sternoclavicular_rotate);
        double globalRightZ = laughter.getRadianValue(JointType.vl1.rotationZ) + laughter.getRadianValue(JointType.vt4.rotationZ) + laughter.getRadianValue(JointType.vt1.rotationZ)
                + laughter.getRadianValue(JointType.vt8.rotationZ)+ laughter.getRadianValue(BAPType.r_sternoclavicular_abduct);

        globalRightX *= factor_torso;
        globalRightY *= factor_torso;
        globalRightZ *= factor_torso;

        globalRightX = edgePiCutting(globalRightX) * k_factor;
        globalRightY = edgePiCutting(globalRightY) * k_factor;
        globalRightZ = edgePiCutting(globalRightZ) * k_factor;
        //body.get
        addRadianValueFrom(JointType.l_sternoclavicular.rotationX.ordinal() ,blended, -globalLeftX);
        //addRadianValueFrom(JointType.l_shoulder.rotationY.ordinal() ,blended, -globalLeftY);
        addRadianValueFrom(JointType.l_sternoclavicular.rotationZ.ordinal() ,blended, -globalLeftZ);

        addRadianValueFrom(JointType.r_sternoclavicular.rotationX.ordinal() ,blended, -globalRightX);
        //addRadianValueFrom(JointType.r_shoulder.rotationY.ordinal() ,blended, -globalRightY);
        addRadianValueFrom(JointType.r_sternoclavicular.rotationZ.ordinal() ,blended, -globalRightZ);*/


        //torso
       /* addValueFromTorso(BAPType.vt1_roll.ordinal(), blended, laughter, factor_torso);
        addValueFromTorso(BAPType.vt1_torsion.ordinal(), blended, laughter, factor_torso);
        addValueFromTorso(BAPType.vt1_tilt.ordinal(), blended, laughter, factor_torso);

        addValueFromTorso(BAPType.vt4_tilt.ordinal(), blended, laughter, factor_torso);
        addValueFromTorso(BAPType.vt4_torsion.ordinal(), blended, laughter, factor_torso);
        addValueFromTorso(BAPType.vt4_roll.ordinal(), blended, laughter, factor_torso);

        addValueFromTorso(BAPType.vt8_tilt.ordinal(), blended, laughter, factor_torso);
        addValueFromTorso(BAPType.vt8_torsion.ordinal(), blended, laughter, factor_torso);
        addValueFromTorso(BAPType.vt8_roll.ordinal(), blended, laughter, factor_torso);

        addValueFromTorso(BAPType.vl1_roll.ordinal(), blended, laughter, factor_torso);
        addValueFromTorso(BAPType.vl1_torsion.ordinal(), blended, laughter, factor_torso);
        addValueFromTorso(BAPType.vl1_tilt.ordinal(), blended, laughter, factor_torso);*/

//        blended.setMask(BAPType.vt1_roll, false);
//        blended.setMask(BAPType.vt1_torsion, false);
//        blended.setMask(BAPType.vt1_tilt, false);
//        blended.setMask(BAPType.vt4_tilt, false);
//        blended.setMask(BAPType.vt4_torsion, false);
//        blended.setMask(BAPType.vt4_roll, false);
//        blended.setMask(BAPType.vt8_tilt, false);
//        blended.setMask(BAPType.vt8_torsion, false);
//        blended.setMask(BAPType.vt8_roll, false);
//        blended.setMask(BAPType.vl1_roll, false);
//        blended.setMask(BAPType.vl1_torsion, false);
//        blended.setMask(BAPType.vl1_tilt, false);
        //System.out.println("end:" + blended.getRadianValue(JointType.l_shoulder.rotationX.ordinal()));
        return blended;
    }


    private BAPFrame blend(BAPFrame body, BAPFrame laughter) {
        return reversedblend(body, laughter);
        /*
//        BAPFrame blended = new BAPFrame(laughter);
//        //addValueFrom(4, blended, body);
//        replaceValueFrom(BAPType.l_shoulder_abduct.ordinal(), blended, body);
//        replaceValueFrom(BAPType.l_shoulder_flexion.ordinal(), blended, body);
//        replaceValueFrom(BAPType.l_shoulder_twisting.ordinal(), blended, body);
//        replaceValueFrom(BAPType.r_shoulder_abduct.ordinal(), blended, body);
//        replaceValueFrom(BAPType.r_shoulder_flexion.ordinal(), blended, body);
//        replaceValueFrom(BAPType.r_shoulder_twisting.ordinal(), blended, body);
//        replaceValueFrom(BAPType.l_elbow_flexion.ordinal(), blended, body);
//        replaceValueFrom(BAPType.l_elbow_twisting.ordinal(), blended, body);
//        replaceValueFrom(BAPType.r_elbow_flexion.ordinal(), blended, body);
//        replaceValueFrom(BAPType.r_elbow_twisting.ordinal(), blended, body);
//        replaceValueFrom(BAPType.l_wrist_flexion.ordinal(), blended, body);
//        replaceValueFrom(BAPType.l_wrist_pivot.ordinal(), blended, body);
//        replaceValueFrom(BAPType.l_wrist_twisting.ordinal(), blended, body);
//        replaceValueFrom(BAPType.r_wrist_flexion.ordinal(), blended, body);
//        replaceValueFrom(BAPType.r_wrist_pivot.ordinal(), blended, body);
//        replaceValueFrom(BAPType.r_wrist_twisting.ordinal(), blended, body);

        BAPFrame blended = new BAPFrame(body);
        replaceValueFrom(BAPType.vc1_tilt.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vc1_torsion.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vc1_roll.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vc3_tilt.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vc3_torsion.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vc3_roll.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vc6_tilt.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vc6_torsion.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vc6_roll.ordinal(), blended, laughter);

        replaceValueFrom(BAPType.l_sternoclavicular_abduct.ordinal(), blended, laughter);//Z
        replaceValueFrom(BAPType.r_sternoclavicular_abduct.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.l_sternoclavicular_rotate.ordinal(), blended, laughter);//y
        replaceValueFrom(BAPType.r_sternoclavicular_rotate.ordinal(), blended, laughter);//y

        replaceValueFrom(BAPType.vt1_roll.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vt1_torsion.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vt1_tilt.ordinal(), blended, laughter);

//        replaceValueFrom(BAPType.vl4_tilt.ordinal(), blended, laughter);
//        replaceValueFrom(BAPType.vl4_torsion.ordinal(), blended, laughter);
//        replaceValueFrom(BAPType.vl4_roll.ordinal(), blended, laughter);

        replaceValueFrom(BAPType.vt4_tilt.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vt4_torsion.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vt4_roll.ordinal(), blended, laughter);

        replaceValueFrom(BAPType.vt8_tilt.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vt8_torsion.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vt8_roll.ordinal(), blended, laughter);

        replaceValueFrom(BAPType.vl1_roll.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vl1_torsion.ordinal(), blended, laughter);
        replaceValueFrom(BAPType.vl1_tilt.ordinal(), blended, laughter);

//        replaceValueFrom(JointType.vt3.rotationX.ordinal(), blended, laughter);
//        replaceValueFrom(JointType.vt6.rotationX.ordinal(), blended, laughter);
//        replaceValueFrom(JointType.vt10.rotationX.ordinal(), blended, laughter);
//        replaceValueFrom(JointType.vl2.rotationX.ordinal(), blended, laughter);

        double globalLeftX = blended.getRadianValue(JointType.vl1.rotationX) + blended.getRadianValue(JointType.vt4.rotationX) + blended.getRadianValue(JointType.vt1.rotationX)
                + blended.getRadianValue(JointType.vt8.rotationX);
        double globalLeftY = blended.getRadianValue(JointType.vl1.rotationY) + blended.getRadianValue(JointType.vt4.rotationY) + blended.getRadianValue(JointType.vt1.rotationY)
                + blended.getRadianValue(JointType.vt8.rotationX) + blended.getRadianValue(BAPType.l_sternoclavicular_rotate);
        double globalLeftZ = blended.getRadianValue(JointType.vl1.rotationZ) + blended.getRadianValue(JointType.vt4.rotationZ) + blended.getRadianValue(JointType.vt1.rotationZ)
                + blended.getRadianValue(JointType.vt8.rotationZ) + blended.getRadianValue(BAPType.l_sternoclavicular_abduct);
        globalLeftX = edgePiCutting(globalLeftX);
        globalLeftY = edgePiCutting(globalLeftY);
        globalLeftZ = edgePiCutting(globalLeftZ);

        double globalRightX = blended.getRadianValue(JointType.vl1.rotationX) + blended.getRadianValue(JointType.vt4.rotationX) + blended.getRadianValue(JointType.vt1.rotationX)
                + blended.getRadianValue(JointType.vt8.rotationX);
        double globalRightY = blended.getRadianValue(JointType.vl1.rotationY) + blended.getRadianValue(JointType.vt4.rotationY) + blended.getRadianValue(JointType.vt1.rotationY)
                + blended.getRadianValue(JointType.vt8.rotationY) + blended.getRadianValue(BAPType.r_sternoclavicular_rotate);
        double globalRightZ = blended.getRadianValue(JointType.vl1.rotationZ) + blended.getRadianValue(JointType.vt4.rotationZ) + blended.getRadianValue(JointType.vt1.rotationZ)
                + blended.getRadianValue(JointType.vt8.rotationZ)+ blended.getRadianValue(BAPType.r_sternoclavicular_abduct);
        globalRightX = edgePiCutting(globalRightX);
        globalRightY = edgePiCutting(globalRightY);
        globalRightZ = edgePiCutting(globalRightZ);
        //body.get
//        addRadianValueFrom(JointType.l_shoulder.rotationX.ordinal() ,blended, -globalLeftX);
//        addRadianValueFrom(JointType.l_shoulder.rotationY.ordinal() ,blended, -globalLeftY);
//        addRadianValueFrom(JointType.l_shoulder.rotationZ.ordinal() ,blended, -globalLeftZ);
//
//        addRadianValueFrom(JointType.r_shoulder.rotationX.ordinal() ,blended, -globalRightX);
//        addRadianValueFrom(JointType.r_shoulder.rotationY.ordinal() ,blended, -globalRightY);
//        addRadianValueFrom(JointType.r_shoulder.rotationZ.ordinal() ,blended, -globalRightZ);

        return blended;*/
    }

    double edgePiCutting(double x){
        while(x > Math.PI){
            x -= Math.PI;
        }
        while(x < -Math.PI){
            x += Math.PI;
        }
        return x;
    }

    private class Blender extends Thread {

        boolean end = false;

        @Override
        public void run() {
            while (!end) {
                expulseLatedFrame();
                computeAndSend();
                try {
                    sleep(10);
                } catch (Throwable t) {
                }
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        blender.end = false;
        super.finalize();
    }

    private class BAPIDPair {

        ID animId;
        BAPFrame frame;

        BAPIDPair(ID animId, BAPFrame frame) {
            this.animId = animId;
            this.frame = frame;
        }
    }

}
