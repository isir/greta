/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
