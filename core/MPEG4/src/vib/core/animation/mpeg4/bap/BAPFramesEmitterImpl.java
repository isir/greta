/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
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
