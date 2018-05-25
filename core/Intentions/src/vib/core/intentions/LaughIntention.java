/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.intentions;

import vib.core.util.laugh.Laugh;
import vib.core.util.time.TimeMarker;

/**
 *
 * @author Andre-Marie Pez
 * @author Yu Ding
 */
public class LaughIntention extends Laugh implements Intention{


    public LaughIntention(){
        super();
    }

    public LaughIntention(String id, TimeMarker start, TimeMarker end){
        super(id, start, end);
    }

    public LaughIntention(Laugh other){
        super(other);
    }

    @Override
    public String getName() {
        return "laugh";
    }

    @Override
    public String getType() {
        return "laugh";
    }

    @Override
    public double getImportance() {
        return 0.5;
    }

    @Override
    public boolean hasCharacter() {
        return false;
    }

    @Override
    public String getCharacter() {
        return null;
    }

}
