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

import java.util.HashMap;

/**
 *
 * @author Nadine
 */
public class TestSubjectPlanner {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // TODO code application logic here

        CharacteristicDistances CharDist = new CharacteristicDistances();
        Candidates candidates = new Candidates();
        ObjectPreferences objPrefAgent = new ObjectPreferences();
        UserEngagement userEng = new UserEngagement();
        ChooseSentence ChooseSen = new ChooseSentence();
        PolitenessStrategy PolitenessStrat = new PolitenessStrategy();
        PolitenessStrat.userEng = userEng;


        String currentObjectName = "Soldat bandant son arc";
        //Engagement between -5 and 5
    //    userEng.UserEngagementValue = -5;
        //userEng.SetUserEngagementValue(-5);


        for (int objNr = 1; objNr <= 6; objNr++) {

            MuseumObject currentObj = candidates.GetCurrentObject(currentObjectName);
            System.out.println(currentObj);

            if (currentObj != null) {
                HashMap<String, Double> bar = candidates.GetNewSubjectWithoutPreference(currentObj);
                System.out.println("New subject without pref:" + bar);
            } else {
                System.out.println("L'objet " + currentObjectName + " n'est pas dans la liste");
            }

            HashMap<String, Double> biz = new HashMap();
            if (currentObj != null) {
                biz = candidates.GetNewSubjectsWithPreference(currentObj);
                System.out.println("New subject with pref:" + biz);
            } else {
                System.out.println("L'objet " + currentObjectName + " n'est pas dans la liste");
            }

       //     Object newObject = biz.entrySet().toArray()[0];
       //     String newObjectName = newObject.toString().replaceFirst("=.*", "");
      //      System.out.println(newObjectName);

          //  System.out.println("NEW SUBJECT NAME = "+ candidates.GetNewSubjectName("Soldat bandant son arc"));
          //  System.out.println(candidates.GetNewSubjectString(currentObj));


            //For now I consider user engagement and user preference to be equal.
      //      System.out.println(ChooseSen.GetSentence("ExpressOpinion", newObjectName, PolitenessStrat, objPrefAgent));

     //       currentObjectName = newObjectName;
            userEng.UserEngagementValue = userEng.UserEngagementValue+2;

            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }


        }
    }
}
