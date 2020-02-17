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
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitter;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.util.animationparameters.AnimationParametersFrame;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
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
            performer.performFAPFrames(getGretaAPFrameList(m), id);
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
