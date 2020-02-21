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
package greta.core.animation.mpeg4.bap.filters;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.bap.JointType;
import greta.core.util.id.ID;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class ConcatenateJoints extends BAPFrameEmitterImpl implements BAPFramePerformer {

    private ArrayList<Concatenator> concatenators;
    private List<JointType> currentJointUsed;

    public ConcatenateJoints() {
        concatenators = new ArrayList<Concatenator>(JointType.NUMJOINTS);
        for (JointType type : JointType.values()) {
            concatenators.add(new IdentityConcatenator(type));
        }
        currentJointUsed = Arrays.asList(JointType.values());
    }

    public void setJointToUse(JointType... toUse) {
        setJointToUse(Arrays.asList(toUse));
    }

    public void setJointToUse(List<JointType> toUse) {
        concatenators.clear();
        currentJointUsed = toUse;
        for (JointType type : toUse) {
                // only the spine and the chains sternum-to-shoulder can be concatanated
            //sternum-to-shoulder :
            if (type == JointType.r_shoulder) {
                if (!toUse.contains(JointType.r_acromioclavicular)) {
                    if (toUse.contains(JointType.r_sternoclavicular)) {
                        concatenators.add(new ChainConcatenator(JointType.r_acromioclavicular, type));
                    } else {
                        concatenators.add(new ChainConcatenator(JointType.r_sternoclavicular, type));
                    }
                    continue;
                }
            }
            if (type == JointType.r_acromioclavicular) {
                if (!toUse.contains(JointType.r_sternoclavicular)) {
                    concatenators.add(new AdditionConcatenator(JointType.r_sternoclavicular, type));
                    continue;
                }
            }
            if (type == JointType.l_shoulder) {
                if (!toUse.contains(JointType.l_acromioclavicular)) {
                    if (toUse.contains(JointType.l_sternoclavicular)) {
                        concatenators.add(new ChainConcatenator(JointType.l_acromioclavicular, type));
                    } else {
                        concatenators.add(new ChainConcatenator(JointType.l_sternoclavicular, type));
                    }
                    continue;
                }
            }
            if (type == JointType.l_acromioclavicular) {
                if (!toUse.contains(JointType.l_sternoclavicular)) {
                    concatenators.add(new AdditionConcatenator(JointType.l_sternoclavicular, type));
                    continue;
                }
            }

            //spine :
            if (isSpine(type)) {
                JointType parent = type;
                while (parent.parent != JointType.HumanoidRoot && (!toUse.contains(parent.parent))) {
                    parent = parent.parent;
                }
                if (parent != type) {
                    concatenators.add(new ChainConcatenator(parent, type));
                    continue;
                }
            }
            concatenators.add(new IdentityConcatenator(type));
        }
    }

    @Override
    public void performBAPFrames(List<BAPFrame> bapframes, ID requestId) {
        List<BAPFrame> result = new ArrayList<BAPFrame>(bapframes.size());
        for (BAPFrame frame : bapframes) {
            result.add(concatenateJoints(frame));
        }
        sendBAPFrames(requestId, result);
    }

    public BAPFrame concatenateJoints(BAPFrame frame) {
        BAPFrame result = new BAPFrame(frame.getFrameNumber());
        for (Concatenator c : concatenators) {
            c.concatenate(frame, result);
        }
        return result;
    }

    private Quaternion getJointRotation(BAPFrame frame, JointType joint) {
        Quaternion q = new Quaternion();
        q.fromEulerXYZ(
                joint.rotationX == BAPType.null_bap ? 0 : frame.getRadianValue(joint.rotationX),
                joint.rotationY == BAPType.null_bap ? 0 : frame.getRadianValue(joint.rotationY),
                joint.rotationZ == BAPType.null_bap ? 0 : frame.getRadianValue(joint.rotationZ));
        return q;
    }

    private Quaternion computeChain(BAPFrame frame, JointType jointFrom, JointType jointTo) {
        Quaternion qTo = getJointRotation(frame, jointTo);
        if (jointFrom == jointTo) {
            return qTo;
        }
        return Quaternion.multiplication(computeChain(frame, jointFrom, jointTo.parent), qTo);
    }

    private boolean isSpine(JointType joint) {
        return isSpine_(joint, JointType.skullbase);
    }

    private boolean isSpine_(JointType joint, JointType vertebra) {
        if (vertebra == JointType.HumanoidRoot || vertebra == JointType.null_joint || vertebra == null) {
            return false;
        }
        if (joint == vertebra) {
            return true;
        }
        return isSpine_(joint, vertebra.parent);
    }

    public List<JointType> getJointToUse() {
        return new ArrayList<JointType>(currentJointUsed);
    }

    public String getJointToUseString() {
        String res = "";
        for (JointType type : JointType.values()) {
            if (currentJointUsed.contains(type)) {
                res += "1";
            } else {
                res += "0";
            }
        }
        return res;
    }

    public void parseJointToUseString(String status) {
        ArrayList<JointType> toUse = new ArrayList<JointType>();
        for (JointType type : JointType.values()) {
            int ordinal = type.ordinal();
            if (ordinal < status.length() && status.charAt(ordinal) == '1') {
                toUse.add(type);
            }
        }
        setJointToUse(toUse);
    }

    private interface Concatenator {

        public void concatenate(BAPFrame original, BAPFrame result);
    }

    private class EmptyConcatenator implements Concatenator {

        @Override
        public void concatenate(BAPFrame original, BAPFrame result) {
        }
    }

    private class IdentityConcatenator implements Concatenator {

        JointType joint;

        public IdentityConcatenator(JointType joint) {
            this.joint = joint;
        }

        @Override
        public void concatenate(BAPFrame original, BAPFrame result) {
            copy(original, result, joint.rotationX);
            copy(original, result, joint.rotationY);
            copy(original, result, joint.rotationZ);
        }

        private void copy(BAPFrame original, BAPFrame result, BAPType bapType) {
            if (bapType != BAPType.null_bap) {
                if (original.getMask(bapType)) {
                    result.applyValue(bapType, original.getValue(bapType));
                }
            }
        }
    }

    private class ChainConcatenator implements Concatenator {

        JointType from;
        JointType to;

        public ChainConcatenator(JointType from, JointType to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void concatenate(BAPFrame original, BAPFrame result) {
            Vec3d xyz = computeChain(original, from, to).getEulerAngleXYZ();
            if (to.rotationX != BAPType.null_bap) {
                result.setRadianValue(to.rotationX, xyz.x());
            }
            if (to.rotationY != BAPType.null_bap) {
                result.setRadianValue(to.rotationY, xyz.y());
            }
            if (to.rotationZ != BAPType.null_bap) {
                result.setRadianValue(to.rotationZ, xyz.z());
            }
        }
    }

    private class ChainConcatenator2 implements Concatenator {

        JointType from;
        JointType to;

        public ChainConcatenator2(JointType from, JointType to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void concatenate(BAPFrame original, BAPFrame result) {
            Vec3d xyz = computeChain(original, from, to).getEulerAngleXYZ();
            if (from.rotationX != BAPType.null_bap) {
                result.setRadianValue(from.rotationX, xyz.x());
            }
            if (from.rotationY != BAPType.null_bap) {
                result.setRadianValue(from.rotationY, xyz.y());
            }
            if (from.rotationZ != BAPType.null_bap) {
                result.setRadianValue(from.rotationZ, xyz.z());
            }
        }
    }

    private class AdditionConcatenator implements Concatenator {

        JointType from;
        JointType to;

        public AdditionConcatenator(JointType from, JointType to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public void concatenate(BAPFrame original, BAPFrame result) {
            int[] addition = new int[3];
            JointType joint = to;
            while (joint != from) {
                add(addition, original, joint);
                joint = joint.parent;
            }
            add(addition, original, from);

            if (to.rotationX != BAPType.null_bap) {
                result.applyValue(to.rotationX, addition[0]);
            }
            if (to.rotationY != BAPType.null_bap) {
                result.applyValue(to.rotationY, addition[1]);
            }
            if (to.rotationZ != BAPType.null_bap) {
                result.applyValue(to.rotationZ, addition[2]);
            }
        }

        private void add(int[] addition, BAPFrame frame, JointType joint) {
            if (joint.rotationX != BAPType.null_bap && frame.getMask(joint.rotationX)) {
                addition[0] = addition[0] + frame.getValue(joint.rotationX);
            }
            if (joint.rotationY != BAPType.null_bap && frame.getMask(joint.rotationY)) {
                addition[1] = addition[1] + frame.getValue(joint.rotationY);
            }
            if (joint.rotationZ != BAPType.null_bap && frame.getMask(joint.rotationZ)) {
                addition[2] = addition[2] + frame.getValue(joint.rotationZ);
            }
        }
    }
}
