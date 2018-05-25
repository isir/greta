/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.signals;
//package vib.auxiliary.player.ogre.capture;


import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesEmitter;
import vib.core.animation.mpeg4.bap.BAPFramesEmitterImpl;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;
import vib.core.animation.mpeg4.bap.BAPType;
import vib.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import vib.core.keyframes.face.AUEmitter;
import vib.core.keyframes.face.AUPerformer;
import vib.core.repositories.AUAPFrame;
import vib.core.util.Constants;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.log.Logs;
import vib.core.util.time.Timer;

//import vib.auxiliary.player.ogre.capture;

/**
 * This class is an implementation of {@code SignalEmitter} interface.<br/> When
 * calling the {@code load} function, It sends the {@code Signals} contained in
 * a specified BML file to all {@code SignalPerformers} added with the
 * {@code addSignalPerformer} function.
 *
 * @author Thomas Janssoone
 */
public class AUParserFileReader extends FAPFrameEmitterImpl implements AUEmitter, BAPFramesEmitter, SignalEmitter {
    private static final Logger LOGGER = Logger.getLogger( AUParserFileReader.class.getName() );
    
    private ArrayList<AUPerformer> au_perfomers = new ArrayList<>();
    private ArrayList<AUAPFrame> au_frames = new ArrayList<>();

    private ArrayList<BAPFramesPerformer> bap_perfomers = new ArrayList<>();
    private ArrayList<BAPFrame> bap_frames = new ArrayList<>();
    
    BAPFramesEmitterImpl bapFramesEmitterImpl = new BAPFramesEmitterImpl();
    
    boolean isPerforming = false;
    //Phil
    File dirPath;
    String[] selectedHeaders = null;
    
    /**
     * Loads BML as csv files.<br/> The behavior signals in the specified file will be
     * send to all {@code SignalPerformer} added with the
     * {@link #addSignalPerformer(vib.core.signals.SignalPerformer) addSignalPerformer} function.<br/> The
     * base file name of the BML file is used as {@code requestId} parameter
     * when calling the
     * {@link vib.core.signals.SignalPerformer#performSignals(java.util.List, vib.core.util.id.ID, vib.core.util.Mode) performSignals}
     * function.
     *
     * @param csvDir the name of the directory to load
     * @return The ID of the generated event
     */
    public ID load(String csvDir) {     
        Logs.info( String.format("AUParserFileReader.open(%s)",csvDir));
        
        File d = new File(csvDir);
        if(d.exists()){
            Logs.info( String.format("Setting directory to: %s", d.getAbsolutePath()));
            
            dirPath = d;            
        }
        else            
            Logs.warning(String.format("Directory invalid: %s", d.getAbsolutePath()));
           
        ID id = IDProvider.createID(d.getName());
       
        return id;
    }
    
    /**
     * Loads headers from the first csv file in the selected directory.
     *
     * @return headers as a String array
     */
    public String[] list(){
        if(dirPath!=null){
            File[] files = dirPath.listFiles((File file, String name1) -> name1.contains(".csv"));
            if(files.length>0)
                return getHeaders(files[0]);
        }
        return null;
    }
    
     /**
     * Set selected headers 
     *
     * @param selected headers to use
     * 
     */
    public void setSelected(String[] selected){
        if(selected!=null){
            selectedHeaders = selected;
            Logs.info("Setting selected headers to: "+Arrays.toString(selected));
        }
        else
            Logs.warning("No header selected");
    }
    
    private String[] getHeaders(File f){
        BufferedReader brTest = null;
        String[] headers = null;
        try {
            brTest = new BufferedReader(new FileReader(f));
            String line1 = brTest.readLine();
            headers = line1.split(",");                    
        } catch (IOException ex) {            
            LOGGER.log(Level.SEVERE, null, ex);
        }
        finally{
            if(brTest!=null)
                try {
                    brTest.close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
        }
        
        return headers;
    }
    
    public int loadFile(String csvFile){
        int length = 0;
        
        File f = new File(csvFile);
        if(f.exists()){
            if(csvFile.endsWith("openface")){
                length = loadOpenFace(csvFile);
            }
            else if(csvFile.endsWith("nicolle")){
                length = loadNicolle(csvFile);
            }
            else if(csvFile.endsWith("csv")){
                length = loadNicolle(csvFile);
            }
        }
        if(length==0)
            Logs.warning("File invalid: "+f.getAbsolutePath());
        
        return length;
    }
    
    public File getDirPath(){
        return dirPath;
    }
    
    //Format based on https://github.com/TadasBaltrusaitis/OpenFace
    public int loadOpenFace(String csvFile) { 
        Logs.info(String.format("AUParserFileReader.loadOpenFace(%s)", csvFile));
        
        isPerforming = true;
        au_frames.clear();
        bap_frames.clear();
        
        // open the file  
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        
        double min_time = Double.MAX_VALUE;
        double max_time = 0.0;
            
        try {
            br = new BufferedReader(new FileReader(csvFile));
            //int [] au_correspondance = {0, 1, 2, 4,5,6,9,12,15,17,20,25,26};
            //double [] prev_value_au = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            //String[] header_string = { "timestamp", " AU01_r", " AU02_r", " AU04_r", " AU05_r", " AU06_r", " AU07_r", " AU09_r", " AU10_r", " AU12_r", " AU14_r", " AU15_r", " AU17_r", " AU20_r", " AU23_r", " AU25_r", " AU26_r", " AU45_r"};
            String[] header_string = { "timestamp", "AU01_r", "AU02_r", "AU04_r", "AU05_r", "AU06_r", "AU07_r", "AU09_r", "AU10_r", "AU12_r", "AU14_r", "AU15_r", "AU17_r", "AU20_r", "AU23_r", "AU25_r", "AU26_r"};
            // String[] header_string = {"time",	"1",	"2",	"4",	"5",	"6",	"9",	"12",	"15",	"17",	"20",	"25",	"26"};
            List<String> header_list = Arrays.asList(header_string);
            int [] au_to_col = new int[header_string.length];
            int [] au_correspondance = new int[header_string.length-1];
            double [] prev_value_au = new double[header_string.length];
            
            double prev_gaze_x = 0.0;
            double prev_gaze_y = 0.0;
            
            double prev_blink = 0.0;
            int col_blink = 412;            
        
            for(int autc=0; autc < au_to_col.length; autc++){
                au_to_col[autc] = -1;
                prev_value_au[autc] = 0.0;
            }
            
            for(int auc=0; auc < au_correspondance.length; auc++){
                String val = header_string[auc+1].replace(" AU0", "");
                val = val.replace("AU","");
                val = val.replace("_r","");
                //System.out.println("auc="+auc+" -> "+val);   
                au_correspondance[auc] = Integer.parseInt( val);
            }
            
            double alpha = 0.75;//1.0;
            
            //lecture header
            if ((line = br.readLine()) != null) {
                String[] header  = line.split(cvsSplitBy);
                
                for(int h=0; h < header.length; h++){
                    String value = header[h];
                    value = value.replace(" ", "");
                    int index = header_list.indexOf(value);
                    if(index!=-1 & index<au_to_col.length){
                        au_to_col[index] = h;
                    }
                    
                    if("AU45_r".equals(value))
                        col_blink = h;
                    
                    Logs.debug("header["+h+"] = "+value);
                }
                
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(cvsSplitBy);
                    
                    double time;
                    //String val = values[1].replace(',', '.');
                    String val = values[1];                    
                    Logs.debug("time: "+val);
                    
                    time = Double.parseDouble(val);
                    
                    if( time > max_time){
                        max_time = time;
                    }
                    if( time < min_time){
                        min_time = time;
                    }
                    AUAPFrame au_frame = new AUAPFrame();
                    au_frame.setFrameNumber((int) (time * Constants.FRAME_PER_SECOND) + 1);

                    for(int au=1; au<au_to_col.length; au++){
                        String value = values[au_to_col[au]];
                        if(isNumeric(value)){
                            double intensity =alpha*( Double.parseDouble(value)/5.0) + (1-alpha)*prev_value_au[au];
                     //       System.out.println("AU["+au_correspondance[au]+"] : "+intensity);
                            au_frame.setAUAPboth(au_correspondance[au-1], intensity);
                            prev_value_au[au]=intensity;
                        }
                    }
                    
                    //gaze
                    double gaze_x = alpha*(0.5*(Double.parseDouble(values[4])+Double.parseDouble(values[7])))+(1-alpha)*prev_gaze_x;
                    double gaze_y = alpha*(0.5*(Double.parseDouble(values[5])+Double.parseDouble(values[8])))+(1-alpha)*prev_gaze_y;
                    if(gaze_x<0){
                        au_frame.setAUAPboth(62, gaze_x);
                    }
                    else{
                        au_frame.setAUAPboth(61, gaze_x);
                    }
                    
                    if(gaze_y<0){
                        au_frame.setAUAPboth(64, gaze_y);
                    }
                    else{
                        au_frame.setAUAPboth(63, gaze_y);
                    }
                    prev_gaze_x = gaze_x;
                    prev_gaze_y = gaze_y;
                    
                    //blink
                    double blink = alpha*(Double.parseDouble(values[col_blink].replace(',', '.'))/5.0)+(1-alpha)*prev_blink;
                    au_frame.setAUAPboth(43, gaze_y);
                    prev_blink = blink;
                    
                    au_frames.add(au_frame);

                    BAPFrame hmFrame = new BAPFrame();
                    hmFrame.setFrameNumber((int) (time * Constants.FRAME_PER_SECOND) + 1);

                    double rot_X_deg =  0.0;//alpha*Math.toDegrees(rot_X_rad)+(1-alpha)*prev_rot_X;              
                    
                    double rot_Y_deg = 0.0;//alpha*Math.toDegrees(rot_Y_rad)+(1-alpha)*prev_rot_Y;
                   
                    //double rot_Z_rad = 1.0*Double.parseDouble(values[13]);
                    double rot_Z_deg = 0.0;//alpha*Math.toDegrees(rot_Z_rad)+(1-alpha)*prev_rot_Z;
                 
                    hmFrame.setDegreeValue(BAPType.vc3_roll, rot_X_deg);
                    hmFrame.setDegreeValue(BAPType.vc3_torsion, rot_Y_deg);
                    hmFrame.setDegreeValue(BAPType.vc3_tilt, rot_Z_deg);      
    
                    bap_frames.add(hmFrame);
                }
                 
            }
            
        } catch (IOException e) {
            Logs.error(e.getMessage());
            LOGGER.log( Level.SEVERE, e.toString(), e );
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Logs.error(e.getMessage());
                    LOGGER.log( Level.SEVERE, e.toString(), e );
                }
            }
        }
                   
        int length = (int) ((int ) max_time-min_time);
        send(length);        

        return length;
    }

    
    public int loadNicolle(String csvFile) {   
        Logs.info("AUParserFileReader.loadNicolle: "+csvFile);
        
        isPerforming = true;
        au_frames.clear();
        bap_frames.clear();
        
        // open the file  
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        

        double min_time = Double.MAX_VALUE;
        double max_time = 0.0;
            
        try {
            br = new BufferedReader(new FileReader(csvFile));
            
            //int [] au_correspondance = {0, 1, 2, 4,5,6,9,12,15,17,20,25,26};
            //double [] prev_value_au = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            //String[] header_string = { "timestamp", " AU01_r", " AU02_r", " AU04_r", " AU05_r", " AU06_r", " AU07_r", " AU09_r", " AU10_r", " AU12_r", " AU14_r", " AU15_r", " AU17_r", " AU20_r", " AU23_r", " AU25_r", " AU26_r", " AU45_r"};
            //String[] header_string = { "timestamp", "AU01_r", "AU02_r", "AU04_r", "AU05_r", "AU06_r", "AU07_r", "AU09_r", "AU10_r", "AU12_r", "AU14_r", "AU15_r", "AU17_r", "AU20_r", "AU23_r", "AU25_r", "AU26_r"};
            String[] header_string = {"time",	"1",	"2",	"4",	"5",	"6",	"9",	"12",	"15",	"17",	"20",	"25",	"26"};
            if(selectedHeaders!=null)
                header_string = selectedHeaders;
            List<String> header_list = Arrays.asList(header_string);
            int [] au_to_col = new int[header_string.length-1];
            int [] au_correspondance = new int[header_string.length-1];
            double [] prev_value_au = new double[header_string.length-1];
 
            double prev_rot_X = 0.0;
            double prev_rot_Y = 0.0;
            double prev_rot_Z = 0.0;

            double last_blink = 0.0;
            int blink_process = 0;
            double span_blink = 3.0;
            for(int autc=0; autc < au_to_col.length; autc++){
                au_to_col[autc] = -1;
                prev_value_au[autc] = 0.0;
            }
            
            for(int auc=0; auc < au_correspondance.length; auc++){
                String val = header_string[auc+1].replace(" AU0", "");
                val = val.replace("AU","");
                val = val.replace("_r","");
                //System.out.println("auc="+auc+" -> "+val);   
                au_correspondance[auc] = Integer.parseInt( val);
            }
            
            double alpha = 0.75;
            
            //lecture header
            if ((line = br.readLine()) != null) {
                String[] header  = line.split(cvsSplitBy);
                
                for(int h=0; h < header.length; h++){
                    String value = header[h];
                    value = value.replace(" ", "");
                    int index = header_list.indexOf(value);
                    if(index!=-1 & index<au_to_col.length){
                        au_to_col[index] = h;
                    }
                }
                
                while ((line = br.readLine()) != null) {
                    if(line.contains("NAN")==false){
                        String[] values = line.split(cvsSplitBy);

                        double time;
                        //String val = values[1].replace(',', '.');
                        String val = values[0];
                        time = Double.parseDouble(val);
                        Logs.debug("time: "+time);
                        
                        if( time > max_time){
                            max_time = time;
                        }
                        if( time < min_time){
                            min_time = time;
                        }
                        AUAPFrame au_frame = new AUAPFrame();
                        au_frame.setFrameNumber((int) (time * Constants.FRAME_PER_SECOND) + 1);

                        for(int au=1; au<au_to_col.length; au++){                            
                            //System.out.println("value_AU["+au_correspondance[au]+"] : "+str_value);
                            double value = Double.parseDouble(values[au_to_col[au]]);
                            //System.out.println("value_AU["+au_correspondance[au]+"] : "+value);
                            
                            Logs.debug("value_AU["+au_correspondance[au]+"]: "+value);
                            double intensity =alpha*( value/5.0) + (1-alpha)*prev_value_au[au];
                            //System.out.println("AU["+au_correspondance[au]+"] : "+intensity);
                            au_frame.setAUAPboth(au_correspondance[au-1], intensity);
                            prev_value_au[au]=intensity;
                        }
                        
                        //blink
                        if(time-last_blink>1.5){
                            if(blink_process<2*span_blink){
                                blink_process++;
                                if(blink_process<=span_blink){
                                    double blink = blink_process/span_blink;//alpha*(Double.parseDouble(values[col_blink].replace(',', '.'))/5.0)+(1-alpha)*prev_blink;
                                    au_frame.setAUAPboth(43, blink);
                                }
                                else {
                                    double blink = 1.0-(blink_process-span_blink)/span_blink;//alpha*(Double.parseDouble(values[col_blink].replace(',', '.'))/5.0)+(1-alpha)*prev_blink;
                                    au_frame.setAUAPboth(43, blink);
                                }
                            }
                            else{
                                last_blink = time;
                                blink_process = 0;
                            }
                        }
                        au_frames.add(au_frame);

                        BAPFrame hmFrame = new BAPFrame();
                        hmFrame.setFrameNumber((int) (time * Constants.FRAME_PER_SECOND) + 1);

                        double rot_X_rad = 0.0;//-1.0*Double.parseDouble(values[1]);
                        double rot_X_deg =  alpha*(rot_X_rad)+(1-alpha)*prev_rot_X;
                        
                        double rot_Y_rad = -1.0*Double.parseDouble(values[2]);
                        double rot_Y_deg = alpha*(rot_Y_rad)+(1-alpha)*prev_rot_Y;
                        
                        double rot_Z_rad = -1.0*Double.parseDouble(values[3]);
                        double rot_Z_deg = alpha*(rot_Z_rad)+(1-alpha)*prev_rot_Z;
                       
                        hmFrame.setDegreeValue(BAPType.vc3_roll, rot_X_deg);
                        hmFrame.setDegreeValue(BAPType.vc3_torsion, rot_Y_deg);
                        hmFrame.setDegreeValue(BAPType.vc3_tilt, rot_Z_deg);      

                        //System.out.println("BAP["+time+"] : ["+rot_X_deg+"; "+rot_Y_deg+"; "+rot_Z_deg+"]");

                        prev_rot_X = rot_X_deg;
                        prev_rot_Y = rot_Y_deg;
                        prev_rot_Z = rot_Z_deg;

                        //gaze
                        double angle_limite = 30;
                        double rot2gaze_x = 0; 
                        double rot2gaze_y = 0.0;
                        if(Math.abs(prev_rot_Y)>angle_limite){
                            rot2gaze_x = prev_rot_X/Math.abs(prev_rot_Y);
                        }
                        else{
                            rot2gaze_x = prev_rot_Y/angle_limite;
                        }
                        
                        if(Math.abs(prev_rot_Z)>angle_limite){
                            rot2gaze_y = prev_rot_Z/Math.abs(prev_rot_Z);
                        }
                        else{
                            rot2gaze_y = prev_rot_Z/angle_limite;
                        }
                        
                        double gaze_x = rot2gaze_x;//alpha*(rot2gaze_x + 0.05*Math.random())+(1-alpha)*prev_gaze_x;
                        double gaze_y = rot2gaze_y;//alpha*(rot2gaze_y + 0.05*Math.random())+(1-alpha)*prev_gaze_y;                        
                      
                        if(gaze_x>0){
                            
                            au_frame.setAUAPboth(62, Math.abs(gaze_x));
                            au_frame.setAUAPboth(61, 0);
                        }
                        else{
                            au_frame.setAUAPboth(62, 0);
                            au_frame.setAUAPboth(61, Math.abs(gaze_x));
                        }

                        if(gaze_y>0){
                            au_frame.setAUAPboth(64, Math.abs(gaze_y));
                            au_frame.setAUAPboth(63, 0);
                        }
                        else{
                            au_frame.setAUAPboth(63, Math.abs(gaze_y));
                            au_frame.setAUAPboth(64, 0);
                        }
                        
                        bap_frames.add(hmFrame);
                    }
                }                 
            }
        }  catch (IOException e) {
            Logs.error(e.getLocalizedMessage());
            LOGGER.log( Level.SEVERE, e.toString(), e );
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    Logs.error(e.getLocalizedMessage());
                    LOGGER.log( Level.SEVERE, e.toString(), e );
                }
            }
        }
                             
        int length = (int) ((int ) max_time-min_time);
        
        send(length);
        
        //ID id = IDProvider.createID("From_AU_Parser");
        
        return length;
    }

    
    public boolean isPerforming(){
        return isPerforming;
    }
    
    public void prepareForRecord(){
        isPerforming=true;
    }

    public boolean isNumeric(String s) {  
        return s.matches("[-+ ]?\\d*\\.?\\d+");  
    }      

    @Override
    public void addAUPerformer(AUPerformer aup) {
        if (aup != null) {
            au_perfomers.add(aup);
        }
    }
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   @Override
    public void removeAUPerformer(AUPerformer aup) {
        if (aup != null) {
            au_perfomers.remove(aup);
        }
    }

    private void send(int length) {        
        ID id = IDProvider.createID("From_AU_Parser");
         
        int timer = (int) (Timer.getTime() * Constants.FRAME_PER_SECOND);
        Logs.debug("Send");
        
        for (AUAPFrame frame : au_frames) {
            int time_frame = timer + frame.getFrameNumber();
            //System.out.println("AUAPFrame : "+time_frame);
            frame.setFrameNumber(  time_frame);
            for (AUPerformer performer : au_perfomers) {
                performer.performAUAPFrame(frame, id);
            }
        }
        //performBAPFrames(bap_frames, id);
        ArrayList<BAPFrame> curr_bap_frames = new ArrayList<BAPFrame>();
        for (BAPFrame frame : bap_frames) {
            int time_frame = timer + frame.getFrameNumber();
            frame.setFrameNumber(time_frame);
            curr_bap_frames.add(frame);
            
        }
        
        int p =0;
        for (BAPFramesPerformer performer : bap_perfomers) {
            performer.performBAPFrames(curr_bap_frames, id);
        }
        bapFramesEmitterImpl.sendBAPFrames(id, curr_bap_frames);
        
        Logs.debug("--Post BAP");
      
        isPerforming = true;
    }

    
    @Override
    public void addBAPFramesPerformer(BAPFramesPerformer bapfp) {
        //System.out.println("addBAPFramesPerformer");
        if (bapfp != null) {
            bap_perfomers.add(bapfp);
            bapFramesEmitterImpl.addBAPFramesPerformer(bapfp);
        }
    }

    @Override
    public void removeBAPFramesPerformer(BAPFramesPerformer bapfp) {
        if (bapfp != null) {
            bap_perfomers.remove(bapfp);
            bapFramesEmitterImpl.removeBAPFramesPerformer(bapfp);
        }
    }

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
