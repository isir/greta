/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner.structures;

import java.util.ArrayList;
import java.util.List;
import greta.auxiliary.multilayerattitudeplanner.structures.NVBEventType;
import greta.core.util.log.Logs;

/**
 *
 * @author Mathieu
 */
        
public class SequenceProbabilityTuple{
    public double probability;
    public List<NVBEventType> signals;

    public SequenceProbabilityTuple(String[] signals, double probability)
    {
        this.probability=probability;
        this.signals = new ArrayList<NVBEventType>();
        for(int i=0;i<signals.length;i++)
        {
            this.signals.add(NVBEventType.getNVBEventTypeByString(signals[i]));
        }
    }
    
    public SequenceProbabilityTuple()
    {
        this.probability=0.0;
        this.signals = new ArrayList<NVBEventType>();
    }
    
    /*
     * Output a sequence to console
     */
    public void printSequence()
    {
        String str = "Sequence p=";
        str+=probability+" events=";
        for (int i = 0; i < signals.size(); i++) {
            str += signals.get(i).toString() + " ";
        }
        System.out.println(str);
        Logs.info(str);
    }
}
