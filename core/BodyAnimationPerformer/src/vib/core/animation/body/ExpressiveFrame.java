package vib.core.animation.body;

import vib.core.animation.Frame;
import vib.core.keyframes.ExpressivityParameters;

/**
 *
 * @author Jing Huang
 */


public class ExpressiveFrame extends Frame{
    protected double _time;
    protected ExpressivityParameters _exp;
    public double getTime() {
        return _time;
    }

    public void setTime(double time) {
        this._time = time;
    }
    
      public ExpressivityParameters getExpressivityParameters() {
        return _exp;
    }

    public void setExpressivityParameters(ExpressivityParameters exp) {
        this._exp = exp;
    }
}
