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
