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
package greta.core.animation.common.easingcurve;

/**
 *
 * @author Jing Huang
 */
public abstract class EasingCurveFuntion {
    String _t;  //type
    double _p = 0.3f; // period
    double _a = 1.0f;  //amplitude
    double _o = 1.70158f;  //overshoot

    abstract double getValue(double t);

}
