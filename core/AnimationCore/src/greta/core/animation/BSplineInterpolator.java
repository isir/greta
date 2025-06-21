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
package greta.core.animation;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public class BSplineInterpolator implements Interpolator<Double>{
    double offset1X = -0.1;
    double offset2X = 0.1;
    @Override
    public List<Double> interpolate(Double t1, Double t2, int nb) {
        double c1 = t1 + offset1X;
        double c2 = t2 + offset2X;
        ArrayList<Double> re = new ArrayList<Double>();
        for(int i = 0; i < nb; ++i){
            double t = (double)i / (double)nb;
            double p0 =  Math.pow((1 - t), 3);
            double p1 =  Math.pow((1 - t), 2) * t * 3;
            double p2 =  (1 - t) * t * t * 3;
            double p3 = Math.pow(t, 3);
            double current = t1 * p0 + c1 * p1 + c2 * p2 + t2 * p3;
            re.add(current);
        }
        return re;
    }

}
