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

import java.util.Vector;

/**
 *
 * @author Nesrine Fourati
 */
public class Motion {
       protected String cle;
    /*vect contains the 3D euler angles of the bvh file in the xyz order */
    protected Vector vect = new Vector();

    public Motion() {
    }

    public Motion(String cle_in) {
        cle = cle_in;
    }

    public Vector getvect()
    {

        return vect;
    }

    public String getcle()
    {
        return cle;
    }

    void DisplayEulerAngle(int nbframe)
    {
        int framecpt=0;
        System.out.println(cle);
        for (int i=0;i<nbframe;i++)
        {
         System.out.println(i+" "+vect.elementAt(framecpt)+" "+vect.elementAt(framecpt+1)+" "+vect.elementAt(framecpt+2));

         framecpt=framecpt+3;
        }
    }
}
