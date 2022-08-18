package greta.auxiliary.incrementality;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Sean
 */
public class IncRealizerInteraction implements IncrementalityInteractionEmitter {

    private List<IncrementalityInteractionPerformer> performerList = new ArrayList<>();

    @Override
    public void addIncInteractionPerformer(IncrementalityInteractionPerformer performer) {
        performerList.add(performer);
    }

    @Override
    public void removeIncInteractionPerformer(IncrementalityInteractionPerformer performer) {
        performerList.remove(performer);
    }

    public void sendInteruption() {
        for (IncrementalityInteractionPerformer pf : performerList) {
            pf.performIncInteraction("interupt");
        }
    }

    public void sendResume(){
        for (IncrementalityInteractionPerformer pf : performerList) {
            pf.performIncInteraction("resume");
        }
    }
}
