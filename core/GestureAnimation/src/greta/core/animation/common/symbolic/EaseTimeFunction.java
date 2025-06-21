/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
