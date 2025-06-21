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
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Nadine
 */
public class ObjectPreferences {

    private final AgentPreferenceObject agentPreferenceObject;
    private final UserPreferenceObject userPreferenceObject;

    public ObjectPreferences() {

        agentPreferenceObject = new AgentPreferenceObject(AgentPreferenceObject.AGENT_OBJ_PREF);
        userPreferenceObject = new UserPreferenceObject(UserPreferenceObject.OBJECT_PREF_USER_LIB);
    }

    public double GetObjectPreferenceAgent(String ObjectName) {
        double ObjectPreferenceAgent = 0;
        EngineParameterSet prefs = agentPreferenceObject.find("preferences");
        for (EngineParameter pref : prefs.getAll()) {
            if (pref.getParamName().equalsIgnoreCase(ObjectName)) {
                ObjectPreferenceAgent = pref.getValue();
            }
        }
        return ObjectPreferenceAgent;
    }

    public double GetMaxObjectPreferenceAgent(){
        double max = 0;
        ArrayList<Double> prefValues = new ArrayList();
        EngineParameterSet prefs = agentPreferenceObject.find("preferences");
        for (EngineParameter pref : prefs.getAll()){
            prefValues.add(Math.abs(pref.getValue()));
        }
        return Collections.max(prefValues);
    }

        public double GetObjectPreferenceUser(String ObjectName) {
        double ObjectPreferenceUser = 0;
        EngineParameterSet prefs = userPreferenceObject.find("preferences");
        for (EngineParameter pref : prefs.getAll()) {
            if (pref.getParamName().equalsIgnoreCase(ObjectName)) {
                ObjectPreferenceUser = pref.getValue();
            }
        }
        return ObjectPreferenceUser;
    }

}
