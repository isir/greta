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
