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

import greta.FlipperDemo.main.Main;

public class ASRInputManager {
    
   
    
   private SpeechInputReceiver  inputReceiver;
   private Main singletoneInstance = null;
   
   private String host = null;
   private String port = null;
   private String gretaASRTopic = null;
   private String gretaInputTopic = null;
   
   
   public boolean init()
   {   System.out.println("ASR input manager initialized");
       singletoneInstance = Main.getInstance();
       if(singletoneInstance != null){
           System.out.println("jai gayatri mata: asrinput got main singleton instance : "
                   + singletoneInstance.getGretaASRTopic());
       }
       
       host = singletoneInstance.getHost();
       port = singletoneInstance.getPort();
       gretaASRTopic = singletoneInstance.getGretaASRTopic();
       inputReceiver = new SpeechInputReceiver(host, port, gretaASRTopic);
       
       
       
       return true;
   }
   public void initSpeechInputReceiver(String host, String port, String topic){
       inputReceiver = new SpeechInputReceiver(host, port, topic);
   }
   
    public boolean hasMessage(){
        return inputReceiver.hasMessage();
    }
    
     public String getMessage(){
         return inputReceiver.getMessage();
     }
}
