/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.bap;

import vib.core.util.id.ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is a basic implementation of {@code BAPFramesEmitter}.<br/>
 * It provides some methods to send {@code BAPFrames} to all {@code BAPFramesPerfomers} added.
 * @author Andre-Marie Pez
 */
public class BAPFramesEmitterImpl implements BAPFramesEmitter{

    private ArrayList<BAPFramesPerformer> performers = new ArrayList<BAPFramesPerformer>();

    @Override
    public void addBAPFramesPerformer(BAPFramesPerformer performer) {
        if (performer != null) {
            performers.add(performer);
        }
    }

    @Override
    public void removeBAPFramesPerformer(BAPFramesPerformer performer) {
        if (performer != null) {
            performers.remove(performer);
        }
    }

    public void sendBAPFrames(ID requestId, BAPFrame... frames){
        sendBAPFrames(requestId, Arrays.asList(frames));
    }


    public void sendBAPFrames(ID requestId, List<BAPFrame> frames){
        for(BAPFramesPerformer performer : performers){
            performer.performBAPFrames(frames, requestId);
        }
    }

    public void sendBAPFrame(ID requestId, BAPFrame frame){
        sendBAPFrames(requestId, frame);
    }
}
