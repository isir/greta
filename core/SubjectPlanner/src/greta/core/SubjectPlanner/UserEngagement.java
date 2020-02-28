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

/**
 *
 * @author Nadine
 */
public class UserEngagement //implements SpeechRecognizerResultPerformer {
{
    //EngagementValue between -5 and 5.

    public double UserEngagementValue = 5;
    public boolean UserEngagementBoolean;
    private double UserEngagementWeight = 1;
    private SubjectPlanner subjPlanner;

    public void setSubjectPlanner(SubjectPlanner subjPlanner) {
        this.subjPlanner = subjPlanner;
    }

    public void SetUserEngagementValue(double userEngagement) {
        UserEngagementValue = userEngagement;
        //SetUserEngagementBoolean(UserEngagementValue);
        Logs.debug("New EngValue = " + GetUserEngagementValue());

    }

    public double GetUserEngagementValue() {
        return UserEngagementValue;
    }

//    public void SetUserEngagementBoolean(double UserEngagementValue) {
//        if (UserEngagementValue >= 0) {
//            UserEngagementBoolean = true;
//        } else {
//            UserEngagementBoolean = false;
//        }
//        Logs.debug("New EngBoolean = " + UserEngagementBoolean);
//
//    }
    public void SetUserEngagementWeight(boolean userEngagementWeight) {
        this.UserEngagementWeight = UserEngagementWeight;
    }

    public boolean GetUserEngagementBoolean() {
        if (UserEngagementValue >= 0) {
            UserEngagementBoolean = true;
        } else {
            UserEngagementBoolean = false;
        }
        return UserEngagementBoolean;
    }

    public double GetUserEngagementWeight() {
        return UserEngagementWeight;
    }

    //@Override
    public void performSpeechRecognizerResult(String string, double d) {
        if (string != null) {
            if (string.matches("[0-9]*.+[0-9]*")) {
                double EngValue = Double.parseDouble(string);
                Logs.debug("Entered Eng value = " + EngValue);
                SetUserEngagementValue(EngValue);
//            Pattern pattern = Pattern.compile("Eng.value = .*");
//            Matcher m = pattern.matcher(string);
//            while (m.find()) {
//                String EngValueString = m.group(1);
//                EngValueString.replace("Eng.value = ", "");
//                double EngValue = Double.parseDouble(EngValueString);
//                SetUserEngagementValue(EngValue);
            } else {
                //any other input then "if" above, makes Strclient and SampleThriftZmq from A11 integration demo stop.
                //So far the else statement does not solve this.
                Logs.debug(string);
            }
        }
    }
}
