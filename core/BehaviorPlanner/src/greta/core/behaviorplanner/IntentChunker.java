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
package greta.core.behaviorplanner;

import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.Signal;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * This class divides a list of {@code Intention} into several list of
 * {@code Intention} (chunks).<br/> Each chunk will be sent to all
 * {@code IntentionPerformer} added.
 *
 * @author Andre-Marie Pez
 */
public class IntentChunker implements IntentionPerformer, IntentionEmitter {

    private ArrayList<IntentionPerformer> performers;

    public IntentChunker() {
        performers = new ArrayList<IntentionPerformer>();
    }

    /**
     * Divide a list of {@code Intention} into chunks (cut speech at each
     * boundaries in interaction mode, else only at points).<br/> Chunks are
     * more little lists of {@code Intention}.<br/> All created chunks are send
     * to all added {@code IntentionPerformer}.
     *
     * @param intentions
     * @param requestId
     */
    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode) {
        //TODO implement : divide then use the send funtion for all chunk created.
        ID chunkID = IDProvider.createID("IntentChunker", requestId);
        send(intentions, chunkID, mode);//pass all for now
    }

    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode, List<Signal> inputSignals){
        
    };
    
    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        if (performer != null && performer != this) {
            performers.add(performer);
        }
    }

    private void send(List<Intention> chunks, ID id, Mode mode) {
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(chunks, id, mode);
        }
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }
}
