/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.intentions;

import vib.core.util.time.TimeMarker;

/**
 *
 * @author Brice Donval
 */
public class IdeationalUnitIntention extends BasicIntention {

    private String mainIntentionId;

    public IdeationalUnitIntention(String id, TimeMarker start, TimeMarker end) {
        super("ideationalunit", id, null, start, end);
    }

    public String getMainIntentionId() {
        return mainIntentionId;
    }

    public void setMainIntentionId(String intentionId) {
        mainIntentionId = intentionId;
    }

}
