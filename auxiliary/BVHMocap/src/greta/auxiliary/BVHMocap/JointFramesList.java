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
