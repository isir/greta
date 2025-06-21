/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.thrift;

import greta.auxiliary.thrift.gen_java.Message;
import greta.auxiliary.thrift.gen_java.ThriftAnimParam;
import greta.auxiliary.thrift.gen_java.ThriftAnimParamFrame;
import greta.auxiliary.thrift.services.ServerToExternal;
import greta.core.util.animationparameters.AnimationParameter;
import greta.core.util.animationparameters.AnimationParametersFrame;
import greta.core.util.log.Logs;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
   public void updateAnimParamFrameList(List<APF> gretaAPframes, String type, String requestId) {
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
            for (ThriftAnimParamFrame apframe : gretaAPFrameList2thriftAPFrameList(gretaAPframes)) {

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

            //thriftAPFrameList.addAll(gretaAPFrameList2thriftAPFrameList(gretaAPframes));
        }
        // Update of Message with the frames of frameNumber between currentFrame  - DELAY_IN_NUM_OF_FRAMES and currentFrame + DELAY_IN_NUM_OF_FRAMES
        Message m = new Message();
        m.setType(type);
        m.setId(requestId + Timer.getTime());
        m.setTime(Timer.getTimeMillis());
        m.setAPFrameList(thriftAPFrameList);
        // m.APFrameList = gretaAPFrameList2thriftAPFrameList(gretaAPframes);

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
    private List<ThriftAnimParamFrame> gretaAPFrameList2thriftAPFrameList(List<APF> gretaAPframes) {

        List<ThriftAnimParamFrame> thriftAPFrameList = new ArrayList<ThriftAnimParamFrame>(gretaAPframes.size());

        for (AnimationParametersFrame gretaFrame : gretaAPframes) {

            ThriftAnimParamFrame thriftFrame = new ThriftAnimParamFrame();
            thriftFrame.frameNumber = gretaFrame.getFrameNumber();
            //   Logs.debug("thriftFrame.frameNumber: "+thriftFrame.frameNumber);
            thriftFrame.animParamList = new ArrayList<ThriftAnimParam>(gretaFrame.size());
            List<AnimationParameter> apList = gretaFrame.getAnimationParametersList();
            for (AnimationParameter ap : apList) {
                ThriftAnimParam thriftAP = new ThriftAnimParam(ap.getMask(), ap.getValue());
                thriftFrame.animParamList.add(thriftAP);
            }
            thriftAPFrameList.add(thriftFrame);
        }
        return thriftAPFrameList;
    }
}
