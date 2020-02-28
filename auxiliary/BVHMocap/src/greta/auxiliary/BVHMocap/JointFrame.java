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

import greta.core.animation.mpeg4.bap.BAP;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.Vector;

/**
 *
 * @author Nesrine Fourati
 */
public class JointFrame {
    /* Joint information at a particular frame*/
    private Quaternion rotation=new Quaternion();
    // add bap
    BAP CurrentBapX=new BAP();
     BAP CurrentBapY=new BAP();
      BAP CurrentBapZ=new BAP();
      //end add bap

    Vec3d EulerAngles=new Vec3d();
    Vec3d PositionInTheWorld=new Vec3d();//The coordinate system of the world is fixed, so the positions measured in this coordinate system are dependent on the skeleton root motion
    Vec3d PositionInSkeletonRootCoordSystem=new Vec3d();// the coordinate system of root skeleton is related the root motion, So the positions measured in this coordinate system are independent on the skeleton root motion
    private int frame;
    //private float time;
    private String BAPjoint_name;

//    public JointFrame(String jname)
//    {
//        joint_name=jname;
//    }
    public JointFrame( int f,Quaternion q)
    {
//        joint_name=jname;
        frame=f;
        rotation=q;
    }
//    public JointFrame( int f,float t,Quaternion q)
//    {
////      joint_name=jname;
//        frame=f;
//        rotation=q;
//        time=t;
//    }
    public JointFrame(int f,Quaternion q, Vec3d position)
    {
//        joint_name=jname;
        frame=f;
        rotation=q;
        PositionInTheWorld=position;
    }

        public JointFrame(int f,Quaternion q, Vec3d globalposition,Vec3d skeleton_related_position)
    {
//        joint_name=jname;
        frame=f;
        rotation=q;
        PositionInTheWorld=globalposition;
        PositionInSkeletonRootCoordSystem=skeleton_related_position;
    }

    public int GetFrame()
    {
        return frame;
    }

    public String GetBAPJointName()
    {
        return BAPjoint_name;
    }


    public void SetBAPJointName(String jointname)
    {
         BAPjoint_name=jointname;
    }

    public Quaternion GetRotation()
    {
        return rotation;
    }
        public Vec3d GetPositionInSkeletonRootCoordSystem()
    {
        return PositionInSkeletonRootCoordSystem;
    }
    public Vec3d GetWorldPosition()
    {
        return PositionInTheWorld;
    }

        public Vector GetBAPRotationXYZ()
    {
        Vector BAPRotationXYZ=new Vector();
        BAPRotationXYZ.addElement(CurrentBapX);
        BAPRotationXYZ.addElement(CurrentBapY);
        BAPRotationXYZ.addElement(CurrentBapZ);
        return BAPRotationXYZ;
    }
        public BAP GetBAPRotationX()
        {
            return CurrentBapX;
        }
                public BAP GetBAPRotationY()
        {
            return CurrentBapY;
        }
                        public BAP GetBAPRotationZ()
        {
            return CurrentBapZ;
        }

        public void SetRotation(Quaternion q)
    {
         rotation=q;
    }
    public void SetWorldPosition(Vec3d newWorldPosition)
    {
     PositionInTheWorld=newWorldPosition;
    }
    public void SetSkeletonRelatedPosition(Vec3d newSRPosition)
    {
     PositionInSkeletonRootCoordSystem=newSRPosition;
    }
}
