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
import greta.core.animation.Skeleton;
import greta.core.util.log.Logs;
import java.io.File;
import java.util.List;

/**
 *
 * @author Jing
 */
public class BVHWriter {

    private String path;
    private Skeleton skelet;
    private String bvhheader;
    private SkeletonToBVH bap2bvh;

    protected String fileName;
    private boolean fileNameExternallyDefined=false;

    public BVHWriter(Skeleton sk)
    {
        skelet = sk;
        bap2bvh = new SkeletonToBVH();
        bvhheader = bap2bvh.writeBVHHeaderFromSkeleton(skelet);
    }

    public void setBVHFilename(String bvh)
    {
        fileName=bvh;
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
