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
package correctionmesh;

import correctionmesh.util.Bone;
import correctionmesh.util.OgreXML;
import greta.core.util.environment.Node;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;

/**
 *
 * @author Andre-Marie Pez
 */
public class AutodeskSkeletonToAnimationSkeleton {

    private static String original = "./Player/Data/media/camille/body.skeleton.xml";
    private static String newFile = "./BehaviorRealizer/Skeleton/camille_skeleton.xml";
    public static void main(String[] aaa){

        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        Bone skel = OgreXML.readSkeleton(parser.parseFile(original));

        //T-pose to N-pose - should be the same as in greta.auxiliary.player.ogre.agent.autodesk.AutodeskAgent constructor
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

        //spine
        rename(skel, "Hips", "HumanoidRoot");
        rename(skel, "Head", "skullbase");
        rename(skel, "Neck1", "vc5");
        rename(skel, "Neck", "vc7");
        rename(skel, "Spine4", "vt6");
        rename(skel, "Spine3", "vt12");
        rename(skel, "Spine2", "vl1");
        rename(skel, "Spine1", "vl3");
        rename(skel, "Spine", "vl5");
        removeChildrenOf(findBone(skel, "skullbase"));
        remove(findBone(skel, "Spine4Right"));
        remove(findBone(skel, "Spine4Left"));
        remove(findBone(skel, "Spine2V"));
        remove(findBone(skel, "Spine1V"));
        remove(findBone(skel, "SpineV"));
        //vc
        addAfter(findBone(skel, "vc7"), "vc6");
        addAfter(findBone(skel, "vc5"), "vc4");
        addAfter(findBone(skel, "vc4"), "vc3");
        addBefore(findBone(skel, "skullbase"), "vc1");
        addBefore(findBone(skel, "vc1"), "vc2");
        //vt
        Bone vt6 = findBone(skel, "vt6");
        addAfter(vt6, "vt1");
        addAfter(vt6, "vt2");
        addAfter(vt6, "vt3");
        addAfter(vt6, "vt4");
        addAfter(vt6, "vt5");
        Bone vt12 = findBone(skel, "vt12");
        addAfter(vt12, "vt7");
        addAfter(vt12, "vt8");
        addAfter(vt12, "vt9");
        addAfter(vt12, "vt10");
        addAfter(vt12, "vt11");
        //vl
        addBefore(findBone(skel, "vl1"), "vl2");
        addBefore(findBone(skel, "vl3"), "vl4");

        //legs
        rename(skel, "LeftUpLeg","l_hip");
        rename(skel, "LeftLeg", "l_knee");
        rename(skel, "LeftFoot", "l_ankle");
        rename(skel, "LeftToeBase", "l_midtarsal");
        removeChildrenOf(findBone(skel, "l_midtarsal"));
        removeBoneButKeepChildren(findBone(skel, "LeftUpLegRoll"));
        removeBoneButKeepChildren(findBone(skel, "LeftLegRoll"));

        rename(skel, "RightUpLeg", "r_hip");
        rename(skel, "RightLeg", "r_knee");
        rename(skel, "RightFoot", "r_ankle");
        rename(skel, "RightToeBase", "r_midtarsal");
        removeChildrenOf(findBone(skel, "r_midtarsal"));
        removeBoneButKeepChildren(findBone(skel, "RightUpLegRoll"));
        removeBoneButKeepChildren(findBone(skel, "RightLegRoll"));

        addAfter(findBone(skel, "HumanoidRoot"), "sacroiliac", false);
        Bone sacro = findBone(skel, "sacroiliac");
        sacro.addChildNode(findBone(skel, "l_hip"));
        sacro.addChildNode(findBone(skel, "r_hip"));
        addBefore(findBone(skel, "r_midtarsal"), "r_subtalar");
        addAfter(findBone(skel, "r_midtarsal"), "r_metatarsal");
        addBefore(findBone(skel, "l_midtarsal"), "l_subtalar");
        addAfter(findBone(skel, "l_midtarsal"), "l_metatarsal");

        //arms
        rename(skel, "RightShoulder", "r_sternoclavicular");
        rename(skel, "RightArm", "r_shoulder");
        rename(skel, "RightForeArm", "r_elbow");
        rename(skel, "RightHand", "r_wrist");

        rename(skel, "RightHandThumb1", "r_thumb1");
        rename(skel, "RightHandThumb2", "r_thumb2");
        rename(skel, "RightHandThumb3",  "r_thumb3");

        rename(skel, "RightHandIndex0",  "r_index0");
        rename(skel, "RightHandIndex1",  "r_index1");
        rename(skel, "RightHandIndex2",  "r_index2");
        rename(skel, "RightHandIndex3",  "r_index3");

        rename(skel, "RightHandMiddle1",  "r_middle1");
        rename(skel, "RightHandMiddle2",  "r_middle2");
        rename(skel, "RightHandMiddle3",  "r_middle3");

        rename(skel, "RightHandRing1",  "r_ring1");
        rename(skel, "RightHandRing2",  "r_ring2");
        rename(skel, "RightHandRing3",  "r_ring3");

        rename(skel, "RightHandPinky0",  "r_pinky0");
        rename(skel, "RightHandPinky1",  "r_pinky1");
        rename(skel, "RightHandPinky2",  "r_pinky2");
        rename(skel, "RightHandPinky3",  "r_pinky3");
        removeBoneButKeepChildren(findBone(skel, "RightArmRoll"));
        removeBoneButKeepChildren(findBone(skel, "RightForeArmRoll"));
        removeBoneButKeepChildren(findBone(skel, "RightFingerBase"));
        remove(findBone(skel, "RightHandThumb4"));
        remove(findBone(skel, "RightHandIndex4"));
        remove(findBone(skel, "RightHandMiddle4"));
        remove(findBone(skel, "RightHandRing4"));
        remove(findBone(skel, "RightHandPinky4"));

        addBefore(findBone(skel, "r_shoulder"), "r_acromioclavicular");
        addBefore(findBone(skel, "r_middle1"), "r_middle0");
        addBefore(findBone(skel, "r_ring1"), "r_ring0");


        rename(skel, "LeftShoulder", "l_sternoclavicular");
        rename(skel, "LeftArm", "l_shoulder");
        rename(skel, "LeftForeArm", "l_elbow");
        rename(skel, "LeftHand", "l_wrist");

        rename(skel, "LeftHandThumb1",  "l_thumb1");
        rename(skel, "LeftHandThumb2",  "l_thumb2");
        rename(skel, "LeftHandThumb3",  "l_thumb3");

        rename(skel, "LeftHandIndex0",  "l_index0");
        rename(skel, "LeftHandIndex1",  "l_index1");
        rename(skel, "LeftHandIndex2",  "l_index2");
        rename(skel, "LeftHandIndex3",  "l_index3");

        rename(skel, "LeftHandMiddle1",  "l_middle1");
        rename(skel, "LeftHandMiddle2",  "l_middle2");
        rename(skel, "LeftHandMiddle3",  "l_middle3");

        rename(skel, "LeftHandRing1",  "l_ring1");
        rename(skel, "LeftHandRing2",  "l_ring2");
        rename(skel, "LeftHandRing3",  "l_ring3");

        rename(skel, "LeftHandPinky0",  "l_pinky0");
        rename(skel, "LeftHandPinky1",  "l_pinky1");
        rename(skel, "LeftHandPinky2",  "l_pinky2");
        rename(skel, "LeftHandPinky3",  "l_pinky3");
        removeBoneButKeepChildren(findBone(skel, "LeftArmRoll"));
        removeBoneButKeepChildren(findBone(skel, "LeftForeArmRoll"));
        removeBoneButKeepChildren(findBone(skel, "LeftFingerBase"));
        remove(findBone(skel, "LeftHandThumb4"));
        remove(findBone(skel, "LeftHandIndex4"));
        remove(findBone(skel, "LeftHandMiddle4"));
        remove(findBone(skel, "LeftHandRing4"));
        remove(findBone(skel, "LeftHandPinky4"));
        addBefore(findBone(skel, "l_shoulder"), "l_acromioclavicular");
        addBefore(findBone(skel, "l_middle1"), "l_middle0");
        addBefore(findBone(skel, "l_ring1"), "l_ring0");

        skel.setToZeroOrientationAndPropagateToChildren();

        Bone root = findBone(skel, "HumanoidRoot");
        removeBoneButKeepChildren(findBone(skel, "Reference"));
        removeBoneButKeepChildren(findBone(skel, "master"));
        root.reIndex(0);
        XMLTree skelxml = OgreXML.writeSkeleton(root);
        skelxml.save(newFile);
    }
    private static void rename(Bone bone, String oldName, String newName){
        Bone found = findBone(bone, oldName);
        if(found != null){
            found.setIdentifier(newName);
        }
    }

    private static void renameAll(Bone bone, String oldName, String newName){
        if(bone.getIdentifier().equals(oldName)){
            bone.setIdentifier(newName);
        }
        for(Node child : bone.getChildren()){
            if(child instanceof Bone){
                renameAll(((Bone)child), oldName, newName);
            }
        }
    }

    private static Bone findBone(Bone skel, String name){
        if(skel.getIdentifier().equals(name)){
            return skel;
        }
        for(Node child : skel.getChildren()){
            if(child instanceof Bone){
                Bone found = findBone((Bone)child, name);
                if(found != null){
                    return found;
                }
            }
        }
        return null;
    }

    private static void removeChildrenOf(Bone bone) {
        if(bone !=null){
            bone.getChildren().clear();
        }
    }

    private static void removeBoneButKeepChildren(Bone bone) {
        bone.setToZeroOrientation();
        for(Node child : bone.getChildren()){
            if(child instanceof Bone){
                Bone childBone = (Bone)child;
                childBone.getCoordinates().add(bone.getCoordinates());
            }
        }
        Bone parent = (Bone)(bone.getParent());
        if(parent !=null){
            parent.removeChild(bone);
            while( ! bone.getChildren().isEmpty()){
                parent.addChildNode(bone.getChildren().get(0));
            }
        }
    }

    private static void remove(Bone bone) {
        if(bone !=null){
            bone.remove();
        }
    }

    private static void correct(Bone skel, String rightShoulder, Quaternion rclavC) {
        Bone bone = findBone(skel, rightShoulder);
        if(bone !=null){
            bone.setOrientation(Quaternion.multiplication(rclavC, bone.getOrientation()));
        }
    }

    private static void addBefore(Bone bone, String newBoneName){
        Bone newParent = new Bone();
        newParent.setIdentifier(newBoneName);

        newParent.setCoordinates(bone.getCoordinates());
        newParent.setOrientation(bone.getOrientation());
        newParent.setScale(bone.getScale());

        bone.setCoordinates(0, 0, 0);
        bone.setOrientation(0, 0, 0);
        bone.setScale(1, 1, 1);

        bone.getParent().addChildNode(newParent);
        newParent.addChildNode(bone);
    }

    private static void addAfter(Bone bone, String newBoneName){
        addAfter(bone, newBoneName, true);
    }
    private static void addAfter(Bone bone, String newBoneName, boolean transfertChildren){

        Bone newChild = new Bone();
        newChild.setIdentifier(newBoneName);

        newChild.setCoordinates(0, 0, 0);
        newChild.setOrientation(0, 0, 0);
        newChild.setScale(1, 1, 1);

        if(transfertChildren){
            ArrayList<Node> childrenCopy = new ArrayList<Node>(bone.getChildren());
            for(Node child : childrenCopy){
                newChild.addChildNode(child);
            }
        }

        bone.addChildNode(newChild);
    }

}
