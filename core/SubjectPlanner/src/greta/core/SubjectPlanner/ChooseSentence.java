/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
