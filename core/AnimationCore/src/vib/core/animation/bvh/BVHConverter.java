package vib.core.animation.bvh;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import vib.core.animation.Frame;
import vib.core.animation.Skeleton;
import vib.core.animation.mocap.MotionSequence;
import vib.core.animation.mocap.SequenceConverter;

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
