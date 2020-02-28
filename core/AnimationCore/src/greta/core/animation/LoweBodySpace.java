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

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class LoweBodySpace {
    double dragger = 1;
    Quaternion _rotation = new Quaternion();
    double xMin = 100;
    double xMax = -100;
    double zMin = 100;
    double zMax = -100;
    double yMax = -100;
    double yMin = 100;
    Vec3d _local;
    public LoweBodySpace(Skeleton sk){
        Joint j_r_subtalar = sk.getJoint("r_subtalar");
        Joint j_l_subtalar = sk.getJoint("l_subtalar");
        Joint j_r_metatarsal = sk.getJoint("r_metatarsal");
        Joint j_l_metatarsal = sk.getJoint("l_metatarsal");
        Vec3d pos1 = j_r_subtalar.getWorldPosition();
        if(pos1.x() < xMin){
            xMin = pos1.x();
        }
        if(pos1.x() > xMax){
            xMax = pos1.x();
        }
        if(pos1.z() < zMin){
            zMin = pos1.x();
        }
        if(pos1.z() > zMax){
            zMax = pos1.x();
        }
        Vec3d pos2 = j_l_subtalar.getWorldPosition();
        if(pos2.x() < xMin){
            xMin = pos2.x();
        }
        if(pos2.x() > xMax){
            xMax = pos2.x();
        }
        if(pos2.z() < zMin){
            zMin = pos2.x();
        }
        if(pos2.z() > zMax){
            zMax = pos2.x();
        }
        Vec3d pos3 = j_r_metatarsal.getWorldPosition();
        if(pos3.x() < xMin){
            xMin = pos3.x();
        }
        if(pos3.x() > xMax){
            xMax = pos3.x();
        }
        if(pos3.z() < zMin){
            zMin = pos3.x();
        }
        if(pos3.z() > zMax){
            zMax = pos3.x();
        }
        Vec3d pos4 = j_l_metatarsal.getWorldPosition();
        if(pos4.x() < xMin){
            xMin = pos4.x();
        }
        if(pos4.x() > xMax){
            xMax = pos4.x();
        }
        if(pos4.z() < zMin){
            zMin = pos4.x();
        }
        if(pos4.z() > zMax){
            zMax = pos4.x();
        }
        Joint j_root = sk.getJoint("HumanoidRoot");
        yMax = j_root.getWorldPosition().y();
        Joint j_ankle = sk.getJoint("r_ankle");
        yMin = j_ankle.getWorldPosition().y();
    }


    public Vec3d applyConstraint(Vec3d target){
        Vec3d re = new Vec3d();
        if(target.x() < xMin){
            re.setX(xMin);
        }else if(target.x() > xMax){
            re.setX(xMax);
        }else{
            re.setX(target.x());
        }
        if(target.y() < yMin){
            re.setY(yMin);
        }else if(target.y() > yMax){
            re.setY(yMax);
        }else{
            re.setY(target.y());
        }
         if(target.z() < zMin){
            re.setZ(zMin);
        }else if(target.z() > zMax){
            re.setZ(zMax);
        }else{
            re.setZ(target.z());
        }
         return re;
    }

}
