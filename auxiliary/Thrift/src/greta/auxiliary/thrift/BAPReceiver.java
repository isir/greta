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
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.util.animationparameters.AnimationParametersFrame;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.util.ArrayList;

/**
 *
 * @author Ken Prepin
 */
public class BAPReceiver extends APReceiver implements BAPFrameEmitter {

    private ArrayList<BAPFramePerformer> bapFramesPerfList;

    public BAPReceiver() {
        super();
        bapFramesPerfList = new ArrayList<BAPFramePerformer>();
    }

    public BAPReceiver(int port) {
        super(port);
        bapFramesPerfList = new ArrayList<BAPFramePerformer>();
    }

    @Override
    public void addBAPFramePerformer(BAPFramePerformer performer) {
        bapFramesPerfList.add(performer);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer performer) {
        bapFramesPerfList.remove(performer);
    }

    @Override
    protected AnimationParametersFrame newAnimParamFrame(int frameNumber) {
        return new BAPFrame(frameNumber);
    }

    @Override
    public void perform(Message m) {
        ID id = IDProvider.createID(m.getId());
        for (BAPFramePerformer performer : bapFramesPerfList) {
            performer.performBAPFrames(getGretaAPFrameList(m), id);
        }
    }
}
