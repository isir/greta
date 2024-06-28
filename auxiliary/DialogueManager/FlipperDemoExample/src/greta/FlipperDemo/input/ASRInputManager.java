/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.input;

/**
 *
 * @author Barnge
 */

import greta.FlipperDemo.main.FlipperLauncherMain;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ASRInputManager {
    
   
    
   private SpeechInputReceiver  inputReceiver;
   private FlipperLauncherMain singletoneInstance = null;
   
   private String host = null;
   private String port = null;
   private String gretaASRTopic = null;
   private String gretaInputTopic = null;
   public boolean connected = false;
   public static Client client;
   public static double positivity =0.0;
   
   
   // crée le client et le lance en thread a la bonne adress et au bon port 
   public void startClient(String address, String port) {
    Thread clientThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                client = new Client(address, port);
                System.out.println("Le client à été initialisé");
                client.startConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
    clientThread.start();
}
   
   public boolean init()
   {   System.out.println("ASR input manager initialized");
       singletoneInstance = FlipperLauncherMain.getInstance();
       if(singletoneInstance != null){
           System.out.println("asrinput got main singleton instance TEST AVEC CLIENT FINAL : "
                   + singletoneInstance.getGretaASRTopic());
       }
       
       host = singletoneInstance.getHost();
       port = singletoneInstance.getPort();
       gretaASRTopic = singletoneInstance.getGretaASRTopic();
       // start le server en thread
       startClient("localhost", "50150");
       inputReceiver = new SpeechInputReceiver(host, port, gretaASRTopic);
       
       
       return true;
   }
     
   public void initSpeechInputReceiver(String host, String port, String topic){
       inputReceiver = new SpeechInputReceiver(host, port, topic);
   }
   
    /*public double receivePositivity(){
        try {
                ASRInputManager.positivity =client.receivePositivity();
            } catch (IOException ex) {
                Logger.getLogger(SpeechInputReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        return ASRInputManager.positivity ;
        
    }*/
     
   // verifie si la positivité a été recu (instancié dans le template example.xml. Lance la methode de recup de donnée recu par le client. et les assigne a la variable positivité de l'Input manager
    public boolean hasSmile(){
        // if chunk or np frame = chunk size
        //return true 
        try {
                ASRInputManager.positivity =client.receivePositivity();
            } catch (IOException ex) {
                Logger.getLogger(ASRInputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        System.out.println("VERIFICATION DE SOURIR DETECTION");
        return ASRInputManager.positivity != 0.0;
    }
    
    //Converti la valeur récupereé de positivité en string pr etre instancié dans example.xml et associé au bon paramètre 'positivity' dans le Json de flipper.
    public String getPositivity(){
           
        System.out.println("LA VALEUR DE SOURIR DETECTEE EST : "+ String.valueOf(ASRInputManager.positivity));
        return String.valueOf(ASRInputManager.positivity);
    }
 
    public boolean hasMessage(){
        return inputReceiver.hasMessage();
    }
    
     public String getMessage(){
         return inputReceiver.getMessage();
     }
    
     
     /*public String getPositivity(){
        return inputReceiver.getPositivity();
    }
    
    public boolean hasSmile(){
        return inputReceiver.hasSmile();
    }*/
    
    public static Client getClient(){
        return client;
    }
    
}
