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
package greta.core.animation.body;

import greta.core.animation.Frame;
import greta.core.keyframes.ExpressivityParameters;

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
