/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeCluster;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeDimension;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeVariation;
import greta.auxiliary.multilayerattitudeplanner.structures.NVBEventType;
import greta.auxiliary.sequencemining.Segmenter;
import greta.auxiliary.sequencemining.SeqMinMain;

/**
 *
 * @author Mathieu
 */
public class MinedSequenceQuerier {
    
    public final String NVBEVENT_INTEGER_MAP = ".\\Common\\Data\\MultiLayerAttitude\\Source\\simpleNVBEventsToIntegerMap.txt";
    public final String SEGMENTS_DIR = ".\\Common\\Data\\MultiLayerAttitude\\Source\\";
    public final Map<AttitudeDimension,Map<AttitudeCluster, List<List<NVBEventType>>>> segmentsMap;
    
    public MinedSequenceQuerier() throws IOException
    {
        segmentsMap = new HashMap<AttitudeDimension, Map<AttitudeCluster, List<List<NVBEventType>>>>();
        
         Segmenter   seg = new Segmenter(NVBEVENT_INTEGER_MAP);
        SeqMinMain.retrieveSequences(SEGMENTS_DIR+"\\Dominance\\BigDecr.raw");
        for(AttitudeDimension ad : AttitudeDimension.values())
        {
            Map<AttitudeCluster, List<List<NVBEventType>>> dimMap = 
                    new HashMap<AttitudeCluster, List<List<NVBEventType>>>();
            for(AttitudeCluster ac : AttitudeCluster.values())
            {
                if(!ac.equals(AttitudeCluster.Null))
                {
                    List<List<NVBEventType>> sequencesInSegment = new ArrayList<List<NVBEventType>>();
                    for(List<String> list : SeqMinMain.retrieveSequencesByEventType(seg, SEGMENTS_DIR+
                        "\\"+ad.toString()+"\\"+ac.toString()+".raw"))
                    {
                        sequencesInSegment.add(NVBEventType.readListString(list));
                    }
                    dimMap.put(ac, sequencesInSegment);
                    /*segmentsMap.put(new AttitudeVariation(ad, ac), 
                        NVBEventType.readListString(SeqMinMain.retrieveSequencesByEventType(SEGMENTS_DIR+
                        "\\"+ad.toString()+"\\"+ac.toString()+".raw")));*/
                }
                else
                {
                    List<List<NVBEventType>> sequencesInSegment = new ArrayList<List<NVBEventType>>();
                    for(List<String> list : SeqMinMain.retrieveSequencesByEventType(seg, SEGMENTS_DIR+
                        "\\"+ad.toString()+"\\All.raw"))
                    {
                        sequencesInSegment.add(NVBEventType.readListString(list));
                    }
                    dimMap.put(ac, sequencesInSegment);
                    /*segmentsMap.put(new AttitudeVariation(ad, ac), 
                        NVBEventType.readListString(sequencemining.SeqMinMain.retrieveSequencesByEventType(SEGMENTS_DIR+
                        "\\"+ad.toString()+"\\All.raw")));*/
                }
            }
            segmentsMap.put(ad, dimMap);
        }
    }
    
    public int countSupportAll(String[] seqToCount, AttitudeVariation attvar) {
        if(attvar.getDimension().equals(AttitudeDimension.Friendliness))
        {
            return countSupport(seqToCount, new AttitudeVariation(AttitudeDimension.Friendliness, AttitudeCluster.BigDecr))+
                    countSupport(seqToCount, new AttitudeVariation(AttitudeDimension.Friendliness, AttitudeCluster.SmallDecr))+
                    countSupport(seqToCount, new AttitudeVariation(AttitudeDimension.Friendliness, AttitudeCluster.SmallIncr))+
                    countSupport(seqToCount, new AttitudeVariation(AttitudeDimension.Friendliness, AttitudeCluster.BigIncr));
        }
        else
        {
            return countSupport(seqToCount, new AttitudeVariation(AttitudeDimension.Dominance, AttitudeCluster.BigDecr))+
                    countSupport(seqToCount, new AttitudeVariation(AttitudeDimension.Dominance, AttitudeCluster.SmallDecr))+
                    countSupport(seqToCount, new AttitudeVariation(AttitudeDimension.Dominance, AttitudeCluster.SmallIncr))+
                    countSupport(seqToCount, new AttitudeVariation(AttitudeDimension.Dominance, AttitudeCluster.BigIncr));
        }
    }
    
    public int countSupport(String[] seqToCount, AttitudeVariation attvar) {
        int support=0;
        
        List<List<NVBEventType>> sequencesList = segmentsMap.get(attvar.getDimension()).get(attvar.getCluster());
        
        for(int seqNumber=0; seqNumber<sequencesList.size();seqNumber++)
        {
            List<NVBEventType> thisSegment = sequencesList.get(seqNumber);
            //String[] dataSeqSplit = sequencesList.get(seqNumber).split(" ");
            boolean found=false;
            int iCountedSeq =0;
            int iDataSeq =0;
            while(iDataSeq<thisSegment.size())
            {//tant qu'on n'a pas epuise la sequence ou on regarde
                if(thisSegment.get(iDataSeq).equals(seqToCount[iCountedSeq]))
                {//si les elements sont egaux dans la source et les donnees
                    if(iCountedSeq==seqToCount.length-1)
                    {//si on est au dernier
                        support++;
                        found=true;
                        break;
                    }
                    iCountedSeq++;
                }
                iDataSeq++;
            }
        }
        return support;
    }
}
