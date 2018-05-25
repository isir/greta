/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.BvhMocap;
import vib.core.util.math.Vec3d;
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
