/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.BvhMocap;

import java.util.*;
/**
 *
 * @author Nesrine Fourati
 */
public class AngleAxis {
    protected String cle;
    public Vector angle = new Vector();
    public Vector axis=new Vector();

    public AngleAxis() {
    }

    public AngleAxis(String cle_in) {
        cle = cle_in;
    }

    public Vector getvect() {
        return angle;
    }

    public String getcle() {
        return cle;
    }
    public Vector getaxis()
    {
        return axis;
    }

}
