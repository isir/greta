/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;
import vib.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class BAPServerToExternal extends APServerToExternal implements BAPFramesPerformer {
   public BAPServerToExternal() {
    }

    public BAPServerToExternal(int port) {
        super(port);
    }

    @Override
    public void performBAPFrames(List<BAPFrame> bapFrameList, ID idRequest) {
        updateAnimParamFrameList(bapFrameList, "BAPFrames", idRequest.toString());
    }


    public void performBAPFrame(BAPFrame bapf, ID idRequest) {
        List<BAPFrame> bapFrameList = new ArrayList<BAPFrame>(1);
        if(bapFrameList.add(bapf)) {
            updateAnimParamFrameList(bapFrameList, "BAPFrames", idRequest.toString());
        }
    }

}
