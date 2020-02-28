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
package greta.core.SubjectPlanner;

import greta.core.util.parameter.EngineParameter;
import greta.core.util.parameter.EngineParameterSet;

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
