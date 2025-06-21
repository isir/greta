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
