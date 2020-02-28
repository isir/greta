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

import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 * One specificity of those {@code Intentions} is that if theire endding {@code TimeMarker} are not specified,
 * they equals to {@code TimeMarker.INFINITY}.
 * @author Andre-Marie Pez
 * @see greta.core.intentions.Intention Intention
 * @see greta.core.util.time.TimeMarker#INFINITY
 */
public class BasicIntention implements Intention{

    private String name;
    private String id;
    private String type;
    private double importance;
    private TimeMarker start;
    private TimeMarker end;
    private List<TimeMarker> markers;
    private String character;
    private String mode;
    // add a variable to store the target to which gaze to
    private String Target;

    public BasicIntention(String name, String id, String type, TimeMarker start, TimeMarker end, double importance){
        this.name = name;
        this.id = id;
        this.type = type;
        this.importance = importance;
        this.start = start;
        this.Target = "";
        if(end != null) {
            this.end = end;
        }
        else{
            this.end = new TimeMarker("end");
            this.end.addReference(TimeMarker.INFINITY);
        }
        markers = new ArrayList<TimeMarker>();
        markers.add(this.start);
        markers.add(this.end);
    }

    public BasicIntention(String name, String id, String type, TimeMarker start, TimeMarker end){
        this(name, id, type, start, end, 0.5);
        this.Target = "";
    }

    // set the Target
    public void setTarget (String Target){
        this.Target = Target;
    }

    @Override
    public String getTarget (){
        return Target;
    }

    /**
     * Sets the importance of this {@code Intention}.
     * @param importance the value of importance to set
     */
    public void setImportance(double importance){
        this.importance = importance;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public double getImportance() {
        return importance;
    }

    @Override
    public List<TimeMarker> getTimeMarkers() {
        return markers;
    }

    @Override
    public TimeMarker getTimeMarker(String name) {
        if(name != null){
            if(name.equalsIgnoreCase("start")) {
                return start;
            }
            if(name.equalsIgnoreCase("end")) {
                return end;
            }
        }
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void schedule() {
        if(!start.isConcretized()) {
            start.setValue(0);
        }
        if(!end.isConcretized()) {
            end.setValue(TimeMarker.INFINITY.getValue());
        }
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean hasCharacter() {
        return character!=null;
    }

    @Override
    public String getCharacter() {
        return character;
    }

    public void setCharacter(String name){
        character = name;
    }


    @Override
    public TimeMarker getStart() {
        return start;
    }

    @Override
    public TimeMarker getEnd() {
        return end;
    }
}
