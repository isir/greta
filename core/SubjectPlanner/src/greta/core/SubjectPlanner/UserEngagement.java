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
