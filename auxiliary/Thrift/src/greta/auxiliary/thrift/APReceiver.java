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
import greta.auxiliary.thrift.services.Receiver;
import greta.core.util.animationparameters.AnimationParameter;
import greta.core.util.animationparameters.AnimationParametersFrame;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public abstract class APReceiver<APF extends AnimationParametersFrame> extends Receiver {

    public APReceiver() {
    }

    public APReceiver(int port) {
        super(port);
    }

    protected abstract APF newAnimParamFrame(int frameNumber);

    public List<APF> getGretaAPFrameList(Message m) {
        return thriftAPFrameList2GretaAPFrameList(m.getAPFrameList());
    }

    public List<APF> thriftAPFrameList2GretaAPFrameList(List<ThriftAnimParamFrame> thriftAPframes) {
        List<APF> gretaAPFrameList = new ArrayList<APF>(thriftAPframes.size());

        for (ThriftAnimParamFrame thriftFrame : thriftAPframes) {
            APF gretaFrame = newAnimParamFrame(thriftFrame.getFrameNumber());
            List<ThriftAnimParam> thriftAPList = thriftFrame.getAnimParamList();
            int i=0;
            for (ThriftAnimParam thriftAP : thriftAPList) {
                AnimationParameter gretaAP = gretaFrame.newAnimationParameter(thriftAP.isMask(), thriftAP.getValue());
                gretaFrame.getAnimationParametersList().set(i,gretaAP);
                ++i;
            }
            gretaAPFrameList.add(gretaFrame);
        }
        return gretaAPFrameList;
    }
}
