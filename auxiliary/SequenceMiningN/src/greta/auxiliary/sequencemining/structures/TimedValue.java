/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining.structures;

import java.math.BigDecimal;

/**
 *
 * @author Mathieu
 */
public class TimedValue {
    
    public int time;
    public BigDecimal value;
    
    public TimedValue(int time, BigDecimal value)
    {
        this.time=time;
        this.value=value;
    }
}
