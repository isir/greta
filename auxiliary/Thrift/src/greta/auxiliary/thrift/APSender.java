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
import greta.auxiliary.thrift.services.Sender;
import greta.core.util.animationparameters.AnimationParameter;
import greta.core.util.animationparameters.AnimationParametersFrame;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public abstract class APSender<APF extends AnimationParametersFrame> extends Sender{
    public APSender(){
        this(Sender.DEFAULT_THRIFT_HOST, Sender.DEFAULT_THRIFT_PORT);
    }

    public APSender(String host, int port){
        super(host, port);
    }

    public void sendAnimParamFrameList(List<APF> gretaAPFrames, String type, ID requestId) {
       List<ThriftAnimParamFrame> thriftAPFrameList = gretaAPFrameListToThriftAPFrameList(gretaAPFrames);
       Message m = new Message();
       m.type = type;
       m.id = requestId.toString();
       m.time = Timer.getTimeMillis();
       m.APFrameList = thriftAPFrameList;
       send(m);
    }

    private List<ThriftAnimParamFrame> gretaAPFrameListToThriftAPFrameList(List<APF> gretaAPFrames) {
        List<ThriftAnimParamFrame> thriftAPFrameList  = new ArrayList<>(gretaAPFrames.size());

        for (AnimationParametersFrame gretaFrame : gretaAPFrames) {
            ThriftAnimParamFrame thriftFrame = new ThriftAnimParamFrame();
            thriftFrame.frameNumber = gretaFrame.getFrameNumber();
            Logs.debug("thriftFrame.frameNumber: " + thriftFrame.frameNumber);
            thriftFrame.animParamList = new ArrayList<>(gretaFrame.size());
            List<AnimationParameter> apList = gretaFrame.getAnimationParametersList();
            for (AnimationParameter ap : apList){
                ThriftAnimParam thriftAP = new ThriftAnimParam(ap.getMask(), ap.getValue());
                thriftFrame.animParamList.add(thriftAP);
            }
            thriftAPFrameList.add(thriftFrame);
        }
        return thriftAPFrameList;
    }
}
