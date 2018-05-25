/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.fap.filters;

import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import vib.core.animation.mpeg4.fap.FAPFramePerformer;
import vib.core.util.id.ID;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class NulFilter extends FAPFrameEmitterImpl implements FAPFramePerformer{

    @Override
    public void performFAPFrames(List<FAPFrame> fap_animation, ID requestId) {
        ArrayList<FAPFrame> newlist = new ArrayList<FAPFrame>(fap_animation.size());
        for (FAPFrame frame : fap_animation) {
            newlist.add(new FAPFrame(frame));
        }
        sendFAPFrames(requestId, newlist);
    }

    @Override
    public void performFAPFrame(FAPFrame fap_anim, ID requestId) {
        sendFAPFrame(requestId, fap_anim);
    }

}
