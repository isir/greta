/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.core.animation;

import java.util.ArrayList;
import java.util.List;
import vib.core.util.math.pid.*;
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
