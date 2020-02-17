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
package greta.auxiliary.BVHMocap;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.util.math.Quaternion;
import java.util.HashMap;

/**
 *
 * @author Nesrine Fourati
 */
public class SpineInterpolation {
       public HashMap<String,Quaternion> spineKeys = new HashMap<String,Quaternion>();

        public SpineInterpolation()
        {
        spineKeys.put("vt1", null);
        spineKeys.put("vt6", null);
        spineKeys.put("vt12", null);
        }

        public void SetVt1(Quaternion q)
        {
        spineKeys.put("vt1", q) ;
        }

        public void SetVt6(Quaternion q)
        {
        spineKeys.put("vt6", q) ;
        }
        public void SetVt12(Quaternion q)
        {
        spineKeys.put("vt12", q) ;
        }

       public BAPFrame Interpolation(BAPFrame bapframe)
        {
        /* spineKeys is a hashmap with 3 String keys (vt1, vt6, vt12) and their correpondent quaternion */
        BapAnimationConverter converter=new BapAnimationConverter();
        Quaternion q;
        float t;

        if ( spineKeys.get("vt6")!=null &&  spineKeys.get("vt1")!=null)
        {   //interpolate vt1 -> vt5
            t = (float) 1 / 5;
            for (int k = 2; k <= 5; k++)
            {
                q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt1"), t, true);
                bapframe = converter.setBAPframeRotation(bapframe, "vt".concat(String.valueOf(k)), q);
            }

            q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt1"), t, true);
            bapframe = converter.setBAPframeRotation(bapframe, "vt1", q);

            //interpolate vt6 -> vt11
            t = (float) 1 / 6;
            for (int k = 7; k <= 11; k++)
            {
                q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt6"), t, true);
                bapframe = converter.setBAPframeRotation(bapframe, "vt".concat(String.valueOf(k)), q);
            }

            q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt6"), t, true);
            bapframe = converter.setBAPframeRotation(bapframe, "vt6", q);
        } else
        {
            //interpolate vt1 -> vt11

            if ( spineKeys.get("vt1")!=null)
            {

                 t = (float) 1 / 11;
                for (int k = 2; k <= 11; k++)
                {
                    q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt1"), t, true);
                    bapframe = converter.setBAPframeRotation(bapframe, "vt".concat(String.valueOf(k)), q);
                }

                  q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt1"), t, true);
                  bapframe = converter.setBAPframeRotation(bapframe, "vt1", q);
            }
            else
                if ( spineKeys.get("vt6")!=null)
            {
                //interpolate vt6 -> vt11

                 t = (float) 1 / 6;
                for (int k = 7; k <= 11; k++)
                {
                   q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt6"), t, true);
                    bapframe = converter.setBAPframeRotation(bapframe, "vt".concat(String.valueOf(k)), q);
                }

                q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt6"), t, true);
                bapframe = converter.setBAPframeRotation(bapframe, "vt6", q);

            }
        }

//            //interpolate vt12 -> vl4
        if ( spineKeys.get("vt12")!=null)
        {
           // System.out.println("interp vt12 vl4");
             t = (float) 1.0f / 5.0f;
            for (int k = 1; k <= 4; k++)
            {
                q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt12"), t, true);
                bapframe = converter.setBAPframeRotation(bapframe, "vl".concat(String.valueOf(k)), q);
            }

            q = Quaternion.slerp(new Quaternion(), (Quaternion) spineKeys.get("vt12"), t, true);
            bapframe = converter.setBAPframeRotation(bapframe, "vt12", q);
        }

        return bapframe;
        }
}
