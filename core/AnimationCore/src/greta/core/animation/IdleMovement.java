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
package greta.core.animation;

import greta.core.util.math.Noise;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class IdleMovement {

    Skeleton _sk;
    double xNoise = 0, yNoise = 0, zNoise = 0;
    double addX = 0.5;
    double addY = 0.5;
    double addZ = 0.5;
    double incre = 5;
    double xRange = 2;
    double yRange = 0.5;
    double zRange = 2;
    double xOffset = 2.5;
    double yOffset = 0;
    double zOffset = 0;
    LoweBodySpace _loweBodySpace;
    Vec3d _posRoot = new Vec3d();

    public IdleMovement(Skeleton sk) {
        _sk = sk;
        _loweBodySpace = new LoweBodySpace(sk);
    }

    public Frame apply() {

        computeRootOffset();

        CharacterLowerBody boy = new CharacterLowerBody();

        boy.setRootOffset(_posRoot);
        boy.setSkeleton(_sk);
        boy.compute();

        applyUpBody();

        for (String name : boy.getRotations().keySet()) {
            _sk.getJoint(name).setLocalRotation(boy.getRotations().get(name));
        }
        _sk.getJoint(0).setLocalPosition(_posRoot);
        _sk.update();
        return boy.getFrame();
    }


    public Frame apply(Vec3d offset) {

        computeRootOffset();
        CharacterLowerBody boy = new CharacterLowerBody();
        _posRoot.divide(5);
        offset.add(_posRoot);
        boy.setRootOffset(offset);
        boy.setSkeleton(_sk);
        boy.compute();

        applyUpBody();

        for (String name : boy.getRotations().keySet()) {
            _sk.getJoint(name).setLocalRotation(boy.getRotations().get(name));
        }
        _sk.getJoint(0).setLocalPosition(_posRoot);
        _sk.update();
        return boy.getFrame();
    }

    void computeRootOffset() {
        double x = (double) Noise.noise(xNoise / 255.0, 0, 0);
        double y = (double) Noise.noise(0, (yNoise + 177) / 255.0, 0);
        double z = (double) Noise.noise(0, 0, (zNoise + 19) / 255.0);

        xNoise += addX * incre;
        yNoise += addY * incre;
        zNoise += addZ * incre;
        _posRoot = new Vec3d((x) * (double) xRange + (double) xOffset, -y * (double) yRange - 1 + (double) yOffset, (z - 0.25f) * (double) zRange + (double) zOffset);
        _posRoot = _loweBodySpace.applyConstraint(_posRoot);
    }

    Vec3d applyResponse(Vec3d force){
        Vec3d offset = new Vec3d();
        return offset;
    }

    void applyUpBody() {
        //upbody
        {
            Quaternion q = new Quaternion();
            q.fromEulerXYZByAngle(-_posRoot.x() * 1.5f, -_posRoot.y() * 1.5f, -_posRoot.z() * 1.5f);
            _sk.getJoint("vl4").setLocalRotation(q);

        }
    }

    public Skeleton getSkeleton() {
        return _sk;
    }


}
