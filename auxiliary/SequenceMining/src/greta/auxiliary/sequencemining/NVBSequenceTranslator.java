/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining;

import greta.auxiliary.sequencemining.structures.NVBEvent;
import greta.auxiliary.sequencemining.structures.Modality;
import greta.auxiliary.sequencemining.exceptions.DifferentFrameSizeException;
import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * I use the OPENCSV library http://opencsv.sourceforge.net/
 * And the SPMF library http://www.philippe-fournier-viger.com/spmf/index.php
 * @author Mathieu
 */
public class NVBSequenceTranslator {
    
    public static List<Integer> INDICES_TO_COMPARE;
    public static Map<String,Modality> MAP_STRING_MODALITY;
    
    public NVBSequenceTranslator(List<Integer> indices)
    {
        INDICES_TO_COMPARE = new LinkedList<>();
        for(Integer i : indices)
        {
            INDICES_TO_COMPARE.add(i);
        }
        MAP_STRING_MODALITY=new HashMap<>();
    }
    
    public NVBSequenceTranslator(){
        INDICES_TO_COMPARE=new LinkedList<>();
        INDICES_TO_COMPARE.add(8);
        INDICES_TO_COMPARE.add(10);
        INDICES_TO_COMPARE.add(12);
        INDICES_TO_COMPARE.add(14);
        INDICES_TO_COMPARE.add(16);
        INDICES_TO_COMPARE.add(18);
        INDICES_TO_COMPARE.add(20);
        INDICES_TO_COMPARE.add(21);
        //0,time        
        //1,occlusions
        //2,task
        //3,turn
        //4,Paraverbal
        //5,CandidateParaverbal
        //6,IPA
        //7,Topic
        //8,Eyes
        //9,HeadDrct
        //10,HeadDrctComplete
        //11,HeadMvmt
        //12,HeadMvmtIntRep
        //13,Eyebrows
        //14,EyebrowsComplete
        //15,Mouth
        //16,MouthComplete
        //17,Posture
        //18,PostureComplete
        //19,Gestures
        //20,GesturePartIntSpa
        //21,HandsPosition
        //22,ConcatGesturesTurn
        //23,DOCUMENT
        //24,HeadDrctIntensity
        //25,HeadMvmtIntRep
        //26,EyebrowsInt
        //27,MouthInt
        //28,PostureInt
        //29,GestureCommIntSpa
        //30,GestAdaptorPart
        MAP_STRING_MODALITY=new HashMap<>();
        MAP_STRING_MODALITY.put("EyesAt", Modality.Eyes);
        /*MAP_STRING_MODALITY.put("EyesAt", Modality.Eyes);
        MAP_STRING_MODALITY.put("EyesAt", Modality.Eyes);
        MAP_STRING_MODALITY.put("EyesAt", Modality.Eyes);
        MAP_STRING_MODALITY.put("EyesAt", Modality.Eyes);
        MAP_STRING_MODALITY.put("EyesAt", Modality.Eyes);
        MAP_STRING_MODALITY.put("EyesAt", Modality.Eyes);*/
    }
    
    public List<NVBEvent> translate (String inputfilename, String outputfilename, boolean speaking) throws FileNotFoundException,IOException 
    {       //output
        List<NVBEvent> sequenceOfEvents = new LinkedList<NVBEvent>();
        File f = new File(outputfilename); 
        if(f.exists())
        {
            System.err.println("output file already exists");
            return null;
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(f)); //writer
                
        //input
        CSVReader csvr = new CSVReader(new FileReader(inputfilename),';');
        
        //handle column definitions
        String[] lineDef = csvr.readNext();
                
        String[] nextLine, prevLine;
        
        //handle first Line
        prevLine = csvr.readNext();
        
        //handle rest of file
        while((nextLine=csvr.readNext()) != null)
        {
            try{
                
                //manage occlusions
                if(nextLine[1].equalsIgnoreCase("off"))
                {
                    prevLine=nextLine;
                    continue;
                }
                
                //manage turn
                if(speaking)
                {
                    if(nextLine[3].equalsIgnoreCase("Silence") 
                            || nextLine[3].equalsIgnoreCase("Candidate"))
                        {
                            prevLine=nextLine;
                            continue;
                        }
                }
                else
                {
                    if(nextLine[3].equalsIgnoreCase("Recruiter") 
                            || nextLine[3].equalsIgnoreCase("Interruption"))
                        {
                            prevLine=nextLine;
                            continue;
                        }
                }
                
                if (isEvent(prevLine,nextLine))
                {
                    List<Integer> indices = indexDifferences(prevLine, nextLine);
                    String diff = nextLine[0];
                    for(Integer i : indices)
                    {
                        if(!nextLine[i].equalsIgnoreCase("N/A"))
                        {
                            sequenceOfEvents.add(new NVBEvent(Modality.getModalityFromColNumber(i), 
                                    nextLine[i], Integer.valueOf(nextLine[0])));
                            diff = diff+","+nextLine[i];
                        }
                    }
                    diff+="\n";
                    //System.out.print(diff);
                    if(!diff.equalsIgnoreCase(nextLine[0]+"\n"))
                    {
                        bw.write(diff);
                    }
                    prevLine=nextLine;
                }
            }
                catch(DifferentFrameSizeException dfse)
            {
                System.err.println("error in the file definition: some times have more columns than others");
                break;
            }
        }
        bw.close();
        
        return sequenceOfEvents; // if no error
    }
    
    public List<NVBEvent> findEndTimes(List<NVBEvent> lst, String inputfilename) throws FileNotFoundException, IOException, DifferentFrameSizeException
    {
        List<NVBEvent> sequenceOfEvents = new LinkedList<NVBEvent>();
        //input
        CSVReader csvr = new CSVReader(new FileReader(inputfilename),';');
        List<String[]> myEntries = csvr.readAll();
        
        /*String[] line = csvr.readNext();//def
        line=csvr.readNext();//first event*/
        String line[] = myEntries.get(1);
        int i=0;
        int j = 1;
        boolean finished=false;
        while(!finished)
        {
            NVBEvent nvbev=null;
            if(i<lst.size())
                nvbev = lst.get(i);
            if(nvbev==null || line==null)
            {
                finished=true;
                
            }
            else
            {
                if(nvbev.start_time==100*(1+(Integer.parseInt(line[1])/100))
                        || nvbev.start_time==100*((Integer.parseInt(line[1])/100)))
                {
                    if(nvbev.type.equalsIgnoreCase(line[4]))
                    {
                        nvbev.end_time=100*(Integer.parseInt(line[2])/100);
                        sequenceOfEvents.add(nvbev);
                        i++;  
                    }
                    else
                    {
                        j++; 
                        line=myEntries.get(j);
                    }
                }
                else if(nvbev.start_time<100*(1+(Integer.parseInt(line[1])/100)))
                {
                    while(nvbev.start_time<=100*(1+(Integer.parseInt(line[1])/100)))
                    {
                        j--;
                        line=myEntries.get(j);
                    }
                }
                else
                {
                    j++; 
                    line=myEntries.get(j);
                }
            }
        }
        
        
        return sequenceOfEvents;
    }
    
    //translate csv file with each line being a time frame (ex, every 0.1second)
    //into a sequence of events
    public List<NVBEvent> translate(String inputfilename, String outputfilename) throws FileNotFoundException, IOException
    {
        //output
        List<NVBEvent> sequenceOfEvents = new LinkedList<NVBEvent>();
        File f = new File(outputfilename); 
        if(f.exists())
        {
            System.err.println("output file already exists");
            return null;
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(f)); //writer
                
        //input
        CSVReader csvr = new CSVReader(new FileReader(inputfilename),';');
        
        //handle column definitions
        String[] lineDef = csvr.readNext();
                
        String[] nextLine, prevLine;
        
        //handle first Line
        prevLine = csvr.readNext();
        
        //handle rest of file
        while((nextLine=csvr.readNext()) != null)
        {
            try{
                
                //manage occlusions
                if(nextLine[1].equalsIgnoreCase("off"))
                {
                    prevLine=nextLine;
                    continue;
                }
                
                if (isEvent(prevLine,nextLine))
                {
                    List<Integer> indices = indexDifferences(prevLine, nextLine);
                    String diff = nextLine[0];
                    for(Integer i : indices)
                    {
                        if(!nextLine[i].equalsIgnoreCase("N/A"))
                        {
                            sequenceOfEvents.add(new NVBEvent(Modality.getModalityFromColNumber(i), 
                                    nextLine[i], Integer.valueOf(nextLine[0])));
                            diff = diff+","+nextLine[i];
                        }
                    }
                    diff+="\n";
                    //System.out.print(diff);
                    if(!diff.equalsIgnoreCase(nextLine[0]+"\n"))
                    {
                        bw.write(diff);
                    }
                    prevLine=nextLine;
                }
            }
                catch(DifferentFrameSizeException dfse)
            {
                System.err.println("error in the file definition: some times have more columns than others");
                break;
            }
        }
        bw.close();
        
        return sequenceOfEvents; // if no error
    }
    
    //reads file to list<NVBEvent>
    public static List<NVBEvent> fileToList(String filePath) throws FileNotFoundException, IOException
    {
        String[] nextLine;
        List<NVBEvent> sequenceOfEvents = new LinkedList<NVBEvent>();
        CSVReader csvr = new CSVReader(new FileReader(filePath),',');
        while((nextLine=csvr.readNext()) != null)
        {
            
        }
        return sequenceOfEvents;
    }
    
    //given two frames, if they are the same return false, else return true
    public boolean isEvent(String[] prevFrame, String[] nextFrame) throws DifferentFrameSizeException
    {
        int n = prevFrame.length;
        if(n!=nextFrame.length) throw new DifferentFrameSizeException();
        
        for(Integer i : INDICES_TO_COMPARE)
        //for(int i=0;i<n;i++)
        {
            if(!prevFrame[i].equals(nextFrame[i]))
                return true;
        }
        return false;
    }
    
    //given two frames that are different, find the item numbers that are different
    public List<Integer> indexDifferences(String[] prevFrame, String[] nextFrame) throws DifferentFrameSizeException
    {
        int n = prevFrame.length;
        if(n!=nextFrame.length) throw new DifferentFrameSizeException();
        
        List<Integer> diffIndices = new LinkedList<Integer>();
        for(Integer i : INDICES_TO_COMPARE)
        //for(int i=0;i<n;i++)
        {
            if(!prevFrame[i].equals(nextFrame[i]))
                diffIndices.add(i);
        }
        return diffIndices;
    }
}
