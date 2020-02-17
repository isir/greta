/*
 * This file is part of Greta.
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
package greta.core.animation.common.Frame;

import greta.core.keyframes.ExpressivityParameters;
import greta.core.util.Constants;

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
