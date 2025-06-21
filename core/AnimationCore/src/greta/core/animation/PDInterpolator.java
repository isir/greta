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
