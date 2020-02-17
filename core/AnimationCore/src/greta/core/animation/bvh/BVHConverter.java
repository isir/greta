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
                String filepath = "C:\\Users\\Jing\\Documents\\bvhfiles";
                if(args.length > 0){
                    filepath = args[0];
                }
                
                File dir = new File(filepath);
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
                    String filename = file.getName();
                    BVHConverter.convert(path, filename);
                    System.out.print("convert : " + filename);
                    //return;
                }
            }
        });
    }
}
