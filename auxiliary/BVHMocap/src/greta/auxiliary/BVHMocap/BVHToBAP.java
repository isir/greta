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
