/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner.structures;

import java.util.Random;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeDimension;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeCluster;

/**
 *
 * @author Mathieu
 */
public class ValuedAttitudeVariation {

    private double value;
    private AttitudeVariation attvar;
    
    public ValuedAttitudeVariation(AttitudeDimension ad, double val, AttitudeCluster ac)
    {
        this.value=val;
        this.attvar=new AttitudeVariation(ad, ac);
    }
    
    public ValuedAttitudeVariation(AttitudeDimension ad)
    {
        this.value=0.0;
        this.attvar = new AttitudeVariation(ad);
    }
    
    public ValuedAttitudeVariation(AttitudeDimension ad, double val)
    {
        this.value=val;
        this.attvar = new AttitudeVariation(ad);
        if(val<-0.2)
            {attvar.setCluster(AttitudeCluster.BigDecr);}
        else if(val>0.2)
            {attvar.setCluster(AttitudeCluster.BigIncr);}
        else if(val<-0.0 && val>=-0.2)
            {attvar.setCluster(AttitudeCluster.SmallDecr);}
        else if(val>0.0 && val<=0.2)
            {attvar.setCluster(AttitudeCluster.SmallIncr);}
        else if(val==0.0)
        {
            attvar.setCluster(AttitudeCluster.Null);
        }
    }
        
    public void setValue(double val)
    {
        this.value=val;
    }
    
    public double getValue()
    {
        return this.value;
    }

    public AttitudeVariation getAttitudeVariation() {
        return attvar;
    }

    public void setAttitudeVariation(AttitudeVariation attvar) {
        this.attvar = attvar;
    }
    
}
