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
