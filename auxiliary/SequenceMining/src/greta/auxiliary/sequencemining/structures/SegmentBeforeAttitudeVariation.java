/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining.structures;

import greta.auxiliary.sequencemining.structures.NVBEvent;
import greta.auxiliary.sequencemining.structures.AttitudeVariationEvent;
import java.util.List;

/**
 *
 * @author Mathieu
 */
public class SegmentBeforeAttitudeVariation {
        
    public int start;
    public int end;
    public List<NVBEvent> sequence;
    public int variationClusterID;
    public String sourcefile;
    public int duration;
    public AttitudeVariationEvent ave;
    
    public List<NVBEvent> turns; 
    
    public SegmentBeforeAttitudeVariation(int start_time, int end_time, 
            List<NVBEvent> listNVBEvents, int clusterID, String file, AttitudeVariationEvent attvarev, List<NVBEvent> trns)
    {
        start = start_time;
        end = end_time;
        sequence = listNVBEvents;
        variationClusterID = clusterID;
        sourcefile=file;
        ave= attvarev;
        duration = end-start;
        turns=trns;
    }
    
    public double getSpeakingPercentage()
    {
        double percentage=0.0;
        
        for(int i=0;i<turns.size()-1;i++)
        {
            if(turns.get(i).type.equalsIgnoreCase("Recruiter"))
            {
                percentage+=turns.get(i+1).start_time-turns.get(i).start_time;
            }
        }
         //add last      
        if(turns.get(turns.size()-1).type.equalsIgnoreCase("Recruiter"))
        {
            percentage+= end-turns.get(turns.size()-1).start_time;
        }
        
        return percentage/(end-start);
    }
    

}
