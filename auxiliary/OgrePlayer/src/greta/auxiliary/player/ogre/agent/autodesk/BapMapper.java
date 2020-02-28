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
package greta.auxiliary.player.ogre.agent.autodesk;

import greta.auxiliary.player.ogre.Ogre;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import vib.auxiliary.player.ogre.natives.Bone;
import vib.auxiliary.player.ogre.natives.Node;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class BapMapper {

    Bone bone;
    Quaternion preRotation;
    Quaternion postRotation;
    BapMapper(Bone bone, Quaternion correction){
        this.bone = bone;
        bone.setManuallyControlled(true);
        Quaternion parentOrientation;
        try {
            Node parent = bone.getParent();
            if(parent.isNull()){
                parentOrientation = new greta.core.util.math.Quaternion();
            }
            else{
                parentOrientation = Ogre.convert(parent._getDerivedOrientation()).normalized();
            }
        } catch (Throwable t) {
            parentOrientation = new greta.core.util.math.Quaternion();
        }

        preRotation = Quaternion.multiplication(parentOrientation, Ogre.convert(bone.getOrientation())).normalized();
        if(correction != null){
            preRotation = Quaternion.multiplication(correction, preRotation).normalized();
        }
        postRotation = parentOrientation.inverse().normalized();
    }

    public void applyBap(BAPFrame bf){
        if(needsUpdate(bf)){
            Quaternion orient = Quaternion.multiplication(Quaternion.multiplication(
                            postRotation, getRotation(bf)), preRotation);
            orient.normalize();
            bone.setOrientation(orient);
        }
    }
    abstract boolean needsUpdate(BAPFrame bf);
    abstract Quaternion getRotation(BAPFrame bf);

    public static class OneDOF extends BapMapper{
        BAPType type1;
        Vec3d axis1;

        public OneDOF(Bone bone, BAPType type1, Vec3d axis1) {
            this(bone, null, type1, axis1);
        }

        public OneDOF(Bone bone, Quaternion correction, BAPType type1, Vec3d axis1) {
            super(bone, correction);
            this.type1 = type1;
            this.axis1 = axis1;
        }

        @Override
        boolean needsUpdate(BAPFrame bf) {
            return bf.getMask(type1);
        }

        @Override
        Quaternion getRotation(BAPFrame bf) {
            return new Quaternion(axis1, bf.getRadianValue(type1));
        }
    }


    public static class TwoDOF extends BapMapper{
        private BAPType type1;
        Vec3d axis1;
        double lastVal1;
        private BAPType type2;
        Vec3d axis2;
        double lastVal2;

        public TwoDOF(Bone bone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2) {
            this(bone, null, type1, axis1, type2, axis2);
        }

        public TwoDOF(Bone bone, Quaternion correction, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2) {
            super(bone, correction);
            this.type1 = type1;
            this.axis1 = axis1;
            lastVal1 = 0;
            this.type2 = type2;
            this.axis2 = axis2;
            lastVal2 = 0;
        }

        @Override
        boolean needsUpdate(BAPFrame bf) {
            boolean toReturn = false;
            if(bf.getMask(type1)){
                lastVal1 = bf.getRadianValue(type1);
                toReturn = true;
            }
            if(bf.getMask(type2)){
                lastVal2 = bf.getRadianValue(type2);
                toReturn = true;
            }
            return toReturn;
        }

        @Override
        Quaternion getRotation(BAPFrame bf) {
            Quaternion q1 = new Quaternion(axis1, lastVal1);
            Quaternion q2 = new Quaternion(axis2, lastVal2);
            Quaternion res = Quaternion.multiplication(q2, q1);
            res.normalize();
            return res;
        }
    }


    public static class ThreeDOF extends TwoDOF{
        private BAPType type3;
        Vec3d axis3;
        double lastVal3;

        public ThreeDOF(Bone bone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3) {
            this(bone, null, type1, axis1, type2, axis2, type3, axis3);
        }

        public ThreeDOF(Bone bone, Quaternion correction, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3) {
            super(bone, correction, type1, axis1, type2, axis2);
            this.type3 = type3;
            this.axis3 = axis3;
            lastVal3 = 0;
        }

        @Override
        boolean needsUpdate(BAPFrame bf) {
            boolean toReturn = super.needsUpdate(bf);
            if(bf.getMask(type3)){
                lastVal3 = bf.getRadianValue(type3);
                toReturn = true;
            }
            return toReturn;
        }

        @Override
        Quaternion getRotation(BAPFrame bf) {
            Quaternion q1 = new Quaternion(axis1, lastVal1);
            Quaternion q2 = new Quaternion(axis2, lastVal2);
            Quaternion q3 = new Quaternion(axis3, lastVal3);

            Quaternion res = Quaternion.multiplication(Quaternion.multiplication(q3,q2),q1);
            res.normalize();
            return res;
        }
    }


    static abstract class TwistMapper extends ThreeDOF{

        protected Twist twist;
        double twistFactor = 0;
        public TwistMapper(Bone bone, Bone twistBone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3, double twistFactor) {
            super(bone, type1, axis1, type2, axis2, type3, axis3);
            this.twistFactor = twistFactor;
            twist = new Twist(twistBone);
        }

        @Override
        boolean needsUpdate(BAPFrame bf) {
            boolean update = super.needsUpdate(bf);
            twist.twistUpdate = update;
            return update;
        }

        @Override
        Quaternion getRotation(BAPFrame bf) {
            Quaternion original = super.getRotation(bf);
            twist.twistRotation = getTwistRotation(original);
            return propagateTwist(twist.twistRotation, original);
        }

        @Override
        public void applyBap(BAPFrame bf) {
            super.applyBap(bf);
            twist.applyBap(bf);
        }

        abstract Quaternion propagateTwist(Quaternion twist, Quaternion original);

        abstract Quaternion getTwistRotation(Quaternion originalRotation);

        private class Twist extends BapMapper{
            Quaternion twistRotation;
            boolean twistUpdate = false;
            public Twist(Bone bone, Quaternion correction) {
                super(bone, correction);
            }
            public Twist(Bone bone){
                this(bone, null);
            }

            @Override
            boolean needsUpdate(BAPFrame bf) {
                return twistUpdate;
            }

            @Override
            Quaternion getRotation(BAPFrame bf) {
                return twistRotation;
            }

        }
    }

    static abstract class YawTwistMapper extends TwistMapper{
        private Vec3d yawAxis = new Vec3d(0, 1, 0);
        public YawTwistMapper(Bone bone, Bone twistBone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3, double twistFactor) {
            super(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor);
        }

        @Override
        Quaternion getTwistRotation(Quaternion originalRotation) {
            double fTx  = 2.0*originalRotation.x();
            double fTy  = 2.0*originalRotation.y();
            double fTz  = 2.0*originalRotation.z();
            double fTwy = fTy*originalRotation.w();
            double fTxx = fTx*originalRotation.x();
            double fTxz = fTz*originalRotation.x();
            double fTyy = fTy*originalRotation.y();
            double yaw = Math.atan2(fTxz+fTwy, 1.0-(fTxx+fTyy)) * twistFactor;

            return new Quaternion(yawAxis, yaw);
        }
    }

    public static class YawTwistBeforeMapper extends YawTwistMapper{

        public YawTwistBeforeMapper(Bone bone, Bone twistBone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3, double twistFactor) {
            super(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor);
        }

        @Override
        Quaternion propagateTwist(Quaternion twist, Quaternion original) {
            Quaternion q = Quaternion.multiplication(twist.inverse(), original);
            q.normalize();
            return q;
        }
    }
    public static class YawTwistAfterMapper extends YawTwistMapper{

        public YawTwistAfterMapper(Bone bone, Bone twistBone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3, double twistFactor) {
            super(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor);
        }

        @Override
        Quaternion propagateTwist(Quaternion twist, Quaternion original) {
            Quaternion q = Quaternion.multiplication(original, twist.inverse());
            q.normalize();
            return q;
        }
    }

    public static class ThreeDOFScaled extends ThreeDOF{
        private double scale = 1;
        public void setScale(double scale){
            this.scale = scale;
        }

        public ThreeDOFScaled(Bone bone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3) {
            this(bone, null, type1, axis1, type2, axis2, type3, axis3);
        }

        public ThreeDOFScaled(Bone bone, Quaternion correction, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3) {
            super(bone, correction, type1, axis1, type2, axis2, type3, axis3);
        }

        @Override
        Quaternion getRotation(BAPFrame bf) {
            Quaternion q1 = new Quaternion(axis1, (lastVal1*scale));
            Quaternion q2 = new Quaternion(axis2, (lastVal2*scale));
            Quaternion q3 = new Quaternion(axis3, (lastVal3*scale));

            Quaternion res = Quaternion.multiplication(Quaternion.multiplication(q3,q2),q1);
            res.normalize();
            return res;
        }
    }


    static class SortedTwistMapper extends TwistMapper{
        public SortedTwistMapper(Bone bone, Bone twistBone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3, double twistFactor) {
            super(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor);
        }

        @Override
        Quaternion propagateTwist(Quaternion twist, Quaternion original) {
            return null;
        }

        @Override
        Quaternion getTwistRotation(Quaternion originalRotation) {
            return null;
        }

        @Override
        Quaternion getRotation(BAPFrame bf) {
            twist.twistRotation = new Quaternion(axis1, lastVal1*twistFactor);
            Quaternion q1 = new Quaternion(axis1, lastVal1*(1-twistFactor));
            Quaternion q2 = new Quaternion(axis2, lastVal2);
            Quaternion q3 = new Quaternion(axis3, lastVal3);
            Quaternion res = Quaternion.multiplication(Quaternion.multiplication(q3,q2),q1);
            res.normalize();
            return res;
        }
    }
    static class SortedTwistBeforeMapper extends TwistMapper{
        public SortedTwistBeforeMapper(Bone bone, Bone twistBone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3, double twistFactor) {
            super(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor);
        }

        @Override
        Quaternion propagateTwist(Quaternion twist, Quaternion original) {
            return null;
        }

        @Override
        Quaternion getTwistRotation(Quaternion originalRotation) {
            return null;
        }

        @Override
        Quaternion getRotation(BAPFrame bf) {
            twist.twistRotation = new Quaternion(axis1, lastVal1*twistFactor);
            Quaternion q1 = new Quaternion(axis1, lastVal1*(1-twistFactor));
            Quaternion q2 = new Quaternion(twist.twistRotation.inverseRotate(axis2), lastVal2);
            Quaternion q3 = new Quaternion(twist.twistRotation.inverseRotate(axis3), lastVal3);
            Quaternion res = Quaternion.multiplication(Quaternion.multiplication(q3,q2),q1);
            res.normalize();
            return res;
        }
    }

    static class ShoulderSortedTwistMapper extends SortedTwistMapper{

        BapMapper acromium;

        public ShoulderSortedTwistMapper(Bone bone, Bone twistBone, BAPType type1, Vec3d axis1, BAPType type2, Vec3d axis2, BAPType type3, Vec3d axis3, double twistFactor, BapMapper acromium) {
            super(bone, twistBone, type1, axis1, type2, axis2, type3, axis3, twistFactor);
            this.acromium = acromium;
        }


        @Override
        boolean needsUpdate(BAPFrame bf) {
            boolean superNeedsUpdate = super.needsUpdate(bf);
            boolean acromiumNeedsUpdate = acromium.needsUpdate(bf);
            return superNeedsUpdate || acromiumNeedsUpdate;
        }

        @Override
        Quaternion getRotation(BAPFrame bf) {
            Quaternion res = Quaternion.multiplication(acromium.getRotation(bf),super.getRotation(bf));
            res.normalize();
            return res;
        }


    }
}
