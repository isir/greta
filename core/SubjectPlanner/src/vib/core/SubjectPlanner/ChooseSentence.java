package vib.core.SubjectPlanner;

import vib.core.util.parameter.EngineParameter;
import vib.core.util.parameter.EngineParameterSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Nadine
 */
public class ChooseSentence {

    private final Sentences sentences;

    public ChooseSentence() {

        sentences = new Sentences(Sentences.SENTENCE_LIB);
    }

    public String GetSentence(String intention, String ObjectName, PolitenessStrategy PolStrat, ObjectPreferences ObjPref) {
        String noSentence = "";
//        PolitenessStrategy PolStrat = new PolitenessStrategy();
//        ObjectPreferences ObjPref = new ObjectPreferences();
        double AgentPref = ObjPref.GetObjectPreferenceAgent(ObjectName);
        double UserPref = ObjPref.GetObjectPreferenceUser(ObjectName);
        System.out.println("Agent Pref = " + AgentPref);
        System.out.println("User Pref = " + UserPref);
        double politenessStrategy = PolStrat.GetPolitenessStrategy(AgentPref, UserPref);
        System.out.println("Politeness Strategy = " + politenessStrategy);
        EngineParameterSet utterances = sentences.find(intention);
        for (EngineParameter utterance : utterances.getAll()) {
            if ((utterance.getMin() <= AgentPref) && (utterance.getMax() >= AgentPref)) {
                if (utterance.getValue() == politenessStrategy) {
                    String candidateSentence = utterance.getParamName();
                    String nextSentence = candidateSentence.replace("this object", ObjectName);
                    return nextSentence;
                } else {
                    noSentence = "Too big of a threat to talk about this object";
                }
            } else {
                noSentence = "No sentence found";
            }
        }
        return noSentence;
    }   
 
}