package vib.auxiliary.BvhMocap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import vib.core.animation.bvh.BVHAnimation;
import vib.core.animation.bvh.BVHChannel;
import vib.core.animation.bvh.BVHFrame;
import vib.core.animation.bvh.BVHLoader;
import vib.core.animation.mpeg4.bap.*;
import vib.core.animation.mpeg4.bap.file.BAPFileWriter;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */


public class BVHToBAP  implements BAPFramesEmitter {
    
    ArrayList<BAPFramesPerformer> _bapframesPerformer = new ArrayList<BAPFramesPerformer>();
    @Override
    public void addBAPFramesPerformer(BAPFramesPerformer bapfp) {
        _bapframesPerformer.add(bapfp);
    }

    @Override
    public void removeBAPFramesPerformer(BAPFramesPerformer bapfp) {
        _bapframesPerformer.remove(bapfp);
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        BVHLoader bvhloader = new BVHLoader();
        //bvhloader.load("C:\\Users\\Jing\\Desktop\\greta_svn\\vib\\bin\\Examples\\BvhMocap\\fast_gesture.bvh");
        bvhloader.load("K:\\Yu_Bvh_Files\\test\\CarolineSd20.bvh");

        BVHAnimation ani = bvhloader.getAnimation();
        ArrayList<BAPFrame> bapframes = new ArrayList<BAPFrame>();
        ArrayList<BVHFrame> frames = ani.getSequence();
        int i = 0;
        for(BVHFrame frame: frames){
            BAPFrame bf = new BAPFrame(i);i++;
            for(String name: frame.getValues().keySet()){
                BVHChannel channel = frame.getValues().get(name);
                Quaternion q = channel.convert();
                Vec3d angle = q.getEulerAngleXYZ();
                JointType joint = JointType.get(name);
                BAPType z = joint.rotationZ;
                BAPType y = joint.rotationY;
                BAPType x = joint.rotationX;
                bf.setRadianValue(z, angle.z());
                bf.setRadianValue(y, angle.y());
                bf.setRadianValue(x, angle.x());    
            }
            bapframes.add(bf);
        }
        BAPFileWriter writer = new BAPFileWriter();
        ID id = IDProvider.createID("BVH" + System.currentTimeMillis());
        writer.performBAPFrames(bapframes, id);
    }
}
