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
package greta.core.animation.mpeg4.fap;

import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is a basic implementation of {@code FAPFrameEmitter}.<br/>
 * It provides some methods to send {@code FAPFrames} to all {@code FAPFramePerfomers} added.
 * @author Andre-Marie Pez
 */
public class FAPFrameEmitterImpl implements FAPFrameEmitter {
    private ArrayList<FAPFramePerformer> performers = new ArrayList<>();

    @Override
    public void addFAPFramePerformer(FAPFramePerformer performer) {
        if (performer != null) {
            performers.add(performer);
        }
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer performer) {
        if (performer != null) {
            performers.remove(performer);
        }
    }

    public void sendFAPFrames(ID requestId, FAPFrame... frames) {
        sendFAPFrames(requestId, Arrays.asList(frames));
    }

    public void sendFAPFrames(ID requestId, List<FAPFrame> frames) {
        for (FAPFramePerformer performer : performers) {
            performer.performFAPFrames(frames, requestId);
        }
    }

    public void sendFAPFrame(ID requestId, FAPFrame frame) {
        sendFAPFrames(requestId, frame);
    }

    /**
     * Sends a message to cancel all the {@code FAPFrame} with the given {@code ID} to all linked FAPFramePerformer.
     * @param requestId ID of the frames to cancel
     */
    public void cancelFramesWithIDInLinkedPerformers(ID requestId) {
        for (FAPFramePerformer performer : performers) {
            if (performer instanceof CancelableFAPFramePerformer) {
                ((CancelableFAPFramePerformer) performer).cancelFAPFramesById(requestId);
            }
        }
    }
}
