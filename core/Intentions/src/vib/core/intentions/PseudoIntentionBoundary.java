/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.intentions;

import vib.core.util.speech.Boundary;
import vib.core.util.time.TimeMarker;
import java.util.List;

/**
 * This class allows the Intent planner (or other) to use {@code vib.core.util.speech.Boundary} has an intention
 * @author Andre-Marie Pez
 * @see vib.core.util.speech.Boundary
 */
public class PseudoIntentionBoundary extends Boundary implements Intention{

    public PseudoIntentionBoundary(Boundary b){
        super(b);
        //we add a fake TimeMarker to force the Temporizer to call the schedule function
        getTimeMarkers().add(new TimeMarker("#only used to offset#"));
    }

    @Override
    public String getName() {
        return "boundary";
    }

    @Override
    public String getType() {
        return Boundary.stringOfType(this.getBoundaryType());
    }

    @Override
    public double getImportance() {
        return 0.5;
    }

    @Override
    public void schedule() {
        //The signals associated to a boundary must end at the start of the boundary
        //(CommunicativeIntention.cpp ner line 150)
        //so, we need to offset this pseudo intention :

        //call the super implementation
        super.schedule();
        //here, all TimeMarkers are concretized

        //markers are offset by the duration
        List<TimeMarker> markers = getTimeMarkers();
        //there is only 3 TimeMarker inside :
        //0 : start
        //1 : end
        //2 : fake TimeMarker

        //start
        markers.set(0, new TimeMarker("start", Math.max(0.0,markers.get(0).getValue()-duration)));
        //end
        markers.set(1, new TimeMarker("end", Math.max(0.0,markers.get(1).getValue()-duration)));
        //update the duration
        duration = markers.get(1).getValue()-markers.get(0).getValue();

        //set by default the end value to the fake TimeMarker
        markers.get(2).setValue(markers.get(1).getValue());
    }

    @Override
    public boolean hasCharacter() {
        return false;
    }

    @Override
    public String getCharacter() {
        return null;
    }

    public String getMode() {
        return "replace";
    }
}
