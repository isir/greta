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
package greta.auxiliary.BVHMocap;

import java.util.HashMap;

/**
 *
 * @author Nesrine Fourati
 */
public class Dictionary {

    private HashMap<String, String> dict = new HashMap<String, String>();

    public Dictionary() {
    }

    public Dictionary(HashMap<String, String> dictionary) {
        dict = dictionary;
    }

    public String GetJointName(String bvh_joint_name) {
        String joint_name = dict.get(bvh_joint_name);
        return joint_name;
    }

    public HashMap<String, String> GetDict() {
        return dict;
    }

    public void Initialize() {

        String characterName;
        characterName = "Poppy";
                //CharacterManager.getCurrentCharacterName();

        //"Greta";
        //"Poppy";
        //System.out.println();
        //CharacterManager.getCurrentCharacterName()
//bvh_skeleton

        //convert "Hips"/"root" to "HumanoidRoot" if you want to create a bap file in the new version of bap,
        //convert "Hips"/"root" to "sacroiliac" if you want to create a bap file in the old version pf bap

        dict.put("sacroiliac", "sacroiliac");
        dict.put("Head", "skullbase");
        dict.put("Hips", "HumanoidRoot");
        dict.put("root", "HumanoidRoot");
        dict.put("Neck", "vc6");
        dict.put("LeftCollar", "l_sternoclavicular");
        dict.put("RightCollar", "r_sternoclavicular");
        dict.put("Chest", "vl5");// **********
        dict.put("Chest2", "vl3");// ********
        dict.put("Chest3", "vt12");// **********
        dict.put("Chest4", "vt8");// ********

        // dict.put("LeftShoulder", "l_acromioclavicular");
        dict.put("LeftCollar", "l_acromioclavicular");
        dict.put("LeftShoulder", "l_shoulder");
        dict.put("LeftArm", "l_shoulder");
        dict.put("LeftUpArm", "l_shoulder");
        dict.put("LeftLowArm", "l_elbow");
        dict.put("LeftForeArm", "l_elbow");
        dict.put("LeftElbow", "l_elbow");
        dict.put("LeftHand", "l_wrist");
        dict.put("LeftWrist", "l_wrist");
        dict.put("LeftHandThumb1", "l_thumb1");
        dict.put("LeftHandThumb2", "l_thumb2");
        dict.put("LeftHandThumb3", "l_thumb3");
        dict.put("LeftHandThumb4", "l_thumb4");
        dict.put("LeftHandIndex1", "l_index1");
        dict.put("LeftHandIndex2", "l_index2");
        dict.put("LeftHandIndex3", "l_index3");
        dict.put("LeftHandIndex4", "l_index4");
        dict.put("LeftHandMiddle1", "l_middle1");
        dict.put("LeftHandMiddle2", "l_middle2");
        dict.put("LeftHandMiddle3", "l_middle3");
        dict.put("LeftHandMiddle4", "l_middle4");
        dict.put("LeftHandRing1", "l_ring1");
        dict.put("LeftHandRing2", "l_ring2");
        dict.put("LeftHandRing3", "l_ring3");
        dict.put("LeftHandRing4", "l_ring4");
        dict.put("LeftHandPinky1", "l_pinky1");
        dict.put("LeftHandPinky2", "l_pinky2");
        dict.put("LeftHandPinky3", "l_pinky3");
        dict.put("LeftHandPinky4", "l_pinky4");

        // dict.put("RightShoulder", "r_acromioclavicular");
        dict.put("RightCollar", "r_acromioclavicular");
        dict.put("RightShoulder", "r_shoulder");
        dict.put("RightArm", "r_shoulder");
        dict.put("RightUpArm", "r_shoulder");
        dict.put("RightLowArm", "r_elbow");
        dict.put("RightForeArm", "r_elbow");

        dict.put("RightElbow", "r_elbow");
        dict.put("RightHand", "r_wrist");
        dict.put("RightWrist", "r_wrist");
        dict.put("RighttHandThumb1", "r_thumb1");
        dict.put("RightHandThumb2", "r_thumb2");
        dict.put("RightHandThumb3", "r_thumb3");
        dict.put("RightHandThumb4", "r_thumb4");
        dict.put("RightHandIndex1", "r_index1");
        dict.put("RightHandIndex2", "r_index2");
        dict.put("RightHandIndex3", "r_index3");
        dict.put("RightHandIndex4", "r_index4");
        dict.put("RightHandMiddle1", "r_middle1");
        dict.put("RightHandMiddle2", "r_middle2");
        dict.put("RightHandMiddle3", "r_middle3");
        dict.put("RightHandMiddle4", "r_middle4");
        dict.put("RightHandRing1", "r_ring1");
        dict.put("RightHandRing2", "r_ring2");
        dict.put("RightHandRing3", "r_ring3");
        dict.put("RightHandRing4", "r_ring4");
        dict.put("RightHandPinky1", "r_pinky1");
        dict.put("RightHandPinky2", "r_pinky2");
        dict.put("RightHandPinky3", "r_pinky3");
        dict.put("RightHandPinky4", "r_pinky4");

        if (characterName.equalsIgnoreCase("Poppy")) {
            dict.put("Spine", "vl1");//vl5
            dict.put("Spine1", "vt11");// ************* vt12


        } else {
            dict.put("Spine", "vl5");//vl5
            dict.put("Spine1", "vt12");// ************* vt12
        }

        dict.put("Spine2", "vt6");
        dict.put("Spine3", "vt1");


        dict.put("LeftUpLeg", "l_hip");
        dict.put("LeftHip", "l_hip");
        dict.put("LeftLowLeg", "l_knee");

        dict.put("LeftLeg", "l_knee");
        dict.put("LeftKnee", "l_knee");
        dict.put("LeftFoot", "l_ankle");
        dict.put("LeftAnkle", "l_ankle");
        dict.put("LeftToe", "l_metatarsal");
        dict.put("LeftFootHeel", "l_metatarsal");
// l_subtalar
        dict.put("RightUpLeg", "r_hip");
        dict.put("RightHip", "r_hip");
        dict.put("RightLeg", "r_knee");
        dict.put("RightLowLeg", "r_knee");
        dict.put("RightKnee", "r_knee");
        dict.put("RightFoot", "r_ankle");
        dict.put("RightAnkle", "r_ankle");
        dict.put("RightToe", "r_metatarsal");
        dict.put("RightFootHeel", "r_metatarsal");


        // For Tardis bvh files:
        dict.put("l_hip", "l_hip");
        dict.put("l_knee", "l_knee");
        dict.put("l_ankle", "l_ankle");
        dict.put("l_subtalar", "l_subtalar");

        dict.put("r_hip", "r_hip");
        dict.put("r_knee", "r_knee");
        dict.put("r_ankle", "r_ankle");
        dict.put("r_subtalar", "r_subtalar");

        dict.put("vl5", "vl5");
        dict.put("vl3", "vl3");
        dict.put("vl1", "vl1");
        dict.put("vt10", "vt10");
        dict.put("vc4", "vc4");
        dict.put("vc2", "vc2");

        dict.put("l_sternoclavicular", "l_sternoclavicular");
        dict.put("l_shoulder", "l_shoulder");
        dict.put("l_elbow", "l_elbow");
        dict.put("l_wrist", "l_wrist");

        dict.put("r_sternoclavicular", "r_sternoclavicular");
        dict.put("r_shoulder", "r_shoulder");
        dict.put("r_elbow", "r_elbow");
        dict.put("r_wrist", "r_wrist");

    }
}
