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
package greta.core.animation.body;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 * @author Radoslaw Niewiadomski
 */
public class Shoulder extends ExpressiveFrame {

    String _side;

    public Shoulder() {
    }

    public void setSide(String side) {
        _side = side;
    }

    public String getSide() {
        return _side;
    }

    public void compute(double horizontal, double vertical) {

        double MAX_horizontal = 0.5;//back
        double MAX_vertical = 0.5;//up

        double MIN_vertical = 0;//down
        double MIN_horizontal = 0.3;//front


        //System.out.println ("horizontal" + horizontal + ", verticql "+ vertical);

        //direction reversed
        horizontal = -1d * horizontal;

        //negative horizontal is forward!
        //positive horizontal is backward!

        if (horizontal > 0) {
            horizontal = MAX_horizontal * horizontal;
        }
        if (horizontal < 0) {
            horizontal = MIN_horizontal * horizontal;
        }

        if (vertical > 0) {
            vertical = MAX_vertical * vertical;
        }
        if (vertical < 0) {
            vertical = MIN_vertical * vertical;
        }


        //horizontal = Math.min(0.1, Math.max(-0.1f, horizontal));
        //vertical =  Math.min(0.2, Math.max(-0.2f, vertical));

        if (_side.equalsIgnoreCase("LEFT")) {
            Quaternion h = new Quaternion();
            h.setAxisAngle(new Vec3d(0, 1, 0), (double) horizontal);
            Quaternion v = new Quaternion();
            v.setAxisAngle(new Vec3d(0, 0, 1), (double) vertical);
            addRotation("l_sternoclavicular", Quaternion.multiplication(v, h));


            Quaternion h2 = new Quaternion();
            h2.setAxisAngle(new Vec3d(0, 1, 0), (-1f)*(double) horizontal);
            Quaternion v2 = new Quaternion();
            v2.setAxisAngle(new Vec3d(0, 0, 1), (-1f)*(double) vertical);
            addRotation("l_acromioclavicular", Quaternion.multiplication(v2, h2));

        } else {

            Quaternion h = new Quaternion();
            h.setAxisAngle(new Vec3d(0, 1, 0), -(double) horizontal);
            Quaternion v = new Quaternion();
            v.setAxisAngle(new Vec3d(0, 0, -1), (double) vertical);
            addRotation("r_sternoclavicular", Quaternion.multiplication(v, h));


            Quaternion h2 = new Quaternion();
            h2.setAxisAngle(new Vec3d(0, 1, 0),  (double) horizontal);
            Quaternion v2 = new Quaternion();
            v2.setAxisAngle(new Vec3d(0, 0, 1), (double) vertical);
            addRotation("r_acromioclavicular", Quaternion.multiplication(v2, h2));

        }
    }
}
