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
package greta.core.behaviorplanner;

import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
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
