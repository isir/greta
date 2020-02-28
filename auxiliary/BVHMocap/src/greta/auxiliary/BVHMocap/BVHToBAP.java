/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.BVHMocap;

import greta.core.animation.bvh.BVHAnimation;
import greta.core.animation.bvh.BVHChannel;
import greta.core.animation.bvh.BVHFrame;
import greta.core.animation.bvh.BVHLoader;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.bap.JointType;
import greta.core.animation.mpeg4.bap.file.BAPFileWriter;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class BVHToBAP  implements BAPFrameEmitter {

    ArrayList<BAPFramePerformer> _bapFramePerformer = new ArrayList<BAPFramePerformer>();
    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapfp) {
        _bapFramePerformer.add(bapfp);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapfp) {
        _bapFramePerformer.remove(bapfp);
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        BVHLoader bvhloader = new BVHLoader();
        //bvhloader.load("C:\\Users\\Jing\\Desktop\\greta_svn\\bin\\Examples\\BVHMocap\\fast_gesture.bvh");
        bvhloader.load("K:\\Yu_BVH_Files\\test\\CarolineSd20.bvh");

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
