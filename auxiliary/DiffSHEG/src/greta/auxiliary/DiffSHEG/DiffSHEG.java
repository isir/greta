/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.DiffSHEG;

import greta.auxiliary.DiffSHEG.BVHProcessor;


import greta.core.util.CharacterManager;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
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
public class DiffSHEG implements BAPFrameEmitter {
    private final String base_bvh_path = "Common\\Data\\DiffSHEG\\data\\GRETA\\Base_greta_fingers_bis.bvh";
    private final String python_env_checker_path = "Common\\Data\\DiffSHEG\\check_env.py";
    private final String batch_env_installer_path = "Common\\Data\\DiffSHEG\\init_env.bat";
    private final String batch_main_path = "Common\\Data\\DiffSHEG\\run_DiffSHEG.bat";
    private final String batch_kill_path = "Common\\Data\\DiffSHEG\\kill_server.bat";
    private Process server_process;

    private Thread server_shutdownHook;

    private Server feedback_server;
    private String response;

    private Server gesture_server;
    private InputStream inputStream;
    private String result;

    private BVHProcessor bvhProcessor;

    private final BAPFrameEmitterImpl bapFrameEmitterImpl = new BAPFrameEmitterImpl();

    public DiffSHEG () throws IOException {
        System.out.println("greta.auxiliary.DiffSHEG.DiffSHEG()");
        
        feedback_server = new Server(); 
        feedback_server.setAddress("localhost"); 
        feedback_server.setPort("6500"); 


        gesture_server = new Server();
        gesture_server.setAddress("localhost"); 
        gesture_server.setPort("6501"); 

        bvhProcessor = new BVHProcessor();
        try (BufferedReader br = new BufferedReader(new FileReader(base_bvh_path))) {
            bvhProcessor.parseBVHHeader(br);
        } catch (IOException e) {
            System.err.println("FATAL: Could not read BVH header file. " + e.getMessage());
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

    @Override
    public void addBAPFramePerformer(BAPFramePerformer perfomer) {
        this.bapFrameEmitterImpl.addBAPFramePerformer(perfomer);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer performer) {
        this.bapFrameEmitterImpl.removeBAPFramePerformer(performer);
    }

    private void startServersAndPython() {
        new Thread(() -> {
            try {
                System.out.println("Feedback server waiting for connection on port 6500...");
                feedback_server.startConnection();
                System.out.println("Feedback server connected.");
            } catch (IOException ex) {
                Logger.getLogger(DiffSHEG.class.getName()).log(Level.SEVERE, null, ex);
            }
        }).start();
    
        new Thread(() -> {
            try {
                System.out.println("Gesture server waiting for connection on port 6501...");
                gesture_server.startConnection();
                System.out.println("Gesture server connected.");
                // Once connected, send a confirmation to Python
                gesture_server.sendMessage("ok");

                // Start the main loop to receive gesture data
                receiveGestureDataLoop();

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
            System.out.println("Launching Python script: " + batch_main_path);
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
        System.out.println(".init_DiffSHEG_server(): DiffSHEG, python env exist: " + result); //

        if (result.equals("0")) { //
            System.out.println(".init_DiffSHEG_server(): DiffSHEG, installing python environment..."); //
            try { //
                server_process = new ProcessBuilder(batch_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start(); //
                server_process.waitFor(); //
            } catch (Exception e) { //
                e.printStackTrace(); //
            }
        }
    }

    private void receiveGestureDataLoop() {
        System.out.println("Starting gesture reception loop...");
        try {
            String bvhFrameLine;
            while ((bvhFrameLine = gesture_server.receiveMessage()) != null) {
                if (!bvhFrameLine.isEmpty()) {
                    BAPFrame bapFrame = bvhProcessor.convertLineToBAP(bvhFrameLine);
                    if (bapFrame != null) {
                        ID id = IDProvider.createID("DiffSHEG_GESTURE");
                        bapFrameEmitterImpl.sendBAPFrame(id, bapFrame);
                    }
                    gesture_server.sendMessage("ok");
                }
            }
        } catch (IOException e) {
            System.err.println("Connection lost or IO error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void sendFeedbackToPython(String type) {
        try {
            System.out.println("Sending feedback to Python: " + type);
            feedback_server.sendMessage(type);
            feedback_server.receiveMessage(); // Wait for 'ok' acknowledgment
        } catch (IOException e) {
            System.err.println("Failed to send feedback to python: " + e.getMessage());
        }
    }
}