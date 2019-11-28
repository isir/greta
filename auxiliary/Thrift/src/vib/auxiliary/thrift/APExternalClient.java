/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.gen_java.ThriftAnimParam;
import vib.auxiliary.thrift.gen_java.ThriftAnimParamFrame;
import vib.auxiliary.thrift.services.ExternalClient;
import vib.core.util.animationparameters.AnimationParameter;
import vib.core.util.animationparameters.AnimationParametersFrame;
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

    public List<APF> getVibAPFrameList(Message m) {
        return thriftAPFrameList2VibAPFrameList(m.getAPFrameList());
    }

    public List<APF> thriftAPFrameList2VibAPFrameList(List<ThriftAnimParamFrame> thriftAPframes) {
        List<APF> vibAPFrameList = new ArrayList<APF>(thriftAPframes.size());

        for (ThriftAnimParamFrame thriftFrame : thriftAPframes) {
            APF vibFrame = newAnimParamFrame(thriftFrame.getFrameNumber());
            List<ThriftAnimParam> thriftAPList = thriftFrame.getAnimParamList();
            int i=0;
            for (ThriftAnimParam thriftAP : thriftAPList) {
                AnimationParameter vibAP = vibFrame.newAnimationParameter(thriftAP.isMask(), thriftAP.getValue());
                vibFrame.getAnimationParametersList().set(i,vibAP);
                ++i;
            }
            vibAPFrameList.add(vibFrame);
        }
        return vibAPFrameList;
    }

}
