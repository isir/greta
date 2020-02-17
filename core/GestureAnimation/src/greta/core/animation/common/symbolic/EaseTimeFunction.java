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
package greta.core.animation.common.symbolic;

import greta.core.keyframes.ExpressivityParameters;
import greta.core.util.math.Function;
import java.util.Random;

/**
 *
 * @author Jing Huang
 */
public class EaseTimeFunction {

    Random rand = new Random();
    static EaseTimeFunction ct = new EaseTimeFunction();

    private EaseTimeFunction() {
    }

    public static EaseTimeFunction getInstance() {
        return ct;
    }

    public double getTime(double inputTime, ExpressivityParameters p, Function function) {
        return (double)function.f(inputTime);
        //return inputTime;
//        double f = inputTime;
//        if (p == null) {
//            return f;
//        }
//
//        double pwr = p.pwr;
//        if(pwr == 0)
//            return f;
//
//        if (function != null) {
//            f = (double) function.f((double) f);
//            f = (double) java.lang.Math.pow(f, (1 - pwr * pwr * pwr));
//            return f;
//        } else {
//            if (0.1 < pwr && pwr < 0.3) {
//                f = EquationFunctions.easeInOutSine(f);
//            } else if (0.3 <= pwr && pwr < 0.5) {
//                f = EquationFunctions.easeOutQuad(f);
//                f = (double) java.lang.Math.pow(f, (1 - pwr));
//            } else if (0.5 <= pwr && pwr < 0.7) {
//                f = EquationFunctions.easeOutQuad(f);
//                f = (double) java.lang.Math.pow(f, (1 - pwr));
//            } else if (pwr >= 0.7 && pwr < 0.95) {
//                f = EquationFunctions.easeOutBack(f, 0.20158f); //overshoot
//                f = (double) java.lang.Math.pow(f, (1 - pwr));
//            } else if (pwr >= 0.95) {
//                f = EquationFunctions.easeOutBounce(f, 0.1f); //rebounce
//            }
//            return f;
//        }
    }
}
