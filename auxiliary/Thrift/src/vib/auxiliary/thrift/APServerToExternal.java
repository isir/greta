/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.gen_java.ThriftAnimParam;
import vib.auxiliary.thrift.gen_java.ThriftAnimParamFrame;
import vib.auxiliary.thrift.services.ServerToExternal;
import vib.core.util.Constants;
import vib.core.util.animationparameters.AnimationParameter;
import vib.core.util.animationparameters.AnimationParametersFrame;
import vib.core.util.log.Logs;
import vib.core.util.time.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import vib.core.animation.mpeg4.fap.FAPFrame;

/**
 *
 * @author Ken Prepin
 */
public class APServerToExternal<APF extends AnimationParametersFrame> extends ServerToExternal {

    public APServerToExternal() {
    }

    public APServerToExternal(int port) {
        super(port);
    }

   public void updateAnimParamFrameList() {
        updateAnimParamFrameList(new ArrayList<APF>(), message.getType(), "");
   }
   public void updateAnimParamFrameList(List<APF> vibAPframes, String type, String requestId) {
        List<ThriftAnimParamFrame> thriftAPFrameList = new ArrayList<ThriftAnimParamFrame>();
        synchronized (messageLock) {

            // update messageTempon by removing frames which FrameNumber is < (Timer.getTimeMillis() / Constants.FRAME_DURATION_MILLIS) + DELAY_IN_NUM_OF_FRAMES and adding them to thriftAPFrameList
            if (messageTempon.isSetAPFrameList()) {
              //  Logs.debug("Tempon list exists");
                ListIterator<ThriftAnimParamFrame> iter = messageTempon.getAPFrameList().listIterator();
                while (iter.hasNext()) {
                    ThriftAnimParamFrame frame = iter.next();
                    if (frame.getFrameNumber() <= (Timer.getCurrentFrameNumber()) + DELAY_IN_NUM_OF_FRAMES) {
                        if (frame.getFrameNumber() > (Timer.getCurrentFrameNumber()) - DELAY_IN_NUM_OF_FRAMES) {
                            Logs.debug("Frame from tempon: " + frame.getFrameNumber() + " current frame number " + (Timer.getCurrentFrameNumber()));
                            thriftAPFrameList.add(new ThriftAnimParamFrame(frame));
                        } else {
                            Logs.debug("Remove from tempon: " + frame.getFrameNumber() + " current frame number " + (Timer.getCurrentFrameNumber()));
                        }
                        iter.remove();
                    }
                }
            }
            if (!isMessageOutdated()) {
                thriftAPFrameList.addAll(this.getMessage().getAPFrameList());
                for (int i = 0; i < thriftAPFrameList.size(); i++) {
                    // remove of outdated frames
                    if (thriftAPFrameList.get(i).getFrameNumber() < ((Timer.getCurrentFrameNumber()) - DELAY_IN_NUM_OF_FRAMES)) {
                        thriftAPFrameList.remove(i);
                        i--;
                    }
                }
            } else {
                Logs.debug(" Message outdated " + message.getId() + " content "+ message.getString_content() + " type " + message.getType());
            }
            for (ThriftAnimParamFrame apframe : vibAPFrameList2thriftAPFrameList(vibAPframes)) {

                // Put in MessageTmp the frames of FrameNumber > currentFrame + DELAY_IN_NUM_OF_FRAMES
                if (apframe.getFrameNumber() > (Timer.getCurrentFrameNumber()) + DELAY_IN_NUM_OF_FRAMES) {
                    if (!messageTempon.isSetAPFrameList()) {
                        messageTempon.setAPFrameList(new ArrayList<ThriftAnimParamFrame>());
                    }
                    messageTempon.getAPFrameList().add(new ThriftAnimParamFrame(apframe));

                    // Add to Message the frames of FrameNumber > currentFrame - DELAY_IN_NUM_OF_FRAMES
                } else if (apframe.getFrameNumber() > (Timer.getCurrentFrameNumber()) - DELAY_IN_NUM_OF_FRAMES) {
                    Logs.debug("New frame: " + apframe.getFrameNumber());
                    thriftAPFrameList.add(new ThriftAnimParamFrame(apframe));
                }
            }

            //thriftAPFrameList.addAll(vibAPFrameList2thriftAPFrameList(vibAPframes));
        }
        // Update of Message with the frames of frameNumber between currentFrame  - DELAY_IN_NUM_OF_FRAMES and currentFrame + DELAY_IN_NUM_OF_FRAMES
        Message m = new Message();
        m.setType(type);
        m.setId(requestId + Timer.getTime());
        m.setTime(Timer.getTimeMillis());
        m.setAPFrameList(thriftAPFrameList);
        // m.APFrameList = vibAPFrameList2thriftAPFrameList(vibAPframes);

        if (!thriftAPFrameList.isEmpty()) {
            m.setFirstFrameNumber(thriftAPFrameList.get(0).getFrameNumber());
            m.setLastFrameNumber(thriftAPFrameList.get(thriftAPFrameList.size() - 1).getFrameNumber());
            Logs.debug("lastFrameNumber = " + m.getLastFrameNumber());
        }
        setMessage(m);
   /*     if (messageTempon.isSetAPFrameList()) {
            Logs.debug("Message " + m.type + " " + m.id + " currentTime " + (Timer.getCurrentFrameNumber()) + " size " + m.getAPFrameList().size() + " size tampon " + messageTempon.getAPFrameList().size());
        } else {
            Logs.debug("Message " + m.type + " " + m.id + " size " + m.getAPFrameList().size());
        }*/

    }

    @Override
    public  Message getMessage(String oldMessageId) {
        updateAnimParamFrameList();
        synchronized(messageLock){
        if(isNewMessage(oldMessageId)){
            return message;

        } else {
            Message m = new Message();
            m.setType("empty");
            return m;
        }
        }
    }
    private List<ThriftAnimParamFrame> vibAPFrameList2thriftAPFrameList(List<APF> vibAPframes) {

        List<ThriftAnimParamFrame> thriftAPFrameList = new ArrayList<ThriftAnimParamFrame>(vibAPframes.size());

        for (AnimationParametersFrame vibFrame : vibAPframes) {

            ThriftAnimParamFrame thriftFrame = new ThriftAnimParamFrame();
            thriftFrame.frameNumber = vibFrame.getFrameNumber();
            //   Logs.debug("thriftFrame.frameNumber: "+thriftFrame.frameNumber);
            thriftFrame.animParamList = new ArrayList<ThriftAnimParam>(vibFrame.size());
            List<AnimationParameter> apList = vibFrame.getAnimationParametersList();
            for (AnimationParameter ap : apList) {
                ThriftAnimParam thriftAP = new ThriftAnimParam(ap.getMask(), ap.getValue());
                thriftFrame.animParamList.add(thriftAP);
            }
            thriftAPFrameList.add(thriftFrame);
        }
        return thriftAPFrameList;
    }
}
