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
import greta.auxiliary.player.ogre.agent.MPEG4Agent;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.util.math.Vec3d;
import java.util.Map;
import java.util.Map.Entry;
import vib.auxiliary.player.ogre.natives.AxisAlignedBox;
import vib.auxiliary.player.ogre.natives.Entity;
import vib.auxiliary.player.ogre.natives.Quaternion;
import vib.auxiliary.player.ogre.natives.SceneManager;
import vib.auxiliary.player.ogre.natives.SceneNode;
import vib.auxiliary.player.ogre.natives.Vector3;

/**
 *
 * @author Andre-Marie Pez
 */
public class CompositeAgent extends MPEG4Agent{

    private long accessoryIterator = 0;
   // private IBone skull;
    private Entity body;
    private Entity l_eye;
    private Entity r_eye;
    private Entity inf_jaw;
    private Entity sup_jaw;
    private Entity glottis;
    private SceneNode bodyNode;
    private SceneNode headNode;
    private SceneNode rotationNode;
    private SceneNode orientationNode;
    private SceneNode headPartsNode;
    private SceneNode faceNode;
    private SceneNode l_EyeNode;
    private SceneNode r_EyeNode;
    private SceneNode jawNode;
    private SceneNode inf_jawNode;
    private BodySkeleton skeleton;
    private HeadPoses headByPose;
    private SceneManager sceneManager;


    public CompositeAgent(String id, SceneNode parent, SceneManager sceneManager, String bodyMeshFile, String faceMeshFile, String wrinklesMaterialName){
        super(id, parent);
        this.sceneManager = sceneManager;

        //load the body
	bodyNode = createChildSceneNode(sceneManager.getRootSceneNode(),id+"_bodyNode");
	body = Ogre.createEntity(sceneManager, id+"_body", bodyMeshFile, false);
	bodyNode.attachObject(body);

	skeleton = new BodySkeleton(body.getSkeleton());

	//set the minimum coordinate of the body as the origine point of the agent
        AxisAlignedBox boundingBox = body.getWorldBoundingBox(true);
        float prePaddingFactor = vib.auxiliary.player.ogre.natives.MeshManager.getBoundsPaddingFactor();
        float postPaddingFactor = (1 + prePaddingFactor * 2) / prePaddingFactor;

        Vector3 center = boundingBox.getCenter();
	Vector3 bodyGap = new Vector3(
		-center.getx(),
		-boundingBox.getMinimum().gety()
                    -(boundingBox.getMaximum().gety()-boundingBox.getMinimum().gety())/postPaddingFactor,
		-center.getz());
        sceneManager.getRootSceneNode().removeChild(bodyNode);
        agentNode.addChild(bodyNode);
	bodyNode.setPosition(bodyGap);


        //create the head
        headNode = createChildSceneNode(agentNode,id+"_headNode");
	rotationNode = createChildSceneNode(headNode,id+"_rotNode");
	orientationNode = createChildSceneNode(rotationNode,id+"_orientationNode");
	headPartsNode = createChildSceneNode(orientationNode,id+"_headPartsNode");


	//instanciation eyes
	//	left
	l_EyeNode = createChildSceneNode(headPartsNode,id+"_l_EyeNode");
	l_eye = Ogre.createEntity(sceneManager, id+"_l_eye", "eye.mesh", false);
	l_EyeNode.attachObject(l_eye);

	//	right
	r_EyeNode = createChildSceneNode(headPartsNode,id+"_r_EyeNode");
	r_eye = Ogre.createEntity(sceneManager, id+"_r_eye", "eye.mesh", false);
	r_EyeNode.attachObject(r_eye);


	//jaw
	jawNode = createChildSceneNode(headPartsNode,id+"_jawNode");

	sup_jaw = Ogre.createEntity(sceneManager, id+"_sup_jaw", "sup_jaw.mesh", false);
	jawNode.attachObject(sup_jaw);

	glottis = Ogre.createEntity(sceneManager, id+"_glottis", "glottis.mesh", false);
	jawNode.attachObject(glottis);

	inf_jawNode = createChildSceneNode(jawNode,id+"_inf_jawNode");
	inf_jaw = Ogre.createEntity(sceneManager, id+"_inf_jaw", "inf_jaw.mesh", false);
	inf_jawNode.attachObject(inf_jaw);

        headNode.setPosition(bodyGap);

        Quaternion inverseSkullOrientation = skeleton.skull._getDerivedOrientation().Inverse();
        orientationNode.setOrientation(inverseSkullOrientation);
        skeleton.setParentAgent(this);
        skeleton.updateSkullLink();

        //instanciation the face
	faceNode = createChildSceneNode(headPartsNode,id+"_faceNode");
        headByPose = new HeadPoses(sceneManager,faceNode, faceMeshFile, inf_jawNode, "tongue.mesh", l_EyeNode, r_EyeNode, wrinklesMaterialName);
    }

    @Override
    protected void requestUpdateHead(){
        skeleton.updateSkullLink();
    }

    @Override
    public void setEntitiesVisible(boolean visible) {
        for(int i=0; i<body.getNumSubEntities(); ++i) body.getSubEntity(i).setVisible(visible);
        headNode.setVisible(visible, true);
    }

    protected void updateHead(Vector3 position, Quaternion orientation){
        //set the orientation from the skull
        rotationNode.setOrientation(orientation);
        //set the position from the skull
        rotationNode.setPosition(position);
        rotationNode._update(true, false);
        if(getMPEG4Animatable()!=null){
            Vec3d pos = Ogre.convert(position);
            pos.add(Ogre.convert(bodyNode.getPosition()));
            pos = Vec3d.multiplicationOfComponents(pos, Ogre.convert(agentNode.getScale()));
            updateHeadPosition(pos);
            updateHeadOrientation(orientation);
        }
    }

    @Override
    protected void applyFapFrame(FAPFrame fapFrame) {
        headByPose.applyFAPFrame(fapFrame);
    }

    @Override
    protected void applyBapFrame(BAPFrame bapFrame) {
        skeleton.applyBAPFrame(bapFrame);
    }

    public void addHeadAccessory(String mesh, Vector3 position, Quaternion orientation, Vector3 scale, Map<String, Integer> materials){
        String accessoryId = getAgentId()+"_Accessory_"+(accessoryIterator++);
        SceneNode accessoryNode = createChildSceneNode(headPartsNode, accessoryId+"_Node");
        Entity accessoryEntity = Ogre.createEntity(sceneManager, accessoryId, mesh, false);
	accessoryNode.attachObject(accessoryEntity);
        accessoryNode.setPosition(position);
        accessoryNode.setScale(scale);
        accessoryNode.setOrientation(orientation);
        for(Entry<String, Integer> entry : materials.entrySet()){
            Ogre.setMaterial(accessoryEntity, entry.getKey(), entry.getValue().intValue());
        }
    }


    //face modifiers
    public void setFacePosition(Vector3 position){
        faceNode.setPosition(position);
    }

    public void setFaceScale(Vector3 scale){
        faceNode.setScale(scale);
    }

    public void setFaceOrientation(Quaternion orientation){
        faceNode.setOrientation(orientation);
    }

    public void setFaceMaterial(String materialName, int target){
        Ogre.setMaterial(headByPose.face, materialName, target);
    }

    //left eye modifiers
    public void setLeftEyePosition(Vector3 position){
        l_EyeNode.setPosition(position);
    }

    public void setLeftEyeScale(Vector3 scale){
        l_EyeNode.setScale(scale);
    }

    public void setLeftEyeOrientation(Quaternion orientation){
        l_EyeNode.setOrientation(orientation);
    }

    public void setLeftEyeMaterial(String materialName){
        Ogre.setMaterial(l_eye, materialName);
    }

    //right eye modifiers
    public void setRightEyePosition(Vector3 position){
        r_EyeNode.setPosition(position);
    }

    public void setRightEyeScale(Vector3 scale){
        r_EyeNode.setScale(scale);
    }

    public void setRightEyeOrientation(Quaternion orientation){
        r_EyeNode.setOrientation(orientation);
    }

    public void setRightEyeMaterial(String materialName){
        Ogre.setMaterial(r_eye, materialName);
    }

    //jaw modifiers
    public void setJawPosition(Vector3 position){
        jawNode.setPosition(position);
    }

    public void setJawScale(Vector3 scale){
        jawNode.setScale(scale);
    }

    public void setJawOrientation(Quaternion orientation){
        jawNode.setOrientation(orientation);
    }

    //head parts modifiers
    public void setHeadPosition(Vector3 position){
        headPartsNode.setPosition(position);
    }

    public void setHeadScale(Vector3 scale){
        headPartsNode.setScale(scale);
    }

    public void setHeadOrientation(Quaternion orientation){
        headPartsNode.setOrientation(orientation);
    }

    //body modifiers
    public void setBodyMaterial(String materialName, int target){
        Ogre.setMaterial(body, materialName, target);
    }

    @Override
    public  Entity getMainEntityWithSkeleton() {
        return body;
    }
}
