/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.animation.mpeg4.fap.FAPFramePerformer;
import vib.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class FAPSender extends APSender implements FAPFramePerformer {

    public FAPSender() {
    }

    public FAPSender(String host, int port) {
        super(host, port);
    }

    @Override
    public void performFAPFrames(List<FAPFrame> fapFrameList, ID idRequest) {
        sendAnimParamFrameList(fapFrameList, "FAPFrames", idRequest);
    }

    @Override
    public void performFAPFrame(FAPFrame fapf, ID idRequest) {
        List<FAPFrame> fapFrameList = new ArrayList<FAPFrame>(1);
        if(fapFrameList.add(fapf)) {
            sendAnimParamFrameList(fapFrameList, "FAPFrames", idRequest);
        }
    }
}
