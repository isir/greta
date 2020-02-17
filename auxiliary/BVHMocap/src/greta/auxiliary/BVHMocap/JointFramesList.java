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

import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Nesrine Fourati
 */
public class JointFramesList
{
     protected ArrayList<JointFrame> JointFrames=new ArrayList<JointFrame>();
     protected String JointName="";
     protected Vec3d NeutralPosition=new Vec3d(); // the origine is the humanoid root

     public JointFramesList(String jname)
     {
         JointName=jname;
     }
     public JointFramesList(String jname, Vec3d neutral_position)
     {
         JointName=jname;
         NeutralPosition=neutral_position;

     }

    public JointFramesList()
    {

    }
    public void AddJointFrame(JointFrame jointframe)
    {
        JointFrames.add(jointframe);
    }
    public ArrayList<JointFrame> GetJointFrames()
    {
        return JointFrames;
    }
    public Vec3d GetNeutralPosition()
    {   /* frame number*/
        return NeutralPosition;
    }
    public int GetSize()
    {   /* frame number*/
        return JointFrames.size();
    }
        public void SetJointFrameAt(int index,JointFrame jointframe)
    {
        JointFrames.set(index, jointframe);
    }
    public String GetJointName()
    {
        return JointName;
    }
    public JointFrame GetJointFrameAt(int frame)
    {
        return JointFrames.get(frame);
    }
   public String GetDicionaryName()
   {
        Dictionary  dictionary = new Dictionary();
        dictionary.Initialize();
       return (String) (dictionary.GetDict()).get(JointName);
   }
}
