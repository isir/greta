/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.Microphone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Takeshi Saga
 */

public class Microphone {
    
    private final String python_env_checker_path = "Common\\Data\\microphone\\check_env.py";
    private final String batch_env_installer_path = "Common\\Data\\microphone\\init_env.bat";
    private final String batch_main_path = "Common\\Data\\microphone\\run_mic.bat";
    private final String batch_kill_path = "Common\\Data\\microphone\\kill_server.bat";
    private Process server_process;
    private Thread server_shutdownHook;
    
    private String port;

    public Microphone(String port_local) {
        
        port = port_local;
        
        System.out.println("greta.auxiliary.Microphone.Microphone()");
        
        ///////////////////////
        // Check environment
        ///////////////////////

        try{
            server_process = new ProcessBuilder("python", python_env_checker_path).redirectErrorStream(true).start();
            // server_process.waitFor();
        } catch (Exception e){
           e.printStackTrace();
        }
        InputStream inputStream = server_process.getInputStream();
        String result = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")
                );
        System.out.println(".init_Microphone_server(): Microphone, python env exist: " + result);        
        
        ///////////////////////
        // Create environment if not exit
        ///////////////////////

        if(result.equals("0")){
            System.out.println(".init_Microphone_server(): Microphone, installing python environment...");
            try{
                server_process = new ProcessBuilder(batch_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                server_process.waitFor();
            } catch (Exception e){
                e.printStackTrace();
            }            
        }

        ///////////////////////
        // Start microphone server to broadcast
        ///////////////////////
        
        System.out.println(".init_Microphone_server(): Microphone, starting microphone server...");
        try{
            server_process = new ProcessBuilder(batch_main_path, port).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            
            server_shutdownHook = new Thread(() -> {
                
                    while (server_process.isAlive()) {
                        Process process;
                        try {
                            process = new ProcessBuilder(batch_kill_path, port).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                            process.waitFor();
                        } catch (IOException ex) {
                            Logger.getLogger(Microphone.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Microphone.class.getName()).log(Level.SEVERE, null, ex);
                        }                        
                    }
                }
            );
            Runtime.getRuntime().addShutdownHook(server_shutdownHook);
        } catch (Exception e){
            e.printStackTrace();
        }
        
    }
    
    public void kill_server() {
        try {
            Process kill_server_process = new ProcessBuilder(batch_kill_path, port).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            kill_server_process.waitFor();
        } catch (IOException ex) {
            Logger.getLogger(Microphone.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Microphone.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
