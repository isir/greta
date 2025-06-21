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

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author Nesrine Fourati
 */
public class BVHMocap {

    /**
     * @param args the command line arguments
     */
            public static File LoadFile()
    {
            FileSystemView vueSysteme = FileSystemView.getFileSystemView();
            File defaut = vueSysteme.getDefaultDirectory();

            JFileChooser defautChooser = new JFileChooser(defaut);
            defautChooser.showOpenDialog(null);
            File file = defautChooser.getSelectedFile();
            return file;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        // TODO code application logic here

        // Generate a BAP file from a bvh file

       // File file=LoadFile();
       // String bvhFilePath= file.getPath();
        //System.out.println(bvhFilePath);
        //String bvhFileName=file.getName();
        String bvhpath="C:\\Users\\fourati\\Documents\\MotionCapture Database\\Janina\\Anger\\Simple Walk\\Action_repetitions\\";
        String bvhname="Ag1SW_Janina";
//
        BVHReader bvhread = new BVHReader(bvhpath + bvhname+".bvh");
        BVH bvh = bvhread.JFTableBasedBVHCreator();
        AllJointFramesTable jointframestable = bvh.GetAllJointFramesTable();
        //System.out.println(jointframestable.GetNbFrame());
        SkeletonRelatedPositions SekeltonRelatedPos=new SkeletonRelatedPositions(jointframestable);
        SekeltonRelatedPos.CreateSkeletonRelatedPositions();
        AllJointFramesTable newjointframestable=SekeltonRelatedPos.GetAllJointFramesTable();
        Quaternion q=new Quaternion(new Vec3d(0,1,0),(float) -0.5);
        //System.out.println(q.angle()+"   "+q.axis().get(1));
        System.out.println(newjointframestable.GetJFListfromDictName("r_acromioclavicular").GetJointFrameAt(0).PositionInSkeletonRootCoordSystem);


//        ArrayList<BAPFrame> bapframes=bvhread.BVHToBAPFrames();
//        BapAnimationConverter converter=new BapAnimationConverter();
//        BAPFrame bapframe=bapframes.get(bapframes.size()-1);


        //converter.BapOutput(bapframes, "C:/Users/fourati/Documents/Bap files/", bvhFileName.split("[.]")[0], "____.bap", "0.0 xxx 25 1\n");

       // BVH bvh=bvhread.BVHCreator();
//       ArrayList<Motion> molist= bvh.GetMotionList();
//      molist.get(9).DisplayEulerAngle(1);
        //bvh.JointsDisplay();

      // bvh.FillJointFramesTable();
//       AllJointFramesTable table=bvh.GetAllJointFramesTable();
//       ArrayList<JointFramesList> list=table.GetJointFramesTable();
//       //String[] Jointnames=table.GetJointNamesList();
//       System.out.println(list.get(0).JointName);


    }
}
