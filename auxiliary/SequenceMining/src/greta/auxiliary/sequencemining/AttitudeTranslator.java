/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.sequencemining;

import greta.auxiliary.sequencemining.exceptions.NoValueInAnyTraceException;
import greta.auxiliary.sequencemining.structures.TimedValue;
import greta.auxiliary.sequencemining.exceptions.NoValueAtTimeInTraceException;
import greta.auxiliary.sequencemining.structures.AttitudeVariationEvent;
import au.com.bytecode.opencsv.CSVReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;
import greta.auxiliary.sequencemining.structures.AttitudeVariationEvent.VariationType;

/**
 *
 * @author Mathieu
 */
public class AttitudeTranslator {
    
    private int SECONDS_SMOOTHING_DELAY=1000; //in milliseconds
    private BigDecimal VALUE_SLOPE_SMOOTHING = BigDecimal.valueOf(0.05).setScale(3, RoundingMode.HALF_EVEN);
    
    private double domcaliboffset,domcalibmin,domcalibmax,frdcaliboffset,frdcalibmin,frdcalibmax;
    
    //reads GTrace file and outputs with a fixed time
    /*public List<TimedValue>  translate(String//[] 
            inputfiles, boolean domIfTrue) throws FileNotFoundException, IOException
    {
        List<List<TimedValue>> outputs=new LinkedList<>();
            File f = new File(outputfilename); 
            if(f.exists())
            {
                System.err.println("output file already exists");
                return null;
            }
        BufferedWriter bw = new BufferedWriter(new FileWriter(f)); //writer
            
        
        //for(String inputfilename :inputfiles)
        //{
        String inputfilename = inputfiles;//[0];
            List<TimedValue> output = regularizeTimeAndCalibrate(inputfilename, domIfTrue);
            //outputs.add(output);
        //}
        
        //List<TimedValue> finalAttitude = averageRegularizedFiles(outputs);
        //for(TimedValue tv : finalAttitude)
        //{
        //    bw.write(tv.time+";"+tv.value);
        //}
        
        return output;
    }*/

    public void setCalibration() {
        //these come from calibration files
        domcaliboffset=0.001132488;
        domcalibmin=-0.8911425;
        domcalibmax=0.90027115;
      
        frdcaliboffset=0.01142788;
        frdcalibmin=-0.87745152;
        frdcalibmax=0.9174302;
    }
    
    public double adjustValueToCalibration(double value, boolean domIfTrue)
    {
        if(domIfTrue) //dominance 
            return 2*(value-domcaliboffset)/(domcalibmax-domcalibmin);
        else
            return 2*(value-frdcaliboffset)/(frdcalibmax-frdcalibmin);
    }

    //regularize time steps, adjust values to calibration
    public List<TimedValue> translate(String inputfilename, boolean domIfTrue) throws IOException, FileNotFoundException {

        //input
        CSVReader csvr = new CSVReader(new FileReader(inputfilename),';');

        //output
        List<TimedValue> output = new LinkedList<TimedValue>();

        String[] nextLine = csvr.readNext();

        int previousTime = ((int) (Double.valueOf(nextLine[0])*10));

        int numberOfValuesOnTime=1;
        double valueOnTime=Double.valueOf(nextLine[1]);

        
        
        while((nextLine=csvr.readNext()) != null)
        {
            int currentTime = ((int) (Double.valueOf(nextLine[0])*10));
            if(currentTime!=previousTime)
            {
                //average values in the time frame
                BigDecimal roundedvalue = BigDecimal.valueOf(valueOnTime/numberOfValuesOnTime).setScale(3, RoundingMode.HALF_EVEN);
                output.add(new TimedValue(previousTime*100, roundedvalue));
                //System.out.println(previousTime*100+" "+valueOnTime/numberOfValuesOnTime);
                //set new time frame
                previousTime=currentTime;
                numberOfValuesOnTime=1;
                
                valueOnTime=adjustValueToCalibration(Double.valueOf(nextLine[1]), domIfTrue);
                
            }
            else
            {
                numberOfValuesOnTime++;
                valueOnTime+=adjustValueToCalibration(Double.valueOf(nextLine[1]), domIfTrue);
            }
        }
        return output;
    }

    //take multiple traces, average them and remove values not present in some files
    private List<TimedValue> averageRegularizedFiles(List<List<TimedValue>> outputs) {
        int numberOutputs=outputs.size();
        List<TimedValue> finalOutput = new LinkedList<TimedValue>();
        
        int start_time=0;
        int end_time=Integer.MAX_VALUE;
        
        
        for(int i=0;i<numberOutputs;i++)
        {
            //conservative way : only take when in all files
            start_time = Math.max(start_time, outputs.get(i).get(0).time);
            end_time = Math.min(end_time, outputs.get(i).get(outputs.get(i).size()-1).time);
        }
        
        
        //0.1 timestep
        for(int j=start_time;j<end_time;j=j+100)
        {
            try {
                finalOutput.add(new TimedValue(j, averagedTimeValue(j,outputs)));
            } catch (NoValueInAnyTraceException ex) {
                System.err.println("no value in any trace for time "+j);
            }
        }
        
        //plot(finalOutput);
        
        return finalOutput;
    }
    
    //take multiple traces, average them and remove values not present in some files
    //this variation returns only the values, not the times
    private List<BigDecimal> averageRegularizedFilesValuesOnly(List<List<TimedValue>> outputs) {
        int numberOutputs=outputs.size();
        List<BigDecimal> finalOutput = new LinkedList<BigDecimal>();
        
        int start_time=0;
        int end_time=Integer.MAX_VALUE;
        
        
        for(int i=0;i<numberOutputs;i++)
        {
            //conservative way : only take when in all files
            start_time = Math.max(start_time, outputs.get(i).get(0).time);
            end_time = Math.min(end_time, outputs.get(i).get(outputs.get(i).size()-1).time);
        }
        
        
        //0.1 timestep
        for(int j=start_time;j<end_time;j=j+100)
        {
            try {
                finalOutput.add(averagedTimeValue(j,outputs));
            } catch (NoValueInAnyTraceException ex) {
                System.err.println("no value in any trace for time "+j);
            }
        }
        
        //plot(finalOutput);
        
        return finalOutput;
    }
    
    /*public boolean cluster(String[] inputfiles, String outputfilename, boolean domIfTrue) throws IOException
    {
        List<List<TimedValue>> outputs=new LinkedList<>();
            File f = new File(outputfilename); 
            if(f.exists())
            {
                System.err.println("output file already exists");
                return false;
            }
        BufferedWriter bw = new BufferedWriter(new FileWriter(f)); //writer
            
        
        for(String inputfilename :inputfiles)
        {
            List<TimedValue> output = regularizeTimeAndCalibrate(inputfilename, domIfTrue);
            outputs.add(output);
        }
        
        List<BigDecimal> listValuesToCluster = averageRegularizedFilesValuesOnly(outputs);
        
        for(BigDecimal d : listValuesToCluster)
            bw.write(d+"\n");
        
        return true;
        
    }*/
    
    public List<AttitudeVariationEvent> variations(String inputfilename, boolean domIfTrue) throws IOException
    {       
        
        List<TimedValue> input = translate(inputfilename, domIfTrue);
        return findVariationEvents(input,inputfilename);
    }
    
    public List<AttitudeVariationEvent> findVariationEvents(List<TimedValue> input,String sourcefile)
    {
        List<AttitudeVariationEvent> variations = new LinkedList<>();
        
        List<Integer> varTimes = new LinkedList<>();
        
        TimedValue previousTV = input.get(0);
        
        VariationType current_event_var_type = VariationType.PLATEAU;
        int current_event_start_time = previousTV.time;
        BigDecimal current_event_value=previousTV.value;
        
        for(TimedValue tv : input)
        {
            //if NaN
            if(previousTV.time!=tv.time-100)
            {
                varTimes.add(current_event_start_time);
                if(current_event_var_type.equals(VariationType.PLATEAU))
                    variations.add(new AttitudeVariationEvent(current_event_var_type,
                            current_event_value,current_event_start_time, tv.time-current_event_start_time,sourcefile));
                else
                {
                    variations.add(new AttitudeVariationEvent(current_event_var_type,
                            tv.value.subtract(current_event_value),current_event_start_time, tv.time-current_event_start_time,sourcefile));
                }
            }
            
            //else{
            
            if( !(tv.value.compareTo(previousTV.value) ==0) && current_event_var_type.equals(VariationType.PLATEAU))
        {
                //here, we assume it is a true one
                //end of previous plateau
                varTimes.add(current_event_start_time);
                variations.add(new AttitudeVariationEvent(current_event_var_type,
                        current_event_value,current_event_start_time, tv.time-current_event_start_time,sourcefile));

                //start new slope
                if(tv.value.compareTo(previousTV.value)<0)
                    current_event_var_type = VariationType.FALL;
                else
                    current_event_var_type = VariationType.RISE;

                current_event_start_time = tv.time;
                current_event_value = tv.value; // if slope, it is used to compute the full slopte at the end
            }
            else if(!(tv.value.compareTo(previousTV.value) ==0) && 
                    (current_event_var_type.equals(VariationType.FALL)||current_event_var_type.equals(VariationType.RISE)))
            {
                //continuation of slope, nothing to do
            }
            else if((tv.value.compareTo(previousTV.value) ==0) && 
                    (current_event_var_type.equals(VariationType.FALL)||current_event_var_type.equals(VariationType.RISE)))
            {
                //here, we assume it is a true one
                //end of previous slope
                varTimes.add(current_event_start_time);
                variations.add(new AttitudeVariationEvent(current_event_var_type,
                        tv.value.subtract(current_event_value),current_event_start_time, tv.time-current_event_start_time,sourcefile));

                //start new plateau
                current_event_var_type = VariationType.PLATEAU;
                current_event_start_time = tv.time;
                current_event_value = tv.value; // if slope, it is used to compute the full slopte at the end
            }
            else if((tv.value.compareTo(previousTV.value) ==0) && current_event_var_type.equals(VariationType.PLATEAU))
            {
                //continuation of plateau, nothing to do
            }
            //}
            previousTV=tv;
        }
        return variations;
    }
    

    //return the average value for a time j
    private BigDecimal averagedTimeValue(int j, List<List<TimedValue>> traces) throws NoValueInAnyTraceException {
        BigDecimal averagedValue=BigDecimal.valueOf(0.0).setScale(3, RoundingMode.HALF_EVEN);
        
        int numberOfPresentValues=0;
        for(int traceNumber=0; traceNumber<traces.size();traceNumber++)
        {
            numberOfPresentValues++;
            try
            {
                averagedValue.add(findValueForTime(j,traces.get(traceNumber)));
            }
            catch(NoValueAtTimeInTraceException noval)
            {
                numberOfPresentValues--;
            }
        }
        
        if(numberOfPresentValues==0) throw new NoValueInAnyTraceException();
        
        return BigDecimal.valueOf(averagedValue.doubleValue()/numberOfPresentValues).setScale(3,RoundingMode.HALF_EVEN);
    }
    
    //finds the value in a trace for time actualtime
    private BigDecimal findValueForTime(int actualTime, List<TimedValue> trace) throws NoValueAtTimeInTraceException
    {
        BigDecimal value =BigDecimal.valueOf(0.0).setScale(3, RoundingMode.HALF_EVEN);
        boolean stillNoValue=true;

        for(TimedValue tv : trace)
        {
            if(actualTime==tv.time)
            {
                stillNoValue=false;
                value=tv.value;
                break;
            }
        }
        
        if(stillNoValue)
            throw new NoValueAtTimeInTraceException();
        
        return value;
    }
    
    //unused 
    //finds the index in a trace for time actualtime
    /*private int findIndexForTime(int actualTime, List<TimedValue> trace) throws NoValueAtTimeInTraceException 
    {
        int index =0;
        boolean stillNoValue=true;

        for(int i=0;i<trace.size();i++)
        {
            TimedValue tv = trace.get(i);
            if(actualTime==tv.time)
            {
                stillNoValue=false;
                index=i;
                break;
            }
        }
        
        if(stillNoValue)
            throw new NoValueAtTimeInTraceException();
        
        return index;
    }*/

    List<AttitudeVariationEvent> smootheVariations(List<AttitudeVariationEvent> variations) {
        
        List<AttitudeVariationEvent> smoothedVariations = new LinkedList<>();
        variations.remove(0);
        smoothedVariations.add(variations.get(0));
        
        //first: smoothe too short plateaux iff surrounded by slopes of same type
        for(int i=1;i<variations.size()-1;i++)
        {
            AttitudeVariationEvent sourceVar = variations.get(i);
            AttitudeVariationEvent previousVar = smoothedVariations.get(smoothedVariations.size()-1);
            if(sourceVar.type.equals(VariationType.PLATEAU) &&sourceVar.duration<SECONDS_SMOOTHING_DELAY )
            {
                //check if before ending of trace
                if(variations.get(i+1).time!=sourceVar.time+sourceVar.duration //end of local trace (break in clicking mouse)
                        || !variations.get(i+1).sourceFile.equals(sourceVar.sourceFile)) //new variations file
                {
                    //end of trace : still add plateau
                    smoothedVariations.add(sourceVar);
                    i++; //go one variation further
                    smoothedVariations.add(variations.get(i));
                    continue;
                }
                
                if(previousVar.type.equals(variations.get(i+1).type))
                {
                    AttitudeVariationEvent mergedSlope = 
                            new AttitudeVariationEvent(previousVar.type, 
                            previousVar.value.add(variations.get(i+1).value), 
                            previousVar.time, 
                            variations.get(i+1).duration+sourceVar.duration+previousVar.duration, 
                            previousVar.sourceFile);
                    smoothedVariations.remove(smoothedVariations.size()-1); //
                    smoothedVariations.add(mergedSlope);
                    i++;
                    continue;
                }
            }   
            else
            {
                smoothedVariations.add(sourceVar);
            }
        }
       
        //second: smoothe too short or small slopes
        List<AttitudeVariationEvent> secondSmoothedVariations = new LinkedList<>();
        secondSmoothedVariations.add(smoothedVariations.get(0)); 
        for(int i=1;i<smoothedVariations.size()-1;i++)
        {
            AttitudeVariationEvent sourceVar = smoothedVariations.get(i);
            AttitudeVariationEvent previousVar = secondSmoothedVariations.get(secondSmoothedVariations.size()-1);
            if(!sourceVar.type.equals(VariationType.PLATEAU) && //rise or fall
                    (sourceVar.duration<SECONDS_SMOOTHING_DELAY //too short
                    ||sourceVar.value.abs().subtract(VALUE_SLOPE_SMOOTHING).signum()<0)) //too small
            {
                if(previousVar.type.equals(smoothedVariations.get(i+1).type)) //should always be the case ?
                {
                    AttitudeVariationEvent mergedPlateau = 
                            new AttitudeVariationEvent(previousVar.type, //plateau
                            previousVar.value.add(variations.get(i+1).value).divide(BigDecimal.ONE.add(BigDecimal.ONE)), //mean
                            previousVar.time, 
                            variations.get(i+1).duration+sourceVar.duration+previousVar.duration, 
                            previousVar.sourceFile);
                    secondSmoothedVariations.remove(secondSmoothedVariations.size()-1); //
                    secondSmoothedVariations.add(mergedPlateau);
                    i++;
                    continue;
                }                
                
                
            }
            else
            {
                secondSmoothedVariations.add(sourceVar);
            }
        }        
        
        //old code that produced wrong segmentation
        /*for(int i=1;i<variations.size()-1;i++)
        {
            AttitudeVariationEvent sourceVar = variations.get(i);
            //RISE OR FALL
            if(sourceVar.type.equals(VariationType.RISE)||sourceVar.type.equals(VariationType.FALL)
                    && sourceVar.duration<SECONDS_SMOOTHING_DELAY || sourceVar.value.abs().subtract(VALUE_SLOPE_SMOOTHING).signum()==-1)
            {
            //if variation smaller than minimal duration, or smaller than minimal increase
                //last check if next event even smaller
                if(variations.get(i+1).duration<sourceVar.duration)
                {
                    smoothedVariations.add(sourceVar);
                }
                else
                {
                    int start_time = smoothedVariations.get(smoothedVariations.size()-1).time;
                    //smoothe with surrounding plateaux
                    int smoothedPlateauDuration = 
                        smoothedVariations.get(smoothedVariations.size()-1).duration
                            +sourceVar.duration
                            +variations.get(i+1).duration;
                    double prevPercetange = ((double)smoothedVariations.get(smoothedVariations.size()-1).duration)/smoothedPlateauDuration;
                    double slopePercetange=((double)sourceVar.duration)/smoothedPlateauDuration;
                    double nextPercentage=((double)variations.get(i+1).duration)/smoothedPlateauDuration;

                    double smoothedValue = 
                        (smoothedVariations.get(smoothedVariations.size()-1).value.doubleValue()*prevPercetange
                            +variations.get(i+1).value.doubleValue()*nextPercentage
                            +(sourceVar.value.doubleValue()/2+smoothedVariations.get(smoothedVariations.size()-1).value.doubleValue())*slopePercetange);
                    BigDecimal smoothedBD = BigDecimal.valueOf(smoothedValue).setScale(3, RoundingMode.HALF_EVEN);

                    smoothedVariations.remove(smoothedVariations.size()-1);
                    smoothedVariations.add(new AttitudeVariationEvent(VariationType.PLATEAU,smoothedBD,start_time,smoothedPlateauDuration,sourceVar.sourceFile));
                    i++;//skip next plateau : it has been merged 
                }
            }
            else if(sourceVar.type.equals(VariationType.PLATEAU) &&sourceVar.duration<SECONDS_SMOOTHING_DELAY )
            {
                //PLATEAU very small
                //check if changing direction of slope !
                if((variations.get(i+1).type.equals(VariationType.RISE) 
                        && smoothedVariations.get(smoothedVariations.size()-1).type.equals(VariationType.FALL))
                        ||
                        (variations.get(i+1).type.equals(VariationType.FALL) 
                        && smoothedVariations.get(smoothedVariations.size()-1).type.equals(VariationType.RISE)))
                {
                    smoothedVariations.add(sourceVar);
                }
                else if(variations.get(i+1).duration<sourceVar.duration)
                {
                    //last check if next event even smaller
                    smoothedVariations.add(sourceVar);
                }
                else
                {
                    int start_time = smoothedVariations.get(smoothedVariations.size()-1).time;
                    //smoothe with surrounding slopes
                    int smoothedSlopeDuration = 
                        smoothedVariations.get(smoothedVariations.size()-1).duration
                            +sourceVar.duration
                            +variations.get(i+1).duration;
                    
                    BigDecimal smoothedBD = 
                            smoothedVariations.get(smoothedVariations.size()-1).value.add(variations.get(i+1).value);
                    
                    smoothedVariations.remove(smoothedVariations.size()-1);
                    smoothedVariations.add(new AttitudeVariationEvent(variations.get(i-1).type,smoothedBD,start_time,smoothedSlopeDuration,sourceVar.sourceFile));
                    i++;//skip next slope : it has been merged
                }
                
            }
            else
            {
                //no problem
                smoothedVariations.add(sourceVar);
            }
            
        }*/
        return secondSmoothedVariations;
    }

    public static void writeToFile(List<AttitudeVariationEvent> normalizedVars, String outputVariations) throws IOException {
        Util.checkDeleteOutputFile(outputVariations);
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputVariations))); //writer
        for(AttitudeVariationEvent ave : normalizedVars)
        {
            bw.write(ave.time+","+ave.type+","+ave.duration+","+ave.value+","+ave.sourceFile+"\n");
        }
        bw.close();
    }


    void writeAttitudeFile(List<TimedValue> attitudes, String outputPath) throws IOException {
        Util.checkDeleteOutputFile(outputPath);
        //Util.checkDeleteOutputFile(filename);
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath))); //writer
        for(TimedValue tv : attitudes)
        {
            bw.write(tv.time+" "+tv.value.doubleValue()+"\n");
        }
        bw.close();
    }
}
