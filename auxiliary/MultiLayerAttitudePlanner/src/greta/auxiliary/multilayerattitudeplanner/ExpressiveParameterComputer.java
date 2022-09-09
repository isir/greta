/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner;

import java.util.List;
import java.util.Random;
import greta.core.signals.FaceSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.Signal;
import greta.core.signals.gesture.GestureSignal;

/**
 *
 * @author Mathieu
 */
public class ExpressiveParameterComputer {

    private Random r;
    private double lowGestureSPCBound=0.0;
    private double highGestureSPCBound=1.0;

    private double lowGesturePWRBound=0.0;
    private double highGesturePWRBound=1.0;

    private double lowHeadSPCBound=0.0;
    private double highHeadSPCBound=1.0;

    private double lowFaceIntensityBound=0.0;
    private double highFaceIntensityBound=1.0;

    public ExpressiveParameterComputer(String att)
    {
        r = new Random();
        if(att.equalsIgnoreCase("DomBigDecr"))
        {
            lowGestureSPCBound=-1.0;
            highGestureSPCBound=-0.3;

            lowGesturePWRBound=0.0;
            highGesturePWRBound=0.3;

            lowHeadSPCBound=-1.0;
            highHeadSPCBound=-0.3;

            lowFaceIntensityBound=0.0;
            highFaceIntensityBound=0.3;
        }
        else if(att.equalsIgnoreCase("DomSmallDecr"))
        {
            lowGestureSPCBound=-0.55;
            highGestureSPCBound=0.15;

            lowGesturePWRBound=0.15;
            highGesturePWRBound=0.55;

            lowHeadSPCBound=-0.55;
            highHeadSPCBound=0.15;

            lowFaceIntensityBound=0.15;
            highFaceIntensityBound=0.55;
        }
        else if(att.equalsIgnoreCase("DomSmallIncr"))
        {
            lowGestureSPCBound=-0.15;
            highGestureSPCBound=0.55;

            lowGesturePWRBound=0.45;
            highGesturePWRBound=0.8;

            lowHeadSPCBound=-0.15;
            highHeadSPCBound=0.55;

            lowFaceIntensityBound=0.45;
            highFaceIntensityBound=0.8;
        }
        else if(att.equalsIgnoreCase("DomBigIncr"))
        {
            lowGestureSPCBound=0.3;
            highGestureSPCBound=1.0;

            lowGesturePWRBound=0.7;
            highGesturePWRBound=1.0;

            lowHeadSPCBound=0.3;
            highHeadSPCBound=1.0;

            lowFaceIntensityBound=0.7;
            highFaceIntensityBound=1.0;
        }
        else
        {
            lowGestureSPCBound=0.25;
            highGestureSPCBound=0.75;

            lowGesturePWRBound=0.25;
            highGesturePWRBound=0.75;

            lowHeadSPCBound=0.25;
            highHeadSPCBound=0.75;

            lowFaceIntensityBound=0.25;
            highFaceIntensityBound=0.75;
        }
    }

    void adaptSequence(List<Signal> temporizedSequence) {
        for(Signal s : temporizedSequence)
        {
            if(s instanceof GestureSignal)
            {
                GestureSignal gs = (GestureSignal) s;
                gs.setSPC(computeGestureSPC(false));
                gs.setPWR(computeGesturePWR(false));
            }
            else if (s instanceof HeadSignal)
            {
                HeadSignal hs = (HeadSignal) s;
                hs.setSPC(computeHeadSPC(false));
            }
            else if (s instanceof FaceSignal)
            {
                FaceSignal fs = (FaceSignal) s;
                fs.setIntensity(computeFaceIntensity(false));
            }
        }
    }

    double computeGestureSPC(boolean gaussian) //true = gaussian, false = uniform
    {
        double spc=0.5;
        if(gaussian)
        {
            spc = r.nextGaussian();
            //because of the -3*sigma to 3*sigma interval of the gaussian distribution,
            //and in java.Random sigma=1, here we are between -3 and +3, so :
            spc = (3+spc)/6;
        }
        else
        {
            spc=r.nextDouble();
        }
        //here we are between 0 and 1 (either case)
        spc= spc*(highGestureSPCBound-lowGestureSPCBound)+lowGestureSPCBound;
        //now spc between lowgestureSPCbound and highGestureSPCbound
        return spc;
    }

    private double computeGesturePWR(boolean gaussian) {
        double pwr=0.5;
        if(gaussian)
        {
            pwr = r.nextGaussian();
            //because of the -3*sigma to 3*sigma interval of the gaussian distribution,
            //and in java.Random sigma=1, here we are between -3 and +3, so :
            pwr = (3+pwr)/6;
        }
        else
        {
            pwr=r.nextDouble();
        }
        //here we are between 0 and 1 (either case)
        pwr= pwr*(highGesturePWRBound-lowGesturePWRBound)+lowGesturePWRBound;
        //now spc between lowgesturePWRbound and highGesturePWRbound
        return pwr;
    }

    private double computeHeadSPC(boolean gaussian) {
        double spc=0.5;
        if(gaussian)
        {
            spc = r.nextGaussian();
            //because of the -3*sigma to 3*sigma interval of the gaussian distribution,
            //and in java.Random sigma=1, here we are between -3 and +3, so :
            spc = (3+spc)/6;
        }
        else
        {
            spc=r.nextDouble();
        }
        //here we are between 0 and 1 (either case)
        spc= spc*(highHeadSPCBound-lowHeadSPCBound)+lowHeadSPCBound;
        //now spc between lowgestureSPCbound and highGestureSPCbound
        return spc;
    }

    private double computeFaceIntensity(boolean gaussian) {
        double intensity=0.5;
        if(gaussian)
        {
            intensity = r.nextGaussian();
            //because of the -3*sigma to 3*sigma interval of the gaussian distribution,
            //and in java.Random sigma=1, here we are between -3 and +3, so :
            intensity = (3+intensity)/6;
        }
        else
        {
            intensity=r.nextDouble();
        }
        //here we are between 0 and 1 (either case)
        intensity= intensity*(highFaceIntensityBound-lowFaceIntensityBound)+lowFaceIntensityBound;
        //now spc between lowgestureSPCbound and highGestureSPCbound
        return intensity;
    }
}
