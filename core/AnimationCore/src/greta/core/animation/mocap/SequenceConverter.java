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
package greta.core.animation.mocap;

import greta.core.animation.Frame;
import greta.core.animation.bvh.BVHChannel;
import greta.core.animation.bvh.BVHFrame;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class SequenceConverter {

    public static ArrayList<Frame> convertFromBVHFramesToFrames(ArrayList<BVHFrame> bvhframe) {
        boolean rotate90y = false;
        ArrayList<Frame> fs = new ArrayList<Frame>();
        int i = 0;
        Vec3d baseT = new Vec3d();
        //Quaternion baseA = new Quaternion();
        for (BVHFrame frame : bvhframe) {
            Frame f = new Frame();
            for (String name : frame.getValues().keySet()) {
                BVHChannel channel = frame.getValues().get(name);
                Quaternion q = channel.getRotation();

                if(rotate90y && name.equalsIgnoreCase("Hips")){
                    Quaternion qR = new Quaternion();
                    //System.out.println(name + q.getEulerAngleXYZByAngle());
                    qR.setAxisAngle(new Vec3d(0,1,0), 3.14159265f/ 2.0f);
                    q.multiply(qR.inverse());
                    //System.out.println(q.getEulerAngleXYZByAngle());
                    //System.out.println();
                }

                if(channel.has6DOF()){
                    Vec3d t = channel.getTranslation();

                    //Quaternion qy = new Quaternion();
                    //qy.fromEulerXYZ(0, -3.14f/2.0f, 0);

//                    if (!t.equals(new Vec3d())) {
//                        if (i == 0) {
//                            f.setRootTranslation(baseT);
//                            baseT = t;
//                            baseA.fromEulerXYZ(0, q.getEulerAngleXYZ().y(), 0);
//                            i++;
//                        } else {
//                            f.setRootTranslation(Vec3d.substraction(qy.rotate(t), qy.rotate(baseT)));
//                            //f.setRootTranslation(Vec3d.substraction(t, baseT));
//                        }
//
//                    }
                    f.setRootTranslation(t);
                    //f.addRotation(name, Quaternion.multiplication(qy ,q));
                    f.addRotation(name, q);
                }else
                {
                    f.addRotation(name, q);
                }
            }
            fs.add(f);
        }
        return fs;
    }
}
