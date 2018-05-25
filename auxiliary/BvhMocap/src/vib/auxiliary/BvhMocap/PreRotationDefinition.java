/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.BvhMocap;

import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author fourati
 */


public class PreRotationDefinition // For TARDIS Project 
{
    public String filename;// file that contains the pre-rotation of some specific joints
    private HashMap<String,Quaternion> JointsPreRotation = null;
    
    public PreRotationDefinition(String fname)
    {
        filename=fname;
        JointsPreRotation = new HashMap<String,Quaternion>();
    }
    
    public HashMap<String,Quaternion>  GetJointsPreRotation()
    {
        return JointsPreRotation;
    }
    public void SetJointsPreRotation() throws IOException
    {
        BufferedReader br1 = BvhReader.ReadFile(filename);
        String line;
        line=br1.readLine();// Joint	Pre-rotations in Maya:		
        line=br1.readLine(); // Orientation order
        BvhReader bvhreader=new BvhReader();
        line=br1.readLine();
        int      EulerAngleOrder=102; // 18/09
        int ix = EulerAngleOrder / 100;
        int iy = (EulerAngleOrder % 100) / 10;
        int iz = (EulerAngleOrder % 100) % 10;
        while (line!=null)
        {
            
            line = bvhreader.SpaceRegularization(line);
            
            String JointName=line.split(" ")[0];
            
            float rx=Float.parseFloat(line.split(" ")[1+ix]);
            float ry=Float.parseFloat(line.split(" ")[1+iy]);
            float rz=Float.parseFloat(line.split(" ")[1+iz]);
            
          //  System.out.println(JointName+"  "+ rx+" "+ry+" "+rz);
         //    System.out.println(line);
           JointsPreRotation.put(JointName,bvhreader.JointQuaternion(rx, ry, rz, EulerAngleOrder).inverse()); // 

         //  System.out.println("New rotation of "+JointName+ "  "+Bvh.RTOD*JointsPreRotation.get(JointName).angle()+"  "+JointsPreRotation.get(JointName).axis())     ;
        
            line=br1.readLine();
            //  System.out.println(line);
            
        }
        
        
        
     //br1.close();   
     // br1.reset();   
    }
    
    public  static void main(String[] args) throws  IOException
    {
        String filename="C:\\Users\\fourati\\Desktop\\Pre Rotations for Maya T-Pose.txt";
        PreRotationDefinition prerot=new PreRotationDefinition(filename);
        prerot.SetJointsPreRotation();
        
        
    }
    
    
    
}
