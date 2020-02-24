/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.openface;

import greta.core.util.StringArrayListener;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUEmitterImpl;
import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

/**
 * This class is an implementation of {@code SignalEmitter} interface.<br/> When
 * calling the {@code load} function, It sends the {@code Signals} contained in
 * a specified BML file to all {@code SignalPerformers} added with the
 * {@code addSignalPerformer} function.
 *
 * @author Thomas Janssoone and soumia dermouche
 */
public class AUStreamReader extends FAPFrameEmitterImpl implements AUEmitter, BAPFrameEmitter, Runnable {
    private static final Logger LOGGER = Logger.getLogger(AUStreamReader.class.getName() );
    private List<StringArrayListener> headerListeners = new ArrayList<>();
    private Thread t;
    private final String threadName = "AUStreamReader";
    private int startInputFrame = 0;
    private final int offsetFrame = 0;
    
    private AUEmitterImpl auEmitterImpl = new AUEmitterImpl();
    private BAPFrameEmitterImpl bapFramesEmitterImpl = new BAPFrameEmitterImpl();
    
    private boolean isPerforming = false;
    private boolean isAlive = true;
    //Phil
    ID id;
    String url;
    boolean isConnected;
    final static String DEFAULTURL = "tcp://localhost:5000";
    final static String TOPIC = "";
    String[] selectedHeaders = null;
    ZContext zContext;
    Socket zSubscriber; 
    String lastDataStr=null;
    int col_blink = 412;
    double fps = 0.;
    double frameDuration = 0.;
    OpenFaceFrame curFrame = new OpenFaceFrame();
    OpenFaceFrame prevFrame = new OpenFaceFrame();
    
    // loop variables
    double prev_rot_X = 0.0;
    double prev_rot_Y = 0.0;
    double prev_rot_Z = 0.0;
  
    double min_time = Double.MAX_VALUE;
    double max_time = 0.0;

    double prev_gaze_x = 0.0;
    double prev_gaze_y = 0.0;

    double prev_blink = 0.0;

    double alpha = 0.75;//1.0; 
    
    public AUStreamReader(){        
    }
    
    public String[] getHeaders(){
        return OpenFaceFrame.headers;
    }
    
    public void listen(String url) {     
        if (zContext == null)
            zContext = new ZContext();
        try {
            if(zSubscriber!=null){
                LOGGER.info("Closing previous socket");
                zSubscriber.close();
            }
            zSubscriber = zContext.createSocket(SocketType.SUB);
            if (url.length() == 0)
                url = DEFAULTURL;
            
            isConnected = zSubscriber.connect(url);
            
            if(isConnected){
                zSubscriber.subscribe(TOPIC.getBytes(ZMQ.CHARSET));
                LOGGER.info(String.format("Connected to: %s",url));
                start();
            }else
                LOGGER.warning(String.format("Failed to open: %s",url));
            
        }
        catch(ZMQException ex){
            LOGGER.warning(String.format("Couldn't connect to: %s\n%s", url, ex.getMessage()));
            isConnected = false;
        }
    }
    
    @Override
    public void finalize() throws Throwable{
        super.finalize();
        if(zSubscriber!=null){
            zSubscriber.disconnect(url);
            zSubscriber.close();
        }
        if(zContext!=null)
            zContext.close();
        isConnected = false;
        isPerforming = false;
        isAlive = false;
    }
    
     /**
     * Set selected headers 
     *
     * @param selected headers to use
     * 
     */
    public void setSelected(String[] selected){
        if(selected!=null){
            if(selected!=null && !selected.equals(selectedHeaders)){
                selectedHeaders = selected;
                OpenFaceFrame.setSelectedHeaders(selectedHeaders);
            }
            LOGGER.info(String.format("Setting selected headers to: %s",Arrays.toString(selected)));
        }
        else
            LOGGER.warning("No header selected");
    }
        
    public void start () {
      LOGGER.fine(String.format("Starting %s..", threadName ));
      if (t == null) {
         t = new Thread (this, threadName);
         isAlive = true;
         t.start();
      }
    }    
    
    @Override
    public void run() {        
        LOGGER.info(String.format("Running %s", threadName ));
        try {                        
            LOGGER.fine(String.format("Thread: %s", threadName));
            do{
                do{
                    processLine(); 
                    Thread.sleep(30);
                }while(isConnected);
                Thread.sleep(1000);
            }while(isAlive);
                      
        } catch (InterruptedException e) {
           LOGGER.warning(String.format("Thread: %s interrupted", threadName));
        }
        LOGGER.info(String.format("Thread: %s exiting", threadName));
    }
    
    private void processLine(){
        if(isConnected){
            String line = zSubscriber.recvStr();
            if(line!=null){
                if(line.startsWith("DATA:")){
                    lastDataStr = line.substring(5);
                    processFrame(lastDataStr);
                }
                else if(line.startsWith("HEADER:")){                    
                    boolean changed = OpenFaceFrame.readHeader(line.substring(7));
                    if(changed){
                        LOGGER.info("Header headerChanged");
                        headerChanged(OpenFaceFrame.headers);
                    }
                }
                else
                    LOGGER.warning(String.format("Line not recognized: %s", line));
                    
            }else
                LOGGER.warning(String.format("Line is empty"));
        }
    }
        
    private void processFrame(String line){        
        if(line!=null && isPerforming){            
            int curGretaFrame = (int) (Timer.getTime() * Constants.FRAME_PER_SECOND);
            prevFrame.copy(curFrame);
            
            if(startInputFrame==0)
                startInputFrame = curFrame.frameId;
            
            curFrame.readDataLine(line);
            //curFrame.frameId += offsetFrame-startInputFrame + curGretaFrame;
            curFrame.frameId = offsetFrame + curGretaFrame;
            int frameDiff = curFrame.frameId-prevFrame.frameId;
            if(frameDiff<10 && frameDiff>0){ // If less than 10 frame delay
                frameDuration = curFrame.timestamp - prevFrame.timestamp;            
            
                fps = 1./frameDuration;
                //LOGGER.info(String.format("frameid: %d, fps:%f, f dur:%f",curFrame.frameId, fps, frameDuration));
                processOpenFace();
            }
        }        
    }
    
    //Format based on https://github.com/TadasBaltrusaitis/OpenFace
    //timestamp, gaze_0_x, gaze_0_y, gaze_0_z, gaze_1_x, gaze_1_y, gaze_1_z, gaze_angle_x, gaze_angle_y, pose_Tx, pose_Ty, pose_Tz, pose_Rx, pose_Ry, pose_Rz, AU01_r, AU02_r, AU04_r, AU05_r, AU06_r, AU07_r, AU09_r, AU10_r, AU12_r, AU14_r, AU15_r, AU17_r, AU20_r, AU23_r, AU25_r, AU26_r, AU45_r, AU01_c, AU02_c, AU04_c, AU05_c, AU06_c, AU07_c, AU09_c, AU10_c, AU12_c, AU14_c, AU15_c, AU17_c, AU20_c, AU23_c, AU25_c, AU26_c, AU28_c, AU45_c
    private void processOpenFace() { 
        if (isConnected && isPerforming()) {                
            if (frameDuration != 0) {
                if( frameDuration > max_time){
                    max_time = frameDuration;
                }
                if( frameDuration < min_time){
                    min_time = frameDuration;
                }
                sendAUFrame(makeAUFrame());
                sendBAPFrame(makeBAPFrame());
            }
        }        
    }
    
    private AUAPFrame makeAUFrame(){
        AUAPFrame au_frame = new AUAPFrame();                        
        au_frame.setFrameNumber(curFrame.frameId);

        for(int i=0;i<OpenFaceFrame.getAUCCount();i++)
        { 
            double value = curFrame.au_c[i];
            double prevValue = prevFrame.intensity[i];                            
            double intensity = alpha*value/5. + (1-alpha)*prevValue;            
            au_frame.setAUAPboth(OpenFaceFrame.getAUCIndex(i), intensity);
        }

        //gaze
        double gaze_x = alpha*(0.5*(curFrame.gaze0.x()+curFrame.gaze1.x()))+(1-alpha)*prev_gaze_x;
        double gaze_y = alpha*(0.5*(curFrame.gaze0.y()+curFrame.gaze1.y()))+(1-alpha)*prev_gaze_y;
        
        if(gaze_x<0){
            au_frame.setAUAPboth(62, -gaze_x);
        }
        else{
            au_frame.setAUAPboth(61, gaze_x);
        }

        if(gaze_y<0){
            au_frame.setAUAPboth(64, -gaze_y);
        }
        else{
            au_frame.setAUAPboth(63, gaze_y);
        }
        prev_gaze_x = gaze_x;
        prev_gaze_y = gaze_y;

        //blink
        // double blink = alpha*(Double.parseDouble(values[col_blink].replace(',', '.'))/5.0)+(1-alpha)*prev_blink;
        double blink = curFrame.blink/5.0 ;
        au_frame.setAUAPboth(43, blink);
        prev_blink = blink;
        return au_frame;
    }
    
    private BAPFrame makeBAPFrame(){
        BAPFrame hmFrame = new BAPFrame();
        hmFrame.setFrameNumber(curFrame.frameId);

        double rot_X_deg =  curFrame.headRot.x();
        double rot_Y_deg =  -1.0*curFrame.headRot.y();
        double rot_Z_deg =  -1.0*curFrame.headRot.z();

        rot_X_deg = alpha*(rot_X_deg)+(1-alpha)*prev_rot_X;
        rot_Y_deg = alpha*(rot_Y_deg)+(1-alpha)*prev_rot_Y;
        rot_Z_deg = alpha*(rot_Z_deg)+(1-alpha)*prev_rot_Z;

        hmFrame.setDegreeValue(BAPType.vc3_tilt , rot_X_deg);
        hmFrame.setDegreeValue(BAPType.vc3_torsion, rot_Y_deg);
        hmFrame.setDegreeValue(BAPType.vc3_roll, rot_Z_deg);      

        prev_rot_X = rot_X_deg;
        prev_rot_Y = rot_Y_deg;
        prev_rot_Z = rot_Z_deg; 

        return hmFrame;
    }
    
    private void sendAUFrame(AUAPFrame frame){        
        ID id = IDProvider.createID("AUStreamReader_sendAUFrame");
        auEmitterImpl.performAUAPFrame(frame, id);
    }
    
    private void sendBAPFrame(BAPFrame frame){
        ID id = IDProvider.createID("AUStreamReader_sendBAPFrame");        
        bapFramesEmitterImpl.sendBAPFrame(id, frame);        
    }

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapfp) {        
        if (bapfp != null) {        
            LOGGER.info("addBAPFramesPerformer");
            bapFramesEmitterImpl.addBAPFramePerformer(bapfp);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapfp) {
        if (bapfp != null) {          
            LOGGER.info("removeBAPFramesPerformer");
            bapFramesEmitterImpl.removeBAPFramePerformer(bapfp);
        }
    }
    
    @Override
    public void addAUPerformer(AUPerformer aup) {
        if (aup != null) {
            LOGGER.info("addAUPerformer");
            auEmitterImpl.addAUPerformer(aup);
        }
    }

    @Override
    public void removeAUPerformer(AUPerformer aup) {
        if (aup != null) {
            LOGGER.info("addAUPerformer");
            auEmitterImpl.removeAUPerformer(aup);
        }
    }
    
    public void addHeaderListener(StringArrayListener hl) { 
        if(hl!=null&&!headerListeners.contains(hl))
            headerListeners.add(hl);        
    }
    
    public void removeHeaderListene(StringArrayListener hl) {
        if(hl!=null&&headerListeners.contains(hl))
            headerListeners.remove(hl);
    }
    
    private void headerChanged(String[] headers){        
        headerListeners.forEach((hl) -> {
            hl.stringArrayChanged(headers);
        });
    }

    public boolean isPerforming() {
        return isPerforming;
    }

    public void setIsPerforming(Boolean isPerforming) {
        startInputFrame = 0;
        this.isPerforming = isPerforming;
    }

    public boolean isIsAlive() {
        return isAlive;
    }

    public void setIsAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }
}
