/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.DiffSHEG;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author takes
 */

public class shutdownHook extends Thread{
    
    Process process;
    Process killer_process;
    String killer_path;
    
    public shutdownHook(Process process, String server_killer_path){
        this.process = process;
        this.killer_path = server_killer_path;
    }
    
    public void run(){
        while(this.process.isAlive()){
            System.out.println("greta.auxiliary.DiffSHEG.shutdownHook: trying to kill server process.");
            try {
                killer_process = new ProcessBuilder(killer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            } catch (IOException ex) {
                Logger.getLogger(DiffSHEG.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("greta.auxiliary.DiffSHEG.shutdownHook: destroyed.");
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(shutdownHook.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}