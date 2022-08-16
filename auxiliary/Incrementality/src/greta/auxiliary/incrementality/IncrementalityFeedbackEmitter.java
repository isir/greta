package greta.auxiliary.incrementality;

/**
 *
 * @author Sean
 */
public interface IncrementalityFeedbackEmitter {

    void addIncFeedbackPerformer(IncrementalityFeedbackPerformer performer);
    
    void removeIncFeedbackPerformer(IncrementalityFeedbackPerformer performer);
}
