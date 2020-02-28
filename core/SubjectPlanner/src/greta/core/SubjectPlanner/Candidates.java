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

import greta.core.util.log.Logs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Nadine Glas
 */
public class Candidates {

    public static Candidates candidates = new Candidates();
    private final ObjectsInMind obj_mind;
    CharacteristicDistances CharDist = new CharacteristicDistances();
    AgentEngagement AgentEng = new AgentEngagement();
    UserEngagement UserEng = new UserEngagement();
    ArrayList<String> discussedObjects = new ArrayList();
    String Transition = "close";

    public Candidates() {

        obj_mind = new ObjectsInMind(ObjectsInMind.OBJECTS_IN_MIND);

    }

    public MuseumObject GetCurrentObject(String CurrentObjectName) {
        System.out.println("CurrentObjectName = " + CurrentObjectName);
        MuseumObject museumObject;
        for (MuseumObject obj : obj_mind.getAll()) {
            //           System.out.println(obj.getParamName());
            if (CurrentObjectName.equalsIgnoreCase(obj.getParamName())) {
                museumObject = new MuseumObject(obj);
                return museumObject;
            }
        }
        return null;
    }

    public String GetCurrentPeriod(String CurrentObjectNameUnderscore) {
        String CurrentObjectName = CurrentObjectNameUnderscore.replace("_", " ");
        MuseumObject CurrentObject = GetCurrentObject(CurrentObjectName);
        if (CurrentObject != null) {
            String CurrentPeriod = CurrentObject.period;
            System.out.println("CurrentPeriod = " + CurrentPeriod);
            if (CurrentPeriod != "") {
                return CurrentPeriod;
            } else {
                return "UnknownPeriod";
            }
        } else {
            return "UnknownPeriod";
        }
    }

    public String GetCurrentArtist(String CurrentObjectNameUnderscore) {
        String CurrentObjectName = CurrentObjectNameUnderscore.replace("_", " ");
        MuseumObject CurrentObject = GetCurrentObject(CurrentObjectName);
        if (CurrentObject != null) {
            String CurrentArtist = CurrentObject.artist;
            System.out.println("CurrentArtist = " + CurrentArtist);
            if (CurrentArtist != "") {
                return CurrentArtist;
            } else {
                return "UnknownArtist";
            }
        } else {
            return "UnknownArtist";
        }
    }

//    public boolean EngagementOrientationAndBoolean(String Characteristic, String CurrentInstance, String NewInstance) {
//        HashMap<String, Double> CharacteristicScore = new HashMap();
//        double distance = CharDist.GetDistanceFromCurrentInstance(Characteristic, CurrentInstance, NewInstance);
//        boolean EngagementOrientationAndBoolean;
//        //AgentOrientation decides if we select according to AgentEngagement or UserEngagement,
//        // which decides if we want something close to original instance of far.
//        if (Characteristic.equalsIgnoreCase(AgentEng.GetAgentOrientation())) {
//            EngagementOrientationAndBoolean = AgentEng.GetAgentEngagement();
//        } else {
//            EngagementOrientationAndBoolean = UserEng.GetUserEngagementBoolean();
//        }
//        return EngagementOrientationAndBoolean;
//    }
//
//    public HashMap<String, Double> GetCharacteristicScoreCandidate(String Characteristic, String CurrentInstance, String NewInstance) {
//        boolean EngagementValue = EngagementOrientationAndBoolean(Characteristic, CurrentInstance, NewInstance);
//        HashMap<String, Double> CharacteristicScore = new HashMap();
//        double distance = CharDist.GetDistanceFromCurrentInstance(Characteristic, CurrentInstance, NewInstance);
//        //Revert distances to scores
//        if (EngagementValue == false) {
//            CharacteristicScore.put(NewInstance, distance);
//            SetTransition("far");
//
//        } else if (EngagementValue == true) {
//            CharacteristicScore.put(NewInstance, 1 / distance);
//            SetTransition("close");
//        }
//        //     System.out.println(CharacteristicScore);
//        return CharacteristicScore;
//    }
    public HashMap<String, Double> GetListCharacteristicScoresCandidates(String Characteristic, String CurrentInstance) {
        HashMap<String, Double> CharacteristicScores = new HashMap();
        HashMap<String, Double> Distances = CharDist.GetListDistancesFromCurrentInstance(Characteristic, CurrentInstance);
//        System.out.println("distances: " + Distances);
        boolean EngagementValue;
        //AgentOrientation decides if we select according to AgentEngagement or UserEngagement,
        // which decides if we want something close to original instance of far.
        if (Characteristic.equalsIgnoreCase(AgentEng.GetAgentOrientation())) {
            EngagementValue = AgentEng.GetAgentEngagement();
        } else {
            EngagementValue = UserEng.GetUserEngagementBoolean();
        }
        //Revert distances to scores
        if (EngagementValue == false) {
            double max = Collections.max(Distances.values());
            for (Map.Entry<String, Double> entry : Distances.entrySet()) {
                entry.setValue(entry.getValue() / max);
            }
            CharacteristicScores = Distances;
            CharacteristicScores.put(CurrentInstance, 0.001);

        } else if (EngagementValue == true) {
            for (Map.Entry<String, Double> entry : Distances.entrySet()) {
                entry.setValue(1 / entry.getValue());
            }
            double max = Collections.max(Distances.values());
            for (Map.Entry<String, Double> entry : Distances.entrySet()) {
                entry.setValue(entry.getValue() / max);
            }
            CharacteristicScores = Distances;
            CharacteristicScores.put(CurrentInstance, 1.0);
        }
        //       System.out.println(CharacteristicScores);
        return CharacteristicScores;
    }

    public double GetCandidateValueForObject(MuseumObject CurrentObject, MuseumObject NewObject) {
        double valueType = 0;
        double valuePeriod = 0;
        if (NewObject.type.equalsIgnoreCase("")) {
            valueType = 0;
        } else {
            valueType = GetListCharacteristicScoresCandidates("type", CurrentObject.type).get(NewObject.type);
        }
        if (NewObject.period.equalsIgnoreCase("")) {
            valuePeriod = 0;
        } else {
            valuePeriod = GetListCharacteristicScoresCandidates("period", CurrentObject.period).get(NewObject.period);
        }
        double ValueForObject = valueType + valuePeriod;
        return ValueForObject;
    }

    public HashMap<String, Double> GetBestCandidates(MuseumObject CurrentObject) {
        HashMap<String, Double> CandidateValues = new HashMap();
        String concat = "";
        for (String discussedObj : discussedObjects) {
            concat = concat + discussedObj;
        }
        for (MuseumObject obj : obj_mind.getAll()) {
            if (!concat.contains(obj.getParamName())) {
                for (String discussedObj : discussedObjects) {
                    //if (!obj.getParamName().equalsIgnoreCase(discussedObj)) {
                    if (!obj.getParamName().contains(discussedObj)) {
                        double CandidateValueForObject = GetCandidateValueForObject(CurrentObject, obj);
                        CandidateValues.put(obj.name, CandidateValueForObject);
                    }
                }
            }
        }
        //    System.out.println("CANDIDATE VALUES " + CandidateValues);
        return CandidateValues;

    }

    public HashMap<String, Double> GetNewSubjectWithoutPreference(MuseumObject CurrentObject) {
        if (!discussedObjects.contains(CurrentObject.name)) {
            discussedObjects.add(CurrentObject.name);
        }
        HashMap<String, Double> NewSubject = new HashMap();
        HashMap<String, Double> CandidateValues = GetBestCandidates(CurrentObject);
        Double max = Collections.max(CandidateValues.values());
        for (Map.Entry<String, Double> entry : CandidateValues.entrySet()) {
            if (entry.getValue().equals(max)) {
                NewSubject.put(entry.getKey(), entry.getValue());
            }
        }
        System.out.println("Agent Engagement = " + AgentEng.GetAgentEngagement());
        System.out.println("User Engagement = " + UserEng.GetUserEngagementBoolean());
        System.out.println("Candidates without pref:" + CandidateValues);
        return NewSubject;
    }

    public HashMap<String, Double> GetNewSubjectsWithPreference(MuseumObject CurrentObject) {
        if (!discussedObjects.contains(CurrentObject.name)) {
            discussedObjects.add(CurrentObject.name);
        }
        //     discussedObjects.add(CurrentObject.name);
        HashMap<String, Double> NewSubjectMap = new HashMap();
        HashMap<String, Double> NewSubject = new HashMap();
        HashMap<String, Double> CandidateValues = GetBestCandidates(CurrentObject);
        for (Map.Entry<String, Double> entry : CandidateValues.entrySet()) {
            if (CurrentObject.period != "" && CurrentObject.type != "") {
                if (CandidateValues.containsValue(2.0)) {
                    if (entry.getValue() >= 2) {
                        ObjectPreferences objPrefAgent = new ObjectPreferences();
                        double objPref = objPrefAgent.GetObjectPreferenceAgent(entry.getKey());
                        double maxPref = objPrefAgent.GetMaxObjectPreferenceAgent();
                        //         System.out.println("maxPref = " + maxPref);
                        NewSubjectMap.put(entry.getKey(), entry.getValue() * (Math.abs(objPref) / maxPref));
                    }
                } else {
                    ObjectPreferences objPrefAgent = new ObjectPreferences();
                    double objPref = objPrefAgent.GetObjectPreferenceAgent(entry.getKey());
                    double maxPref = objPrefAgent.GetMaxObjectPreferenceAgent();
                    //         System.out.println("maxPref = " + maxPref);
                    NewSubjectMap.put(entry.getKey(), entry.getValue() * (Math.abs(objPref) / maxPref));
                }

            } else if (CurrentObject.period.equalsIgnoreCase("") || CurrentObject.type.equalsIgnoreCase("")) {
                if (entry.getValue() >= 1) {
                    ObjectPreferences objPrefAgent = new ObjectPreferences();
                    double objPref = objPrefAgent.GetObjectPreferenceAgent(entry.getKey());
                    double maxPref = objPrefAgent.GetMaxObjectPreferenceAgent();
                    //           System.out.println("maxPref = " + maxPref);
                    NewSubjectMap.put(entry.getKey(), entry.getValue() * (Math.abs(objPref) / maxPref));
                }
            }
        }
        //   System.out.println(CandidateValues);
        //   System.out.println(NewSubjectMap);
        double max = Collections.max(NewSubjectMap.values());
        for (Map.Entry<String, Double> entry : NewSubjectMap.entrySet()) {
            if (entry.getValue().equals(max)) {
                NewSubject.put(entry.getKey(), entry.getValue());
                String NewSubjectName = entry.getKey();
            }
        }

        System.out.println("Agent Engagement = " + AgentEng.GetAgentEngagement());
        System.out.println("User Engagement = " + UserEng.GetUserEngagementBoolean());
        System.out.println("Candidates with pref: " + NewSubjectMap);
        return NewSubject;
    }

    public String GetNewSubjectName(String CurrentObjectName) {
        HashMap<String, Double> NewSubjectsWithPreference = GetNewSubjectsWithPreference(GetCurrentObject(CurrentObjectName));
        Object newObject = NewSubjectsWithPreference.entrySet().toArray()[0];
        String newObjectName = newObject.toString().replaceFirst("=.*", "");
        System.out.println("CURRENTOBJ = " + CurrentObjectName + "NEW OBJECT =" + newObjectName);
        //  discussedObjects.add(newObjectName);
        //  System.out.println("discussed objects: " + discussedObjects);
        return (newObjectName);
        //   return ("Saint Louis Marie Grignon de Montfort");

    }

    public String GetNewSubjectNameUnderscores(String CurrentObjectNameUnderscore) {
        String CurrentObjectName = CurrentObjectNameUnderscore.replace("_", " ");
        String NewSubjectName = GetNewSubjectName(CurrentObjectName);
        String NewSubjectNameUnderscore = NewSubjectName.replace(" ", "_");
        return NewSubjectNameUnderscore;
    }

    //  public String GetNewSubjectName(String blablb) {
    //    String SubjectName = "Saint Louis Marie Grignon de Montfort";
    //  return SubjectName;
    //}
    public String ContinueSubject() {
        String ContinueOrStop;
        if (UserEng.GetUserEngagementBoolean()) {
            ContinueOrStop = "continue";
        } else {
            ContinueOrStop = "stop";
        }
        Logs.debug("Returned value for ContinueSubject = " + Transition);
        return ContinueOrStop;
    }

    public void SetTransition(String Transition_) {
        Transition = Transition_;
    }

    public String GetTransition() {
        //Temporal solution. In the future let transition be based on distance to current object.
        if (UserEng.GetUserEngagementBoolean()) {
            Transition = "close";
        } else {
            Transition = "far";
        }
        Logs.debug("Returned value for Get Transition = " + Transition);

        return Transition;
    }

    public int GetPolitenessStrat() {
        int PolitenessStrat = 3;
        return PolitenessStrat;
    }
    //  public HashMap<String, Double> GetNewSubjectsWithPreference(MuseumObject CurrentObject) {
    //        HashMap<String, Double> CandidateScores = GetNewSubjectWithoutPreference(MuseumObject CurrentObject);
    //       for (Map.Entry<String, Double> entry : CandidateScores.entrySet()) {
    //          if
    //          obj_pref_agent
    //     }
    //  }
    //   }
}
