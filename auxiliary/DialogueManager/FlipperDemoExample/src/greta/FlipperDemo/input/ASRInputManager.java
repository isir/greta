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
public class ASRInputManager {
    
   private SpeechInputReceiver  inputReceiver;
   
   public boolean init()
   {   System.out.println("ASR input manager initialized");
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
