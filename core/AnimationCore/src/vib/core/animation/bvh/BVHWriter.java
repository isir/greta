/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.bvh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import vib.core.animation.Frame;
import vib.core.animation.Skeleton;
import vib.core.util.Constants;
import vib.core.util.IniManager;
import vib.core.util.id.ID;
import vib.core.util.log.Logs;

/**
 *
 * @author Jing
 */
public class BVHWriter {
    
    private String path;
    private Skeleton skelet;
    private String bvhheader;
    private SkeletonToBVH bap2bvh;
    
    protected String filename;
    private boolean fileNameExternallyDefined=false;
    
    public BVHWriter(Skeleton sk)
    {
        skelet = sk;
        bap2bvh = new SkeletonToBVH();
        bvhheader = bap2bvh.writeBVHHeaderFromSkeleton(skelet);
    }

    public void setBVHFilename(String bvh)
    {
        filename=bvh;
        fileNameExternallyDefined=true;
    }
    
    
    public void writeFile(List<Frame> frames,double frametime, String fileName) {
        try {
            java.io.FileWriter fos;
            if (new File(fileName).exists()) {
                fos = new java.io.FileWriter(fileName, true);

                //TODO rewrite header

            } else {
                fos = new java.io.FileWriter(fileName);
                if(bvhheader=="")
                {
                    bvhheader = bap2bvh.writeBVHHeaderFromSkeleton(skelet);
                }
                fos.write(bvhheader);
            }
            fos.write("Frames: "+frames.size()+"\n"+
                    "Frame Time: "+frametime);
            System.out.println("BVHWriter: Frames: "+frames.size());
            
            for (Frame frame : frames) {
                String bvhframe = bap2bvh.frame2BVHFrameString(frame);
                fos.write(bvhframe);
            }

            fos.close();
        } catch (Exception ignored2) {
            Logs.warning("Error saving file: " + ignored2);
        }
    }
}


