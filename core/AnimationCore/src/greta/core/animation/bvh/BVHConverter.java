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
import greta.core.animation.mocap.SequenceConverter;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class BVHConverter {

    public static void convert(String path, String Filename) {
        BVHLoader bvhloader = new BVHLoader();
        bvhloader.load(path+"/"+Filename);
        BVHAnimation ani = bvhloader.getAnimation();
        ArrayList<Frame> frames = SequenceConverter.convertFromBVHFramesToFrames(ani.getSequence());
        Skeleton sk = ani.getSkeleton();

        BVHWriter writer = new BVHWriter(sk);
        String p = path+"/convertedZYX_" + Filename;
        writer.writeFile(frames, ani.getFrameTime(), p);
        System.out.println("to : " + p);
    }

    public static void main(final String args[]) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                String filePath = "C:\\Users\\Jing\\Documents\\bvhfiles";
                if(args.length > 0){
                    filePath = args[0];
                }

                File dir = new File(filePath);
                File[] files = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".bvh");
                    }
                });
                System.out.println("Files number: " +files.length);
                int i = 0;
                for (File file : files) {
                    System.out.println(i);
                    String path = file.getParent();
                    String fileName = file.getName();
                    BVHConverter.convert(path, fileName);
                    System.out.print("convert : " + fileName);
                    //return;
                }
            }
        });
    }
}
