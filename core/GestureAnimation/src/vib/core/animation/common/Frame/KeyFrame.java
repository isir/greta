/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.Frame;

import vib.core.keyframes.ExpressivityParameters;
import vib.core.util.Constants;

/**
 *
 * @author Jing Huang
 */
public class KeyFrame extends Frame {

    double _time = -1;
    int _count = -1;
//    static int _framesPerSecond = Constants.FRAME_PER_SECOND;
    ExpressivityParameters _p;
    String _phase;
    public boolean _flag = false;
    public String getPhase() {
        return _phase;
    }

    public void setPhase(String _phase) {
        this._phase = _phase;
    }

    public KeyFrame(double time) {
        setTime(time);
    }


    public void setTime(double time) {
        _time = time;
        _count = (int) (_time * Constants.FRAME_PER_SECOND);
    }

    public double getTime() {
        return _time;
    }

    public void setCountNumber(int count) {
        _count = count;
    }

    public int getCountNumber() {
        return _count;
    }

//    public static void setFramePerSecond(int framesPerSecond) {
//        _framesPerSecond = framesPerSecond;
//    }

    public static int getFramePerSecond() {
        return Constants.FRAME_PER_SECOND;
    }


    public ExpressivityParameters getExpressivityParameters() {
        return _p;
    }

    public void setExpressivityParameters(ExpressivityParameters _p) {
        this._p = _p;
    }
}
