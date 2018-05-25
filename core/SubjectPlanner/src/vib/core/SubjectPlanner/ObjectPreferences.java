/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.SubjectPlanner;

import java.util.ArrayList;
import java.util.Collections;
import vib.core.util.parameter.EngineParameter;
import vib.core.util.parameter.EngineParameterSet;

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
