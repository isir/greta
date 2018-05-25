/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */

package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextSender;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.util.id.ID;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 * @author Radoslaw Niewiadomski
*/
public class FAPSender extends TextSender implements vib.core.animation.mpeg4.fap.FAPFramePerformer {

    private HashMap<String,Object> semaineMap;
    private FapCommander commander = new FapCommander();//semaine tricks

    public FAPSender(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "vib.FAP");
    }

    public FAPSender(String host, String port, String topic){
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "FAP");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
    }

    @Override
    public void performFAPFrames(List<FAPFrame> fapframes, ID requestId) {
        String fapString = getFrames(fapframes);
        semaineMap.put("content-id", requestId.toString() + System.currentTimeMillis());
          this.send(fapString, semaineMap);

          //Logs.warning(fapString);
    }


   @Override
    public void performFAPFrame(FAPFrame fapf, ID requestId) {
       ArrayList<FAPFrame> frames = new ArrayList<FAPFrame>(1);
       frames.add(fapf);
       performFAPFrames(frames, requestId);
    }




    @Override
   public void send(String text, Map<String, Object> properties) {
      String requestId=(String) properties.get("content-id");
        if(commander!=null) commander.sendDataInfo(requestId);
        super.send("\n"+text, properties);
        if(commander!=null) commander.sendPlayCommand(requestId);
    }


   public String getFrames(List<FAPFrame> frames) {
        StringBuffer fap = new StringBuffer();
        System.out.println("FAP start: " + frames.get(0).getFrameNumber());
        for (FAPFrame fapframe : frames) {
            fapframe.setFrameNumber(fapframe.getFrameNumber());
            String out = fapframe.AnimationParametersFrame2String();
            fap.append(out);
        }
        System.out.println(fap);
        return fap.toString();
    }
      @Override
    protected void onSend(Map<String, Object> properties) {
        properties.put("usertime", System.currentTimeMillis());
        properties.put("content-creation-time", System.currentTimeMillis());
        properties.putAll(semaineMap);
        // Must be overrided to complete the map
    }



}
