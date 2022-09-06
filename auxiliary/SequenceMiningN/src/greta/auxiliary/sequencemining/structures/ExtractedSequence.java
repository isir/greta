/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining.structures;

import java.util.List;

/**
 *
 * @author Mathieu
 */
public class ExtractedSequence {
    
    public List<String> events;
    public int clusterId;
    public String attitudeType;//F or D
    public double support;
    public double confidence;
    public double lift;
    
    public ExtractedSequence(List<String> list, int cl, String att, double sup,
            double conf, double lif)
    {
        events = list;
        clusterId = cl;
        attitudeType = att;
        support = sup;
        confidence = conf;
        lift = lif;
    }
}
