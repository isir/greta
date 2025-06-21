/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.BVHMocap;

import greta.core.animation.Joint;
import greta.core.animation.Skeleton;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Nesrine Fourati
 */
public class AllJointFramesTable {

    // For each joint => list of jointframe
    private ArrayList<JointFramesList> NameJointFrames = new ArrayList<JointFramesList>();

    public AllJointFramesTable(ArrayList<JointFramesList> JointFtable, int startf, int endf) {   //
        NameJointFrames = GetMotionSegment(JointFtable, startf, endf);
    }

    public AllJointFramesTable(Skeleton skeleton) {   //Initialize JointFrameTable with the neutral positions and the names of all the joints using the skeleton as input
        // the joint names correspond to the names used in the motion capture file
        int nbjoint = skeleton.getJoints().size();

        Joint joint = skeleton.getJoint(0);
        joint.reset();
        joint.update();

        for (int j = 0; j < nbjoint; j++) {
            String joint_name = skeleton.getJoint(j).getName();
            // System.out.println(joint_name);
            Vec3d neutral_position = skeleton.getJoint(j).getWorldPosition();
            NameJointFrames.add(new JointFramesList(joint_name, neutral_position));
        }
    }

    public AllJointFramesTable(ArrayList<Motion> motionlist) {   //Initialize JointFrameTable with the names of all the joints
        int nbjoint = motionlist.size();
        for (int j = 0; j < nbjoint; j++) {
            String joint_name = motionlist.get(j).getcle();
            NameJointFrames.add(new JointFramesList(joint_name));
        }
    }

    public String[] GetJointNamesList() {
        String[] names = new String[NameJointFrames.size()];
        // System.out.println(NameJointFrames.size()+ "  "+names.length);
        for (int i = 0; i < names.length; i++) {
            names[i] = NameJointFrames.get(i).JointName;
        }

        return names;
    }

    public int GetJointId(String JointName) {
        String[] AllJointsName = GetJointNamesList();

        int jointID = 0;

        while (!(AllJointsName[jointID].equalsIgnoreCase(JointName))) {

            jointID = jointID + 1;
        }
        return jointID;
    }

    public void AddJointFrameAt(int joint_index, JointFrame jointframe) {
        NameJointFrames.get(joint_index).AddJointFrame(jointframe);
    }

    public ArrayList<JointFramesList> GetJointFramesTable() {
        return NameJointFrames;
    }

    public int GetNbFrame() {
        return NameJointFrames.get(0).GetSize();
    }

    public void SubtractLocalRootPositions() {
        int nbframe = GetNbFrame();
        JointFramesList CjointFrames;
        JointFramesList HipsFrames = NameJointFrames.get(0);
        ArrayList<Vec3d> LocalRootPositions = new ArrayList<Vec3d>();
        for (int i = 0; i < nbframe; i++)//nbframe
        {
            LocalRootPositions.add(HipsFrames.GetJointFrameAt(i).PositionInSkeletonRootCoordSystem);// NO
        }

        for (int j = 1; j < NameJointFrames.size(); j++)
        {

            CjointFrames = NameJointFrames.get(j);

            //  subtract the global root position for all the frames of all the joints except the root
            for (int f = 0; f < nbframe; f++)
            {
                JointFrame currentJointFrame = CjointFrames.GetJointFrameAt(f);
                Vec3d newLocalPosition = currentJointFrame.GetPositionInSkeletonRootCoordSystem();
                newLocalPosition.setX(newLocalPosition.x() - (LocalRootPositions.get(f).x()));
                newLocalPosition.setY(newLocalPosition.y() - (LocalRootPositions.get(f).y()));
                newLocalPosition.setZ(newLocalPosition.z() - (LocalRootPositions.get(f).z()));
                currentJointFrame.SetSkeletonRelatedPosition(newLocalPosition);
                CjointFrames.SetJointFrameAt(f, currentJointFrame);

            }

            SetJointFramesListAt(j, CjointFrames);
        }
        // sybtract root position from root <=> new Vec3d()
        for (int f = 0; f < nbframe; f++)//nbframe
        {
               JointFrame currentHipsFrame = HipsFrames.GetJointFrameAt(f);

                currentHipsFrame.SetSkeletonRelatedPosition(new Vec3d());
                HipsFrames.SetJointFrameAt(f, currentHipsFrame);
        }
        SetJointFramesListAt(0, HipsFrames);
    }

    public JointFramesList GetJFListfromDictName(String dictJName)
    {// Warning; One dictName can correspond to one or many possible joint name
        int i = 0;
        while (!dictJName.equalsIgnoreCase(NameJointFrames.get(i).GetDicionaryName())) {
            i = i + 1;
        }
        //System.out.println(NameJointFrames.get(i).JointName);
        return NameJointFrames.get(i);
    }

    public JointFramesList GetJFListfromJointName(String dictJName)
    {// Warning; One dictName can correspond to one or many possible joint name
        int i = 0;
        while (!dictJName.equalsIgnoreCase(NameJointFrames.get(i).GetJointName())) {
            i = i + 1;
        }
        //System.out.println(NameJointFrames.get(i).JointName);
        return NameJointFrames.get(i);
    }
    public void SetJointFramesListAt(int index, JointFramesList JFList) {
        NameJointFrames.set(index, JFList);
    }

    public ArrayList<JointFramesList> GetMotionSegment(int startf, int endf) {
        JointFramesList CurrentjointFList;
        JointFramesList NewjointFList = new JointFramesList();

        ArrayList<JointFramesList> NewNameJointFrames = new ArrayList<JointFramesList>();
        for (int i = 0; i < NewNameJointFrames.size(); i++) {
            CurrentjointFList = NameJointFrames.get(i);
            for (int j = startf; j < endf; j++) {
                NewjointFList.AddJointFrame(CurrentjointFList.GetJointFrameAt(j));
            }
            NewNameJointFrames.add(NewjointFList);
        }
        return NewNameJointFrames;
    }

    public ArrayList<JointFramesList> GetMotionSegment(ArrayList<JointFramesList> JFtable, int startf, int endf) {
        JointFramesList CurrentjointFrames;
        JointFramesList NewjointFrames;
        ArrayList<JointFramesList> NewjointFramesTable = new ArrayList<JointFramesList>();


        //System.out.println("Head 3D position in CurrentjointFList _ frame ="+ startf+ "  "+JFtable.get(6).JointFrames.get(startf).PositionInSkeletonRootCoordSystem);
        for (int i = 0; i < JFtable.size(); i++)// joints
        {
            CurrentjointFrames = JFtable.get(i);
            NewjointFrames = new JointFramesList(CurrentjointFrames.JointName, CurrentjointFrames.NeutralPosition);
            for (int j = startf; j < endf; j++) // frames
            {
                NewjointFrames.AddJointFrame(CurrentjointFrames.GetJointFrameAt(j));
            }
            NewjointFramesTable.add(NewjointFrames);
        }
        // System.out.println("Head 3D position in NewNameJointFrames  _ frame ="+ 0+ "  "+NewjointFramesTable.get(6).JointFrames.get(0).PositionInSkeletonRootCoordSystem);
        return NewjointFramesTable;
    }
}
