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
