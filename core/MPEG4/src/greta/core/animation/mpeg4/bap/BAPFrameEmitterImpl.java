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
