/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextSender;
import vib.auxiliary.activemq.WhiteBoard;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class BapCommander extends TextSender{

    private HashMap<String,Object> semaineMap;

    public BapCommander(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "semaine.data.synthesis.lowlevel.command");
    }
    public BapCommander(String host, String port, String topic){
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
    }

    public void sendDataInfo(String requestId) {
        semaineMap.put("datatype", "dataInfo");
        semaineMap.put("content-id", requestId);
        semaineMap.put("usertime", System.currentTimeMillis());
        semaineMap.put("content-creation-time", System.currentTimeMillis());
        this.send("HASAUDIO " + 0 + "\nHASFAP " + 0 + "\nHASBAP " + 1 + "\n", semaineMap);
    }

    public void sendPlayCommand(String requestId) {
        semaineMap.put("datatype", "playCommand");
        semaineMap.put("content-id", requestId);
        semaineMap.put("usertime", System.currentTimeMillis());
        semaineMap.put("content-creation-time", System.currentTimeMillis());
        this.send("STARTAT " + 0 + "\nLIFETIME " + 10000 + "\nPRIORITY " + 1 + "\n", semaineMap);
    }
}
