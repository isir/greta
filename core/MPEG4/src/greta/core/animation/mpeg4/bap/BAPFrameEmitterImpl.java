/*
 * This file is part of Greta.
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
package greta.core.animation.mpeg4.bap;

import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is a basic implementation of {@code BAPFrameEmitter}.<br/>
 * It provides some methods to send {@code BAPFrames} to all {@code BAPFramePerformer} added.
 * @author Andre-Marie Pez
 */
public class BAPFrameEmitterImpl implements BAPFrameEmitter{

    private ArrayList<BAPFramePerformer> performers = new ArrayList<>();

    @Override
    public void addBAPFramePerformer(BAPFramePerformer performer) {
        if (performer != null) {
            performers.add(performer);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer performer) {
        if (performer != null) {
            performers.remove(performer);
        }
    }

    public void sendBAPFrames(ID requestId, BAPFrame... frames){
        sendBAPFrames(requestId, Arrays.asList(frames));
    }

    public void sendBAPFrames(ID requestId, List<BAPFrame> frames){
        for(BAPFramePerformer performer : performers){
            performer.performBAPFrames(frames, requestId);
        }
    }

    public void sendBAPFrame(ID requestId, BAPFrame frame){
        sendBAPFrames(requestId, frame);
    }

    /**
     * Sends a message to cancel all the {@code BAPFrame} with the given {@code ID} to all linked BAPFramePerformer.
     * @param requestId ID of the frames to cancel
     */
    public void cancelFramesWithIDInLinkedPerformers(ID requestId) {
        for (BAPFramePerformer performer : performers) {
            if (performer instanceof CancelableBAPFramePerformer) {
                ((CancelableBAPFramePerformer) performer).cancelBAPFramesById(requestId);
            }
        }
    }
}
