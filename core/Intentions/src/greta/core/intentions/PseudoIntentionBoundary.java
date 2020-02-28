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
package greta.core.intentions;

import greta.core.util.speech.Boundary;
import greta.core.util.time.TimeMarker;
import java.util.List;

/**
 * This class allows the Intent planner (or other) to use {@code greta.core.util.speech.Boundary} has an intention
 * @author Andre-Marie Pez
 * @see greta.core.util.speech.Boundary
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

    @Override
    public String getTarget (){
        return null;
    }

}
