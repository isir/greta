/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.BvhMocap;

import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPType;
import vib.core.animation.mpeg4.bap.JointType;
import vib.core.util.math.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Nesrine Fourati
 */
public class BapAnimationConverter {

    public BAPFrame setBAPframeTranslation(BAPFrame bapframe, String name,Vec3d translation)
    {
        JointType joint = JointType.get(name);
        if(joint != JointType.HumanoidRoot){
            return bapframe;
        }

        BAPType tx = BAPType.HumanoidRoot_tr_lateral;
        BAPType ty = BAPType.HumanoidRoot_tr_vertical;
        BAPType tz = BAPType.HumanoidRoot_tr_frontal;

        double valuex = translation.x() * 10;
        double valuey = translation.y()* 10;
        double valuez = translation.z()* 10;

        bapframe.applyValue(tx, ((Number) valuex).intValue());
        bapframe.applyValue(ty, ((Number) valuey).intValue());
        bapframe.applyValue(tz, ((Number) valuez).intValue());

        return bapframe;

    }
    public BAPFrame setBAPframeRotation(BAPFrame bapframe, String name, Quaternion q) {


        JointType joint = JointType.get(name);

        BAPType tx = joint.rotationX;
        BAPType ty = joint.rotationY;
        BAPType tz = joint.rotationZ;
        // System.out.println(name);
        Vec3d angle = q.getEulerAngleXYZ();

        bapframe.setRadianValue(tx, angle.x());
        bapframe.setRadianValue(ty, angle.y());
        bapframe.setRadianValue(tz, angle.z());

        return bapframe;
    }

    public static BufferedWriter NewFile(String pathfile, String filename, String fileformat) throws IOException {
        Random rand = new Random();
        int r = rand.nextInt(100);

        BufferedWriter output = null;
        String adrfile = pathfile + filename + r + fileformat;
        FileWriter fw = new FileWriter(adrfile, true);
        output = new BufferedWriter(fw);
        output.flush();
        return output;
    }

    public void BapOutput(ArrayList<BAPFrame> bapframes, String pathfile, String filename, String fileformat, String firstLine) throws IOException {
        BufferedWriter output = NewFile(pathfile, filename, fileformat);
        output.write(firstLine);
        output.flush();

        for (int i = 0; i < bapframes.size(); i++) {
            output.write(bapframes.get(i).AnimationParametersFrame2String());
            output.flush();
        }
        output.close();
    }
}
