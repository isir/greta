/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.BvhMocap;
import java.util.*;
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
