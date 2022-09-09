package greta.auxiliary.incrementality;

/**
 *
 * @author Sean
 */
public interface IncrementalityInteractionEmitter {

    void addIncInteractionPerformer(IncrementalityInteractionPerformer performer);

    void removeIncInteractionPerformer(IncrementalityInteractionPerformer performer);
}
