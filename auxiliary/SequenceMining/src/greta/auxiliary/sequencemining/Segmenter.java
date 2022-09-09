/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining;

import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import greta.auxiliary.sequencemining.structures.AttitudeVariationEvent;
import greta.auxiliary.sequencemining.structures.AttitudeVariationEvent.VariationType;
import greta.auxiliary.sequencemining.structures.NVBEvent;
import greta.auxiliary.sequencemining.structures.SegmentBeforeAttitudeVariation;

/**
 *
 * @author Mathieu
 */
public class Segmenter {
    
    public static Map<String,Integer> NVBEVENT_INTEGER_MAP;
    public static Map<Integer,String> REVERSE_NVBEVENT_INTEGER_MAP;
    
    public Segmenter(String eventToIntegerMapFile) throws FileNotFoundException, IOException
    {
        NVBEVENT_INTEGER_MAP = new HashMap<>();
        REVERSE_NVBEVENT_INTEGER_MAP = new HashMap<>();
        CSVReader csvr = new CSVReader(new FileReader(new File(eventToIntegerMapFile)),',');
        String[] line;
        while((line=csvr.readNext()) != null)
        {
            if(!line[0].startsWith("#"))
            {
                NVBEVENT_INTEGER_MAP.put(line[0], Integer.valueOf(line[1]));
                REVERSE_NVBEVENT_INTEGER_MAP.put(Integer.valueOf(line[1]),line[0]);
            }
        }
    }
    
   /* private static void saveSegmentation(List<List<NVBEvent>> segmentation, String segseq) throws IOException {
        
        BufferedWriter bw = null;
        if(!segseq.equals(""))
        {
            //output
            File f = new File(segseq); 
            if(f.exists())
            {
                System.err.println("output file already exists");
            }
             bw = new BufferedWriter(new FileWriter(f)); //writer
        }
        
        for(List<NVBEvent> sequence : segmentation)
        {
            if(bw!=null)
            {
                for(NVBEvent ev : sequence)
                    bw.write("{"+ev.start_time+","+ev.type+"},");
                
                bw.write("\n");
                bw.flush();
            }
        }
    }*/
    
        
    
    //given a start, end time, a source file name and a map of the source files NVB sequence translations
    //return the NVBEvent segmentation
    private static List<NVBEvent> getSequenceInFile(int startSegment, int endSegment, String sourceFile,
            Map<String,List<NVBEvent>> mapSeqFiles, Map<String, String>  attitudeFileToNVBFileMap) 
    {
        List<NVBEvent> segmentation = new LinkedList<>();
        List<NVBEvent> events = mapSeqFiles.get(attitudeFileToNVBFileMap.get(sourceFile));
        if(events==null)
        {
            System.err.println("didnt find any correspondance between attitude file and nvb file sequences");
            return null;
        }
        for(NVBEvent nvbv : events)
        {
            if(nvbv.start_time>endSegment) //after end time of segment
                break;
            else if(nvbv.start_time<startSegment) //before start time fo segment
                continue;
            else
            {
                segmentation.add(nvbv);
            }
        }
        return segmentation;
    }

    //taking all the attitude variations and the cluster centers, get a segmentation of the data
    public List<SegmentBeforeAttitudeVariation> doSegmentation(List<AttitudeVariationEvent> allvariations, 
            double[][] clustercenters,Map<String,List<NVBEvent>> mapSeqFiles,
            Map<String,List<NVBEvent>> mapTurnFiles,
            Map<String,List<NVBEvent>> mapVoiceSegmentsFiles,
            Map<String, String>  attitudeFileToNVBFileMap,
            VariationType varTypeUsedForSegmenting, boolean turnlevel, boolean speaking) 
    {
       List<SegmentBeforeAttitudeVariation> sbav = new LinkedList<>();
        //segment times : star, end, cluster num

        EuclideanDistanceMetric edm = new EuclideanDistanceMetric();
        AttitudeVariationEvent previousAve = null;
        for(int index=0;index<allvariations.size()-1;index++)
        {
            //is the new attitude event the first (if so, previous ave==null) or in a different file ?
            AttitudeVariationEvent ave =allvariations.get(index);
            if(previousAve!=null && !previousAve.sourceFile.equalsIgnoreCase(ave.sourceFile))
            {
                previousAve=ave;
                continue;
            }

            //for each variation, take the NVB happening before the variation hapened :i.e. the previous plateau
            if(varTypeUsedForSegmenting==null || ave.type.equals(varTypeUsedForSegmenting))
            {
                int startSegment = ave.time;
                int endSegment = allvariations.get(index+1).time;
                double varNormalizedDur = allvariations.get(index+1).normalizedDuration;
                double valNormalized = allvariations.get(index+1).value.doubleValue();
                double value = allvariations.get(index+1).value.doubleValue();


                //what kind of variation is it ? find closest cluster
                double distanceClosestCluster = Double.MAX_VALUE;
                int jCluster=0;
                double[] aveVal = new double[2];
                //aveVal[0] = ave.normalizedDuration;
                //aveVal[1] = ave.value.abs().doubleValue();
                aveVal[0] = varNormalizedDur;
                aveVal[1] = valNormalized;
                for(int j=0;j<clustercenters.length;j++)
                {
                    double distToJ = edm.distance(aveVal,clustercenters[j]);
                    if(distToJ<distanceClosestCluster)
                    {
                        jCluster=j;
                        distanceClosestCluster=distToJ;
                    }
                }
                AttitudeVariationEvent nextAve = allvariations.get(index+1);
                if(!nextAve.sourceFile.equalsIgnoreCase(ave.sourceFile))
                    nextAve=null;
                
                
                if(turnlevel)
                {
                    //old and wrong
                    List<NVBEvent> turns = getTurnsInFile(startSegment,endSegment,ave.sourceFile,
                        mapTurnFiles,attitudeFileToNVBFileMap);

                    List<NVBEvent> thisSegmentation = getSequenceInFile(startSegment,endSegment,ave.sourceFile,
                            mapSeqFiles,attitudeFileToNVBFileMap);

                    if(thisSegmentation!=null)
                        sbav.add(new SegmentBeforeAttitudeVariation(startSegment, 
                                endSegment, thisSegmentation,jCluster,ave.sourceFile,allvariations.get(index+1),turns));
                }
                else
                {
                    List<NVBEvent> voiceSegmentation = getSequenceInFile(startSegment, endSegment, ave.sourceFile, mapVoiceSegmentsFiles, attitudeFileToNVBFileMap);
                    for(NVBEvent nvbev : voiceSegmentation)
                    {
                        if((speaking && nvbev.type.equalsIgnoreCase("V")) || (!speaking && nvbev.type.equalsIgnoreCase("S")))
                        {
                            int voiceSegmentStart = nvbev.start_time;
                            int voiceSegmentEnd = nvbev.end_time;
                            List<NVBEvent> thisSegmentation = getSequenceInFile(voiceSegmentStart,voiceSegmentEnd,ave.sourceFile,
                                mapSeqFiles,attitudeFileToNVBFileMap);

                            if(thisSegmentation!=null)
                                sbav.add(new SegmentBeforeAttitudeVariation(voiceSegmentStart, 
                                        voiceSegmentEnd, thisSegmentation,jCluster,ave.sourceFile,allvariations.get(index+1),(List) new ArrayList<>()));
                            
                        }
                    }
                }
                
                
                //old segmentation : on coupait par plateau. Ensuite on extrait la séquence, et si on a Turn>75%, alors on considère speakin... Bof !
                /*List<NVBEvent> turns = getTurnsInFile(startSegment,endSegment,ave.sourceFile,
                        mapTurnFiles,attitudeFileToNVBFileMap);
                
                List<NVBEvent> thisSegmentation = getSequenceInFile(startSegment,endSegment,ave.sourceFile,
                        mapSeqFiles,attitudeFileToNVBFileMap);
                
                if(thisSegmentation!=null)
                    sbav.add(new SegmentBeforeAttitudeVariation(startSegment, 
                            endSegment, thisSegmentation,jCluster,ave.sourceFile,allvariations.get(index+1),turns));*/
            } 

            previousAve=ave;
        }
        
        return sbav;
    }
    
    //creer les tableaux cf mail magalie 10/12 15h43
    public void statsCreator(List<SegmentBeforeAttitudeVariation> list,
            String spk, String att) throws IOException {
        //part 1 : signals
        Util.checkDeleteOutputFile("C:\\Dropbox\\TableauxSignaux.csv");
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File("C:\\Dropbox\\TableauxSignaux.csv")));
        
        String line="ID,Type,Turn,Attitude,AttVar,Duree\n";
        bw1.write(line);
        int nLine=0;
        for(SegmentBeforeAttitudeVariation sbav : list)
        {
            for(NVBEvent nvbev:sbav.sequence)
            {
                line=""+nLine+",";
                
                line+=nvbev.type+",";
                line+=spk+",";
                line+=att+",";
                line+=sbav.ave.stringvalue+",";
                line+=nvbev.end_time-nvbev.start_time;
                
                line+="\n";
                bw1.write(line);
                nLine++;
            }
        }
        bw1.close();
        
        //part 2 : attitudes
        Util.checkDeleteOutputFile("C:\\Dropbox\\TableauxAttitudes.csv");
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("C:\\Dropbox\\TableauxAttitudes.csv")));
        line="ID,Turn,Attitude,ClusterID,AttVar,Duree,";
        line+="nSmile,Low,Mid,High,Mean,Var,";
        line+="nEyebrowUp,Low,Mid,High,Mean,Var,";
        line+="nEyebrowDown,Low,Mid,High,Mean,Var,";
        line+="nNod,Low-NoRep,Low-Rep,Mid-NoRep,Mid-Rep,High-NoRep,High-Rep,Mean,Var,";
        line+="nShake,Low-NoRep,Low-Rep,Mid-NoRep,Mid-Rep,High-NoRep,High-Rep,Mean,Var,";
        line+="nGestComm,Low-Small,Low-Mid,Low-Large,Mid-Small,Mid-Mid,Mid-Large,High-Small,High-Mid,High-Large,";
        
        line+="nGestAdaptor,";
        line+="nGestManipulation,";
        line+="nBodyLean,Low,Mid,High,";
        line+="nBodyStraight,";
        line+="nBodyRecline,Low,Mid,High,";
        line+="nHeadAt,";
        line+="nHeadDown,Low,Mid,High,";
        line+="nHeadUp,Low,Mid,High,";
        line+="nHeadSide,Low,Mid,High,";
        line+="nHeadTilt,Low,Mid,High,";
        line+="nRestOver,";
        line+="nRestUnder,";
        line+="nRestHandsTogether,";
        line+="nRestArmsCrossed,";
        
        line+="\n";
        bw2.write(line);
        nLine=0;
        for(SegmentBeforeAttitudeVariation sbav : list)
        {
            line=""+nLine+",";
            
            line+=spk+",";
            line+=att+",";
            line+=sbav.variationClusterID+",";
            line+=sbav.ave.stringvalue+",";
            line+=sbav.duration+",";
            
            int nSmile=0,nSmileL=0,nSmileN=0,nSmileH =0; //count
            double tSmile=0,tSmileL=0,tSmileN=0,tSmileH =0; //mean
            double vSmile=0,vSmileL=0,vSmileN=0,vSmileH =0; //var
            
            int nEyebrowUp=0,nEyebrowUpL=0,nEyebrowUpN=0,nEyebrowUpH =0; //count
            double tEyebrowUp=0,tEyebrowUpL=0,tEyebrowUpN=0,tEyebrowUpH =0; //mean
            double vEyebrowUp=0,vEyebrowUpL=0,vEyebrowUpN=0,vEyebrowUpH =0; //var
            
            int nEyebrowDown=0,nEyebrowDownL=0,nEyebrowDownN=0,nEyebrowDownH =0; //count
            double tEyebrowDown=0,tEyebrowDownL=0,tEyebrowDownN=0,tEyebrowDownH =0; //mean
            double vEyebrowDown=0,vEyebrowDownL=0,vEyebrowDownN=0,vEyebrowDownH =0; //var
            
            int nNodLN=0,nNodLY=0,nNodNN=0,nNodNY=0,nNodHN=0,nNodHY=0,nNod=0;
            double tNodLN=0,tNodLY=0,tNodNN=0,tNodNY=0,tNodHN=0,tNodHY=0,tNod=0;
            double vNodLN=0,vNodLY=0,vNodNN=0,vNodNY=0,vNodHN=0,vNodHY=0,vNod=0;
            
            int nShakeLN=0,nShakeLY=0,nShakeNN=0,nShakeNY=0,nShakeHN=0,nShakeHY=0,nShake=0;
            double tShakeLN=0,tShakeLY=0,tShakeNN=0,tShakeNY=0,tShakeHN=0,tShakeHY=0,tShake=0;
            double vShakeLN=0,vShakeLY=0,vShakeNN=0,vShakeNY=0,vShakeHN=0,vShakeHY=0,vShake=0;
            
            int nGestComm=0,nGestCommLS=0,nGestCommLN=0,nGestCommLL=0,nGestCommNS=0,nGestCommNN=0,nGestCommNL=0,nGestCommHS=0,nGestCommHN=0,nGestCommHL=0;
            double tGestComm=0,tGestCommLS=0,tGestCommLN=0,tGestCommLL=0,tGestCommNS=0,tGestCommNN=0,tGestCommNL=0,tGestCommHS=0,tGestCommHN=0,tGestCommHL=0;
            double vGestComm=0,vGestCommLS=0,vGestCommLN=0,vGestCommLL=0,vGestCommNS=0,vGestCommNN=0,vGestCommNL=0,vGestCommHS=0,vGestCommHN=0,vGestCommHL=0;
            
            int nGestAdaptor=0;
            int nGestManipulation=0;
            int nBodyLean=0,nBodyLeanL=0,nBodyLeanN=0,nBodyLeanH=0;
            int nBodyStraight=0;
            int nBodyRecline=0,nBodyReclineL=0,nBodyReclineN=0,nBodyReclineH=0;
            int nHeadAt=0;
            int nHeadDown=0,nHeadDownL=0,nHeadDownN=0,nHeadDownH=0;
            int nHeadUp=0,nHeadUpL=0,nHeadUpN=0,nHeadUpH=0;
            int nHeadSide=0,nHeadSideL=0,nHeadSideN=0,nHeadSideH=0;
            int nHeadTilt=0,nHeadTiltL=0,nHeadTiltN=0,nHeadTiltH=0;
            int nRestOver=0,nRestUnder=0,nRestHandsTogether=0,nRestArmsCrossed=0;

            
            for(NVBEvent nvbev:sbav.sequence){
                double dur = ((double)nvbev.end_time-(double)nvbev.start_time)/1000;
                if(nvbev.type.equalsIgnoreCase("SmileL"))
                {nSmile++;nSmileL++;
                tSmile+=dur;vSmile+=Math.pow(dur,2);
                tSmileL+=dur;vSmileL+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("SmileN"))
                {nSmile++;nSmileN++;
                tSmile+=dur;vSmile+=Math.pow(dur,2);
                tSmileN+=dur;vSmileN+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("SmileH"))
                {nSmile++;nSmileH++;
                tSmile+=dur;vSmile+=Math.pow(dur,2);
                tSmileH+=dur;vSmileH+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("EyebrowUpL"))
                {nEyebrowUp++;nEyebrowUpL++;
                tEyebrowUp+=dur;vEyebrowUp+=Math.pow(dur,2);
                tEyebrowUpL+=dur;vEyebrowUpL+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("EyebrowUpN"))
                {nEyebrowUp++;nEyebrowUpN++;
                tEyebrowUp+=dur;vEyebrowUp+=Math.pow(dur,2);
                tEyebrowUpN+=dur;vEyebrowUpN+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("EyebrowUpH"))
                {nEyebrowUp++;nEyebrowUpH++;
                tEyebrowUp+=dur;vEyebrowUp+=Math.pow(dur,2);
                tEyebrowUpH+=dur;vEyebrowUpH+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("EyebrowDownL"))
                {nEyebrowDown++;nEyebrowDownL++;
                tEyebrowDown+=dur;vEyebrowDown+=Math.pow(dur,2);
                tEyebrowDownL+=dur;vEyebrowDownL+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("EyebrowDownN"))
                {nEyebrowDown++;nEyebrowDownN++;
                tEyebrowDown+=dur;vEyebrowDown+=Math.pow(dur,2);
                tEyebrowDownN+=dur;vEyebrowDownN+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("EyebrowDownH"))
                {nEyebrowDown++;nEyebrowDownH++;
                tEyebrowDown+=dur;vEyebrowDown+=Math.pow(dur,2);
                tEyebrowDownH+=dur;vEyebrowDownH+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadNodLN"))
                {nNod++;nNodLN++;
                tNod+=dur;vNod+=Math.pow(dur,2);
                tNodLN+=dur;vNodLN+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadNodLY"))
                {nNod++;nNodLY++;
                tNod+=dur;vNod+=Math.pow(dur,2);
                tNodLY+=dur;vNodLY+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadNodNN"))
                {nNod++;nNodNN++;
                tNod+=dur;vNod+=Math.pow(dur,2);
                tNodNN+=dur;vNodNN+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadNodNY"))
                {nNod++;nNodNY++;
                tNod+=dur;vNod+=Math.pow(dur,2);
                tNodNY+=dur;vNodNY+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadNodHN"))
                {nNod++;nNodHN++;
                tNod+=dur;vNod+=Math.pow(dur,2);
                tNodHN+=dur;vNodHN+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadNodHY"))
                {nNod++;nNodHY++;
                tNod+=dur;vNod+=Math.pow(dur,2);
                tNodHY+=dur;vNodHY+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadShakeLN"))
                {nShake++;nShakeLN++;
                tShake+=dur;vShake+=Math.pow(dur,2);
                tShakeLN+=dur;vShakeLN+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadShakeLY"))
                {nShake++;nShakeLY++;
                tShake+=dur;vShake+=Math.pow(dur,2);
                tShakeLY+=dur;vShakeLY+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadShakeNN"))
                {nShake++;nShakeNN++;
                tShake+=dur;vShake+=Math.pow(dur,2);
                tShakeNN+=dur;vShakeNN+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadShakeNY"))
                {nShake++;nShakeNY++;
                tShake+=dur;vShake+=Math.pow(dur,2);
                tShakeNY+=dur;vShakeNY+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadShakeHN"))
                {nShake++;nShakeHN++;
                tShake+=dur;vShake+=Math.pow(dur,2);
                tShakeHN+=dur;vShakeHN+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("HeadShakeHY"))
                {nShake++;nShakeHY++;
                tShake+=dur;vShake+=Math.pow(dur,2);
                tShakeHY+=dur;vShakeHY+=Math.pow(dur,2);}
                else if(nvbev.type.equalsIgnoreCase("GestCommLS"))
                {nGestComm++;nGestCommLS++;}
                else if(nvbev.type.equalsIgnoreCase("GestCommLN"))
                {nGestComm++;nGestCommLN++;}
                else if(nvbev.type.equalsIgnoreCase("GestCommLL"))
                {nGestComm++;nGestCommLL++;}
                else if(nvbev.type.equalsIgnoreCase("GestCommNS"))
                {nGestComm++;nGestCommNS++;}
                else if(nvbev.type.equalsIgnoreCase("GestCommNN"))
                {nGestComm++;nGestCommNN++;}
                else if(nvbev.type.equalsIgnoreCase("GestCommNL"))
                {nGestComm++;nGestCommNL++;}
                else if(nvbev.type.equalsIgnoreCase("GestCommHS"))
                {nGestComm++;nGestCommHS++;}
                else if(nvbev.type.equalsIgnoreCase("GestCommHN"))
                {nGestComm++;nGestCommHN++;}
                else if(nvbev.type.equalsIgnoreCase("GestCommHL"))
                {nGestComm++;nGestCommHL++;}
                else if(nvbev.type.equalsIgnoreCase("GestAdaptorHair"))
                {nGestAdaptor++;}
                else if(nvbev.type.equalsIgnoreCase("GestAdaptorFace"))
                {nGestAdaptor++;}
                else if(nvbev.type.equalsIgnoreCase("GestAdaptorHands"))
                {nGestAdaptor++;}
                else if(nvbev.type.equalsIgnoreCase("GestAdaptorBody"))
                {nGestAdaptor++;}
                else if(nvbev.type.equalsIgnoreCase("GestAdaptorNeck"))
                {nGestAdaptor++;}
                else if(nvbev.type.equalsIgnoreCase("GestAdaptorArms"))
                {nGestAdaptor++;}
                else if(nvbev.type.equalsIgnoreCase("GestObjectManipulation"))
                {nGestManipulation++;}
                else if(nvbev.type.equalsIgnoreCase("BodyLeanL"))
                {nBodyLean++;nBodyLeanL++;}
                else if(nvbev.type.equalsIgnoreCase("BodyLeanN"))
                {nBodyLean++;nBodyLeanN++;}
                else if(nvbev.type.equalsIgnoreCase("BodyLeanH"))
                {nBodyLean++;nBodyLeanH++;}
                else if(nvbev.type.equalsIgnoreCase("BodyStraight"))
                {nBodyStraight++;}
                else if(nvbev.type.equalsIgnoreCase("BodyReclineL"))
                {nBodyRecline++;nBodyReclineL++;}
                else if(nvbev.type.equalsIgnoreCase("BodyReclineN"))
                {nBodyRecline++;nBodyReclineN++;}
                else if(nvbev.type.equalsIgnoreCase("BodyReclineH"))
                {nBodyRecline++;nBodyReclineH++;}
                else if(nvbev.type.equalsIgnoreCase("HeadAt"))
                {nHeadAt++;}
                else if(nvbev.type.equalsIgnoreCase("HeadTiltL"))
                {nHeadTilt++;nHeadTiltL++;}
                else if(nvbev.type.equalsIgnoreCase("HeadTiltN"))
                {nHeadTilt++;nHeadTiltN++;}
                else if(nvbev.type.equalsIgnoreCase("HeadTiltH"))
                {nHeadTilt++;nHeadTiltH++;}
                else if(nvbev.type.equalsIgnoreCase("HeadDownL"))
                {nHeadDown++;nHeadDownL++;}
                else if(nvbev.type.equalsIgnoreCase("HeadDownN"))
                {nHeadDown++;nHeadDownN++;}
                else if(nvbev.type.equalsIgnoreCase("HeadDownH"))
                {nHeadDown++;nHeadDownH++;}
                else if(nvbev.type.equalsIgnoreCase("HeadUpL"))
                {nHeadUp++;nHeadUpL++;}                
                else if(nvbev.type.equalsIgnoreCase("HeadUpN"))
                {nHeadUp++;nHeadUpN++;}
                else if(nvbev.type.equalsIgnoreCase("HeadUpH"))
                {nHeadUp++;nHeadUpH++;}
                else if(nvbev.type.equalsIgnoreCase("HeadSideL"))
                {nHeadSide++;nHeadSideL++;}
                else if(nvbev.type.equalsIgnoreCase("HeadSideN"))
                {nHeadSide++;nHeadSideN++;}
                else if(nvbev.type.equalsIgnoreCase("HeadSideH"))
                {nHeadSide++;nHeadSideH++;}
                else if(nvbev.type.equalsIgnoreCase("RestArmsCrossed"))
                {nRestArmsCrossed++;}
                else if(nvbev.type.equalsIgnoreCase("RestHandsTogether"))
                {nRestHandsTogether++;}
                else if(nvbev.type.equalsIgnoreCase("RestOver"))
                {nRestOver++;}
                else if(nvbev.type.equalsIgnoreCase("RestUnder"))
                {nRestUnder++;}
            }
            
            tSmile=tSmile/((double)nSmile);
            vSmile=vSmile/((double)nSmile)-(tSmile*tSmile);
            
            tEyebrowUp=tEyebrowUp/((double)nEyebrowUp);
            vEyebrowUp=vEyebrowUp/((double)nEyebrowUp)-(tEyebrowUp*tEyebrowUp);
            
            tEyebrowDown=tEyebrowDown/((double)nEyebrowDown);
            vEyebrowDown=vEyebrowDown/((double)nEyebrowDown)-(tEyebrowDown*tEyebrowDown);
            
            tNod=tNod/((double)nNod);
            vNod=vNod/((double)nNod)-(tNod*tNod);
            
            tShake=tShake/((double)nShake);
            vShake=vShake/((double)nShake)-(tShake*tShake);
            
            line+=nSmile+","+nSmileL+","+nSmileN+","+nSmileH+","+tSmile+","+vSmile+",";
            line+=nEyebrowUp+","+nEyebrowUpL+","+nEyebrowUpN+","+nEyebrowUpH+","+tEyebrowUp+","+vEyebrowUp+",";
            line+=nEyebrowDown+","+nEyebrowDownL+","+nEyebrowDownN+","+nEyebrowDownH+","+tEyebrowDown+","+vEyebrowDown+",";
            line+=nNod+","+nNodLN+","+nNodLY+","+nNodNN+","+nNodNY+","+nNodHN+","+nNodHY+","+tNod+","+vNod+",";
            line+=nShake+","+nShakeLN+","+nShakeLY+","+nShakeNN+","+nShakeNY+","+nShakeHN+","+nShakeHY+","+tShake+","+vShake+",";
            line+=nGestComm+","+nGestCommLS+","+nGestCommLN+","+nGestCommLL+","+nGestCommNS+","+nGestCommNN+","+nGestCommNL+","+nGestCommHS+","+nGestCommHN+","+nGestCommHL+",";
            
            line+=nGestAdaptor+","+nGestManipulation+",";
            line+=nBodyLean+","+nBodyLeanL+","+nBodyLeanN+","+nBodyLeanH+",";
            line+=nBodyStraight+",";
            line+=nBodyRecline+","+nBodyReclineL+","+nBodyReclineN+","+nBodyReclineH+",";
            line+=nHeadAt+",";
            line+=nHeadDown+","+nHeadDownL+","+nHeadDownN+","+nHeadDownH+",";
            line+=nHeadUp+","+nHeadUpL+","+nHeadUpN+","+nHeadUpH+",";
            line+=nHeadSide+","+nHeadSideL+","+nHeadSideN+","+nHeadSideH+",";
            line+=nHeadTilt+","+nHeadTiltL+","+nHeadTiltN+","+nHeadTiltH+",";
            line+=nRestOver+","+nRestUnder+","+nRestHandsTogether+","+nRestArmsCrossed+",";
            
            line+="\n";
            bw2.write(line);
            nLine++;
        }
        bw2.close();
        //"Colonnes : nbr de sourire, nb de hochement, attitude annotée ; speaking/listening"
    }

    //creation des sequences pour l'algorithme GSP
    //l'algo utilise -1 pour separer 2 items, -2 pour indiquer une fin de sequence
    public void writeToFile(String outputSegments, List<SegmentBeforeAttitudeVariation> sbav,
            int n_clusters, boolean toInteger, boolean toMDPattern, boolean saveAll, boolean saveByCluster) throws IOException {

        
        //int n_clustertypes =0;
        /*for(SegmentBeforeAttitudeVariation segment : sbav)
        {
            if(segment.variationClusterID+1>n_clustertypes)
                n_clustertypes=segment.variationClusterID;
        }*/
        BufferedWriter bw_all;
        if(saveAll)
        {
            String filename_all_raw=outputSegments+"segments.raw";
            Util.checkDeleteOutputFile(filename_all_raw);
            bw_all = new BufferedWriter(new FileWriter(new File(filename_all_raw)));
        }
        else 
            bw_all = new BufferedWriter(new PrintWriter(System.out));
        
        for(int i=0;i<n_clusters;i++)
        {
            BufferedWriter bw_thiscluster;
            if(saveByCluster)
            {
                String filename_thiscluster=outputSegments+i+".raw";
                Util.checkDeleteOutputFile(filename_thiscluster);
                bw_thiscluster = new BufferedWriter(new FileWriter(new File(filename_thiscluster))); //writer
            }
            else 
                bw_thiscluster = new BufferedWriter(new PrintWriter(System.out));
            
            
            for(SegmentBeforeAttitudeVariation segment : sbav)
            {
                if(segment.variationClusterID==i)
                {
                   // String line="";
                    String line_raw="";
                    if(toMDPattern)
                    {
                        //MD pattern : premier entier: turn. 1 recruiter, 2 candidate, 3 mixed
                        //deuxieme entier: numero cluster : i
                        if(segment.getSpeakingPercentage()>0.75)
                        {
                            line_raw+="1 "+i;
                        }
                        else if(segment.getSpeakingPercentage()<0.25)
                        {
                            line_raw+="2 "+i;
                        }
                        else
                        {
                            line_raw+="3 "+i;
                        }

                        line_raw+=" -3 "; //fin MD pattern : debut du non verbal
                    }
                    
                    boolean addFirstNVBEvent=true;
                    //line+=segment.ave.type+","+segment.duration+","+segment.ave.value.doubleValue()+";";
                    for(NVBEvent nvbev : segment.sequence)
                    {
                        //line+=","+nvbev.type;
                        if(addFirstNVBEvent)
                        {
                            if(!toInteger)
                            {
                                line_raw+=nvbev.type;
                            addFirstNVBEvent=false;
                            }
                            else
                            {
                                if(NVBEVENT_INTEGER_MAP.get(nvbev.type)!=null)
                                {
                                    line_raw+=NVBEVENT_INTEGER_MAP.get(nvbev.type);
                                    addFirstNVBEvent=false;
                                }
                            }
                        }
                        else
                        {
                            if(!toInteger)
                            {
                                line_raw+=" -1 "+nvbev.type; //spmf sequencedatabase.loadFile function looks at whitespace " " separators !
                            }
                            else
                            {
                                if(NVBEVENT_INTEGER_MAP.get(nvbev.type)!=null)
                                    line_raw+=" -1 "+NVBEVENT_INTEGER_MAP.get(nvbev.type); //spmf sequencedatabase.loadFile function looks at whitespace " " separators !
                            }
                        } 
                    }
                    //bw.write(line+"\n"); 
                    if(line_raw!="" && !line_raw.endsWith(" -3 ")) //check if sequence is not empty 
                        //(can be empty because of NVB_INTEGER_MAP when we restrict nvb events we look at
                    {
                        if(saveByCluster)
                            bw_thiscluster.write(line_raw+" -1 -2 \n");
                        if(saveAll)
                            bw_all.write(line_raw+" -1 -2 \n");
                    }
                }
   
            }  
            bw_thiscluster.close();
        }
        bw_all.close();
    }

    private List<NVBEvent> getTurnsInFile(int startSegment, int endSegment, String sourceFile, Map<String, List<NVBEvent>> mapTurnsFiles, Map<String, String> attitudeFileToNVBFileMap) {
        
        List<NVBEvent> turns = new LinkedList<>();
        List<NVBEvent> events = mapTurnsFiles.get(attitudeFileToNVBFileMap.get(sourceFile));
        if(events==null)
        {
            System.err.println("didnt find any correspondance between attitude file and nvb file sequences");
            return null;
        }
        int lastInteger=0;
        for(int i=0;i<events.size();i++)
        {
            NVBEvent nvbv = events.get(i);
            if(nvbv.start_time>endSegment) //after end time of segment
            {
                break;
            }
            else if(nvbv.start_time<startSegment) //before start time fo segment
            {
                continue;
            }
            else
            {
                if(turns.isEmpty() && i>0)
                {
                    NVBEvent startingTurn = new NVBEvent(events.get(i-1)); // copy
                    startingTurn.start_time = startSegment;
                    turns.add(startingTurn); //add 
                }
                turns.add(nvbv);
            }
            lastInteger=i;
        }
        if(turns.isEmpty())
        {
            turns.add(new NVBEvent(events.get(lastInteger)));
        }
        return turns;
    }
}
