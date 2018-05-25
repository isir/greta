/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.animation.mpeg4.fap.FAPFrameEmitter;
import vib.core.animation.mpeg4.fap.FAPFramePerformer;
import vib.core.util.animationparameters.AnimationParametersFrame;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import java.util.ArrayList;

/**
 *
 * @author Ken Prepin
 */
public class FAPReceiver extends APReceiver implements FAPFrameEmitter{

    private ArrayList<FAPFramePerformer> fapFramesPerfList;

    public FAPReceiver(){
        super();
        fapFramesPerfList =  new ArrayList<FAPFramePerformer>();
    }
    public FAPReceiver(int port){
        super(port);
        fapFramesPerfList =  new ArrayList<FAPFramePerformer>();
   }
    @Override
    protected AnimationParametersFrame newAnimParamFrame(int frameNumber) {
        return new FAPFrame(frameNumber);
    }

    @Override
    public void perform(Message m) {
        ID id = IDProvider.createID(m.getId());
        for(FAPFramePerformer performer:fapFramesPerfList){
            performer.performFAPFrames(getVibAPFrameList(m), id);
        }
    }

    @Override
    public void addFAPFramePerformer(FAPFramePerformer fapfp) {
        fapFramesPerfList.add(fapfp);
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer fapfp) {
        fapFramesPerfList.remove(fapfp);
    }

/*    public static void main(final String[] args){
        FAPReceiver receiver = new FAPReceiver(9091);
    }//*/

}
