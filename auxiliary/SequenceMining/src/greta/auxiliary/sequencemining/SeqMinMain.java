/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining;

import au.com.bytecode.opencsv.CSVReader;
import ca.pfv.spmf.algorithms.sequentialpatterns.BIDE_and_prefixspan_with_strings.AlgoBIDEPlus_withStrings;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.AlgoBIDEPlus;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalpatterns.AlgoDim;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.AlgoSeqDim;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.multidimensionalsequentialpatterns.MDSequenceDatabase;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.AlgoGSP;
//import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.AlgoGSP;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator;
import ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.creators.AbstractionCreator_Qualitative;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import greta.auxiliary.sequencemining.structures.TimedValue;
import greta.auxiliary.sequencemining.structures.SegmentBeforeAttitudeVariation;
import greta.auxiliary.sequencemining.structures.NVBEvent;
import greta.auxiliary.sequencemining.structures.AttitudeVariationEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.battelle.clodhopper.distance.EuclideanDistanceMetric;
import org.battelle.clodhopper.xmeans.XMeansParams;

import org.battelle.clodhopper.Cluster;
import greta.auxiliary.sequencemining.exceptions.DifferentFrameSizeException;
import greta.auxiliary.sequencemining.structures.AttitudeVariationEvent.VariationType;
import greta.auxiliary.sequencemining.structures.ExtractedSequence;

/**
 * Main class for performing sequence mining on Tardis Data
 * @author Mathieu
 */
public class SeqMinMain {

    public SeqMinMain(){
        
    }
    /**
     * @param args the command line arguments
     */
    
    public static Map<String,List<NVBEvent>> mapSeqFiles;
    public static Map<String,List<NVBEvent>> mapTurnsFiles;
    public static Map<String,List<NVBEvent>> mapVoiceSegmentFiles;
    public static Map<String, String> attitudeFileToNVBFileMap;
    
    public static void main(String[] args) throws DifferentFrameSizeException  {
        
        //these booleans define which steps of the preprocessing and sequence mining
        //that we go through
        boolean translateNVBFilesToSequence = true;
        boolean translateNVBFilesToTurns = true;
        boolean translateNVBFilesToVoiceSegments = true;
        boolean translateAttitudeFilesToVariations = true;
        boolean normalizeAVEs = true;
        boolean doClusteringVariations = false;
        boolean doPlot=false;
        boolean segmentSequences =true;
        boolean mineSequences=true;
        boolean measureQuality=true;
        boolean translate=true;
        
        //test for multidimensional sequence mining (no great results)
        boolean multiDimSequences=false;
        
        //are we extracting for dominance or friendliness
        boolean friendliness = true;
        boolean dominance = false;
        
        //are we extracting for speaking or listening
        boolean speaking = true;
        
        boolean turnlevel = true;
              
        double minimumsupport = 0.11;
        int nmaxSequence=7;
        
        //####################
        //NVB
        //####################
        mapSeqFiles = new HashMap<>();
        mapTurnsFiles = new HashMap<>();
        mapVoiceSegmentFiles = new HashMap<>();
        
        if(translateNVBFilesToSequence || translateNVBFilesToTurns)
        {
            List<NVBEvent> nvbev = new LinkedList<>();
            String ML2NVB = "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML2Recruteur.csv";
            String ML2NVBBySignals = "C:\\Dropbox\\codeprojects\\sequencemining\\data\\databysignals\\ML2BySignals.csv";
            String outputML2NVB = "..\\data\\fullNvbSequences\\ML2Recruteuroutput.txt";
            
            String ML3NVB = "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML3Recruteur.csv";
            String ML3NVBBySignals = "C:\\Dropbox\\codeprojects\\sequencemining\\data\\databysignals\\ML3BySignals.csv";
            String outputML3NVB = "..\\data\\fullNvbSequences\\ML3Recruteuroutput.txt";
            
            String ML6NVB = "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML6Recruteur.csv";
            String ML6NVBBySignals = "C:\\Dropbox\\codeprojects\\sequencemining\\data\\databysignals\\ML6BySignals.csv";
            String outputML6NVB = "..\\data\\fullNvbSequences\\ML6Recruteuroutput.txt";
            Util.checkDeleteOutputFile(outputML2NVB); //for now I can remove files
            Util.checkDeleteOutputFile(outputML3NVB); //for now I can remove files
            Util.checkDeleteOutputFile(outputML6NVB); //for now I can remove files

            if(translateNVBFilesToSequence)
            {
                NVBSequenceTranslator etst = new NVBSequenceTranslator();
                try {
                    nvbev= etst.translate(ML2NVB,outputML2NVB,speaking);
                    nvbev = etst.findEndTimes(nvbev, ML2NVBBySignals);
                    mapSeqFiles.put(ML2NVB, nvbev);

                    nvbev= etst.translate(ML3NVB,outputML3NVB,speaking);
                    nvbev = etst.findEndTimes(nvbev, ML3NVBBySignals);
                    mapSeqFiles.put(ML3NVB, nvbev);

                    nvbev= etst.translate(ML6NVB,outputML6NVB,speaking);
                    nvbev = etst.findEndTimes(nvbev, ML6NVBBySignals);
                    mapSeqFiles.put(ML6NVB, nvbev);
                    /*nvbev= etst.translate(ML2NVB,outputML2NVB);
                    mapSeqFiles.put(ML2NVB, nvbev);

                    nvbev= etst.translate(ML3NVB,outputML3NVB);
                    mapSeqFiles.put(ML3NVB, nvbev);

                    nvbev= etst.translate(ML6NVB,outputML6NVB);
                    mapSeqFiles.put(ML6NVB, nvbev);*/
                } catch (FileNotFoundException ex) {
                    System.err.println(ex.getMessage());
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
            else
            {
                //load nvb files
            }

            if(translateNVBFilesToTurns)
            {
                List<Integer> lst = new LinkedList<>();
                lst.add(3);
                NVBSequenceTranslator etst = new NVBSequenceTranslator(lst);
                String outputML2Turns= "..\\data\\fullNvbSequences\\ML2RecruteurTurns.txt";
                String outputML3Turns = "..\\data\\fullNvbSequences\\ML3RecruteurTurns.txt";
                String outputML6Turns = "..\\data\\fullNvbSequences\\ML6RecruteurTurns.txt";
                Util.checkDeleteOutputFile(outputML2Turns); //for now I can remove files
                Util.checkDeleteOutputFile(outputML3Turns); //for now I can remove files
                Util.checkDeleteOutputFile(outputML6Turns); //for now I can remove files

                try {
                    nvbev= etst.translate(ML2NVB,outputML2Turns);
                    nvbev = etst.findEndTimes(nvbev, ML2NVBBySignals);
                    mapTurnsFiles.put(ML2NVB, nvbev);

                    nvbev= etst.translate(ML3NVB,outputML3Turns);
                    nvbev = etst.findEndTimes(nvbev, ML3NVBBySignals);
                    mapTurnsFiles.put(ML3NVB, nvbev);

                    nvbev= etst.translate(ML6NVB,outputML6Turns);
                    nvbev = etst.findEndTimes(nvbev, ML6NVBBySignals);
                    mapTurnsFiles.put(ML6NVB, nvbev);
                } catch (FileNotFoundException ex) {
                    System.err.println(ex.getMessage());
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }

            if(translateNVBFilesToVoiceSegments)
            {
                List<Integer> lst = new LinkedList<>();
                lst.add(4);
                NVBSequenceTranslator etst = new NVBSequenceTranslator(lst);
                String outputML2VoiceSegments= "..\\data\\fullNvbSequences\\ML2RecruteurVoiceSegments.txt";
                String outputML3VoiceSegments = "..\\data\\fullNvbSequences\\ML3RecruteurVoiceSegments.txt";
                String outputML6VoiceSegments = "..\\data\\fullNvbSequences\\ML6RecruteurVoiceSegments.txt";
                Util.checkDeleteOutputFile(outputML2VoiceSegments); //for now I can remove files
                Util.checkDeleteOutputFile(outputML3VoiceSegments); //for now I can remove files
                Util.checkDeleteOutputFile(outputML6VoiceSegments); //for now I can remove files

                try {
                    nvbev= etst.translate(ML2NVB,outputML2VoiceSegments);
                    nvbev = etst.findEndTimes(nvbev, ML2NVBBySignals);
                    mapVoiceSegmentFiles.put(ML2NVB, nvbev);

                    nvbev= etst.translate(ML3NVB,outputML3VoiceSegments);
                    nvbev = etst.findEndTimes(nvbev, ML3NVBBySignals);
                    mapVoiceSegmentFiles.put(ML3NVB, nvbev);

                    nvbev= etst.translate(ML6NVB,outputML6VoiceSegments);
                    nvbev = etst.findEndTimes(nvbev, ML6NVBBySignals);
                    mapVoiceSegmentFiles.put(ML6NVB, nvbev);
                } catch (FileNotFoundException ex) {
                    System.err.println(ex.getMessage());
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
        
        //####################
        //stances
        //####################
        AttitudeTranslator gtstt = new AttitudeTranslator();
        List<AttitudeVariationEvent> allvariations = new LinkedList<>();
        
        List<String> dominanceFiles = new LinkedList<>();
        List<String> friendlinessFiles = new LinkedList<>();
        if(dominance)
        {
        dominanceFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2DomFlorian.csv");
        dominanceFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2DomMathieu.csv");
        dominanceFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML3DomChloe.csv");
        dominanceFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML3DomMathieu.csv");
        dominanceFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML6DomBrian.csv");
        dominanceFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML6DomMathieu.csv");
        }
        if(friendliness)
        {
        friendlinessFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2FrdJess.csv");
        friendlinessFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2FrdMathieu.csv");
        friendlinessFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML3FrdBrian.csv");
        friendlinessFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML3FrdJess.csv");
        friendlinessFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML6FrdMathieu.csv");
        friendlinessFiles.add("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML6FrdPierre.csv");
        }
        
        attitudeFileToNVBFileMap = new HashMap<>();
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2DomFlorian.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML2Recruteur.csv");
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2DomMathieu.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML2Recruteur.csv");
        
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML3DomChloe.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML3Recruteur.csv");
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML3DomMathieu.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML3Recruteur.csv");
        
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML6DomBrian.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML6Recruteur.csv");
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML6DomMathieu.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML6Recruteur.csv");
        
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2FrdJess.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML2Recruteur.csv");
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2FrdMathieu.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML2Recruteur.csv");
        
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML3FrdBrian.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML3Recruteur.csv");
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML3FrdJess.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML3Recruteur.csv");
        
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML6FrdMathieu.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML6Recruteur.csv");
        attitudeFileToNVBFileMap.put("C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML6FrdPierre.csv", 
                "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\nonverbal\\ML6Recruteur.csv");
        
        //####################
        //find attitude variation timestamps
        //####################
        if(translateAttitudeFilesToVariations)
        {
            gtstt.setCalibration();
            //String[] ML2DomFiles = new String[1];//2];
            //ML2DomFiles[0] = "C:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2DomFlorian.csv";
            //ML2DomFiles[1] = "D:\\Dropbox\\Work\\These\\Analyse Seq\\scripts\\attitude\\segmentation\\ML2DomMathieu.csv";
            //String outputML2Dom = "..\\data\\ML2Domoutput.csv";
            try {
                for(String s : dominanceFiles)
                {
                    String[] splittedPath = s.split("\\\\");
                    String outputPath= "C:\\Dropbox\\Work\\These\\interraterAttitudesTest\\"+splittedPath[splittedPath.length-1].substring(0,splittedPath[splittedPath.length-1].length()-4)+".txt";
                    Util.checkDeleteOutputFile(outputPath); //for now I can remove files
                    List<TimedValue> attitudes=gtstt.translate(s, true);
                    gtstt.writeAttitudeFile(attitudes,outputPath);
                    List<AttitudeVariationEvent> variations = gtstt.findVariationEvents(attitudes,s);
                    variations = gtstt.smootheVariations(variations);
                    for(AttitudeVariationEvent ave :variations)
                        allvariations.add(ave);
                }
                for(String s : friendlinessFiles)
                {
                    String[] splittedPath = s.split("\\\\");
                    String outputPath= "C:\\Dropbox\\Work\\These\\interraterAttitudesTest\\"+splittedPath[splittedPath.length-1].substring(0,splittedPath[splittedPath.length-1].length()-4)+".txt";
                    Util.checkDeleteOutputFile(outputPath); //for now I can remove files
                    List<TimedValue> attitudes=gtstt.translate(s, false);
                    gtstt.writeAttitudeFile(attitudes,outputPath);
                    List<AttitudeVariationEvent> variations = gtstt.findVariationEvents(attitudes,s);
                    variations = gtstt.smootheVariations(variations);
                    for(AttitudeVariationEvent ave :variations)
                        allvariations.add(ave);
                }
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
        
        }
  
        //####################
        //normalize Attitude variation events
        //####################
        double minPlatDuration=Integer.MAX_VALUE;
        double maxPlatDuration=Integer.MIN_VALUE; 
        double minSlopDuration=Integer.MAX_VALUE;
        double maxSlopDuration=Integer.MIN_VALUE; 
        List<AttitudeVariationEvent> normalizedVars = allvariations;
        if(normalizeAVEs)
        {
            String outputVariations="..\\data\\attitudeVariations\\variations.txt";
            normalizedVars = new LinkedList<>();
            
            for(AttitudeVariationEvent avec : allvariations)
            {
                //get min, max durations
                if(avec.type.equals(AttitudeVariationEvent.VariationType.PLATEAU))
                {
                    if(avec.duration>maxPlatDuration) maxPlatDuration=avec.duration;
                    if(avec.duration<minPlatDuration) minPlatDuration=avec.duration;
                }
                else
                {
                    if(avec.duration>maxSlopDuration) maxSlopDuration=avec.duration;
                    if(avec.duration<minSlopDuration) minSlopDuration=avec.duration;
                }
            }
            for(AttitudeVariationEvent avec : allvariations)
            {
                //normalize
                AttitudeVariationEvent normaizedAVE = new AttitudeVariationEvent(avec.type, 
                        avec.value, avec.time, avec.duration,avec.sourceFile);
                if(normaizedAVE.type.equals(AttitudeVariationEvent.VariationType.PLATEAU))
                    normaizedAVE.normalizeDuration(minPlatDuration, maxPlatDuration);
                else
                    normaizedAVE.normalizeDuration(minSlopDuration, maxSlopDuration);
                //if(normaizedAVE.normalizedDuration<=0.5)
                normalizedVars.add(normaizedAVE);
            }
            try {
                AttitudeTranslator.writeToFile(normalizedVars,outputVariations);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
            
            if(doPlot)
            {
                String variationsPointsDiaramPath = "..\\data\\plots\\GraphVariations.png";
                Plotter.plotAttitudeVariationsPoints(normalizedVars,variationsPointsDiaramPath);
            }
            //GTraceTranslator.writeToFile(outputVariations,attitudeVariations);
        }
        
        //####################
        //clustering
        //####################
        AttitudeClusterer ac = new AttitudeClusterer();
        List<Cluster> orderedCluster = new LinkedList();   
        double[][] clustercenters=new double[0][0];
        String outputCluster = "..\\data\\clustering\\";
        if(doClusteringVariations)
        {
            
            String tmpCluster = "..\\data\\clustering\\ClusterData.txt";
            Util.checkDeleteOutputFile(outputCluster); //for now I can remove files
            try {
                Util.createDirectory(outputCluster);
            } catch (IOException ex) {
                System.err.println("coulndt create clustering dir :"+ex.getMessage());
            }
            Util.checkDeleteOutputFile(tmpCluster); //for now I can remove files
            
            XMeansParams.Builder builder = new XMeansParams.Builder();
            XMeansParams params = builder.distanceMetric(new EuclideanDistanceMetric()).minClusters(5).maxClusters(12).build();
            List<Cluster> clusters = ac.cluster(normalizedVars, params);
            
            //remove small clusters
            /*for(Cluster c : clusters)
            {
                if(c.getMemberCount()>9)
                    orderedCluster.add(c);
            }*/
            
            //ordering clusters
            orderedCluster = ac.orderClusters(clusters);
            
            //AttitudeClusterer.writeToFile(outputCluster,orderedCluster);
            
             
            //def clusters
            int n_clusters=orderedCluster.size();
            clustercenters = new double[n_clusters][2];
            try {
                AttitudeClusterer.writeToFile(orderedCluster, outputCluster);
            } catch (IOException ex) {
                System.err.println(ex.getMessage());
            }
            {
                int j=0;
                for(Object c :orderedCluster)
                {
                    Cluster cl = (Cluster) c;
                    clustercenters[j][0] = cl.getCenter()[0];
                    clustercenters[j][1] = cl.getCenter()[1];
                    j++;
                }
            }
        }
        else
        {
            //read from directory
            File f = new File(outputCluster);
            if(f.isDirectory())
            {
                File[] clusterFiles = f.listFiles();
                clustercenters = new double[clusterFiles.length][2];
                for(int i=0;i<clusterFiles.length;i++)
                {
                    try {
                        CSVReader csvr = new CSVReader(new FileReader(clusterFiles[i]));
                        String[] line = csvr.readNext();
                        clustercenters[i][0] = Double.parseDouble(line[1]);
                        clustercenters[i][1] = Double.parseDouble(line[2]);
                    } catch (FileNotFoundException ex) {
                        System.err.println(ex.getMessage());
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        }
        //plotting clusters
        if(doPlot)
        {
            String clustersDiaramPath = "..\\data\\plots\\GraphClusters.png";
            Plotter.plotAttitudeClusters(normalizedVars,clustercenters, clustersDiaramPath);
        }
        
        //####################
        //segmentation
        //####################
        String outputSegments = "..\\data\\segmentationWithAttitudeVariations\\segments\\";
        Segmenter seg;
        try {
            seg = new Segmenter("..\\data\\simpleNVBEventsToIntegerMap.txt");
            if(segmentSequences)
            {                
                List<SegmentBeforeAttitudeVariation> segmentsBeforeVariation = 
                        seg.doSegmentation(normalizedVars,clustercenters,mapSeqFiles,mapTurnsFiles,mapVoiceSegmentFiles,
                        attitudeFileToNVBFileMap, VariationType.PLATEAU, turnlevel, speaking); //segment by looking at plateaux only
                    seg.writeToFile(outputSegments,segmentsBeforeVariation,clustercenters.length,true,multiDimSequences,false,true);//1st boolean means : convert to integers, 2nd means:multidimpattern, 3rd means : save all, 4th means, saveByCluster
                    //seg.writeToFile(outputSegments,segmentsBeforeVariation,clustercenters.length,false,multiDimSequences,false,true);//1st boolean means : convert to integers, 2nd means:multidimpattern, 3rd means : save all, 4th means, saveByCluster
                    
                List<SegmentBeforeAttitudeVariation> allSegments =  
                    seg.doSegmentation(normalizedVars,clustercenters,mapSeqFiles,mapTurnsFiles,mapVoiceSegmentFiles,
                        attitudeFileToNVBFileMap, VariationType.PLATEAU, turnlevel, speaking); //segment all
                seg.writeToFile(outputSegments,allSegments,clustercenters.length,true,multiDimSequences,true,false);//1st boolean means : convert to integers, 2nd means:multidimpattern, 3rd means : save all, 4th means, saveByCluster                
                //seg.writeToFile(outputSegments,allSegments,clustercenters.length,false,multiDimSequences,true,false);//1st boolean means : convert to integers, 2nd means:multidimpattern, 3rd means : save all, 4th means, saveByCluster                
                seg.statsCreator(allSegments,speaking?"SPK":"LSTN",friendliness?"FRD":"DOM");
            }
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
        
        //####################
        //sequence mining 
        //####################
        String outputMinedSequences = "..//data//segmentationWithAttitudeVariations//mined//";
        if(mineSequences)
        {
            //MDSequetialPatternMininClosed
            // Minimum absolute support = 20 % ??
            String input=outputSegments+"segments.raw";
            String output = outputMinedSequences+"mined.raw";
            if(multiDimSequences)
            {
                double minsupp = 0.025;
                MDSequenceDatabase contextMDDatabase  = new MDSequenceDatabase();

                // If the second boolean is true, the algorithm will use
                // CHARM instead of AprioriClose for mining frequent closed itemsets.
                // This options is offered because on some database, AprioriClose does not
                // perform very well. Other algorithms could be added.
                AlgoDim algoDim = new AlgoDim(false, true);
                AlgoSeqDim algoSeqDim = new AlgoSeqDim();
                try {
                    //input = fileToPath(outputSegments+"segments.raw");
                    // Load a sequence database
                    contextMDDatabase.loadFile(input);

                    // Apply algorithm
                    AlgoBIDEPlus bideplus = new AlgoBIDEPlus(minsupp);  
                    algoSeqDim.runAlgorithm(contextMDDatabase, bideplus, algoDim, true, output);
                }catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }

                // Print results
                algoSeqDim.printStatistics(contextMDDatabase.size());
            }
            else
            {
                int n_clusters=clustercenters.length;
                //GSP algorithm (see ca.pfv.spmf.test.MainTestGSP*.java)
                double support = minimumsupport, mingap = 0, maxgap = Integer.MAX_VALUE, windowSize = 0;

                boolean keepPatterns = true;
                boolean verbose=true;

                for(int i=0;i<n_clusters;i++)
                {
                    File f = new File(outputSegments+i+".raw");
                    if(f.exists())
                    {
                        if(f.length()==0)
                        {
                            continue;
                        }
                    }
                    AbstractionCreator abstractionCreator = AbstractionCreator_Qualitative.getInstance();
                    ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.SequenceDatabase sequenceDatabase 
                            = new ca.pfv.spmf.algorithms.sequentialpatterns.gsp_AGP.items.SequenceDatabase(abstractionCreator);
                    
                    //bide+
                    ca.pfv.spmf.input.sequence_database_list_strings.SequenceDatabase seqbide = 
                            new ca.pfv.spmf.input.sequence_database_list_strings.SequenceDatabase();
                    try {
                        

                        if(false)
                        {//BIDE+
                            AlgoBIDEPlus_withStrings algobide  = new AlgoBIDEPlus_withStrings();  //
                            seqbide.loadFile(outputSegments+i+".raw");
                            // execute the algorithm
                            algobide.runAlgorithm(seqbide, outputMinedSequences+"\\raw\\"+i+".raw", 20);    
                        }
                        else
                        {//GSP
                            sequenceDatabase.loadFile(outputSegments+i+".raw", support);
                            AlgoGSP algorithm = new AlgoGSP(support, mingap, maxgap, windowSize,abstractionCreator);


                            System.out.println(sequenceDatabase.toString());

                            //Change the file path in order to change the destination file
                            algorithm.runAlgorithm(sequenceDatabase,keepPatterns,verbose, outputMinedSequences+"\\raw\\"+i+".raw");
                            System.out.println(algorithm.getNumberOfFrequentPatterns()+ " frequent pattern found.");

                            System.out.println(algorithm.printedOutputToSaveInFile());
                        }


                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        }
        
        //####################            
        //computation quality measures
        //####################
        List<ExtractedSequence> extractedSeqs = new LinkedList<>();    
        if(measureQuality)
        {
            //strings to use :  outputMinedSequences = base directory of mined sequences. look into raw//n.raw with n the cluster number
            //                  outputSegments = base directory for all segments and segments by cluster type
            String outputQuality ="..//data//segmentationWithAttitudeVariations//mined//allSeqQuality.csv";
            BufferedWriter bw;
            
            try {
                List<String> allSequences = retrieveSequences(outputSegments+"segments.raw");
                bw = new BufferedWriter(new FileWriter(new File(outputQuality)));
                bw.write("Sequence,Cluster,Dimension,Turn,Value,nBeforeCluster,nAllSegments,Support,Confidence,Lift,Conviction\n");

                for(int i=0;i<clustercenters.length;i++)
                {
                    String clusterStr =(friendliness)?"Frd":"Dom";
                    clusterStr+=(speaking)?",Spk,":",Lstn,";
                    clusterStr+=String.valueOf(clustercenters[i][1]);
                    int numSegmentsForCluster = retrieveSequences(outputSegments+i+".raw").size();
                    //for each cluster
                    List<String> freqSequencesInICluster = retrieveSequences(outputMinedSequences+"\\raw\\"+i+".raw"); //parses a .raw file outputted by GSP
                    double clusterSupport = ((double)numSegmentsForCluster)/((double)allSequences.size());
                    for(String seq : freqSequencesInICluster)
                    {
                        String[] splitSeq = seq.split(" ");
                        int nInCluster = Integer.valueOf(splitSeq[splitSeq.length-1]);
                        
                        String[] cleanedSplitSeq = cleanupMinedSequence(splitSeq);
                        int nInAllSegments = countSupport(cleanedSplitSeq,allSequences);
                        
                        /////THIS SHOULD BE CHECKED ! PROBABLY INCORRECT !!!
                        //double supportInCluster = ((double)nInCluster)/((double)freqSequencesInICluster.size());
                        double supportInAllSegments = ((double)nInCluster)/((double)allSequences.size());


                        double confidence = ((double)nInCluster)/((double)nInAllSegments);
                        double lift = confidence/((double)clusterSupport);
                        
                        double conviction = (1.0-((double)clusterSupport))/(1.0-confidence);
                        
                        String resultingLine="";
                        for(String s : cleanedSplitSeq) {
                            if(s.equalsIgnoreCase("-1"))
                                resultingLine+="-";
                            else
                                resultingLine+=Segmenter.REVERSE_NVBEVENT_INTEGER_MAP.get(Integer.valueOf(s));;
                        }
                        resultingLine+=","+i+","+clusterStr+","+nInCluster+","+nInAllSegments+","+supportInAllSegments+","+confidence+","+lift+","+conviction+"\n";
                        bw.write(resultingLine);
                        
                        String[] splitNoSup = new String[splitSeq.length-1];
                        for(int k=0;k<splitNoSup.length;k++) {splitNoSup[k]=splitSeq[k];}
                        
                        /*ExtractedSequence exseq = new ExtractedSequence(translateSequence(splitNoSup), 
                                i, dominance?"D":"F", supportInAllSegments, confidence, lift);
                        
                        extractedSeqs.add(exseq);*/
                    }
                }

                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(SeqMinMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //####################     
        //translation of mined sequences
        //####################     
        if(translate)
        {
            File minedDir = new File(outputMinedSequences+"\\raw\\");
            if(minedDir.isDirectory())
            {
                for(File f : minedDir.listFiles())
                {
                    try {
                        String nameFile=f.getName();
                        String wekaFile=f.getName();
                        nameFile=nameFile.substring(0, nameFile.length()-4)+".txt";
                        wekaFile=wekaFile.substring(0, nameFile.length()-4)+".weka";
                        translateMinedFile(f,outputMinedSequences+nameFile);
                        createWekaFile(f,outputMinedSequences+wekaFile,wekaFile.substring(0, nameFile.length()-4),nmaxSequence);
                    } catch (FileNotFoundException ex) {
                        System.err.println(ex.getMessage());
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                }
            }
        }
        
        
        System.out.println("finished");
    } 
    
    private static List<String> translateSequence(String[] seqInIntegers)
    {
        List<String> list = new ArrayList<String>();
        for(String s : seqInIntegers)
        {
            if(!s.equalsIgnoreCase("-1") && !s.equalsIgnoreCase("-2") && !s.equalsIgnoreCase("#SUP:"))
                list.add(Segmenter.REVERSE_NVBEVENT_INTEGER_MAP.get(Integer.valueOf(s)));
        }
        return list;
    }

    private static void translateMinedFile(File f, String outputFileName) throws FileNotFoundException, IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputFileName)));
        
        CSVReader csvr = new CSVReader(new FileReader(f),' ');
        String[] line;
        while((line=csvr.readNext()) != null)
        {
            String newLine="";
            for(int i=0;i<line.length;i++)
            {
                String s = line[i];
                if(s.equals("-1"))
                {
                    newLine+=" -> ";
                }
                else if(s.equals("#SUP:"))
                {
                    newLine+=" happens "+line[i+1];
                    break;
                }
                else
                {
                    newLine+=Segmenter.REVERSE_NVBEVENT_INTEGER_MAP.get(Integer.valueOf(s));
                }
            }
            newLine+=" times\n";
            bw.write(newLine);
        }        
        bw.close();
    }

    private static String[] cleanupMinedSequence(String[] dirtySeq) {
        String[] returnSeq = new String[dirtySeq.length-1];
        System.arraycopy(dirtySeq, 0, returnSeq, 0, returnSeq.length-1);
        returnSeq[returnSeq.length-1]="-2";
        return returnSeq;
    }

    public static int countSupport(String[] seqToCount, List<String> sequencesList) {
        int support=0;
        
        for(int seqNumber=0; seqNumber<sequencesList.size();seqNumber++)
        {
            String[] dataSeqSplit = sequencesList.get(seqNumber).split(" ");
            boolean found=false;
            int iCountedSeq =0;
            int iDataSeq =0;
            while(iDataSeq<dataSeqSplit.length)
            {//tant qu'on n'a pas epuise la sequence ou on regarde
                if(seqToCount[iCountedSeq].equalsIgnoreCase(dataSeqSplit[iDataSeq]))
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

    public static List<String> retrieveSequences(String filename) throws IOException {
        List<String> list = new LinkedList<>();
        BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
        String nextLine="";
        while((nextLine = br.readLine() )!=null)
        {
            list.add(nextLine);
        }
        return list;
    }
    

    public static List<List<String>> retrieveSequencesByEventType(Segmenter seg,String filename) throws IOException {
        List<String> listByInt = retrieveSequences(filename);
        List<List<String>> listByNVBEvent = new ArrayList<>();
        for(String seq : listByInt)
        {
            List<String> thisLine = new ArrayList<>();
            String[] splitSeq = seq.split(" ");
            String[] cleanedSplitSeq = cleanupMinedSequence(splitSeq);
            
            String resultingLine="";
            for(String s : cleanedSplitSeq) {
                if(s.equalsIgnoreCase("-1"))
                    continue;
                else
                    thisLine.add(seg.REVERSE_NVBEVENT_INTEGER_MAP.get(Integer.valueOf(s)));
            }
            listByNVBEvent.add(thisLine);
        }
        return listByNVBEvent;
    }

    private static void createWekaFile(File f, String outputfilename,String clusterStr,int nmaxseq) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputfilename)));
        
        CSVReader csvr = new CSVReader(new FileReader(f),' ');
        String[] line;
        while((line=csvr.readNext()) != null)
        {
            int count=Integer.parseInt(line[line.length-1]);
            for(int j=0;j<count;j++)
            {
                int nbSignaux=0;
                String newLine="";
                for(int i=0;i<line.length;i++)
                {
                    String s = line[i];
                    if(s.equals("-1"))
                    {
                        newLine+=",";
                        nbSignaux++;
                    }
                    else if(s.equals("#SUP:"))
                    {
                        break;
                    }
                    else
                    {
                        newLine+=Segmenter.REVERSE_NVBEVENT_INTEGER_MAP.get(Integer.valueOf(s));
                    }
                }
                if(nbSignaux>nmaxseq)
                    continue;
                while(nbSignaux<nmaxseq)
                {
                    newLine+=",";
                    nbSignaux++;
                }
                newLine+=clusterStr+"\n";
                bw.write(newLine);
                
            }
                
                /*String s = line[i];
                if(s.equals("-1"))
                {
                    newLine+=" -> ";
                }
                else if(s.equals("#SUP:"))
                {
                    newLine+=" happens "+line[i+1];
                    break;
                }
                else
                {
                    newLine+=Segmenter.REVERSE_NVBEVENT_INTEGER_MAP.get(Integer.valueOf(s));
                }*/
            
        }        
        bw.close();
    }
}
