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
package greta.core.animation.bvh;

import greta.core.animation.Frame;
import greta.core.animation.Joint;
import greta.core.animation.Skeleton;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Mathieu
 */
public class SkeletonToBVH {

    private ArrayList<String> jointTypeOrder;

    public SkeletonToBVH()
    {
        jointTypeOrder = new ArrayList<String>();
    }

    public String frame2BVHFrameString(Frame fr)
    {
        String line ="\n";
        for(int i=0;i<jointTypeOrder.size();i++)
        {
            String jt = jointTypeOrder.get(i);
            if(i == 0)
            {
                Vec3d tran = fr.getRootTranslation();
                line += tran.x()+" "+tran.y() + " " + tran.z() +" "; //positions root
            }
            Quaternion r = fr.getRotation(jt);
            Vec3d angle = r.getEulerAngleXYZByAngle();
            line += angle.z() + " ";
            line += angle.y() + " ";
            line += angle.x() + " ";
        }

        /*for(int i=0;i<bapfr.getAnimationParametersList().size();i++)
        {
            line+=bapfr.getAnimationParameter(i).getDegreeValue()+" ";
        }*/
        return line;
    }

    public String writeBVHHeaderFromSkeleton(Skeleton skeleton)
    {
        Joint root = skeleton.getJoint(0);
        if(root == null) System.out.println("SkeletonToBVH: root deos not exist" );
        String bvhfile = "HIERARCHY\n";
        //header
        //root = getskeletonroot
        bvhfile+="ROOT "+root.getName()+"\n{\n";
        jointTypeOrder.clear();
        bvhfile+=readJoint(root,skeleton,true,1);
        bvhfile+="}\nMOTION\n";
        return bvhfile;
    }

    private String readJoint(Joint jnt, Skeleton skeleton, boolean rootJoint,int indentLevel)
    {
        jointTypeOrder.add(jnt.getName());

        String jointLines="";
        jointLines+=indent("OFFSET "+
                jnt.getLocalPosition().x()+" "+
                jnt.getLocalPosition().y()+" "+
                jnt.getLocalPosition().z()+"\n",indentLevel);
        jointLines+=indent("CHANNELS ",indentLevel);
        if(rootJoint)
        {
            jointLines+="6 Xposition Yposition Zposition Zrotation Yrotation Xrotation\n";
        }
        else
        {
            jointLines+="3 Zrotation Yrotation Xrotation\n";
        }

        boolean removeFakeEndSites =false;
        for(int jid : jnt.getChildren())
        {
            Joint j = skeleton.getJoint(jid);
            if(j.getChildren().isEmpty() && jnt.getChildren().size()>1)
            {
                removeFakeEndSites = true;
            }
        }


        for(int jid : jnt.getChildren())
        {
            Joint j = skeleton.getJoint(jid);
            if(j.getChildren().isEmpty())
            {
                if(!removeFakeEndSites)
                {
                    jointLines+= indent("End Site"+"\n",indentLevel);
                    jointLines+= indent("{\n",indentLevel);
                    jointLines+=readEndSite(j,indentLevel+1);
                    jointLines+= indent("}\n",indentLevel);
                }
            }
            else
            {
                jointLines+=indent("JOINT "+j.getName()+"\n",indentLevel);
                jointLines+= indent("{\n",indentLevel);
                jointLines+=readJoint(j, skeleton,false,indentLevel+1);
                jointLines+= indent("}\n",indentLevel);
            }
        }
        return jointLines;
    }

    private String readEndSite(Joint jnt, int indentLevel)
    {
        String jointLines=indent("OFFSET "+
                jnt.getLocalPosition().x()+" "+
                jnt.getLocalPosition().y()+" "+
                jnt.getLocalPosition().z()+"\n",indentLevel);
        return jointLines;
    }

    private String indent(String s,int indentLevel)
    {
        String indented="";
        int i=0;while(i<indentLevel){indented+="    ";i++;}
        return indented+s;
    }
}
