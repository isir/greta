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

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author Nesrine Fourati
 */
public class SkeletonRelatedPositions { // This class measure skeleton related positions (or Position In Skeleton Root Coordinate System) from global positions in the joint frame table
    // This process must be offline. We need first to measure all the global positions (when considering the root rotation).

    protected AllJointFramesTable jointframestable = null;

    public SkeletonRelatedPositions(AllJointFramesTable jftable) {
        jointframestable = jftable;
    }

    public AllJointFramesTable GetAllJointFramesTable() {
        return jointframestable;
    }

    public double[] GetAllRootPositions() {
        int nbframe = jointframestable.GetNbFrame();
        double[] RootPositions = new double[nbframe];
        // find root Frames
        JointFramesList HipsFrames = jointframestable.GetJFListfromDictName("HumanoidRoot");
        //System.out.println(HipsFrames.GetJointFrameAt(0).PositionInTheWorld);
        // find Left and Right Hip Frames
        //JointFramesList LeftHipFrames=jointframestable.GetJFListfromDictName("l_hip");
        // JointFramesList RightHipFrames=jointframestable.GetJFListfromDictName("r_hip");
        // find the required vertical rotation from hips vector (from right to left) for each frame
        // ArrayList<Quaternion> RequiredAngle=new ArrayList<Quaternion>();
        for (int i = 0; i < nbframe; i++)//nbframe
        {
            RootPositions[i] = (HipsFrames.GetJointFrameAt(i).PositionInTheWorld).x();// NOOOOOOOOOOO JointFrames.get(i).GetWorldPosition()
            // RequiredAngle.add(GetRequiredVerticalRotation( RightHipFrames.GetJointFrameAt(i),  LeftHipFrames.GetJointFrameAt(i)));

        }
        return RootPositions;
    }

    public void RotateRootAnglesThrough(Quaternion RequiredAngle)
    {
        JointFramesList RootJFL=jointframestable.GetJointFramesTable().get(0);
        int nbframe = jointframestable.GetNbFrame();
         for (int f = 0; f < nbframe; f++)
         {
                     JointFrame RootFrame = RootJFL.GetJointFrameAt(f);
        Quaternion q = Quaternion.multiplication(RootFrame.GetRotation(), RequiredAngle);
//        if (f < 100)
//        {
//            System.out.println("old root rotation   " + RootFrame.GetRotation());
//        }
        RootFrame.SetRotation(q);
//        if (f < 100)
//        {
//            System.out.println("new root rotation   " + RootFrame.GetRotation());
//        }
         RootJFL.SetJointFrameAt(f, RootFrame);
         }
jointframestable.SetJointFramesListAt(0, RootJFL);
    }

    public void RotateAxesThrough(Quaternion RequiredAngle) {
        int nbframe = jointframestable.GetNbFrame();
        for (int j = 0; j < jointframestable.GetJointFramesTable().size(); j++) {
            JointFramesList CjointFrames = jointframestable.GetJointFramesTable().get(j);
            // First rotate all the world positions of this joint according to the required angle
            for (int f = 0; f < nbframe; f++) {

                JointFrame currentJointFrame = CjointFrames.GetJointFrameAt(f);
                Vec3d newSkeletonRelatedPos = currentJointFrame.GetWorldPosition();
                newSkeletonRelatedPos = Quaternion.multiplication(RequiredAngle, newSkeletonRelatedPos);
                currentJointFrame.SetSkeletonRelatedPosition(newSkeletonRelatedPos);

                CjointFrames.SetJointFrameAt(f, currentJointFrame);
            }
            jointframestable.SetJointFramesListAt(j, CjointFrames);
        }
    }

    public void CreateSkeletonRelatedPositions() {
        int nbframe = jointframestable.GetNbFrame();
        JointFramesList LeftHipFrames = jointframestable.GetJFListfromDictName("l_hip");
        JointFramesList RightHipFrames = jointframestable.GetJFListfromDictName("r_hip");
        JointFramesList HipsFrames = jointframestable.GetJFListfromDictName("HumanoidRoot");

        // find the required vertical rotation from hips vector (from right to left) for each frame
        ArrayList<Quaternion> RequiredAngle = new ArrayList<Quaternion>();
        for (int i = 0; i < nbframe; i++)//nbframe
        {
            RequiredAngle.add(GetRequiredVerticalRotation(RightHipFrames.GetJointFrameAt(i), LeftHipFrames.GetJointFrameAt(i), HipsFrames.GetJointFrameAt(i)));
        }
        for (int j = 1; j < jointframestable.GetJointFramesTable().size(); j++) {
            JointFramesList CjointFrames = jointframestable.GetJointFramesTable().get(j);
            //
            // First rotate all the world positions of this joint according to the required angle
            for (int f = 0; f < nbframe; f++) {
                Vec3d RootPosition = HipsFrames.GetJointFrameAt(f).GetWorldPosition();
                JointFrame currentJointFrame = CjointFrames.GetJointFrameAt(f);
                Vec3d newSkeletonRelatedPos = new Vec3d(currentJointFrame.GetWorldPosition(), RootPosition);
                newSkeletonRelatedPos = Quaternion.multiplication(RequiredAngle.get(f), newSkeletonRelatedPos);
                currentJointFrame.SetSkeletonRelatedPosition(newSkeletonRelatedPos);
                CjointFrames.SetJointFrameAt(f, currentJointFrame);

            }
            jointframestable.SetJointFramesListAt(j, CjointFrames);

        }
        //jointframestable.SubtractLocalRootPositions();

    }

    /**
     *
     * @return the Trigonometric functions of the angle defined between the vector (position1-position2) and the principal axis
     */
    public Vector FindVectorOrientation(Vec3d position1, Vec3d position2, int first_AxisIndex, int second_AxisIndex, int principal_AxisIndex) { // the orientation that we want to find is formed between :
        //     - the vector defined respectively by the positions1 and positions2 (from the first to the second point). This vector is contained in the plan defined by first_axis and second_axis
        //     - the principal axis: which is the first or the second axis
        float teta = 0;
        Vector TrigonometricFunctions = new Vector();
        Vec3d vector = new Vec3d(position2.x() - position1.x(), position2.y() - position1.y(), position2.z() - position1.z());
        int secondary_AxisIndex = 0;
        if (principal_AxisIndex == first_AxisIndex) {
            secondary_AxisIndex = second_AxisIndex;
        } else {
            secondary_AxisIndex = first_AxisIndex;
        }
        float vector_norm = (float) Math.sqrt(Math.pow(vector.x(), 2) + Math.pow(vector.y(), 2) + Math.pow(vector.z(), 2));
        double cosineTeta = (vector.get(principal_AxisIndex) / vector_norm);
        double sinusTeta = (vector.get(secondary_AxisIndex) / vector_norm);

        double tangteta = sinusTeta / cosineTeta;
        teta = (float) Math.atan(tangteta);
        //System.out.println("teta  "+teta+ "  tang  "+tangteta+ "  cos  "+cosineTeta+ "  sin  "+sinusTeta);
        TrigonometricFunctions.add((float) teta);
        TrigonometricFunctions.add(tangteta);
        TrigonometricFunctions.add(cosineTeta);
        TrigonometricFunctions.add(sinusTeta);
        return TrigonometricFunctions;
    }

    /**
     * This function find the required rotation in a particular frame that allow the change the Cartesian Coordinate System of all the 3D joint positions in a skeleton
     * @param joint1: is the Right JointFrame that define the first point of the vector (like Right Shoulder or Right Hip)
     * @param joint2: is the Left JointFrame that define the second point of the vector (like Left Shoulder or Left Hip)
     * @param Root: is the RootFrame
     * @return the required rotation to change from a global axes to Skeleton Related Axes
     */
    public Quaternion GetRequiredVerticalRotation(JointFrame joint1, JointFrame joint2, JointFrame Root) { // the required vertical rotation  will be used to rotate all the skeleton in order to change the Cartesian Coordinate System
        Vec3d global_position1 = new Vec3d(joint1.GetWorldPosition(), Root.GetWorldPosition());
        Vec3d global_position2 = new Vec3d(joint2.GetWorldPosition(), Root.GetWorldPosition());
//System.out.println("Right pos x "+ global_position1.x()+ "  Left pos x "+global_position2.x());
        float VerticalRotationAngle = (Float) (FindVectorOrientation(global_position1, global_position2, 0, 2, 0).get(0));// using Hips or Shoulder vector contained in horizontal plan
        Quaternion RequiredRotation = new Quaternion();
        RequiredRotation.setAxisAngle(new Vec3d(0, 1, 0), VerticalRotationAngle);
        Vec3d newglobal_position1 = Quaternion.multiplication(RequiredRotation, global_position1);
        Vec3d newglobal_position2 = Quaternion.multiplication(RequiredRotation, global_position2);
        // System.out.println("New Right pos x "+ newglobal_position1.x()+ "  New Left pos x "+newglobal_position2.x());
        float Xcomponent = (float) Math.sqrt(Math.pow(newglobal_position1.x() - newglobal_position2.x(), 2));
        float Ycomponent = (float) Math.sqrt(Math.pow(newglobal_position1.y() - newglobal_position2.y(), 2));
        float Zcomponent = (float) Math.sqrt(Math.pow(newglobal_position1.z() - newglobal_position2.z(), 2));
        Vec3d newVectorComponents = new Vec3d(Xcomponent, Ycomponent, Zcomponent);
        // System.out.println("Vector components  "+Xcomponent+ "   "+Ycomponent+"   "+Zcomponent);

        // check whether this is the right rotation, otherwise use the inverse
        // check the three components of the new vector
        Vector index = new Vector();
        for (int i = 0; i < 3; i++) {
            if (newVectorComponents.get(i) <= 0.00001 && newVectorComponents.get(i) >= -0.00001) {
                index.add(i);
            }
        }
        if (index.contains(2)) { // System.out.println("Original Orientation");
            //return RequiredRotation;
        } else {
            RequiredRotation.setAxisAngle(new Vec3d(0, 1, 0), (-1) * VerticalRotationAngle);
            newglobal_position1 = Quaternion.multiplication(RequiredRotation, global_position1);
            newglobal_position2 = Quaternion.multiplication(RequiredRotation, global_position2);
            //System.out.println("Opposite Orientation");
            //return RequiredRotation;
        }
        // Check the axes sign
        RequiredRotation = AxesSignCorrection(RequiredRotation, newglobal_position1, newglobal_position2);

        return RequiredRotation;
    }

    public Quaternion AxesSignCorrection(Quaternion RequiredRotation, Vec3d newglobal_position1, Vec3d newglobal_position2) {
        //System.out.println("Right pos x "+ newglobal_position1.x()+ "  Left pos x "+newglobal_position2.x());
        if (newglobal_position1.x() > 0 && newglobal_position2.x() < 0) {//because the first position corresponds to the right joint position, and the second to the left joint position
            // System.out.println("Change sign");
            return Quaternion.multiplication(RequiredRotation, new Quaternion(new Vec3d(0, 1, 0), (float) Math.PI));
        } else {
            return RequiredRotation;
        }
    }
}
