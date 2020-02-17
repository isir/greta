/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.thrift;

import greta.auxiliary.thrift.gen_java.Message;
import greta.auxiliary.thrift.gen_java.ThriftAnimParam;
import greta.auxiliary.thrift.gen_java.ThriftAnimParamFrame;
import greta.auxiliary.thrift.services.ExternalClient;
import greta.core.util.animationparameters.AnimationParameter;
import greta.core.util.animationparameters.AnimationParametersFrame;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public abstract class APExternalClient<APF extends AnimationParametersFrame> extends ExternalClient{

    public APExternalClient(){
        super();
    }
    public APExternalClient(String host, int port){
        super(host,port);
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
