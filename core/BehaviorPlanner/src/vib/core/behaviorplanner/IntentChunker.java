/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorplanner;

import vib.core.intentions.Intention;
import vib.core.intentions.IntentionEmitter;
import vib.core.intentions.IntentionPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
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
