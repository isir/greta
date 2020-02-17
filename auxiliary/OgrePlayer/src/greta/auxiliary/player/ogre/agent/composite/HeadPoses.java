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
import greta.core.animation.mpeg4.fap.FAP;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import java.util.List;
import vib.auxiliary.player.ogre.natives.AnimationState;
import vib.auxiliary.player.ogre.natives.Entity;
import vib.auxiliary.player.ogre.natives.Mesh;
import vib.auxiliary.player.ogre.natives.SceneManager;
import vib.auxiliary.player.ogre.natives.SceneNode;
import vib.auxiliary.player.ogre.natives.VertexAnimationTrack;
import vib.auxiliary.player.ogre.natives.VertexPoseKeyFrame;

/**
 *
 * @author Andre-Marie Pez
 */
public class HeadPoses {
    private static final float JAWS_VERTICAL_MOVEMENT = 65.53f;
    private static long countIntance = 0;
    Entity face;
    VertexPoseKeyFrame manualKeyFrame;
    AnimationState manualAnimState;

    Entity tongue;
    VertexPoseKeyFrame manualTongueKeyFrame;
    AnimationState manualTongueAnimState;

    SceneNode leftEye;
    SceneNode rightEye;
    SceneNode jaw;

    WrinklesProcess wrinkles;


    public HeadPoses(SceneManager sceneManager,
            SceneNode faceNode,
            String faceMeshFile,
            SceneNode infJawNode,
            String tongueMeshFile,
            SceneNode leftEyeNode,
            SceneNode rightEyeNode,
            String wrinklesMaterialName
            ){

        leftEye = leftEyeNode;
        rightEye = rightEyeNode;
        jaw = infJawNode;
        String fapAnimationName = "FAP_Animation_"+faceNode.getName()+(countIntance++);
        Mesh faceMesh = Ogre.getMesh(faceMeshFile);

        int target = faceMesh.getSubMesh(0).getuseSharedVertices() ? 0 : 1; //the face geometry must be the first submesh !
        //we need to create an Ogre::Animation for each Ogre::Entity using this mesh
        VertexAnimationTrack track = Ogre.getPoseAnimationTrack(faceMesh, fapAnimationName, target);
	manualKeyFrame = track.createVertexPoseKeyFrame(0);
	for(int i=0; i<faceMesh.getPoseCount(); i++) {
            manualKeyFrame.addPoseReference(i, 0);
        }

	// finaly, the face entity is created and the animation is initialized
	face = Ogre.createEntity(sceneManager, faceNode.getName()+"_FaceEntity", faceMeshFile, false);
	faceNode.attachObject(face);
        if(Ogre.useOpenGL() && IniManager.getGlobals().getValueBoolean("OGRE_WRINKLES") && wrinklesMaterialName!=null){
             wrinkles = new doWrinkles(wrinklesMaterialName);
        }
        else{
            wrinkles = new noWrinkles();
        }


	manualAnimState = face.getAnimationState(fapAnimationName);
	manualAnimState.setTimePosition(0);
	manualAnimState.setEnabled(true);
        getPosesIndicesInMesh(faceMesh);

        String togueAnimationName = "Tongue_Animation_"+faceNode.getName()+(countIntance++);
        Mesh tongueMesh = Ogre.getMesh(tongueMeshFile);

        int tongueTarget = 0;
        //we need to create an Ogre::Animation for each Ogre::Entity using this mesh
        VertexAnimationTrack tongueTrack = Ogre.getPoseAnimationTrack(tongueMesh, togueAnimationName, tongueTarget);
	manualTongueKeyFrame = tongueTrack.createVertexPoseKeyFrame(0);
	for(int i=0; i<tongueMesh.getPoseCount(); i++) {
            manualTongueKeyFrame.addPoseReference(i, 0);
        }

	// finaly, the toungue entity is created and the animation is initialized
	tongue = Ogre.createEntity(sceneManager, faceNode.getName()+"_Tongue", tongueMeshFile, false);
	jaw.attachObject(tongue);

        manualTongueAnimState = tongue.getAnimationState(togueAnimationName);
	manualTongueAnimState.setTimePosition(0);
	manualTongueAnimState.setEnabled(true);
    }

    public void applyFAPFrame(FAPFrame ff) {
        applyOnFace(ff);
        applyOnEyes(ff);
        applyOnJaw(ff);
        applyOnTongue(ff);
        wrinkles.applyOnWrinkles(ff);
    }

    private int numberOfPoses;
    private int[] FAPOfPoses;
    private int[] indexOfPose;
    //in the code of the old Greta player, some FAPs are applied under specific conditions.
    //they will be treated separately :
    // <editor-fold defaultstate="collapsed" desc="Uggly variables">
    private int index_Pose_FAP_6; //not applied when FAP 53 != 0
    private int index_Pose_FAP_7; //not applied when FAP 54 != 0
    private int index_Pose_FAP_12_0; //not applied when FAP 59 != 0
    private int index_Pose_FAP_12_1; //not applied when FAP 59 != 0 and when FAP 12 > 0;
    private int index_Pose_FAP_13_0; //not applied when FAP 60 != 0
    private int index_Pose_FAP_13_1; //not applied when FAP 60 != 0 and when FAP 13 > 0;
    private int index_Pose_FAP_17_; //applied with 3.5 when FAP 17 > 0
    private int index_Pose_FAP_31_; //applied with absolute value
    private int index_Pose_FAP_32_; //applied with absolute value
    private int index_Pose_FAP_33_; //applied with absolute value
    private int index_Pose_FAP_34_; //applied with absolute value
    private int index_Pose_FAP_35_; //applied with absolute value
    private int index_Pose_FAP_36_; //applied with absolute value
    private int index_Pose_FAP_39_; //applied when FAP 39 > 0
    private int index_Pose_FAP_40_; //applied when FAP 40 > 0
    private int index_Pose_FAP_41_; //applied with -5 when FAP 41 > 0
    private int index_Pose_FAP_42_; //applied with -5 when FAP 42 > 0
    private int index_Pose_FAP_55_; //applied when FAP 55 < 0
    private int index_Pose_FAP_56_; //applied when FAP 56 < 0
    private int index_Pose_FAP_59_; //applied when FAP 59 > 0
    private int index_Pose_FAP_60_; //applied when FAP 60 > 0
    // </editor-fold>

    /**
     * find indices of poses corresponding to FAPs
     * @param pMesh the Mesh where are poses
     */
    private void getPosesIndicesInMesh(Mesh pMesh){
        // <editor-fold defaultstate="collapsed" desc="Uggly part">
    	numberOfPoses = 0;
	int[] tmpFAPOfPoses = new int[69]; //69 : max number of FAPs
	int[] tmpIndexOfPose = new int[69]; //69 : max number of FAPs
	for(int indexInMesh=0; indexInMesh<pMesh.getPoseCount(); indexInMesh++){
            String nameOfPose = pMesh.getPose(indexInMesh).getName();
            if(nameOfPose.substring(0,4).equals("FAP_")){
                nameOfPose = nameOfPose.substring(4);
                //find Poses used under specifics conditions:
                if(      nameOfPose.equals("6")) {
                    index_Pose_FAP_6 = indexInMesh;
                } else{ if(nameOfPose.equals("7")) {
                    index_Pose_FAP_7 = indexInMesh;
                } else{ if(nameOfPose.equals("12_0")) {
                    index_Pose_FAP_12_0 = indexInMesh;
                } else{ if(nameOfPose.equals("12_1")) {
                    index_Pose_FAP_12_1 = indexInMesh;
                } else{ if(nameOfPose.equals("13_0")) {
                    index_Pose_FAP_13_0 = indexInMesh;
                } else{ if(nameOfPose.equals("13_1")) {
                    index_Pose_FAP_13_1 = indexInMesh;
                } else{ if(nameOfPose.equals("17_")) {
                    index_Pose_FAP_17_ = indexInMesh;
                } else{ if(nameOfPose.equals("31_")) {
                    index_Pose_FAP_31_ = indexInMesh;
                } else{ if(nameOfPose.equals("32_")) {
                    index_Pose_FAP_32_ = indexInMesh;
                } else{ if(nameOfPose.equals("33_")) {
                    index_Pose_FAP_33_ = indexInMesh;
                } else{ if(nameOfPose.equals("34_")) {
                    index_Pose_FAP_34_ = indexInMesh;
                } else{ if(nameOfPose.equals("35_")) {
                    index_Pose_FAP_35_ = indexInMesh;
                } else{ if(nameOfPose.equals("36_")) {
                    index_Pose_FAP_36_ = indexInMesh;
                } else{ if(nameOfPose.equals("39_")) {
                    index_Pose_FAP_39_ = indexInMesh;
                } else{ if(nameOfPose.equals("40_")) {
                    index_Pose_FAP_40_ = indexInMesh;
                } else{ if(nameOfPose.equals("41_")) {
                    index_Pose_FAP_41_ = indexInMesh;
                } else{ if(nameOfPose.equals("42_")) {
                    index_Pose_FAP_42_ = indexInMesh;
                } else{ if(nameOfPose.equals("55_")) {
                    index_Pose_FAP_55_ = indexInMesh;
                } else{ if(nameOfPose.equals("56_")) {
                    index_Pose_FAP_56_ = indexInMesh;
                } else{ if(nameOfPose.equals("59_")) {
                    index_Pose_FAP_59_ = indexInMesh;
                } else{ if(nameOfPose.equals("60_")) {
                    index_Pose_FAP_60_ = indexInMesh;
                } else{ //other FAPs
                    tmpFAPOfPoses[numberOfPoses] = Integer.parseInt(nameOfPose);
                    tmpIndexOfPose[numberOfPoses] = indexInMesh;
                    ++numberOfPoses;
                }}}}}}}}}}}}}}}}}}}}}
            }
	}
	FAPOfPoses  = new int[numberOfPoses];
	indexOfPose = new int[numberOfPoses];
	for(int i=0; i<numberOfPoses; i++){
		FAPOfPoses[i]  = tmpFAPOfPoses[i];
		indexOfPose[i] = tmpIndexOfPose[i];
	}
        // </editor-fold>
    }

    private void applyOnFace(FAPFrame fapFrame){
        boolean modified = false;
        for(int i=0; i<numberOfPoses;i++){
            FAP fap = fapFrame.getAnimationParameter(FAPOfPoses[i]);
            if(fap.getMask()){
                modified=true;
                manualKeyFrame.updatePoseReference(indexOfPose[i], fap.getValue());
            }
        }
        //FAPs applied under specific conditions :
        // <editor-fold defaultstate="collapsed" desc="Uggly part">
        //FAP 6 applied when FAP 53 == 0.0
	if(fapFrame.getAnimationParameter(6).getMask() || fapFrame.getAnimationParameter(53).getMask()){
            if(fapFrame.getAnimationParameter(53).getValue()==0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_6,
                        fapFrame.getAnimationParameter(6).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_6, 0);
            }
            modified=true;
        }

        //FAP 7 applied when FAP 54 == 0.0
        if(fapFrame.getAnimationParameter(7).getMask() || fapFrame.getAnimationParameter(54).getMask()){
            if(fapFrame.getAnimationParameter(54).getValue()==0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_7,
                        fapFrame.getAnimationParameter(7).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_7, 0);
            }
            modified=true;
        }

        //FAP 12 applied when FAP 59 == 0.0
        if(fapFrame.getAnimationParameter(12).getMask() || fapFrame.getAnimationParameter(59).getMask()){
            if(fapFrame.getAnimationParameter(59).getValue()==0){
                manualKeyFrame.updatePoseReference(index_Pose_FAP_12_0,
                        fapFrame.getAnimationParameter(12).getValue());
                if(fapFrame.getAnimationParameter(12).getValue()<0) {
                    manualKeyFrame.updatePoseReference(index_Pose_FAP_12_1,
                            fapFrame.getAnimationParameter(12).getValue());
                }
                else {
                    manualKeyFrame.updatePoseReference(index_Pose_FAP_12_1, 0);
                }
            }
            else{
                manualKeyFrame.updatePoseReference(index_Pose_FAP_12_0, 0);
                manualKeyFrame.updatePoseReference(index_Pose_FAP_12_1, 0);
            }
            modified=true;
        }

        //FAP 13 applied when FAP 60 == 0.0
        if(fapFrame.getAnimationParameter(13).getMask() || fapFrame.getAnimationParameter(60).getMask()){
            if(fapFrame.getAnimationParameter(60).getValue()==0){
                manualKeyFrame.updatePoseReference(index_Pose_FAP_13_0,
                        fapFrame.getAnimationParameter(13).getValue());
                if(fapFrame.getAnimationParameter(13).getValue()<0) {
                    manualKeyFrame.updatePoseReference(index_Pose_FAP_13_1,
                            fapFrame.getAnimationParameter(13).getValue());
                }
                else {
                    manualKeyFrame.updatePoseReference(index_Pose_FAP_13_1, 0);
                }
            }
            else{
                manualKeyFrame.updatePoseReference(index_Pose_FAP_13_0, 0);
                manualKeyFrame.updatePoseReference(index_Pose_FAP_13_1, 0);
            }
            modified=true;
        }

        //FAP 17_ with 3.5 applied when FAP 17 > 0
        if(fapFrame.getAnimationParameter(17).getMask()){
            if(fapFrame.getAnimationParameter(17).getValue() > 0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_17_,
                        3.5f*fapFrame.getAnimationParameter(17).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_17_,
                        fapFrame.getAnimationParameter(17).getValue());
            }
        }

        //FAP 31_ applied with absolute value
        if(fapFrame.getAnimationParameter(31).getMask()) {
            manualKeyFrame.updatePoseReference(index_Pose_FAP_31_,
                    Math.abs(fapFrame.getAnimationParameter(31).getValue()));
        }
        //modified=true is not required because FAP 31 is applied before

        //FAP 32_ applied with absolute value
        if(fapFrame.getAnimationParameter(32).getMask()) {
            manualKeyFrame.updatePoseReference(index_Pose_FAP_32_,
                    Math.abs(fapFrame.getAnimationParameter(32).getValue()));
        }

        //FAP 33_ applied with absolute value
        if(fapFrame.getAnimationParameter(33).getMask()) {
            manualKeyFrame.updatePoseReference(index_Pose_FAP_33_,
                    Math.abs(fapFrame.getAnimationParameter(33).getValue()));
        }

        //FAP 34_ applied with absolute value
        if(fapFrame.getAnimationParameter(34).getMask()) {
            manualKeyFrame.updatePoseReference(index_Pose_FAP_34_,
                    Math.abs(fapFrame.getAnimationParameter(34).getValue()));
        }

        //FAP 35_ applied with absolute value
        if(fapFrame.getAnimationParameter(35).getMask()) {
            manualKeyFrame.updatePoseReference(index_Pose_FAP_35_,
                    Math.abs(fapFrame.getAnimationParameter(35).getValue()));
        }

        //FAP 36_ applied with absolute value
        if(fapFrame.getAnimationParameter(36).getMask()) {
            manualKeyFrame.updatePoseReference(index_Pose_FAP_36_,
                    Math.abs(fapFrame.getAnimationParameter(36).getValue()));
        }

        //FAP 39_ applied when FAP 39 > 0
        if(fapFrame.getAnimationParameter(39).getMask()){
            if(fapFrame.getAnimationParameter(39).getValue() > 0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_39_,
                        fapFrame.getAnimationParameter(39).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_39_, 0);
            }
        }

        //FAP 40_ applied when FAP 40 > 0
        if(fapFrame.getAnimationParameter(40).getMask()){
            if(fapFrame.getAnimationParameter(40).getValue() > 0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_40_,
                        fapFrame.getAnimationParameter(40).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_40_, 0);
            }
        }

        //FAP 41_ with -5 applied when FAP 41 > 0
        if(fapFrame.getAnimationParameter(41).getMask()){
            if(fapFrame.getAnimationParameter(41).getValue() > 0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_41_,
                        -5.0f*fapFrame.getAnimationParameter(41).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_41_,
                        fapFrame.getAnimationParameter(41).getValue());
            }
        }

        //FAP 42_ applied with -5 when FAP 42 > 0
        if(fapFrame.getAnimationParameter(42).getMask()){
            if(fapFrame.getAnimationParameter(42).getValue() > 0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_42_,
                        fapFrame.getAnimationParameter(42).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_42_,
                        -5.0f*fapFrame.getAnimationParameter(42).getValue());
            }
	}

        //FAP 55_ applied when FAP 55 < 0
        if(fapFrame.getAnimationParameter(55).getMask()){
            if(fapFrame.getAnimationParameter(55).getValue() < 0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_55_,
                        fapFrame.getAnimationParameter(55).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_55_, 0);
            }
        }

        //FAP 56_ applied when FAP 56 < 0
        if(fapFrame.getAnimationParameter(56).getMask()){
            if(fapFrame.getAnimationParameter(56).getValue() < 0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_56_,
                        fapFrame.getAnimationParameter(56).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_56_, 0);
            }
        }

        //FAP 59_ applied when FAP 59 > 0
        if(fapFrame.getAnimationParameter(59).getMask()){
            if(fapFrame.getAnimationParameter(59).getValue() > 0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_59_,
                        fapFrame.getAnimationParameter(59).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_59_, 0);
            }
        }

        //FAP 60_ applied when FAP 60 > 0
        if(fapFrame.getAnimationParameter(60).getMask()){
            if(fapFrame.getAnimationParameter(60).getValue() > 0) {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_60_,
                        fapFrame.getAnimationParameter(60).getValue());
            }
            else {
                manualKeyFrame.updatePoseReference(index_Pose_FAP_60_, 0);
            }
	}
        // </editor-fold>

        if(modified) {
            manualAnimState.getParent_notifyDirty();
        }
    }

    private void applyOnEyes(FAPFrame fapFrame){
        List<FAP> fap = fapFrame.getAnimationParametersList();
        //left
        if(fap.get(23).getMask() || fap.get(25).getMask()){
            final greta.core.util.math.Quaternion qx =
                    new greta.core.util.math.Quaternion(
                        new greta.core.util.math.Vec3d(1, 0, 0),
                        fap.get(25).getValue()/100000.0f);
            final greta.core.util.math.Quaternion qy =
                    new greta.core.util.math.Quaternion(
                        new greta.core.util.math.Vec3d(0, 1, 0),
                        fap.get(23).getValue()/100000.0f);
            Ogre.call(new OgreThread.Callback() {
                @Override
                public void run() {
                    leftEye.setOrientation(Ogre.convert(greta.core.util.math.Quaternion.multiplication(qy, qx)));
                    leftEye._update(true, false);
                }
            });
        }
        //right
        if(fap.get(24).getMask() || fap.get(26).getMask()){
            final greta.core.util.math.Quaternion qx =
                    new greta.core.util.math.Quaternion(
                        new greta.core.util.math.Vec3d(1, 0, 0),
                        fap.get(26).getValue()/100000.0f);
            final greta.core.util.math.Quaternion qy =
                    new greta.core.util.math.Quaternion(
                        new greta.core.util.math.Vec3d(0, 1, 0),
                        fap.get(24).getValue()/100000.0f);
            Ogre.call(new OgreThread.Callback() {
                @Override
                public void run() {
                    rightEye.setOrientation(Ogre.convert(greta.core.util.math.Quaternion.multiplication(qy, qx)));
                    rightEye._update(true, false);
                }
             });
        }
    }

    private void applyOnJaw(FAPFrame fapFrame){
        final List<FAP> fap = fapFrame.getAnimationParametersList();
        if(fap.get(3).getMask() || fap.get(14).getMask() || fap.get(15).getMask()){
            Ogre.call(new OgreThread.Callback() {
                @Override
                public void run() {
                    jaw.setPosition(
                            -fap.get(15).getValue()*JAWS_VERTICAL_MOVEMENT,
                            -fap.get(3).getValue()*JAWS_VERTICAL_MOVEMENT,
                            fap.get(14).getValue()*JAWS_VERTICAL_MOVEMENT);
                    jaw._update(true, false);
                }
            });
        }
    }

    private void applyOnTongue(FAPFrame fapFrame){
        List<FAP> fap = fapFrame.getAnimationParametersList();
        boolean modified = false;
        if(fapFrame.getMask(FAPType.shift_tongue_tip)){
            modified=true;
            manualTongueKeyFrame.updatePoseReference(0, //index of corresponding pose in the mesh file
                    fapFrame.getValue(FAPType.shift_tongue_tip));
        }
        if(fapFrame.getMask(44)){
            modified=true;
            manualTongueKeyFrame.updatePoseReference(1, //index of corresponding pose in the mesh file
                    fapFrame.getValue(44));
        }
        if(fapFrame.getMask(45)){
            modified=true;
            manualTongueKeyFrame.updatePoseReference(2, //index of corresponding pose in the mesh file
                    fapFrame.getValue(45));
        }
        /*
        if(fap.get(46).getMask()){
            modified=true;
            if(fap.get(46).getValue()>0){
                manualTongueKeyFrame.updatePoseReference(3, fap.get(46).getValue());
                manualTongueKeyFrame.updatePoseReference(4, 0);
            }
            else{
                manualTongueKeyFrame.updatePoseReference(4, fap.get(46).getValue());
                manualTongueKeyFrame.updatePoseReference(3, 0);
            }
        }
        if(fap.get(47).getMask()){
            modified=true;
            if(fap.get(47).getValue()>0){
                manualTongueKeyFrame.updatePoseReference(5, fap.get(47).getValue());
                manualTongueKeyFrame.updatePoseReference(6, 0);
            }
            else{
                manualTongueKeyFrame.updatePoseReference(6, fap.get(47).getValue());
                manualTongueKeyFrame.updatePoseReference(5, 0);
            }
        }
        //*/

        if(fap.get(46).getMask()){
            modified=true;
            manualTongueKeyFrame.updatePoseReference(3, //index of corresponding pose in the mesh file
                    fap.get(46).getValue());
        }
        if(fap.get(47).getMask()){
            modified=true;
            if(fap.get(47).getValue()>0){
                manualTongueKeyFrame.updatePoseReference(4, fap.get(47).getValue());
                manualTongueKeyFrame.updatePoseReference(5, 0);
            }
            else{
                manualTongueKeyFrame.updatePoseReference(5, fap.get(47).getValue());
                manualTongueKeyFrame.updatePoseReference(4, 0);
            }
        }
        if(modified) {
            manualTongueAnimState.getParent_notifyDirty();
        }
    }


    private interface WrinklesProcess{
        public void applyOnWrinkles(FAPFrame fapFrame);
    }

    private class noWrinkles implements WrinklesProcess{
        @Override
        public void applyOnWrinkles(FAPFrame fapFrame) {}
    }

    private static int instance_count = 0;
    private class doWrinkles implements WrinklesProcess{
        int instance_num;
        vib.auxiliary.player.ogre.natives.GpuProgramParameters params;
        vib.auxiliary.player.ogre.natives.IntBuffer textureIndex;
        vib.auxiliary.player.ogre.natives.FloatBuffer textureValue;

        float tricky_factor = 0.001f;

        boolean wrinklesAviable = false;

        doWrinkles(String materialName){
            if( ! vib.auxiliary.player.ogre.natives.MaterialManager.getSingleton().resourceExists(materialName)){
                Logs.error("Wrinkles not aviable. Material "+ materialName+" does not exist.");
                wrinklesAviable = false;
                return ;
            }
            instance_num = instance_count++;

            //create a new instance of the material
            String newMaterialName = materialName+"-"+instance_num;

//            greta.auxiliary.player.ogre.natives.ResourcePtr resourceMaterialPTR = new greta.auxiliary.player.ogre.natives.ResourcePtr();
//            Ogre.dontDelete(resourceMaterialPTR);//added to prevent crash
            vib.auxiliary.player.ogre.natives.Material originalMaterialPtr = vib.auxiliary.player.ogre.natives.MaterialManager.getSingleton().getByName(materialName);
            if( ! originalMaterialPtr.getTechnique(0).getPass(0).hasFragmentProgram()){
                Logs.error("Wrinkles not aviable. Material "+ materialName+" does not contain any fragment program.");
                wrinklesAviable = false;
                return ;
            }

            vib.auxiliary.player.ogre.natives.Material matClonePtr = originalMaterialPtr.clone(newMaterialName, false, "");

            //get a pointer to the parameters of the shader
//            greta.auxiliary.player.ogre.natives.GpuProgramParameters paramsClonePtr = new greta.auxiliary.player.ogre.natives.GpuProgramParametersSharedPtr();
//            Ogre.dontDelete(paramsClonePtr);//added to prevent crash
            params = matClonePtr.getTechnique(0).getPass(0).getFragmentProgramParameters();
//            params = paramsClonePtr;

            //assign the material on the face
            Ogre.setMaterial(face, newMaterialName);

            //create variables to pass to the shader
            textureIndex = new vib.auxiliary.player.ogre.natives.IntBuffer(13);
            textureValue = new vib.auxiliary.player.ogre.natives.FloatBuffer(13);
            wrinklesAviable = true;
        }

        @Override
        public void applyOnWrinkles(FAPFrame fapFrame) {
            if( ! wrinklesAviable){
                return ;
            }
            List<FAP> fap = fapFrame.getAnimationParametersList();
            int index = 0;
            if((fap.get(31).getValue()>0)||
               (fap.get(32).getValue()>0)){
                int tepmax = Math.max(fap.get(31).getValue(),fap.get(32).getValue());
                float value = (tepmax>450 ? 1 : tepmax/450.0f);
                textureIndex.setIndex(index, 0);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
		//index0 of AU1
            }

            if(fap.get(33).getValue()>0 ){
                float value = fap.get(33).getValue() >500 ? 1 : fap.get(33).getValue()/500.0f;
                textureIndex.setIndex(index, 1);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index1 of AU2 left
            }

            if(fap.get(34).getValue()>0){
                float value = fap.get(34).getValue()>500 ? 1 :  fap.get(34).getValue()/500.0f;
                textureIndex.setIndex(index, 2);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index2 of AU2 right
            }

            if((fap.get(37).getMask()&&
                fap.get(37).getValue()>0)||
               (fap.get(38).getMask()&&fap.get(38).getValue()>0)){
                int tepmax = Math.max(fap.get(37).getValue(), fap.get(38).getValue());

                //Original rules
                //float value = tepmax>150 ? 1 : tepmax/150.0f;

                //Enhanced
                float value = tepmax> 50 ? 1 : tepmax/ 50.0f;

                textureIndex.setIndex(index, 3);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index3 of AU4
            }

            if(fap.get(41).getValue()>0){
                float value = fap.get(41).getValue()>400 ? 1 : fap.get(41).getValue()/400.0f;
                textureIndex.setIndex(index, 4);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index4 of AU6 left
            }

            if(fap.get(42).getValue()>0){
                float value = fap.get(42).getValue()>400 ? 1 : fap.get(42).getValue()/400.0f;
                textureIndex.setIndex(index, 5);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index5 of AU6 right
            }

            if((fap.get(55).getValue() < -90)||
               (fap.get(51).getValue() < -90)){
                int fap_value = Math.min(fap.get(55).getValue(), fap.get(51).getValue());
                float value = (fap_value + 90.0) < -210.0 ? 1 : (fap_value + 90.0f )/(-210.0f);
                textureIndex.setIndex(index, 6);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index6 of AU10 left
            }

            if((fap.get(56).getValue() < -90)||
               (fap.get(51).getValue() < -90)){
                int fap_value = Math.min(fap.get(56).getValue(), fap.get(51).getValue());
                float value = (fap_value + 90.0) < -210.0 ? 1 : (fap_value + 90.0f )/(-210.0f);
                textureIndex.setIndex(index, 7);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index7 of AU10 right
            }

            if((fap.get(53).getValue()>0)&&
               (fap.get(59).getValue()>0)){
                float val53 = fap.get(53).getValue()>200 ? 1 : fap.get(53).getValue()/200.0f;
                float val59 = fap.get(59).getValue()>150 ? 1 : fap.get(59).getValue()/150.0f;
                float value = Math.min(val59, val53);
                textureIndex.setIndex(index, 8);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index8 of AU12 left
            }

            if((fap.get(54).getValue()>0)&&
               (fap.get(60).getValue()>0)){
                float val54 = fap.get(54).getValue()>200 ? 1 : fap.get(54).getValue()/200.0f;
                float val60 = fap.get(60).getValue()>150 ? 1 : fap.get(60).getValue()/150.0f;
                float value = Math.min(val60, val54);
                textureIndex.setIndex(index, 9);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index9 of AU12 right
            }

            if((fap.get(53).getValue()>0)&&
               (fap.get(59).getValue()<=0)){
                float value = fap.get(53).getValue()>100 ? 1 : fap.get(53).getValue()/100.0f;
                textureIndex.setIndex(index, 10);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index10 of AU14left
            }

            if((fap.get(54).getValue()>0)&&
               (fap.get(60).getValue()<=0)){
                float value = fap.get(54).getValue()>100 ? 1 : fap.get(54).getValue()/100.0f;
                textureIndex.setIndex(index, 11);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index11 of AU14right
            }

              if((fap.get(51).getValue()>50)&&
               (fap.get(52).getValue()>50)){
                float value = fap.get(52).getValue()> 200 ? 1 : fap.get(52).getValue()/200.0f;
                textureIndex.setIndex(index, 12);
                textureValue.setIndex(index, value*tricky_factor);
                index++;
                //index12 of AU24
            }
            if(index > 0){
                params.setNamedConstant("textureIndex", textureIndex,index,1);
                params.setNamedConstant("textureValue", textureValue,index,1);
            }
            params.setNamedConstant("nbTextureApplied", index);
        }
    }
}
