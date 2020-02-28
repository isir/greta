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
package greta.core.animation.performer;

import greta.core.animation.Frame;
import greta.core.animation.FrameSequence;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.bap.JointType;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.math.Noise;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public class BodyAnimationBAPFrameEmitter implements BAPFrameEmitter{

    BodyAnimationNoiseGenerator _bodyNoise = new BodyAnimationNoiseGenerator(CharacterManager.getStaticInstance());
    ArrayList<BAPFramePerformer> _bapFramePerformer = new ArrayList<BAPFramePerformer>();

    @Override
    public void addBAPFramePerformer(BAPFramePerformer performer) {
        if (performer != null) {
            _bapFramePerformer.add(performer);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer performer) {
        _bapFramePerformer.remove(performer);
    }

    public void updateFrameList(FrameSequence fs) {
        ArrayList<BAPFrame> bf = getBapFrames(fs);
        ID id = IDProvider.createID("BodyAnimationBAPFrameEmitter");
        for (int i = 0; i < _bapFramePerformer.size(); ++i) {
            BAPFramePerformer performer = _bapFramePerformer.get(i);
            performer.performBAPFrames(bf, id);
        }
    }

    public void updateFrameList(List<BAPFrame> frames) {
        ID id = IDProvider.createID("BodyAnimationBAPFrameEmitter");
        for (int i = 0; i < _bapFramePerformer.size(); ++i) {
            BAPFramePerformer performer = _bapFramePerformer.get(i);
            performer.performBAPFrames(frames, id);
        }
    }

    /**
     *
     * @param frames container the rotation info : quaternions
     * @return bap frames object
     */
    public ArrayList<BAPFrame> getBapFrames(FrameSequence fs) {
        ArrayList<BAPFrame> bapframes = new ArrayList<BAPFrame>();
        //int count = (int)(Timer.getTimeMillis()/ 40 );
        int first = (int) (fs.getStartTime() * Constants.FRAME_PER_SECOND + 0.5);
        for (Frame frame : fs.getSequence()) {
            int level = (int) (180.0);
            Quaternion noise = new Quaternion();
            double d = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
            double d1 = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
            double d2 = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
            noise.fromEulerXYZ((double) d / 8.0f, (double) d1 / 8.0f, (double) d2 / 8.0f);
            frame.accumulateRotation("vl5", noise);

            Quaternion noise3 = new Quaternion();
            d = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
            d1 = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
            d2 = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
            noise3.fromEulerXYZ((double) d / 15.0f, (double) d1 / 15.0f, (double) d2 / 15.0f);
            frame.accumulateRotation("vt10", noise3);

            Quaternion noise4 = new Quaternion();
            d = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
            d1 = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
            d2 = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
            noise4.fromEulerXYZ((double) d / 15.0f, (double) d1 / 15.0f, (double) d2 / 15.0f);
            frame.accumulateRotation("vt1", noise4);

            {
                Quaternion noise5 = new Quaternion();
                d = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
                noise5.fromEulerXYZ(0, 0, (double) d / 15.0f);
                frame.accumulateRotation("vc1", noise5);

                Quaternion noise6 = new Quaternion();
                d = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
                noise6.fromEulerXYZ((double) d / 15.0f, 0, 0);
                frame.accumulateRotation("vc2", noise6);

                Quaternion noise7 = new Quaternion();
                d = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
                noise7.fromEulerXYZ(0, (double) d / 15.0f, 0);
                frame.accumulateRotation("vc3", noise7);

                Quaternion noise2 = new Quaternion();
                d = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
                d1 = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
                d2 = Noise.noise((first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level, (first + Math.random() - level * 0.5) / level);
                noise2.fromEulerXYZ((double) d / 15.0f, (double) d1 / 15.0f, (double) d2 / 15.0f);
                frame.accumulateRotation("vc5", noise2);
            }

            BAPFrame bapframe = getBapFrame(frame, first);
            first++;
            bapframes.add(bapframe);
        }
        return bapframes;
    }

    /**
     *
     * @param info
     * @param index
     * @return the {@code BAPFrame} at the specified index
     */
    public BAPFrame getBapFrame(Frame info, int index) {
        BAPFrame bf = new BAPFrame();
        bf.setFrameNumber(index);
        HashMap<String, Quaternion> results = info.getRotations();
        Iterator iterator = results.keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            Quaternion q = results.get(name);
            Vec3d angle = q.getEulerAngleXYZ();
            JointType joint = JointType.get(name);
            BAPType z = joint.rotationZ;
            BAPType y = joint.rotationY;
            BAPType x = joint.rotationX;
            bf.setRadianValue(z, angle.z());
            bf.setRadianValue(y, angle.y());
            bf.setRadianValue(x, angle.x());
        }
        //_bframe = bf.clone();
        return bf;
    }
}
