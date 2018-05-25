/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.fml.timelines;

import vib.core.behaviorplanner.lexicon.BehaviorSet;
import vib.core.behaviorplanner.lexicon.Lexicon;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Andre-Marie
 */
public class IntentionsAviable {

    private static void getNameButNotStar(String fullName, String functionName, List<String> whereAdd){
        if(fullName.startsWith("SIMPLE:"+functionName)){
            String functionInstanceName = fullName.substring("SIMPLE:".length()+functionName.length()+1);
            if( ! functionInstanceName.equals("*") && ! whereAdd.contains(functionInstanceName)){
                whereAdd.add(functionInstanceName);
                Collections.sort(whereAdd, String.CASE_INSENSITIVE_ORDER);
            }
        }
    }
    static {
        Lexicon l = new Lexicon();
        List<String> emotions = new LinkedList<String>();
        List<String> performatives = new LinkedList<String>();
        List<String> backchannels = new LinkedList<String>();
        List<String> belief_relaions = new LinkedList<String>();
        List<String> certainties = new LinkedList<String>();
        for(BehaviorSet bs : l.getAll()){
            String fullName = bs.getParamName().toUpperCase();
            getNameButNotStar(fullName, "EMOTION", emotions);
            getNameButNotStar(fullName, "PERFORMATIVE", performatives);
            getNameButNotStar(fullName, "BACKCHANNEL", backchannels);
            getNameButNotStar(fullName, "BELIEF-RELATION", belief_relaions);
            getNameButNotStar(fullName, "CERTAINTY", certainties);
        }
        EMOTIONS = emotions.toArray(new String[emotions.size()]);
        PERFORMATIVES = performatives.toArray(new String[performatives.size()]);
        BACKCHANNELS = backchannels.toArray(new String[backchannels.size()]);
        BELIEF_RELATIONS = belief_relaions.toArray(new String[belief_relaions.size()]);
        //ensure certain and uncertain
        getNameButNotStar("SIMPLE:-CERTAIN", "", certainties);
        getNameButNotStar("SIMPLE:-UNCERTAIN", "", certainties);
        CERTAINTIES = certainties.toArray(new String[certainties.size()]);
    }

    public static String[] getAviableFor(String functionName){
        if(functionName.equalsIgnoreCase("emotion")) return EMOTIONS;
        if(functionName.equalsIgnoreCase("performative")) return PERFORMATIVES;
        if(functionName.equalsIgnoreCase("backchannel")) return BACKCHANNELS;
        if(functionName.equalsIgnoreCase("belief-relation")) return BELIEF_RELATIONS;
        if(functionName.equalsIgnoreCase("certainty")) return CERTAINTIES;
        if(functionName.equalsIgnoreCase("world-ref")) return WORLDS_REFERENCES;
        if(functionName.equalsIgnoreCase("world-prop")) return WORLDS_PROPERTIES;
        return new String[0];
    }

    public static final String[] EMOTIONS;
    public static final String[] PERFORMATIVES;
    public static final String[] BACKCHANNELS;
    public static final String[] BELIEF_RELATIONS;

    public static final String[] CERTAINTIES
//            = {"CERTAIN", "UNCERTAIN"}
            ;

    public static final String[] WORLDS_REFERENCES = {
        "EVENT",
        "OBJECT",
        "PERSON",
        "PLACE"
    };

    public static final String[] WORLDS_PROPERTIES ={
        "DURATION",
        "LOCATION",
        "QUANTITY",
        "SHAPE"
    };
}
