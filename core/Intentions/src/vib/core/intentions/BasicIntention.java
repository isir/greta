/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.intentions;

import java.util.ArrayList;
import java.util.List;
import vib.core.util.time.TimeMarker;

/**
 * One specificity of those {@code Intentions} is that if theire endding {@code TimeMarker} are not specified,
 * they equals to {@code TimeMarker.INFINITY}.
 * @author Andre-Marie Pez
 * @see vib.core.intentions.Intention Intention
 * @see vib.core.util.time.TimeMarker#INFINITY
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

    public BasicIntention(String name, String id, String type, TimeMarker start, TimeMarker end, double importance){
        this.name = name;
        this.id = id;
        this.type = type;
        this.importance = importance;
        this.start = start;
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
