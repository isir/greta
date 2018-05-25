/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesEmitter;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;
import vib.core.util.animationparameters.AnimationParametersFrame;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import java.util.ArrayList;

/**
 *
 * @author Ken Prepin
 */
public class BAPReceiver extends APReceiver implements BAPFramesEmitter {

    private ArrayList<BAPFramesPerformer> bapFramesPerfList;

    public BAPReceiver() {
        super();
        bapFramesPerfList = new ArrayList<BAPFramesPerformer>();
    }

    public BAPReceiver(int port) {
        super(port);
        bapFramesPerfList = new ArrayList<BAPFramesPerformer>();
    }

    @Override
    public void addBAPFramesPerformer(BAPFramesPerformer performer) {
        bapFramesPerfList.add(performer);
    }

    @Override
    public void removeBAPFramesPerformer(BAPFramesPerformer performer) {
        bapFramesPerfList.remove(performer);
    }

    @Override
    protected AnimationParametersFrame newAnimParamFrame(int frameNumber) {
        return new BAPFrame(frameNumber);
    }

    @Override
    public void perform(Message m) {
        ID id = IDProvider.createID(m.getId());
        for (BAPFramesPerformer performer : bapFramesPerfList) {
            performer.performBAPFrames(getVibAPFrameList(m), id);
        }
    }
}
