/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining.structures;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Mathieu
 */
public class SegmentGroup {
    
    public int clusterID;
    public int turntakingType; //0 = recruiter, 1 = candidate, 2 = mixed
    public String originalSequencesFilePath;
    public String minedSequencesFilePath;
    public Map<FrequentSequence, Double > map; //sequenceID, support
    
    /*
     * The map won't work (list types...) how to do it ?
     * 
     */
    
    public class FrequentSequence{
        public List<NVBEvent> nvbevents;
        
        public boolean equals(Object o)
        {
            boolean allequals=true;
            if (o instanceof FrequentSequence)
            {
                FrequentSequence fs = (FrequentSequence) o;
                if(fs.nvbevents.size()==this.nvbevents.size())
                {
                    for (int i=0;i<this.nvbevents.size();i++)
                    {
                        if(!fs.nvbevents.get(i).moda.equals(this.nvbevents.get(i).moda)
                                || !fs.nvbevents.get(i).type.equals(this.nvbevents.get(i).type))
                        {
                            allequals=false;
                            break;
                        }
                    }
                    if(allequals)
                        return true;
                }
            }
            return false;
        }
    }
}
