/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;
import vib.core.util.id.ID;
import java.util.List;



/**
 *
 * @author Ken Prepin
 */
public class BAPSender extends APSender implements BAPFramesPerformer{

    public BAPSender(){

    }
    public BAPSender(String host, int port){
        super(host, port);
    }

    @Override
    public void performBAPFrames(List<BAPFrame> bapframes, ID requestId) {
       sendAnimParamFrameList(bapframes, "BAPFrames", requestId);
    }
}
