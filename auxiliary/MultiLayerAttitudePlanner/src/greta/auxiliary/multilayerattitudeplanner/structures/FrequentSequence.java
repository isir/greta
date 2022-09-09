/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner.structures;

import greta.auxiliary.multilayerattitudeplanner.structures.Turn;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeDimension;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Mathieu
 */
public class FrequentSequence {
    
    private List<String> nvbEvents;
    private Integer clusterID;
    private AttitudeDimension ad;
    private Turn tn;
    private Double value;
    private Integer countInCluster;
    private Integer countTotal;
    private Double support;
    private Double confidence;
    private Double lift;
    private Double conviction;
    private int ID;
        
    public FrequentSequence(){}
    
    //read from sequences.csv file
    public FrequentSequence(String[] dataLine, int id)
    {
        this.ID=id;
        nvbEvents=readSequenceString(dataLine[0]);
        clusterID=Integer.valueOf(dataLine[1]);
        
        if(dataLine[2].equalsIgnoreCase("Frd"))
            ad=AttitudeDimension.Friendliness;
        else if(dataLine[2].equalsIgnoreCase("Dom"))
            ad=AttitudeDimension.Dominance;
        
        if(dataLine[3].equalsIgnoreCase("Spk"))
            tn= Turn.SPEAKING;
        else if(dataLine[3].equalsIgnoreCase("Lstn"))
            tn= Turn.LISTENING;
        
        value=Double.valueOf(dataLine[4]);
        countInCluster=Integer.valueOf(dataLine[5]);
        countTotal=Integer.valueOf(dataLine[6]);
        support=Double.valueOf(dataLine[7]);
        confidence=Double.valueOf(dataLine[8]);
        lift=Double.valueOf(dataLine[9]);
        conviction=Double.valueOf(dataLine[10]);           
    }
    
    private List<String> readSequenceString(String seq)
    {
        List<String> lst = new ArrayList<String>();
        for(String s:seq.split("-"))
        {
            if(!s.equalsIgnoreCase("null"))
                lst.add(s);
        }
        return lst;
    }

    /**
     * @return the nvbEvents
     */
    public List<String> getNvbEvents() {
        return nvbEvents;
    }

    /**
     * @return the ad
     */
    public AttitudeDimension getAttitudeDimension() {
        return ad;
    }

    /**
     * @return the tn
     */
    public Turn getTurn() {
        return tn;
    }

    /**
     * @return the value
     */
    public Double getValue() {
        return value;
    }

    /**
     * @return the support
     */
    public Double getSupport() {
        return support;
    }

    /**
     * @return the confidence
     */
    public Double getConfidence() {
        return confidence;
    }

    /**
     * @return the lift
     */
    public Double getLift() {
        return lift;
    }

    /**
     * @return the conviction
     */
    public Double getConviction() {
        return conviction;
    }

    /**
     * @return the ID
     */
    public int getID() {
        return ID;
    }

    /**
     * @return the clusterID
     */
    public Integer getClusterID() {
        return clusterID;
    }
    
}
