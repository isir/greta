/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package greta.application.ariavaluspa.tools;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainerFormat;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.LoginException;
import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;
import greta.auxiliary.player.ogre.capture.Capturecontroller;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.interruptions.reactions.InterruptionReactionEmitter;
import greta.core.interruptions.reactions.InterruptionReactionPerformer;
import greta.core.util.Mode;
import greta.core.util.enums.interruptions.ReactionDuration;
import greta.core.util.enums.interruptions.ReactionType;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import greta.tools.ogre.capture.video.XuggleVideoCapture;
import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.interruptions.reactions.BehaviorType;
import greta.core.interruptions.reactions.InterruptionReaction;
import greta.core.interruptions.reactions.InterruptionReactionImpl;
import greta.core.interruptions.reactions.InterruptionReactionParameters;
import greta.core.util.CharacterManager;
import java.util.concurrent.*;

/**
 *
 * @author Angelo Cafaro
 */
public class ARIAVideoRequestsManager implements Listener, IntentionEmitter, InterruptionReactionEmitter, CallbackPerformer {

    // LOCAL PATHS
    private final String LOCAL_BASE_FML_FILENAME;
    private final String LOCAL_INTERRUPTION_REACTION_FML_FILENAME;
    private final String LOCAL_BASE_VIDEO_OUTPUT_DIR;
        
    // FTP
    private final String REMOTE_FTP_SERVER;
    private final String REMOTE_BASE_FOLDER_PATH;
    private final String REMOTE_FTP_USERNAME;
    private final String REMOTE_FTP_PASSWORD;
    
    // Thresholds
    private final int WAIT_TIME_LOOPS_SHORT_MS = 500;
    private final int WAIT_TIME_LOOPS_LONG_MS = 5000;
    private final int WAIT_TIME_PRE_PLAY_FML_MS = 1000;
    private final int WAIT_TIME_POST_PLAY_FML_MS = 2000;
    private final int WAIT_TIME_POST_STOP_RECORDING_MS = 1000;
    private final int WAIT_TIME_REACTION_TO_INTERRUPTION_DEFAULT_MS = 2800;
    
    // Connection and STOMP
    private final String STOMP_HOST;
    private final int STOMP_PORT;
    private final String STOMP_QUEUE_NAME;
    private Client stompClient;
    
    // Video
    private Capturecontroller videoCaptureController;
    private XuggleVideoCapture xuggleVideoCapture;
    private IContainerFormat containerCodec;
    private ICodec videoCodec;
    private ICodec audioCodec;
    
    // FML Files and Intents for video generation
    private List<Intention> baseFMLIntentions;
    private List<Intention> interruptionReactionFMLIntentions;
    private Mode modeBaseFML;
    private Mode modeInterruptionReactionFML;
    private String idBaseFML;
    
    // Performers
    private ArrayList<IntentionPerformer> intentionPerformers = new ArrayList<>();
    private ArrayList<InterruptionReactionPerformer> interruptionReactionPerformers = new ArrayList<>();
    
    // Multithreading
    private final Object handleNewRequestLock = new Object();
    private final Object fmlStoppedLock = new Object();
    private final Object videoReadyToSendLock = new Object();
    private final Object videoFTPCompletedLock = new Object();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    
    // DEFAULT VALUES
    private final static String DEFAULT_LOCAL_BASE_FML_FILENAME = "C:\\Projects\\VIB\\bin\\Examples\\ARIA-VALUSPA\\FML\\Interruptions\\FML-IGA.xml";
    private final static String DEFAULT_LOCAL_INTERRUPTION_REACTION_FML_FILENAME = "C:\\Projects\\VIB\\bin\\Examples\\ARIA-VALUSPA\\FML\\Interruptions\\FML-Halt-IGA.xml";
    private final static String DEFAULT_LOCAL_BASE_VIDEO_OUTPUT_DIR = "D:\\Crowdsourcing";
    private final static String DEFAULT_REMOTE_FTP_SERVER = "waws-prod-am2-123.ftp.azurewebsites.windows.net";
    private final static String DEFAULT_REMOTE_BASE_FOLDER_PATH = "/site/wwwroot/videos/";
    private final static String DEFAULT_REMOTE_FTP_USERNAME = "crowdsourcingexperiment\\$crowdsourcingexperiment";
    private final static String DEFAULT_REMOTE_FTP_PASSWORD = "avZMoTtQ2plsP9XvP511hdWpPz5FLGbrf2uulQQjmc78cpinz0wy1lCn21fc";
    private static final String DEFAULT_STOMP_HOST = "localhost";
    private static final int DEFAULT_STOMP_PORT = 61616;
    private final static String DEFAULT_STOMP_QUEUE_NAME = "/queue/aria.videorequests.manager";
    public CharacterManager c;
    
    public ARIAVideoRequestsManager(CharacterManager cm) {
        this(DEFAULT_LOCAL_BASE_FML_FILENAME,
             DEFAULT_LOCAL_INTERRUPTION_REACTION_FML_FILENAME,
             DEFAULT_LOCAL_BASE_VIDEO_OUTPUT_DIR,
             DEFAULT_REMOTE_FTP_SERVER, 
             DEFAULT_REMOTE_BASE_FOLDER_PATH, 
             DEFAULT_REMOTE_FTP_USERNAME,
             DEFAULT_REMOTE_FTP_PASSWORD,
             DEFAULT_STOMP_HOST,
             DEFAULT_STOMP_PORT,
             DEFAULT_STOMP_QUEUE_NAME);
        this.c=cm;
    }
    
    public ARIAVideoRequestsManager(
            String localBaseFMLFilename, 
            String localInterruptionReactionFilename, 
            String localBaseVideoOutputDir,
            String remoteFTPServer, 
            String remoteBaseFolderPath, 
            String ftpUsername, 
            String ftpPassword,
            String stompHost,
            int stompPort,
            String stompQueueName)
    {
        
        LOCAL_BASE_FML_FILENAME = localBaseFMLFilename;
        LOCAL_INTERRUPTION_REACTION_FML_FILENAME = localInterruptionReactionFilename;
        LOCAL_BASE_VIDEO_OUTPUT_DIR = localBaseVideoOutputDir;
        REMOTE_FTP_SERVER = remoteFTPServer;
        REMOTE_BASE_FOLDER_PATH = remoteBaseFolderPath;
        REMOTE_FTP_USERNAME = ftpUsername;
        REMOTE_FTP_PASSWORD = ftpPassword;
        STOMP_HOST = stompHost;
        STOMP_PORT = stompPort;
        STOMP_QUEUE_NAME = stompQueueName;
        
        try {
            
            // Init the STOMP client and connect to the Broker
            stompClient = new Client(STOMP_HOST, STOMP_PORT, null, null);
        }
        catch (IOException ex) 
        {
            Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + " [" + ex + "]");
            //System.out.println("\n\n" + ARIAVideoRequestsManager.class.getSimpleName() + " [" + ex + "]");
        } 
        catch (LoginException ex) 
        {
            Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + " [" + ex + "]");
            //System.out.println("\n\n" + ARIAVideoRequestsManager.class.getSimpleName() + " [" + ex + "]");
        }
        
        // Subscribe the STOMP client to the message queue for receiving messages
        if (stompClient != null) 
        {
            Logs.info(ARIAVideoRequestsManager.class.getSimpleName() + ": connection to ARIA Stomp Broker OK");
            stompClient.subscribe(STOMP_QUEUE_NAME, this);
            Logs.info(ARIAVideoRequestsManager.class.getSimpleName() + ": message receiver subscribed to queue[" + STOMP_QUEUE_NAME + "]");
        }
        
        // Init the Base FML file (i.e. FML file to play and that gets interrupted)
                
        // Get the Intentions of the FML Base file
        XMLParser fmlParser = XML.createParser();
        fmlParser.setValidating(true);
        XMLTree baseFML = fmlParser.parseFile(LOCAL_BASE_FML_FILENAME);
        baseFMLIntentions = FMLTranslator.FMLToIntentions(baseFML,this.c);

        // Set the mode for the Base FML
        modeBaseFML = FMLTranslator.getDefaultFMLMode();
        if (baseFML.hasAttribute("composition")) {
            modeBaseFML.setCompositionType(baseFML.getAttribute("composition"));
        }
        if (baseFML.hasAttribute("reaction_type")) {
            modeBaseFML.setReactionType(baseFML.getAttribute("reaction_type"));
        }
        if (baseFML.hasAttribute("reaction_duration")) {
            modeBaseFML.setReactionDuration(baseFML.getAttribute("reaction_duration"));
        }
        if (baseFML.hasAttribute("social_attitude")) {
            modeBaseFML.setSocialAttitude(baseFML.getAttribute("social_attitude"));
        }

        // Creates the ID for the Base FML
        idBaseFML = (new File(LOCAL_BASE_FML_FILENAME)).getName().replaceAll("\\.xml$", "");
        
        // Init the Interruption Reaction FML file (i.e. FML file to use as reaction to interruption)
        XMLTree interruptionReactionFML = fmlParser.parseFile(LOCAL_INTERRUPTION_REACTION_FML_FILENAME);
        
        // Get the Intentions
        interruptionReactionFMLIntentions = FMLTranslator.FMLToIntentions(interruptionReactionFML,this.c);
        
        // Set the mode for the Interruption Reaction FML
        modeInterruptionReactionFML = FMLTranslator.getDefaultFMLMode();
        modeInterruptionReactionFML.setCompositionType("replace");
        modeInterruptionReactionFML.setReactionType(ReactionType.HALT);
        modeInterruptionReactionFML.setReactionDuration(ReactionDuration.SHORT);
        modeInterruptionReactionFML.setSocialAttitude("neutral");
        
        // Init Video Parameters
        containerCodec = IContainerFormat.getInstalledOutputFormat(0);
        for (IContainerFormat f : IContainerFormat.getInstalledOutputFormats()) {
           if (f.getOutputFormatShortName().equalsIgnoreCase("mp4")) {
               containerCodec = f;
           }
        }
        videoCodec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_MPEG4);
        audioCodec = ICodec.findEncodingCodec(ICodec.ID.CODEC_ID_MP3);
        
        // Init the Output Directory
        File videoOutputBaseDir = new File(LOCAL_BASE_VIDEO_OUTPUT_DIR);

        // if the directory does not exist, create it
        if (!videoOutputBaseDir.exists()) {

            try{
                videoOutputBaseDir.mkdir();
            } 
            catch(SecurityException se)
            {
                Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + "[" + se + "]");
            }        
        }
        else {
            for (File childFile : videoOutputBaseDir.listFiles()) {
                try {
                    this.delete(childFile);
                } catch (IOException ex) {
                   Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + "[" + ex + "]");
                }
            }
        }
       
    }
        
    private void initVideoCaptureController() {
        xuggleVideoCapture.setWantedFormat(containerCodec, videoCodec, audioCodec);
    }
    
    /**
     * Delete a file or a directory and its children.
     *
     * @param file The directory to delete.
     * @throws IOException Exception when problem occurs during deleting the
     * directory.
     */
    private void delete(File file) throws IOException {

        for (File childFile : file.listFiles()) {

            if (childFile.isDirectory()) {
                delete(childFile);
            } else {
                if (!childFile.delete()) {
                    throw new IOException();
                }
            }
        }

        if (!file.delete()) {
            throw new IOException();
        }
    }
        
    /**
     * This method is called when a new text is received on the ActiveMQ queue
     *
     * @param content the content of the new message
     * @param map a set of properties coming from activeMQ
     */
    public void message(Map map, String content) {
        
        if ((content == null) || (content.trim().isEmpty()))
        {
            Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".message(): Received an invalid request [empty or without content]");
            return;
        }
        
        if (!map.containsKey("userID") || map.get("userID").toString().trim().isEmpty()) {
            Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".message(): Received an invalid request [empty or without userID]");
            return;
        }
        
        if (!map.containsKey("requestID") || map.get("requestID").toString().trim().isEmpty()) {
            Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".message(): Received an invalid request [empty or without requestID]");
            return;
        }
                
        //System.out.println("Entering monitor: RequestID [" + map.get("requestID") + "], UserID [" + map.get("userID") + "]");
        
        // Set Lock as busy
        try {
            synchronized (handleNewRequestLock) {
                
                //System.out.println("Inside monitor: RequestID [" + map.get("requestID") + "], UserID [" + map.get("userID") + "]");
                
                // Prepare the output directory
                String videoOutputDirPath = LOCAL_BASE_VIDEO_OUTPUT_DIR + "\\" + map.get("userID");
                File videoOutputDir = new File(videoOutputDirPath);

                // if the directory does not exist, create it
                if (!videoOutputDir.exists()) {

                    try{
                        videoOutputDir.mkdir();
                    } 
                    catch(SecurityException se)
                    {
                        Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + " [" + se + "]");
                    }        
                }
                                
                // Set the filename for the video to be generated
                videoCaptureController.setBaseFileName(videoOutputDirPath + "\\" + content);
                
                // Start New Video Creation Thread
                videoCaptureController.startVideoCapture();
                Logs.info(ARIAVideoRequestsManager.class.getSimpleName() + ".message(): Video capture started [" + content + "], RequestID [" + map.get("requestID").toString() + "], UserID [" + map.get("userID").toString() + "]");
                
                // Wait before sending the FML Intentions
                greta.core.util.time.Timer.sleep(WAIT_TIME_PRE_PLAY_FML_MS);
                
                // Send Base FML Intentions to all Intention Performers
                String reactionParams = content.replaceAll("V_", "");
                int reactionTimeMS = WAIT_TIME_REACTION_TO_INTERRUPTION_DEFAULT_MS;
                if (map.containsKey("reactionTimeMS")) 
                {
                    reactionTimeMS = Integer.parseInt(map.get("reactionTimeMS").toString());
                }
                
                this.sendFMLIntentions(reactionParams);
                
                // Schedule the Interruption Reaction        
                scheduler.schedule(new InterruptionReactionRunnable(reactionTimeMS), 0, TimeUnit.MILLISECONDS);
                
                // Schedule the End of Video Task       
                scheduler.schedule(new EndRecordingVideoRunnable(map.get("userID").toString(), map.get("requestID").toString(), content), 0, TimeUnit.MILLISECONDS);
                
                // Schedule the FTP Video Sender
                scheduler.schedule(new VideoSenderRunnable(map.get("userID").toString(), map.get("requestID").toString(), content), 0, TimeUnit.MILLISECONDS);
                
                // Wait to be notified when the video has been created
                //System.out.println("Before wait monitor: RequestID [" + map.get("requestID") + "], UserID [" + map.get("userID") + "]");
                handleNewRequestLock.wait(WAIT_TIME_LOOPS_LONG_MS);
                
                //System.out.println("After wait monitor: RequestID [" + map.get("requestID") + "], UserID [" + map.get("userID") + "]");
            }
        } 
        catch (Throwable ex) 
        {
            Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".message() [" + ex + "]");
        }
    }
    
    private void sendFMLIntentions(String reactionParams) {
        
        // Send Base FML Intentions
        for (IntentionPerformer performer : intentionPerformers) {
            performer.performIntentions(baseFMLIntentions, IDProvider.createID(idBaseFML), modeBaseFML);
        }
        
        // Prepare the reaction
        List<InterruptionReaction> interruptionReactions = new ArrayList<>();
        
        String[] reactionParameters = reactionParams.split("_");
        
        interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.HEAD_TILT, new InterruptionReactionParameters(Float.parseFloat(reactionParameters[0]), Float.parseFloat(reactionParameters[1]))));
        interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.HEAD_NOD_TOSS, new InterruptionReactionParameters(Float.parseFloat(reactionParameters[2]), Float.parseFloat(reactionParameters[3]))));
        interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.EYES_LIDS_CLOSE, new InterruptionReactionParameters(Float.parseFloat(reactionParameters[4]), Float.parseFloat(reactionParameters[5]))));
        interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.EYES_BROWS, new InterruptionReactionParameters(Float.parseFloat(reactionParameters[6]), Float.parseFloat(reactionParameters[7]))));
        interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.EYES_SQUEEZE, new InterruptionReactionParameters(Float.parseFloat(reactionParameters[8]), Float.parseFloat(reactionParameters[8]))));
        interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.SMILE, new InterruptionReactionParameters(Float.parseFloat(reactionParameters[10]), Float.parseFloat(reactionParameters[11]))));
        interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.SHOULDERS_UP_FORWARD, new InterruptionReactionParameters(Float.parseFloat(reactionParameters[12]), Float.parseFloat(reactionParameters[13]))));
        
        int gestureHoldRetract = Integer.parseInt(reactionParameters[14]);
        float gestureHoldRetractDuration = Float.parseFloat(reactionParameters[15]);
        if (gestureHoldRetract < 0) {
            interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.GESTURE_HOLD, new InterruptionReactionParameters(0.0f, gestureHoldRetractDuration)));
            interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.GESTURE_RETRACT, new InterruptionReactionParameters(0.0f, 0.0f)));
        }
        else if (gestureHoldRetract > 0) {
            interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.GESTURE_RETRACT, new InterruptionReactionParameters(0.0f, gestureHoldRetractDuration)));
            interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.GESTURE_HOLD, new InterruptionReactionParameters(0.0f, 0.0f)));
        }
        else {
            interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.GESTURE_RETRACT, new InterruptionReactionParameters(0.0f, 0.0f)));
            interruptionReactions.add(new InterruptionReactionImpl(BehaviorType.GESTURE_HOLD, new InterruptionReactionParameters(0.0f, 0.0f)));
        }
        
        // Send Interruption Reaction Parameters to all Interruption Reaction Performers
        for (InterruptionReactionPerformer performer : interruptionReactionPerformers) {
            performer.performInterruptionReactions(interruptionReactions, IDProvider.createID("ARIAVideoRequestsManager-InterruptionReactionParams"));
        }
        
    }
    
    private class InterruptionReactionRunnable implements Runnable {
        
        private int waitTimeMS;
        
        public InterruptionReactionRunnable(int _waitTimeMS) {
          this.waitTimeMS = _waitTimeMS;
        }

        public void run() {
            // Wait and then send interruption reaction
            greta.core.util.time.Timer.sleep(waitTimeMS);

            // Send Interruption Reaction FML Intentions to all Intention Performers
            for (IntentionPerformer performer : intentionPerformers) {
                performer.performIntentions(interruptionReactionFMLIntentions, IDProvider.createID("ARIAVideoRequestsManager-Reaction"), modeInterruptionReactionFML);
            }
        }
    }
    
    private class EndRecordingVideoRunnable implements Runnable {
        
        private String userID;
        private String requestID;
        private String videoName;
        
        public EndRecordingVideoRunnable(String userID, String requestID , String videoName) {
            
            this.userID = userID;
            this.requestID = requestID;
            this.videoName = videoName;
            
        }

        public void run() {
            
            try 
            {
                synchronized (fmlStoppedLock) {
                    fmlStoppedLock.wait();
                }
            } catch (InterruptedException ex) {
                Logs.warning(ARIAVideoRequestsManager.class.getSimpleName() + "EndRecordingVideoRunnable.run() [" + ex + "]");
            }
            
            // Wait before stopping the Recording
            greta.core.util.time.Timer.sleep(WAIT_TIME_POST_PLAY_FML_MS);
            
            videoCaptureController.stopVideoCapture();
            Logs.info(ARIAVideoRequestsManager.class.getSimpleName() + ".EndRecordingVideoRunnable.run(): Video capture finished [" + videoName + "], RequestID [" + requestID + "], UserID [" + userID + "]");
            
            greta.core.util.time.Timer.sleep(WAIT_TIME_POST_STOP_RECORDING_MS);
            
            synchronized (videoReadyToSendLock) {
                
                try{
                    PrintWriter writer = new PrintWriter(LOCAL_BASE_VIDEO_OUTPUT_DIR + "\\" + this.userID + "\\" + requestID + ".tmp");
                    writer.close();
                } catch (IOException e) {
                   
                }
                videoReadyToSendLock.notifyAll();
            }
            
            synchronized (handleNewRequestLock) {
                handleNewRequestLock.notifyAll();
            }
        }
    }
    
    private class VideoSenderRunnable implements Runnable {
        
        private String userID;
        private String requestID;
        private String videoName;
        
        public VideoSenderRunnable(String userID, String requestID , String videoName) {
            this.userID = userID;
            this.requestID = requestID;
            this.videoName = videoName;
            
        }

        public void run() {
            
            while (!isVideoReady())
            {
                try {
                    synchronized (videoReadyToSendLock) {
                        videoReadyToSendLock.wait(WAIT_TIME_LOOPS_SHORT_MS);
                    }
                } catch (InterruptedException ex) {
                    Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".VideoSenderRunnable.run() [" + ex + "]");
                }
            }
            
            Logs.info(ARIAVideoRequestsManager.class.getSimpleName() + ".VideoSenderRunnable.run(): Video Ready to be sent [" + videoName + "], RequestID [" + requestID + "], UserID [" + userID + "]");

            try {
                // Sends over FTP
                this.sendFTP();
            } catch (Throwable msg) {
                Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".VideoSenderRunnable.run() [" + msg + "]");
            }
        }
        
        private boolean isVideoReady() {
            File f = new File(LOCAL_BASE_VIDEO_OUTPUT_DIR + "\\" + this.userID + "\\" + requestID + ".tmp");
            if (f.exists()) {
                return true;
            }
            else {
                return false;
            }
        }
        
        private boolean isFTPTransferComplete() {
            File f = new File(LOCAL_BASE_VIDEO_OUTPUT_DIR + "\\" + this.userID + "\\" + requestID + ".tmp");
            if (f.exists()) {
                return false;
            }
            else {
                return true;
            }
        }
        
        private void sendFTP() {
            
                        
            try {
            
                // Create FTP client
                FTPClient client = new FTPClient();
                
                // Set parameters for FTP client and connect
                client.setType(FTPClient.TYPE_AUTO);
                client.connect(REMOTE_FTP_SERVER);
                client.login(REMOTE_FTP_USERNAME,REMOTE_FTP_PASSWORD);
                          
                // Chage to the remote directory for videos
                client.changeDirectory(REMOTE_BASE_FOLDER_PATH);
                
                String[] listFileNames = client.listNames();
                boolean directoryExists = false;
                for (String fileName : listFileNames) {
                    if (fileName.equalsIgnoreCase(this.userID)) {
                        directoryExists = true;
                        break;
                    }
                }
                
                // Create the directory for the file to upload (only if it does not exist)
                if (!directoryExists) {
                    client.createDirectory(this.userID);
                }
                
                // Chage to the newly created directory
                client.changeDirectory(REMOTE_BASE_FOLDER_PATH + this.userID);
                
                // Create a remote temp file to signal trasfer begin
                client.upload(new java.io.File(LOCAL_BASE_VIDEO_OUTPUT_DIR + "\\" + userID + "\\" + requestID + ".tmp"));
                
                // Upload File
                client.upload(new java.io.File(LOCAL_BASE_VIDEO_OUTPUT_DIR + "\\" + this.userID + "\\" + videoName + ".mp4"), new MyTransferListener());
                
                while (!isFTPTransferComplete())
                {
                    try {
                        synchronized (videoFTPCompletedLock) {
                            videoFTPCompletedLock.wait(WAIT_TIME_LOOPS_SHORT_MS);
                        }
                    } catch (InterruptedException ex) {
                        Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".VideoSenderRunnable.sendFTP() [" + ex + "]");
                    }
                }
                
                // Rename the remote temp file for signalling transfer end
                client.rename(requestID + ".tmp", requestID + ".finished");
                
                // Disconnect the FTP client
                client.disconnect(false);
                
                // Delete the local video file
                File videoFile = new File(LOCAL_BASE_VIDEO_OUTPUT_DIR + "\\" + userID + "\\" + videoName + ".mp4");
                try {
                    Files.deleteIfExists(videoFile.toPath());
                } catch (IOException ex) {
                    Logs.warning(ARIAVideoRequestsManager.class.getSimpleName() + ".VideoSenderRunnable.sendFTP() [" + ex + "]");
                }
                                
            } catch (Throwable msg) {
                Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".VideoSenderRunnable.sendFTP() [" + msg + "]");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                msg.printStackTrace(pw);
                String sStackTrace = sw.toString(); // stack trace as a string
                Logs.error("Details [" + sStackTrace + "]");
            } 
        }
        
        private class MyTransferListener implements FTPDataTransferListener {

            public void started() {
                
                Logs.info(ARIAVideoRequestsManager.class.getSimpleName() + ".MyTransferListener.started() FTP Transfer started for Video [" + videoName + "], RequestID [" + requestID + "], UserID [" + userID + "]");
            }

            public void transferred(int length) {
                   
            }

            public void completed() {
                
                // Delete the local temporary file (for checking if the video is ready)
                File tempFile = new File(LOCAL_BASE_VIDEO_OUTPUT_DIR + "\\" + userID + "\\" + requestID + ".tmp");
                try {
                    Files.deleteIfExists(tempFile.toPath());
                } catch (IOException ex) {
                    Logs.warning(ARIAVideoRequestsManager.class.getSimpleName() + ".MyTransferListener.completed() [" + ex + "]");
                }
                
                Logs.info(ARIAVideoRequestsManager.class.getSimpleName() + ".MyTransferListener.completed() FTP Transfer completed for Video [" + videoName + "], RequestID [" + requestID + "], UserID [" + userID + "]");
                
                synchronized (videoFTPCompletedLock) {
                    videoFTPCompletedLock.notifyAll();
                }
            }

            public void aborted() {
                
                Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".MyTransferListener.aborted() FTP Transfer aborted for Video [" + videoName + "], RequestID [" + requestID + "], UserID [" + userID + "]");
                
                synchronized (videoFTPCompletedLock) {
                    videoFTPCompletedLock.notifyAll();
                }
            }

            public void failed() {
                
                Logs.error(ARIAVideoRequestsManager.class.getSimpleName() + ".MyTransferListener.failed() FTP Transfer failed for Video [" + videoName + "], RequestID [" + requestID + "], UserID [" + userID + "]");

                synchronized (videoFTPCompletedLock) {
                    videoFTPCompletedLock.notifyAll();
                }
            }
            
        }
    }
    
    /* ---------------------------------------------------------------------- */
    /*                           CallbackPerformer                            */
    /* ---------------------------------------------------------------------- */

    public void performCallback(Callback clbck) {

        //Logs.info("ARIA Video Request Manager received callback ID [" + clbck.animId() + "] of Type [" + clbck.type() + "] at Time [" + String.format("%.2f", clbck.time()) + "].");
        
        if (clbck.type().equalsIgnoreCase("stopped")) {
            
            synchronized (fmlStoppedLock) {
                    fmlStoppedLock.notifyAll();
            }
            
        }
    }

    /* ---------------------------------------------------------------------- */
    /*                            IntentionEmitter                            */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        intentionPerformers.add(performer);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        intentionPerformers.remove(performer);
    }
    
    /* ---------------------------------------------------------------------- */
    /*                      Interruption Reaction Emitter                     */
    /* ---------------------------------------------------------------------- */

    public void addInterruptionReactionPerformer(InterruptionReactionPerformer performer) {
        interruptionReactionPerformers.add(performer);
    }

    public void removeInterruptionReactionPerformer(InterruptionReactionPerformer performer) {
        interruptionReactionPerformers.remove(performer);
    }
    
    /* ---------------------------------------------------------------------- */
    /*                      Video Capture Controller and Xuggle               */
    /* ---------------------------------------------------------------------- */
    public void setVideoCaptureController(Capturecontroller  vcc){
        this.videoCaptureController = vcc;
    }
    
    public void setXuggleVideoCapture(XuggleVideoCapture  xvc){
        this.xuggleVideoCapture = xvc;
        if (xuggleVideoCapture != null) {
            this.initVideoCaptureController();
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        scheduler.shutdown();
        super.finalize();
    }
}