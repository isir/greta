/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
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
