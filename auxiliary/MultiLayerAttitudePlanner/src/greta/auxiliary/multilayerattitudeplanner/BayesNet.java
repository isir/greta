/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package greta.auxiliary.multilayerattitudeplanner;

import java.io.File;
import greta.auxiliary.multilayerattitudeplanner.structures.SequenceProbabilityTuple;
import greta.auxiliary.multilayerattitudeplanner.structures.NVBEventType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import greta.auxiliary.multilayerattitudeplanner.structures.AttitudeVariation;
import weka.classifiers.bayes.net.BIFReader;
import weka.classifiers.bayes.net.estimate.DiscreteEstimatorBayes;

/**
 *
 * @author Mathieu
 */
public class BayesNet {
    
    public BIFReader br;
    public Map<String,String[]> signalenum;
    public Map<String, Integer> nodeIntByString;
    
    public Map<String,List<SequenceProbabilityTuple>> seqProbabilitiesMap;
        
    public BayesNet(String filename,boolean dominance) throws Exception
    {
        this.seqProbabilitiesMap = new LinkedHashMap<String, List<SequenceProbabilityTuple>>();
        this.br = new BIFReader();
        this.br.processFile(filename);
        //variable : signal 1,...,signal 7, frdvar
        //frdvar : smalldecr, bigdecr, smallincr, bigincr
        this.signalenum = new LinkedHashMap<String, String[]>();
        this.nodeIntByString = new LinkedHashMap<String, Integer>();
        for(int i=0;i<this.br.getNrOfNodes();i++)
        {
            this.nodeIntByString.put(this.br.getNodeName(i), i);
        }
        
        for(int n=1;n<8;n++)
        {
            String[] senum = new String[this.br.getCardinality(nodeIntByString.get("Signal"+n))];
            for(int i=0;i<senum.length;i++)
            {
                senum[i] = this.br.getNodeValue(nodeIntByString.get("Signal"+n), i);
            }
        
            this.signalenum.put("Signal"+n, senum);
        }
        String[] attenum = new String[4];
        if(dominance)
        {
            for(int i=0;i<attenum.length;i++)
            {
                attenum[i] = this.br.getNodeValue(nodeIntByString.get("DomVar"), i);
            }
        }
        else
        {
            for(int i=0;i<attenum.length;i++)
            {
                attenum[i] = this.br.getNodeValue(nodeIntByString.get("FrdVar"), i);
            }
        }
        this.signalenum.put("Var", attenum);
        
        /*String[] s0enum = {"HeadAt","HeadTiltL","HeadDownL","HeadUpH","HeadSideH","HeadNodLN",
            "HeadShakeNN","RestArmsCrossed","RestHandsTogether","RestOver","RestUnder","EyebrowUpL","GestCommLL",
            "GestAdaptorArms","GestObjectManipulation","SmileL","BodyStraight","BodyLeanL","BodyReclineL"};//new String[19];
        String[] sothersenum = {"HeadAt","HeadTiltL","HeadDownL","HeadUpH","HeadSideH","HeadNodLN",
            "HeadShakeNN","RestArmsCrossed","RestHandsTogether","RestOver","RestUnder","EyebrowUpL","GestCommLL",
            "GestAdaptorArms","GestObjectManipulation","SmileL","BodyStraight","BodyLeanL","BodyReclineL","No"};//new String[19];
        String[] frdvar = {"SmallDecr","BigDecr","SmallIncr","BigIncr"};
        this.signalenum.put("Signal1", s0enum);
        this.signalenum.put("Signal2", sothersenum);
        this.signalenum.put("Signal3", sothersenum);
        this.signalenum.put("Signal4", sothersenum);
        this.signalenum.put("Signal5", sothersenum);
        this.signalenum.put("Signal6", sothersenum);
        this.signalenum.put("Signal7", sothersenum);
        this.signalenum.put("FrdVar", frdvar);*/
        
        
        
    }
    
    public void orderSequences(){
        for(String s : seqProbabilitiesMap.keySet())
        {
            seqProbabilitiesMap.put(s, order(seqProbabilitiesMap.get(s)));
        }
    }
    
    public List<SequenceProbabilityTuple> order(List<SequenceProbabilityTuple> probaToOrder)
    {
        List<SequenceProbabilityTuple> newList = new ArrayList<SequenceProbabilityTuple>();
        for(SequenceProbabilityTuple spt : probaToOrder)
        {
            if(newList.isEmpty())
            {
                newList.add(spt);
            }
            else
            {
                boolean added=false;
                for(int i=0;i<newList.size();i++)
                {
                    if(spt.probability>=newList.get(i).probability)
                    {
                        newList.add(i,spt); 
                        added=true;
                        break;
                    }
                } 
                if(!added)
                    newList.add(spt);
            }
        }
        return newList;
    }
    
    public List<SequenceProbabilityTuple> prune(List<SequenceProbabilityTuple> probaToPrune, double pruningfactor)
    {
        double max = -1;
        for(SequenceProbabilityTuple spt : probaToPrune)
        {
            if(spt.probability>max)
                max=spt.probability;
        }
        
        List<SequenceProbabilityTuple> newList = new ArrayList<SequenceProbabilityTuple>();
        for(SequenceProbabilityTuple spt : probaToPrune)
        {
            if(spt.probability>max*pruningfactor)
                newList.add(spt);
        }
        return newList;
    }
    
    /*public void computeAllSequenceProbabilities(int length, String[] attitudes) throws Exception
    {
        for(String att : attitudes)
        {
            List<SequenceProbabilityTuple> previouslist = new ArrayList<SequenceProbabilityTuple>();
            //Map<String[],Double> previousmap = new HashMap<String[], Double>();
            for(int i=0;i<length;i++)
            {
                List<SequenceProbabilityTuple> list = new ArrayList<SequenceProbabilityTuple>();
                
                //Map<String[],Double> map = new HashMap<String[], Double>();

                if(i==0)
                {
                    for(String s : signalenum.get("Signal1"))
                    {
                        String[] strings = {s};
                        double p =getProbabilitySequence(0,att,strings);
                        list.add(new SequenceProbabilityTuple(strings, p));
                    }
                }
                else
                {
                    for(SequenceProbabilityTuple spt : previouslist)
                    {
                        List<NVBEventType>prevstrings = spt.signals;
                        for(String s : signalenum.get("Signal"+i))
                        {
                            if(!s.equalsIgnoreCase("No") && !prevstrings.get(prevstrings.size()-1).equals(s))
                            {
                                String[] newstrings = new String[prevstrings.size()+1];
                                for(int k=0;k<prevstrings.size();k++)
                                {
                                    newstrings[k]=prevstrings.get(k).toString();
                                }
                                newstrings[newstrings.length-1]=s;
                                double p = getProbabilitySequence(i, att, newstrings);
                                list.add(new SequenceProbabilityTuple(newstrings, p));
                            }
                        }
                    }
                }

                list = prune(list,0.01);
                previouslist=list;
                seqProbabilitiesMap.put(att, list);
            }
        }
        
    }*/
        
    public List<SequenceProbabilityTuple> getOrderedTuplesContainingSignals(int length, AttitudeVariation attvar,//String attitude, 
            List<NVBEventType> signals, MinedSequenceQuerier msq,List<NVBEventType> timings)throws Exception
    {
        System.out.println(attvar.getDimension().toString()+" "+attvar.getCluster().toString()+" "+timings.toString());
        //##########################
        //TODO : refactoring so we don't have to recompute all the 
        //sequences of length n-1, n-2,...,1 for computing length n
        //##########################
        
        
        Map<String[],Double> previousValues = new LinkedHashMap<String[], Double>();
                
        double bestProbaScore=-1.0;
        //i=0
        //for(String s : signalenum.get("Signal1"))
        for(int i=0;i<signalenum.get("Signal1").length;i++)
        {
            String[] strings = {signalenum.get("Signal1")[i]};
            double p =getProbabilitySequence(0,attvar.getCluster().toString()//attitude
                    ,strings);
            previousValues.put(strings, p);
            if(p>bestProbaScore)
                bestProbaScore=p;
        }
        Map<Integer, Map<String[],Double>> allValues = new HashMap<Integer, Map<String[], Double>>();
        allValues.put(1, previousValues);
        
        //after i=0
        for(int i=1;i<length;i++)
        {
            //System.out.print("Building length ("+i+"/"+(length-1)+") sequences... ");
            bestProbaScore=-1;
            Map<String[],Double> thisValues = new LinkedHashMap<String[], Double>();
            for(String[] prevstrings : previousValues.keySet())
            {
                for(String s : signalenum.get("Signal"+i))
                {
                    if(!s.equalsIgnoreCase("No") && !prevstrings[prevstrings.length-1].equals(s))
                    {
                        String[] newstrings = new String[prevstrings.length+1];
                        for(int j=0;j<prevstrings.length;j++)
                        {
                            newstrings[j]=prevstrings[j];
                        }
                        newstrings[newstrings.length-1]=s;
                        double p = getProbabilitySequence(i, attvar.getCluster().toString(), newstrings);
                        if(p>0.002*bestProbaScore)
                            thisValues.put(newstrings, p); 
                        if(p>bestProbaScore)
                            bestProbaScore=p;
                    }
                }
            }
            allValues.put(i+1, thisValues);
            previousValues=thisValues;
            System.out.println("Done.");
        }
        
        double maxconfidence=0.25;
        List<SequenceProbabilityTuple> listWithSubsequence=new ArrayList<SequenceProbabilityTuple>();
        for(int i=1;i<=length;i++)
        {
            //System.out.println("Evaluating length "+i+" sequences");
            for(String[] strings : allValues.get(i).keySet())
            {
                if(NVBEventType.isSubsequence(NVBEventType.readArrayString(strings),timings))
                {
                double confidence=1;
                if(msq!=null)
                {
                    if(msq.countSupportAll(strings,attvar)==0)
                    {
                        continue;
                    }
                    confidence = ((double)msq.countSupport(strings, attvar))/((double)msq.countSupportAll(strings,attvar));
                }
                    //confidence =Math.pow(confidence, i);
                
                
                if(confidence>=maxconfidence)
                {
                    if(confidence>maxconfidence)
                    {
                        listWithSubsequence.clear();
                    }
                    
                    maxconfidence=confidence;
                    listWithSubsequence.add(new SequenceProbabilityTuple(strings, //allValues.get(i).get(strings)*
                            confidence));
                }
                }
            }
        }
        Collections.shuffle(listWithSubsequence);
        return listWithSubsequence;
        //return order(listWithSubsequence) ;
    }
    
    /*public SequenceProbabilityTuple getBestSequenceContainingSignals(int length, AttitudeVariation attvar,//String attitude, 
            List<NVBEventType> signals, MinedSequenceQuerier msq) throws Exception
    {
        //##########################
        //TODO : refactoring so we don't have to recompute all the 
        //sequences of length n-1, n-2,...,1 for computing length n
        //##########################
        
        
        Map<String[],Double> previousValues = new LinkedHashMap<String[], Double>();
                
        //i=0
        //for(String s : signalenum.get("Signal1"))
        for(int i=0;i<signalenum.get("Signal1").length;i++)
        {
            String[] strings = {signalenum.get("Signal1")[i]};
            double p =getProbabilitySequence(0,attvar.getCluster().toString()//attitude
                    ,strings);
            previousValues.put(strings, p);
        }
        
        //after i=0
        for(int i=1;i<length;i++)
        {
            Map<String[],Double> thisValues = new LinkedHashMap<String[], Double>();
            for(String[] prevstrings : previousValues.keySet())
            {
                for(String s : signalenum.get("Signal"+i))
                {
                    if(!s.equalsIgnoreCase("No") && !prevstrings[prevstrings.length-1].equals(s))
                    {
                        String[] newstrings = new String[prevstrings.length+1];
                        for(int j=0;j<prevstrings.length;j++)
                        {
                            newstrings[j]=prevstrings[j];
                        }
                        newstrings[newstrings.length-1]=s;
                        double p = getProbabilitySequence(i, attvar.getCluster().toString(), newstrings);
                        thisValues.put(newstrings, p); 
                    }
                }
            }

            previousValues=thisValues;
        }
        
        String[] returnseq = {};
        double p =-1.0;
        for(String[] strings : previousValues.keySet())
        {
            double confidence = ((double)msq.countSupport(strings, attvar))/(double)msq.countSupportAll(strings,attvar);
            if(NVBEventType.isSubsequence(NVBEventType.readArrayString(strings),signals)
                    //NVBEventType.readArrayString(strings).containsAll(signals)
                    &&previousValues.get(strings)*confidence>p)
            {
                returnseq=strings; 
                p =previousValues.get(strings)*confidence;
            }
        }
        return new SequenceProbabilityTuple(returnseq, p);
    }*/
    
    /*public SequenceProbabilityTuple getBestSequence(int length, AttitudeVariation attvar,//String attitude
            SequencePlanner sp) throws Exception
    {
        Map<String[],Double> previousValues = new HashMap<String[], Double>();
        Map<String[],Double> thisValues = new HashMap<String[], Double>();
                
        //i=0
        for(String s : signalenum.get("Signal1"))
        {
            String[] strings = {s};
            double p =getProbabilitySequence(0,attvar.getCluster().toString(),strings);
            previousValues.put(strings, p);
        }
        
        //after i=0
        for(int i=1;i<length;i++)
        {
            thisValues = new HashMap<String[], Double>();
            for(String[] prevstrings : previousValues.keySet())
            {
                for(String s : signalenum.get("Signal"+i))
                {
                    if(!s.equalsIgnoreCase("No") && !prevstrings[prevstrings.length-1].equals(s))
                    {
                        String[] newstrings = new String[prevstrings.length+1];
                        for(int j=0;j<prevstrings.length;j++)
                        {
                            newstrings[j]=prevstrings[j];
                        }
                        newstrings[newstrings.length-1]=s;
                        double p = getProbabilitySequence(i, attvar.getCluster().toString(), newstrings);
                        thisValues.put(newstrings, p); 
                    }
                }
            }

            previousValues=thisValues;
        }
        
        String[] returnseq = {};
        double p =0;
        for(String[] strings : previousValues.keySet())
        {
            double confidence = ((double)sp.countSupport(strings, attvar))/(double)sp.countSupportAll(strings);
            if(previousValues.get(strings)*confidence>p)
            {
                returnseq=strings; 
                p =previousValues.get(strings)*confidence;
            }
        }
        return new SequenceProbabilityTuple(returnseq, p);
    }*/
    
    public double getProbabilitySequence(int length, String attitude, String[] signals) throws Exception
    {
        double p=0;
        p = getProbability(signals[0], 1, attitude, "");
        for(int i=1;i<length;i++)
        {
             p=p*getProbability(signals[i], i+1, attitude, signals[i-1]);
       }
        
        return p;
    }
    
    //la valeur du signal "signaltype" en position "noeud", dans le cas de l'attitude "attitude" et avec le signal precedent "signalprecedent"
    public double getProbability(String signaltype, int noeud, String attitude, String signalprecedent) 
    {
        double p=0;
        
        int previousSignalNode = 0;
        if(noeud!=1)
        {
            previousSignalNode = nodeIntByString.get("Signal"+(noeud-1));
        }
        int thisSignalNode = nodeIntByString.get("Signal"+noeud);
        int thisAttitude = -1;
        String[] arr = signalenum.get("Var");
        for(int i=0;i<signalenum.get("Var").length;i++)
        {
            if(arr[i].equalsIgnoreCase(attitude))
            {
                thisAttitude=i;
            }  
        }
        int thisSignalValue = 0;
         arr = signalenum.get("Signal"+noeud);
        for(int i=0;i<arr.length;i++)
        {
            if(arr[i].equalsIgnoreCase(signaltype))
            {
                thisSignalValue=i;
            }
        }
        
        int previousSignalValue = 0;
        if(previousSignalNode!=0)
        {
           arr = signalenum.get("Signal"+(noeud-1));
           for(int i=0;i<arr.length;i++)
           {
               if(arr[i].equalsIgnoreCase(signalprecedent))
               {
                   previousSignalValue=i;
               }
           }
        }
        //p = ((DiscreteEstimatorBayes)br.m_Distributions[thisSignalNode][previousSignalNode*4+thisAttitude]).getProbability(thisSignalValue);
        
        try{
        p = ((DiscreteEstimatorBayes)br.m_Distributions[thisSignalNode][previousSignalValue*4+thisAttitude]).getProbability(thisSignalValue);
        }
        catch(Exception e){
            System.err.println("errur");
        }
        
        return p;
    }
    
    
}
