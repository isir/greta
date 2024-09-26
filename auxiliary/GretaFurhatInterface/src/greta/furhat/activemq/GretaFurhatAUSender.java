/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.furhat.activemq;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.furhat.activemq.TextSender;
import greta.furhat.activemq.WhiteBoard;
import greta.core.util.speech.Speech;
import greta.core.util.speech.TTS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.animation.mpeg4.fap.FAPType;
import greta.core.util.audio.Audio;
import greta.core.util.id.ID;
import greta.core.util.speech.Phoneme;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioFormat;

/**
 *
 * @author Fousseyni Sangaré 04/2024-09/2024
 */


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


public class GretaFurhatAUSender extends TextSender {
    
    private HashMap<String,Object> metaDataMap;
    private ScheduledExecutorService executorService;

    public GretaFurhatAUSender(){
        this("localhost",
             "61616",
             "greta.furhat.AUs");
        
    }

    public GretaFurhatAUSender(String host, String port, String topic){
        super(host, port, topic);
        metaDataMap = new HashMap<String,Object>();
        metaDataMap.put("content-type", "rotation Angles");
        metaDataMap.put("datatype", "radian");
        metaDataMap.put("source", "Greta MPEG4Agent");
        metaDataMap.put("event", "single");
        
        //executorService = Executors.newScheduledThreadPool(1);
        //startConnectionAttempt();
    }
    
    
        @Override
    protected void onSend(Map<String, Object> properties) {
        properties.put("usertime", System.currentTimeMillis());
        properties.put("content-creation-time", System.currentTimeMillis());
        properties.putAll(metaDataMap);
        // Must be overrided to complete the map
    }
    
    private static String serializeRotation(float[] rotation){
        // float rotation = {r, p, y}
        StringBuilder sb = new StringBuilder();
        for (float angle : rotation){
            sb.append(angle).append(",");
        }
        return sb.toString();
    }    

    private void attemptConnection() {
        
        
        if (!this.isConnected()){
            
            try{
                //System.out.println("Attempting to connect audioserver to broker at: "+this.getURL());
                startConnection();
            } catch(Exception e){
            System.err.println("greta.furhat.activemq.GretaFurhatAUSender: au server connection attempt failed: " + e.getMessage());
            }
        }
        else{
            System.out.println("greta.furhat.activemq.GretaFurhatAUSender: au server connected to broker at: "+this.getURL() );
            executorService.shutdown(); // Stop retrying after a successful connection
         }   
    }
    
    
    
}
