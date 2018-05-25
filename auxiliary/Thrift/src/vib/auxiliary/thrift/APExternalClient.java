/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
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
