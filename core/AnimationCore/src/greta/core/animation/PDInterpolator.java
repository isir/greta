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

import greta.core.util.math.pid.PIDController;
import greta.core.util.math.pid.PIDState;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public class PDInterpolator implements Interpolator<Double> {
    PIDState state = new PIDState();
    PIDController pd = new PIDController();
    @Override
    public List<Double> interpolate(Double t1, Double t2, int nb) {
        ArrayList<Double> re = new ArrayList<Double>();
        state.updateState(t1);
        double current = t1;
        for(int i = 0; i < nb; ++i){
            double dif = pd.updatePD(state, t1, t2);
            current = dif + current;
            re.add(current);
        }
        return re;
    }

}
