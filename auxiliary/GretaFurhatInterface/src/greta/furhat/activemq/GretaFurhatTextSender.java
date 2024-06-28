/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.furhat.activemq;

import greta.furhat.activemq.TextSender;
import greta.furhat.activemq.WhiteBoard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 *
 * @author Admin
 */
public class GretaFurhatTextSender extends TextSender {
    
    private HashMap<String,Object> metaDataMap;

    public GretaFurhatTextSender(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "greta.furhat.head.Rotation");
    }

    public GretaFurhatTextSender(String host, String port, String topic){
        super(host, port, topic);
        metaDataMap = new HashMap<String,Object>();
        metaDataMap.put("content-type", "totation Angles");
        metaDataMap.put("datatype", "radian");
        metaDataMap.put("source", "Greta MPEG4Agent");
        metaDataMap.put("event", "single");
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
    
}
