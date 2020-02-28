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
package greta.auxiliary.player.ogre.agent.composite;

import greta.auxiliary.player.ogre.Ogre;
import greta.auxiliary.player.ogre.OgreThread;
import greta.core.animation.mpeg4.bap.BAP;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.bap.JointType;
import greta.core.util.math.Vec3d;
import vib.auxiliary.player.ogre.natives.Bone;
import vib.auxiliary.player.ogre.natives.Node;
import vib.auxiliary.player.ogre.natives.SkeletonInstance;

/**
 *
 * @author Andre-Marie Pez
 */
public class BodySkeleton {

    private SkeletonInstance bodySkeleton;
    private double[][] values;//values of the frame currently displayed
    private boolean[] mask;
    private int numBones;
    //mapping between skeleton and BAPs
    private int[][] BAPtoBONE = new int[BAPType.NUMBAPS + 1][2];
    private greta.core.util.math.Quaternion[][] originalderivedOrientation; //(parents orientation)
    protected Bone skull;
    private CompositeAgent agent;

    private int null_bap_ordinal = BAPType.null_bap.ordinal();

    private int l_wrist_twisting_ordinal = BAPType.l_wrist_twisting.ordinal();
    private int l_wristTwist1_index=-1;
    private int l_wristTwist2_index=-1;

    private int r_wrist_twisting_ordinal = BAPType.r_wrist_twisting.ordinal();
    private int r_wristTwist1_index=-1;
    private int r_wristTwist2_index=-1;

    private int l_ankle_twisting_ordinal = BAPType.l_ankle_twisting.ordinal();
    private int l_ankleTwist_index=-1;

    private int r_ankle_twisting_ordinal = BAPType.r_ankle_twisting.ordinal();
    private int r_ankleTwist_index=-1;

    public BodySkeleton(SkeletonInstance skel) {
        bodySkeleton = skel;
        numBones = bodySkeleton.getNumBones();
        values = new double[numBones][3];
        mask = new boolean[numBones];
        originalderivedOrientation = new greta.core.util.math.Quaternion[numBones][2];
        skull = skel.getBone("skullbase");
        for (int i = 0; i < numBones; i++) {
            mask[i] = false;
            for (int j = 0; j < 3; j++) {
                values[i][j] = 0;
            }

            try {
                Node parent = bodySkeleton.getBone(i).getParent();
                if( ! parent.isNull())
                originalderivedOrientation[i][0] = Ogre.convert(parent._getDerivedOrientation());
                else{

                originalderivedOrientation[i][0] = new greta.core.util.math.Quaternion();
                }
            } catch (Throwable t) {
                originalderivedOrientation[i][0] = new greta.core.util.math.Quaternion();
            }
            originalderivedOrientation[i][1] = originalderivedOrientation[i][0].inverse();
            originalderivedOrientation[i][0] = mult(originalderivedOrientation[i][0], Ogre.convert(bodySkeleton.getBone(i).getOrientation()));


            //special bones
            if(bodySkeleton.getBone(i).getName().equals("l_wristTwist1")) {
                l_wristTwist1_index = i;
            }
            if(bodySkeleton.getBone(i).getName().equals("l_wristTwist2")) {
                l_wristTwist2_index = i;
            }
            if(bodySkeleton.getBone(i).getName().equals("r_wristTwist1")) {
                r_wristTwist1_index = i;
            }
            if(bodySkeleton.getBone(i).getName().equals("r_wristTwist2")) {
                r_wristTwist2_index = i;
            }

            if(bodySkeleton.getBone(i).getName().equals("l_ankleTwist")) {
                l_ankleTwist_index = i;
            }
            if(bodySkeleton.getBone(i).getName().equals("r_ankleTwist")) {
                r_ankleTwist_index = i;
            }


//            if(i>0){
//                Vec3d pos = Ogre.convert(bodySkeleton.getBone(i)._getDerivedPosition());
//                pos.minus(Ogre.convert(bodySkeleton.getBone(i).getParent()._getDerivedPosition()));
//                System.out.print(
//                        "        <bone id=\""+i+"\" name=\""+bodySkeleton.getBone(i).getName()+"\">\r\n"+
//                        "            <position x=\""+pos.x()+"\" y=\""+pos.y()+"\" z=\""+pos.z()+"\" />\r\n"+
//                        "            <rotation angle=\"0\">\r\n"+
//                        "                <axis x=\"1\" y=\"0\" z=\"0\" />\r\n"+
//                        "            </rotation>\r\n"+
//                        "        </bone>\r\n"
//                        );
//            }
        }
        for (int i = 0; i < numBones; i++) {
            //initialization of the mapping
            JointType joint = JointType.get(bodySkeleton.getBone(i).getName());
            int BAPofXRotation = joint.rotationX.ordinal();
            int BAPofYRotation = joint.rotationY.ordinal();
            int BAPofZRotation = joint.rotationZ.ordinal();
            BAPtoBONE[BAPofXRotation][0] = i;
            BAPtoBONE[BAPofXRotation][1] = 0;
            BAPtoBONE[BAPofYRotation][0] = i;
            BAPtoBONE[BAPofYRotation][1] = 1;
            BAPtoBONE[BAPofZRotation][0] = i;
            BAPtoBONE[BAPofZRotation][1] = 2;

            //to control the skeleton
            bodySkeleton.getBone(i).setManuallyControlled(true);
        }
    }

    public void applyBAPFrame(BAPFrame bf) {
        int i=0;
        for (BAP bap : bf) {
            if (bap.getMask()) {
                values[BAPtoBONE[i][0]][BAPtoBONE[i][1]] = bap.getRadianValue();
                mask[BAPtoBONE[i][0]] = true;
            }
            ++i;
        }
        mask[BAPtoBONE[null_bap_ordinal][0]] = false;
        values[BAPtoBONE[null_bap_ordinal][0]][BAPtoBONE[null_bap_ordinal][1]] = 0;

        //special bones
        applyTwist(BAPtoBONE[l_wrist_twisting_ordinal][0], l_wristTwist1_index, 0.5, l_wristTwist2_index, 0.75);
        applyTwist(BAPtoBONE[r_wrist_twisting_ordinal][0], r_wristTwist1_index, 0.5, r_wristTwist2_index, 0.75);
        applyTwist(BAPtoBONE[l_ankle_twisting_ordinal][0], l_ankleTwist_index, 0.5, -1, 0);
        applyTwist(BAPtoBONE[r_ankle_twisting_ordinal][0], r_ankleTwist_index, 0.5, -1, 0);

        applyValues();
    }

    private void applyTwist(int boneIndexTwistFrom, int boneIndex1TwistTo, double factor1, int boneIndex2TwistTo, double factor2){
        if(mask[boneIndexTwistFrom] && (boneIndex1TwistTo>=0 || boneIndex2TwistTo>=0)){
            double twisting = Ogre.convert(eulerXYZToQuaternion(values[boneIndexTwistFrom][0], values[boneIndexTwistFrom][1], values[boneIndexTwistFrom][2])).getYaw(true);
            if(boneIndex1TwistTo>=0){
                values[boneIndex1TwistTo][1] = twisting * factor1;
                mask[boneIndex1TwistTo] = true;
            }
            if(boneIndex2TwistTo>=0){
                values[boneIndex2TwistTo][1] = twisting * factor2;
                mask[boneIndex2TwistTo] = true;
            }
        }
    }

    public void reset() {
        for (int i = 0; i < numBones; i++) {
            mask[i] = false;
            for (int j = 0; j < 3; j++) {
                mask[i] = mask[i] || values[i][j] != 0;
                values[i][j] = 0;
            }
        }
        applyValues();
    }

    private void applyValues() {
        Ogre.callSync(new OgreThread.Callback() {
            @Override
            public void run() {
                boolean modified = false;
                for (int i = 0; i < numBones; i++) {
                    if (mask[i]) {
                        greta.core.util.math.Quaternion q = eulerXYZToQuaternion(values[i][0], values[i][1], values[i][2]);
                        q = mult(mult(originalderivedOrientation[i][1], q), originalderivedOrientation[i][0]);
                        bodySkeleton.getBone(i).setOrientation(q);
                        mask[i] = false;
                        modified = true;
                    }
                }
                if(modified){
                    bodySkeleton.getBone(0)._update(true, false);
                    updateSkullLink();
                }
            }
        });
    }

    private greta.core.util.math.Quaternion eulerXYZToQuaternion(double x, double y, double z) {
        greta.core.util.math.Quaternion qz = new greta.core.util.math.Quaternion(new Vec3d(0, 0, 1), (float) z);
        greta.core.util.math.Quaternion qy = new greta.core.util.math.Quaternion(new Vec3d(0, 1, 0), (float) y);
        greta.core.util.math.Quaternion qx = new greta.core.util.math.Quaternion(new Vec3d(1, 0, 0), (float) x);

        return mult(mult(qz, qy), qx);
    }

    private greta.core.util.math.Quaternion mult(greta.core.util.math.Quaternion q1, greta.core.util.math.Quaternion q2) {
        return greta.core.util.math.Quaternion.multiplication(q1, q2);
    }

    public SkeletonInstance getSkeleton() {
        return bodySkeleton;
    }

    protected void updateSkullLink(){
        agent.updateHead(skull._getDerivedPosition(), skull._getDerivedOrientation());
    }

    protected void setParentAgent(CompositeAgent agent){
        this.agent = agent;
    }
}
