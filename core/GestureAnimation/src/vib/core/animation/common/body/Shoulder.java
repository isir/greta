/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.body;

import vib.core.animation.common.Frame.ExtendedKeyFrame;
import vib.core.animation.common.Frame.JointFrame;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 * @author Radoslaw Niewiadomski
 */
public class Shoulder extends ExtendedKeyFrame {

    String _side;

    public Shoulder(double time) {
        super(time);
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
            //horizontal = Math.max(0, horizontal);
            h.setAxisAngle(new Vec3d(0, 1, 0), (double) horizontal);
            Quaternion v = new Quaternion();
            v.setAxisAngle(new Vec3d(0, 0, 1), (double) vertical);
            //JointFrame jf = new JointFrame();
            //jf._localrotation = v;
            //addJointFrame("l_acromioclavicular", jf);
            JointFrame jf2 = new JointFrame();
            jf2._localrotation = Quaternion.multiplication(v, h);
            addJointFrame("l_sternoclavicular", jf2);


            Quaternion h2 = new Quaternion();
            //horizontal = Math.max(0, horizontal);
            h2.setAxisAngle(new Vec3d(0, 1, 0), (-1f)*(double) horizontal);

            Quaternion v2 = new Quaternion();
            v2.setAxisAngle(new Vec3d(0, 0, 1), (-1f)*(double) vertical);

            JointFrame jf3 = new JointFrame();
            jf3._localrotation = Quaternion.multiplication(v2, h2);
            //addJointFrame("l_shoulder", (jf3));
            addJointFrame("l_acromioclavicular", (jf3));

        } else {

            Quaternion h = new Quaternion();
            //horizontal = Math.max(0, horizontal);
            h.setAxisAngle(new Vec3d(0, 1, 0), -(double) horizontal);
            Quaternion v = new Quaternion();
            v.setAxisAngle(new Vec3d(0, 0, -1), (double) vertical);


            JointFrame jf2 = new JointFrame();
            jf2._localrotation = Quaternion.multiplication(v, h);
            addJointFrame("r_sternoclavicular", jf2);

            //JointFrame jf = new JointFrame();
            //jf._localrotation = v;
            //addJointFrame("r_acromioclavicular", jf);
            //JointFrame jf2 = new JointFrame();
            //jf2._localrotation = h;
            //addJointFrame("r_sternoclavicular", jf2);

            Quaternion h2 = new Quaternion();
            //horizontal = Math.max(0, horizontal);
            h2.setAxisAngle(new Vec3d(0, 1, 0),  (double) horizontal);

            Quaternion v2 = new Quaternion();
            v2.setAxisAngle(new Vec3d(0, 0, 1), (double) vertical);

            JointFrame jf3 = new JointFrame();
            jf3._localrotation = Quaternion.multiplication(v2, h2);
            addJointFrame("r_acromioclavicular", (jf3));

        }
    }
}
