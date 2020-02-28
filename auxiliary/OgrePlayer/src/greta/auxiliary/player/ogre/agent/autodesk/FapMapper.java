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
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import vib.auxiliary.player.ogre.natives.Bone;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class FapMapper {

    Bone faceBone;
    FapMapper(Bone faceBone){
        this.faceBone = faceBone;
        faceBone.setManuallyControlled(true);
    }
    FapMapper(){

    }
    public abstract void applyFap(FAPFrame ff);


    public static class Lip extends FapMapper{

        FAPType axis1Type;
        Vec3d direction1;
        FAPType axis2Type;
        Vec3d direction2;
        FAPType innerType;
        Vec3d rotationAxis;
        Vec3d position;
        Quaternion orientinitial;
        double magicNumber = 0.0008;


        public Lip(Bone faceBone, FAPType outerType, Vec3d directionAxis, FAPType outerType2, Vec3d directionAxis2, FAPType innerType, Vec3d rotationAxis) {
            super(faceBone);
            this.axis1Type = outerType;
            this.direction1 = directionAxis;
            this.axis2Type = outerType2;
            this.direction2 = directionAxis2;
            this.innerType = innerType;
            this.rotationAxis = rotationAxis;
            position = Ogre.convert(faceBone.getPosition());
            this.orientinitial = Ogre.convert(faceBone.getOrientation());

        }

        @Override
        public void applyFap(FAPFrame ff) {

            if(ff.getMask(axis1Type) ||ff.getMask(axis2Type)){
                faceBone.setPosition(
                        (position.x()+direction1.x()*ff.getValue(axis1Type)+direction2.x()*ff.getValue(axis2Type)),
                        (position.y()+direction1.y()*ff.getValue(axis1Type)+direction2.y()*ff.getValue(axis2Type)),
                        (position.z()+direction1.z()*ff.getValue(axis1Type)+direction2.z()*ff.getValue(axis2Type)));
            }
            if(ff.getMask(innerType)){
                double dist = ff.getValue(innerType);
                final greta.core.util.math.Quaternion q =
                        new greta.core.util.math.Quaternion(
                            rotationAxis,
                            dist*magicNumber);
                faceBone.setOrientation(Quaternion.multiplication(q, orientinitial));
            }
        }

    }

    public static class MidLip extends Lip {

        FAPType stretchType;
        Vec3d stretchDirection;
        public MidLip(Bone faceBone, FAPType outerType, Vec3d directionAxis, FAPType outerType2, Vec3d directionAxis2, FAPType innerType, Vec3d rotationAxis, FAPType stretchType, Vec3d stretchDirection) {
            super(faceBone, outerType, directionAxis, outerType2, directionAxis2, innerType, rotationAxis);
            this.stretchType = stretchType;
            this.stretchDirection = stretchDirection;
        }


        @Override
        public void applyFap(FAPFrame ff) {

            if(ff.getMask(axis1Type) || ff.getMask(axis2Type) || ff.getMask(stretchType)){
                faceBone.setPosition(
                        (position.x()+
                                direction1.x()*ff.getValue(axis1Type)+
                                direction2.x()*ff.getValue(axis2Type)+
                                stretchDirection.x()*ff.getValue(stretchType)),
                        (position.y()+
                                direction1.y()*ff.getValue(axis1Type)+
                                direction2.y()*ff.getValue(axis2Type)+
                                stretchDirection.y()*ff.getValue(stretchType)),
                        (position.z()+
                                direction1.z()*ff.getValue(axis1Type)+
                                direction2.z()*ff.getValue(axis2Type)+
                                stretchDirection.z()*ff.getValue(stretchType)));
            }
            if(ff.getMask(innerType)){
                double dist = ff.getValue(innerType);
                final greta.core.util.math.Quaternion q =
                        new greta.core.util.math.Quaternion(
                            rotationAxis,
                            dist*magicNumber);
                faceBone.setOrientation(Quaternion.multiplication(q, orientinitial));
            }
        }
    }

    public static class Rotation extends FapMapper{

        FAPType pitch;
        Vec3d pitchAxis;

        FAPType yaw;
        Vec3d yawAxis;

        Quaternion orientInitial;

        double amplitude1;
        double amplitude2;

        public Rotation(Bone faceBone, FAPType pitch, Vec3d pitchAxis, double amplitude1, FAPType yaw, Vec3d yawAxis, double amplitude2) {
            super(faceBone);
            this.pitch = pitch;
            this.pitchAxis = pitchAxis;
            this.yaw = yaw;
            this.yawAxis = yawAxis;
            this.orientInitial = Ogre.convert(faceBone.getOrientation());
            this.amplitude1 = amplitude1;
            this.amplitude2 = amplitude2;
        }

        @Override
        public void applyFap(FAPFrame ff) {
            if(ff.getMask(pitch) || ff.getMask(yaw)){
                final greta.core.util.math.Quaternion qx =
                        new greta.core.util.math.Quaternion(
                            pitchAxis,
                            ff.getValue(pitch)*amplitude1);
                final greta.core.util.math.Quaternion qy =
                        new greta.core.util.math.Quaternion(
                            yawAxis,
                            ff.getValue(yaw)*amplitude2);
                faceBone.setOrientation(Quaternion.multiplication(Quaternion.multiplication(qy, qx), orientInitial));
            }
        }


    }

    public static class Eye extends Rotation{
        public Eye(Bone faceBone, FAPType pitch, Vec3d pitchAxis, FAPType yaw, Vec3d yawAxis) {
            super(faceBone, pitch, pitchAxis, 1.0/100000.0, yaw, yawAxis, 1.0/100000.0);
        }
    }

    public static class OneDOF extends FapMapper{

        FAPType type1;
        Vec3d dir;
        Vec3d position;
        public OneDOF(Bone faceBone, FAPType type1, Vec3d dir){
            super(faceBone);
            this.dir = dir;
            this.type1 = type1;
            position = Ogre.convert(faceBone.getPosition());
        }

        @Override
        public void applyFap(FAPFrame ff){
            if(ff.getMask(type1)){
                faceBone.setPosition(
                        (position.x()+dir.x()*ff.getValue(type1)),
                        (position.y()+dir.y()*ff.getValue(type1)),
                        (position.z()+dir.z()*ff.getValue(type1)));
            }
        }

    }

    public static class TwoDOF extends OneDOF{

        FAPType type2;
        Vec3d dir2;

        public TwoDOF(Bone faceBone, FAPType type1, Vec3d dir, FAPType type2, Vec3d dir2){
            super(faceBone, type1, dir);
            this.faceBone = faceBone;
            this.dir2 = dir2;
            this.type2 = type2;
        }

        @Override
        public void applyFap(FAPFrame ff){
            faceBone.setPosition(
                    (position.x()+dir.x()*ff.getValue(type1) + dir2.x()*ff.getValue(type2)),
                    (position.y()+dir.y()*ff.getValue(type1) + dir2.y()*ff.getValue(type2)),
                    (position.z()+dir.z()*ff.getValue(type1) + dir2.z()*ff.getValue(type2)));
        }

    }

    public static class Nostril extends FapMapper {

        FAPType type;
        FAPType type2;
        Vec3d dir;

        public Nostril(Bone faceBone, FAPType fapType, FAPType fapType2, Vec3d scaleDirection) {
            super(faceBone);
            type = fapType;
            type2 = fapType2;
            dir = scaleDirection;
        }

        @Override
        public void applyFap(FAPFrame ff) {
            if(ff.getMask(type) || ff.getMask(type2)){
                double fapvaltouse = 0;
                if(ff.getMask(type)){
                    if(ff.getMask(type2)){
                        fapvaltouse = Math.max(ff.getValue(type),ff.getValue(type2));
                    }
                    else{
                        fapvaltouse = ff.getValue(type);
                    }
                }
                else{
                    ff.getValue(type2);
                }
                faceBone.setScale(
                        (1+dir.x()*fapvaltouse),
                        (1+dir.y()*fapvaltouse),
                        (1+dir.z()*fapvaltouse));
            }
        }

    }

    public static class Jaw extends Rotation {
        OneDOF translation;

        public Jaw(Bone faceBone, FAPType pitch, Vec3d pitchAxis, double amplitude1, FAPType yaw, Vec3d yawAxis, double amplitude2, FAPType thrustType, Vec3d thrustDirection) {
            super(faceBone, pitch, pitchAxis, amplitude1, yaw, yawAxis, amplitude2);
            translation = new OneDOF(faceBone, thrustType, thrustDirection);
        }

        @Override
        public void applyFap(FAPFrame ff) {
            translation.applyFap(ff);
            super.applyFap(ff);
        }


    }
}
