/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining.structures;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author Mathieu
 */
public class AttitudeVariationEvent {
    
    public AttitudeVariationEvent(VariationType var, BigDecimal v, int t, int dur,String file)
    {
        type=var;
        value=v;
        time=t;
        duration=dur;
        stringvalue=v.toString();
        normalizedDuration=dur;
        original_duration=dur;
        sourceFile=file;
    }
    
    public String sourceFile;
    public VariationType type;
    public BigDecimal value;
    public int time;
    public int duration;
    public int original_duration;
    public String stringvalue;
    
    public double normalizedDuration;
    public void normalizeDuration(double minDuration, double maxDuration)
    {
        normalizedDuration=(normalizedDuration-minDuration)/(maxDuration-minDuration);
    }
    
    public enum VariationType{
        PLATEAU,
        RISE,
        FALL
    }
}
