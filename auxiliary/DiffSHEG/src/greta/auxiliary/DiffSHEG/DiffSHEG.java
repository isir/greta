/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.DiffSHEG;

import greta.core.util.CharacterManager;
import greta.core.util.CharacterDependent;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
import greta.core.util.Constants;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 *
 * @author Leroux Paul
 */
public class DiffSHEG extends BAPFrameEmitterImpl implements CharacterDependent {
    private final String base_bvh_path = "Common\\Data\\DiffSHEG\\data\\GRETA\\Base_greta_fingers_bis.bvh";
    private final String python_env_checker_path = "Common\\Data\\DiffSHEG\\check_env.py";
    private final String batch_env_installer_path = "Common\\Data\\DiffSHEG\\init_env.bat";
    private final String batch_main_path = "Common\\Data\\DiffSHEG\\run_DiffSHEG.bat";
    private final String batch_kill_path = "Common\\Data\\DiffSHEG\\kill_server.bat";
    private Process server_process;

    private int frameCounter = 0;
    private int baseFrameTime = 0;
    private boolean isFirstFrame = true;

    private Server gesture_server;
    private String result;

    private BVHProcessor bvhProcessor;

    private CharacterManager cm;

    public DiffSHEG (CharacterManager cm) throws IOException {
        System.out.println("[Greta DiffSHEG] greta.auxiliary.DiffSHEG.DiffSHEG()");
        this.cm = cm;
        this.cm.setHoldFrame(true);

        gesture_server = new Server();
        gesture_server.setAddress("localhost"); 
        gesture_server.setPort("6501"); 
        
        bvhProcessor = new BVHProcessor();
        try (BufferedReader br = new BufferedReader(new FileReader(base_bvh_path))) {
            bvhProcessor.parseBVHHeader(br);
        } catch (IOException e) {
            System.err.println("[Greta DiffSHEG] FATAL: Could not read BVH header file. " + e.getMessage());
            e.printStackTrace();
        }
        ///////////////////////
        // Check environment ; Create environment if it does not exist
        ///////////////////////    
        
        checkAndInstallEnvironment();

        ///////////////////////
        // Start Servers and Python
        ///////////////////////    

        startServersAndPython();
    }

    private void startServersAndPython() {
        new Thread(() -> {
            try {
                System.out.println("[Greta DiffSHEG] Gesture server waiting for connection on port 6501...");
                gesture_server.startConnection();
                System.out.println("[Greta DiffSHEG] Gesture server connected.");
                // Once connected, send a confirmation to Python
                gesture_server.sendMessage("ok");
                String readySignal = gesture_server.receiveMessage();
                if (readySignal != null && readySignal.equals("READY")){
                    System.out.println("[Greta DiffSHEG] READY signal received from Python. Starting gesture loop.");
                    receiveGestureDataLoop();
                } else {
                System.err.println("[Greta DiffSHEG] Did not receive READY signal. Gesture loop will not start.");
                }
                // Start the main loop to receive gesture data
                

            } catch (IOException ex) {
                Logger.getLogger(DiffSHEG.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("[Greta DiffSHEG] Launching Python script: " + batch_main_path);
            server_process = new ProcessBuilder(batch_main_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            Runtime.getRuntime().addShutdownHook(new shutdownHook(server_process, batch_kill_path)); // Your hook
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void checkAndInstallEnvironment() throws IOException {
        try { //
            server_process = new ProcessBuilder("python", python_env_checker_path).redirectErrorStream(true).start(); //
        } catch (Exception e) { //
            e.printStackTrace(); //
        }
        InputStream inputStream = server_process.getInputStream(); //
        result = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n")); //
        System.out.println("[Greta DiffSHEG].init_DiffSHEG_server(): DiffSHEG, python env exist: " + result); //

        if (result.equals("0")) { //
            System.out.println("[Greta DiffSHEG].init_DiffSHEG_server(): DiffSHEG, installing python environment..."); //
            try { //
                server_process = new ProcessBuilder(batch_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start(); //
                server_process.waitFor(); //
            } catch (Exception e) { //
                e.printStackTrace(); //
            }
        }
    }

    private void receiveGestureDataLoop() {
        System.out.println("[Greta DiffSHEG] Starting gesture reception loop...");
        try {
            String bvhbatch;
            while ((bvhbatch = gesture_server.receiveMessage()) != null) {
                if (bvhbatch.equals("END_OF_GENERATION")) {
                    System.out.println("[Greta DiffSHEG] Received end signal. Holding frame and resetting state.");
                    this.cm.setHoldFrame(true);
                    this.isFirstFrame = true;
                    //gesture_server.sendMessage("ok_end");
                } else if (!bvhbatch.isEmpty()) {
                    ArrayList<BAPFrame> bapFrameBatch = new ArrayList<>();
                    String[] bvhFrameLines = bvhbatch.split((";"));

                    for (String bvhFrameLine : bvhFrameLines){
                        BAPFrame bapFrame = bvhProcessor.convertLineToBAP(bvhFrameLine);
                        if (bapFrame != null) {
                            if (isFirstFrame) {
                                baseFrameTime = (int) (Timer.getTime() * Constants.FRAME_PER_SECOND);
                                frameCounter = 0;
                                isFirstFrame = false;
                                this.cm.setHoldFrame(false);
                            }
                            int currentFrameNumber = baseFrameTime + frameCounter;
                            bapFrame.setFrameNumber(currentFrameNumber);

                            bapFrameBatch.add(bapFrame);

                            frameCounter ++;
                        }
                    }
                    if (!bapFrameBatch.isEmpty()) {
                        ID id = IDProvider.createID("DiffSHEG_GESTURE_BATCH");
                        this.sendBAPFrames(id, bapFrameBatch);
                        System.out.println("[Greta DiffSHEG] Sending a batch od size" + bapFrameBatch.size());
                    }
                    //gesture_server.sendMessage("ok_frame");
                }
            }
        } catch (IOException e) {
            System.err.println("[Greta DiffSHEG] Connection lost or IO error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void setCharacterManager(CharacterManager cm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }   
    
    @Override
    public CharacterManager getCharacterManager() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onCharacterChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}