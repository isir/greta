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
