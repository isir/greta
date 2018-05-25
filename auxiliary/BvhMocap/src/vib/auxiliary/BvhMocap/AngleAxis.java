/*
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
