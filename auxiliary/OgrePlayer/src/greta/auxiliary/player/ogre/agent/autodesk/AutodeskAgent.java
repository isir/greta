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
package greta.auxiliary.player.ogre.agent.autodesk;

import greta.auxiliary.player.ogre.Ogre;
import greta.auxiliary.player.ogre.agent.MPEG4Agent;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.bap.JointType;
import greta.core.animation.mpeg4.bap.filters.ConcatenateJoints;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.IniManager;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;
import vib.auxiliary.player.ogre.natives.Bone;
import vib.auxiliary.player.ogre.natives.Entity;
import vib.auxiliary.player.ogre.natives.SceneManager;
import vib.auxiliary.player.ogre.natives.SceneNode;
import vib.auxiliary.player.ogre.natives.SkeletonInstance;

/**
 *
 * @author Andre-Marie Pez
 */
public class AutodeskAgent extends MPEG4Agent{

    private Entity body;
    private List<FapMapper> fapMappers = new ArrayList<FapMapper>();
    private List<BapMapper> bapMappers = new ArrayList<BapMapper>();
    private ConcatenateJoints concat = new ConcatenateJoints();

    private Bone root;
    private Bone skull;
    private Quaternion skullOriginalOrientationInverse;

    boolean sortForMPEG4Compleance = false;

    public AutodeskAgent(String id, SceneNode parent, SceneManager sceneManager, String meshFile) {
        super(id, parent);

        SceneNode bodyNode = createChildSceneNode(sceneManager.getRootSceneNode(),id+"_bodyNode");
        body = Ogre.createEntity(sceneManager, id+"_body", meshFile, false);
	bodyNode.attachObject(body);

        SkeletonInstance skel = body.getSkeleton();


        sceneManager.getRootSceneNode().removeChild(bodyNode);
        agentNode.addChild(bodyNode);


        // <editor-fold defaultstate="collapsed" desc="setup face">
        double ens = 0;
        double es = 0;
        double mw = 0;
        double mns = 0;
        if(skel.hasBone("LeftEye") && skel.hasBone("RightEye")){
            es = Vec3d.substraction(
                    Ogre.convert(skel.getBone("LeftEye").getPosition()),
                    Ogre.convert(skel.getBone("RightEye").getPosition())).length();
        }
        if(skel.hasBone("LeftEye") && skel.hasBone("RightEye") && skel.hasBone("Nostrils")){
            ens = Vec3d.substraction(Vec3d.multiplication(
                    Vec3d.addition(
                            Ogre.convert(skel.getBone("LeftEye").getPosition()),
                            Ogre.convert(skel.getBone("RightEye").getPosition())
                    ), 0.5f),
                    Ogre.convert(skel.getBone("Nostrils").getPosition())).length()
                    ;
        }
        if(skel.hasBone("LipCornerL") && skel.hasBone("LipCornerR")){
            mw = Vec3d.substraction(
                    Ogre.convert(skel.getBone("LipCornerL").getPosition()),
                    Ogre.convert(skel.getBone("LipCornerR").getPosition())).length();
        }
        if(skel.hasBone("LipCornerL") && skel.hasBone("LipCornerR") && skel.hasBone("Nostrils")){
            mns = Vec3d.substraction(Vec3d.multiplication(
                    Vec3d.addition(
                            Ogre.convert(skel.getBone("LipCornerL").getPosition()),
                            Ogre.convert(skel.getBone("LipCornerR").getPosition())
                    ), 0.5f),
                    Ogre.convert(skel.getBone("Nostrils").getPosition())).length()
                    ;
        }

        if(skel.hasBone("UpperLidL") &&skel.hasBone("LowerLidL")){
            Bone upper = skel.getBone("UpperLidL");
            Bone lower = skel.getBone("LowerLidL");
            float dist = lower.getPosition().distance(upper.getPosition());
            fapMappers.add(new FapMapper.OneDOF(upper, FAPType.close_t_l_eyelid, new Vec3d(-dist/1024f, 0, 0)));
            fapMappers.add(new FapMapper.OneDOF(lower, FAPType.close_b_l_eyelid, new Vec3d(dist/1024f, 0, 0)));
        }
        if(skel.hasBone("UpperLidR") && skel.hasBone("LowerLidR")){
            Bone upper = skel.getBone("UpperLidR");
            Bone lower = skel.getBone("LowerLidR");
            float dist = lower.getPosition().distance(upper.getPosition());
            fapMappers.add(new FapMapper.OneDOF(upper, FAPType.close_t_r_eyelid, new Vec3d(-dist/1024f, 0, 0)));
            fapMappers.add(new FapMapper.OneDOF(lower, FAPType.close_b_r_eyelid, new Vec3d(dist/1024f, 0, 0)));
        }
        if(skel.hasBone("LeftEye")){
            fapMappers.add(new FapMapper.Eye(skel.getBone("LeftEye"), FAPType.pitch_l_eyeball, new Vec3d(0, 0, -1), FAPType.yaw_l_eyeball, new Vec3d(1, 0, 0)));
        }
        if(skel.hasBone("RightEye")){
            fapMappers.add(new FapMapper.Eye(skel.getBone("RightEye"), FAPType.pitch_r_eyeball, new Vec3d(0, 0, -1), FAPType.yaw_r_eyeball, new Vec3d(1, 0, 0)));
        }
        if(skel.hasBone("BrowInnerL")){
            fapMappers.add(new FapMapper.TwoDOF(skel.getBone("BrowInnerL"), FAPType.raise_l_i_eyebrow, new Vec3d(ens/1024, 0, 0), FAPType.squeeze_l_eyebrow, new Vec3d(0, -es/2048, es/1024)));
        }
        if(skel.hasBone("BrowOuterL")){
            fapMappers.add(new FapMapper.OneDOF(skel.getBone("BrowOuterL"), FAPType.raise_l_o_eyebrow, new Vec3d(ens/1024, es/4096, 0)));
        }
        if(skel.hasBone("BrowInnerR")){
            fapMappers.add(new FapMapper.TwoDOF(skel.getBone("BrowInnerR"), FAPType.raise_r_i_eyebrow, new Vec3d(ens/1024, 0, 0), FAPType.squeeze_r_eyebrow, new Vec3d(0, -es/2048, -es/1024)));
        }
        if(skel.hasBone("BrowOuterR")){
            fapMappers.add(new FapMapper.OneDOF(skel.getBone("BrowOuterR"), FAPType.raise_r_o_eyebrow, new Vec3d(ens/1024, es/4096, 0)));
        }
        if(skel.hasBone("CheekL")){
            fapMappers.add(new FapMapper.OneDOF(skel.getBone("CheekL"), FAPType.lift_l_cheek, new Vec3d(ens/1024, 0, 0)));
        }
        if(skel.hasBone("CheekR")){
            fapMappers.add(new FapMapper.OneDOF(skel.getBone("CheekR"), FAPType.lift_r_cheek, new Vec3d(ens/1024, 0, 0)));
        }


        if(skel.hasBone("LipCornerL")){
            fapMappers.add(new FapMapper.Lip(skel.getBone("LipCornerL"),
                    FAPType.stretch_l_cornerlip_o, new Vec3d(0, 0, -mw/1024),
                    FAPType.raise_l_cornerlip_o, new Vec3d(mns/1024, 0, 0),
                    FAPType.stretch_l_cornerlip, new Vec3d(1, 0, 0)));
        }
        if(skel.hasBone("LipCornerR")){
            fapMappers.add(new FapMapper.Lip(skel.getBone("LipCornerR"),
                    FAPType.stretch_r_cornerlip_o, new Vec3d(0, 0, mw/1024),
                    FAPType.raise_r_cornerlip_o, new Vec3d(mns/1024, 0, 0),
                    FAPType.stretch_r_cornerlip, new Vec3d(-1, 0, 0)));
        }

        if(skel.hasBone("LipLowerL")){
            fapMappers.add(new FapMapper.MidLip(skel.getBone("LipLowerL"),
                    FAPType.raise_b_lip_lm_o, new Vec3d(mns/1024, 0, 0),
                    FAPType.push_b_lip, new Vec3d(0, -mns/1024, 0),
                    FAPType.raise_b_lip_lm, new Vec3d(0, 0, 1),
                    FAPType.stretch_l_cornerlip_o, new Vec3d(0, 0, -mw/4096)));
        }

        if(skel.hasBone("LipLowerR")){
            fapMappers.add(new FapMapper.MidLip(skel.getBone("LipLowerR"),
                    FAPType.raise_b_lip_rm_o, new Vec3d(mns/1024, 0, 0),
                    FAPType.push_b_lip, new Vec3d(0, -mns/1024, 0),
                    FAPType.raise_b_lip_rm, new Vec3d(0, 0, 1),
                    FAPType.stretch_r_cornerlip_o, new Vec3d(0, 0, mw/4096)));
        }

        if(skel.hasBone("LipUpperL")){
            fapMappers.add(new FapMapper.MidLip(skel.getBone("LipUpperL"),
                    FAPType.lower_t_lip_lm_o, new Vec3d(-mns/1024, 0, 0),
                    FAPType.push_t_lip, new Vec3d(0, -mns/1024, 0),
                    FAPType.lower_t_lip_lm, new Vec3d(0, 0, -1),
                    FAPType.stretch_l_cornerlip_o, new Vec3d(0, 0, -mw/4096)));
        }
        if(skel.hasBone("LipUpperR")){
            fapMappers.add(new FapMapper.MidLip(skel.getBone("LipUpperR"),
                    FAPType.lower_t_lip_rm_o, new Vec3d(-mns/1024, 0, 0),
                    FAPType.push_t_lip, new Vec3d(0, -mns/1024, 0),
                    FAPType.lower_t_lip_rm, new Vec3d(0, 0, -1),
                    FAPType.stretch_r_cornerlip_o, new Vec3d(0, 0, mw/4096)));
        }

        if(skel.hasBone("Jaw")){
            fapMappers.add(new FapMapper.Jaw(skel.getBone("Jaw"),
                    FAPType.open_jaw, new Vec3d(0, 0, -1), 0.0008,
                    FAPType.shift_jaw, new Vec3d(-1, 0, 0), 0.0008,
                    FAPType.thrust_jaw, new Vec3d(0, -mns/1024,0)));
        }
        if(skel.hasBone("Nostrils")){
            fapMappers.add(new FapMapper.Nostril(skel.getBone("Nostrils"), FAPType.stretch_l_nose, FAPType.stretch_r_nose, new Vec3d(ens/32, ens/256, ens/512)));
        }
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="correction T-pose to N-pose">
        double clavC = Math.PI/20;
        Quaternion rclavC = new Quaternion(new Vec3d(0, -1, 0), clavC);
        Quaternion lclavC = new Quaternion(new Vec3d(0, 1, 0), clavC);
        Quaternion shoulderC = new Quaternion(new Vec3d(0, -1, 0), Math.PI/2 - clavC);
        Quaternion rThumbC = Quaternion.multiplication(Quaternion.multiplication(
                new Quaternion(new Vec3d(0, 0, 1), Math.toRadians(-33)),
                new Quaternion(new Vec3d(0, 1, 0), Math.toRadians(20))),
                new Quaternion(new Vec3d(1, 0, 0), Math.toRadians(15)));

        correct(skel, "RightShoulder", rclavC);
        correct(skel, "LeftShoulder", lclavC);
        correct(skel, "RightArm", shoulderC);
        correct(skel, "LeftArm", shoulderC);
        correct(skel, "RightHandThumb1", rThumbC);
        correct(skel, "LeftHandThumb1", rThumbC);
        skel.getBone(0)._update(true, true);
        // </editor-fold>

        ArrayList<JointType> typesUsed = new ArrayList<JointType>();

        // <editor-fold defaultstate="collapsed" desc="spine">
        map(typesUsed, skel, "Hips", JointType.HumanoidRoot);
        map(typesUsed, skel, "Head", JointType.skullbase);
        map(typesUsed, skel, "Neck1", JointType.vc5);
        map(typesUsed, skel, "Neck", JointType.vc7);
        map(typesUsed, skel, "Spine4", JointType.vt6);
        map(typesUsed, skel, "Spine3", JointType.vt12);
        map(typesUsed, skel, "Spine2", JointType.vl1);
        map(typesUsed, skel, "Spine1", JointType.vl3);
        map(typesUsed, skel, "Spine", JointType.vl5);
        map(typesUsed, skel, "Spine2V", JointType.vl1, -0.5);
        map(typesUsed, skel, "Spine1V", JointType.vl3, -0.5);
        map(typesUsed, skel, "SpineV", JointType.vl5, -0.5);

        //map(typesUsed, skel, "Spine4Right", JointType.vt12, -0.5); // ???
        //map(typesUsed, skel, "Spine4Left", JointType.vt12, -0.5); // ???

        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="legs">
        map(typesUsed, skel, "LeftUpLeg","LeftUpLegRoll", JointType.l_hip, 0.5, false);
        map(typesUsed, skel, "LeftLeg", JointType.l_knee);
        map(typesUsed, skel, "LeftFoot", "LeftLegRoll", JointType.l_ankle, 0.5, true);
        map(typesUsed, skel, "LeftToeBase", JointType.l_midtarsal);

        map(typesUsed, skel, "RightUpLeg", "RightUpLegRoll", JointType.r_hip, 0.5, false);
        map(typesUsed, skel, "RightLeg", JointType.r_knee);
        map(typesUsed, skel, "RightFoot", "RightLegRoll", JointType.r_ankle, 0.5, true);
        map(typesUsed, skel, "RightToeBase", JointType.r_midtarsal);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="right arm">
        map(typesUsed, skel, "RightShoulder",    JointType.r_sternoclavicular);
//        map(typesUsed, skel, "RightArm",         JointType.r_shoulder);
//        map(typesUsed, skel, "RightArm", "RightArmRoll", JointType.r_shoulder, 0.5, false);
        mapShoulder(typesUsed, skel, "RightArm", "RightArmRoll", JointType.r_shoulder, JointType.r_acromioclavicular, 0.5);
        map(typesUsed, skel, "RightForeArm",     JointType.r_elbow);
        map(typesUsed, skel, "RightHand", "RightForeArmRoll", JointType.r_wrist, 0.75, true);

        map(typesUsed, skel, "RightHandThumb1",  JointType.r_thumb1);
        map(typesUsed, skel, "RightHandThumb2",  JointType.r_thumb2);
        map(typesUsed, skel, "RightHandThumb3",  JointType.r_thumb3);

        map(typesUsed, skel, "RightHandIndex0",  JointType.r_index0);
        map(typesUsed, skel, "RightHandIndex1",  JointType.r_index1);
        map(typesUsed, skel, "RightHandIndex2",  JointType.r_index2);
        map(typesUsed, skel, "RightHandIndex3",  JointType.r_index3);

        map(typesUsed, skel, "RightHandMiddle1",  JointType.r_middle1);
        map(typesUsed, skel, "RightHandMiddle2",  JointType.r_middle2);
        map(typesUsed, skel, "RightHandMiddle3",  JointType.r_middle3);

        map(typesUsed, skel, "RightHandRing1",  JointType.r_ring1);
        map(typesUsed, skel, "RightHandRing2",  JointType.r_ring2);
        map(typesUsed, skel, "RightHandRing3",  JointType.r_ring3);

        map(typesUsed, skel, "RightHandPinky0",  JointType.r_pinky0);
        map(typesUsed, skel, "RightHandPinky1",  JointType.r_pinky1);
        map(typesUsed, skel, "RightHandPinky2",  JointType.r_pinky2);
        map(typesUsed, skel, "RightHandPinky3",  JointType.r_pinky3);
        // </editor-fold>

        // <editor-fold defaultstate="collapsed" desc="left arm">
        map(typesUsed, skel, "LeftShoulder",    JointType.l_sternoclavicular);
//        map(typesUsed, skel, "LeftArm", "LeftArmRoll", JointType.l_shoulder, 0.5, false);
        mapShoulder(typesUsed, skel, "LeftArm", "LeftArmRoll", JointType.l_shoulder, JointType.l_acromioclavicular, 0.5);

        map(typesUsed, skel, "LeftForeArm",     JointType.l_elbow);
        map(typesUsed, skel, "LeftHand", "LeftForeArmRoll", JointType.l_wrist, 0.75, true);

        map(typesUsed, skel, "LeftHandThumb1",  JointType.l_thumb1);
        map(typesUsed, skel, "LeftHandThumb2",  JointType.l_thumb2);
        map(typesUsed, skel, "LeftHandThumb3",  JointType.l_thumb3);

        map(typesUsed, skel, "LeftHandIndex0",  JointType.l_index0);
        map(typesUsed, skel, "LeftHandIndex1",  JointType.l_index1);
        map(typesUsed, skel, "LeftHandIndex2",  JointType.l_index2);
        map(typesUsed, skel, "LeftHandIndex3",  JointType.l_index3);

        map(typesUsed, skel, "LeftHandMiddle1",  JointType.l_middle1);
        map(typesUsed, skel, "LeftHandMiddle2",  JointType.l_middle2);
        map(typesUsed, skel, "LeftHandMiddle3",  JointType.l_middle3);

        map(typesUsed, skel, "LeftHandRing1",  JointType.l_ring1);
        map(typesUsed, skel, "LeftHandRing2",  JointType.l_ring2);
        map(typesUsed, skel, "LeftHandRing3",  JointType.l_ring3);

        map(typesUsed, skel, "LeftHandPinky0",  JointType.l_pinky0);
        map(typesUsed, skel, "LeftHandPinky1",  JointType.l_pinky1);
        map(typesUsed, skel, "LeftHandPinky2",  JointType.l_pinky2);
        map(typesUsed, skel, "LeftHandPinky3",  JointType.l_pinky3);
        // </editor-fold>


        skull = skel.getBone("Head");

        skullOriginalOrientationInverse = Ogre.convert(skull._getDerivedOrientation());
        skullOriginalOrientationInverse = skullOriginalOrientationInverse.inverse();
        root = skel.getRootBone();
        concat.setJointToUse(typesUsed);
    }

    public void setWrinklesMaterial(String materialName, int target){
        if(Ogre.useOpenGL() && IniManager.getGlobals().getValueBoolean("OGRE_WRINKLES") && materialName!=null){
            WrinklesFapMapper wfm = new WrinklesFapMapper(body, materialName, target);
            if(wfm.wrinklesAviable){
                //add it only if the constructor does not fail to load wrinkles
                fapMappers.add(wfm);
            }
        }
    }

    private void correct(SkeletonInstance skel, String boneName, Quaternion correction){
        if(skel.hasBone(boneName)){
            Bone bone = skel.getBone(boneName);
            bone.setOrientation(Quaternion.multiplication(correction, Ogre.convert(bone.getOrientation())));
        }
    }

    private void map(List<JointType> typesUsed, SkeletonInstance skel, String boneName, JointType joint){
        map(typesUsed, skel, boneName, null, joint, 0, true, 1);
    }

    private void map(List<JointType> typesUsed, SkeletonInstance skel, String boneName, JointType joint, double scale){
        map(typesUsed, skel, boneName, null, joint, 0, true, scale);
    }

    private void map(List<JointType> typesUsed, SkeletonInstance skel, String boneName, String twistBoneName, JointType joint, double twistFactor, boolean before){
        map(typesUsed, skel, boneName, twistBoneName, joint, twistFactor, before, 1);
    }

    private void map(List<JointType> typesUsed, SkeletonInstance skel, String boneName, String twistBoneName, JointType joint, double twistFactor, boolean before, double scale){
        if(skel.hasBone(boneName)){
            ArrayList<Vec3d> dofs = new ArrayList<Vec3d>(3);
            ArrayList<BAPType> types = new ArrayList<BAPType>(3);
            if(joint.rotationX != BAPType.null_bap){
                dofs.add(new Vec3d(1, 0, 0));
                types.add(joint.rotationX);
            }
            if(joint.rotationY != BAPType.null_bap){
                dofs.add(new Vec3d(0, 1, 0));
                types.add(joint.rotationY);
            }
            if(joint.rotationZ != BAPType.null_bap){
                dofs.add(new Vec3d(0, 0, 1));
                types.add(joint.rotationZ);
            }

            if(dofs.isEmpty()){
                return;
            }
            if(dofs.size()==1){
                bapMappers.add(new BapMapper.OneDOF(skel.getBone(boneName), types.get(0), dofs.get(0)));
            }
            sortDegreeOfFreedom(types, dofs);
            if(dofs.size()==2){
                bapMappers.add(new BapMapper.TwoDOF(skel.getBone(boneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1)));
            }
            if(dofs.size()==3){
                if(twistBoneName !=null && skel.hasBone(twistBoneName)){
                    if(sortForMPEG4Compleance){
                        if(before) {
                            bapMappers.add(new BapMapper.SortedTwistBeforeMapper(skel.getBone(boneName), skel.getBone(twistBoneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1), types.get(2), dofs.get(2), twistFactor));
                        } else {
                            bapMappers.add(new BapMapper.SortedTwistMapper(skel.getBone(boneName), skel.getBone(twistBoneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1), types.get(2), dofs.get(2), twistFactor));
                        }
                    }
                    else {
                        if(before){
                            bapMappers.add(new BapMapper.YawTwistBeforeMapper(skel.getBone(boneName), skel.getBone(twistBoneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1), types.get(2), dofs.get(2), twistFactor));
                        } else {
                            bapMappers.add(new BapMapper.YawTwistAfterMapper(skel.getBone(boneName), skel.getBone(twistBoneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1), types.get(2), dofs.get(2), twistFactor));
                        }
                    }
                }
                else {
                    if(scale == 1){
                         bapMappers.add(new BapMapper.ThreeDOF(skel.getBone(boneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1), types.get(2), dofs.get(2)));
                    }
                    else{
                        BapMapper.ThreeDOFScaled bm = new BapMapper.ThreeDOFScaled(skel.getBone(boneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1), types.get(2), dofs.get(2));
                        bm.setScale(scale);
                        bapMappers.add(bm);
                    }
                }
            }
            typesUsed.add(joint);
        }
    }

    private void mapShoulder(List<JointType> typesUsed, SkeletonInstance skel, String boneName, String twistBoneName, JointType shoulderJoint, JointType acromiumJoint, double twistFactor) {
        if (skel.hasBone(boneName)) {
            ArrayList<Vec3d> dofs = new ArrayList<Vec3d>(3);
            ArrayList<BAPType> types = new ArrayList<BAPType>(3);
            dofs.add(new Vec3d(1, 0, 0));
            types.add(shoulderJoint.rotationX);
            dofs.add(new Vec3d(0, 1, 0));
            types.add(shoulderJoint.rotationY);
            dofs.add(new Vec3d(0, 0, 1));
            types.add(shoulderJoint.rotationZ);
            sortDegreeOfFreedom(types, dofs);
            if (skel.hasBone(twistBoneName)) {
                if (sortForMPEG4Compleance) {

                    ArrayList<Vec3d> dofs2 = new ArrayList<Vec3d>(2);
                    ArrayList<BAPType> types2 = new ArrayList<BAPType>(2);
                    dofs2.add(new Vec3d(0, 1, 0));
                    types2.add(acromiumJoint.rotationY);
                    dofs2.add(new Vec3d(0, 0, 1));
                    types2.add(acromiumJoint.rotationZ);
                    sortDegreeOfFreedom(types2, dofs2);

                    BapMapper acromium = new BapMapper.TwoDOF(skel.getBone(boneName), types2.get(0), dofs2.get(0), types2.get(1), dofs2.get(1));

                    bapMappers.add(new BapMapper.ShoulderSortedTwistMapper(skel.getBone(boneName), skel.getBone(twistBoneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1), types.get(2), dofs.get(2), twistFactor, acromium));
                    typesUsed.add(acromiumJoint);
                } else {
                    bapMappers.add(new BapMapper.YawTwistAfterMapper(skel.getBone(boneName), skel.getBone(twistBoneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1), types.get(2), dofs.get(2), twistFactor));
                }
            } else {
                bapMappers.add(new BapMapper.ThreeDOF(skel.getBone(boneName), types.get(0), dofs.get(0), types.get(1), dofs.get(1), types.get(2), dofs.get(2)));
            }
            typesUsed.add(shoulderJoint);
        }
    }

    void sortDegreeOfFreedom(ArrayList<BAPType> types, ArrayList<Vec3d> dofs){
        if(sortForMPEG4Compleance){
            //bubble sort
            for(int loop=0; loop<types.size(); ++loop){
                for(int i=0; i<types.size()-1; ++i){
                    if(types.get(i).ordinal()<types.get(i+1).ordinal()){
                        BAPType tempType = types.get(i);
                        Vec3d tempDoF = dofs.get(i);
                        types.set(i, types.get(i+1));
                        dofs.set(i, dofs.get(i+1));
                        types.set(i+1, tempType);
                        dofs.set(i+1, tempDoF);
                    }
                }
            }
        }
    }

    @Override
    protected void setEntitiesVisible(boolean visible) {
        for(int i=0; i<body.getNumSubEntities(); ++i){
            body.getSubEntity(i).setVisible(visible);
        }
    }

    @Override
    protected void applyFapFrame(FAPFrame fapFrame) {
        for (FapMapper mapper : fapMappers) {
            mapper.applyFap(fapFrame);
        }
        skull._update(true, false);
    }

    @Override
    protected void applyBapFrame(BAPFrame bapFrame) {
        BAPFrame bapFrameConat = concat.concatenateJoints(bapFrame);
        for (BapMapper mapper : bapMappers) {
            mapper.applyBap(bapFrameConat);
        }
        root._update(true, false);
        requestUpdateHead();
    }

    @Override
    protected void requestUpdateHead() {
        if(getMPEG4Animatable()!=null){
            updateHeadPosition(skull._getDerivedPosition());

            updateHeadOrientation(Quaternion.multiplication(
                    Ogre.convert(skull._getDerivedOrientation()),
                    skullOriginalOrientationInverse
            ));
        }
    }

    @Override
    public Entity getMainEntityWithSkeleton() {
        return body;
    }
}
